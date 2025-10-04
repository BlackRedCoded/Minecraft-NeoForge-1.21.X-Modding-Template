package net.blackredcoded.brassmanmod.util;

import net.blackredcoded.brassmanmod.items.BrassPneumaticCoreItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class AirUtils {

    // For now, check the player's inventory instead of Curios slots
    // We'll add Curios integration later once we can access the API properly
    public static boolean consumeAirFromPlayer(Player player, int amount) {
        // Check main inventory for the pneumatic core
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof BrassPneumaticCoreItem core) {
                return core.consumeAir(stack, amount);
            }
        }
        return false;
    }

    public static int getPlayerAirPressure(Player player) {
        // Check main inventory for the pneumatic core
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof BrassPneumaticCoreItem core) {
                return core.getAirPressure(stack);
            }
        }
        return 0;
    }

    public static boolean hasAirPressure(Player player, int minimumAmount) {
        return getPlayerAirPressure(player) >= minimumAmount;
    }
}
