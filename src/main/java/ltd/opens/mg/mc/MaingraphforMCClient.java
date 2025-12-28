package ltd.opens.mg.mc;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import ltd.opens.mg.mc.client.gui.BlueprintScreen;
import ltd.opens.mg.mc.client.gui.BlueprintWebServer;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.awt.Desktop;
import java.net.URI;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = MaingraphforMC.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = MaingraphforMC.MODID, value = Dist.CLIENT)
public class MaingraphforMCClient {
    public MaingraphforMCClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        MaingraphforMC.LOGGER.info("HELLO FROM CLIENT SETUP");
        MaingraphforMC.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        
        // Sync player info to web server
        BlueprintWebServer.setPlayerInfo(
            Minecraft.getInstance().getUser().getName(),
            Minecraft.getInstance().getUser().getProfileId().toString()
        );
        
        // Start the web server
        BlueprintWebServer.start();
    }

    @SubscribeEvent
    static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(literal("mgmc")
            .then(literal("gui")
                .executes(context -> {
                        try {
                            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                Desktop.getDesktop().browse(new URI(BlueprintWebServer.getUrl()));
                            } else {
                                // Fallback: print to chat
                                context.getSource().sendFailure(net.minecraft.network.chat.Component.literal("Cannot open browser automatically. Please visit: " + BlueprintWebServer.getUrl()));
                            }
                        } catch (Exception e) {
                            context.getSource().sendFailure(net.minecraft.network.chat.Component.literal("Failed to open browser: " + e.getMessage()));
                        }
                        return 1;
                    })
            )
        );

        event.getDispatcher().register(literal("mgrun")
            .then(argument("name", StringArgumentType.string())
                .then(argument("args", StringArgumentType.greedyString())
                    .executes(context -> {
                        String name = StringArgumentType.getString(context, "name");
                        String argsStr = StringArgumentType.getString(context, "args");
                        String[] args = argsStr.split("\\s+");
                        // Trigger execution engine
                        BlueprintWebServer.executeBlueprint("on_mgrun", name, args);
                        return 1;
                    })
                )
                .executes(context -> {
                    String name = StringArgumentType.getString(context, "name");
                    // Trigger execution engine with no args
                    BlueprintWebServer.executeBlueprint("on_mgrun", name, new String[0]);
                    return 1;
                })
            )
        );
    }
}
