package ltd.opens.mg.mc.client.gui;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import java.util.ArrayList;
import java.util.List;

public class BlueprintState {
    public float panX = 0;
    public float panY = 0;
    public float zoom = 1.0f;

    public final List<GuiNode> nodes = new ArrayList<>();
    public final List<GuiConnection> connections = new ArrayList<>();
    
    public GuiNode draggingNode = null;
    public float dragOffsetX, dragOffsetY;

    public GuiNode connectionStartNode = null;
    public String connectionStartPort = null;
    public boolean isConnectionFromInput = false;

    public boolean isPanning = false;
    public double lastMouseX, lastMouseY;
    
    public boolean showNodeMenu = false;
    public double menuX, menuY;
    public boolean showNodeContextMenu = false;
    public GuiNode contextMenuNode = null;
    public final BlueprintMenu menu = new BlueprintMenu();

    public GuiNode focusedNode = null;
    public String focusedPort = null;
    public int cursorTick = 0;
    public boolean isDirty = false;
    public String notificationMessage = null;
    public int notificationTimer = 0;

    public void showNotification(String message) {
        this.notificationMessage = message;
        this.notificationTimer = 60; // 3 seconds at 20fps, but Minecraft render is faster, let's use ticks or simple counter
    }

    public void markDirty() {
        this.isDirty = true;
    }

    public void resetView() {
        panX = 0;
        panY = 0;
        zoom = 1.0f;
    }
}
