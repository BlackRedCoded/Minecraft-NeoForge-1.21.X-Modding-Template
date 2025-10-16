package net.blackredcoded.brassmanmod.items.upgrades;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class RemoteAssemblyUpgradeItem extends UpgradeModuleItem {

    public RemoteAssemblyUpgradeItem(Properties properties) {
        super(properties, "Remote Assembly Protocol", ChatFormatting.AQUA);
    }

    @Override
    protected void addUpgradeDescription(List<Component> tooltip) {
        tooltip.add(Component.literal("Enables suit calling capabilities").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("⭐⭐ Stage 1: Call from Armor Stands").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("⭐⭐⭐ Stage 2: Call from anywhere").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("Does not use upgrade slots!").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("Max 2 per chestplate").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public String getUpgradeType() {
        return "remote_assembly";
    }

    @Override
    public int getMaxStacksPerArmor() {
        return 2; // Can be applied twice (Stage 1 and Stage 2)
    }
}
