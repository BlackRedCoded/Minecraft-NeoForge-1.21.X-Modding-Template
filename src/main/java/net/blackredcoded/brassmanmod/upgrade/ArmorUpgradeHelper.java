package net.blackredcoded.brassmanmod.upgrade;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

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
            return false; // Max upgrades reached
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

    // NEW: Efficiency multipliers (10% per upgrade, max 50% reduction)
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

    public static final int MAX_TOTAL_UPGRADES = 15; // Increased to accommodate efficiency upgrades
}
