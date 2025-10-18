package net.blackredcoded.brassmanmod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class ArmorStyleHelper {

    static LocalPlayer player = Minecraft.getInstance().player;
    public static final String BRASS = "brass";
    public static final String AQUA = "aqua";
    public static final String DARK_AQUA = "darkaqua";
    public static final String FLAMING = "flaming";
    public static final ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
    public static final ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
    public static final ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
    public static final ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

    public static String getArmorStyle(ItemStack stack) {
        if (stack.isEmpty()) return BRASS;
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return data.contains("ArmorStyle") ? data.getString("ArmorStyle") : BRASS;
    }

    public static boolean hasArmorStyle(Player player, ItemStack armorItem, String armorStyle) {
        ItemStack chestplate = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        String currentStyle = getArmorStyle(chestplate);
        return currentStyle.equalsIgnoreCase(armorStyle);
    }

    public static void setArmorStyle(ItemStack stack, String style) {
        if (stack.isEmpty()) return;
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        data.putString("ArmorStyle", style);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }
}
