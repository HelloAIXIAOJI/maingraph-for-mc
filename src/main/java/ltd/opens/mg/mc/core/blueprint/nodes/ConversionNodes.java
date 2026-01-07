package ltd.opens.mg.mc.core.blueprint.nodes;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import java.util.Arrays;

/**
 * 类型转换相关节点
 */
public class ConversionNodes {
    private static final int COLOR_CONVERSION = 0xFF888888;

    public static void register() {
        // 获取所有 PortType 名称作为转换选项，排除 EXEC 和 ANY
        String[] typeOptions = Arrays.stream(NodeDefinition.PortType.values())
            .filter(t -> t != NodeDefinition.PortType.EXEC && t != NodeDefinition.PortType.ANY)
            .map(Enum::name)
            .toArray(String[]::new);

        // 强制转换节点 (Cast)
        NodeHelper.setup("cast", "node.mgmc.cast.name")
            .category("node_category.mgmc.logic.math")
            .color(COLOR_CONVERSION)
            .input("input", "node.mgmc.cast.port.input", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .input("to_type", "node.mgmc.cast.port.to_type", NodeDefinition.PortType.STRING, 0xFFFFFFFF, true, "STRING", typeOptions)
            .output("output", "node.mgmc.cast.port.output", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .registerValue((node, portId, ctx) -> {
                Object input = NodeLogicRegistry.evaluateInput(node, "input", ctx);
                String targetType = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "to_type", ctx));
                return TypeConverter.cast(input, targetType);
            });
    }
}
