package net.blackredcoded.brassmanmod.items;

import net.blackredcoded.brassmanmod.registry.ModArmorMaterials;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BrassBootsItem extends ArmorItem {

    public BrassBootsItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.BOOTS, properties.durability(Type.BOOTS.getDurability(15)).rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Shock Absorbing Boots").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltip.add(Component.literal("Absorbs first 10 HP of fall damage").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Power Cost: 2 per HP absorbed").withStyle(ChatFormatting.YELLOW));

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.literal("Hydraulic shock absorbers convert").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("kinetic energy into heat using power").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("Hold Shift for details").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
