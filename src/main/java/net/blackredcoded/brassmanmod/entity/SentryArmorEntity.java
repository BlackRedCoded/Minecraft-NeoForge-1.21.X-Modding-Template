package net.blackredcoded.brassmanmod.entity;

import net.blackredcoded.brassmanmod.blockentity.AirCompressorBlockEntity;
import net.blackredcoded.brassmanmod.blockentity.BrassArmorStandBlockEntity;
import net.blackredcoded.brassmanmod.blocks.BrassArmorStandBaseBlock;
import net.blackredcoded.brassmanmod.items.BrassManChestplateItem;
import net.blackredcoded.brassmanmod.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SentryArmorEntity extends LivingEntity {
    private static final EntityDataAccessor<ItemStack> HELMET =
            SynchedEntityData.defineId(SentryArmorEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> CHESTPLATE_STACK =
            SynchedEntityData.defineId(SentryArmorEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> LEGGINGS =
            SynchedEntityData.defineId(SentryArmorEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> BOOTS =
            SynchedEntityData.defineId(SentryArmorEntity.class, EntityDataSerializers.ITEM_STACK);

    private String targetSetName;
    private int scanTick = 0;
    private static final int POWER_DRAIN_INTERVAL = 20;

    public SentryArmorEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public SentryArmorEntity(Level level, BlockPos spawnPos, String setName,
                             ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
        this(ModEntityTypes.SENTRY_ARMOR.get(), level);
        this.targetSetName = setName;
        this.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        this.entityData.set(HELMET, helmet.copy());
        this.entityData.set(CHESTPLATE_STACK, chestplate.copy());
        this.entityData.set(LEGGINGS, leggings.copy());
        this.entityData.set(BOOTS, boots.copy());

        this.setItemSlot(EquipmentSlot.HEAD, helmet.copy());
        this.setItemSlot(EquipmentSlot.CHEST, chestplate.copy());
        this.setItemSlot(EquipmentSlot.LEGS, leggings.copy());
        this.setItemSlot(EquipmentSlot.FEET, boots.copy());

        // Make completely invisible
        this.setInvisible(true);
        this.setCustomNameVisible(false);
        this.setNoGravity(true); // Optional: makes it float
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HELMET, ItemStack.EMPTY);
        builder.define(CHESTPLATE_STACK, ItemStack.EMPTY);
        builder.define(LEGGINGS, ItemStack.EMPTY);
        builder.define(BOOTS, ItemStack.EMPTY);
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return java.util.List.of(
                this.getItemBySlot(EquipmentSlot.FEET),
                this.getItemBySlot(EquipmentSlot.LEGS),
                this.getItemBySlot(EquipmentSlot.CHEST),
                this.getItemBySlot(EquipmentSlot.HEAD)
        );
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> this.entityData.get(HELMET);
            case CHEST -> this.entityData.get(CHESTPLATE_STACK);
            case LEGS -> this.entityData.get(LEGGINGS);
            case FEET -> this.entityData.get(BOOTS);
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        switch (slot) {
            case HEAD -> this.entityData.set(HELMET, stack);
            case CHEST -> this.entityData.set(CHESTPLATE_STACK, stack);
            case LEGS -> this.entityData.set(LEGGINGS, stack);
            case FEET -> this.entityData.set(BOOTS, stack);
        }
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        scanTick++;

        if (scanTick % POWER_DRAIN_INTERVAL == 0) {
            if (!drainPower()) {
                dropArmorPieces();
                this.discard();
                return;
            }
        }

        if (scanTick % 40 == 0) {
            BlockPos targetStand = findMatchingArmorStand();
            if (targetStand != null) {
                flyToArmorStand(targetStand);
                this.discard();
            }
        }
    }

    private boolean drainPower() {
        ItemStack chestplate = this.entityData.get(CHESTPLATE_STACK);
        if (chestplate.getItem() instanceof BrassManChestplateItem brass) {
            int power = brass.power(chestplate);
            if (power >= 10) {
                brass.setPower(chestplate, power - 10);
                this.entityData.set(CHESTPLATE_STACK, chestplate);
                this.setItemSlot(EquipmentSlot.CHEST, chestplate);
                return true;
            }
        }
        return false;
    }

    private BlockPos findMatchingArmorStand() {
        if (targetSetName == null) return null;

        BlockPos pos = this.blockPosition();
        for (BlockPos scanPos : BlockPos.betweenClosed(pos.offset(-32, -16, -32), pos.offset(32, 16, 32))) {
            if (level().getBlockEntity(scanPos) instanceof AirCompressorBlockEntity compressor) {
                if (targetSetName.equals(compressor.getCustomName().getString())) {
                    BlockPos above = scanPos.above();
                    if (level().getBlockState(above).getBlock() instanceof BrassArmorStandBaseBlock) {
                        if (level().getBlockEntity(above) instanceof BrassArmorStandBlockEntity stand) {
                            if (stand.getArmor(0).isEmpty() && stand.getArmor(1).isEmpty() &&
                                    stand.getArmor(2).isEmpty() && stand.getArmor(3).isEmpty()) {
                                return above;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void flyToArmorStand(BlockPos standPos) {
        if (!(level() instanceof ServerLevel)) return;

        BrassArmorStandBlockEntity stand = (BrassArmorStandBlockEntity) level().getBlockEntity(standPos);
        if (stand == null) return;

        stand.setArmor(0, this.entityData.get(HELMET));
        stand.setArmor(1, this.entityData.get(CHESTPLATE_STACK));
        stand.setArmor(2, this.entityData.get(LEGGINGS));
        stand.setArmor(3, this.entityData.get(BOOTS));
    }

    private void dropArmorPieces() {
        if (!(level() instanceof ServerLevel)) return;

        ItemStack[] pieces = {
                this.entityData.get(HELMET),
                this.entityData.get(CHESTPLATE_STACK),
                this.entityData.get(LEGGINGS),
                this.entityData.get(BOOTS)
        };

        for (ItemStack piece : pieces) {
            if (!piece.isEmpty()) {
                ItemEntity droppedItem = this.spawnAtLocation(piece.copy());
                // IMPORTANT: Remove the auto-return tag to prevent infinite loop
                if (droppedItem != null) {
                    droppedItem.getPersistentData().remove("BrassManReturning");
                    droppedItem.getPersistentData().remove("BrassManSetName");
                    droppedItem.getPersistentData().remove("BrassManOwner");
                }
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TargetSetName")) {
            this.targetSetName = tag.getString("TargetSetName");
        }
        this.scanTick = tag.getInt("ScanTick");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.targetSetName != null) {
            tag.putString("TargetSetName", this.targetSetName);
        }
        tag.putInt("ScanTick", this.scanTick);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Sentry armor is invulnerable
        return false;
    }
}
