package net.blackredcoded.brassmanmod.util;

import net.blackredcoded.brassmanmod.blockentity.BrassArmorStandBlockEntity;
import net.blackredcoded.brassmanmod.items.BrassManBootsItem;
import net.blackredcoded.brassmanmod.items.BrassManChestplateItem;
import net.blackredcoded.brassmanmod.items.BrassManHelmetItem;
import net.blackredcoded.brassmanmod.items.BrassManLeggingsItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public class ArmorUpgradeHelper {

    // Upgrade type constants
    public static final String POWER_CELL = "power_cell";
    public static final String AIR_TANK = "air_tank";
    public static final String SPEED_AMPLIFIER = "speed_amplifier";
    public static final String AIR_EFFICIENCY = "air_efficiency";
    public static final String POWER_EFFICIENCY = "power_efficiency";

    // Max stacks per upgrade type
    public static final int MAX_POWER_CELLS = 4;
    public static final int MAX_AIR_TANKS = 4;
    public static final int MAX_SPEED_AMPLIFIERS = 2;
    public static final int MAX_AIR_EFFICIENCY = 5;
    public static final int MAX_POWER_EFFICIENCY = 5;
    public static final int MAX_TOTAL_UPGRADES = 15;

    // Get upgrade count for specific type
    public static int getUpgradeCount(ItemStack stack, String upgradeType) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        CompoundTag upgrades = tag.getCompound("Upgrades");
        return upgrades.getInt(upgradeType);
    }

    // Add upgrade to armor piece
    public static boolean addUpgrade(ItemStack stack, String upgradeType, int maxAllowed) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        CompoundTag upgrades = tag.getCompound("Upgrades");
        int currentCount = upgrades.getInt(upgradeType);
        if (currentCount >= maxAllowed) {
            return false;
        }

        upgrades.putInt(upgradeType, currentCount + 1);
        tag.put("Upgrades", upgrades);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return true;
    }

    // Get total bonus from upgrades
    public static int getPowerBonus(ItemStack stack) {
        return getUpgradeCount(stack, POWER_CELL) * 250;
    }

    public static int getAirBonus(ItemStack stack) {
        return getUpgradeCount(stack, AIR_TANK) * 3000;
    }

    public static int getSpeedBonus(ItemStack stack) {
        return getUpgradeCount(stack, SPEED_AMPLIFIER) * 25;
    }

    // Efficiency multipliers (10% per upgrade, max 50% reduction)
    public static float getAirEfficiencyMultiplier(ItemStack chestplate) {
        int count = getUpgradeCount(chestplate, AIR_EFFICIENCY);
        return Math.max(0.5f, 1.0f - (count * 0.1f));
    }

    public static float getPowerEfficiencyMultiplier(ItemStack chestplate) {
        int count = getUpgradeCount(chestplate, POWER_EFFICIENCY);
        return Math.max(0.5f, 1.0f - (count * 0.1f));
    }

    // Check if armor piece has any upgrades
    public static boolean hasUpgrades(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        return tag.contains("Upgrades");
    }

    // Get max upgrade slots available
    public static int getTotalUpgradeCount(ItemStack stack) {
        if (!hasUpgrades(stack)) return 0;
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        CompoundTag upgrades = tag.getCompound("Upgrades");
        int total = 0;
        total += upgrades.getInt(POWER_CELL);
        total += upgrades.getInt(AIR_TANK);
        total += upgrades.getInt(SPEED_AMPLIFIER);
        total += upgrades.getInt(AIR_EFFICIENCY);
        total += upgrades.getInt(POWER_EFFICIENCY);
        return total;
    }

    // Get Remote Assembly level (0, 1, or 2)
    public static int getRemoteAssemblyLevel(ItemStack armorStack) {
        if (armorStack.isEmpty()) return 0;
        CustomData data = armorStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        return tag.getInt("RemoteAssemblyLevel");
    }

    // Set Remote Assembly level
    public static void setRemoteAssemblyLevel(ItemStack armorStack, int level) {
        if (armorStack.isEmpty()) return;
        CustomData data = armorStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        tag.putInt("RemoteAssemblyLevel", Math.min(level, 2));
        armorStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * When chestplate is upgraded, sync the upgrade level to all pieces with the same Set UUID
     */
    public static void syncUpgradeAcrossSet(ServerLevel level, ItemStack upgradedChestplate, int newLevel) {
        String setUUID = BrassArmorStandBlockEntity.getSetUUID(upgradedChestplate);
        if (setUUID == null || setUUID.isEmpty()) {
            System.out.println("WARNING: Cannot sync upgrade - chestplate has no Set UUID!");
            return;
        }

        UUID ownerUUID = BrassArmorStandBlockEntity.getSetOwner(upgradedChestplate);
        if (ownerUUID == null) {
            System.out.println("WARNING: Cannot sync upgrade - chestplate has no owner UUID!");
            return;
        }

        System.out.println("DEBUG: Syncing upgrade level " + newLevel + " across set UUID: " + setUUID);

        // Sync all matching pieces in the world
        int piecesSynced = 0;
        piecesSynced += syncPlayerInventories(level, setUUID, ownerUUID, newLevel);
        piecesSynced += syncArmorStands(level, setUUID, ownerUUID, newLevel);
        piecesSynced += syncContainers(level, setUUID, ownerUUID, newLevel);

        System.out.println("DEBUG: Synced " + piecesSynced + " pieces to level " + newLevel);
    }

    private static int syncPlayerInventories(ServerLevel level, String setUUID, UUID ownerUUID, int newLevel) {
        int count = 0;
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (!player.getUUID().equals(ownerUUID)) continue;

            // Check inventory
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (matchesSet(stack, setUUID, ownerUUID)) {
                    setRemoteAssemblyLevel(stack, newLevel);
                    player.getInventory().setItem(i, stack);
                    count++;
                }
            }

            // Check equipped armor - FIXED: Use correct enum values
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {  // FIXED: Changed from ARMOR to HUMANOID_ARMOR
                    ItemStack stack = player.getItemBySlot(slot);
                    if (matchesSet(stack, setUUID, ownerUUID)) {
                        setRemoteAssemblyLevel(stack, newLevel);
                        player.setItemSlot(slot, stack);
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static int syncArmorStands(ServerLevel level, String setUUID, UUID ownerUUID, int newLevel) {
        int count = 0;
        // Search in loaded chunks only - within a reasonable radius
        for (int x = -100; x <= 100; x++) {
            for (int z = -100; z <= 100; z++) {
                if (!level.hasChunk(x, z)) continue;

                var chunk = level.getChunk(x, z);
                for (var entry : chunk.getBlockEntities().entrySet()) {
                    BlockEntity be = entry.getValue();
                    if (be instanceof BrassArmorStandBlockEntity stand) {
                        for (int i = 0; i < 4; i++) {
                            ItemStack stack = stand.getArmor(i);
                            if (matchesSet(stack, setUUID, ownerUUID)) {
                                setRemoteAssemblyLevel(stack, newLevel);
                                stand.setArmor(i, stack);
                                count++;
                            }
                        }
                    }
                }
            }
        }
        return count;
    }

    private static int syncContainers(ServerLevel level, String setUUID, UUID ownerUUID, int newLevel) {
        int count = 0;
        // Search in loaded chunks only
        for (int x = -100; x <= 100; x++) {
            for (int z = -100; z <= 100; z++) {
                if (!level.hasChunk(x, z)) continue;

                var chunk = level.getChunk(x, z);
                for (var entry : chunk.getBlockEntities().entrySet()) {
                    BlockEntity be = entry.getValue();
                    if (be instanceof Container container && !(be instanceof BrassArmorStandBlockEntity)) {
                        for (int i = 0; i < container.getContainerSize(); i++) {
                            ItemStack stack = container.getItem(i);
                            if (matchesSet(stack, setUUID, ownerUUID)) {
                                setRemoteAssemblyLevel(stack, newLevel);
                                container.setItem(i, stack);
                                count++;
                            }
                        }
                    }
                }
            }
        }
        return count;
    }

    private static boolean matchesSet(ItemStack stack, String setUUID, UUID ownerUUID) {
        if (stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof BrassManHelmetItem ||
                stack.getItem() instanceof BrassManChestplateItem ||
                stack.getItem() instanceof BrassManLeggingsItem ||
                stack.getItem() instanceof BrassManBootsItem)) {
            return false;
        }

        String stackUUID = BrassArmorStandBlockEntity.getSetUUID(stack);
        UUID stackOwner = BrassArmorStandBlockEntity.getSetOwner(stack);

        return setUUID.equals(stackUUID) && ownerUUID.equals(stackOwner);
    }
}
