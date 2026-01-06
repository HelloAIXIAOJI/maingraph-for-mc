package ltd.opens.mg.mc.core.blueprint.engine.handlers.collection;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import java.util.List;

public class ListContainsHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("result")) {
            List<Object> list = TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, "list", ctx));
            Object item = NodeLogicRegistry.evaluateInput(node, "item", ctx);
            
            if (list.contains(item)) return true;
            
            // 兜底逻辑：如果对象引用不同，尝试数值比较或字符串比较
            if (item instanceof Number num) {
                double dv = num.doubleValue();
                for (Object o : list) {
                    if (o instanceof Number on && on.doubleValue() == dv) return true;
                }
            }

            String itemStr = TypeConverter.toString(item);
            for (Object o : list) {
                if (TypeConverter.toString(o).equals(itemStr)) return true;
            }
            return false;
        }
        return false;
    }
}





