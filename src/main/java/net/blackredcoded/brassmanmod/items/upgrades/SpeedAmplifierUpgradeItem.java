package net.blackredcoded.brassmanmod.items.upgrades;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import java.util.List;

public class SpeedAmplifierUpgradeItem extends UpgradeModuleItem {

    public SpeedAmplifierUpgradeItem(Properties properties) {
        super(properties, "Speed Amplifier Upgrade", ChatFormatting.LIGHT_PURPLE);
    }

    @Override
    protected void addUpgradeDescription(List<Component> tooltip) {
        tooltip.add(Component.literal("+25% Movement Speed").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Max 2 per boots").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public String getUpgradeType() {
        return "speed_amplifier";
    }

    @Override
    public int getMaxStacksPerArmor() {
        return 2; // +50% speed total
    }
}
