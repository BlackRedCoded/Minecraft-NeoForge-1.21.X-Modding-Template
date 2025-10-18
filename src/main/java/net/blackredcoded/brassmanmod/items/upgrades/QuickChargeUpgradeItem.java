package net.blackredcoded.brassmanmod.items.upgrades;

import net.blackredcoded.brassmanmod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Set;

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

    @Override
    public Set<Item> getApplicableItems() {
        return Set.of(
                ModItems.KINETIC_BATTERY.get(),
                ModItems.COMPRESSOR_NETWORK_TABLET.get());
    }
}
