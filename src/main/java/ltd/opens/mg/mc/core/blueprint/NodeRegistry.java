package ltd.opens.mg.mc.core.blueprint;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NodeRegistry {
    private static final Map<String, NodeDefinition> REGISTRY = new HashMap<>();

    public static void register(NodeDefinition definition) {
        REGISTRY.put(definition.id(), definition);
    }

    public static NodeDefinition get(String id) {
        return REGISTRY.get(id);
    }

    public static Collection<NodeDefinition> getAll() {
        return REGISTRY.values();
    }

    static {
        // Colors from web editor
        int colorExec = 0xFFFFFFFF;
        int colorString = 0xFFDA00DA;
        int colorFloat = 0xFF36CF36;
        int colorBoolean = 0xFF920101;
        int colorObject = 0xFF00AAFF;
        int colorList = 0xFFFFCC00; // Yellow-ish for List
        int colorUUID = 0xFF55FF55; // Light Green for UUID
        int colorEnum = 0xFFFFAA00; // Orange for Enum

        // Register default nodes matching web editor
        // Events
        register(new NodeDefinition.Builder("on_mgrun", "node.mgmc.on_mgrun.name")
            .category("node_category.mgmc.events")
            .color(0xFF880000)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("name", "node.mgmc.port.name", NodeDefinition.PortType.STRING, colorString)
            .addOutput("parameters", "node.mgmc.on_mgrun.port.parameters", NodeDefinition.PortType.LIST, colorList)
            .addOutput("trigger_uuid", "node.mgmc.on_mgrun.port.trigger_uuid", NodeDefinition.PortType.UUID, colorUUID)
            .addOutput("trigger_name", "node.mgmc.on_mgrun.port.trigger_name", NodeDefinition.PortType.STRING, colorString)
            .addOutput("trigger_x", "node.mgmc.on_mgrun.port.trigger_x", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("trigger_y", "node.mgmc.on_mgrun.port.trigger_y", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("trigger_z", "node.mgmc.on_mgrun.port.trigger_z", NodeDefinition.PortType.FLOAT, colorFloat)
            .build());

        register(new NodeDefinition.Builder("on_player_move", "node.mgmc.on_player_move.name")
            .category("node_category.mgmc.events")
            .color(0xFF0088FF)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("speed", "node.mgmc.on_player_move.port.speed", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, colorUUID)
            .build());

        // Function
        register(new NodeDefinition.Builder("print_chat", "node.mgmc.print_chat.name")
            .category("node_category.mgmc.function")
            .color(0xFF4488FF)
            .addInput("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("message", "node.mgmc.port.message", NodeDefinition.PortType.STRING, colorString, true, "Hello Chat")
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .build());

        register(new NodeDefinition.Builder("play_effect", "node.mgmc.play_effect.name")
            .category("node_category.mgmc.function")
            .color(0xFF4488FF)
            .addInput("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("effect", "node.mgmc.play_effect.port.effect", NodeDefinition.PortType.STRING, colorString, true, "minecraft:happy_villager")
            .addInput("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addInput("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addInput("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .build());

        register(new NodeDefinition.Builder("explosion", "node.mgmc.explosion.name")
            .category("node_category.mgmc.function")
            .color(0xFFFF4444)
            .addInput("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addInput("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addInput("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addInput("radius", "node.mgmc.explosion.port.radius", NodeDefinition.PortType.FLOAT, colorFloat, true, 3.0)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .build());

        register(new NodeDefinition.Builder("get_list_item", "node.mgmc.get_list_item.name")
            .category("node_category.mgmc.function")
            .color(0xFF44AA44)
            .addInput("list", "node.mgmc.get_list_item.port.list", NodeDefinition.PortType.LIST, colorList)
            .addInput("index", "node.mgmc.get_list_item.port.index", NodeDefinition.PortType.FLOAT, colorFloat, true, 0)
            .addOutput("value", "node.mgmc.get_list_item.port.value", NodeDefinition.PortType.STRING, colorString)
            .build());

        register(new NodeDefinition.Builder("get_entity_info", "node.mgmc.get_entity_info.name")
            .category("node_category.mgmc.function")
            .color(0xFF44AA44)
            .addInput("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, colorUUID)
            .addOutput("name", "node.mgmc.port.name", NodeDefinition.PortType.STRING, colorString)
            .addOutput("type", "node.mgmc.get_entity_info.port.type", NodeDefinition.PortType.ENUM, colorEnum)
            .addOutput("registry_name", "node.mgmc.get_entity_info.port.registry_name", NodeDefinition.PortType.STRING, colorString)
            .addOutput("pos_x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("pos_y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("pos_z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("health", "node.mgmc.get_entity_info.port.health", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("max_health", "node.mgmc.get_entity_info.port.max_health", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("is_living", "node.mgmc.get_entity_info.port.is_living", NodeDefinition.PortType.BOOLEAN, colorBoolean)
            .addOutput("is_player", "node.mgmc.get_entity_info.port.is_player", NodeDefinition.PortType.BOOLEAN, colorBoolean)
            .build());

        // Logic
        register(new NodeDefinition.Builder("branch", "node.mgmc.branch.name")
            .category("node_category.mgmc.logic")
            .color(0xFF888888)
            .addInput("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("condition", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, colorBoolean, true, true)
            .addOutput("true", "node.mgmc.port.true", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("false", "node.mgmc.port.false", NodeDefinition.PortType.EXEC, colorExec)
            .build());

        register(new NodeDefinition.Builder("cast", "node.mgmc.cast.name")
            .category("node_category.mgmc.logic")
            .color(0xFF888888)
            .addInput("input", "node.mgmc.cast.port.input", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .addInput("to_type", "node.mgmc.cast.port.to_type", NodeDefinition.PortType.STRING, colorString, true, "STRING", 
                new String[]{"STRING", "FLOAT", "BOOLEAN", "UUID", "INT", "LIST"})
            .addOutput("output", "node.mgmc.cast.port.output", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .build());

        register(new NodeDefinition.Builder("switch", "node.mgmc.switch.name")
            .category("node_category.mgmc.logic")
            .color(0xFF888888)
            .addInput("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("control", "node.mgmc.switch.port.control", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .addOutput("default", "node.mgmc.switch.port.default", NodeDefinition.PortType.EXEC, colorExec)
            .build());

        register(new NodeDefinition.Builder("string", "node.mgmc.string.name")
            .category("node_category.mgmc.logic")
            .color(colorString)
            .addInput("value", "node.mgmc.port.value", NodeDefinition.PortType.STRING, colorString, true, "")
            .addOutput("output", "node.mgmc.port.output", NodeDefinition.PortType.STRING, colorString)
            .build());
    }
}
