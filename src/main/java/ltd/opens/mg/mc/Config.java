package ltd.opens.mg.mc;

import net.neoforged.neoforge.common.ModConfigSpec;

// Config class for the mod.
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<Integer> MAX_RECURSION_DEPTH_VAL = BUILDER
            .comment("最大递归执行深度，防止蓝图死循环导致崩溃")
            .define("max_recursion_depth", 10);

    private static final ModConfigSpec.ConfigValue<Integer> MAX_NODE_EXECUTIONS_VAL = BUILDER
            .comment("单次蓝图运行最大执行节点数，防止超大规模循环导致卡顿")
            .define("max_node_executions", 5000);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int getMaxRecursionDepth() {
        return MAX_RECURSION_DEPTH_VAL.get();
    }

    public static int getMaxNodeExecutions() {
        return MAX_NODE_EXECUTIONS_VAL.get();
    }
}
