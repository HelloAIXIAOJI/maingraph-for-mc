package ltd.opens.mg.mc.core.blueprint.engine.handlers.event;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import java.util.Arrays;

public class OnMgRunHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("name")) return ctx.eventName;
        if (pinId.equals("parameters")) {
            return Arrays.asList(ctx.args);
        }
        if (pinId.equals("trigger_uuid")) {
            return ctx.triggerUuid != null ? ctx.triggerUuid : "";
        }
        if (pinId.equals("trigger_name")) {
            return ctx.triggerName != null ? ctx.triggerName : "";
        }
        if (pinId.equals("trigger_x")) return ctx.triggerX;
        if (pinId.equals("trigger_y")) return ctx.triggerY;
        if (pinId.equals("trigger_z")) return ctx.triggerZ;
        return null;
    }
}




