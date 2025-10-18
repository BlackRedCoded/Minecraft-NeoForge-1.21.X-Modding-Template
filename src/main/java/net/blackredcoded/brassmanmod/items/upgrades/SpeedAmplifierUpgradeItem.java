package net.blackredcoded.brassmanmod.items.upgrades;

import net.blackredcoded.brassmanmod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Set;

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

    @Override
    public Set<Item> getApplicableItems() {
        return Set.of(ModItems.BRASS_MAN_LEGGINGS.get());
    }
}
