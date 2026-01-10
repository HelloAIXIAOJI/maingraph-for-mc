package ltd.opens.mg.mc.core.blueprint;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.MaingraphforMC;
import ltd.opens.mg.mc.core.blueprint.engine.BlueprintEngine;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.routing.BlueprintRouter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 通用事件分发器
 * 负责监听 Minecraft 事件并根据 NodeDefinition 中的元数据分发到蓝图引擎。
 */
public class EventDispatcher {

    private static final Map<Class<? extends Event>, List<NodeDefinition>> EVENT_NODES = new ConcurrentHashMap<>();
    private static final Set<Class<? extends Event>> REGISTERED_CLASSES = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * 初始化分发器，从注册表中提取所有事件节点并注册监听。
     */
    public static void init() {
        for (NodeDefinition def : NodeRegistry.getAllDefinitions()) {
            EventMetadata metadata = (EventMetadata) def.properties().get("event_metadata");
            if (metadata != null) {
                EVENT_NODES.computeIfAbsent(metadata.eventClass(), k -> new ArrayList<>()).add(def);
                registerListener(metadata.eventClass());
            }
        }
        MaingraphforMC.LOGGER.info("EventDispatcher initialized with {} event types", EVENT_NODES.size());
    }

    private static <T extends Event> void registerListener(Class<T> eventClass) {
        if (REGISTERED_CLASSES.contains(eventClass)) return;
        
        NeoForge.EVENT_BUS.addListener(eventClass, (Consumer<T>) EventDispatcher::handleEvent);
        REGISTERED_CLASSES.add(eventClass);
    }

    private static <T extends Event> void handleEvent(T event) {
        List<NodeDefinition> defs = EVENT_NODES.get(event.getClass());
        if (defs == null) return;

        // 获取 Level (通常事件都能拿到 Level)
        Level level = getLevelFromEvent(event);
        if (!(level instanceof ServerLevel serverLevel)) return;

        for (NodeDefinition def : defs) {
            EventMetadata metadata = (EventMetadata) def.properties().get("event_metadata");
            if (metadata == null) continue;

            String routingId = metadata.routingIdExtractor().apply(event);
            if (routingId == null) continue;

            // 获取绑定的蓝图
            var manager = MaingraphforMC.getServerManager();
            if (manager == null) continue;
            List<JsonObject> blueprints = manager.getBlueprintsForId(serverLevel, BlueprintRouter.GLOBAL_ID, routingId);
            if (blueprints.isEmpty()) continue;

            // 构造 Context
            NodeContext.Builder contextBuilder = new NodeContext.Builder(serverLevel);
            metadata.contextPopulator().accept(event, contextBuilder);
            
            // 执行蓝图
            for (JsonObject blueprint : blueprints) {
                BlueprintEngine.execute(serverLevel, blueprint, def.id(), contextBuilder);
            }
        }
    }

    private static Level getLevelFromEvent(Event event) {
        if (event instanceof net.neoforged.neoforge.event.level.LevelEvent le) {
            if (le.getLevel() instanceof Level l) return l;
        }
        if (event instanceof net.neoforged.neoforge.event.entity.EntityEvent ee) return ee.getEntity().level();
        if (event instanceof net.neoforged.neoforge.event.tick.PlayerTickEvent pte) return pte.getEntity().level();
        return null;
    }
}
