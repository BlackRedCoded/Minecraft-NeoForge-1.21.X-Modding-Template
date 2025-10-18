package net.blackredcoded.brassmanmod.items.upgrades;

import net.blackredcoded.brassmanmod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Set;

public class PowerCellUpgradeItem extends UpgradeModuleItem {

    public PowerCellUpgradeItem(Properties properties) {
        super(properties, "Power Cell Upgrade", ChatFormatting.YELLOW);
    }

    @Override
    protected void addUpgradeDescription(List<Component> tooltip) {
        tooltip.add(Component.literal("+10% Maximum Power Capacity").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Max 5 per item, can be applied to Chestplate & Battery").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public String getUpgradeType() {
        return "power_cell";
    }

    @Override
    public int getMaxStacksPerArmor() {
        return 5; // CHANGED: 4 -> 5
    }

    @Override
    public Set<Item> getApplicableItems() {
        return Set.of(
                ModItems.BRASS_MAN_CHESTPLATE.get(),
                ModItems.KINETIC_BATTERY.get());
    }
}