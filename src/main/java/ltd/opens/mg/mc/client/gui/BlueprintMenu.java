package ltd.opens.mg.mc.client.gui;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeRegistry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlueprintMenu {
    private String hoveredCategory = null;
    private String searchQuery = "";
    private int selectedIndex = 0;
    private List<NodeDefinition> filteredNodes = new ArrayList<>();
    private float scrollAmount = 0;
    private float subScrollAmount = 0;

    public void renderNodeContextMenu(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, double menuX, double menuY) {
        int x = (int) menuX;
        int y = (int) menuY;
        int width = 120;
        int height = 46;
        
        // Shadow/Glow
        guiGraphics.fill(x + 2, y + 2, x + width + 2, y + height + 2, 0x44000000);
        // Background
        guiGraphics.fill(x, y, x + width, y + height, 0xF01E1E1E);
        // Border
        guiGraphics.renderOutline(x, y, width, height, 0xFF444444);
        
        // Delete Node
        boolean hoverDelete = mouseX >= x && mouseX <= x + width && mouseY >= y + 3 && mouseY <= y + 23;
        if (hoverDelete) guiGraphics.fill(x + 1, y + 3, x + width - 1, y + 23, 0x44FFFFFF);
        guiGraphics.drawString(font, Component.translatable("gui.mgmc.blueprint_editor.context_menu.delete"), x + 8, y + 8, 0xFFFFFFFF, false);
        
        // Break Links
        boolean hoverBreak = mouseX >= x && mouseX <= x + width && mouseY >= y + 23 && mouseY <= y + 43;
        if (hoverBreak) guiGraphics.fill(x + 1, y + 23, x + width - 1, y + 43, 0x44FFFFFF);
        guiGraphics.drawString(font, Component.translatable("gui.mgmc.blueprint_editor.context_menu.break_links"), x + 8, y + 28, 0xFFFFFFFF, false);
    }

    public void renderNodeMenu(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, double menuX, double menuY, int screenWidth, int screenHeight) {
        int x = (int) menuX;
        int y = (int) menuY;
        int width = 180;
        
        // Handle screen boundaries
        if (x + width > screenWidth) x -= width;
        
        // 1. Render Search Box
        int searchHeight = 25;
        guiGraphics.fill(x, y, x + width, y + searchHeight, 0xF0121212);
        guiGraphics.renderOutline(x, y, width, searchHeight, 0xFF555555);
        
        Component searchHint = Component.translatable("gui.mgmc.blueprint_editor.search_hint");
        String displaySearch = searchQuery.isEmpty() ? searchHint.getString() : searchQuery;
        int searchColor = searchQuery.isEmpty() ? 0xFF888888 : 0xFFFFFFFF;
        guiGraphics.drawString(font, displaySearch + (System.currentTimeMillis() / 500 % 2 == 0 ? "_" : ""), x + 8, y + (searchHeight - 9) / 2, searchColor, false);

        int contentY = y + searchHeight + 2;
        int maxVisibleItems = 12;
        int itemHeight = 18;
        
        if (!searchQuery.isEmpty()) {
            // --- Search Results Mode ---
            updateSearch();
            int displayCount = Math.min(filteredNodes.size(), maxVisibleItems);
            int height = displayCount * itemHeight + 6;
            
            if (contentY + height > screenHeight) contentY = y - height;

            guiGraphics.fill(x, contentY, x + width, contentY + height, 0xF01E1E1E);
            guiGraphics.renderOutline(x, contentY, width, height, 0xFF444444);

            if (filteredNodes.isEmpty()) {
                guiGraphics.drawString(font, Component.translatable("gui.mgmc.blueprint_editor.no_nodes_found"), x + 8, contentY + 8, 0xFF888888, false);
            } else {
                guiGraphics.enableScissor(x, contentY + 3, x + width, contentY + height - 3);
                int totalHeight = filteredNodes.size() * itemHeight;
                int maxScroll = Math.max(0, totalHeight - (displayCount * itemHeight));
                scrollAmount = Mth.clamp(scrollAmount, 0, maxScroll);
                
                for (int i = 0; i < filteredNodes.size(); i++) {
                    NodeDefinition def = filteredNodes.get(i);
                    int itemY = contentY + 3 + i * itemHeight - (int)scrollAmount;
                    
                    // Optimization: only render visible items
                    if (itemY + itemHeight < contentY || itemY > contentY + height) continue;
                    
                    boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + itemHeight;
                    
                    if (hovered || i == selectedIndex) {
                        guiGraphics.fill(x + 1, itemY, x + width - 1, itemY + itemHeight, 0x44FFFFFF);
                    }
                    
                    String name = Component.translatable(def.name()).getString();
                    String cat = Component.translatable(def.category()).getString();
                    guiGraphics.drawString(font, name, x + 8, itemY + 4, 0xFFFFFFFF, false);
                    int catW = font.width(cat);
                    guiGraphics.drawString(font, cat, x + width - catW - 8, itemY + 4, 0xFF666666, false);
                }
                guiGraphics.disableScissor();
                
                // Scrollbar
                if (totalHeight > height - 6) {
                    renderScrollbar(guiGraphics, x + width - 4, contentY + 3, 2, height - 6, scrollAmount, totalHeight);
                }
            }
        } else {
            // --- Category Mode ---
            Map<String, List<NodeDefinition>> categories = new HashMap<>();
            for (NodeDefinition def : NodeRegistry.getAll()) {
                categories.computeIfAbsent(def.category(), k -> new ArrayList<>()).add(def);
            }
            
            List<String> sortedCategories = new ArrayList<>(categories.keySet());
            sortedCategories.sort((a, b) -> Component.translatable(a).getString().compareTo(Component.translatable(b).getString()));
            
            int displayCount = Math.min(sortedCategories.size(), maxVisibleItems);
            int height = displayCount * itemHeight + 6;
            
            if (contentY + height > screenHeight) contentY = y - height;

            guiGraphics.fill(x, contentY, x + width, contentY + height, 0xF01E1E1E);
            guiGraphics.renderOutline(x, contentY, width, height, 0xFF444444);
            
            guiGraphics.enableScissor(x, contentY + 3, x + width, contentY + height - 3);
            int totalHeight = sortedCategories.size() * itemHeight;
            int maxScroll = Math.max(0, totalHeight - (displayCount * itemHeight));
            scrollAmount = Mth.clamp(scrollAmount, 0, maxScroll);

            String currentHoveredCat = null;
            for (int i = 0; i < sortedCategories.size(); i++) {
                String catKey = sortedCategories.get(i);
                int itemY = contentY + 3 + i * itemHeight - (int)scrollAmount;
                
                if (itemY + itemHeight < contentY || itemY > contentY + height) continue;

                boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + itemHeight;
                
                if (hovered) {
                    guiGraphics.fill(x + 1, itemY, x + width - 1, itemY + itemHeight, 0x44FFFFFF);
                    currentHoveredCat = catKey;
                }
                
                guiGraphics.drawString(font, Component.translatable(catKey), x + 8, itemY + 4, 0xFFFFFFFF, false);
                guiGraphics.drawString(font, ">", x + width - 15, itemY + 4, 0xFF888888, false);
            }
            guiGraphics.disableScissor();
            
            if (totalHeight > height - 6) {
                renderScrollbar(guiGraphics, x + width - 4, contentY + 3, 2, height - 6, scrollAmount, totalHeight);
            }
            
            if (currentHoveredCat != null) {
                hoveredCategory = currentHoveredCat;
            }
            
            if (hoveredCategory != null) {
                List<NodeDefinition> catNodes = categories.get(hoveredCategory);
                if (catNodes != null) {
                    int subWidth = 150;
                    int subX = (x + width + subWidth > screenWidth) ? x - subWidth : x + width;
                    
                    int catIndex = sortedCategories.indexOf(hoveredCategory);
                    int subY = contentY + catIndex * itemHeight - (int)scrollAmount;
                    int subDisplayCount = Math.min(catNodes.size(), maxVisibleItems);
                    int subHeight = subDisplayCount * itemHeight + 6;
                    
                    if (subY + subHeight > screenHeight) subY = screenHeight - subHeight - 5;
                    if (subY < 0) subY = 5;

                    guiGraphics.fill(subX, subY, subX + subWidth, subY + subHeight, 0xF01E1E1E);
                    guiGraphics.renderOutline(subX, subY, subWidth, subHeight, 0xFF444444);
                    
                    guiGraphics.enableScissor(subX, subY + 3, subX + subWidth, subY + subHeight - 3);
                    int subTotalHeight = catNodes.size() * itemHeight;
                    int subMaxScroll = Math.max(0, subTotalHeight - (subDisplayCount * itemHeight));
                    subScrollAmount = Mth.clamp(subScrollAmount, 0, subMaxScroll);

                    for (int i = 0; i < catNodes.size(); i++) {
                        NodeDefinition def = catNodes.get(i);
                        int itemY = subY + 3 + i * itemHeight - (int)subScrollAmount;
                        
                        if (itemY + itemHeight < subY || itemY > subY + subHeight) continue;

                        boolean hovered = mouseX >= subX && mouseX <= subX + subWidth && mouseY >= itemY && mouseY <= itemY + itemHeight;
                        
                        if (hovered) {
                            guiGraphics.fill(subX + 1, itemY, subX + subWidth - 1, itemY + itemHeight, 0x44FFFFFF);
                        }
                        
                        guiGraphics.drawString(font, Component.translatable(def.name()), subX + 8, itemY + 4, 0xFFFFFFFF, false);
                    }
                    guiGraphics.disableScissor();
                    
                    if (subTotalHeight > subHeight - 6) {
                        renderScrollbar(guiGraphics, subX + subWidth - 4, subY + 3, 2, subHeight - 6, subScrollAmount, subTotalHeight);
                    }
                }
            }
        }
    }

    private void renderScrollbar(GuiGraphics guiGraphics, int x, int y, int width, int height, float scroll, int totalHeight) {
        guiGraphics.fill(x, y, x + width, y + height, 0x22FFFFFF);
        int barHeight = (int) ((height / (float) totalHeight) * height);
        int barY = y + (int) ((scroll / (float) totalHeight) * height);
        guiGraphics.fill(x, barY, x + width, barY + barHeight, 0x88FFFFFF);
    }

    private void updateSearch() {
        String query = searchQuery.toLowerCase();
        filteredNodes = NodeRegistry.getAll().stream()
            .filter(def -> {
                String localizedName = Component.translatable(def.name()).getString().toLowerCase();
                String localizedCat = Component.translatable(def.category()).getString().toLowerCase();
                String rawName = def.name().toLowerCase();
                String rawCat = def.category().toLowerCase();
                
                return localizedName.contains(query) || 
                       localizedCat.contains(query) || 
                       rawName.contains(query) || 
                       rawCat.contains(query);
            })
            .collect(Collectors.toList());
        
        if (selectedIndex >= filteredNodes.size()) {
            selectedIndex = Math.max(0, filteredNodes.size() - 1);
        }
    }

    public void reset() {
        hoveredCategory = null;
        searchQuery = "";
        selectedIndex = 0;
        filteredNodes.clear();
        scrollAmount = 0;
        subScrollAmount = 0;
    }

    public void mouseScrolled(double mouseX, double mouseY, double amount) {
        // Simple logic: if mouse is on the right side (sub-menu), scroll sub-menu
        // Otherwise scroll main menu
        if (hoveredCategory != null) {
            // This is a bit simplified, but should work for most cases
            // We'd need the actual subX to be precise
            subScrollAmount -= (float) (amount * 15);
        } else {
            scrollAmount -= (float) (amount * 15);
        }
    }

    public enum ContextMenuResult {
        DELETE, BREAK_LINKS, NONE
    }

    public ContextMenuResult onClickContextMenu(double mouseX, double mouseY, double menuX, double menuY) {
        int x = (int) menuX;
        int y = (int) menuY;
        int width = 120;
        
        if (mouseX >= x && mouseX <= x + width && mouseY >= y + 3 && mouseY <= y + 23) {
            return ContextMenuResult.DELETE;
        }
        if (mouseX >= x && mouseX <= x + width && mouseY >= y + 23 && mouseY <= y + 43) {
            return ContextMenuResult.BREAK_LINKS;
        }
        return ContextMenuResult.NONE;
    }

    public NodeDefinition onClickNodeMenu(double mouseX, double mouseY, double menuX, double menuY, int screenWidth, int screenHeight) {
        int x = (int) menuX;
        int y = (int) menuY;
        int width = 180;
        if (x + width > screenWidth) x -= width;

        int itemHeight = 18;
        int maxVisibleItems = 12;

        if (!searchQuery.isEmpty()) {
            int contentY = y + 25 + 2;
            int displayCount = Math.min(filteredNodes.size(), maxVisibleItems);
            int height = displayCount * itemHeight + 6;
            if (contentY + height > screenHeight) contentY = y - height;

            for (int i = 0; i < filteredNodes.size(); i++) {
                int itemY = contentY + 3 + i * itemHeight - (int)scrollAmount;
                if (itemY + itemHeight < contentY || itemY > contentY + height) continue;
                if (mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + itemHeight) {
                    return filteredNodes.get(i);
                }
            }
        } else {
            Map<String, List<NodeDefinition>> categories = new HashMap<>();
            for (NodeDefinition def : NodeRegistry.getAll()) {
                categories.computeIfAbsent(def.category(), k -> new ArrayList<>()).add(def);
            }
            List<String> sortedCategories = new ArrayList<>(categories.keySet());
            sortedCategories.sort((a, b) -> Component.translatable(a).getString().compareTo(Component.translatable(b).getString()));

            int contentY = y + 25 + 2;
            int displayCount = Math.min(sortedCategories.size(), maxVisibleItems);
            int height = displayCount * itemHeight + 6;
            if (contentY + height > screenHeight) contentY = y - height;

            if (hoveredCategory != null) {
                List<NodeDefinition> catNodes = categories.get(hoveredCategory);
                int subWidth = 150;
                int subX = (x + width + subWidth > screenWidth) ? x - subWidth : x + width;
                
                int catIndex = sortedCategories.indexOf(hoveredCategory);
                int subY = contentY + catIndex * itemHeight - (int)scrollAmount;
                int subDisplayCount = Math.min(catNodes.size(), maxVisibleItems);
                int subHeight = subDisplayCount * itemHeight + 6;
                if (subY + subHeight > screenHeight) subY = screenHeight - subHeight - 5;
                if (subY < 0) subY = 5;

                for (int i = 0; i < catNodes.size(); i++) {
                    int itemY = subY + 3 + i * itemHeight - (int)subScrollAmount;
                    if (itemY + itemHeight < subY || itemY > subY + subHeight) continue;
                    if (mouseX >= subX && mouseX <= subX + subWidth && mouseY >= itemY && mouseY <= itemY + itemHeight) {
                        return catNodes.get(i);
                    }
                }
            }
        }
        return null;
    }

    public boolean isClickInsideNodeMenu(double mouseX, double mouseY, double menuX, double menuY, int screenWidth, int screenHeight) {
        int x = (int) menuX;
        int y = (int) menuY;
        int width = 180;
        if (x + width > screenWidth) x -= width;

        // Search box
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 25) return true;

        int contentY = y + 25 + 2;
        int maxVisibleItems = 12;
        if (!searchQuery.isEmpty()) {
            int displayCount = Math.min(filteredNodes.size(), maxVisibleItems);
            int height = displayCount * 18 + 6;
            if (contentY + height > screenHeight) contentY = y - height;
            if (mouseX >= x && mouseX <= x + width && mouseY >= contentY && mouseY <= contentY + height) return true;
        } else {
            int catCount = (int) NodeRegistry.getAll().stream().map(NodeDefinition::category).distinct().count();
            int displayCount = Math.min(catCount, maxVisibleItems);
            int height = displayCount * 18 + 6;
            if (contentY + height > screenHeight) contentY = y - height;
            if (mouseX >= x && mouseX <= x + width && mouseY >= contentY && mouseY <= contentY + height) return true;

            if (hoveredCategory != null) {
                int subWidth = 150;
                int subX = (x + width + subWidth > screenWidth) ? x - subWidth : x + width;
                int count = (int) NodeRegistry.getAll().stream().filter(d -> d.category().equals(hoveredCategory)).count();
                int subDisplayCount = Math.min(count, maxVisibleItems);
                int subHeight = subDisplayCount * 18 + 6;
                // Simplified check
                if (mouseX >= subX && mouseX <= subX + subWidth) return true;
            }
        }
        return false;
    }

    public boolean keyPressed(int key) {
        if (key == 259) { // Backspace
            if (!searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                updateSearch();
                scrollAmount = 0; // Reset scroll on search
                return true;
            }
        } else if (key == 257) { // Enter
            if (!filteredNodes.isEmpty() && !searchQuery.isEmpty()) {
                // This will be handled by the handler to create the node
                return true; 
            }
        } else if (key == 265) { // Up
            selectedIndex = Math.max(0, selectedIndex - 1);
            // Auto-scroll to selected
            scrollAmount = Math.min(scrollAmount, selectedIndex * 18);
            return true;
        } else if (key == 264) { // Down
            selectedIndex = Math.min(filteredNodes.size() - 1, selectedIndex + 1);
            // Auto-scroll to selected
            scrollAmount = Math.max(scrollAmount, (selectedIndex - 11) * 18);
            return true;
        }
        return false;
    }

    public boolean charTyped(char codePoint) {
        searchQuery += codePoint;
        updateSearch();
        scrollAmount = 0; // Reset scroll on search
        return true;
    }

    public NodeDefinition getSelectedNode() {
        if (!filteredNodes.isEmpty() && selectedIndex >= 0 && selectedIndex < filteredNodes.size()) {
            return filteredNodes.get(selectedIndex);
        }
        return null;
    }
}
