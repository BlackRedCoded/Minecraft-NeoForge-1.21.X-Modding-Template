package net.blackredcoded.brassmanmod.items.upgrades;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import java.util.List;

public class PowerEfficiencyUpgradeItem extends UpgradeModuleItem {

    public PowerEfficiencyUpgradeItem(Properties properties) {
        super(properties, "Power Efficiency Module", ChatFormatting.GOLD);
    }

    @Override
    protected void addUpgradeDescription(List<Component> tooltip) {
        tooltip.add(Component.literal("-10% Power Consumption").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Max 5 per chestplate (50% reduction)").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public String getUpgradeType() {
        return "power_efficiency";
    }

    @Override
    public int getMaxStacksPerArmor() {
        return 5;
    }
}
