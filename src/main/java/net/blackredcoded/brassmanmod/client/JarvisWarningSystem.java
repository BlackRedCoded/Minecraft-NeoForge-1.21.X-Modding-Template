package net.blackredcoded.brassmanmod.client;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.items.BrassChestplateItem;
import net.blackredcoded.brassmanmod.items.JarvisCommunicatorItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;

@EventBusSubscriber(modid = BrassManMod.MOD_ID, value = Dist.CLIENT)
public class JarvisWarningSystem {

    private static int currentChestplateDur = 0;
    private static int currentLeggingsDur = 0;
    private static int currentBootsDur = 0;
    private static int currentHelmetDur = 0;
    private static int tickCounter = 0;
    private static boolean lowPowerWarned = false;
    private static boolean criticalPowerWarned = false;
    private static boolean lowAirWarned = false;
    private static boolean criticalAirWarned = false;
    private static boolean lowHealthWarned = false;
    private static boolean lowHungerWarned = false;
    private static boolean lowBreathWarned = false;
    private static boolean lowChestplateDurWarned = false;
    private static boolean lowLeggingsDurWarned = false;
    private static boolean lowBootsDurWarned = false;
    private static boolean lowHelmetDurWarned = false;
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        // Check for JARVIS communicator
        if (!JarvisCommunicatorItem.hasJarvis(player)) return;

        tickCounter++;

        if (tickCounter % 20 != 0) return;
        EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        currentBootsDur = boots.getMaxDamage() - boots.getDamageValue();
        currentLeggingsDur = leggings.getMaxDamage() - leggings.getDamageValue();
        currentChestplateDur = chestplate.getMaxDamage() - chestplate.getDamageValue();
        currentHelmetDur = helmet.getMaxDamage() - helmet.getDamageValue();
        if (currentChestplateDur <= chestplate.getMaxDamage() / 10) {
            if (!lowChestplateDurWarned) {
                sendJarvisMessage(player, "WARNING: Chestplate has low durability! (Chestplate Durability: " + currentChestplateDur + ")", ChatFormatting.DARK_RED);
                lowChestplateDurWarned = true;
            }
        }
        if (currentLeggingsDur <= leggings.getMaxDamage() / 10) {
            if (!lowLeggingsDurWarned) {
                sendJarvisMessage(player, "WARNING: Leggings has low durability! (Leggings  Durability: " + currentLeggingsDur + ")", ChatFormatting.DARK_RED);
                lowLeggingsDurWarned = true;
            }
        }
        if (currentBootsDur <= boots.getMaxDamage() / 10) {
            if (!lowBootsDurWarned) {
                sendJarvisMessage(player, "WARNING: Boots has low durability! (Boots  Durability: " + currentBootsDur + ")", ChatFormatting.DARK_RED);
                lowBootsDurWarned = true;
            }
        }
        if (currentHelmetDur <= helmet.getMaxDamage() / 10) {
            if (!lowHelmetDurWarned) {
                sendJarvisMessage(player, "WARNING: Helmet has low durability! (Helmet Durability: " + currentHelmetDur + ")", ChatFormatting.DARK_RED);
                lowHelmetDurWarned = true;
            }
        }
        if (chestplate.getItem() instanceof BrassChestplateItem brass) {
            int power = brass.power(chestplate);
            int air = brass.air(chestplate);

            if (power <= 0) {
                if (!criticalPowerWarned) {
                    sendJarvisMessage(player, "CRITICAL: Suit power depleted!", ChatFormatting.DARK_RED);
                    criticalPowerWarned = true;
                    lowPowerWarned = true;
                }
            } else if (power < 100) {
                if (!lowPowerWarned) {
                    sendJarvisMessage(player, "WARNING: Suit power critically low (" + power + "/" + BrassChestplateItem.MAX_POWER + ")", ChatFormatting.RED);
                    lowPowerWarned = true;
                }
            } else {
                lowPowerWarned = false;
                criticalPowerWarned = false;
            }

            if (air <= 0) {
                if (!criticalAirWarned) {
                    sendJarvisMessage(player, "CRITICAL: Air supply depleted!", ChatFormatting.DARK_RED);
                    criticalAirWarned = true;
                    lowAirWarned = true;
                }
            } else if (air < 1200) {
                if (!lowAirWarned) {
                    sendJarvisMessage(player, "WARNING: Air supply critically low (" + air + "/" + BrassChestplateItem.MAX_AIR + ")", ChatFormatting.RED);
                    lowAirWarned = true;
                }
            } else {
                lowAirWarned = false;
                criticalAirWarned = false;
            }
        }

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        if (health < maxHealth * 0.25f) {
            if (!lowHealthWarned) {
                sendJarvisMessage(player, "WARNING: Life signs critical - Health at " + (int)health + "/" + (int)maxHealth, ChatFormatting.RED);
                lowHealthWarned = true;
            }
        } else {
            lowHealthWarned = false;
        }

        int hunger = player.getFoodData().getFoodLevel();
        if (hunger < 5) {
            if (!lowHungerWarned) {
                sendJarvisMessage(player, "WARNING: Nutrition levels critically low", ChatFormatting.GOLD);
                lowHungerWarned = true;
            }
        } else {
            lowHungerWarned = false;
        }

        int breath = player.getAirSupply();
        int maxBreath = player.getMaxAirSupply();
        if (player.isUnderWater() && breath < maxBreath * 0.3f) {
            if (!lowBreathWarned) {
                sendJarvisMessage(player, "WARNING: Oxygen levels critical - Surface immediately", ChatFormatting.AQUA);
                lowBreathWarned = true;
            }
        } else {
            lowBreathWarned = false;
        }
    }

    private static void sendJarvisMessage(LocalPlayer player, String message, ChatFormatting color) {
        player.displayClientMessage(
                Component.literal("JARVIS: ").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                        .append(Component.literal(message).withStyle(color)),
                true
        );
    }
}
