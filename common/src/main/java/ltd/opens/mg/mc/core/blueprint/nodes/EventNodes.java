package ltd.opens.mg.mc.core.blueprint.nodes;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.events.MGMCEventType;
import ltd.opens.mg.mc.core.blueprint.ItemComponentsCodec;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.routing.BlueprintRouter;
import net.minecraft.world.entity.player.Player;
import ltd.opens.mg.mc.core.blueprint.data.XYZ;

import java.util.*;

/**
 * 事件类节点注册
 * 包含节点定义及其对应的数据提取逻辑
 */
public class EventNodes {

    public static void register() {
        // --- 世界事件 ---
        NodeHelper.setup("on_mgrun", "node.mgmc.on_mgrun.name")
            .category("node_category.mgmc.events.world")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_mgrun.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/world/on_mgrun")
            .execOut()
            .output(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.PARAMETERS, "node.mgmc.on_mgrun.port.parameters", NodeDefinition.PortType.LIST, NodeThemes.COLOR_PORT_LIST)
            .output(NodePorts.TRIGGER_ENTITY, "node.mgmc.port.trigger_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.TRIGGER_NAME, "node.mgmc.port.trigger_name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case NodePorts.NAME -> ctx.eventName;
                case NodePorts.PARAMETERS -> ctx.args != null ? Arrays.asList(ctx.args) : Collections.emptyList();
                case NodePorts.TRIGGER_ENTITY -> ctx.triggerEntity;
                case NodePorts.TRIGGER_NAME -> ctx.triggerName != null ? ctx.triggerName : "";
                case NodePorts.XYZ -> ctx.triggerXYZ;
                default -> null;
            });

        // --- 玩家事件 ---
        NodeHelper.setup("on_break_block", "node.mgmc.on_break_block.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_break_block.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_break_block")
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.BLOCK_ID, "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.BLOCK_BREAK, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getPos() != null) {
                    b.triggerX(e.getPos().getX()).triggerY(e.getPos().getY()).triggerZ(e.getPos().getZ());
                }
                if (e.getBlockState() != null) {
                    b.triggerBlockId(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getBlockState().getBlock()).toString());
                }
            }, e -> e.getBlockState() != null ? net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getBlockState().getBlock()).toString() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.BLOCK_ID -> ctx.triggerBlockId;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_place_block", "node.mgmc.on_place_block.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_place_block.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_place_block")
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.BLOCK_ID, "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.BLOCK_PLACE, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getPos() != null) {
                    b.triggerX(e.getPos().getX()).triggerY(e.getPos().getY()).triggerZ(e.getPos().getZ());
                }
                if (e.getBlockState() != null) {
                    b.triggerBlockId(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getBlockState().getBlock()).toString());
                }
            }, e -> e.getBlockState() != null ? net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getBlockState().getBlock()).toString() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.BLOCK_ID -> ctx.triggerBlockId;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_interact_block", "node.mgmc.on_interact_block.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_interact_block")
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.BLOCK_ID, "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.BLOCK_INTERACT, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getPos() != null) {
                    b.triggerX(e.getPos().getX()).triggerY(e.getPos().getY()).triggerZ(e.getPos().getZ());
                }
                if (e.getBlockState() != null) {
                    b.triggerBlockId(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getBlockState().getBlock()).toString());
                }
            }, e -> e.getBlockState() != null ? net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getBlockState().getBlock()).toString() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.BLOCK_ID -> ctx.triggerBlockId;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_left_click_block", "node.mgmc.on_left_click_block.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_left_click_block.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_left_click_block")
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.BLOCK_ID, "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.BLOCK_LEFT_CLICK, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getPos() != null) {
                    b.triggerX(e.getPos().getX()).triggerY(e.getPos().getY()).triggerZ(e.getPos().getZ());
                }
                if (e.getBlockState() != null) {
                    b.triggerBlockId(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getBlockState().getBlock()).toString());
                }
            }, e -> e.getBlockState() != null ? net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getBlockState().getBlock()).toString() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.BLOCK_ID -> ctx.triggerBlockId;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_join", "node.mgmc.on_player_join.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_player_join.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_join")
            .execOut()
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .registerEvent(MGMCEventType.PLAYER_JOIN, (e, b) -> {
                if (e.getEntity() != null) {
                    b.triggerUuid(e.getEntity().getUUID().toString())
                     .triggerName(e.getEntity().getName().getString())
                     .triggerEntity(e.getEntity());
                }
            }, e -> BlueprintRouter.PLAYERS_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.ENTITY -> ctx.triggerEntity;
                case NodePorts.NAME -> ctx.triggerName;
                default -> null;
            });

        NodeHelper.setup("on_player_death", "node.mgmc.on_player_death.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_death")
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.ATTACKER_ENTITY, "node.mgmc.port.attacker_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.PLAYER_DEATH, (e, b) -> {
                if (e.getEntity() != null) {
                    b.triggerUuid(e.getEntity().getUUID().toString())
                     .triggerName(e.getEntity().getName().getString())
                     .triggerEntity(e.getEntity())
                     .triggerX(e.getEntity().getX()).triggerY(e.getEntity().getY()).triggerZ(e.getEntity().getZ());
                }
                if (e.getDamageSource() != null) {
                    b.triggerExtraUuid(e.getDamageSource().getEntity() != null ? e.getDamageSource().getEntity().getUUID().toString() : "")
                     .triggerExtraEntity(e.getDamageSource().getEntity());
                }
            }, e -> e.getEntity() instanceof Player ? BlueprintRouter.PLAYERS_ID : null,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                case NodePorts.ATTACKER_ENTITY -> ctx.triggerExtraEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_respawn", "node.mgmc.on_player_respawn.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_respawn")
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.PLAYER_RESPAWN, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer())
                     .triggerX(e.getPlayer().getX()).triggerY(e.getPlayer().getY()).triggerZ(e.getPlayer().getZ());
                }
            }, e -> BlueprintRouter.PLAYERS_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_leave", "node.mgmc.on_player_leave.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_leave")
            .execOut()
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .registerEvent(MGMCEventType.PLAYER_LEAVE, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
            }, e -> BlueprintRouter.PLAYERS_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.ENTITY -> ctx.triggerEntity;
                case NodePorts.NAME -> ctx.triggerName;
                default -> null;
            });

        NodeHelper.setup("on_player_tick", "node.mgmc.on_player_tick.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_player_tick.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_tick")
            .execOut()
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .registerEvent(MGMCEventType.PLAYER_TICK, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
            }, e -> BlueprintRouter.PLAYERS_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.ENTITY -> ctx.triggerEntity;
                case NodePorts.NAME -> ctx.triggerName;
                default -> null;
            });

        NodeHelper.setup("on_player_move", "node.mgmc.on_player_move.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_player_move.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_move")
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.SPEED, "node.mgmc.port.speed", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.PLAYER_MOVE, (e, b) -> {
                if (e.getPlayer() != null && e.getXyz() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer())
                     .triggerX(e.getXyz().x()).triggerY(e.getXyz().y()).triggerZ(e.getXyz().z())
                     .triggerSpeed(e.getSpeed());
                }
            }, e -> BlueprintRouter.PLAYERS_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.SPEED -> ctx.triggerSpeed;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_item_pickup", "node.mgmc.on_item_pickup.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_item_pickup.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_item_pickup")
            .execOut()
            .output(NodePorts.ITEM_ID, "node.mgmc.port.item_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.COMPONENTS, "node.mgmc.port.components", NodeDefinition.PortType.COMPONENTS, 0xFFE0A000)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.ITEM_PICKUP, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getItemId() != null) {
                    b.triggerItemId(e.getItemId());
                }
                if (e.getItem() != null) {
                    b.triggerItem(e.getItem());
                }
            }, e -> e.getItemId() != null ? e.getItemId() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.ITEM_ID -> ctx.triggerItemId;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                case NodePorts.COMPONENTS -> ctx.triggerItem != null ? ItemComponentsCodec.serialize(ctx.triggerItem, ctx.level.registryAccess()) : "{}";
                default -> null;
            });

        NodeHelper.setup("on_player_hurt", "node.mgmc.on_player_hurt.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_player_hurt.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_hurt")
            .execOut()
            .output(NodePorts.DAMAGE_AMOUNT, "node.mgmc.port.damage_amount", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.ATTACKER_ENTITY, "node.mgmc.port.attacker_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.PLAYER_HURT, (e, b) -> {
                if (e.getEntity() != null) {
                    b.triggerUuid(e.getEntity().getUUID().toString())
                     .triggerName(e.getEntity().getName().getString())
                     .triggerEntity(e.getEntity())
                     .triggerValue(e.getAmount());
                }
                if (e.getDamageSource() != null) {
                    b.triggerExtraUuid(e.getDamageSource().getEntity() != null ? e.getDamageSource().getEntity().getUUID().toString() : "")
                     .triggerExtraEntity(e.getDamageSource().getEntity());
                }
            }, e -> e.getEntity() instanceof Player ? BlueprintRouter.PLAYERS_ID : null,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.DAMAGE_AMOUNT -> ctx.triggerValue;
                case NodePorts.ATTACKER_ENTITY -> ctx.triggerExtraEntity;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_use_item", "node.mgmc.on_use_item.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_use_item.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_use_item")
            .execOut()
            .output(NodePorts.ITEM_ID, "node.mgmc.port.item_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.COMPONENTS, "node.mgmc.port.components", NodeDefinition.PortType.COMPONENTS, 0xFFE0A000)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.ITEM_USE, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getItem() != null) {
                    b.triggerItemId(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(e.getItem().getItem()).toString());
                    b.triggerItem(e.getItem());
                }
            }, e -> e.getItem() != null ? net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(e.getItem().getItem()).toString() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.ITEM_ID -> ctx.triggerItemId;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                case NodePorts.COMPONENTS -> ctx.triggerItem != null ? ItemComponentsCodec.serialize(ctx.triggerItem, ctx.level.registryAccess()) : "{}";
                default -> null;
            });

        NodeHelper.setup("on_player_attack", "node.mgmc.on_player_attack.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_player_attack.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_attack")
            .execOut()
            .output(NodePorts.VICTIM_ENTITY, "node.mgmc.port.victim_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.PLAYER_ATTACK, (e, b) -> {
                if (e.getEntity() != null) {
                    b.triggerUuid(e.getEntity().getUUID().toString())
                     .triggerName(e.getEntity().getName().getString())
                     .triggerEntity(e.getEntity());
                }
                if (e.getTargetEntity() != null) {
                    b.triggerExtraUuid(e.getTargetEntity().getUUID().toString())
                     .triggerExtraEntity(e.getTargetEntity());
                }
            }, e -> BlueprintRouter.PLAYERS_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.VICTIM_ENTITY -> ctx.triggerExtraEntity;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        // --- 实体事件 ---
        NodeHelper.setup("on_entity_death", "node.mgmc.on_entity_death.name")
            .category("node_category.mgmc.events.entity")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/entity/on_entity_death")
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.ATTACKER_ENTITY, "node.mgmc.port.attacker_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.ENTITY_DEATH, (e, b) -> {
                if (e.getEntity() != null) {
                    b.triggerUuid(e.getEntity().getUUID().toString())
                     .triggerName(e.getEntity().getName().getString())
                     .triggerEntity(e.getEntity())
                     .triggerX(e.getEntity().getX()).triggerY(e.getEntity().getY()).triggerZ(e.getEntity().getZ());
                }
                if (e.getDamageSource() != null) {
                    b.triggerExtraUuid(e.getDamageSource().getEntity() != null ? e.getDamageSource().getEntity().getUUID().toString() : "")
                     .triggerExtraEntity(e.getDamageSource().getEntity());
                }
            }, e -> e.getEntity() != null ? net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getEntity().getType()).toString() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                case NodePorts.ATTACKER_ENTITY -> ctx.triggerExtraEntity;
                default -> null;
            });

        NodeHelper.setup("on_entity_hurt", "node.mgmc.on_entity_hurt.name")
            .category("node_category.mgmc.events.entity")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/entity/on_entity_hurt")
            .execOut()
            .output(NodePorts.DAMAGE_AMOUNT, "node.mgmc.port.damage_amount", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.ATTACKER_ENTITY, "node.mgmc.port.attacker_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.ENTITY_HURT, (e, b) -> {
                if (e.getEntity() != null) {
                    b.triggerUuid(e.getEntity().getUUID().toString())
                     .triggerName(e.getEntity().getName().getString())
                     .triggerEntity(e.getEntity())
                     .triggerValue(e.getAmount());
                }
                if (e.getDamageSource() != null) {
                    b.triggerExtraUuid(e.getDamageSource().getEntity() != null ? e.getDamageSource().getEntity().getUUID().toString() : "")
                     .triggerExtraEntity(e.getDamageSource().getEntity());
                }
            }, e -> e.getEntity() != null ? net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getEntity().getType()).toString() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.DAMAGE_AMOUNT -> ctx.triggerValue;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                case NodePorts.ATTACKER_ENTITY -> ctx.triggerExtraEntity;
                default -> null;
            });

        NodeHelper.setup("on_entity_spawn", "node.mgmc.on_entity_spawn.name")
            .category("node_category.mgmc.events.entity")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_entity_spawn.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/entity/on_entity_spawn")
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.ENTITY_SPAWN, (e, b) -> {
                if (e.getEntity() != null) {
                    b.triggerUuid(e.getEntity().getUUID().toString())
                     .triggerName(e.getEntity().getName().getString())
                     .triggerEntity(e.getEntity())
                     .triggerX(e.getEntity().getX()).triggerY(e.getEntity().getY()).triggerZ(e.getEntity().getZ());
                }
            }, e -> e.getEntity() != null ? net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getEntity().getType()).toString() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        // --- 蓝图事件 ---
        NodeHelper.setup("on_blueprint_called", "node.mgmc.on_blueprint_called.name")
            .category("node_category.mgmc.events.blueprint")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_blueprint_called.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/blueprint/on_blueprint_called")
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .output(NodePorts.LIST, "node.mgmc.port.args_list", NodeDefinition.PortType.LIST, NodeThemes.COLOR_PORT_LIST)
            .register(new NodeHelper.NodeHandlerAdapter() {
                @Override
                public void execute(JsonObject node, NodeContext ctx) {
                    NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
                }

                @Override
                public Object getValue(JsonObject node, String portId, NodeContext ctx) {
                    if (NodePorts.LIST.equals(portId)) {
                        List<Object> list = new ArrayList<>();
                        if (ctx.args != null) {
                            for (String arg : ctx.args) {
                                list.add(arg);
                            }
                        }
                        return list;
                    }
                return null;
            }
        });

        // --- 服务器 / 世界生命周期 ---
        NodeHelper.setup("on_server_start", "node.mgmc.on_server_start.name")
            .category("node_category.mgmc.events.world")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_server_start.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/world/on_server_start")
            .execOut()
            .registerEvent(MGMCEventType.SERVER_START, (e, b) -> {}, e -> BlueprintRouter.GLOBAL_ID,
            (node, portId, ctx) -> null);

        NodeHelper.setup("on_server_stop", "node.mgmc.on_server_stop.name")
            .category("node_category.mgmc.events.world")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_server_stop.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/world/on_server_stop")
            .execOut()
            .registerEvent(MGMCEventType.SERVER_STOP, (e, b) -> {}, e -> BlueprintRouter.GLOBAL_ID,
            (node, portId, ctx) -> null);

        NodeHelper.setup("on_level_load", "node.mgmc.on_level_load.name")
            .category("node_category.mgmc.events.world")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_level_load.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/world/on_level_load")
            .execOut()
            .output(NodePorts.DIMENSION_ID, "node.mgmc.port.dimension_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .registerEvent(MGMCEventType.LEVEL_LOAD, (e, b) -> {
                if (e.getDimensionId() != null) b.triggerDimensionId(e.getDimensionId());
            }, e -> BlueprintRouter.GLOBAL_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.DIMENSION_ID -> ctx.triggerDimensionId;
                default -> null;
            });

        NodeHelper.setup("on_explosion", "node.mgmc.on_explosion.name")
            .category("node_category.mgmc.events.world")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_explosion.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/world/on_explosion")
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .registerEvent(MGMCEventType.EXPLOSION, (e, b) -> {
                if (e.getExplosionPos() != null) b.triggerExplosionPos(e.getExplosionPos());
            }, e -> BlueprintRouter.GLOBAL_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerExplosionPos;
                default -> null;
            });

        // --- 玩家补充 ---
        NodeHelper.setup("on_player_craft", "node.mgmc.on_player_craft.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_player_craft.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_craft")
            .execOut()
            .output(NodePorts.ITEM_ID, "node.mgmc.port.item_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.COUNT, "node.mgmc.port.count", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.COMPONENTS, "node.mgmc.port.components", NodeDefinition.PortType.COMPONENTS, 0xFFE0A000)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.PLAYER_CRAFT, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getItemId() != null) b.triggerItemId(e.getItemId());
                if (e.getItem() != null) b.triggerItem(e.getItem());
            }, e -> e.getItemId() != null ? e.getItemId() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.ITEM_ID -> ctx.triggerItemId;
                case NodePorts.COUNT -> ctx.triggerItem != null ? (double) ctx.triggerItem.getCount() : 0.0;
                case NodePorts.COMPONENTS -> ctx.triggerItem != null ? ItemComponentsCodec.serialize(ctx.triggerItem, ctx.level.registryAccess()) : "{}";
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_smelt", "node.mgmc.on_player_smelt.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_player_smelt.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_smelt")
            .execOut()
            .output(NodePorts.ITEM_ID, "node.mgmc.port.item_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.COUNT, "node.mgmc.port.count", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.COMPONENTS, "node.mgmc.port.components", NodeDefinition.PortType.COMPONENTS, 0xFFE0A000)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.PLAYER_SMELT, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getItemId() != null) b.triggerItemId(e.getItemId());
                if (e.getItem() != null) b.triggerItem(e.getItem());
            }, e -> e.getItemId() != null ? e.getItemId() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.ITEM_ID -> ctx.triggerItemId;
                case NodePorts.COUNT -> ctx.triggerItem != null ? (double) ctx.triggerItem.getCount() : 0.0;
                case NodePorts.COMPONENTS -> ctx.triggerItem != null ? ItemComponentsCodec.serialize(ctx.triggerItem, ctx.level.registryAccess()) : "{}";
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_drop_item", "node.mgmc.on_player_drop_item.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_player_drop_item.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_drop_item")
            .execOut()
            .output(NodePorts.ITEM_ID, "node.mgmc.port.item_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.COUNT, "node.mgmc.port.count", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.COMPONENTS, "node.mgmc.port.components", NodeDefinition.PortType.COMPONENTS, 0xFFE0A000)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.PLAYER_DROP, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getItemId() != null) b.triggerItemId(e.getItemId());
                if (e.getItem() != null) b.triggerItem(e.getItem());
            }, e -> e.getItemId() != null ? e.getItemId() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.ITEM_ID -> ctx.triggerItemId;
                case NodePorts.COUNT -> ctx.triggerItem != null ? (double) ctx.triggerItem.getCount() : 0.0;
                case NodePorts.COMPONENTS -> ctx.triggerItem != null ? ItemComponentsCodec.serialize(ctx.triggerItem, ctx.level.registryAccess()) : "{}";
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_change_dimension", "node.mgmc.on_player_change_dimension.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_player_change_dimension.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_change_dimension")
            .execOut()
            .output(NodePorts.FROM_DIMENSION, "node.mgmc.port.from_dimension", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.TO_DIMENSION, "node.mgmc.port.to_dimension", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.PLAYER_CHANGE_DIMENSION, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getDimensionFrom() != null) b.triggerDimensionFrom(e.getDimensionFrom());
                if (e.getDimensionTo() != null) b.triggerDimensionTo(e.getDimensionTo());
            }, e -> BlueprintRouter.PLAYERS_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.FROM_DIMENSION -> ctx.triggerDimensionFrom;
                case NodePorts.TO_DIMENSION -> ctx.triggerDimensionTo;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_advancement", "node.mgmc.on_player_advancement.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_player_advancement.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_advancement")
            .execOut()
            .output(NodePorts.ADVANCEMENT_ID, "node.mgmc.port.advancement_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.PLAYER_ADVANCEMENT, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getAdvancementId() != null) b.triggerAdvancementId(e.getAdvancementId());
            }, e -> BlueprintRouter.PLAYERS_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.ADVANCEMENT_ID -> ctx.triggerAdvancementId;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_open_menu", "node.mgmc.on_player_open_menu.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_player_open_menu.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_open_menu")
            .execOut()
            .output(NodePorts.MENU_TYPE, "node.mgmc.port.menu_type", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.PLAYER_OPEN_MENU, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getMenuType() != null) b.triggerMenuType(e.getMenuType());
            }, e -> BlueprintRouter.PLAYERS_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.MENU_TYPE -> ctx.triggerMenuType;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_close_menu", "node.mgmc.on_player_close_menu.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_player_close_menu.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_close_menu")
            .execOut()
            .output(NodePorts.MENU_TYPE, "node.mgmc.port.menu_type", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.PLAYER_CLOSE_MENU, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getMenuType() != null) b.triggerMenuType(e.getMenuType());
            }, e -> BlueprintRouter.PLAYERS_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.MENU_TYPE -> ctx.triggerMenuType;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_chat", "node.mgmc.on_player_chat.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_player_chat.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_chat")
            .execOut()
            .output(NodePorts.MESSAGE, "node.mgmc.port.message", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.PLAYER_CHAT, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getMessage() != null) b.triggerMessage(e.getMessage());
            }, e -> e.getPlayer() != null ? BlueprintRouter.PLAYERS_ID : BlueprintRouter.GLOBAL_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.MESSAGE -> ctx.triggerMessage;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_command_perform", "node.mgmc.on_command_perform.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_command_perform.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_command_perform")
            .execOut()
            .output(NodePorts.COMMAND, "node.mgmc.port.command", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.COMMAND_PERFORM, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getCommand() != null) b.triggerCommand(e.getCommand());
            }, e -> BlueprintRouter.GLOBAL_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.COMMAND -> ctx.triggerCommand;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        // --- 实体补充 ---
        NodeHelper.setup("on_entity_tame", "node.mgmc.on_entity_tame.name")
            .category("node_category.mgmc.events.entity")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_entity_tame.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/entity/on_entity_tame")
            .execOut()
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.TAMER, "node.mgmc.port.tamer", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.ENTITY_TAME, (e, b) -> {
                if (e.getEntity() != null) {
                    b.triggerUuid(e.getEntity().getUUID().toString())
                     .triggerName(e.getEntity().getName().getString())
                     .triggerEntity(e.getEntity());
                }
                if (e.getTargetEntity() != null) b.triggerExtraEntity(e.getTargetEntity());
            }, e -> e.getEntity() != null ? net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getEntity().getType()).toString() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.ENTITY -> ctx.triggerEntity;
                case NodePorts.TAMER -> ctx.triggerExtraEntity;
                default -> null;
            });

        NodeHelper.setup("on_interact_entity", "node.mgmc.on_interact_entity.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .description("node.mgmc.on_interact_entity.desc")
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_interact_entity")
            .execOut()
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.TARGET_ENTITY, "node.mgmc.port.target_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.INTERACT_ENTITY, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getTargetEntity() != null) b.triggerExtraEntity(e.getTargetEntity());
            }, e -> BlueprintRouter.PLAYERS_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.ENTITY -> ctx.triggerEntity;
                case NodePorts.TARGET_ENTITY -> ctx.triggerExtraEntity;
                default -> null;
            });
    }
}
