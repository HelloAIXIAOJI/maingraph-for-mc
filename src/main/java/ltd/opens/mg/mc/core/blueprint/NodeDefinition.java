package ltd.opens.mg.mc.core.blueprint;

import java.util.ArrayList;
import java.util.List;

public record NodeDefinition(
    String id,
    String name,
    String category,
    int color,
    List<PortDefinition> inputs,
    List<PortDefinition> outputs
) {
    public static class Builder {
        private final String id;
        private final String name;
        private String category = "General";
        private int color = 0xFF444444;
        private final List<PortDefinition> inputs = new ArrayList<>();
        private final List<PortDefinition> outputs = new ArrayList<>();

        public Builder(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder addInput(String name, PortType type, int color) {
            inputs.add(new PortDefinition(name, type, color, false, null));
            return this;
        }

        public Builder addInput(String name, PortType type, int color, boolean hasInput, Object defaultValue) {
            inputs.add(new PortDefinition(name, type, color, hasInput, defaultValue));
            return this;
        }

        public Builder addOutput(String name, PortType type, int color) {
            outputs.add(new PortDefinition(name, type, color, false, null));
            return this;
        }

        public NodeDefinition build() {
            return new NodeDefinition(id, name, category, color, List.copyOf(inputs), List.copyOf(outputs));
        }
    }

    public record PortDefinition(String name, PortType type, int color, boolean hasInput, Object defaultValue) {}

    public enum PortType {
        EXEC, STRING, FLOAT, BOOLEAN, OBJECT, LIST, UUID, ENUM
    }
}
