package net.blackredcoded.brassmanmod.client;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.config.FlightConfig;
import net.blackredcoded.brassmanmod.items.BrassChestplateItem;
import net.blackredcoded.brassmanmod.items.BrassHelmetItem;
import net.blackredcoded.brassmanmod.network.ConsumeNightvisionPowerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = BrassManMod.MOD_ID, value = Dist.CLIENT)
public class JarvisNightvisionHandler {
    private static int powerConsumeTicks = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !player.level().isClientSide) return;

        // Check if player is wearing brass helmet
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(helmet.getItem() instanceof BrassHelmetItem)) {
            return;
        }

        // Check for chestplate with power
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chestplate.getItem() instanceof BrassChestplateItem)) {
            return;
        }

        int currentPower = BrassChestplateItem.getAir(chestplate); // Note: This seems wrong, should be getPower?
        FlightConfig.PlayerFlightData config = FlightConfig.get(player);

        // If nightvision is enabled and we have power
        if (config.nightvisionEnabled && currentPower > 0) {
            // Apply night vision effect
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    300, // 15 seconds duration
                    0,
                    false,
                    false,
                    true
            ));

            // Consume power every 40 ticks (2 seconds)
            powerConsumeTicks++;
            if (powerConsumeTicks >= 40) {
                PacketDistributor.sendToServer(new ConsumeNightvisionPowerPacket(1));
                powerConsumeTicks = 0;
            }
        } else {
            // If disabled or no power, remove the effect
            if (player.hasEffect(MobEffects.NIGHT_VISION)) {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
            powerConsumeTicks = 0;
        }
    }
}
