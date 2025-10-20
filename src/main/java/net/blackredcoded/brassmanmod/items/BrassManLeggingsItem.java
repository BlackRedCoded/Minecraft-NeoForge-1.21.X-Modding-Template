package net.blackredcoded.brassmanmod.items;

import net.blackredcoded.brassmanmod.blockentity.BrassArmorStandBlockEntity;
import net.blackredcoded.brassmanmod.client.renderer.BrassArmorRenderer;
import net.blackredcoded.brassmanmod.util.ArmorStyleHelper;
import net.blackredcoded.brassmanmod.util.ArmorUpgradeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BrassManLeggingsItem extends ArmorItem {

    public BrassManLeggingsItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.LEGGINGS, properties.durability(Type.LEGGINGS.getDurability(15)).rarity(Rarity.UNCOMMON));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (entity instanceof Player player && !level.isClientSide) {
            String armorStyle = ArmorStyleHelper.getArmorStyle(stack);

            // Flaming armor takes damage in water
            if (armorStyle.equals(ArmorStyleHelper.FLAMING)) {
                if (player.isInWater() || level.isRainingAt(player.blockPosition())) {
                    // Damage armor every second (20 ticks)
                    if (level.getGameTime() % 20 == 0) {
                        stack.hurtAndBreak(1, player, EquipmentSlot.CHEST);
                    }
                }
            }
        }
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // Show set name if it exists
        String setName = BrassArmorStandBlockEntity.getSetName(stack);
        if (setName != null && !setName.isEmpty()) {
            tooltip.add(Component.literal("Set: " + setName).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
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

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return BrassArmorRenderer.getArmorTexture(stack, slot, innerModel);
    }

}
