package net.blackredcoded.brassmanmod.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class BatteryHelper {

    private static final String BATTERY_KEY = "BatteryCharge";
    private static final String MAX_BATTERY_KEY = "MaxBatteryCharge";

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
     * Get maximum battery capacity
     */
    public static int getMaxBatteryCharge(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        return tag.getInt(MAX_BATTERY_KEY);
    }

    /**
     * Set battery charge AND sync durability bar to match
     */
    public static void setBatteryCharge(ItemStack stack, int charge) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();

        int maxCharge = tag.getInt(MAX_BATTERY_KEY);
        if (maxCharge == 0) maxCharge = 100; // Default if not initialized

        int actualCharge = Math.max(0, Math.min(charge, maxCharge)); // Clamp to 0-max
        tag.putInt(BATTERY_KEY, actualCharge);

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        // Sync durability bar to battery percentage
        // 100% battery = 0 damage, 0% battery = max damage
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
    public static void initBattery(ItemStack stack, int maxCharge) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(BATTERY_KEY, maxCharge); // Start fully charged
        tag.putInt(MAX_BATTERY_KEY, maxCharge);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        // Set durability to 0 (full battery)
        if (stack.isDamageableItem()) {
            stack.setDamageValue(0);
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
     * Drain battery by amount
     */
    public static void drainBattery(ItemStack stack, int amount) {
        int current = getBatteryCharge(stack);
        setBatteryCharge(stack, Math.max(0, current - amount));
    }

    /**
     * Charge battery by amount
     */
    public static void chargeBattery(ItemStack stack, int amount) {
        int current = getBatteryCharge(stack);
        setBatteryCharge(stack, current + amount);
    }
}
