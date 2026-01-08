package ltd.opens.mg.mc.network;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.MaingraphforMC;
import ltd.opens.mg.mc.client.gui.screens.BlueprintMappingScreen;
import ltd.opens.mg.mc.client.gui.screens.BlueprintSelectionForMappingScreen;
import ltd.opens.mg.mc.client.gui.screens.BlueprintScreen;
import ltd.opens.mg.mc.client.gui.screens.BlueprintSelectionScreen;
import ltd.opens.mg.mc.network.payloads.*;
import ltd.opens.mg.mc.core.blueprint.routing.BlueprintRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.stream.Collectors;

public class BlueprintNetworkHandler {

    public static class Server {
        private static boolean hasPermission(ServerPlayer player) {
            if (player.level().getServer() == null) return false;
            return player.level().getServer().getProfilePermissions(new NameAndId(player.getUUID(), player.getGameProfile().name())).level().id() >= 2;
        }

        public static void handleRequestList(final RequestBlueprintListPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    if (!hasPermission(player)) {
                        return;
                    }
                    var blueprints = MaingraphforMC.BlueprintServerHandler.getAllBlueprints((ServerLevel) player.level());
                    java.util.List<String> names = blueprints.stream()
                            .map(bp -> bp.has("name") ? bp.get("name").getAsString() : "unknown")
                            .collect(Collectors.toList());
                    
                    // Fallback to filenames if name property is missing
                    if (names.isEmpty() || names.contains("unknown")) {
                        try (var stream = java.nio.file.Files.list(MaingraphforMC.BlueprintServerHandler.getBlueprintsDir((ServerLevel) player.level()))) {
                            names = stream.filter(p -> p.toString().endsWith(".json"))
                                    .map(p -> p.getFileName().toString())
                                    .collect(Collectors.toList());
                        } catch (Exception e) {}
                    }
                    
                    context.reply(new ResponseBlueprintListPayload(names));
                }
            });
        }

        public static void handleRequestData(final RequestBlueprintDataPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    if (!hasPermission(player)) {
                        return;
                    }
                    JsonObject bp = MaingraphforMC.BlueprintServerHandler.getBlueprint((ServerLevel) player.level(), payload.name());
                    if (bp != null) {
                        long version = MaingraphforMC.BlueprintServerHandler.getBlueprintVersion((ServerLevel) player.level(), payload.name());
                        context.reply(new ResponseBlueprintDataPayload(payload.name(), bp.toString(), version));
                    }
                }
            });
        }

        public static void handleSave(final SaveBlueprintPayload payload, final IPayloadContext context) {
            if (context.player() instanceof ServerPlayer player) {
                if (!hasPermission(player)) {
                    context.reply(new SaveResultPayload(false, "You do not have permission to save blueprints.", 0));
                    return;
                }
                MaingraphforMC.BlueprintServerHandler.saveBlueprintAsync(
                        (ServerLevel) player.level(),
                        payload.name(),
                        payload.data(),
                        payload.expectedVersion()
                ).thenAccept(result -> {
                    context.reply(new SaveResultPayload(result.success(), result.message(), result.newVersion()));
                });
            }
        }

        public static void handleDelete(final DeleteBlueprintPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    if (!hasPermission(player)) {
                        return;
                    }
                    MaingraphforMC.BlueprintServerHandler.deleteBlueprint((ServerLevel) player.level(), payload.name());
                    // Refresh list for all clients or just the sender?
                    // For simplicity, the client can request a refresh or we can broadcast.
                    // Usually, the client that deleted it will refresh its list.
                    var blueprints = MaingraphforMC.BlueprintServerHandler.getAllBlueprints((ServerLevel) player.level());
                    java.util.List<String> names = blueprints.stream()
                            .map(bp -> bp.has("name") ? bp.get("name").getAsString() : "unknown")
                            .collect(Collectors.toList());
                    context.reply(new ResponseBlueprintListPayload(names));
                }
            });
        }

        public static void handleRequestMappings(final RequestMappingsPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    if (!hasPermission(player)) return;
                    context.reply(new ResponseMappingsPayload(BlueprintRouter.getFullRoutingTable()));
                }
            });
        }

        public static void handleSaveMappings(final SaveMappingsPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    if (!hasPermission(player)) return;
                    BlueprintRouter.updateAllMappings(payload.mappings());
                    // 广播更新？目前先简单回复
                    context.reply(new ResponseMappingsPayload(BlueprintRouter.getFullRoutingTable()));
                }
            });
        }

        public static void handleRename(final RenameBlueprintPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    if (!hasPermission(player)) {
                        return;
                    }
                    MaingraphforMC.BlueprintServerHandler.renameBlueprint((ServerLevel) player.level(), payload.oldName(), payload.newName());
                    var blueprints = MaingraphforMC.BlueprintServerHandler.getAllBlueprints((ServerLevel) player.level());
                    java.util.List<String> names = blueprints.stream()
                            .map(bp -> bp.has("name") ? bp.get("name").getAsString() : "unknown")
                            .collect(Collectors.toList());
                    context.reply(new ResponseBlueprintListPayload(names));
                }
            });
        }
    }

    public static class Client {
        public static void handleResponseList(final ResponseBlueprintListPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                if (Minecraft.getInstance().screen instanceof BlueprintSelectionScreen selectionScreen) {
                    selectionScreen.updateListFromServer(payload.blueprints());
                } else if (Minecraft.getInstance().screen instanceof BlueprintSelectionForMappingScreen mappingSelectionScreen) {
                    mappingSelectionScreen.updateListFromServer(payload.blueprints());
                }
            });
        }

        public static void handleResponseData(final ResponseBlueprintDataPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                if (Minecraft.getInstance().screen instanceof BlueprintScreen screen) {
                    screen.loadFromNetwork(payload.data(), payload.version());
                }
            });
        }

        public static void handleSaveResult(final SaveResultPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                if (Minecraft.getInstance().screen instanceof BlueprintScreen screen) {
                    screen.onSaveResult(payload.success(), payload.message(), payload.newVersion());
                }
            });
        }

        public static void handleResponseMappings(final ResponseMappingsPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                if (Minecraft.getInstance().screen instanceof BlueprintMappingScreen screen) {
                    screen.updateMappingsFromServer(payload.mappings());
                }
            });
        }
    }
}
