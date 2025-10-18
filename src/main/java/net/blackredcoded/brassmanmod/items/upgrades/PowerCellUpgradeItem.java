package net.blackredcoded.brassmanmod.items.upgrades;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

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
}