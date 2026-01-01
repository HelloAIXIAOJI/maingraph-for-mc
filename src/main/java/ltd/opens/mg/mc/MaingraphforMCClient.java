package ltd.opens.mg.mc;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import ltd.opens.mg.mc.client.gui.BlueprintScreen;
import net.minecraft.network.chat.Component;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import ltd.opens.mg.mc.core.blueprint.engine.BlueprintEngine;
import com.mojang.brigadier.arguments.StringArgumentType;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;

import net.minecraft.world.level.storage.LevelResource;
import java.io.IOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = MaingraphforMC.MODID, dist = Dist.CLIENT)
public class MaingraphforMCClient {
    public static final KeyMapping.Category MGMC_CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(MaingraphforMC.MODID, "main"));

    public static final KeyMapping BLUEPRINT_KEY = new KeyMapping(
        "key.mgmc.open_blueprint",
        KeyConflictContext.IN_GAME,
        KeyModifier.CONTROL,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_B,
        MGMC_CATEGORY
    );

    private double lastX, lastY, lastZ;
    private boolean hasLastPos = false;
    private JsonObject cachedBlueprint = null;
    private long lastBlueprintLoadTime = 0;
    private int tickCounter = 0;

    public MaingraphforMCClient(ModContainer container, IEventBus modEventBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(this::onRegisterKeyMappings);
        modEventBus.addListener(this::onClientSetup);
        
        // Register for game events (ClientTickEvent, etc.)
        NeoForge.EVENT_BUS.register(this);
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(MGMC_CATEGORY);
        event.register(BLUEPRINT_KEY);
    }

    private JsonObject getBlueprint() {
        try {
            Path dataFile = getBlueprintPath();
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
    public void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (BLUEPRINT_KEY.consumeClick()) {
            mc.setScreen(new BlueprintScreen());
            // Clear cache when opening screen to ensure fresh reload after editing
            cachedBlueprint = null;
        }

        if (mc.player != null && mc.level != null) {
            tickCounter++;
            // Only check movement every 5 ticks to save performance
            if (tickCounter % 5 != 0) return;

            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();

            if (hasLastPos) {
                double dx = x - lastX;
                double dy = y - lastY;
                double dz = z - lastZ;

                // Only trigger if moved more than 0.5 blocks (squared distance > 0.25)
                if (dx * dx + dy * dy + dz * dz > 0.25) {
                    JsonObject blueprint = getBlueprint();
                    if (blueprint != null) {
                        BlueprintEngine.execute(mc.level, blueprint, "on_player_move", "", new String[0], 
                            mc.player.getUUID().toString(), mc.player.getName().getString(), x, y, z);
                    }
                    lastX = x;
                    lastY = y;
                    lastZ = z;
                }
            } else {
                lastX = x;
                lastY = y;
                lastZ = z;
                hasLastPos = true;
            }
        } else {
            hasLastPos = false;
        }
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // Client setup logic
    }

    @SubscribeEvent
    public void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(literal("mgrun")
            .then(argument("name", StringArgumentType.string())
                .then(argument("args", StringArgumentType.greedyString())
                    .executes(context -> {
                        String name = StringArgumentType.getString(context, "name");
                        String argsStr = StringArgumentType.getString(context, "args");
                        String[] args = argsStr.split("\\s+");
                        
                        var source = context.getSource();
                        String triggerUuid = source.getEntity() != null ? source.getEntity().getUUID().toString() : "";
                        String triggerName = source.getTextName();
                        var pos = source.getPosition();
                        
                        try {
                            Path dataFile = getBlueprintPath();
                            if (Files.exists(dataFile)) {
                                String json = Files.readString(dataFile);
                                BlueprintEngine.execute(Minecraft.getInstance().level, json, "on_mgrun", name, args, triggerUuid, triggerName, pos.x, pos.y, pos.z);
                            } else {
                                context.getSource().sendFailure(Component.literal("Blueprint data file not found: " + dataFile.toAbsolutePath()));
                            }
                        } catch (Exception e) {
                            context.getSource().sendFailure(Component.literal("Failed to execute blueprint: " + e.getMessage()));
                        }
                        return 1;
                    })
                )
                .executes(context -> {
                    String name = StringArgumentType.getString(context, "name");
                    var source = context.getSource();
                    String triggerUuid = source.getEntity() != null ? source.getEntity().getUUID().toString() : "";
                    String triggerName = source.getTextName();
                    var pos = source.getPosition();
                    try {
                        Path dataFile = getBlueprintPath();
                        if (Files.exists(dataFile)) {
                            String json = Files.readString(dataFile);
                            BlueprintEngine.execute(Minecraft.getInstance().level, json, "on_mgrun", name, new String[0], triggerUuid, triggerName, pos.x, pos.y, pos.z);
                        } else {
                            context.getSource().sendFailure(Component.literal("Blueprint data file not found: " + dataFile.toAbsolutePath()));
                        }
                    } catch (Exception e) {
                        context.getSource().sendFailure(Component.literal("Failed to execute blueprint: " + e.getMessage()));
                    }
                    return 1;
                })
            )
        );
    }

    public static Path getBlueprintPath() {
        Minecraft mc = Minecraft.getInstance();
        Path baseDir;
        if (mc.getSingleplayerServer() != null) {
            // Singleplayer world directory
            baseDir = mc.getSingleplayerServer().getWorldPath(LevelResource.ROOT);
        } else if (mc.getCurrentServer() != null) {
            // Multiplayer server specific directory in game root
            String serverName = mc.getCurrentServer().ip.replaceAll("[^a-zA-Z0-9.-]", "_");
            baseDir = mc.gameDirectory.toPath().resolve("blueprints").resolve(serverName);
        } else {
            // Fallback to game root
            baseDir = mc.gameDirectory.toPath();
        }

        try {
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }
        } catch (IOException e) {
            MaingraphforMC.LOGGER.error("Failed to create blueprint directory", e);
        }

        return baseDir.resolve("blueprint_data.json");
    }
}
