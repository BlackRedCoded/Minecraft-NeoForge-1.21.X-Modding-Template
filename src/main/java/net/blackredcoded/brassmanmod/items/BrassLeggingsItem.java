package net.blackredcoded.brassmanmod.items;

import net.blackredcoded.brassmanmod.registry.ModArmorMaterials;
import net.blackredcoded.brassmanmod.upgrade.ArmorUpgradeHelper;
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

public class BrassLeggingsItem extends ArmorItem {

    public BrassLeggingsItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.LEGGINGS, properties.durability(Type.LEGGINGS.getDurability(15)).rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Hydraulic Leg Enhancements").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltip.add(Component.literal("Adjustable speed & jump boost").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Use /jarvis speedboost & jumpboost").withStyle(ChatFormatting.YELLOW));

        // Display speed upgrades if present
        if (ArmorUpgradeHelper.hasUpgrades(stack)) {
            int speedAmp = ArmorUpgradeHelper.getUpgradeCount(stack, ArmorUpgradeHelper.SPEED_AMPLIFIER);
            if (speedAmp > 0) {
                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal("━━━ Installed Upgrades ━━━").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                tooltip.add(Component.literal("⚡ Speed Amplifiers: " + speedAmp + "/" + ArmorUpgradeHelper.MAX_SPEED_AMPLIFIERS
                        + " (+" + (speedAmp * 25) + "% speed)").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("━━━ Information ━━━").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.literal("Powered hydraulic systems enhance").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("jumping height and movement speed").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Upgrade at Modification Station").withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Hold Shift for details").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }
}
