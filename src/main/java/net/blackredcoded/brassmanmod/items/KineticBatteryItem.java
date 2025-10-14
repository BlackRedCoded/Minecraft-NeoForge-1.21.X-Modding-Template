package net.blackredcoded.brassmanmod.items;

import net.blackredcoded.brassmanmod.util.BatteryHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KineticBatteryItem extends Item {

    // Much larger base capacity - enough for small/medium setups
    public static final int BASE_MAX_SU = 128_000; // ~2000 ticks (100s) at 64 SU/tick

    public KineticBatteryItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        int stored = BatteryHelper.getBatteryCharge(stack);
        int max = BatteryHelper.getMaxBatteryCharge(stack);

        if (max == 0) {
            tooltip.add(Component.literal("Empty Battery").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Insert into Air Compressor to charge").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            int baseMax = BatteryHelper.getBaseBatteryCapacity(stack);
            int upgradeCount = BatteryHelper.getPowerCellUpgrades(stack);

            tooltip.add(Component.literal("Stored SU: " + String.format("%,d", stored) + " / " + String.format("%,d", max)).withStyle(ChatFormatting.AQUA));

            double percentage = (double) stored / max * 100.0;
            ChatFormatting percentColor = percentage > 75 ? ChatFormatting.GREEN : percentage > 25 ? ChatFormatting.YELLOW : ChatFormatting.RED;
            tooltip.add(Component.literal(String.format("Charge: %.1f%%", percentage)).withStyle(percentColor));

            tooltip.add(Component.literal(""));

            if (upgradeCount > 0) {
                int bonusCapacity = max - baseMax;
                tooltip.add(Component.literal("⚡ Power Cell Upgrades: " + upgradeCount + "/5 (+" + (upgradeCount * 10) + "%)")
                        .withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.literal("  Base: " + String.format("%,d", baseMax) + " SU")
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("  Bonus: +" + String.format("%,d", bonusCapacity) + " SU")
                        .withStyle(ChatFormatting.GOLD));
            } else {
                tooltip.add(Component.literal("⚡ No Power Cell Upgrades")
                        .withStyle(ChatFormatting.GRAY));
            }

            // NEW: Quick Charge section
            int quickChargeUpgrades = BatteryHelper.getQuickChargeUpgrades(stack);
            if (quickChargeUpgrades > 0) {
                float chargeRate = BatteryHelper.getChargeRateMultiplier(stack);
                tooltip.add(Component.literal("⚡ Quick Charge: " + quickChargeUpgrades + "/5 (+" + ((int)((chargeRate - 1.0f) * 100)) + "% speed)")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            } else {
                tooltip.add(Component.literal("⚡ No Quick Charge Upgrades")
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return BatteryHelper.isBatteryItem(stack);
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        int stored = BatteryHelper.getBatteryCharge(stack);
        int max = BatteryHelper.getMaxBatteryCharge(stack);
        if (max == 0) return 0;
        return Math.round(13.0f * stored / max);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        int stored = BatteryHelper.getBatteryCharge(stack);
        int max = BatteryHelper.getMaxBatteryCharge(stack);
        float ratio = max > 0 ? (float) stored / max : 0;

        // Color gradient: red (empty) -> yellow (half) -> green (full)
        if (ratio > 0.5f) {
            return 0x00FF00; // Green
        } else if (ratio > 0.25f) {
            return 0xFFFF00; // Yellow
        } else {
            return 0xFF0000; // Red
        }
    }
}
