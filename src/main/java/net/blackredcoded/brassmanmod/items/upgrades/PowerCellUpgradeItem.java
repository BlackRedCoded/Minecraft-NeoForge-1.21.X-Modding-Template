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
        tooltip.add(Component.literal("Chestplate: +10% Maximum Power").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Battery: +10% Maximum Capacity").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Max 5 per item").withStyle(ChatFormatting.GRAY));
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
