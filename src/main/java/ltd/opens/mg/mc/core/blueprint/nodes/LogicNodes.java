package ltd.opens.mg.mc.core.blueprint.nodes;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

/**
 * 比较与布尔逻辑运算节点注册
 */
public class LogicNodes {
    private static final int COLOR_MATH = 0xFF888888;
    private static final int COLOR_FLOAT = 0xFF00FF00;
    private static final int COLOR_BOOLEAN = 0xFF920101;

    public static void register() {
        // --- 比较运算 ---
        NodeHelper.setup("compare_eq", "node.mgmc.compare_eq.name")
            .category("node_category.mgmc.logic.comparison")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a == b;
            });

        NodeHelper.setup("compare_neq", "node.mgmc.compare_neq.name")
            .category("node_category.mgmc.logic.comparison")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a != b;
            });

        NodeHelper.setup("compare_gt", "node.mgmc.compare_gt.name")
            .category("node_category.mgmc.logic.comparison")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a > b;
            });

        NodeHelper.setup("compare_gte", "node.mgmc.compare_gte.name")
            .category("node_category.mgmc.logic.comparison")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a >= b;
            });

        NodeHelper.setup("compare_lt", "node.mgmc.compare_lt.name")
            .category("node_category.mgmc.logic.comparison")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a < b;
            });

        NodeHelper.setup("compare_lte", "node.mgmc.compare_lte.name")
            .category("node_category.mgmc.logic.comparison")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a <= b;
            });

        // --- 布尔逻辑 ---
        NodeHelper.setup("logic_and", "node.mgmc.logic_and.name")
            .category("node_category.mgmc.logic.boolean")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN, true)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN, true)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                boolean a = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                boolean b = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a && b;
            });

        NodeHelper.setup("logic_or", "node.mgmc.logic_or.name")
            .category("node_category.mgmc.logic.boolean")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN, false)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN, false)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                boolean a = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                boolean b = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a || b;
            });

        NodeHelper.setup("logic_not", "node.mgmc.logic_not.name")
            .category("node_category.mgmc.logic.boolean")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN, false)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .registerValue((node, portId, ctx) -> !TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, "a", ctx)));

        NodeHelper.setup("logic_xor", "node.mgmc.logic_xor.name")
            .category("node_category.mgmc.logic.boolean")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN, false)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN, false)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                boolean a = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                boolean b = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a ^ b;
            });
    }
}
