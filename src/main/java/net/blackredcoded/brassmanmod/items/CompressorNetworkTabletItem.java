package net.blackredcoded.brassmanmod.items;

import net.blackredcoded.brassmanmod.util.BatteryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class CompressorNetworkTabletItem extends Item {

    public CompressorNetworkTabletItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Initialize battery if not present
        if (!BatteryHelper.isBatteryItem(stack)) {
            BatteryHelper.initBattery(stack, 100); // 100% max battery
        }

        // If shift-clicking, pass through (let block handle it)
        if (player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        // Check if tablet has battery
        if (BatteryHelper.isBatteryEmpty(stack)) {
            if (level.isClientSide) {
                player.displayClientMessage(Component.literal("Tablet battery depleted!"), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        // Check if tablet is bound to a terminal
        BlockPos terminalPos = getLinkedTerminal(stack);
        if (terminalPos == null) {
            if (level.isClientSide) {
                player.displayClientMessage(Component.literal("Tablet not linked! Right-Click a terminal to link."), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (level.isClientSide) {
            openTabletScreen(stack, terminalPos);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Let blocks handle all interactions
        return InteractionResult.PASS;
    }

    private void openTabletScreen(ItemStack tablet, BlockPos terminalPos) {
        net.minecraft.client.Minecraft.getInstance().setScreen(
                new net.blackredcoded.brassmanmod.client.screen.TabletTerminalScreen(tablet, terminalPos)
        );
    }

    public static BlockPos getLinkedTerminal(ItemStack tablet) {
        CustomData customData = tablet.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (tag.contains("LinkedTerminal")) {
            return BlockPos.of(tag.getLong("LinkedTerminal"));
        }
        return null;
    }

    public static void linkToTerminal(ItemStack tablet, BlockPos terminalPos) {
        CustomData data = tablet.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        tag.putLong("LinkedTerminal", terminalPos.asLong());
        tablet.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void unlinkFromTerminal(ItemStack tablet) {
        CustomData data = tablet.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        tag.remove("LinkedTerminal");
        tablet.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        // Initialize battery if not present
        if (!BatteryHelper.isBatteryItem(stack)) {
            BatteryHelper.initBattery(stack, 100);
        }

        int battery = BatteryHelper.getBatteryCharge(stack);
        int maxBattery = BatteryHelper.getMaxBatteryCharge(stack);

        // Yellow battery text
        tooltip.add(Component.literal("Battery: " + battery + "/" + maxBattery + "%")
                .withStyle(style -> style.withColor(0xFFFF00)));

        BlockPos terminalPos = getLinkedTerminal(stack);
        if (terminalPos != null) {
            // Dark green if linked
            tooltip.add(Component.literal("Linked to Terminal at: " +
                            terminalPos.getX() + ", " + terminalPos.getY() + ", " + terminalPos.getZ())
                    .withStyle(style -> style.withColor(0x00AA00)));
        } else {
            // Dark red if not linked
            tooltip.add(Component.literal("Not linked to any terminal")
                    .withStyle(style -> style.withColor(0xAA0000)));
            tooltip.add(Component.literal("Right-Click a terminal to link")
                    .withStyle(style -> style.withColor(0xAA0000)));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFFFF00; // Yellow battery bar
    }
}
