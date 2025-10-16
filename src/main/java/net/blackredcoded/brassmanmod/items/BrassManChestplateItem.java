package net.blackredcoded.brassmanmod.items;

import net.blackredcoded.brassmanmod.registry.ModArmorMaterials;
import net.blackredcoded.brassmanmod.util.ArmorUpgradeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BrassManChestplateItem extends ArmorItem {

    public static final int BASE_MAX_AIR = 12_000;
    public static final int BASE_MAX_POWER = 1_000;
    public static final int MAX_AIR = BASE_MAX_AIR;
    public static final int MAX_POWER = BASE_MAX_POWER;

    public BrassManChestplateItem(Properties props) {
        super(ModArmorMaterials.BRASS_MAN, Type.CHESTPLATE,
                props.durability(Type.CHESTPLATE.getDurability(15)).rarity(Rarity.UNCOMMON));
    }

    public static int getMaxAir(ItemStack stack) {
        return BASE_MAX_AIR + ArmorUpgradeHelper.getAirBonus(stack);
    }

    public static int getMaxPower(ItemStack stack) {
        return BASE_MAX_POWER + ArmorUpgradeHelper.getPowerBonus(stack);
    }

    public int air(ItemStack s) {
        CustomData cd = s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return cd.copyTag().getInt("BrassAir");
    }

    public void setAir(ItemStack s, int v) {
        CustomData cd = s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag t = cd.copyTag();
        int existingPower = t.getInt("BrassPower");
        t.putInt("BrassAir", Math.max(0, Math.min(v, getMaxAir(s))));
        t.putInt("BrassPower", existingPower);
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    public int power(ItemStack s) {
        CustomData cd = s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return cd.copyTag().getInt("BrassPower");
    }

    public void setPower(ItemStack s, int v) {
        CustomData cd = s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag t = cd.copyTag();
        int existingAir = t.getInt("BrassAir");
        t.putInt("BrassPower", Math.max(0, Math.min(v, getMaxPower(s))));
        t.putInt("BrassAir", existingAir);
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    public void setAirAndPower(ItemStack s, int air, int power) {
        CustomData cd = s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag t = cd.copyTag();
        t.putInt("BrassAir", Math.max(0, Math.min(air, getMaxAir(s))));
        t.putInt("BrassPower", Math.max(0, Math.min(power, getMaxPower(s))));
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    public boolean consume(ItemStack s, int baseAmount) {
        float multiplier = ArmorUpgradeHelper.getAirEfficiencyMultiplier(s);
        int actualCost = Math.max(1, (int)(baseAmount * multiplier));
        int cur = air(s);
        if (cur < actualCost) return false;
        setAir(s, cur - actualCost);
        return true;
    }

    public boolean consumePower(ItemStack s, int baseAmount) {
        float multiplier = ArmorUpgradeHelper.getPowerEfficiencyMultiplier(s);
        int actualCost = Math.max(1, (int)(baseAmount * multiplier));
        int cur = power(s);
        if (cur < actualCost) return false;
        setPower(s, cur - actualCost);
        return true;
    }

    public static int getAir(ItemStack stack) {
        CustomData cd = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return cd.copyTag().getInt("BrassAir");
    }

    public static void consumeAir(ItemStack stack, int amount) {
        CustomData cd = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = cd.copyTag();
        int currentAir = tag.getInt("BrassAir");
        int currentPower = tag.getInt("BrassPower");
        float airDebt = tag.getFloat("AirDebt");

        float multiplier = ArmorUpgradeHelper.getAirEfficiencyMultiplier(stack);
        float exactCost = amount * multiplier;

        airDebt += exactCost;

        int wholeUnitsToConsume = (int) airDebt;
        if (wholeUnitsToConsume > 0) {
            currentAir = Math.max(0, currentAir - wholeUnitsToConsume);
            airDebt -= wholeUnitsToConsume;
        }

        tag.putInt("BrassAir", currentAir);
        tag.putInt("BrassPower", currentPower);
        tag.putFloat("AirDebt", airDebt);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack s, @Nullable TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        int air = air(s);
        int power = power(s);
        int maxAir = getMaxAir(s);
        int maxPower = getMaxPower(s);
        ChatFormatting airColor = maxAir > BASE_MAX_AIR ? ChatFormatting.GREEN : ChatFormatting.AQUA;
        ChatFormatting powerColor = maxPower > BASE_MAX_POWER ? ChatFormatting.GREEN : ChatFormatting.YELLOW;

        tooltip.add(Component.literal("Air: %d / %d".formatted(air, maxAir)).withStyle(airColor));
        tooltip.add(Component.literal("Power: %d / %d".formatted(power, maxPower)).withStyle(powerColor));

// NEW: Show Remote Assembly Level (stars) - ALWAYS shown, starting at 1 star
        int remoteLevel = ArmorUpgradeHelper.getRemoteAssemblyLevel(s);
        int displayStars = remoteLevel + 1; // 0 -> 1 star, 1 -> 2 stars, 2 -> 3 stars

        tooltip.add(Component.literal(""));

        String stars;
        ChatFormatting starColor;
        String levelText;

        if (displayStars == 1) {
            stars = "⭐";
            starColor = ChatFormatting.WHITE;
            levelText = "Base Configuration";
        } else if (displayStars == 2) {
            stars = "⭐⭐";
            starColor = ChatFormatting.AQUA;
            levelText = "Remote Assembly (MK 7)";
        } else { // 3 stars
            stars = "⭐⭐⭐";
            starColor = ChatFormatting.GOLD;
            levelText = "Field Assembly (MK 42)";
        }

        tooltip.add(Component.literal(stars + " " + levelText).withStyle(starColor, ChatFormatting.BOLD));

        if (displayStars == 1) {
            tooltip.add(Component.literal("Manual suit equipping only").withStyle(ChatFormatting.GRAY));
        } else if (displayStars == 2) {
            tooltip.add(Component.literal("Can call suit from Armor Stands").withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.literal("Can call suit from anywhere!").withStyle(ChatFormatting.GOLD));
        }

        if (ArmorUpgradeHelper.hasUpgrades(s)) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("━━━ Installed Upgrades ━━━").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

            int powerCells = ArmorUpgradeHelper.getUpgradeCount(s, ArmorUpgradeHelper.POWER_CELL);
            if (powerCells > 0) {
                int powerBonus = ArmorUpgradeHelper.getPowerBonus(s);
                tooltip.add(Component.literal("⚡ Power Cells: " + powerCells + "/" + ArmorUpgradeHelper.MAX_POWER_CELLS
                        + " (+" + (powerCells * 10) + "% = +" + powerBonus + " power)").withStyle(ChatFormatting.YELLOW));
            }

            int airTanks = ArmorUpgradeHelper.getUpgradeCount(s, ArmorUpgradeHelper.AIR_TANK);
            if (airTanks > 0) {
                tooltip.add(Component.literal("🌀 Air Tanks: " + airTanks + "/" + ArmorUpgradeHelper.MAX_AIR_TANKS
                        + " (+" + (airTanks * 3000) + " air)").withStyle(ChatFormatting.AQUA));
            }

            int airEff = ArmorUpgradeHelper.getUpgradeCount(s, ArmorUpgradeHelper.AIR_EFFICIENCY);
            if (airEff > 0) {
                tooltip.add(Component.literal("💨 Air Efficiency: " + airEff + "/" + ArmorUpgradeHelper.MAX_AIR_EFFICIENCY
                        + " (-" + (airEff * 10) + "% air use)").withStyle(ChatFormatting.AQUA));
            }

            int powerEff = ArmorUpgradeHelper.getUpgradeCount(s, ArmorUpgradeHelper.POWER_EFFICIENCY);
            if (powerEff > 0) {
                tooltip.add(Component.literal("⚙ Power Efficiency: " + powerEff + "/" + ArmorUpgradeHelper.MAX_POWER_EFFICIENCY
                        + " (-" + (powerEff * 10) + "% power use)").withStyle(ChatFormatting.GOLD));
            }

            int total = ArmorUpgradeHelper.getTotalUpgradeCount(s);
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Upgrade Slots: " + total + "/" + ArmorUpgradeHelper.MAX_TOTAL_UPGRADES)
                    .withStyle(total >= ArmorUpgradeHelper.MAX_TOTAL_UPGRADES ? ChatFormatting.RED : ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("⬆ No Upgrades Installed").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("━━━ Information ━━━").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.literal("Wear to power Brass Man suit").withStyle(ChatFormatting.DARK_RED));
            tooltip.add(Component.literal("Recharge at Brass Armor Stand").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal("Upgrade at Modification Station").withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Hold Shift for more info").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }
}
