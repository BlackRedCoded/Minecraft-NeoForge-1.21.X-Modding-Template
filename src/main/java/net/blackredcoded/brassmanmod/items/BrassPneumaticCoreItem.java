package net.blackredcoded.brassmanmod.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;

public class BrassPneumaticCoreItem extends Item {
    public static final int MAX_AIR_PRESSURE = 300; // 5 minutes of air at 1 per second

    public BrassPneumaticCoreItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    // NO TOOLTIPS - This is just a crafting component now

    // Air pressure management methods using 1.21.1 data components
    public int getAirPressure(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().getInt("AirPressure");
    }

    public int getMaxAirPressure() {
        return MAX_AIR_PRESSURE;
    }

    public boolean consumeAir(ItemStack stack, int amount) {
        int currentAir = getAirPressure(stack);
        if (currentAir >= amount) {
            setAirPressure(stack, currentAir - amount);
            return true;
        }
        return false;
    }

    public void addAir(ItemStack stack, int amount) {
        int currentAir = getAirPressure(stack);
        int newAir = Math.min(MAX_AIR_PRESSURE, currentAir + amount);
        setAirPressure(stack, newAir);
    }

    private void setAirPressure(ItemStack stack, int pressure) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("AirPressure", pressure);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public float getAirPressureRatio(ItemStack stack) {
        return (float) getAirPressure(stack) / (float) getMaxAirPressure();
    }
}
