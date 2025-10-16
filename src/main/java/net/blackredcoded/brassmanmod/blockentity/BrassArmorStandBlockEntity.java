package net.blackredcoded.brassmanmod.blockentity;

import net.blackredcoded.brassmanmod.items.BrassManChestplateItem;
import net.blackredcoded.brassmanmod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BrassArmorStandBlockEntity extends BlockEntity {

    // Armor slots: 0=helmet, 1=chestplate, 2=leggings, 3=boots
    private NonNullList<ItemStack> armorSlots = NonNullList.withSize(4, ItemStack.EMPTY);
    private int airChargeProgress = 0; // For Create integration later

    public BrassArmorStandBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.BRASS_ARMOR_STAND.get(), pos, blockState);
    }

    // Armor management
    public ItemStack getArmor(int slot) {
        if (slot >= 0 && slot < 4) {
            return armorSlots.get(slot);
        }
        return ItemStack.EMPTY;
    }

    public void setArmor(int slot, ItemStack stack) {
        if (slot >= 0 && slot < 4) {
            armorSlots.set(slot, stack);
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    public boolean canEquipArmor(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem armorItem)) {
            return false;
        }
        int slot = getSlotForArmorType(armorItem.getType());
        return slot != -1 && getArmor(slot).isEmpty();
    }

    public int getSlotForArmorType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 0;
            case CHESTPLATE -> 1;
            case LEGGINGS -> 2;
            case BOOTS -> 3;
            default -> -1;
        };
    }

    public ItemStack removeArmor(int slot) {
        if (slot >= 0 && slot < 4) {
            ItemStack removed = armorSlots.get(slot);
            setArmor(slot, ItemStack.EMPTY);
            return removed;
        }
        return ItemStack.EMPTY;
    }

    // FIXED: Check if chestplate needs charging (respects upgrades)
    public boolean canCharge() {
        ItemStack chestplate = getArmor(1);
        if (chestplate.getItem() instanceof BrassManChestplateItem brassChest) {
            int currentAir = brassChest.air(chestplate);
            int maxAir = BrassManChestplateItem.getMaxAir(chestplate);
            return currentAir < maxAir;
        }
        return false;
    }

    // FIXED: Charge chestplate (respects upgraded max values)
    public void chargeChestplate(int amount) {
        ItemStack chestplate = getArmor(1);
        if (chestplate.getItem() instanceof BrassManChestplateItem brassChest) {
            int currentAir = brassChest.air(chestplate);
            int maxAir = BrassManChestplateItem.getMaxAir(chestplate);
            brassChest.setAir(chestplate, Math.min(currentAir + amount, maxAir));
            setChanged();
        }
    }

    // FIXED: Check if any armor needs charging or repair (respects upgrades)
    public boolean needsChargingOrRepair() {
        for (int i = 0; i < 4; i++) {
            ItemStack armor = getArmor(i);
            if (!armor.isEmpty()) {
                // Check if armor is damaged
                if (armor.isDamaged()) {
                    return true;
                }

                // Check if Brass armor needs charging (with upgraded max values)
                if (armor.getItem() instanceof BrassManChestplateItem brassChest) {
                    int currentAir = brassChest.air(armor);
                    int currentPower = brassChest.power(armor);
                    int maxAir = BrassManChestplateItem.getMaxAir(armor);
                    int maxPower = BrassManChestplateItem.getMaxPower(armor);

                    if (currentAir < maxAir || currentPower < maxPower) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // NBT Saving/Loading
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, armorSlots, registries);
        tag.putInt("AirChargeProgress", airChargeProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        armorSlots = NonNullList.withSize(4, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, armorSlots, registries);
        airChargeProgress = tag.getInt("AirChargeProgress");
    }

    // Client sync
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    // Apply set name and owner to all armor pieces
    public void applySetNameToArmor(String setName, UUID ownerUUID) {
        for (int i = 0; i < 4; i++) {
            ItemStack armor = armorSlots.get(i);
            if (!armor.isEmpty()) {
                // Get or create custom data (1.21.1 way)
                CustomData customData = armor.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag tag = customData.copyTag();

                // Add set name and owner
                tag.putString("SetName", setName);
                if (ownerUUID != null) {
                    tag.putUUID("SetOwner", ownerUUID);
                }

                // Apply back to item (1.21.1 way)
                armor.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }
        setChanged();
    }

    // Get set name from armor
    public static String getSetName(ItemStack armor) {
        CustomData customData = armor.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("SetName")) {
            return tag.getString("SetName");
        }
        return null;
    }

    // Get owner UUID from armor
    public static UUID getSetOwner(ItemStack armor) {
        CustomData customData = armor.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.hasUUID("SetOwner")) {
            return tag.getUUID("SetOwner");
        }
        return null;
    }

    public static String getSetUUID(ItemStack stack) {
        if (stack.isEmpty()) return null;
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (data.hasUUID("SetUUID")) {
            return data.getUUID("SetUUID").toString();
        }
        return null;
    }
}
