package net.blackredcoded.brassmanmod.client;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.config.FlightConfig;
import net.blackredcoded.brassmanmod.items.BrassChestplateItem;
import net.blackredcoded.brassmanmod.network.ConsumeAirPacket;
import net.blackredcoded.brassmanmod.network.FallsavePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = BrassManMod.MOD_ID, value = Dist.CLIENT)
public class FlightHandler {
    private static boolean isFlying = false;
    private static int floatingTicks = 0;
    private static int airConsumeTicks = 0;
    private static boolean fallsaveTriggered = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !player.level().isClientSide) return;

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chestplate.getItem() instanceof BrassChestplateItem)) {
            stopFlying(player);
            return;
        }

        int airAmount = BrassChestplateItem.getAir(chestplate);
        FlightConfig.PlayerFlightData config = FlightConfig.get(player);

        // Always trigger fallsave when falling
        if (!player.onGround() && player.getDeltaMovement().y < -0.5) {
            if (!fallsaveTriggered && isFalling(player)) {
                PacketDistributor.sendToServer(new FallsavePacket());
                fallsaveTriggered = true;
                player.displayClientMessage(
                        Component.literal("JARVIS: Fall detected! Emergency protocols engaged").withStyle(ChatFormatting.RED),
                        true
                );
            }
        } else if (player.onGround()) {
            fallsaveTriggered = false;
        }

        if (airAmount <= 0) {
            stopFlying(player);
            return;
        }

        boolean spacePressed = mc.options.keyJump.isDown();
        boolean shiftPressed = mc.options.keyShift.isDown();
        boolean isInAir = player.level().getBlockState(player.blockPosition().below()).isAir();
        int speedPercent = config.flightSpeed;

        int ticksPerAir;
        if (speedPercent <= 10) {
            ticksPerAir = 50;
        } else if (speedPercent <= 20) {
            ticksPerAir = 40;
        } else if (speedPercent <= 30) {
            ticksPerAir = 30;
        } else if (speedPercent <= 40) {
            ticksPerAir = 25;
        } else if (speedPercent <= 50) {
            ticksPerAir = 20;
        } else if (speedPercent <= 60) {
            ticksPerAir = 15;
        } else if (speedPercent <= 70) {
            ticksPerAir = 10;
        } else if (speedPercent <= 80) {
            ticksPerAir = 7;
        } else if (speedPercent <= 90) {
            ticksPerAir = 4;
        } else {
            ticksPerAir = 2;
        }

        // FLIGHT: Space pressed + flight enabled
        if (spacePressed && airAmount > 0 && config.flightEnabled) {
            if (!isFlying) {
                startFlying(player);
            }

            player.fallDistance = 0;
            airConsumeTicks++;
            if (airConsumeTicks >= ticksPerAir) {
                PacketDistributor.sendToServer(new ConsumeAirPacket(1));
                airConsumeTicks = 0;
            }

            float speedMultiplier = speedPercent / 100.0f;
            Vec3 lookVector = player.getLookAngle();
            double baseSpeed = 0.225;
            double horizontalX = lookVector.x * baseSpeed * (0.5 + speedMultiplier);
            double verticalY = lookVector.y * baseSpeed * (0.5 + speedMultiplier) * 2.0;
            double horizontalZ = lookVector.z * baseSpeed * (0.5 + speedMultiplier);
            Vec3 movement = new Vec3(horizontalX, verticalY, horizontalZ);
            Vec3 currentMovement = player.getDeltaMovement();
            player.setDeltaMovement(
                    currentMovement.x * 0.8 + movement.x,
                    movement.y,
                    currentMovement.z * 0.8 + movement.z
            );
            player.setPose(Pose.STANDING);
            player.setSwimming(false);
            floatingTicks = 0;

            spawnParticles(player);
        }
        // HOVER: In air + hover enabled (independent of flight)
        else if (isInAir && airAmount > 0 && config.hoverEnabled) {
            floatingTicks++;
            player.fallDistance = 0;
            if (floatingTicks % 40 == 0) {
                PacketDistributor.sendToServer(new ConsumeAirPacket(1));
            }

            if (shiftPressed) {
                Vec3 currentMovement = player.getDeltaMovement();
                double sinkSpeed = -0.05 + (floatingTicks * -0.001);
                sinkSpeed = Math.max(sinkSpeed, -0.15);
                player.setDeltaMovement(currentMovement.multiply(0.9, 0.1, 0.9).add(0, sinkSpeed, 0));
            } else {
                player.setDeltaMovement(player.getDeltaMovement().multiply(0.8, 0.05, 0.8));
            }

            player.setPose(Pose.STANDING);
            player.setSwimming(false);
            spawnParticles(player);
        } else {
            stopFlying(player);
        }
    }

    private static void spawnParticles(LocalPlayer player) {
        Vec3 pos = player.position();
        double px = pos.x, py = pos.y, pz = pos.z;
        double[][] offsets = {
                { -0.3, 0.1,  0.0 },
                {  0.3, 0.1,  0.0 },
                { -0.5, 1.2,  0.2 },
                {  0.5, 1.2,  0.2 }
        };
        for (double[] off : offsets) {
            player.level().addParticle(
                    ParticleTypes.CLOUD,
                    px + off[0],
                    py + off[1],
                    pz + off[2],
                    0.0, 0.05, 0.0
            );
        }
    }

    private static boolean isFalling(LocalPlayer player) {
        BlockPos pos = player.blockPosition();
        for (int i = 1; i <= 5; i++) {
            if (!player.level().getBlockState(pos.below(i)).isAir()) {
                return false;
            }
        }
        return true;
    }

    private static void startFlying(LocalPlayer player) {
        if (!isFlying) {
            isFlying = true;
            airConsumeTicks = 0;
            player.setSwimming(false);
        }
    }

    private static void stopFlying(LocalPlayer player) {
        if (isFlying) {
            isFlying = false;
            floatingTicks = 0;
            airConsumeTicks = 0;
            player.setPose(Pose.STANDING);
            player.setSwimming(false);
        }
    }
}
