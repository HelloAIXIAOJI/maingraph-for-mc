package ltd.opens.mg.mc.client.gui.screens;

import ltd.opens.mg.mc.core.blueprint.routing.BlueprintRouter;
import ltd.opens.mg.mc.network.payloads.RequestMappingsPayload;
import ltd.opens.mg.mc.network.payloads.SaveMappingsPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

import java.util.*;

public class BlueprintMappingScreen extends Screen {
    private final Map<String, Set<String>> workingMappings = new HashMap<>();
    private IdList idList;
    private BlueprintSelectionList blueprintList;
    private EditBox idInput;
    private String selectedId = null;

    public BlueprintMappingScreen() {
        super(Component.translatable("gui.mgmc.mapping.title"));
    }

    @Override
    protected void init() {
        // 请求数据
        if (Minecraft.getInstance().getConnection() != null) {
            Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new RequestMappingsPayload()));
        }

        // ID 列表 (左侧)
        this.idList = new IdList(this.minecraft, this.width / 3, this.height - 100, 40, 20);
        this.idList.setX(10);
        this.addRenderableWidget(this.idList);

        // 蓝图列表 (右侧)
        this.blueprintList = new BlueprintSelectionList(this.minecraft, this.width / 2, this.height - 100, 40, 20);
        this.blueprintList.setX(this.width / 3 + 20);
        this.addRenderableWidget(this.blueprintList);

        // ID 输入框
        this.idInput = new EditBox(this.font, 10, this.height - 45, this.width / 3, 20, Component.literal("ID"));
        this.idInput.setHint(Component.translatable("gui.mgmc.mapping.id_hint"));
        this.addRenderableWidget(this.idInput);

        // 添加 ID 按钮
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.mapping.add_id"), b -> {
            String id = this.idInput.getValue().trim();
            if (!id.isEmpty() && !workingMappings.containsKey(id)) {
                workingMappings.put(id, new HashSet<>());
                refreshIdList();
                this.idInput.setValue("");
            }
        }).bounds(10 + this.width / 3 + 5, this.height - 45, 60, 20).build());

        // 添加蓝图按钮
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.mapping.add_blueprint"), b -> {
            if (selectedId != null) {
                Minecraft.getInstance().setScreen(new BlueprintSelectionForMappingScreen(this, selectedId));
            }
        }).bounds(this.width / 3 + 20, this.height - 45, 100, 20).build());

        // 保存按钮
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.mapping.save"), b -> {
            if (Minecraft.getInstance().getConnection() != null) {
                Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new SaveMappingsPayload(new HashMap<>(workingMappings))));
            }
            this.onClose();
        }).bounds(this.width - 110, this.height - 30, 100, 20).build());

        // 返回按钮
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.mapping.back"), b -> {
            this.onClose();
        }).bounds(this.width - 220, this.height - 30, 100, 20).build());

        refreshIdList();
    }

    public void updateMappingsFromServer(Map<String, Set<String>> mappings) {
        this.workingMappings.clear();
        mappings.forEach((k, v) -> this.workingMappings.put(k, new HashSet<>(v)));
        refreshIdList();
    }

    private void refreshIdList() {
        String prevSelected = selectedId;
        this.idList.clearEntries();
        
        // 确保内置 ID 存在
        workingMappings.putIfAbsent(BlueprintRouter.GLOBAL_ID, new HashSet<>());
        workingMappings.putIfAbsent(BlueprintRouter.PLAYERS_ID, new HashSet<>());

        workingMappings.keySet().stream().sorted().forEach(id -> {
            this.idList.add(new IdEntry(id));
        });

        if (prevSelected != null && workingMappings.containsKey(prevSelected)) {
            selectId(prevSelected);
        }
    }

    public void addMapping(String id, String path) {
        workingMappings.computeIfAbsent(id, k -> new HashSet<>()).add(path);
        if (id.equals(selectedId)) {
            selectId(id);
        }
    }

    private void selectId(String id) {
        this.selectedId = id;
        this.blueprintList.clearEntries();
        if (id != null && workingMappings.containsKey(id)) {
            workingMappings.get(id).forEach(bp -> {
                this.blueprintList.add(new BlueprintMappingEntry(bp));
            });
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        
        guiGraphics.drawString(this.font, Component.translatable("gui.mgmc.mapping.ids"), 10, 30, 0xAAAAAA);
        guiGraphics.drawString(this.font, Component.translatable("gui.mgmc.mapping.blueprints"), this.width / 3 + 20, 30, 0xAAAAAA);
    }

    // --- 内部类：ID 列表 ---
    class IdList extends ObjectSelectionList<IdEntry> {
        public IdList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        public void add(IdEntry entry) {
            super.addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return this.width - 10;
        }
    }

    class IdEntry extends ObjectSelectionList.Entry<IdEntry> {
        private final String id;

        public IdEntry(String id) {
            this.id = id;
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int index, int top, boolean isHovered, float partialTick) {
            int y = this.getY();
            if (y <= 0) y = top;
            int color = selectedId != null && selectedId.equals(id) ? 0xFFFFFF00 : 0xFFFFFFFF;
            guiGraphics.drawString(font, id, this.getX() + 5, y + 5, color);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
            selectId(id);
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal(id);
        }
    }

    // --- 内部类：蓝图列表 ---
    class BlueprintSelectionList extends ObjectSelectionList<BlueprintMappingEntry> {
        public BlueprintSelectionList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        public void add(BlueprintMappingEntry entry) {
            super.addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return this.width - 10;
        }
    }

    class BlueprintMappingEntry extends ObjectSelectionList.Entry<BlueprintMappingEntry> {
        private final String blueprintPath;

        public BlueprintMappingEntry(String path) {
            this.blueprintPath = path;
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int index, int top, boolean isHovered, float partialTick) {
            int y = this.getY();
            if (y <= 0) y = top;
            guiGraphics.drawString(font, blueprintPath, this.getX() + 5, y + 5, 0xFFFFFFFF);
            
            // 删除按钮 (X)
            int xBtnX = this.getX() + this.getWidth() - 20;
            // 简单的悬停检测逻辑可以根据 mouseX/mouseY 实现，这里先渲染
            guiGraphics.drawString(font, "X", xBtnX, y + 5, 0xFFAAAAAA);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
            int xBtnX = this.getX() + this.getWidth() - 20;
            if (event.x() >= xBtnX) {
                if (selectedId != null && workingMappings.containsKey(selectedId)) {
                    workingMappings.get(selectedId).remove(blueprintPath);
                    selectId(selectedId);
                }
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return Component.literal(blueprintPath);
        }
    }
}
