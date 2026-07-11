package ltd.opens.mg.mc.client.gui.screens;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 物品数据组件可视化编辑器（参考 CAD Editor 的分类编辑 UI，MIT License, Copyright (c) 2022 Franck Velasco）。
 *
 * <p>按分类编辑物品组件，编辑结果序列化为 JSON 字符串回传给蓝图 {@code COMPONENTS} 端口。
 * 由于 Minecraft 1.21.1 的 {@code CustomModelData} 仅支持单 float，自定义模型数据此处只提供 float 输入。
 */
public class ItemComponentEditorScreen extends Screen {
    private static final String[] CATEGORIES = {
            "enchantments", "custom_name", "lore", "custom_model_data", "attribute_modifiers"
    };

    private final Screen parent;
    private final Consumer<String> onConfirm;
    private JsonObject config;
    private String category;

    private int sx, sy, sw, sh;
    private final List<Label> labels = new ArrayList<>();

    public ItemComponentEditorScreen(Screen parent, String initialJson, Consumer<String> onConfirm, String initialCategory) {
        super(Component.literal("Item Component Editor"));
        this.parent = parent;
        this.onConfirm = onConfirm;
        this.category = initialCategory;
        try {
            JsonObject parsed = JsonParser.parseString(initialJson).getAsJsonObject();
            this.config = parsed != null ? parsed : new JsonObject();
        } catch (Exception e) {
            this.config = new JsonObject();
        }
    }

    @Override
    protected void init() {
        this.labels.clear();
        this.sw = 360;
        this.sh = 240;
        this.sx = (this.width - sw) / 2;
        this.sy = (this.height - sh) / 2;

        int catX = sx + 10;
        int catY = sy + 34;
        int catW = 110;
        int catH = 20;
        for (int i = 0; i < CATEGORIES.length; i++) {
            final String cat = CATEGORIES[i];
            this.addRenderableWidget(Button.builder(Component.translatable(catKey(cat)), b ->
                    Minecraft.getInstance().setScreen(new ItemComponentEditorScreen(parent, config.toString(), onConfirm, cat))
            ).bounds(catX, catY + i * (catH + 4), catW, catH).build());
        }

        int panelX = sx + 130;
        int panelY = sy + 34;
        int panelW = sw - 140;
        switch (category) {
            case "enchantments" -> buildEnchantments(panelX, panelY, panelW);
            case "custom_name" -> buildCustomName(panelX, panelY, panelW);
            case "lore" -> buildLore(panelX, panelY, panelW);
            case "custom_model_data" -> buildModelData(panelX, panelY, panelW);
            case "attribute_modifiers" -> buildAttributes(panelX, panelY, panelW);
            default -> buildEnchantments(panelX, panelY, panelW);
        }

        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.components.confirm"),
                b -> {
                    onConfirm.accept(config.toString());
                    Minecraft.getInstance().setScreen(parent);
                }).bounds(sx + sw - 170, sy + sh - 26, 80, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.components.cancel"),
                b -> Minecraft.getInstance().setScreen(parent)
        ).bounds(sx + sw - 85, sy + sh - 26, 80, 20).build());
    }

    private void buildEnchantments(int px, int py, int pw) {
        JsonArray arr = readArray("enchantments");
        boolean stored = config.has("stored_enchantments") && config.get("stored_enchantments").getAsBoolean();
        addLabel(Component.translatable("gui.mgmc.components.enchantments.stored"), px, py);
        this.addRenderableWidget(Button.builder(Component.literal(stored ? "✔" : "✘"), b -> {
            config.addProperty("stored_enchantments", !stored);
            Minecraft.getInstance().setScreen(new ItemComponentEditorScreen(parent, config.toString(), onConfirm, category));
        }).bounds(px + pw - 28, py - 2, 24, 16).build());

        int y = py + 18;
        for (int i = 0; i < arr.size(); i++) {
            JsonObject o = arr.get(i).getAsJsonObject();
            final int idx = i;
            addLabel(Component.translatable("gui.mgmc.components.enchantment_id"), px, y);
            EditBox idBox = new EditBox(font, px + 70, y - 2, pw - 170, 16, Component.literal(""));
            idBox.setValue(o.has("id") ? o.get("id").getAsString() : "");
            idBox.setMessage(Component.translatable("gui.mgmc.components.enchantment_id"));
            idBox.setResponder(s -> updateAt(arr, idx, ob -> {
                if (s.isBlank()) ob.remove("id");
                else ob.addProperty("id", s);
            }));
            this.addRenderableWidget(idBox);

            EditBox lvlBox = new EditBox(font, px + pw - 96, y - 2, 44, 16, Component.literal(""));
            lvlBox.setValue(o.has("lvl") ? String.valueOf(o.get("lvl").getAsInt()) : "1");
            lvlBox.setMessage(Component.translatable("gui.mgmc.components.level"));
            lvlBox.setResponder(s -> updateAt(arr, idx, ob -> {
                try {
                    ob.addProperty("lvl", Integer.parseInt(s.trim()));
                } catch (Exception ignore) {
                }
            }));
            this.addRenderableWidget(lvlBox);

            this.addRenderableWidget(Button.builder(Component.literal("✕"), b -> {
                arr.remove(idx);
                Minecraft.getInstance().setScreen(new ItemComponentEditorScreen(parent, config.toString(), onConfirm, category));
            }).bounds(px + pw - 46, y - 2, 18, 16).build());
            y += 22;
        }
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.components.add"), b -> {
            ensureArray("enchantments").add(new JsonObject());
            Minecraft.getInstance().setScreen(new ItemComponentEditorScreen(parent, config.toString(), onConfirm, category));
        }).bounds(px, y + 4, 80, 18).build());
    }

    private void buildCustomName(int px, int py, int pw) {
        addLabel(Component.translatable("gui.mgmc.components.custom_name.hint"), px, py);
        EditBox box = new EditBox(font, px, py + 14, pw, 18, Component.literal(""));
        box.setValue(config.has("custom_name") ? config.get("custom_name").getAsString() : "");
        box.setMessage(Component.translatable("gui.mgmc.components.custom_name.placeholder"));
        box.setResponder(s -> {
            if (s.isBlank()) config.remove("custom_name");
            else config.addProperty("custom_name", s);
        });
        this.addRenderableWidget(box);
    }

    private void buildLore(int px, int py, int pw) {
        JsonArray arr = readArray("lore");
        int y = py;
        for (int i = 0; i < arr.size(); i++) {
            String line = arr.get(i).getAsString();
            final int idx = i;
            addLabel(Component.translatable("gui.mgmc.components.lore.line", i + 1), px, y);
            EditBox box = new EditBox(font, px + 60, y - 2, pw - 100, 16, Component.literal(""));
            box.setValue(line);
            box.setMessage(Component.translatable("gui.mgmc.components.lore.placeholder"));
            box.setResponder(s -> {
                if (idx < arr.size()) arr.set(idx, new JsonPrimitive(s));
            });
            this.addRenderableWidget(box);
            this.addRenderableWidget(Button.builder(Component.literal("✕"), b -> {
                arr.remove(idx);
                Minecraft.getInstance().setScreen(new ItemComponentEditorScreen(parent, config.toString(), onConfirm, category));
            }).bounds(px + pw - 34, y - 2, 18, 16).build());
            y += 22;
        }
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.components.add"), b -> {
            ensureArray("lore").add(new JsonPrimitive("{\"text\":\"\"}"));
            Minecraft.getInstance().setScreen(new ItemComponentEditorScreen(parent, config.toString(), onConfirm, category));
        }).bounds(px, y + 4, 80, 18).build());
    }

    private void buildModelData(int px, int py, int pw) {
        int v = 0;
        if (config.has("custom_model_data")) {
            JsonPrimitive p = config.getAsJsonPrimitive("custom_model_data");
            if (p != null) v = p.getAsInt();
        }
        addLabel(Component.translatable("gui.mgmc.components.model_data"), px, py);
        EditBox box = new EditBox(font, px, py + 14, pw, 18, Component.literal(""));
        box.setValue(String.valueOf(v));
        box.setMessage(Component.translatable("gui.mgmc.components.model_data.placeholder"));
        box.setResponder(s -> {
            try {
                int i = Integer.parseInt(s.trim());
                if (i == 0) config.remove("custom_model_data");
                else config.addProperty("custom_model_data", i);
            } catch (Exception ignore) {
            }
        });
        this.addRenderableWidget(box);
    }

    private void buildAttributes(int px, int py, int pw) {
        JsonArray arr = readArray("attribute_modifiers");
        int y = py;
        for (int i = 0; i < arr.size(); i++) {
            JsonObject o = arr.get(i).getAsJsonObject();
            final int idx = i;
            EditBox attrBox = new EditBox(font, px, y, pw - 30, 16, Component.literal(""));
            attrBox.setValue(o.has("attribute") ? o.get("attribute").getAsString() : "");
            attrBox.setMessage(Component.translatable("gui.mgmc.components.attr.attribute"));
            attrBox.setResponder(s -> updateAt(arr, idx, ob -> {
                if (s.isBlank()) ob.remove("attribute");
                else ob.addProperty("attribute", s);
            }));
            this.addRenderableWidget(attrBox);
            this.addRenderableWidget(Button.builder(Component.literal("✕"), b -> {
                arr.remove(idx);
                Minecraft.getInstance().setScreen(new ItemComponentEditorScreen(parent, config.toString(), onConfirm, category));
            }).bounds(px + pw - 26, y, 18, 16).build());
            y += 20;

            int third = (pw - 30) / 3;
            EditBox slotBox = new EditBox(font, px, y, third, 16, Component.literal(""));
            slotBox.setValue(o.has("slot") ? o.get("slot").getAsString() : "any");
            slotBox.setMessage(Component.translatable("gui.mgmc.components.attr.slot"));
            slotBox.setResponder(s -> updateAt(arr, idx, ob -> {
                if (s.isBlank()) ob.remove("slot");
                else ob.addProperty("slot", s);
            }));
            this.addRenderableWidget(slotBox);

            EditBox opBox = new EditBox(font, px + third + 5, y, third, 16, Component.literal(""));
            opBox.setValue(o.has("operation") ? String.valueOf(o.get("operation").getAsInt()) : "0");
            opBox.setMessage(Component.translatable("gui.mgmc.components.attr.operation"));
            opBox.setResponder(s -> updateAt(arr, idx, ob -> {
                try {
                    ob.addProperty("operation", Integer.parseInt(s.trim()));
                } catch (Exception ignore) {
                }
            }));
            this.addRenderableWidget(opBox);

            EditBox amtBox = new EditBox(font, px + 2 * third + 10, y, third, 16, Component.literal(""));
            amtBox.setValue(o.has("amount") ? String.valueOf(o.get("amount").getAsDouble()) : "0");
            amtBox.setMessage(Component.translatable("gui.mgmc.components.attr.amount"));
            amtBox.setResponder(s -> updateAt(arr, idx, ob -> {
                try {
                    ob.addProperty("amount", Double.parseDouble(s.trim()));
                } catch (Exception ignore) {
                }
            }));
            this.addRenderableWidget(amtBox);
            y += 24;
        }
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.components.add"), b -> {
            JsonObject e = new JsonObject();
            e.addProperty("attribute", "");
            e.addProperty("slot", "any");
            e.addProperty("operation", 0);
            e.addProperty("amount", 0d);
            ensureArray("attribute_modifiers").add(e);
            Minecraft.getInstance().setScreen(new ItemComponentEditorScreen(parent, config.toString(), onConfirm, category));
        }).bounds(px, y + 4, 80, 18).build());
    }

    private void addLabel(Component text, int x, int y) {
        labels.add(new Label(text, x, y, 0xFFAAAAAA));
    }

    private JsonArray readArray(String key) {
        if (config.has(key) && config.get(key).isJsonArray()) {
            return config.getAsJsonArray(key);
        }
        return new JsonArray();
    }

    private JsonArray ensureArray(String key) {
        if (!config.has(key) || !config.get(key).isJsonArray()) {
            config.add(key, new JsonArray());
        }
        return config.getAsJsonArray(key);
    }

    private void updateAt(JsonArray arr, int idx, java.util.function.Consumer<JsonObject> mutator) {
        if (idx < arr.size() && arr.get(idx).isJsonObject()) {
            mutator.accept(arr.get(idx).getAsJsonObject());
        }
    }

    private static String catKey(String cat) {
        return "gui.mgmc.components.category." + cat;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);
        guiGraphics.fill(sx, sy, sx + sw, sy + sh, 0xEE1A1A1A);
        guiGraphics.renderOutline(sx, sy, sw, sh, 0xFFFFFFFF);
        guiGraphics.drawString(font, Component.translatable("gui.mgmc.components.title"), sx + 10, sy + 10, 0xFFFFFFFF, false);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        for (Label l : labels) {
            guiGraphics.drawString(font, l.text, l.x, l.y, l.color, false);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private record Label(Component text, int x, int y, int color) {
    }
}
