package net.blackredcoded.brassmanmod.items.upgrades;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class QuickChargeUpgradeItem extends UpgradeModuleItem {

    public QuickChargeUpgradeItem(Properties properties) {
        super(properties, "Quick Charge Upgrade", ChatFormatting.LIGHT_PURPLE);
    }

    @Override
    protected void addUpgradeDescription(List<Component> tooltip) {
        tooltip.add(Component.literal("Battery: +20% Charge Speed").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Max 5 per battery (up to 2x speed)").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public String getUpgradeType() {
        return "quick_charge";
    }

    @Override
    public int getMaxStacksPerArmor() {
        return 5;
    }
}
