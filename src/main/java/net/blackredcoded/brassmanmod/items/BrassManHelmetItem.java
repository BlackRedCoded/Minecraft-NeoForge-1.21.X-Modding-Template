package net.blackredcoded.brassmanmod.items;

import net.blackredcoded.brassmanmod.blockentity.BrassArmorStandBlockEntity;
import net.blackredcoded.brassmanmod.config.FlightConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.blackredcoded.brassmanmod.client.renderer.BrassArmorRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class BrassManHelmetItem extends ArmorItem {
    public BrassManHelmetItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.HELMET, properties.durability(Type.HELMET.getDurability(15)));
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        // Show set name if it exists
        String setName = BrassArmorStandBlockEntity.getSetName(stack);
        if (setName != null && !setName.isEmpty()) {
            tooltip.add(Component.literal("Set: " + setName).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player) {
            if (player.getItemBySlot(EquipmentSlot.HEAD).equals(stack)) {
                consumePowerForHUD(stack, player);
            }
        }
    }

    private void consumePowerForHUD(ItemStack helmet, Player player) {
        // Don't consume power if HUD is disabled
        if (!FlightConfig.isHudEnabled(player)) {
            return;
        }

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.getItem() instanceof BrassManChestplateItem brassChest) {
            int power = brassChest.power(chestplate);
            if (power > 0 && player.level().getGameTime() % 40 == 0) {
                brassChest.consumePower(chestplate, 1); // Uses efficiency multiplier
            }
        }
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return BrassArmorRenderer.getArmorTexture(stack, slot, innerModel);
    }

}
