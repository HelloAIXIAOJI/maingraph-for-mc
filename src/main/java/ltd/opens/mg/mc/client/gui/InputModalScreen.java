package ltd.opens.mg.mc.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class InputModalScreen extends Screen {
    private final Screen parent;
    private final String titleStr;
    private final String initialValue;
    private final Consumer<String> onConfirm;
    private EditBox editBox;
    private final boolean isNumeric;

    public InputModalScreen(Screen parent, String title, String initialValue, boolean isNumeric, Consumer<String> onConfirm) {
        super(Component.literal(title));
        this.parent = parent;
        this.titleStr = title;
        this.initialValue = initialValue;
        this.isNumeric = isNumeric;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        int width = 200;
        int height = 80;
        int startX = (this.width - width) / 2;
        int startY = (this.height - height) / 2;

        this.editBox = new EditBox(this.font, startX + 10, startY + 30, width - 20, 20, Component.literal("Input"));
        this.editBox.setValue(initialValue);
        if (isNumeric) {
            this.editBox.setFilter(s -> s.isEmpty() || s.matches("^-?\\d*\\.?\\d*$"));
        }
        this.addRenderableWidget(this.editBox);
        this.setInitialFocus(this.editBox);

        this.addRenderableWidget(Button.builder(Component.literal("Confirm"), (btn) -> {
            onConfirm.accept(editBox.getValue());
            this.minecraft.setScreen(parent);
        }).bounds(startX + 10, startY + 55, 85, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), (btn) -> {
            this.minecraft.setScreen(parent);
        }).bounds(startX + 105, startY + 55, 85, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Use a manual dark overlay instead of renderBackground to avoid "blur once per frame" crash in 1.21.x
        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);
        
        int width = 200;
        int height = 80;
        int startX = (this.width - width) / 2;
        int startY = (this.height - height) / 2;
        
        guiGraphics.fill(startX, startY, startX + width, startY + height, 0xEE1A1A1A);
        guiGraphics.renderOutline(startX, startY, width, height, 0xFFFFFFFF);
        
        guiGraphics.drawString(font, titleStr, startX + 10, startY + 10, 0xFFFFFFFF, false);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            onConfirm.accept(editBox.getValue());
            this.minecraft.setScreen(parent);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.setScreen(parent);
            return true;
        }
        return super.keyPressed(event);
    }
}
