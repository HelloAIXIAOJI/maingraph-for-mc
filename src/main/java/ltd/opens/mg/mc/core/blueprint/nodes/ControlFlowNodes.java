package ltd.opens.mg.mc.core.blueprint.nodes;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

/**
 * 控制流相关节点
 */
public class ControlFlowNodes {
    private static final int COLOR_CONTROL = 0xFF888888;
    private static final int COLOR_EXEC = 0xFFFFFFFF;
    private static final int COLOR_BOOLEAN = 0xFF4444FF;
    private static final int COLOR_FLOAT = 0xFFFFCC00;

    public static void register() {
        // 分支节点 (Branch)
        NodeHelper.setup("branch", "node.mgmc.branch.name")
            .category("node_category.mgmc.logic.control")
            .color(COLOR_CONTROL)
            .input("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, COLOR_EXEC)
            .input("condition", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN, true, true)
            .output("true", "node.mgmc.port.true", NodeDefinition.PortType.EXEC, COLOR_EXEC)
            .output("false", "node.mgmc.port.false", NodeDefinition.PortType.EXEC, COLOR_EXEC)
            .registerExec((node, ctx) -> {
                boolean condition = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, "condition", ctx));
                NodeLogicRegistry.triggerExec(node, condition ? "true" : "false", ctx);
            });

        // 切换器节点 (Switch) - 修复硬编码，标记为动态节点
        NodeHelper.setup("switch", "node.mgmc.switch.name")
            .category("node_category.mgmc.logic.control")
            .color(COLOR_CONTROL)
            .property("dynamic_outputs", true) // 标记为动态输出端口节点
            .input("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, COLOR_EXEC)
            .input("control", "node.mgmc.switch.port.control", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .output("default", "node.mgmc.switch.port.default", NodeDefinition.PortType.EXEC, COLOR_EXEC)
            .registerExec((node, ctx) -> {
                String controlValue = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "control", ctx));
                if (node.has("outputs")) {
                    var outputs = node.getAsJsonObject("outputs");
                    for (String key : outputs.keySet()) {
                        if (key.equals("default") || key.equals("exec")) continue;
                        if (controlValue.equals(key)) {
                            NodeLogicRegistry.triggerExec(node, key, ctx);
                            return;
                        }
                    }
                }
                NodeLogicRegistry.triggerExec(node, "default", ctx);
            });

        // 循环节点 (For Loop)
        NodeHelper.setup("for_loop", "node.mgmc.for_loop.name")
            .category("node_category.mgmc.logic.control")
            .color(COLOR_CONTROL)
            .input("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, COLOR_EXEC)
            .input("start", "node.mgmc.for_loop.port.start", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, true, 0)
            .input("end", "node.mgmc.for_loop.port.end", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, true, 10)
            .input("break", "node.mgmc.for_loop.port.break", NodeDefinition.PortType.EXEC, COLOR_EXEC)
            .output("loop_body", "node.mgmc.for_loop.port.loop_body", NodeDefinition.PortType.EXEC, COLOR_EXEC)
            .output("completed", "node.mgmc.for_loop.port.completed", NodeDefinition.PortType.EXEC, COLOR_EXEC)
            .output("index", "node.mgmc.for_loop.port.index", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .register(new NodeHelper.NodeHandlerAdapter() {
                @Override
                public void execute(JsonObject node, NodeContext ctx) {
                    if ("break".equals(ctx.lastTriggeredPin)) {
                        ctx.breakRequested = true;
                        return;
                    }
                    int start = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, "start", ctx));
                    int end = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, "end", ctx));
                    boolean previousBreakRequested = ctx.breakRequested;
                    ctx.breakRequested = false;
                    for (int i = start; i <= end; i++) {
                        node.addProperty("_index", i);
                        NodeLogicRegistry.triggerExec(node, "loop_body", ctx);
                        if (ctx.breakRequested) {
                            ctx.breakRequested = false;
                            break;
                        }
                    }
                    ctx.breakRequested = previousBreakRequested;
                    NodeLogicRegistry.triggerExec(node, "completed", ctx);
                }

                @Override
                public Object getValue(JsonObject node, String portId, NodeContext ctx) {
                    if ("index".equals(portId)) {
                        return node.has("_index") ? node.get("_index").getAsInt() : 0;
                    }
                    return null;
                }
            });

        // 中断节点 (Break Loop)
        NodeHelper.setup("break_loop", "node.mgmc.break_loop.name")
            .category("node_category.mgmc.logic.control")
            .color(COLOR_CONTROL)
            .input("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, COLOR_EXEC)
            .output("break", "node.mgmc.break_loop.port.break", NodeDefinition.PortType.EXEC, COLOR_EXEC)
            .registerExec((node, ctx) -> {
                ctx.breakRequested = true;
            });
    }
}
