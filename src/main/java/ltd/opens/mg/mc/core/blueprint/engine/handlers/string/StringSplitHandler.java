package ltd.opens.mg.mc.core.blueprint.engine.handlers.string;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class StringSplitHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("list")) {
            String str = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "string", ctx));
            String delim = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "delimiter", ctx));
            
            if (str == null || str.isEmpty()) return new ArrayList<>();
            if (delim == null || delim.isEmpty()) {
                List<String> list = new ArrayList<>();
                list.add(str);
                return list;
            }
            
            String[] parts = str.split(Pattern.quote(delim));
            return new ArrayList<>(Arrays.asList(parts));
        }
        return null;
    }
}



