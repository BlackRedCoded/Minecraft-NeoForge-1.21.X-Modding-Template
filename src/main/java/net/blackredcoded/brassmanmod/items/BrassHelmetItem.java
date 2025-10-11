package net.blackredcoded.brassmanmod.items;

import net.blackredcoded.brassmanmod.config.FlightConfig;
import net.blackredcoded.brassmanmod.upgrade.ArmorUpgradeHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BrassHelmetItem extends ArmorItem {
    public BrassHelmetItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.HELMET, properties.durability(Type.HELMET.getDurability(15)));
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
        if (chestplate.getItem() instanceof BrassChestplateItem brassChest) {
            int power = brassChest.power(chestplate);
            if (power > 0 && player.level().getGameTime() % 40 == 0) {
                brassChest.consumePower(chestplate, 1); // Uses efficiency multiplier
            }
        }
    }

}
