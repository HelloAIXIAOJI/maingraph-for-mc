package ltd.opens.mg.mc.core.blueprint.events;

import dev.architectury.event.EventResult;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.ExplosionEvent;
import dev.architectury.event.events.common.ChatEvent;
import dev.architectury.event.events.common.CommandPerformEvent;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import ltd.opens.mg.mc.core.blueprint.EventDispatcher;
import ltd.opens.mg.mc.core.blueprint.data.XYZ;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CommonEvents {
    // 位置追踪系统
    private static final Map<UUID, XYZ> lastPlayerPositions = new ConcurrentHashMap<>();

    public static void init() {
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            if (level.isClientSide()) return EventResult.pass();
            EventDispatcher.dispatch(MGMCEventType.BLOCK_BREAK, MGMCEventContext.builder(level)
                .pos(pos)
                .blockState(state)
                .player(player)
                .build());
            return EventResult.pass();
        });

        BlockEvent.PLACE.register((level, pos, state, placer) -> {
            if (level.isClientSide()) return EventResult.pass();
            Player player = (placer instanceof Player) ? (Player) placer : null;
            EventDispatcher.dispatch(MGMCEventType.BLOCK_PLACE, MGMCEventContext.builder(level)
                .pos(pos)
                .blockState(state)
                .player(player)
                .entity(placer)
                .build());
            return EventResult.pass();
        });

        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, face) -> {
            if (player.level().isClientSide()) return EventResult.pass();
            EventDispatcher.dispatch(MGMCEventType.BLOCK_INTERACT, MGMCEventContext.builder(player.level())
                .pos(pos)
                .player(player)
                .blockState(player.level().getBlockState(pos))
                .build());
            return EventResult.pass();
        });
        
        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            if (player.level().isClientSide()) return CompoundEventResult.pass();
            EventDispatcher.dispatch(MGMCEventType.ITEM_USE, MGMCEventContext.builder(player.level())
                .player(player)
                .entity(player)
                .item(player.getItemInHand(hand))
                .build());
            return CompoundEventResult.pass();
        });

        PlayerEvent.ATTACK_ENTITY.register((player, level, target, hand, result) -> {
            if (level.isClientSide()) return EventResult.pass();
            EventDispatcher.dispatch(MGMCEventType.PLAYER_ATTACK, MGMCEventContext.builder(level)
                .player(player)
                .entity(player)
                .targetEntity(target)
                .build());
            return EventResult.pass();
        });

        PlayerEvent.PLAYER_JOIN.register(player -> {
            if (player.level().isClientSide()) return;
            EventDispatcher.dispatch(MGMCEventType.PLAYER_JOIN, MGMCEventContext.builder(player.level())
                .player(player)
                .entity(player)
                .build());
        });

        PlayerEvent.PLAYER_QUIT.register(player -> {
            if (player.level().isClientSide()) return;
            // 清理位置追踪缓存
            lastPlayerPositions.remove(player.getUUID());
            EventDispatcher.dispatch(MGMCEventType.PLAYER_LEAVE, MGMCEventContext.builder(player.level())
                .player(player)
                .entity(player)
                .build());
        });

        // 服务器tick事件 - 检测玩家移动
        TickEvent.SERVER_POST.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.level().isClientSide()) continue;

                UUID playerId = player.getUUID();
                XYZ currentPos = new XYZ(player.getX(), player.getY(), player.getZ());
                XYZ oldPos = lastPlayerPositions.get(playerId);

                if (oldPos != null) {
                    double dx = currentPos.x() - oldPos.x();
                    double dy = currentPos.y() - oldPos.y();
                    double dz = currentPos.z() - oldPos.z();
                    double distanceSq = dx * dx + dy * dy + dz * dz;

                    // 如果移动距离大于阈值，触发PLAYER_MOVE事件
                    if (distanceSq > 1E-6) {
                        float speed = (float) Math.sqrt(distanceSq);
                        EventDispatcher.dispatch(MGMCEventType.PLAYER_MOVE, MGMCEventContext.builder(player.level())
                            .player(player)
                            .entity(player)
                            .pos(player.blockPosition())
                            .xyz(currentPos)
                            .speed(speed)
                            .build());
                    }
                }

                // 更新位置缓存
                lastPlayerPositions.put(playerId, currentPos);
            }
        });

        // 玩家tick事件 - 每游戏刻为在线玩家触发（高频事件）
        TickEvent.PLAYER_POST.register(player -> {
            if (player.level().isClientSide()) return;
            EventDispatcher.dispatch(MGMCEventType.PLAYER_TICK, MGMCEventContext.builder(player.level())
                .player(player)
                .entity(player)
                .build());
        });

        EntityEvent.ADD.register((entity, level) -> {
            if (level.isClientSide()) return EventResult.pass();
            EventDispatcher.dispatch(MGMCEventType.ENTITY_SPAWN, MGMCEventContext.builder(level)
                .entity(entity)
                .build());
            return EventResult.pass();
        });

        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (entity.level().isClientSide()) return EventResult.pass();
            
            EventDispatcher.dispatch(MGMCEventType.ENTITY_DEATH, MGMCEventContext.builder(entity.level())
                .entity(entity)
                .damageSource(source)
                .build());
                
            if (entity instanceof Player player) {
                EventDispatcher.dispatch(MGMCEventType.PLAYER_DEATH, MGMCEventContext.builder(player.level())
                    .player(player)
                    .entity(player)
                    .damageSource(source)
                    .build());
            }
            return EventResult.pass();
        });

        PlayerEvent.PLAYER_RESPAWN.register((player, conqueredEnd, reason) -> {
            if (player.level().isClientSide()) return;
            EventDispatcher.dispatch(MGMCEventType.PLAYER_RESPAWN, MGMCEventContext.builder(player.level())
                .player(player)
                .entity(player)
                .build());
        });

        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            if (entity.level().isClientSide()) return EventResult.pass();
            
            EventDispatcher.dispatch(MGMCEventType.ENTITY_HURT, MGMCEventContext.builder(entity.level())
                .entity(entity)
                .damageSource(source)
                .amount(amount)
                .build());
                
            if (entity instanceof Player player) {
                EventDispatcher.dispatch(MGMCEventType.PLAYER_HURT, MGMCEventContext.builder(player.level())
                    .player(player)
                    .entity(player)
                    .damageSource(source)
                    .amount(amount)
                    .build());
            }
            return EventResult.pass();
        });

        // --- 服务器 / 世界生命周期 ---
        LifecycleEvent.SERVER_STARTED.register(server -> {
            ServerLevel level = server.getLevel(Level.OVERWORLD);
            if (level == null) return;
            EventDispatcher.dispatch(MGMCEventType.SERVER_START, MGMCEventContext.builder(level).build());
        });

        LifecycleEvent.SERVER_STOPPED.register(server -> {
            ServerLevel level = server.getLevel(Level.OVERWORLD);
            if (level == null) return;
            EventDispatcher.dispatch(MGMCEventType.SERVER_STOP, MGMCEventContext.builder(level).build());
        });

        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> {
            EventDispatcher.dispatch(MGMCEventType.LEVEL_LOAD, MGMCEventContext.builder(level)
                .dimensionId(level.dimension().location().toString())
                .build());
        });

        // --- 玩家补充 ---
        PlayerEvent.CRAFT_ITEM.register((player, constructed, inventory) -> {
            if (player.level().isClientSide()) return;
            EventDispatcher.dispatch(MGMCEventType.PLAYER_CRAFT, MGMCEventContext.builder(player.level())
                .player(player).entity(player).item(constructed).itemId(itemIdOf(constructed)).build());
        });

        PlayerEvent.SMELT_ITEM.register((player, smelted) -> {
            if (player.level().isClientSide()) return;
            EventDispatcher.dispatch(MGMCEventType.PLAYER_SMELT, MGMCEventContext.builder(player.level())
                .player(player).entity(player).item(smelted).itemId(itemIdOf(smelted)).build());
        });

        PlayerEvent.DROP_ITEM.register((player, entity) -> {
            if (player.level().isClientSide()) return EventResult.pass();
            EventDispatcher.dispatch(MGMCEventType.PLAYER_DROP, MGMCEventContext.builder(player.level())
                .player(player).entity(player).item(entity.getItem()).itemId(itemIdOf(entity.getItem())).build());
            return EventResult.pass();
        });

        PlayerEvent.CHANGE_DIMENSION.register((player, oldLevel, newLevel) -> {
            if (player.level().isClientSide()) return;
            EventDispatcher.dispatch(MGMCEventType.PLAYER_CHANGE_DIMENSION, MGMCEventContext.builder(player.level())
                .player(player).entity(player)
                .dimensionFrom(oldLevel.location().toString())
                .dimensionTo(newLevel.location().toString())
                .build());
        });

        PlayerEvent.PLAYER_ADVANCEMENT.register((player, advancement) -> {
            if (player.level().isClientSide()) return;
            EventDispatcher.dispatch(MGMCEventType.PLAYER_ADVANCEMENT, MGMCEventContext.builder(player.level())
                .player(player).entity(player).advancementId(advancement.id().toString()).build());
        });

        PlayerEvent.OPEN_MENU.register((player, menu) -> {
            if (player.level().isClientSide()) return;
            EventDispatcher.dispatch(MGMCEventType.PLAYER_OPEN_MENU, MGMCEventContext.builder(player.level())
                .player(player).entity(player).menuType(menu.getClass().getSimpleName()).build());
        });

        PlayerEvent.CLOSE_MENU.register((player, menu) -> {
            if (player.level().isClientSide()) return;
            EventDispatcher.dispatch(MGMCEventType.PLAYER_CLOSE_MENU, MGMCEventContext.builder(player.level())
                .player(player).entity(player).menuType(menu.getClass().getSimpleName()).build());
        });

        ChatEvent.RECEIVED.register((player, component) -> {
            if (player == null || player.level().isClientSide()) return EventResult.pass();
            EventDispatcher.dispatch(MGMCEventType.PLAYER_CHAT, MGMCEventContext.builder(player.level())
                .player(player).entity(player).message(component.getString()).build());
            return EventResult.pass();
        });

        CommandPerformEvent.EVENT.register(event -> {
            var results = event.getResults();
            if (results == null) return EventResult.pass();
            CommandSourceStack source = results.getContext().getSource();
            if (!(source.getLevel() instanceof ServerLevel serverLevel)) return EventResult.pass();
            var builder = MGMCEventContext.builder(serverLevel).command(results.getReader().getString());
            if (source.getEntity() instanceof Player p) builder.player(p).entity(p);
            EventDispatcher.dispatch(MGMCEventType.COMMAND_PERFORM, builder.build());
            return EventResult.pass();
        });

        // --- 实体补充 ---
        EntityEvent.ANIMAL_TAME.register((animal, player) -> {
            if (animal.level().isClientSide()) return EventResult.pass();
            EventDispatcher.dispatch(MGMCEventType.ENTITY_TAME, MGMCEventContext.builder(animal.level())
                .entity(animal).targetEntity(player).build());
            return EventResult.pass();
        });

        InteractionEvent.INTERACT_ENTITY.register((player, entity, hand) -> {
            if (player.level().isClientSide()) return EventResult.pass();
            EventDispatcher.dispatch(MGMCEventType.INTERACT_ENTITY, MGMCEventContext.builder(player.level())
                .player(player).entity(player).targetEntity(entity).build());
            return EventResult.pass();
        });

        ExplosionEvent.DETONATE.register((level, explosion, affectedEntities) -> {
            if (!(level instanceof ServerLevel serverLevel)) return;
            Vec3 pos = explosion.center();
            EventDispatcher.dispatch(MGMCEventType.EXPLOSION, MGMCEventContext.builder(serverLevel)
                .explosionPos(new XYZ(pos.x, pos.y, pos.z)).build());
        });
    }

    private static String itemIdOf(ItemStack stack) {
        return stack.isEmpty() ? "minecraft:air" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }
}
