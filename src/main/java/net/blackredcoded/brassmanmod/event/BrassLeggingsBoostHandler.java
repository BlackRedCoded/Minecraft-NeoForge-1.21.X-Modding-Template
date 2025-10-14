package net.blackredcoded.brassmanmod.event;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.config.FlightConfig;
import net.blackredcoded.brassmanmod.items.BrassManChestplateItem;
import net.blackredcoded.brassmanmod.items.BrassManLeggingsItem;
import net.blackredcoded.brassmanmod.items.JarvisCommunicatorItem;
import net.blackredcoded.brassmanmod.util.ArmorUpgradeHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = BrassManMod.MOD_ID)
public class BrassLeggingsBoostHandler {
    private static int powerConsumeTicks = 0;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!JarvisCommunicatorItem.hasJarvis(player)) return;

        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        if (!(leggings.getItem() instanceof BrassManLeggingsItem)) return;

        ItemStack chestplateStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chestplateStack.getItem() instanceof BrassManChestplateItem brassChest)) return;

        int currentPower = brassChest.power(chestplateStack);
        if (currentPower <= 0) {
            player.removeEffect(MobEffects.MOVEMENT_SPEED);
            player.removeEffect(MobEffects.JUMP);
            return;
        }

        int upgradeCount = ArmorUpgradeHelper.getUpgradeCount(leggings, ArmorUpgradeHelper.SPEED_AMPLIFIER);
        FlightConfig.PlayerFlightData cfg = FlightConfig.get(player);
        int jarvisSpeed = cfg.speedBoost;
        int jumpBoost = cfg.jumpBoost;

        if (jarvisSpeed == 0 && jumpBoost == 0) {
            player.removeEffect(MobEffects.MOVEMENT_SPEED);
            player.removeEffect(MobEffects.JUMP);
            return;
        }

        int baseSpeedLevel = jarvisSpeed > 0 ? (int) Math.ceil(jarvisSpeed / 25.0f) - 1 : -1;
        int finalSpeedLevel = Math.max(0, baseSpeedLevel + upgradeCount);

        if (finalSpeedLevel >= 0) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, finalSpeedLevel, false, false, false));
        }

        if (jumpBoost > 0) {
            int jumpLevel = (int) Math.ceil(jumpBoost / 25.0f) - 1;
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, Math.max(0, jumpLevel), false, false, false));
        }

        powerConsumeTicks++;
        if (powerConsumeTicks >= 40) {
            float jarvisBoostRatio = (jarvisSpeed + jumpBoost) / 200.0f;
            int baseCost = Math.max(1, (int) Math.ceil(jarvisBoostRatio));
            brassChest.consumePower(chestplateStack, baseCost); // Uses efficiency multiplier
            powerConsumeTicks = 0;
        }
    }
}
