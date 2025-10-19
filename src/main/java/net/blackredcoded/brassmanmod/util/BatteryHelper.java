package net.blackredcoded.brassmanmod.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class BatteryHelper {

    private static final String BATTERY_KEY = "BatteryCharge";
    private static final String MAX_BATTERY_KEY = "MaxBatteryCharge";
    private static final String BASE_MAX_KEY = "BaseMaxSU";
    private static final float chargeRateMultiplier = 2; // +200% Charging Rate per Quick Charge upgrade = +1.000% max

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
     * Now reads from ArmorUpgradeHelper instead of CustomData
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

        // CHANGED: Read from ArmorUpgradeHelper instead of CustomData
        int powerCellUpgrades = ArmorUpgradeHelper.getUpgradeCount(stack, "power_cell");

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
     * CHANGED: Now reads from ArmorUpgradeHelper
     */
    public static int getPowerCellUpgrades(ItemStack stack) {
        if (!isBatteryItem(stack)) return 0;
        return ArmorUpgradeHelper.getUpgradeCount(stack, "power_cell");
    }

    /**
     * DEPRECATED: Use Modification Station to add upgrades instead
     * Keeping for backwards compatibility with old code
     */
    @Deprecated
    public static void setPowerCellUpgrades(ItemStack stack, int count) {
        // This method is deprecated - upgrades should be added via Modification Station
        // which uses ArmorUpgradeHelper.addUpgrade()
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
     * PRESERVES existing data like upgrades!
     */
    public static void initBattery(ItemStack stack, int baseMaxCharge) {
        // Get existing data instead of creating new tag
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag(); // ✅ Copy existing data!

        tag.putInt(BATTERY_KEY, 0); // Start empty
        tag.putInt(BASE_MAX_KEY, baseMaxCharge); // Store base capacity
        tag.putInt(MAX_BATTERY_KEY, baseMaxCharge); // Effective capacity (no upgrades initially)

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

    /**
     * Get number of quick charge upgrades
     * CHANGED: Now reads from ArmorUpgradeHelper
     */
    public static int getQuickChargeUpgrades(ItemStack stack) {
        if (!isBatteryItem(stack)) return 0;
        return ArmorUpgradeHelper.getUpgradeCount(stack, "quick_charge");
    }

    /**
     * DEPRECATED: Use Modification Station to add upgrades instead
     */
    @Deprecated
    public static void setQuickChargeUpgrades(ItemStack stack, int count) {
        // This method is deprecated - upgrades should be added via Modification Station
        // which uses ArmorUpgradeHelper.addUpgrade()
    }

    /**
     * Get charge rate multiplier based on upgrades
     * Each upgrade adds +20% charge rate: 1.0 -> 1.2 -> 1.4 -> 1.6 -> 1.8 -> 2.0
     * CHANGED: Now reads from ArmorUpgradeHelper
     */
    public static float getChargeRateMultiplier(ItemStack stack) {
        int upgrades = getQuickChargeUpgrades(stack);
        return 1.0f + (upgrades * chargeRateMultiplier);
    }
}
