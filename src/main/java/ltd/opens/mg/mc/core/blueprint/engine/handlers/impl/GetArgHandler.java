package ltd.opens.mg.mc.core.blueprint.engine.handlers.impl;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class GetArgHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("value")) {
            try {
                String indexStr = NodeLogicRegistry.evaluateInput(node, "index", ctx);
                int index = (int) Double.parseDouble(indexStr);
                if (index >= 0 && index < ctx.args.length) {
                    return ctx.args[index];
                }
            } catch (Exception e) {}
        }
        return "";
    }
}
