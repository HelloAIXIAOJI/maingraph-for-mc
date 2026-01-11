package ltd.opens.mg.mc.core.blueprint;

import ltd.opens.mg.mc.core.blueprint.events.RegisterMGMCNodesEvent;
import ltd.opens.mg.mc.core.blueprint.nodes.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

/**
 * 蓝图节点统一初始化入口
 */
public class NodeInitializer {
    /**
     * 初始化所有节点（通过发布事件实现解耦）
     */
    public static void init(IEventBus modEventBus) {
        // 注册所有内置节点类到 NeoForge 事件总线 (由于事件现在发布在 NeoForge.EVENT_BUS)
        NeoForge.EVENT_BUS.register(MathNodes.class);
        NeoForge.EVENT_BUS.register(LogicNodes.class);
        NeoForge.EVENT_BUS.register(VariableNodes.class);
        NeoForge.EVENT_BUS.register(ConversionNodes.class);
        NeoForge.EVENT_BUS.register(ControlFlowNodes.class);
        NeoForge.EVENT_BUS.register(StringNodes.class);
        NeoForge.EVENT_BUS.register(ListNodes.class);
        NeoForge.EVENT_BUS.register(ActionNodes.class);
        NeoForge.EVENT_BUS.register(EventNodes.class);
        NeoForge.EVENT_BUS.register(GetEntityInfoNode.class);
        NeoForge.EVENT_BUS.register(SpecialNodes.class);

        // 发布注册事件，通知外部模块
        ltd.opens.mg.mc.MaingraphforMC.LOGGER.info("Posting RegisterMGMCNodesEvent to NeoForge.EVENT_BUS...");
        NeoForge.EVENT_BUS.post(new RegisterMGMCNodesEvent());
        ltd.opens.mg.mc.MaingraphforMC.LOGGER.info("RegisterMGMCNodesEvent posted.");
        
        // 冻结注册表，防止运行时动态修改
        NodeRegistry.freeze();
    }
}
