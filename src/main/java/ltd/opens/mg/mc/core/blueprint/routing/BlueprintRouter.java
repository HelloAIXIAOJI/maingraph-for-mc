package ltd.opens.mg.mc.core.blueprint.routing;

import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 中央调度路由中心
 * 负责维护 Minecraft ID (ResourceLocation) 与蓝图文件路径之间的映射关系
 */
public class BlueprintRouter {
    private final Logger LOGGER = LogManager.getLogger();
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    // 虚拟 ID 定义
    public static final String GLOBAL_ID = "mgmc:global";
    public static final String PLAYERS_ID = "mgmc:players";

    // 内存中的路由表: ID -> 蓝图路径列表
    private final Map<String, Set<String>> routingTable = new ConcurrentHashMap<>();

    public BlueprintRouter() {
    }

    /**
     * 从指定世界的路由表文件加载
     */
    public void load(ServerLevel level) {
        Path filePath = getMappingsPath(level);
        if (!Files.exists(filePath)) {
            routingTable.clear();
            save(level); // 创建初始文件
            return;
        }

        try (FileReader reader = new FileReader(filePath.toFile())) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            routingTable.clear();
            if (json != null) {
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    JsonArray array = entry.getValue().getAsJsonArray();
                    Set<String> blueprints = Collections.newSetFromMap(new ConcurrentHashMap<>());
                    for (JsonElement e : array) {
                        blueprints.add(e.getAsString());
                    }
                    routingTable.put(entry.getKey(), blueprints);
                }
            }
            LOGGER.info("MGMC: Loaded {} ID mappings from {}", routingTable.size(), filePath);
        } catch (IOException e) {
            LOGGER.error("MGMC: Failed to load mappings from " + filePath, e);
        }
    }

    /**
     * 保存路由表到指定世界的路由表文件
     */
    public synchronized void save(ServerLevel level) {
        Path filePath = getMappingsPath(level);
        try {
            Files.createDirectories(filePath.getParent());
            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                JsonObject json = new JsonObject();
                for (Map.Entry<String, Set<String>> entry : routingTable.entrySet()) {
                    JsonArray array = new JsonArray();
                    entry.getValue().forEach(array::add);
                    json.add(entry.getKey(), array);
                }
                GSON.toJson(json, writer);
            }
        } catch (IOException e) {
            LOGGER.error("MGMC: Failed to save mappings to " + filePath, e);
        }
    }

    private Path getMappingsPath(ServerLevel level) {
        return level.getServer().getWorldPath(LevelResource.ROOT).resolve("mgmc_blueprints/.routing/mappings.json");
    }

    /**
     * 获取指定 ID 绑定的所有蓝图路径
     */
    public Set<String> getMappedBlueprints(String id) {
        return routingTable.getOrDefault(id, Collections.emptySet());
    }

    /**
     * 添加映射（注意：此方法目前仅由客户端通过网络请求触发，由 handleSaveMappings 统一处理保存）
     */
    public void addMapping(String id, String blueprintPath) {
        routingTable.computeIfAbsent(id, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                    .add(blueprintPath);
    }

    /**
     * 移除映射
     */
    public void removeMapping(String id, String blueprintPath) {
        Set<String> blueprints = routingTable.get(id);
        if (blueprints != null) {
            blueprints.remove(blueprintPath);
            if (blueprints.isEmpty()) {
                routingTable.remove(id);
            }
        }
    }

    /**
     * 获取所有已订阅的 ID
     */
    public Set<String> getAllSubscribedIds() {
        return routingTable.keySet();
    }

    /**
     * 获取完整的路由表快照
     */
    public Map<String, Set<String>> getFullRoutingTable() {
        Map<String, Set<String>> copy = new HashMap<>();
        routingTable.forEach((k, v) -> copy.put(k, new HashSet<>(v)));
        return copy;
    }

    /**
     * 批量更新路由表并保存
     */
    public void updateAllMappings(ServerLevel level, Map<String, Set<String>> newMappings) {
        routingTable.clear();
        newMappings.forEach((k, v) -> {
            Set<String> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
            set.addAll(v);
            routingTable.put(k, set);
        });
        save(level);
    }

    /**
     * 客户端专用的内存更新（不保存文件）
     */
    public void clientUpdateMappings(Map<String, Set<String>> newMappings) {
        routingTable.clear();
        newMappings.forEach((k, v) -> {
            Set<String> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
            set.addAll(v);
            routingTable.put(k, set);
        });
    }
}
