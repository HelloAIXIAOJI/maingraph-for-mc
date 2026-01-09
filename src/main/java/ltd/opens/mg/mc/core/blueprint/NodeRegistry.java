package ltd.opens.mg.mc.core.blueprint;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NodeRegistry {
    private static final Map<String, NodeDefinition> REGISTRY = new ConcurrentHashMap<>();

    public static void register(NodeDefinition definition) {
        if (REGISTRY.containsKey(definition.id())) {
            NodeDefinition existing = REGISTRY.get(definition.id());
            String errorMsg = String.format(
                "\n\n================================================================\n" +
                "Maingraph For MC has detected critical errors: Node ID Conflict!\n" +
                "The following node ID is already registered:\n" +
                " - \"%s\" (Attempted by mod: %s, already registered by mod: %s)\n" +
                "================================================================\n",
                definition.id(), definition.registeredBy(), existing.registeredBy()
            );
            throw new IllegalStateException(errorMsg);
        }
        REGISTRY.put(definition.id(), definition);
    }

    public static NodeDefinition get(String id) {
        return REGISTRY.get(id);
    }

    public static Collection<NodeDefinition> getAllDefinitions() {
        return REGISTRY.values();
    }

    static {
        NodeInitializer.init();
    }
}
