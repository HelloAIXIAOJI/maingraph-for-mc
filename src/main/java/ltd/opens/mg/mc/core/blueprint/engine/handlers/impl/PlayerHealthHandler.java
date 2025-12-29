package ltd.opens.mg.mc.core.blueprint.engine.handlers.impl;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import net.minecraft.client.Minecraft;

public class PlayerHealthHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("value") && Minecraft.getInstance().player != null) {
            return String.valueOf(Minecraft.getInstance().player.getHealth());
        }
        return "0";
    }
}
