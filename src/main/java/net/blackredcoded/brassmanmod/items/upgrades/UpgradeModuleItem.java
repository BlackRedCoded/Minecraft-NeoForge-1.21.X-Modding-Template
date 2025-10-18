package net.blackredcoded.brassmanmod.items.upgrades;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Set;

public abstract class UpgradeModuleItem extends Item {

    private final String upgradeName;
    private final ChatFormatting color;

    public UpgradeModuleItem(Properties properties, String upgradeName, ChatFormatting color) {
        super(properties.stacksTo(16).rarity(Rarity.UNCOMMON));
        this.upgradeName = upgradeName;
        this.color = color;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(upgradeName).withStyle(color, ChatFormatting.BOLD));
        addUpgradeDescription(tooltip);
        tooltip.add(Component.literal("Apply at Modification Station").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    protected abstract void addUpgradeDescription(List<Component> tooltip);

    public abstract String getUpgradeType();
    public abstract int getMaxStacksPerArmor();
    public abstract Set<Item> getApplicableItems();
}
