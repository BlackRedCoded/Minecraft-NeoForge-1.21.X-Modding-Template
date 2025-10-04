package net.blackredcoded.brassmanmod.items.upgrades;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import java.util.List;

public class AirEfficiencyUpgradeItem extends UpgradeModuleItem {

    public AirEfficiencyUpgradeItem(Properties properties) {
        super(properties, "Air Efficiency Module", ChatFormatting.AQUA);
    }

    @Override
    protected void addUpgradeDescription(List<Component> tooltip) {
        tooltip.add(Component.literal("-10% Air Consumption").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("Max 5 per chestplate (50% reduction)").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public String getUpgradeType() {
        return "air_efficiency";
    }

    @Override
    public int getMaxStacksPerArmor() {
        return 5;
    }
}
