package net.blackredcoded.brassmanmod.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class ArmorStyleHelper {

    public static final String BRASS = "brass";
    public static final String AQUA = "aqua";
    public static final String DARK_AQUA = "darkaqua";
    public static final String FLAMING = "flaming";

    public static String getArmorStyle(ItemStack stack) {
        if (stack.isEmpty()) return BRASS;
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return data.contains("ArmorStyle") ? data.getString("ArmorStyle") : BRASS;
    }

    public static void setArmorStyle(ItemStack stack, String style) {
        if (stack.isEmpty()) return;
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        data.putString("ArmorStyle", style);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }
}
