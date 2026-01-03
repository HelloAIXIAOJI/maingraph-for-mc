package ltd.opens.mg.mc.client.gui.screens;

import ltd.opens.mg.mc.MaingraphforMCClient;
import ltd.opens.mg.mc.client.gui.blueprint.*;
import ltd.opens.mg.mc.client.gui.components.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.nio.file.Path;

public class BlueprintScreen extends Screen {
    private final Path dataFile;
    private final BlueprintState state = new BlueprintState();
    private final BlueprintEventHandler eventHandler;

    public BlueprintScreen(Path dataFile) {
        super(Component.translatable("gui.mgmc.blueprint_editor.title", dataFile.getFileName().toString()));
        this.dataFile = dataFile;
        this.eventHandler = new BlueprintEventHandler(state);
        BlueprintIO.load(this.dataFile, state.nodes, state.connections);
    }

    @Override
    protected void init() {
        if (this.minecraft.player != null && !this.minecraft.player.isCreative()) {
            this.minecraft.setScreen(new AboutScreen(null));
            return;
        }
        super.init();
        // Remove vanilla buttons, we'll use custom rendering and interaction
    }

    @Override
    public void tick() {
        super.tick();
        state.cursorTick++;
    }

    private boolean isHovering(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        BlueprintRenderer.drawGrid(guiGraphics, this.width, this.height, state.panX, state.panY, state.zoom);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(state.panX, state.panY);
        guiGraphics.pose().scale(state.zoom, state.zoom);

        BlueprintRenderer.drawConnections(guiGraphics, state.connections, this.width, this.height, state.panX, state.panY, state.zoom);

        for (GuiNode node : state.nodes) {
            float sX = node.x * state.zoom + state.panX;
            float sY = node.y * state.zoom + state.panY;
            float sW = node.width * state.zoom;
            float sH = node.height * state.zoom;
            
            if (sX + sW < 0 || sX > this.width || sY + sH < 0 || sY > this.height) {
                continue;
            }
            
            node.updateConnectedState(state.connections);
            node.render(guiGraphics, this.font, mouseX, mouseY, state.panX, state.panY, state.zoom, state.connections, state.focusedNode, state.focusedPort);
        }

        if (state.connectionStartNode != null) {
            float[] startPos = state.connectionStartNode.getPortPositionByName(state.connectionStartPort, state.isConnectionFromInput);
            BlueprintRenderer.drawBezier(guiGraphics, startPos[0], startPos[1], (float) ((mouseX - state.panX) / state.zoom), (float) ((mouseY - state.panY) / state.zoom), 0x88FFFFFF, state.zoom);
        }

        guiGraphics.pose().popMatrix();
        
        // --- Modern Top Bar (Narrower) ---
        int barHeight = 26;
        guiGraphics.fill(0, 0, this.width, barHeight, 0xF0121212); 
        guiGraphics.fill(0, barHeight, this.width, barHeight + 1, 0xFF2D2D2D); 
        
        // Custom Buttons Rendering
        // Back Button
        renderCustomButton(guiGraphics, mouseX, mouseY, 5, 3, 40, 20, "gui.mgmc.blueprint_editor.back");
        
        // Right side buttons
        int rightX = this.width - 5;
        // Reset View
        rightX -= 70;
        renderCustomButton(guiGraphics, mouseX, mouseY, rightX, 3, 70, 20, "gui.mgmc.blueprint_editor.reset_view");
        
        // Arrange
        rightX -= 45;
        renderCustomButton(guiGraphics, mouseX, mouseY, rightX, 3, 40, 20, "gui.mgmc.blueprint_editor.auto_layout");
        
        // Save
        rightX -= 55;
        renderCustomButton(guiGraphics, mouseX, mouseY, rightX, 3, 50, 20, "gui.mgmc.blueprint_editor.save");

        // --- Bottom UI ---
        // Stats (Bottom Left)
        String statsText = Component.translatable("gui.mgmc.blueprint_editor.stats", state.nodes.size(), state.connections.size()).getString();
        guiGraphics.fill(5, height - 18, 10 + font.width(statsText), height - 4, 0x88000000);
        guiGraphics.drawString(font, statsText, 8, height - 15, 0xFFAAAAAA, false);
        
        // Title (Bottom Right)
        String titleText = this.title.getString();
        int titleW = font.width(titleText);
        guiGraphics.fill(this.width - titleW - 10, height - 18, this.width - 5, height - 4, 0x88000000);
        guiGraphics.drawString(font, titleText, this.width - titleW - 8, height - 15, 0xFFFFFFFF, false);

        if (state.showNodeMenu) {
            state.menu.renderNodeMenu(guiGraphics, font, mouseX, mouseY, state.menuX, state.menuY, this.width, this.height);
        }
        
        if (state.showNodeContextMenu) {
            state.menu.renderNodeContextMenu(guiGraphics, font, mouseX, mouseY, state.menuX, state.menuY);
        }

        // --- Notification Popup ---
        if (state.notificationMessage != null && state.notificationTimer > 0) {
            int msgW = font.width(state.notificationMessage);
            int popupW = msgW + 20;
            int popupH = 20;
            int popupX = (this.width - popupW) / 2;
            int popupY = 40; // Just below top bar
            
            float alpha = Math.min(1.0f, state.notificationTimer / 10.0f);
            int alphaInt = (int)(alpha * 255);
            int bgColor = (alphaInt << 24) | 0x222222;
            int textColor = (alphaInt << 24) | 0xFFFFFF;
            int borderColor = (alphaInt << 24) | 0x555555;

            guiGraphics.fill(popupX, popupY, popupX + popupW, popupY + popupH, bgColor);
            guiGraphics.renderOutline(popupX, popupY, popupW, popupH, borderColor);
            guiGraphics.drawString(font, state.notificationMessage, popupX + 10, popupY + (popupH - 9) / 2, textColor, false);
            
            state.notificationTimer--;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (state.isDirty) {
            Minecraft.getInstance().setScreen(new InputModalScreen(
                this,
                Component.translatable("gui.mgmc.blueprint_editor.save_confirm.title").getString(),
                "",
                false,
                new String[]{
                    Component.translatable("gui.mgmc.blueprint_editor.save_confirm.save").getString(),
                    Component.translatable("gui.mgmc.blueprint_editor.save_confirm.discard").getString()
                },
                InputModalScreen.Mode.SELECTION,
                (selected) -> {
                    if (selected.equals(Component.translatable("gui.mgmc.blueprint_editor.save_confirm.save").getString())) {
                        BlueprintIO.save(this.dataFile, state.nodes, state.connections);
                    }
                    state.isDirty = false;
                    Minecraft.getInstance().setScreen(new BlueprintSelectionScreen());
                }
            ));
        } else {
            Minecraft.getInstance().setScreen(new BlueprintSelectionScreen());
        }
    }

    private void renderCustomButton(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y, int w, int h, String langKey) {
        boolean hovered = isHovering(mouseX, mouseY, x, y, w, h);
        int bgColor = hovered ? 0xFF3D3D3D : 0x00000000; // Transparent background when not hovered
        if (bgColor != 0) {
            guiGraphics.fill(x, y, x + w, y + h, bgColor);
            guiGraphics.renderOutline(x, y, w, h, 0xFF555555);
        }
        
        Component text = Component.translatable(langKey);
        int textW = font.width(text);
        guiGraphics.drawString(font, text, x + (w - textW) / 2, y + (h - 9) / 2, hovered ? 0xFFFFFFFF : 0xFFBBBBBB, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return eventHandler.mouseScrolled(mouseX, mouseY, scrollX, scrollY, this) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return eventHandler.charTyped(event) || super.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return eventHandler.keyPressed(event) || super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
        double mouseX = event.x();
        double mouseY = event.y();
        
        // Handle Top Bar Buttons
        if (mouseY < 26) {
            // Back
            if (isHovering((int)mouseX, (int)mouseY, 5, 3, 40, 20)) {
                onClose();
                return true;
            }
            
            int rightX = this.width - 5;
            // Reset View
            rightX -= 70;
            if (isHovering((int)mouseX, (int)mouseY, rightX, 3, 70, 20)) {
                state.resetView();
                return true;
            }

            // Auto Layout
            rightX -= 45;
            if (isHovering((int)mouseX, (int)mouseY, rightX, 3, 40, 20)) {
                state.autoLayout();
                return true;
            }
            
            // Save
            rightX -= 55;
            if (isHovering((int)mouseX, (int)mouseY, rightX, 3, 50, 20)) {
                BlueprintIO.save(this.dataFile, state.nodes, state.connections);
                state.isDirty = false;
                state.showNotification(Component.translatable("gui.mgmc.blueprint_editor.saved").getString());
                return true;
            }
            
            return true; // Clicked on top bar but not on buttons
        }

        return eventHandler.mouseClicked(event, isDouble, font, this) || super.mouseClicked(event, isDouble);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return eventHandler.mouseReleased(event, this) || super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return eventHandler.mouseDragged(event, dragX, dragY) || super.mouseDragged(event, dragX, dragY);
    }
}
