package ltd.opens.mg.mc.client.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ltd.opens.mg.mc.MaingraphforMC;
import ltd.opens.mg.mc.MaingraphforMCClient;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeRegistry;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlueprintScreen extends Screen {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path dataFile;
    private float panX = 0;
    private float panY = 0;
    private float zoom = 1.0f;

    private List<GuiNode> nodes = new ArrayList<>();
    private List<GuiConnection> connections = new ArrayList<>();
    
    private GuiNode draggingNode = null;
    private float dragOffsetX, dragOffsetY;

    private GuiNode connectionStartNode = null;
    private String connectionStartPort = null;
    private boolean isConnectionFromInput = false;

    private boolean isPanning = false;
    private double lastMouseX, lastMouseY;
    
    private boolean showNodeMenu = false;
    private double menuX, menuY;

    // Focus management for input fields
    private GuiNode focusedNode = null;
    private String focusedPort = null;
    private int cursorTick = 0;

    public BlueprintScreen() {
        super(Component.literal("Blueprint Editor"));
        this.dataFile = MaingraphforMCClient.getBlueprintPath();
        load();
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.literal("Save"), (btn) -> save())
            .bounds(5, 5, 50, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Reset View"), (btn) -> {
            panX = 0;
            panY = 0;
            zoom = 1.0f;
        }).bounds(60, 5, 80, 20).build());
    }

    private void save() {
        try {
            JsonObject root = new JsonObject();
            
            // Execution data for BlueprintEngine
            JsonArray execution = new JsonArray();
            for (GuiNode node : nodes) {
                JsonObject nodeObj = new JsonObject();
                nodeObj.addProperty("id", node.id);
                nodeObj.addProperty("type", node.typeId);
                
                JsonObject inputs = new JsonObject();
                for (NodeDefinition.PortDefinition port : node.definition.inputs()) {
                    GuiConnection conn = findConnectionTo(node, port.name());
                    JsonObject input = new JsonObject();
                    if (conn != null) {
                        input.addProperty("type", "link");
                        input.addProperty("nodeId", conn.from.id);
                        input.addProperty("socket", conn.fromPort);
                    } else {
                        input.addProperty("type", "value");
                        JsonElement val = node.inputValues.get(port.name());
                        input.add("value", val != null ? val : GSON.toJsonTree(port.defaultValue()));
                    }
                    inputs.add(port.name(), input);
                }
                nodeObj.add("inputs", inputs);

                JsonObject outputs = new JsonObject();
                for (NodeDefinition.PortDefinition port : node.definition.outputs()) {
                    JsonArray targets = new JsonArray();
                    for (GuiConnection conn : connections) {
                        if (conn.from == node && conn.fromPort.equals(port.name())) {
                            JsonObject target = new JsonObject();
                            target.addProperty("nodeId", conn.to.id);
                            target.addProperty("socket", conn.toPort);
                            targets.add(target);
                        }
                    }
                    outputs.add(port.name(), targets);
                }
                nodeObj.add("outputs", outputs);
                execution.add(nodeObj);
            }
            root.add("execution", execution);

            // UI data for restoration
            JsonObject ui = new JsonObject();
            JsonArray nodesArray = new JsonArray();
            for (GuiNode node : nodes) {
                JsonObject nodeObj = new JsonObject();
                nodeObj.addProperty("id", node.id);
                nodeObj.addProperty("defId", node.typeId);
                nodeObj.addProperty("x", node.x);
                nodeObj.addProperty("y", node.y);
                nodeObj.add("inputValues", node.inputValues);
                nodesArray.add(nodeObj);
            }
            ui.add("nodes", nodesArray);

            JsonArray connArray = new JsonArray();
            for (GuiConnection conn : connections) {
                JsonObject connObj = new JsonObject();
                connObj.addProperty("fromNode", conn.from.id);
                connObj.addProperty("fromSocket", conn.fromPort);
                connObj.addProperty("toNode", conn.to.id);
                connObj.addProperty("toSocket", conn.toPort);
                connArray.add(connObj);
            }
            ui.add("connections", connArray);
            root.add("ui", ui);

            String json = new GsonBuilder().setPrettyPrinting().create().toJson(root);
            Files.writeString(this.dataFile, json);
            MaingraphforMC.LOGGER.info("Saved blueprint to {}", this.dataFile.toAbsolutePath());
        } catch (Exception e) {
            MaingraphforMC.LOGGER.error("Failed to save blueprint", e);
        }
    }

    private GuiConnection findConnectionTo(GuiNode node, String portName) {
        for (GuiConnection conn : connections) {
            if (conn.to == node && conn.toPort.equals(portName)) {
                return conn;
            }
        }
        return null;
    }

    private void load() {
        try {
            if (!Files.exists(this.dataFile)) return;
            String json = Files.readString(this.dataFile);
            if (json == null || json.isEmpty()) return;
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            
            nodes.clear();
            connections.clear();
            
            Map<String, GuiNode> nodeMap = new HashMap<>();
            
            JsonObject ui = root.has("ui") ? root.getAsJsonObject("ui") : root;

            if (ui.has("nodes")) {
                for (JsonElement e : ui.getAsJsonArray("nodes")) {
                    JsonObject obj = e.getAsJsonObject();
                    String type = obj.has("defId") ? obj.get("defId").getAsString() : obj.get("type").getAsString();
                    NodeDefinition def = NodeRegistry.get(type);
                    if (def != null) {
                        GuiNode node = new GuiNode(def, obj.get("x").getAsFloat(), obj.get("y").getAsFloat());
                        node.id = obj.get("id").getAsString();
                        if (obj.has("inputValues")) {
                            node.inputValues = obj.getAsJsonObject("inputValues");
                        }
                        nodes.add(node);
                        nodeMap.put(node.id, node);
                    }
                }
            }
            
            if (ui.has("connections")) {
                for (JsonElement e : ui.getAsJsonArray("connections")) {
                    JsonObject obj = e.getAsJsonObject();
                    String fromId = obj.has("fromNode") ? obj.get("fromNode").getAsString() : obj.get("from").getAsString();
                    String toId = obj.has("toNode") ? obj.get("toNode").getAsString() : obj.get("to").getAsString();
                    String fromPort = obj.has("fromSocket") ? obj.get("fromSocket").getAsString() : obj.get("fromPort").getAsString();
                    String toPort = obj.has("toSocket") ? obj.get("toSocket").getAsString() : obj.get("toPort").getAsString();
                    
                    GuiNode from = nodeMap.get(fromId);
                    GuiNode to = nodeMap.get(toId);
                    if (from != null && to != null) {
                        connections.add(new GuiConnection(from, fromPort, to, toPort));
                    }
                }
            }
        } catch (Exception e) {
            MaingraphforMC.LOGGER.error("Failed to load blueprint", e);
        }
    }

    @Override
    public void tick() {
        super.tick();
        cursorTick++;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        drawGrid(guiGraphics);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(panX, panY);
        guiGraphics.pose().scale(zoom, zoom);

        drawConnections(guiGraphics);

        for (GuiNode node : nodes) {
            node.render(guiGraphics, this.font, mouseX, mouseY, panX, panY, zoom, connections);
        }

        if (connectionStartNode != null) {
            float[] startPos = connectionStartNode.getPortPositionByName(connectionStartPort, isConnectionFromInput);
            drawBezier(guiGraphics, startPos[0], startPos[1], (float) ((mouseX - panX) / zoom), (float) ((mouseY - panY) / zoom), 0x88FFFFFF);
        }

        guiGraphics.pose().popMatrix();

        // UI Overlay
        guiGraphics.drawString(font, "Nodes: " + nodes.size() + " | Connections: " + connections.size(), 5, height - 15, 0xFFAAAAAA, false);
        guiGraphics.drawString(font, "Right click to add. Drag ports to connect. DEL to delete.", 150, 10, 0xFF888888, false);

        if (showNodeMenu) {
            renderNodeMenu(guiGraphics, mouseX, mouseY);
        }
        
        if (showNodeContextMenu) {
            renderNodeContextMenu(guiGraphics, mouseX, mouseY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderNodeContextMenu(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (int) menuX;
        int y = (int) menuY;
        int width = 100;
        int height = 40;
        
        guiGraphics.fill(x, y, x + width, y + height, 0xEE1A1A1A);
        guiGraphics.renderOutline(x, y, width, height, 0xFFFFFFFF);
        
        // Delete Node
        boolean hoverDelete = mouseX >= x && mouseX <= x + width && mouseY >= y + 5 && mouseY <= y + 20;
        guiGraphics.drawString(font, "Delete Node", x + 5, y + 7, hoverDelete ? 0xFFFFFF00 : 0xFFFFFFFF, false);
        
        // Break Links
        boolean hoverBreak = mouseX >= x && mouseX <= x + width && mouseY >= y + 20 && mouseY <= y + 35;
        guiGraphics.drawString(font, "Break Links", x + 5, y + 22, hoverBreak ? 0xFFFFFF00 : 0xFFFFFFFF, false);
    }

    private String hoveredCategory = null;

    private void renderNodeMenu(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (int) menuX;
        int y = (int) menuY;
        int width = 100;
        
        Map<String, List<NodeDefinition>> categories = new HashMap<>();
        for (NodeDefinition def : NodeRegistry.getAll()) {
            categories.computeIfAbsent(def.category(), k -> new ArrayList<>()).add(def);
        }
        
        List<String> sortedCategories = new ArrayList<>(categories.keySet());
        sortedCategories.sort(String::compareTo);
        
        int height = sortedCategories.size() * 15 + 10;
        guiGraphics.fill(x, y, x + width, y + height, 0xEE1A1A1A);
        guiGraphics.renderOutline(x, y, width, height, 0xFFFFFFFF);
        
        String currentHoveredCat = null;
        for (int i = 0; i < sortedCategories.size(); i++) {
            String cat = sortedCategories.get(i);
            int itemY = y + 5 + i * 15;
            boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + 15;
            int color = hovered ? 0xFFFFFF00 : 0xFFFFFFFF;
            guiGraphics.drawString(font, cat, x + 5, itemY + 2, color, false);
            
            if (hovered) {
                currentHoveredCat = cat;
            }
        }
        
        if (currentHoveredCat != null) {
            hoveredCategory = currentHoveredCat;
        }
        
        if (hoveredCategory != null) {
            List<NodeDefinition> catNodes = categories.get(hoveredCategory);
            int subX = x + width;
            int subY = y + sortedCategories.indexOf(hoveredCategory) * 15;
            int subWidth = 120;
            int subHeight = catNodes.size() * 15 + 10;
            
            guiGraphics.fill(subX, subY, subX + subWidth, subY + subHeight, 0xEE1A1A1A);
            guiGraphics.renderOutline(subX, subY, subWidth, subHeight, 0xFFFFFFFF);
            
            for (int i = 0; i < catNodes.size(); i++) {
                NodeDefinition def = catNodes.get(i);
                int itemY = subY + 5 + i * 15;
                boolean hovered = mouseX >= subX && mouseX <= subX + subWidth && mouseY >= itemY && mouseY <= itemY + 15;
                int color = hovered ? 0xFFFFFF00 : 0xFFFFFFFF;
                guiGraphics.drawString(font, def.name(), subX + 5, itemY + 2, color, false);
            }
        }
    }

    private void drawGrid(GuiGraphics guiGraphics) {
        int width = this.width;
        int height = this.height;
        float scaledGridSize = 20 * zoom;
        int color = 0xFF262626;
        int secondaryColor = 0xFF1A1A1A;

        // Fill background first
        guiGraphics.fill(0, 0, width, height, 0xFF121212);

        float startX = panX % scaledGridSize;
        if (startX > 0) startX -= scaledGridSize;
        float startY = panY % scaledGridSize;
        if (startY > 0) startY -= scaledGridSize;

        for (float x = startX; x < width; x += scaledGridSize) {
            guiGraphics.fill((int) x, 0, (int) x + 1, height, color);
        }
        for (float y = startY; y < height; y += scaledGridSize) {
            guiGraphics.fill(0, (int) y, width, (int) y + 1, color);
        }
        
        // Draw larger grid lines every 5 small grid squares
        float largeGridSize = scaledGridSize * 5;
        float largeStartX = panX % largeGridSize;
        if (largeStartX > 0) largeStartX -= largeGridSize;
        float largeStartY = panY % largeGridSize;
        if (largeStartY > 0) largeStartY -= largeGridSize;
        
        int largeColor = 0xFF333333;
        for (float x = largeStartX; x < width; x += largeGridSize) {
            guiGraphics.fill((int) x, 0, (int) x + 1, height, largeColor);
        }
        for (float y = largeStartY; y < height; y += largeGridSize) {
            guiGraphics.fill(0, (int) y, width, (int) y + 1, largeColor);
        }
    }

    private void drawConnections(GuiGraphics guiGraphics) {
        for (GuiConnection conn : connections) {
            float[] outPos = conn.from.getPortPositionByName(conn.fromPort, false);
            float[] inPos = conn.to.getPortPositionByName(conn.toPort, true);
            drawBezier(guiGraphics, outPos[0], outPos[1], inPos[0], inPos[1], 0xFFFFFFFF);
        }
    }

    private void drawBezier(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, int color) {
        float dist = Math.max(Math.abs(x2 - x1) * 0.5f, 30);
        float cp1x = x1 + dist;
        float cp1y = y1;
        float cp2x = x2 - dist;
        float cp2y = y2;

        int segments = 30; // Increased segments for smoother curves
        float lastX = x1;
        float lastY = y1;

        for (int i = 1; i <= segments; i++) {
            float t = i / (float) segments;
            float invT = 1.0f - t;
            float x = invT * invT * invT * x1 + 3 * invT * invT * t * cp1x + 3 * invT * t * t * cp2x + t * t * t * x2;
            float y = invT * invT * invT * y1 + 3 * invT * invT * t * cp1y + 3 * invT * t * t * cp2y + t * t * t * y2;
            
            // Draw a slightly thicker line by drawing it twice or using a different method
            // For now, we'll stick to simple lines but more of them
            drawLine(guiGraphics, lastX, lastY, x, y, color);
            lastX = x;
            lastY = y;
        }
    }

    private void drawLine(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, int color) {
        int ix1 = (int) x1;
        int iy1 = (int) y1;
        int ix2 = (int) x2;
        int iy2 = (int) y2;
        
        if (ix1 == ix2 && iy1 == iy2) return;
        
        if (Math.abs(ix1 - ix2) > Math.abs(iy1 - iy2)) {
            if (ix1 > ix2) {
                int tmp = ix1; ix1 = ix2; ix2 = tmp;
                tmp = iy1; iy1 = iy2; iy2 = tmp;
            }
            for (int x = ix1; x <= ix2; x++) {
                float t = (ix2 == ix1) ? 0 : (x - ix1) / (float) (ix2 - ix1);
                int y = (int) (iy1 + t * (iy2 - iy1));
                guiGraphics.fill(x, y, x + 1, y + 2, color);
            }
        } else {
            if (iy1 > iy2) {
                int tmp = ix1; ix1 = ix2; ix2 = tmp;
                tmp = iy1; iy1 = iy2; iy2 = tmp;
            }
            for (int y = iy1; y <= iy2; y++) {
                float t = (iy2 == iy1) ? 0 : (y - iy1) / (float) (iy2 - iy1);
                int x = (int) (ix1 + t * (ix2 - ix1));
                guiGraphics.fill(x, y, x + 2, y + 1, color);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        float zoomSensitivity = 0.1f;
        float oldZoom = zoom;
        if (scrollY > 0) {
            zoom *= (1 + zoomSensitivity);
        } else {
            zoom /= (1 + zoomSensitivity);
        }
        
        // Zoom limits
        zoom = Math.max(0.1f, Math.min(3.0f, zoom));
        
        if (zoom != oldZoom) {
            // Adjust pan to zoom towards mouse position
            double worldMouseX = (mouseX - panX) / oldZoom;
            double worldMouseY = (mouseY - panY) / oldZoom;
            panX = (float) (mouseX - worldMouseX * zoom);
            panY = (float) (mouseY - worldMouseY * zoom);
        }
        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return super.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();

        if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            // Delete selected node (last one mouse was over)
            double mouseX = Minecraft.getInstance().mouseHandler.xpos() * (double)Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double)Minecraft.getInstance().getWindow().getWidth();
            double mouseY = Minecraft.getInstance().mouseHandler.ypos() * (double)Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double)Minecraft.getInstance().getWindow().getHeight();
            
            double worldMouseX = (mouseX - panX) / zoom;
            double worldMouseY = (mouseY - panY) / zoom;
            
            GuiNode toRemove = null;
            for (int i = nodes.size() - 1; i >= 0; i--) {
                GuiNode node = nodes.get(i);
                if (worldMouseX >= node.x && worldMouseX <= node.x + node.width && worldMouseY >= node.y && worldMouseY <= node.y + node.height) {
                    toRemove = node;
                    break;
                }
            }
            
            if (toRemove != null) {
                if (focusedNode == toRemove) {
                    focusedNode = null;
                    focusedPort = null;
                }
                final GuiNode finalToRemove = toRemove;
                nodes.remove(toRemove);
                connections.removeIf(c -> c.from == finalToRemove || c.to == finalToRemove);
                return true;
            }
        }
        return super.keyPressed(event);
    }

    private boolean showNodeContextMenu = false;
    private GuiNode contextMenuNode = null;

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.buttonInfo().button();
        if (showNodeContextMenu) {
            int x = (int) menuX;
            int y = (int) menuY;
            int width = 100;
            
            // Delete Node
            if (mouseX >= x && mouseX <= x + width && mouseY >= y + 5 && mouseY <= y + 20) {
                if (contextMenuNode != null) {
                    if (focusedNode == contextMenuNode) {
                        focusedNode = null;
                        focusedPort = null;
                    }
                    final GuiNode finalNode = contextMenuNode;
                    nodes.remove(contextMenuNode);
                    connections.removeIf(c -> c.from == finalNode || c.to == finalNode);
                }
                showNodeContextMenu = false;
                contextMenuNode = null;
                return true;
            }
            // Break Links
            if (mouseX >= x && mouseX <= x + width && mouseY >= y + 20 && mouseY <= y + 35) {
                if (contextMenuNode != null) {
                    final GuiNode finalNode = contextMenuNode;
                    connections.removeIf(c -> c.from == finalNode || c.to == finalNode);
                }
                showNodeContextMenu = false;
                contextMenuNode = null;
                return true;
            }
            
            showNodeContextMenu = false;
            contextMenuNode = null;
            return true;
        }

        if (showNodeMenu) {
            Map<String, List<NodeDefinition>> categories = new HashMap<>();
            for (NodeDefinition def : NodeRegistry.getAll()) {
                categories.computeIfAbsent(def.category(), k -> new ArrayList<>()).add(def);
            }
            List<String> sortedCategories = new ArrayList<>(categories.keySet());
            sortedCategories.sort(String::compareTo);

            int x = (int) menuX;
            int y = (int) menuY;
            int width = 100;

            if (hoveredCategory != null) {
                List<NodeDefinition> catNodes = categories.get(hoveredCategory);
                int subX = x + width;
                int subY = y + sortedCategories.indexOf(hoveredCategory) * 15;
                int subWidth = 120;

                for (int i = 0; i < catNodes.size(); i++) {
                    int itemY = subY + 5 + i * 15;
                    if (mouseX >= subX && mouseX <= subX + subWidth && mouseY >= itemY && mouseY <= itemY + 15) {
                        NodeDefinition def = catNodes.get(i);
                        float worldX = (float) ((menuX - panX) / zoom);
                        float worldY = (float) ((menuY - panY) / zoom);
                        GuiNode node = new GuiNode(def, worldX, worldY);
                        nodes.add(node);
                        showNodeMenu = false;
                        hoveredCategory = null;
                        return true;
                    }
                }
            }

            // If clicked outside sub-menu but inside main menu, we might want to stay open or close
            // For simplicity, close if not clicking a category
            boolean clickedCategory = false;
            for (int i = 0; i < sortedCategories.size(); i++) {
                int itemY = y + 5 + i * 15;
                if (mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + 15) {
                    clickedCategory = true;
                    break;
                }
            }
            
            if (!clickedCategory) {
                showNodeMenu = false;
                hoveredCategory = null;
            }
            return true;
        }

        if (button == 2) { // Middle click for panning
            isPanning = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }

        if (button == 1) { // Right click
            double worldMouseX = (mouseX - panX) / zoom;
            double worldMouseY = (mouseY - panY) / zoom;
            
            for (int i = nodes.size() - 1; i >= 0; i--) {
                GuiNode node = nodes.get(i);
                if (worldMouseX >= node.x && worldMouseX <= node.x + node.width && worldMouseY >= node.y && worldMouseY <= node.y + node.height) {
                    showNodeContextMenu = true;
                    contextMenuNode = node;
                    menuX = mouseX;
                    menuY = mouseY;
                    return true;
                }
            }
            
            showNodeMenu = true;
            menuX = mouseX;
            menuY = mouseY;
            return true;
        }
        
        double worldMouseX = (mouseX - panX) / zoom;
        double worldMouseY = (mouseY - panY) / zoom;

        if (button == 0) { // Left click
            focusedNode = null;
            focusedPort = null;
            
            // Check for port clicks first
            for (GuiNode node : nodes) {
                // Check inputs
                for (int i = 0; i < node.inputs.size(); i++) {
                    GuiNode.NodePort port = node.inputs.get(i);
                    float[] pos = node.getPortPosition(i, true);
                    
                    // Input box click check
                    if (port.hasInput) {
                        float inputX = pos[0] + 8 + font.width(port.name) + 2;
                        float inputY = pos[1] - 4;
                        float inputWidth = 50;
                        float inputHeight = 10;
                        if (worldMouseX >= inputX && worldMouseX <= inputX + inputWidth && worldMouseY >= inputY && worldMouseY <= inputY + inputHeight) {
                            // Only allow editing if not connected
                            boolean isConnected = false;
                            for (GuiConnection conn : connections) {
                                if (conn.to == node && conn.toPort.equals(port.name)) {
                                    isConnected = true;
                                    break;
                                }
                            }
                            if (!isConnected) {
                                if (port.type == NodeDefinition.PortType.BOOLEAN) {
                                    JsonElement val = node.inputValues.get(port.name);
                                    boolean current = val != null ? val.getAsBoolean() : (port.defaultValue instanceof Boolean ? (Boolean) port.defaultValue : false);
                                    node.inputValues.addProperty(port.name, !current);
                                } else {
                                    // Open separate input modal
                                    JsonElement val = node.inputValues.get(port.name);
                                    String initialText = val != null ? val.getAsString() : (port.defaultValue != null ? port.defaultValue.toString() : "");
                                    boolean isNumeric = (port.type == NodeDefinition.PortType.FLOAT);
                                    
                                    final GuiNode targetNode = node;
                                    final String targetPort = port.name;
                                    
                                    Minecraft.getInstance().setScreen(new InputModalScreen(
                                        this, 
                                        "Enter " + port.name, 
                                        initialText, 
                                        isNumeric,
                                        (newText) -> {
                                            targetNode.inputValues.addProperty(targetPort, newText);
                                        }
                                    ));
                                }
                                return true;
                            }
                        }
                    }

                    if (Math.abs(worldMouseX - pos[0]) < 5 && Math.abs(worldMouseY - pos[1]) < 5) {
                        connectionStartNode = node;
                        connectionStartPort = node.inputs.get(i).name;
                        isConnectionFromInput = true;
                        return true;
                    }
                }
                // Check outputs
                for (int i = 0; i < node.outputs.size(); i++) {
                    float[] pos = node.getPortPosition(i, false);
                    if (Math.abs(worldMouseX - pos[0]) < 5 && Math.abs(worldMouseY - pos[1]) < 5) {
                        connectionStartNode = node;
                        connectionStartPort = node.outputs.get(i).name;
                        isConnectionFromInput = false;
                        return true;
                    }
                }
            }

            // Check for node header click
            for (int i = nodes.size() - 1; i >= 0; i--) {
                GuiNode node = nodes.get(i);
                if (node.isMouseOverHeader(worldMouseX, worldMouseY)) {
                    draggingNode = node;
                    dragOffsetX = (float) (worldMouseX - node.x);
                    dragOffsetY = (float) (worldMouseY - node.y);
                    nodes.remove(i);
                    nodes.add(node);
                    return true;
                }
            }
        }
        return super.mouseClicked(event, isDouble);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.buttonInfo().button();
        if (button == 2) {
            isPanning = false;
            return true;
        }
        if (connectionStartNode != null) {
            double worldMouseX = (mouseX - panX) / zoom;
            double worldMouseY = (mouseY - panY) / zoom;

            for (GuiNode node : nodes) {
                if (node == connectionStartNode) continue;
                
                // If started from output, look for input
                if (!isConnectionFromInput) {
                    GuiNode.NodePort startPort = connectionStartNode.getPortByName(connectionStartPort, false);
                    for (int i = 0; i < node.inputs.size(); i++) {
                        GuiNode.NodePort targetPort = node.inputs.get(i);
                        float[] pos = node.getPortPosition(i, true);
                        if (Math.abs(worldMouseX - pos[0]) < 10 && Math.abs(worldMouseY - pos[1]) < 10) {
                            if (startPort != null && startPort.type == targetPort.type) {
                                // Remove existing connections to this input if it's not EXEC
                                if (targetPort.type != NodeDefinition.PortType.EXEC) {
                                    connections.removeIf(c -> c.to == node && c.toPort.equals(targetPort.name));
                                }
                                connections.add(new GuiConnection(connectionStartNode, connectionStartPort, node, targetPort.name));
                            }
                            break;
                        }
                    }
                } else {
                    // Started from input, look for output
                    GuiNode.NodePort startPort = connectionStartNode.getPortByName(connectionStartPort, true);
                    for (int i = 0; i < node.outputs.size(); i++) {
                        GuiNode.NodePort targetPort = node.outputs.get(i);
                        float[] pos = node.getPortPosition(i, false);
                        if (Math.abs(worldMouseX - pos[0]) < 10 && Math.abs(worldMouseY - pos[1]) < 10) {
                            if (startPort != null && startPort.type == targetPort.type) {
                                // Remove existing connections to the start input if it's not EXEC
                                if (startPort.type != NodeDefinition.PortType.EXEC) {
                                    connections.removeIf(c -> c.to == connectionStartNode && c.toPort.equals(startPort.name));
                                }
                                connections.add(new GuiConnection(node, targetPort.name, connectionStartNode, connectionStartPort));
                            }
                            break;
                        }
                    }
                }
            }
            connectionStartNode = null;
            connectionStartPort = null;
        }

        draggingNode = null;
        isPanning = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.buttonInfo().button();
        if (isPanning) {
            panX += (float) (mouseX - lastMouseX);
            panY += (float) (mouseY - lastMouseY);
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }

        if (draggingNode != null) {
            double worldMouseX = (mouseX - panX) / zoom;
            double worldMouseY = (mouseY - panY) / zoom;
            draggingNode.x = (float) (worldMouseX - dragOffsetX);
            draggingNode.y = (float) (worldMouseY - dragOffsetY);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    // Inner class for connection representation
    private class GuiConnection {
        GuiNode from;
        String fromPort;
        GuiNode to;
        String toPort;

        public GuiConnection(GuiNode from, String fromPort, GuiNode to, String toPort) {
            this.from = from;
            this.fromPort = fromPort;
            this.to = to;
            this.toPort = toPort;
        }
    }

    // Inner class for simple node representation
    private class GuiNode {
        String id;
        String typeId;
        NodeDefinition definition;
        String title;
        float x, y;
        int color;
        float width = 120;
        float height = 60;
        float headerHeight = 15;
        JsonObject inputValues = new JsonObject();

        List<NodePort> inputs = new ArrayList<>();
        List<NodePort> outputs = new ArrayList<>();

        private boolean sizeDirty = true;

        public GuiNode(NodeDefinition def, float x, float y) {
            this.id = UUID.randomUUID().toString();
            this.typeId = def.id();
            this.definition = def;
            this.title = def.name();
            this.x = x;
            this.y = y;
            this.color = def.color();
            
            for (NodeDefinition.PortDefinition p : def.inputs()) {
                addInput(p.name(), p.type(), p.color(), p.hasInput(), p.defaultValue());
            }
            for (NodeDefinition.PortDefinition p : def.outputs()) {
                addOutput(p.name(), p.type(), p.color());
            }
        }

        public void addInput(String name, NodeDefinition.PortType type, int color, boolean hasInput, Object defaultValue) {
            inputs.add(new NodePort(name, type, color, true, hasInput, defaultValue));
            sizeDirty = true;
        }

        public void addOutput(String name, NodeDefinition.PortType type, int color) {
            outputs.add(new NodePort(name, type, color, false, false, null));
            sizeDirty = true;
        }

        private void updateSize(net.minecraft.client.gui.Font font) {
            // Calculate height
            int maxPorts = Math.max(inputs.size(), outputs.size());
            this.height = Math.max(40, headerHeight + 10 + maxPorts * 15 + 5);

            // Calculate width
            float minWidth = 100;
            float titleW = font.width(title) + 20;

            float maxInputW = 0;
            for (NodePort p : inputs) {
                float w = 10 + font.width(p.name);
                if (p.hasInput) {
                    w += 55; // Space for input field
                }
                maxInputW = Math.max(maxInputW, w);
            }

            float maxOutputW = 0;
            for (NodePort p : outputs) {
                float w = 10 + font.width(p.name);
                maxOutputW = Math.max(maxOutputW, w);
            }

            this.width = Math.max(minWidth, Math.max(titleW, maxInputW + maxOutputW + 20));
            sizeDirty = false;
        }

        public float[] getPortPositionByName(String name, boolean isInput) {
            List<NodePort> ports = isInput ? inputs : outputs;
            for (int i = 0; i < ports.size(); i++) {
                if (ports.get(i).name.equals(name)) {
                    return getPortPosition(i, isInput);
                }
            }
            return new float[]{x, y};
        }

        public NodePort getPortByName(String name, boolean isInput) {
            List<NodePort> ports = isInput ? inputs : outputs;
            for (NodePort p : ports) {
                if (p.name.equals(name)) return p;
            }
            return null;
        }

        public float[] getPortPosition(int index, boolean isInput) {
            float py = y + headerHeight + 10 + index * 15 + 3f; // Center of port
            float px = isInput ? x : x + width;
            return new float[]{px, py};
        }

        public void render(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font, int mouseX, int mouseY, float panX, float panY, float zoom, List<GuiConnection> connections) {
            if (sizeDirty) {
                updateSize(font);
            }
            // Shadow
            guiGraphics.fill((int) x + 2, (int) y + 2, (int) (x + width + 2), (int) (y + height + 2), 0x88000000);
            
            // Background
            guiGraphics.fill((int) x, (int) y, (int) (x + width), (int) (y + height), 0xEE1A1A1A);
            
            // Border (highlighted if mouse is over)
            double worldMouseX = (mouseX - panX) / zoom;
            double worldMouseY = (mouseY - panY) / zoom;
            boolean isHovered = worldMouseX >= x && worldMouseX <= x + width && worldMouseY >= y && worldMouseY <= y + height;
            int borderColor = isHovered ? 0xFFFFFFFF : 0xFF333333;
            guiGraphics.renderOutline((int) x, (int) y, (int) width, (int) height, borderColor);
            
            // Header
            guiGraphics.fill((int) x + 1, (int) y + 1, (int) (x + width - 1), (int) (y + headerHeight), color);
            
            // Title
            guiGraphics.drawString(font, title, (int) x + 5, (int) y + 4, 0xFFFFFFFF, true);

            // Render Inputs
            for (int i = 0; i < inputs.size(); i++) {
                renderPort(guiGraphics, font, inputs.get(i), (int) x, (int) (y + headerHeight + 10 + i * 15), true, connections);
            }

            // Render Outputs
            for (int i = 0; i < outputs.size(); i++) {
                renderPort(guiGraphics, font, outputs.get(i), (int) (x + width), (int) (y + headerHeight + 10 + i * 15), false, connections);
            }
        }

        private void renderPort(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font, NodePort port, int px, int py, boolean isInput, List<GuiConnection> connections) {
            int color = port.color;
            boolean isConnected = false;
            for (GuiConnection conn : connections) {
                if (isInput) {
                    if (conn.to == this && conn.toPort.equals(port.name)) {
                        isConnected = true;
                        break;
                    }
                } else {
                    if (conn.from == this && conn.fromPort.equals(port.name)) {
                        isConnected = true;
                        break;
                    }
                }
            }

            if (port.type == NodeDefinition.PortType.EXEC) {
                // House shape for EXEC
                if (isInput) {
                    guiGraphics.fill(px - 1, py, px + 1, py + 6, color);
                    guiGraphics.fill(px + 1, py + 1, px + 3, py + 5, color);
                    guiGraphics.fill(px + 3, py + 2, px + 5, py + 4, color);
                    if (isConnected) guiGraphics.fill(px, py + 2, px + 2, py + 4, 0xFFFFFFFF);
                } else {
                    guiGraphics.fill(px - 5, py, px - 3, py + 6, color);
                    guiGraphics.fill(px - 3, py + 1, px - 1, py + 5, color);
                    guiGraphics.fill(px - 1, py + 2, px + 1, py + 4, color);
                    if (isConnected) guiGraphics.fill(px - 3, py + 2, px - 1, py + 4, 0xFFFFFFFF);
                }
            } else {
                // Circle-ish for DATA ports
                if (isConnected) {
                    guiGraphics.fill(px - 3, py + 1, px + 3, py + 5, color);
                    guiGraphics.fill(px - 2, py, px + 2, py + 6, color);
                    guiGraphics.fill(px - 1, py + 2, px + 1, py + 4, 0xFFFFFFFF);
                } else {
                    guiGraphics.fill(px - 3, py + 1, px + 3, py + 5, color);
                    guiGraphics.fill(px - 2, py, px + 2, py + 6, color);
                    guiGraphics.fill(px - 1, py + 2, px + 1, py + 4, 0xAA000000);
                }
            }

            // Port Name and Value
            if (isInput) {
                guiGraphics.drawString(font, port.name, px + 8, py - 1, 0xFFAAAAAA, false);
                
                if (port.hasInput && !isConnected) {
                    float inputX = px + 8 + font.width(port.name) + 2;
                    float inputY = py - 4;
                    float inputWidth = 50;
                    float inputHeight = 10;
                    
                    // Background
                    guiGraphics.fill((int)inputX, (int)inputY, (int)(inputX + inputWidth), (int)(inputY + inputHeight), 0x66000000);
                    
                    if (port.type == NodeDefinition.PortType.BOOLEAN) {
                        JsonElement val = inputValues.get(port.name);
                        boolean boolVal = val != null ? val.getAsBoolean() : (port.defaultValue instanceof Boolean ? (Boolean) port.defaultValue : false);
                        
                        // Checkbox style
                        int boxColor = boolVal ? 0xFF36CF36 : 0xFF333333;
                        guiGraphics.fill((int)inputX + 2, (int)inputY + 2, (int)inputX + 8, (int)inputY + 8, boxColor);
                        guiGraphics.renderOutline((int)inputX + 1, (int)inputY + 1, 8, 8, 0xFFFFFFFF);
                        
                        String text = boolVal ? "True" : "False";
                        guiGraphics.drawString(font, text, (int)inputX + 12, (int)inputY + 1, 0xFFCCCCCC, false);
                    } else {
                        // Border if focused
                        boolean isFocused = focusedNode == this && focusedPort.equals(port.name);
                        guiGraphics.renderOutline((int)inputX, (int)inputY, (int)inputWidth, (int)inputHeight, 0x33FFFFFF);
                        
                        // Text
                        JsonElement val = inputValues.get(port.name);
                        String text = val != null ? val.getAsString() : (port.defaultValue != null ? port.defaultValue.toString() : "");
                        
                        String renderText = text;
                        // Truncate text if too long
                        if (font.width(renderText) > inputWidth - 4) {
                            renderText = "..." + font.plainSubstrByWidth(renderText, (int)inputWidth - 10, true);
                        }
                        
                        guiGraphics.drawString(font, renderText, (int)inputX + 2, (int)inputY + 1, 0xFFCCCCCC, false);
                    }
                }
            } else {
                guiGraphics.drawString(font, port.name, px - 8 - font.width(port.name), py - 1, 0xFFAAAAAA, false);
            }
        }

        public boolean isMouseOverHeader(double worldMouseX, double worldMouseY) {
            return worldMouseX >= x && worldMouseX <= x + width && worldMouseY >= y && worldMouseY <= y + headerHeight;
        }

        static class NodePort {
            String name;
            NodeDefinition.PortType type;
            int color;
            boolean isInput;
            boolean hasInput;
            Object defaultValue;

            public NodePort(String name, NodeDefinition.PortType type, int color, boolean isInput, boolean hasInput, Object defaultValue) {
                this.name = name;
                this.type = type;
                this.color = color;
                this.isInput = isInput;
                this.hasInput = hasInput;
                this.defaultValue = defaultValue;
            }
        }
    }
}
