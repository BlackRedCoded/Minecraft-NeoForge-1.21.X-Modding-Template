package net.blackredcoded.brassmanmod.entity;

import net.blackredcoded.brassmanmod.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class FlyingSuitEntity extends Entity {
    private static final EntityDataAccessor<ItemStack> HELMET =
            SynchedEntityData.defineId(FlyingSuitEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> CHESTPLATE =
            SynchedEntityData.defineId(FlyingSuitEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> LEGGINGS =
            SynchedEntityData.defineId(FlyingSuitEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> BOOTS =
            SynchedEntityData.defineId(FlyingSuitEntity.class, EntityDataSerializers.ITEM_STACK);

    private UUID targetPlayerUUID;
    private BlockPos sourceArmorStandPos;
    private int lifeTicks = 0;
    private static final int MAX_LIFE_TICKS = 100;

    public FlyingSuitEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public FlyingSuitEntity(Level level, BlockPos armorStandPos, Player targetPlayer,
                            ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
        this(ModEntityTypes.FLYING_SUIT.get(), level);
        this.sourceArmorStandPos = armorStandPos;
        this.targetPlayerUUID = targetPlayer.getUUID();

        this.setPos(armorStandPos.getX() + 0.5, armorStandPos.getY() + 1.0, armorStandPos.getZ() + 0.5);

        this.entityData.set(HELMET, helmet.copy());
        this.entityData.set(CHESTPLATE, chestplate.copy());
        this.entityData.set(LEGGINGS, leggings.copy());
        this.entityData.set(BOOTS, boots.copy());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(HELMET, ItemStack.EMPTY);
        builder.define(CHESTPLATE, ItemStack.EMPTY);
        builder.define(LEGGINGS, ItemStack.EMPTY);
        builder.define(BOOTS, ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            spawnParticles();
            return;
        }

        lifeTicks++;

        if (lifeTicks > MAX_LIFE_TICKS) {
            this.discard();
            return;
        }

        Player targetPlayer = level().getPlayerByUUID(targetPlayerUUID);
        if (targetPlayer == null) {
            this.discard();
            return;
        }

        Vec3 targetPos = targetPlayer.position().add(0, 1, 0);
        Vec3 currentPos = this.position();
        Vec3 direction = targetPos.subtract(currentPos);
        double distance = direction.length();

        if (distance < 1.5) {
            equipArmorToPlayer(targetPlayer);
            this.discard();
            return;
        }

        double speed = Math.max(2.0, distance * 0.5);
        Vec3 velocity = direction.normalize().scale(speed);
        this.setDeltaMovement(velocity);
        this.setPos(currentPos.x + velocity.x, currentPos.y + velocity.y, currentPos.z + velocity.z);
    }

    private void spawnParticles() {
        Vec3 pos = this.position();
        for (int i = 0; i < 3; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.5;
            double offsetY = (random.nextDouble() - 0.5) * 0.5;
            double offsetZ = (random.nextDouble() - 0.5) * 0.5;
            level().addParticle(ParticleTypes.CLOUD,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 0, 0, 0);
        }
    }

    private void equipArmorToPlayer(Player player) {
        if (!(level() instanceof ServerLevel)) return;

        ItemStack helmet = this.entityData.get(HELMET);
        ItemStack chestplate = this.entityData.get(CHESTPLATE);
        ItemStack leggings = this.entityData.get(LEGGINGS);
        ItemStack boots = this.entityData.get(BOOTS);

        swapArmor(player, EquipmentSlot.HEAD, helmet);
        swapArmor(player, EquipmentSlot.CHEST, chestplate);
        swapArmor(player, EquipmentSlot.LEGS, leggings);
        swapArmor(player, EquipmentSlot.FEET, boots);

        if (level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 30; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 1.0;
                double offsetY = random.nextDouble() * 2.0;
                double offsetZ = (random.nextDouble() - 0.5) * 1.0;
                serverLevel.sendParticles(ParticleTypes.FLASH,
                        player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ,
                        1, 0, 0, 0, 0);
            }
        }
    }

    private void swapArmor(Player player, EquipmentSlot slot, ItemStack newArmor) {
        ItemStack currentArmor = player.getItemBySlot(slot);

        if (!newArmor.isEmpty()) {
            if (!currentArmor.isEmpty()) {
                if (!player.getInventory().add(currentArmor)) {
                    player.drop(currentArmor, false);
                }
            }
            player.setItemSlot(slot, newArmor.copy());
        }
    }

    public ItemStack getHelmet() { return this.entityData.get(HELMET); }
    public ItemStack getChestplate() { return this.entityData.get(CHESTPLATE); }
    public ItemStack getLeggings() { return this.entityData.get(LEGGINGS); }
    public ItemStack getBoots() { return this.entityData.get(BOOTS); }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("TargetPlayer")) {
            this.targetPlayerUUID = tag.getUUID("TargetPlayer");
        }
        if (tag.contains("SourcePos")) {
            this.sourceArmorStandPos = BlockPos.of(tag.getLong("SourcePos"));
        }
        this.lifeTicks = tag.getInt("LifeTicks");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.targetPlayerUUID != null) {
            tag.putUUID("TargetPlayer", this.targetPlayerUUID);
        }
        if (this.sourceArmorStandPos != null) {
            tag.putLong("SourcePos", this.sourceArmorStandPos.asLong());
        }
        tag.putInt("LifeTicks", this.lifeTicks);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0;
    }
}
