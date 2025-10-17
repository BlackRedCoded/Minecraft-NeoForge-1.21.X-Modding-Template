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
    private NonNullList<ItemStack> armorSlots = NonNullList.withSize(4, ItemStack.EMPTY);
    private int airChargeProgress = 0;

    public BrassArmorStandBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.BRASS_ARMOR_STAND.get(), pos, blockState);
    }

    public ItemStack getArmor(int slot) {
        if (slot >= 0 && slot < 4) {
            return armorSlots.get(slot);
        }
        return ItemStack.EMPTY;
    }

    public void setArmor(int slot, ItemStack stack) {
        if (slot >= 0 && slot < 4) {
            armorSlots.set(slot, stack);
            // Auto-assign UUID when armor is placed
            if (!stack.isEmpty()) {
                manageSetUUID(slot, stack);
            }
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    /**
     * Manages Set UUID assignment when armor is placed on stand
     * - If piece has no UUID, check if stand has a partial set
     * - If partial set exists and has room, assign existing UUID
     * - If set is full or no UUID exists, create new UUID
     */
    private void manageSetUUID(int placedSlot, ItemStack placedPiece) {
        String existingUUID = getSetUUID(placedPiece);

        // Find if there's an existing set UUID on the stand
        String standSetUUID = null;
        boolean[] slotsOccupied = {false, false, false, false};

        for (int i = 0; i < 4; i++) {
            ItemStack piece = armorSlots.get(i);
            if (!piece.isEmpty() && i != placedSlot) {
                String pieceUUID = getSetUUID(piece);
                if (pieceUUID != null) {
                    standSetUUID = pieceUUID;
                    // Load which slots are part of this set
                    boolean[] setSlots = getSetSlots(piece);
                    for (int j = 0; j < 4; j++) {
                        slotsOccupied[j] = slotsOccupied[j] || setSlots[j];
                    }
                }
            }
        }

        // If placed piece has no UUID
        if (existingUUID == null) {
            // Check if stand has a partial set with room
            if (standSetUUID != null && !slotsOccupied[placedSlot]) {
                // Join existing set
                setSetUUID(placedPiece, standSetUUID);
                // Update set slots
                slotsOccupied[placedSlot] = true;
                updateAllSetSlots(standSetUUID, slotsOccupied);
            } else {
                // Create new set
                String newUUID = UUID.randomUUID().toString();
                setSetUUID(placedPiece, newUUID);
                boolean[] newSetSlots = {false, false, false, false};
                newSetSlots[placedSlot] = true;
                setSetSlots(placedPiece, newSetSlots);
            }
        } else {
            // Piece already has UUID - update its set slots
            boolean[] pieceSetSlots = getSetSlots(placedPiece);
            pieceSetSlots[placedSlot] = true;
            setSetSlots(placedPiece, pieceSetSlots);
        }
    }

    /**
     * Updates all pieces with matching UUID to have the same set slot data
     */
    private void updateAllSetSlots(String setUUID, boolean[] slots) {
        for (int i = 0; i < 4; i++) {
            ItemStack piece = armorSlots.get(i);
            if (!piece.isEmpty() && setUUID.equals(getSetUUID(piece))) {
                setSetSlots(piece, slots);
            }
        }
    }

    public ItemStack removeArmor(int slot) {
        if (slot >= 0 && slot < 4) {
            ItemStack removed = armorSlots.get(slot);
            setArmor(slot, ItemStack.EMPTY);

            // Mark this slot as no longer part of the set
            if (!removed.isEmpty()) {
                boolean[] setSlots = getSetSlots(removed);
                setSlots[slot] = false;
                setSetSlots(removed, setSlots);

                // Update other pieces in the set
                String removedUUID = getSetUUID(removed);
                if (removedUUID != null) {
                    for (int i = 0; i < 4; i++) {
                        ItemStack piece = armorSlots.get(i);
                        if (!piece.isEmpty() && removedUUID.equals(getSetUUID(piece))) {
                            setSetSlots(piece, setSlots);
                        }
                    }
                }
            }

            return removed;
        }
        return ItemStack.EMPTY;
    }

    // Static NBT helper methods
    public static String getSetUUID(ItemStack stack) {
        if (stack.isEmpty()) return null;
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (data.contains("SetUUID")) {
            return data.getString("SetUUID");
        }
        return null;
    }

    public static void setSetUUID(ItemStack stack, String setUUID) {
        if (stack.isEmpty()) return;
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        data.putString("SetUUID", setUUID);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    public static boolean[] getSetSlots(ItemStack stack) {
        if (stack.isEmpty()) return new boolean[]{false, false, false, false};
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        boolean[] slots = new boolean[4];
        slots[0] = data.getBoolean("SetSlot0"); // Helmet
        slots[1] = data.getBoolean("SetSlot1"); // Chestplate
        slots[2] = data.getBoolean("SetSlot2"); // Leggings
        slots[3] = data.getBoolean("SetSlot3"); // Boots
        return slots;
    }

    public static void setSetSlots(ItemStack stack, boolean[] slots) {
        if (stack.isEmpty()) return;
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        data.putBoolean("SetSlot0", slots[0]);
        data.putBoolean("SetSlot1", slots[1]);
        data.putBoolean("SetSlot2", slots[2]);
        data.putBoolean("SetSlot3", slots[3]);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    public static String getSetName(ItemStack armor) {
        CustomData customData = armor.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("SetName")) {
            return tag.getString("SetName");
        }
        return null;
    }

    public static UUID getSetOwner(ItemStack armor) {
        CustomData customData = armor.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.hasUUID("SetOwner")) {
            return tag.getUUID("SetOwner");
        }
        return null;
    }

    public void applySetNameToArmor(String setName, UUID ownerUUID) {
        for (int i = 0; i < 4; i++) {
            ItemStack armor = armorSlots.get(i);
            if (!armor.isEmpty()) {
                CustomData customData = armor.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag tag = customData.copyTag();
                tag.putString("SetName", setName);
                if (ownerUUID != null) {
                    tag.putUUID("SetOwner", ownerUUID);
                }
                armor.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }
        setChanged();
    }

    // Rest of your existing methods (charging, NBT, etc.)
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

    public boolean canCharge() {
        ItemStack chestplate = getArmor(1);
        if (chestplate.getItem() instanceof BrassManChestplateItem brassChest) {
            int currentAir = brassChest.air(chestplate);
            int maxAir = BrassManChestplateItem.getMaxAir(chestplate);
            return currentAir < maxAir;
        }
        return false;
    }

    public void chargeChestplate(int amount) {
        ItemStack chestplate = getArmor(1);
        if (chestplate.getItem() instanceof BrassManChestplateItem brassChest) {
            int currentAir = brassChest.air(chestplate);
            int maxAir = BrassManChestplateItem.getMaxAir(chestplate);
            brassChest.setAir(chestplate, Math.min(currentAir + amount, maxAir));
            setChanged();
        }
    }

    public boolean needsChargingOrRepair() {
        for (int i = 0; i < 4; i++) {
            ItemStack armor = getArmor(i);
            if (!armor.isEmpty()) {
                if (armor.isDamaged()) {
                    return true;
                }
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
}
