package ltd.opens.mg.mc.core.blueprint;

import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import net.neoforged.bus.api.Event;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 存储事件节点的监听元数据
 */
public record EventMetadata(
    Class<? extends Event> eventClass,
    BiConsumer<Event, NodeContext.Builder> contextPopulator,
    Function<Event, String> routingIdExtractor
) {}
