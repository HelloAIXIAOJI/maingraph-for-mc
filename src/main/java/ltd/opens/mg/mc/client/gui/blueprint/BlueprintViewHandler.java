package ltd.opens.mg.mc.client.gui.blueprint;

public class BlueprintViewHandler {
    private final BlueprintState state;

    public BlueprintViewHandler(BlueprintState state) {
        this.state = state;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 2 || button == 1) { // Middle click or Right click for panning
            state.isPanning = true;
            state.lastMouseX = mouseX;
            state.lastMouseY = mouseY;
            state.startMouseX = mouseX;
            state.startMouseY = mouseY;
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (state.isPanning && (button == 2 || button == 1)) {
            state.isPanning = false;
            
            // 如果是右键，且位移很小，返回 false，让外层逻辑触发菜单
            if (button == 1) {
                double dist = Math.sqrt(Math.pow(mouseX - state.startMouseX, 2) + Math.pow(mouseY - state.startMouseY, 2));
                if (dist < 5.0) {
                    return false; // 不消费事件，交给菜单处理器
                }
            }
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY) {
        if (state.isPanning) {
            state.panX += (float) (mouseX - state.lastMouseX);
            state.panY += (float) (mouseY - state.lastMouseY);
            state.lastMouseX = mouseX;
            state.lastMouseY = mouseY;
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        float zoomSensitivity = 0.1f;
        float oldZoom = state.zoom;
        if (scrollY > 0) {
            state.zoom *= (1 + zoomSensitivity);
        } else {
            state.zoom /= (1 + zoomSensitivity);
        }
        
        // Zoom limits
        state.zoom = Math.max(0.1f, Math.min(3.0f, state.zoom));
        
        if (state.zoom != oldZoom) {
            // Adjust pan to zoom towards mouse position
            double worldMouseX = (mouseX - state.panX) / oldZoom;
            double worldMouseY = (mouseY - state.panY) / oldZoom;
            state.panX = (float) (mouseX - worldMouseX * state.zoom);
            state.panY = (float) (mouseY - worldMouseY * state.zoom);
        }
        return true;
    }
}
