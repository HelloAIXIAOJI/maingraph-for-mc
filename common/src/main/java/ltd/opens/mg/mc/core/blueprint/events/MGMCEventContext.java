package ltd.opens.mg.mc.core.blueprint.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import ltd.opens.mg.mc.core.blueprint.data.XYZ;

import org.jetbrains.annotations.Nullable;

public class MGMCEventContext {
    private final Level level;
    @Nullable private final Entity entity;
    @Nullable private final Player player;
    @Nullable private final Entity targetEntity; // For attacks/interactions
    @Nullable private final BlockPos pos;
    @Nullable private final net.minecraft.world.level.block.state.BlockState blockState;
    @Nullable private final ItemStack item;
    @Nullable private final String itemId; // For item pickup events
    @Nullable private final DamageSource damageSource;
    private final float amount;
    @Nullable private final XYZ xyz;
    private final float speed;

    // 维度切换 / 菜单 / 成就 / 聊天 / 指令 / 爆炸
    @Nullable private final String dimensionFrom;
    @Nullable private final String dimensionTo;
    @Nullable private final String dimensionId;
    @Nullable private final String advancementId;
    @Nullable private final String menuType;
    @Nullable private final String message;
    @Nullable private final String command;
    @Nullable private final XYZ explosionPos;

    // For MGRUN
    @Nullable private final String eventName;
    @Nullable private final Object[] args;

    public MGMCEventContext(Level level, @Nullable Entity entity, @Nullable Player player, @Nullable Entity targetEntity,
                            @Nullable BlockPos pos, @Nullable net.minecraft.world.level.block.state.BlockState blockState,
                            @Nullable ItemStack item, @Nullable String itemId, @Nullable DamageSource damageSource, float amount,
                            @Nullable XYZ xyz, float speed,
                            @Nullable String dimensionFrom, @Nullable String dimensionTo, @Nullable String dimensionId,
                            @Nullable String advancementId, @Nullable String menuType, @Nullable String message,
                            @Nullable String command, @Nullable XYZ explosionPos,
                            @Nullable String eventName, @Nullable Object[] args) {
        this.level = level;
        this.entity = entity;
        this.player = player;
        this.targetEntity = targetEntity;
        this.pos = pos;
        this.blockState = blockState;
        this.item = item;
        this.itemId = itemId;
        this.damageSource = damageSource;
        this.amount = amount;
        this.xyz = xyz;
        this.speed = speed;
        this.dimensionFrom = dimensionFrom;
        this.dimensionTo = dimensionTo;
        this.dimensionId = dimensionId;
        this.advancementId = advancementId;
        this.menuType = menuType;
        this.message = message;
        this.command = command;
        this.explosionPos = explosionPos;
        this.eventName = eventName;
        this.args = args;
    }

    public Level getLevel() { return level; }
    @Nullable public Entity getEntity() { return entity; }
    @Nullable public Player getPlayer() { return player; }
    @Nullable public Entity getTargetEntity() { return targetEntity; }
    @Nullable public BlockPos getPos() { return pos; }
    @Nullable public net.minecraft.world.level.block.state.BlockState getBlockState() { return blockState; }
    @Nullable public ItemStack getItem() { return item; }
    @Nullable public String getItemId() { return itemId; }
    @Nullable public DamageSource getDamageSource() { return damageSource; }
    public float getAmount() { return amount; }
    @Nullable public XYZ getXyz() { return xyz; }
    public float getSpeed() { return speed; }
    @Nullable public String getDimensionFrom() { return dimensionFrom; }
    @Nullable public String getDimensionTo() { return dimensionTo; }
    @Nullable public String getDimensionId() { return dimensionId; }
    @Nullable public String getAdvancementId() { return advancementId; }
    @Nullable public String getMenuType() { return menuType; }
    @Nullable public String getMessage() { return message; }
    @Nullable public String getCommand() { return command; }
    @Nullable public XYZ getExplosionPos() { return explosionPos; }
    @Nullable public String getEventName() { return eventName; }
    @Nullable public Object[] getArgs() { return args; }

    public static Builder builder(Level level) {
        return new Builder(level);
    }

    public static class Builder {
        private final Level level;
        private Entity entity;
        private Player player;
        private Entity targetEntity;
        private BlockPos pos;
        private net.minecraft.world.level.block.state.BlockState blockState;
        private ItemStack item;
        private String itemId;
        private DamageSource damageSource;
        private float amount;
        private XYZ xyz;
        private float speed;
        private String dimensionFrom;
        private String dimensionTo;
        private String dimensionId;
        private String advancementId;
        private String menuType;
        private String message;
        private String command;
        private XYZ explosionPos;
        private String eventName;
        private Object[] args;

        public Builder(Level level) {
            this.level = level;
        }

        public Builder entity(Entity entity) { this.entity = entity; return this; }
        public Builder player(Player player) { this.player = player; return this; }
        public Builder targetEntity(Entity targetEntity) { this.targetEntity = targetEntity; return this; }
        public Builder pos(BlockPos pos) { this.pos = pos; return this; }
        public Builder blockState(net.minecraft.world.level.block.state.BlockState blockState) { this.blockState = blockState; return this; }
        public Builder item(ItemStack item) { this.item = item; return this; }
        public Builder itemId(String itemId) { this.itemId = itemId; return this; }
        public Builder damageSource(DamageSource damageSource) { this.damageSource = damageSource; return this; }
        public Builder amount(float amount) { this.amount = amount; return this; }
        public Builder xyz(XYZ xyz) { this.xyz = xyz; return this; }
        public Builder speed(float speed) { this.speed = speed; return this; }
        public Builder dimensionFrom(String v) { this.dimensionFrom = v; return this; }
        public Builder dimensionTo(String v) { this.dimensionTo = v; return this; }
        public Builder dimensionId(String v) { this.dimensionId = v; return this; }
        public Builder advancementId(String v) { this.advancementId = v; return this; }
        public Builder menuType(String v) { this.menuType = v; return this; }
        public Builder message(String v) { this.message = v; return this; }
        public Builder command(String v) { this.command = v; return this; }
        public Builder explosionPos(XYZ v) { this.explosionPos = v; return this; }
        public Builder eventName(String eventName) { this.eventName = eventName; return this; }
        public Builder args(Object[] args) { this.args = args; return this; }

        public MGMCEventContext build() {
            return new MGMCEventContext(level, entity, player, targetEntity, pos, blockState, item, itemId, damageSource, amount, xyz, speed, dimensionFrom, dimensionTo, dimensionId, advancementId, menuType, message, command, explosionPos, eventName, args);
        }
    }
}
