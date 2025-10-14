package net.blackredcoded.brassmanmod.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class BatteryHelper {

    private static final String BATTERY_KEY = "BatteryCharge";
    private static final String MAX_BATTERY_KEY = "MaxBatteryCharge";
    private static final String BASE_MAX_KEY = "BaseMaxSU"; // NEW: Store base capacity
    private static final String POWER_CELL_KEY = "PowerCellUpgrades"; // NEW: Store upgrade count

    /**
     * Check if an item can be charged (has battery data)
     */
    public static boolean isBatteryItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        return tag.contains(MAX_BATTERY_KEY);
    }

    /**
     * Get current battery charge
     */
    public static int getBatteryCharge(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        return tag.getInt(BATTERY_KEY);
    }

    /**
     * Get maximum battery capacity (with upgrades applied)
     */
    public static int getMaxBatteryCharge(ItemStack stack) {
        if (!isBatteryItem(stack)) return 0;

        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();

        int baseMax = tag.getInt(BASE_MAX_KEY);
        if (baseMax == 0) {
            // Fallback for old batteries without base max stored
            return tag.getInt(MAX_BATTERY_KEY);
        }

        int powerCellUpgrades = tag.getInt(POWER_CELL_KEY);

        // +10% per upgrade
        float multiplier = 1.0f + (powerCellUpgrades * 0.1f);
        return Math.round(baseMax * multiplier);
    }

    /**
     * Get base battery capacity (without upgrades)
     */
    public static int getBaseBatteryCapacity(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        return tag.getInt(BASE_MAX_KEY);
    }

    /**
     * Get number of power cell upgrades
     */
    public static int getPowerCellUpgrades(ItemStack stack) {
        if (!isBatteryItem(stack)) return 0;
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        return tag.getInt(POWER_CELL_KEY);
    }

    /**
     * Set number of power cell upgrades (0-5)
     */
    public static void setPowerCellUpgrades(ItemStack stack, int count) {
        if (!isBatteryItem(stack)) return;

        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();

        tag.putInt(POWER_CELL_KEY, Math.max(0, Math.min(5, count)));

        // Recalculate max capacity
        int baseMax = tag.getInt(BASE_MAX_KEY);
        float multiplier = 1.0f + (count * 0.1f);
        int newMax = Math.round(baseMax * multiplier);
        tag.putInt(MAX_BATTERY_KEY, newMax);

        // Clamp current charge to new max
        int currentCharge = tag.getInt(BATTERY_KEY);
        if (currentCharge > newMax) {
            tag.putInt(BATTERY_KEY, newMax);
        }

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Set battery charge AND sync durability bar to match
     */
    public static void setBatteryCharge(ItemStack stack, int charge) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();

        int maxCharge = getMaxBatteryCharge(stack);
        if (maxCharge == 0) maxCharge = 100;

        int actualCharge = Math.max(0, Math.min(charge, maxCharge));
        tag.putInt(BATTERY_KEY, actualCharge);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        // Sync durability bar
        if (stack.isDamageableItem() && stack.getMaxDamage() > 0) {
            int maxDamage = stack.getMaxDamage();
            float batteryPercent = (float) actualCharge / maxCharge;
            int damage = Math.round(maxDamage * (1.0f - batteryPercent));
            stack.setDamageValue(damage);
        }
    }

    /**
     * Initialize battery on item (call when item is created)
     */
    public static void initBattery(ItemStack stack, int baseMaxCharge) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(BATTERY_KEY, 0); // Start empty
        tag.putInt(BASE_MAX_KEY, baseMaxCharge); // Store base capacity
        tag.putInt(MAX_BATTERY_KEY, baseMaxCharge); // Effective capacity (no upgrades initially)
        tag.putInt(POWER_CELL_KEY, 0); // No upgrades initially
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        if (stack.isDamageableItem()) {
            stack.setDamageValue(stack.getMaxDamage()); // Empty = full damage
        }
    }

    /**
     * Check if battery is full
     */
    public static boolean isBatteryFull(ItemStack stack) {
        return getBatteryCharge(stack) >= getMaxBatteryCharge(stack);
    }

    /**
     * Check if battery is empty
     */
    public static boolean isBatteryEmpty(ItemStack stack) {
        return getBatteryCharge(stack) <= 0;
    }

    /**
     * Drain battery charge
     */
    public static void drainBattery(ItemStack stack, int amount) {
        if (!isBatteryItem(stack)) return;

        int current = getBatteryCharge(stack);
        int newCharge = Math.max(0, current - amount);
        setBatteryCharge(stack, newCharge);
    }

    /**
     * Charge battery by amount
     */
    public static void chargeBattery(ItemStack stack, int amount) {
        int current = getBatteryCharge(stack);
        setBatteryCharge(stack, current + amount);
    }

    // Get number of quick charge upgrades
    public static int getQuickChargeUpgrades(ItemStack stack) {
        if (!isBatteryItem(stack)) return 0;
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        return tag.getInt("QuickChargeUpgrades");
    }

    // Set quick charge upgrades (0-5)
    public static void setQuickChargeUpgrades(ItemStack stack, int count) {
        if (!isBatteryItem(stack)) return;

        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        tag.putInt("QuickChargeUpgrades", Math.max(0, Math.min(5, count)));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    // Get charge rate multiplier based on upgrades
    // Each upgrade adds +20% charge rate: 1.0 -> 1.2 -> 1.4 -> 1.6 -> 1.8 -> 2.0
    public static float getChargeRateMultiplier(ItemStack stack) {
        int upgrades = getQuickChargeUpgrades(stack);
        return 1.0f + (upgrades * 0.2f);
    }
}
