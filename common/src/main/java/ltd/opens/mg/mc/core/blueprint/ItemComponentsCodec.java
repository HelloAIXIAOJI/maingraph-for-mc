package ltd.opens.mg.mc.core.blueprint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import ltd.opens.mg.mc.MaingraphforMC;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 物品数据组件编解码器：把蓝图 {@code COMPONENTS} 端口的 JSON 配置应用到 {@link ItemStack}，
 * 并执行时将配置写回 JSON 字符串。
 *
 * <p>组件写入逻辑参考 CAD Editor（MIT License, Copyright (c) 2022 Franck Velasco；
 * 基于 IBE Editor by Skye, MIT）。for-mc 构建于 Minecraft 1.21.1，
 * 因此 {@code CustomModelData} 采用单 float 形式（多字段 API 在 1.21.4+ 才提供）。
 */
public final class ItemComponentsCodec {
    public static final String JSON_KEY_ENCHANTMENTS = "enchantments";
    public static final String JSON_KEY_STORED = "stored_enchantments";
    public static final String JSON_KEY_CUSTOM_NAME = "custom_name";
    public static final String JSON_KEY_LORE = "lore";
    public static final String JSON_KEY_CUSTOM_MODEL_DATA = "custom_model_data";
    public static final String JSON_KEY_ATTRIBUTE_MODIFIERS = "attribute_modifiers";

    private ItemComponentsCodec() {
    }

    /**
     * 将 JSON 配置应用到物品栈。配置为空或解析失败时忽略（不影响已设置的其它组件）。
     */
    public static void apply(ItemStack stack, String json, RegistryAccess registryAccess) {
        if (json == null || json.isBlank()) {
            return;
        }
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            applyEnchantments(stack, root, registryAccess);
            applyCustomName(stack, root, registryAccess);
            applyLore(stack, root, registryAccess);
            applyCustomModelData(stack, root);
            applyAttributeModifiers(stack, root, registryAccess);
        } catch (Exception e) {
            MaingraphforMC.LOGGER.error("Failed to apply item components: " + json, e);
        }
    }

    private static void applyEnchantments(ItemStack stack, JsonObject root, RegistryAccess access) {
        if (!root.has(JSON_KEY_ENCHANTMENTS)) {
            return;
        }
        JsonArray arr = root.getAsJsonArray(JSON_KEY_ENCHANTMENTS);
        boolean stored = root.has(JSON_KEY_STORED) && root.get(JSON_KEY_STORED).getAsBoolean();
        var lookupOpt = access.lookup(Registries.ENCHANTMENT);
        if (lookupOpt.isEmpty()) {
            return;
        }
        var lookup = lookupOpt.get();
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (JsonElement e : arr) {
            JsonObject o = e.getAsJsonObject();
            String id = o.has("id") ? o.get("id").getAsString() : "";
            int lvl = o.has("lvl") ? o.get("lvl").getAsInt() : 1;
            if (lvl <= 0) {
                continue;
            }
            ResourceLocation rl = ResourceLocation.tryParse(id);
            if (rl == null) {
                continue;
            }
            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, rl);
            lookup.get(key).ifPresent(holder -> mutable.set(holder, lvl));
        }
        ItemEnchantments applied = mutable.toImmutable();
        if (applied.isEmpty()) {
            stack.remove(stored ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS);
        } else {
            stack.set(stored ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS, applied);
        }
    }

    private static void applyCustomName(ItemStack stack, JsonObject root, RegistryAccess access) {
        if (!root.has(JSON_KEY_CUSTOM_NAME)) {
            return;
        }
        String raw = root.get(JSON_KEY_CUSTOM_NAME).getAsString();
        if (raw == null || raw.isBlank()) {
            stack.remove(DataComponents.CUSTOM_NAME);
            return;
        }
        Component component = parseComponent(raw, access);
        if (component != null) {
            stack.set(DataComponents.CUSTOM_NAME, component);
        }
    }

    private static void applyLore(ItemStack stack, JsonObject root, RegistryAccess access) {
        if (!root.has(JSON_KEY_LORE)) {
            return;
        }
        JsonArray arr = root.getAsJsonArray(JSON_KEY_LORE);
        List<Component> lines = new ArrayList<>();
        for (JsonElement e : arr) {
            Component component = parseComponent(e.getAsString(), access);
            if (component != null) {
                lines.add(component);
            }
        }
        if (lines.isEmpty()) {
            stack.remove(DataComponents.LORE);
        } else {
            stack.set(DataComponents.LORE, new ItemLore(lines));
        }
    }

    private static void applyCustomModelData(ItemStack stack, JsonObject root) {
        if (!root.has(JSON_KEY_CUSTOM_MODEL_DATA)) {
            return;
        }
        JsonElement el = root.get(JSON_KEY_CUSTOM_MODEL_DATA);
        int value;
        if (el.isJsonPrimitive()) {
            value = el.getAsInt();
        } else if (el.isJsonObject()) {
            JsonObject o = el.getAsJsonObject();
            if (o.has("value")) {
                value = o.get("value").getAsInt();
            } else if (o.has("int")) {
                value = o.get("int").getAsInt();
            } else {
                return;
            }
        } else {
            return;
        }
        if (value == 0) {
            stack.remove(DataComponents.CUSTOM_MODEL_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(value));
        }
    }

    private static void applyAttributeModifiers(ItemStack stack, JsonObject root, RegistryAccess access) {
        if (!root.has(JSON_KEY_ATTRIBUTE_MODIFIERS)) {
            return;
        }
        JsonArray arr = root.getAsJsonArray(JSON_KEY_ATTRIBUTE_MODIFIERS);
        var lookupOpt = access.lookup(Registries.ATTRIBUTE);
        if (lookupOpt.isEmpty()) {
            return;
        }
        var attrLookup = lookupOpt.get();
        List<ItemAttributeModifiers.Entry> entries = new ArrayList<>();
        for (JsonElement e : arr) {
            JsonObject o = e.getAsJsonObject();
            String attrName = o.has("attribute") ? o.get("attribute").getAsString() : "";
            ResourceLocation rl = ResourceLocation.tryParse(attrName);
            if (rl == null) {
                continue;
            }
            ResourceKey<Attribute> key = ResourceKey.create(Registries.ATTRIBUTE, rl);
            var holderOpt = attrLookup.get(key);
            if (holderOpt.isEmpty()) {
                continue;
            }
            Holder<Attribute> holder = holderOpt.get();
            String slotName = o.has("slot") ? o.get("slot").getAsString() : "any";
            EquipmentSlotGroup group = fromSlot(slotName);
            int op = o.has("operation") ? o.get("operation").getAsInt() : 0;
            double amount = o.has("amount") ? o.get("amount").getAsDouble() : 0d;
            ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath(
                    "mgmc", "attr_" + UUID.randomUUID().toString().substring(0, 8));
            AttributeModifier modifier = new AttributeModifier(modifierId, amount, operationFromIndex(op));
            entries.add(new ItemAttributeModifiers.Entry(holder, modifier, group));
        }
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(entries, false));
    }

    private static Component parseComponent(String json, RegistryAccess access) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            JsonElement element = JsonParser.parseString(json);
            var ops = RegistryOps.create(JsonOps.INSTANCE, access);
            return ComponentSerialization.CODEC.parse(ops, element).result().orElse(null);
        } catch (Exception e) {
            MaingraphforMC.LOGGER.error("Failed to parse component json: " + json, e);
            return null;
        }
    }

    private static EquipmentSlotGroup fromSlot(String s) {
        if (s == null) {
            return EquipmentSlotGroup.ANY;
        }
        return switch (s) {
            case "mainhand" -> EquipmentSlotGroup.MAINHAND;
            case "offhand" -> EquipmentSlotGroup.OFFHAND;
            case "feet" -> EquipmentSlotGroup.FEET;
            case "legs" -> EquipmentSlotGroup.LEGS;
            case "chest" -> EquipmentSlotGroup.CHEST;
            case "head" -> EquipmentSlotGroup.HEAD;
            case "hand" -> EquipmentSlotGroup.HAND;
            case "armor" -> EquipmentSlotGroup.ARMOR;
            case "body" -> EquipmentSlotGroup.BODY;
            default -> EquipmentSlotGroup.ANY;
        };
    }

    private static AttributeModifier.Operation operationFromIndex(int index) {
        AttributeModifier.Operation[] values = AttributeModifier.Operation.values();
        if (index < 0 || index >= values.length) {
            return values[0];
        }
        return values[index];
    }
}
