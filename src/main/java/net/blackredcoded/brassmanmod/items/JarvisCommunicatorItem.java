package net.blackredcoded.brassmanmod.items;

import net.blackredcoded.brassmanmod.registry.ModArmorMaterials;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class JarvisCommunicatorItem extends ArmorItem {

    public JarvisCommunicatorItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.HELMET, properties.durability(0)); // No durability, just wearable
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Enables JARVIS AI assistance").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Wear in head slot for access").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Commands, warnings, and flight control").withStyle(ChatFormatting.GRAY));
    }

    // Check if player has JARVIS access (helmet OR communicator in head slot)
    public static boolean hasJarvis(net.minecraft.world.entity.player.Player player) {
        ItemStack headItem = player.getItemBySlot(EquipmentSlot.HEAD);
        return headItem.getItem() instanceof JarvisCommunicatorItem ||
                headItem.getItem() instanceof BrassHelmetItem;
    }
}
