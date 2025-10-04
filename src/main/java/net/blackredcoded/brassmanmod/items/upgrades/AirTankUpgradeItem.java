package net.blackredcoded.brassmanmod.items.upgrades;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AirTankUpgradeItem extends UpgradeModuleItem {

    public AirTankUpgradeItem(Properties properties) {
        super(properties, "Compressed Air Tank", ChatFormatting.AQUA);
    }

    @Override
    protected void addUpgradeDescription(List<Component> tooltip) {
        tooltip.add(Component.literal("+3000 Maximum Air").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("Max 4 per chestplate").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public String getUpgradeType() {
        return "air_tank";
    }

    @Override
    public int getMaxStacksPerArmor() {
        return 4; // +12000 air total
    }
}
