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
            .then(Commands.argument("name", StringArgumentType.word())
                .then(Commands.argument("args", StringArgumentType.greedyString())
                    .executes(context -> {
                        String name = StringArgumentType.getString(context, "name");
                        String argsStr = StringArgumentType.getString(context, "args");
                        String[] args = argsStr.split("\\s+");
                        
                        CommandSourceStack source = context.getSource();
                        ServerLevel level = source.getLevel();
                        String triggerUuid = source.getEntity() != null ? source.getEntity().getUUID().toString() : "";
                        String triggerName = source.getTextName();
                        var pos = source.getPosition();
                        
                        try {
                            Path dataFile = level.getServer().getWorldPath(LevelResource.ROOT).resolve("blueprint_data.json");
                            if (Files.exists(dataFile)) {
                                String json = Files.readString(dataFile);
                                BlueprintEngine.execute(level, json, "on_mgrun", name, args, triggerUuid, triggerName, pos.x, pos.y, pos.z);
                            } else {
                                context.getSource().sendFailure(Component.literal("Blueprint data file not found on server: " + dataFile.toAbsolutePath()));
                            }
                        } catch (Exception e) {
                            context.getSource().sendFailure(Component.literal("Failed to execute blueprint on server: " + e.getMessage()));
                        }
                        return 1;
                    })
                )
                .executes(context -> {
                    String name = StringArgumentType.getString(context, "name");
                    CommandSourceStack source = context.getSource();
                    ServerLevel level = source.getLevel();
                    String triggerUuid = source.getEntity() != null ? source.getEntity().getUUID().toString() : "";
                    String triggerName = source.getTextName();
                    var pos = source.getPosition();
                    try {
                        Path dataFile = level.getServer().getWorldPath(LevelResource.ROOT).resolve("blueprint_data.json");
                        if (Files.exists(dataFile)) {
                            String json = Files.readString(dataFile);
                            BlueprintEngine.execute(level, json, "on_mgrun", name, new String[0], triggerUuid, triggerName, pos.x, pos.y, pos.z);
                        } else {
                            context.getSource().sendFailure(Component.literal("Blueprint data file not found on server: " + dataFile.toAbsolutePath()));
                        }
                    } catch (Exception e) {
                        context.getSource().sendFailure(Component.literal("Failed to execute blueprint on server: " + e.getMessage()));
                    }
                    return 1;
                })
            )
        );
    }

    public static class BlueprintServerHandler {
        private final java.util.Map<java.util.UUID, Double[]> lastPositions = new java.util.HashMap<>();
        private JsonObject cachedBlueprint = null;
        private long lastBlueprintLoadTime = 0;

        private JsonObject getBlueprint(ServerLevel level) {
            try {
                Path dataFile = level.getServer().getWorldPath(LevelResource.ROOT).resolve("blueprint_data.json");
                if (Files.exists(dataFile)) {
                    long lastModified = Files.getLastModifiedTime(dataFile).toMillis();
                    if (cachedBlueprint == null || lastModified > lastBlueprintLoadTime) {
                        String json = Files.readString(dataFile);
                        cachedBlueprint = JsonParser.parseString(json).getAsJsonObject();
                        lastBlueprintLoadTime = lastModified;
                    }
                    return cachedBlueprint;
                }
            } catch (Exception e) {
                // Error loading or parsing
            }
            return null;
        }

        @SubscribeEvent
        public void onServerTick(ServerTickEvent.Post event) {
            // Player movement check on server
            var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;

            // Only check every 5 ticks (1/4 second) to reduce load
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

                    // Only trigger if moved more than 0.5 blocks (squared distance > 0.25)
                    if (dx * dx + dy * dy + dz * dz > 0.25) {
                        try {
                            ServerLevel level = (ServerLevel) player.level();
                            JsonObject blueprint = getBlueprint(level);
                            if (blueprint != null) {
                                BlueprintEngine.execute(level, blueprint, "on_player_move", "", new String[0], 
                                    uuid.toString(), player.getName().getString(), x, y, z);
                            }
                            lastPositions.put(uuid, new Double[]{x, y, z});
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                } else {
                    lastPositions.put(uuid, new Double[]{x, y, z});
                }
            }

            // Cleanup offline players
            if (server.getTickCount() % 100 == 0) {
                lastPositions.keySet().removeIf(id -> server.getPlayerList().getPlayer(id) == null);
            }
        }
    }
}
