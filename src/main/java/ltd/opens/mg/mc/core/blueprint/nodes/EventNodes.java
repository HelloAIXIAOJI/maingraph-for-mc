package ltd.opens.mg.mc.core.blueprint.nodes;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;

import java.util.Arrays;

/**
 * 事件类节点注册
 * 包含节点定义及其对应的数据提取逻辑
 */
public class EventNodes {
    private static final int COLOR_EVENT_WORLD = 0xFF880000;
    private static final int COLOR_EVENT_PLAYER = 0xFF0088FF;
    private static final int COLOR_EVENT_ENTITY = 0xFF0088FF;

    private static final int COLOR_STRING = 0xFFFFCC00;
    private static final int COLOR_FLOAT = 0xFF00FF00;
    private static final int COLOR_UUID = 0xFFCC00FF;
    private static final int COLOR_LIST = 0xFF00FFFF;

    public static void register() {
        // --- 世界事件 ---
        NodeHelper.setup("on_mgrun", "node.mgmc.on_mgrun.name")
            .category("node_category.mgmc.events.world")
            .color(COLOR_EVENT_WORLD)
            .execOut()
            .output("name", "node.mgmc.port.name", NodeDefinition.PortType.STRING, COLOR_STRING)
            .output("parameters", "node.mgmc.on_mgrun.port.parameters", NodeDefinition.PortType.LIST, COLOR_LIST)
            .output("trigger_uuid", "node.mgmc.on_mgrun.port.trigger_uuid", NodeDefinition.PortType.UUID, COLOR_UUID)
            .output("trigger_name", "node.mgmc.on_mgrun.port.trigger_name", NodeDefinition.PortType.STRING, COLOR_STRING)
            .output("trigger_x", "node.mgmc.on_mgrun.port.trigger_x", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("trigger_y", "node.mgmc.on_mgrun.port.trigger_y", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("trigger_z", "node.mgmc.on_mgrun.port.trigger_z", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case "name" -> ctx.eventName;
                case "parameters" -> Arrays.asList(ctx.args);
                case "trigger_uuid" -> ctx.triggerUuid != null ? ctx.triggerUuid : "";
                case "trigger_name" -> ctx.triggerName != null ? ctx.triggerName : "";
                case "trigger_x" -> ctx.triggerX;
                case "trigger_y" -> ctx.triggerY;
                case "trigger_z" -> ctx.triggerZ;
                default -> null;
            });

        // --- 玩家事件 ---
        NodeHelper.setup("on_player_move", "node.mgmc.on_player_move.name")
            .category("node_category.mgmc.events.player")
            .color(COLOR_EVENT_PLAYER)
            .execOut()
            .output("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("speed", "node.mgmc.on_player_move.port.speed", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, COLOR_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case "x" -> ctx.triggerX;
                case "y" -> ctx.triggerY;
                case "z" -> ctx.triggerZ;
                case "speed" -> ctx.triggerSpeed;
                case "uuid" -> ctx.triggerUuid;
                default -> null;
            });

        NodeHelper.setup("on_break_block", "node.mgmc.on_break_block.name")
            .category("node_category.mgmc.events.player")
            .color(COLOR_EVENT_PLAYER)
            .execOut()
            .output("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("block_id", "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, COLOR_STRING)
            .output("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, COLOR_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case "x" -> ctx.triggerX;
                case "y" -> ctx.triggerY;
                case "z" -> ctx.triggerZ;
                case "block_id" -> ctx.triggerBlockId;
                case "uuid" -> ctx.triggerUuid;
                default -> null;
            });

        NodeHelper.setup("on_place_block", "node.mgmc.on_place_block.name")
            .category("node_category.mgmc.events.player")
            .color(COLOR_EVENT_PLAYER)
            .execOut()
            .output("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("block_id", "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, COLOR_STRING)
            .output("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, COLOR_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case "x" -> ctx.triggerX;
                case "y" -> ctx.triggerY;
                case "z" -> ctx.triggerZ;
                case "block_id" -> ctx.triggerBlockId;
                case "uuid" -> ctx.triggerUuid;
                default -> null;
            });

        NodeHelper.setup("on_interact_block", "node.mgmc.on_interact_block.name")
            .category("node_category.mgmc.events.player")
            .color(COLOR_EVENT_PLAYER)
            .execOut()
            .output("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("block_id", "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, COLOR_STRING)
            .output("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, COLOR_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case "x" -> ctx.triggerX;
                case "y" -> ctx.triggerY;
                case "z" -> ctx.triggerZ;
                case "block_id" -> ctx.triggerBlockId;
                case "uuid" -> ctx.triggerUuid;
                default -> null;
            });

        NodeHelper.setup("on_player_join", "node.mgmc.on_player_join.name")
            .category("node_category.mgmc.events.player")
            .color(COLOR_EVENT_PLAYER)
            .execOut()
            .output("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, COLOR_UUID)
            .output("name", "node.mgmc.port.name", NodeDefinition.PortType.STRING, COLOR_STRING)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case "uuid" -> ctx.triggerUuid;
                case "name" -> ctx.triggerName;
                default -> null;
            });

        NodeHelper.setup("on_player_death", "node.mgmc.on_player_death.name")
            .category("node_category.mgmc.events.player")
            .color(COLOR_EVENT_PLAYER)
            .execOut()
            .output("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, COLOR_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case "x" -> ctx.triggerX;
                case "y" -> ctx.triggerY;
                case "z" -> ctx.triggerZ;
                case "uuid" -> ctx.triggerUuid;
                default -> null;
            });

        NodeHelper.setup("on_player_respawn", "node.mgmc.on_player_respawn.name")
            .category("node_category.mgmc.events.player")
            .color(COLOR_EVENT_PLAYER)
            .execOut()
            .output("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, COLOR_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case "x" -> ctx.triggerX;
                case "y" -> ctx.triggerY;
                case "z" -> ctx.triggerZ;
                case "uuid" -> ctx.triggerUuid;
                default -> null;
            });

        NodeHelper.setup("on_player_hurt", "node.mgmc.on_player_hurt.name")
            .category("node_category.mgmc.events.player")
            .color(COLOR_EVENT_PLAYER)
            .execOut()
            .output("damage_amount", "node.mgmc.port.damage_amount", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("attacker_uuid", "node.mgmc.port.attacker_uuid", NodeDefinition.PortType.UUID, COLOR_UUID)
            .output("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, COLOR_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case "damage_amount" -> ctx.triggerValue;
                case "attacker_uuid" -> ctx.triggerExtraUuid;
                case "uuid" -> ctx.triggerUuid;
                default -> null;
            });

        NodeHelper.setup("on_use_item", "node.mgmc.on_use_item.name")
            .category("node_category.mgmc.events.player")
            .color(COLOR_EVENT_PLAYER)
            .execOut()
            .output("item_id", "node.mgmc.port.item_id", NodeDefinition.PortType.STRING, COLOR_STRING)
            .output("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, COLOR_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case "item_id" -> ctx.triggerItemId;
                case "uuid" -> ctx.triggerUuid;
                default -> null;
            });

        NodeHelper.setup("on_player_attack", "node.mgmc.on_player_attack.name")
            .category("node_category.mgmc.events.player")
            .color(COLOR_EVENT_PLAYER)
            .execOut()
            .output("victim_uuid", "node.mgmc.port.victim_uuid", NodeDefinition.PortType.UUID, COLOR_UUID)
            .output("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, COLOR_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case "victim_uuid" -> ctx.triggerExtraUuid;
                case "uuid" -> ctx.triggerUuid;
                default -> null;
            });

        // --- 实体事件 ---
        NodeHelper.setup("on_entity_death", "node.mgmc.on_entity_death.name")
            .category("node_category.mgmc.events.entity")
            .color(COLOR_EVENT_ENTITY)
            .execOut()
            .output("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("victim_uuid", "node.mgmc.port.victim_uuid", NodeDefinition.PortType.UUID, COLOR_UUID)
            .output("attacker_uuid", "node.mgmc.port.attacker_uuid", NodeDefinition.PortType.UUID, COLOR_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case "x" -> ctx.triggerX;
                case "y" -> ctx.triggerY;
                case "z" -> ctx.triggerZ;
                case "victim_uuid" -> ctx.triggerUuid;
                case "attacker_uuid" -> ctx.triggerExtraUuid;
                default -> null;
            });

        NodeHelper.setup("on_entity_hurt", "node.mgmc.on_entity_hurt.name")
            .category("node_category.mgmc.events.entity")
            .color(COLOR_EVENT_ENTITY)
            .execOut()
            .output("damage_amount", "node.mgmc.port.damage_amount", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("victim_uuid", "node.mgmc.port.victim_uuid", NodeDefinition.PortType.UUID, COLOR_UUID)
            .output("attacker_uuid", "node.mgmc.port.attacker_uuid", NodeDefinition.PortType.UUID, COLOR_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case "damage_amount" -> ctx.triggerValue;
                case "victim_uuid" -> ctx.triggerUuid;
                case "attacker_uuid" -> ctx.triggerExtraUuid;
                default -> null;
            });

        NodeHelper.setup("on_entity_spawn", "node.mgmc.on_entity_spawn.name")
            .category("node_category.mgmc.events.entity")
            .color(COLOR_EVENT_ENTITY)
            .execOut()
            .output("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, COLOR_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case "x" -> ctx.triggerX;
                case "y" -> ctx.triggerY;
                case "z" -> ctx.triggerZ;
                case "uuid" -> ctx.triggerUuid;
                default -> null;
            });
    }
}
