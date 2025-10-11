package net.blackredcoded.brassmanmod.client;

import net.blackredcoded.brassmanmod.network.DrainTabletBatteryPacket;
import net.blackredcoded.brassmanmod.util.BatteryHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class TabletBatteryManager {

    private static boolean isUsingTablet = false;
    private static int tickCounter = 0;

    public static void startTabletUsage(ItemStack tablet) {
        isUsingTablet = true;
        tickCounter = 0;
    }

    public static void stopTabletUsage() {
        isUsingTablet = false;
        tickCounter = 0;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!isUsingTablet) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen == null) {
            stopTabletUsage();
            return;
        }

        // Check if still holding a battery item
        ItemStack mainHand = mc.player.getMainHandItem();
        if (!BatteryHelper.isBatteryItem(mainHand)) {
            stopTabletUsage();
            return;
        }

        // Drain battery every 5 seconds (100 ticks)
        tickCounter++;
        if (tickCounter >= 100) {
            tickCounter = 0;

            // Check battery on client-side first
            if (BatteryHelper.isBatteryEmpty(mainHand)) {
                mc.player.displayClientMessage(Component.literal("Tablet battery depleted!"), true);
                mc.setScreen(null);
                stopTabletUsage();
                return;
            }

            // Send packet to server to drain battery
            DrainTabletBatteryPacket.send();
        }
    }

    public static boolean isTabletGuiOpen() {
        return isUsingTablet;
    }
}
