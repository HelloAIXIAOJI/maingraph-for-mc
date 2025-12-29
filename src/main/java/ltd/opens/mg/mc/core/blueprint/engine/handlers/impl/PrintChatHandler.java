package ltd.opens.mg.mc.core.blueprint.engine.handlers.impl;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class PrintChatHandler implements NodeHandler {
    @Override
    public void execute(JsonObject node, NodeContext ctx) {
        String message = NodeLogicRegistry.evaluateInput(node, "message", ctx);
        Minecraft.getInstance().gui.getChat().addMessage(Component.literal(message));
        NodeLogicRegistry.triggerExec(node, "exec", ctx);
    }
}
