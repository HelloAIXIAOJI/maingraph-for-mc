package ltd.opens.mg.mc;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import ltd.opens.mg.mc.core.blueprint.engine.BlueprintEngine;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.nio.file.Files;
import java.nio.file.Path;

import net.minecraft.commands.CommandSourceStack;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Mod(MaingraphforMC.MODID)
public class MaingraphforMC {
    public static final String MODID = "mgmc";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MaingraphforMC(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new BlueprintServerHandler());

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Maingraph for MC initialized.");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("mgrun")
            .requires(s -> true)
            .then(Commands.argument("blueprint", StringArgumentType.word())
                .then(Commands.argument("event", StringArgumentType.word())
                    .then(Commands.argument("args", StringArgumentType.greedyString())
                        .executes(context -> {
                            String blueprintName = StringArgumentType.getString(context, "blueprint");
                            String eventName = StringArgumentType.getString(context, "event");
                            String argsStr = StringArgumentType.getString(context, "args");
                            String[] args = argsStr.split("\\s+");
                            
                            CommandSourceStack source = context.getSource();
                            ServerLevel level = source.getLevel();
                            String triggerUuid = source.getEntity() != null ? source.getEntity().getUUID().toString() : "";
                            String triggerName = source.getTextName();
                            var pos = source.getPosition();
                            
                            try {
                                JsonObject blueprint = BlueprintServerHandler.getBlueprint(level, blueprintName);
                                if (blueprint != null) {
                                    BlueprintEngine.execute(level, blueprint, "on_mgrun", eventName, args, triggerUuid, triggerName, pos.x, pos.y, pos.z);
                                } else {
                                    context.getSource().sendFailure(Component.literal("Blueprint '" + blueprintName + "' not found."));
                                }
                            } catch (Exception e) {
                                context.getSource().sendFailure(Component.literal("Failed to execute blueprint: " + e.getMessage()));
                            }
                            return 1;
                        })
                    )
                    .executes(context -> {
                        String blueprintName = StringArgumentType.getString(context, "blueprint");
                        String eventName = StringArgumentType.getString(context, "event");
                        CommandSourceStack source = context.getSource();
                        ServerLevel level = source.getLevel();
                        String triggerUuid = source.getEntity() != null ? source.getEntity().getUUID().toString() : "";
                        String triggerName = source.getTextName();
                        var pos = source.getPosition();
                        try {
                            JsonObject blueprint = BlueprintServerHandler.getBlueprint(level, blueprintName);
                            if (blueprint != null) {
                                BlueprintEngine.execute(level, blueprint, "on_mgrun", eventName, new String[0], triggerUuid, triggerName, pos.x, pos.y, pos.z);
                            } else {
                                context.getSource().sendFailure(Component.literal("Blueprint '" + blueprintName + "' not found."));
                            }
                        } catch (Exception e) {
                            context.getSource().sendFailure(Component.literal("Failed to execute blueprint: " + e.getMessage()));
                        }
                        return 1;
                    })
                )
            )
        );
    }

    public static class BlueprintServerHandler {
        private final java.util.Map<java.util.UUID, Double[]> lastPositions = new java.util.HashMap<>();
        
        private static class CachedBlueprint {
            JsonObject json;
            long lastModified;
            CachedBlueprint(JsonObject json, long lastModified) {
                this.json = json;
                this.lastModified = lastModified;
            }
        }
        
        private static final java.util.Map<String, CachedBlueprint> blueprintCache = new java.util.HashMap<>();

        public static Path getBlueprintsDir(ServerLevel level) {
            Path dir = level.getServer().getWorldPath(LevelResource.ROOT).resolve("mgmc_blueprints");
            if (!Files.exists(dir)) {
                try {
                    Files.createDirectories(dir);
                } catch (Exception e) {}
            }
            return dir;
        }

        public static JsonObject getBlueprint(ServerLevel level, String name) {
            try {
                if (!name.endsWith(".json")) name += ".json";
                Path dataFile = getBlueprintsDir(level).resolve(name);
                if (Files.exists(dataFile)) {
                    long lastModified = Files.getLastModifiedTime(dataFile).toMillis();
                    CachedBlueprint cached = blueprintCache.get(name);
                    if (cached == null || lastModified > cached.lastModified) {
                        String json = Files.readString(dataFile);
                        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                        cached = new CachedBlueprint(obj, lastModified);
                        blueprintCache.put(name, cached);
                    }
                    return cached.json;
                }
            } catch (Exception e) {}
            return null;
        }

        public static java.util.Collection<JsonObject> getAllBlueprints(ServerLevel level) {
            java.util.List<JsonObject> all = new java.util.ArrayList<>();
            try {
                Path dir = getBlueprintsDir(level);
                try (var stream = Files.list(dir)) {
                    stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                        String name = p.getFileName().toString();
                        JsonObject bp = getBlueprint(level, name);
                        if (bp != null) all.add(bp);
                    });
                }
            } catch (Exception e) {}
            return all;
        }

        @SubscribeEvent
        public void onServerTick(ServerTickEvent.Post event) {
            var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;
            if (server.getTickCount() % 5 != 0) return;

            for (var player : server.getPlayerList().getPlayers()) {
                double x = player.getX();
                double y = player.getY();
                double z = player.getZ();
                java.util.UUID uuid = player.getUUID();

                Double[] lastPos = lastPositions.get(uuid);
                if (lastPos != null) {
                    double dx = x - lastPos[0];
                    double dy = y - lastPos[1];
                    double dz = z - lastPos[2];

                    if (dx * dx + dy * dy + dz * dz > 0.25) {
                        try {
                            ServerLevel level = (ServerLevel) player.level();
                            for (JsonObject blueprint : getAllBlueprints(level)) {
                                BlueprintEngine.execute(level, blueprint, "on_player_move", "", new String[0], 
                                    uuid.toString(), player.getName().getString(), x, y, z);
                            }
                            lastPositions.put(uuid, new Double[]{x, y, z});
                        } catch (Exception e) {}
                    }
                } else {
                    lastPositions.put(uuid, new Double[]{x, y, z});
                }
            }

            if (server.getTickCount() % 100 == 0) {
                lastPositions.keySet().removeIf(id -> server.getPlayerList().getPlayer(id) == null);
            }
        }
    }
}
