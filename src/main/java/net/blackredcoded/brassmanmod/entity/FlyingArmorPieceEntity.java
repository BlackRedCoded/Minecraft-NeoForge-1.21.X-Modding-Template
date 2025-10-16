package net.blackredcoded.brassmanmod.entity;

import net.blackredcoded.brassmanmod.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class FlyingArmorPieceEntity extends Entity {
    private static final EntityDataAccessor<ItemStack> ARMOR_PIECE =
            SynchedEntityData.defineId(FlyingArmorPieceEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<String> SLOT_NAME =
            SynchedEntityData.defineId(FlyingArmorPieceEntity.class, EntityDataSerializers.STRING);

    private UUID targetPlayerUUID;
    private BlockPos sourcePos;
    private EquipmentSlot equipmentSlot;
    private int lifeTicks = 0;
    private static final int MAX_LIFE_TICKS = 100;

    public FlyingArmorPieceEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public FlyingArmorPieceEntity(Level level, BlockPos sourcePos, Player targetPlayer,
                                  ItemStack armorPiece, EquipmentSlot slot) {
        this(ModEntityTypes.FLYING_ARMOR_PIECE.get(), level);
        this.sourcePos = sourcePos;
        this.targetPlayerUUID = targetPlayer.getUUID();
        this.equipmentSlot = slot;
        this.setPos(sourcePos.getX() + 0.5, sourcePos.getY() + 1.0, sourcePos.getZ() + 0.5);
        this.entityData.set(ARMOR_PIECE, armorPiece.copy());
        this.entityData.set(SLOT_NAME, slot.getName());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ARMOR_PIECE, ItemStack.EMPTY);
        builder.define(SLOT_NAME, "head");
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
        for (int i = 0; i < 2; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.3;
            double offsetY = (random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (random.nextDouble() - 0.5) * 0.3;
            level().addParticle(ParticleTypes.CLOUD,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 0, 0, 0);
        }
    }

    private void equipArmorToPlayer(Player player) {
        if (!(level() instanceof ServerLevel)) return;

        ItemStack armorPiece = this.entityData.get(ARMOR_PIECE);

        // Get the correct slot from stored name
        String slotName = this.entityData.get(SLOT_NAME);
        EquipmentSlot slot = EquipmentSlot.byName(slotName);

        ItemStack currentArmor = player.getItemBySlot(slot);
        if (!armorPiece.isEmpty()) {
            if (!currentArmor.isEmpty()) {
                // Try to add to inventory, drop if full
                if (!player.getInventory().add(currentArmor)) {
                    player.drop(currentArmor, false);
                }
            }
            player.setItemSlot(slot, armorPiece.copy());
        }

        // Spawn particles
        if (level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 10; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 0.5;
                double offsetY = random.nextDouble() * 1.0;
                double offsetZ = (random.nextDouble() - 0.5) * 0.5;
                serverLevel.sendParticles(ParticleTypes.FLASH,
                        player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ,
                        1, 0, 0, 0, 0);
            }
        }
    }

    public ItemStack getArmorPiece() {
        return this.entityData.get(ARMOR_PIECE);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("TargetPlayer")) {
            this.targetPlayerUUID = tag.getUUID("TargetPlayer");
        }
        if (tag.contains("SourcePos")) {
            this.sourcePos = BlockPos.of(tag.getLong("SourcePos"));
        }
        if (tag.contains("SlotName")) {
            this.equipmentSlot = EquipmentSlot.byName(tag.getString("SlotName"));
        }
        this.lifeTicks = tag.getInt("LifeTicks");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.targetPlayerUUID != null) {
            tag.putUUID("TargetPlayer", this.targetPlayerUUID);
        }
        if (this.sourcePos != null) {
            tag.putLong("SourcePos", this.sourcePos.asLong());
        }
        if (this.equipmentSlot != null) {
            tag.putString("SlotName", this.equipmentSlot.getName());
        }
        tag.putInt("LifeTicks", this.lifeTicks);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0;
    }
}
