package ltd.opens.mg.mc.core.blueprint.events;

import java.util.Objects;

/**
 * Event type definition for MGMC blueprint events.
 * Converted from enum to class to allow extensibility.
 */
public class MGMCEventType {
    private final String id;

    public MGMCEventType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MGMCEventType that = (MGMCEventType) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Standard Events
    public static final MGMCEventType PLAYER_JOIN = new MGMCEventType("player_join");
    public static final MGMCEventType PLAYER_LEAVE = new MGMCEventType("player_leave");
    public static final MGMCEventType PLAYER_RESPAWN = new MGMCEventType("player_respawn");
    public static final MGMCEventType PLAYER_DEATH = new MGMCEventType("player_death");
    public static final MGMCEventType PLAYER_ATTACK = new MGMCEventType("player_attack");
    public static final MGMCEventType PLAYER_HURT = new MGMCEventType("player_hurt");
    public static final MGMCEventType PLAYER_TICK = new MGMCEventType("player_tick");
    public static final MGMCEventType PLAYER_MOVE = new MGMCEventType("player_move");

    public static final MGMCEventType BLOCK_BREAK = new MGMCEventType("block_break");
    public static final MGMCEventType BLOCK_PLACE = new MGMCEventType("block_place");
    public static final MGMCEventType BLOCK_INTERACT = new MGMCEventType("block_interact");
    public static final MGMCEventType BLOCK_LEFT_CLICK = new MGMCEventType("block_left_click");

    public static final MGMCEventType ENTITY_SPAWN = new MGMCEventType("entity_spawn");
    public static final MGMCEventType ENTITY_DEATH = new MGMCEventType("entity_death");
    public static final MGMCEventType ENTITY_HURT = new MGMCEventType("entity_hurt");

    public static final MGMCEventType ITEM_PICKUP = new MGMCEventType("item_pickup");
    public static final MGMCEventType ITEM_USE = new MGMCEventType("item_use");

    // 服务器 / 世界生命周期
    public static final MGMCEventType SERVER_START = new MGMCEventType("server_start");
    public static final MGMCEventType SERVER_STOP = new MGMCEventType("server_stop");
    public static final MGMCEventType LEVEL_LOAD = new MGMCEventType("level_load");

    // 玩家补充
    public static final MGMCEventType PLAYER_CRAFT = new MGMCEventType("player_craft");
    public static final MGMCEventType PLAYER_SMELT = new MGMCEventType("player_smelt");
    public static final MGMCEventType PLAYER_DROP = new MGMCEventType("player_drop_item");
    public static final MGMCEventType PLAYER_CHANGE_DIMENSION = new MGMCEventType("player_change_dimension");
    public static final MGMCEventType PLAYER_ADVANCEMENT = new MGMCEventType("player_advancement");
    public static final MGMCEventType PLAYER_OPEN_MENU = new MGMCEventType("player_open_menu");
    public static final MGMCEventType PLAYER_CLOSE_MENU = new MGMCEventType("player_close_menu");
    public static final MGMCEventType PLAYER_CHAT = new MGMCEventType("player_chat");
    public static final MGMCEventType COMMAND_PERFORM = new MGMCEventType("command_perform");

    // 实体补充
    public static final MGMCEventType ENTITY_TAME = new MGMCEventType("entity_tame");
    public static final MGMCEventType INTERACT_ENTITY = new MGMCEventType("interact_entity");
    public static final MGMCEventType EXPLOSION = new MGMCEventType("explosion");

    public static final MGMCEventType BLUEPRINT_CALLED = new MGMCEventType("blueprint_called");
    public static final MGMCEventType MGRUN = new MGMCEventType("mgrun");
}
