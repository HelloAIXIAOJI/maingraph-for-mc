package ltd.opens.mg.mc.core.blueprint.engine.handlers.impl;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;

public class OnMgRunHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("name")) return ctx.eventName;
        if (pinId.equals("parameters")) {
            // Join args with a delimiter or return as a format that our list item node understands
            // Since our engine currently passes around Strings, we'll use a simple comma-separated string 
            // and have the list handler parse it.
            return String.join("|", ctx.args);
        }
        return "";
    }
}
