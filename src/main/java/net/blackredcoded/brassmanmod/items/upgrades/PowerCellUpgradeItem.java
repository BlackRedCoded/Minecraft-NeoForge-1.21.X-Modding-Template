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
        tooltip.add(Component.literal("+250 Maximum Power").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Max 4 per chestplate").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public String getUpgradeType() {
        return "power_cell";
    }

    @Override
    public int getMaxStacksPerArmor() {
        return 4; // +1000 power total
    }
}
