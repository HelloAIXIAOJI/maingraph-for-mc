package ltd.opens.mg.mc.core.blueprint.engine.handlers.impl;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class SwitchHandler implements NodeHandler {
    @Override
    public void execute(JsonObject node, NodeContext ctx) {
        String controlValue = NodeLogicRegistry.evaluateInput(node, "control", ctx);
        
        // 直接检查输出引脚的 ID 是否匹配控制变量的值
        if (node.has("outputs")) {
            JsonObject outputs = node.getAsJsonObject("outputs");
            for (String key : outputs.keySet()) {
                // 跳过默认引脚和执行输入引脚（如果有的话）
                if (key.equals("default") || key.equals("exec")) continue;
                
                if (controlValue.equals(key)) {
                    NodeLogicRegistry.triggerExec(node, key, ctx);
                    return;
                }
            }
        }
        
        // 如果没有分支匹配，触发默认
        NodeLogicRegistry.triggerExec(node, "default", ctx);
    }
}
