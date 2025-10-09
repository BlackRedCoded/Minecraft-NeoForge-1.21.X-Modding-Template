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
    private static ClientTickEvent.Post event;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        FlightHandler.event = event;
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
        if (spacePressed && config.flightEnabled) {
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
                // FIXED: 2× faster base speed and acceleration, correct sign
                Vec3 currentMovement = player.getDeltaMovement();
                double baseSink = -0.6;                   // initial downward speed
                double accelPerTick = -0.008;             // acceleration per tick (more negative)
                double sinkSpeed = baseSink + (floatingTicks * accelPerTick);
                sinkSpeed = Math.max(sinkSpeed, -3.2);    // max sink speed cap (2× faster max of previous -1.6)
                player.setDeltaMovement(
                        currentMovement.x * 0.9,
                        sinkSpeed,
                        currentMovement.z * 0.9
                );
            } else {
                player.setDeltaMovement(
                        player.getDeltaMovement().x * 0.8,
                        player.getDeltaMovement().y * 0.05,
                        player.getDeltaMovement().z * 0.8
                );
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
        float yaw = (float) Math.toRadians(player.getYRot()); // Player's horizontal rotation

        // Relative offsets: [left-right, up-down, forward-back]
        double[][] relativeOffsets = {
                { -0.3, 0.1,  0.0 },  // Left arm/leg
                {  0.3, 0.1,  0.0 },  // Right arm/leg
                { -0.3, 1.2,  0.0 },  // Left shoulder
                {  0.3, 1.2,  0.0 }   // Right shoulder
        };

        for (double[] offset : relativeOffsets) {
            // Rotate offsets based on player yaw
            double rotatedX = offset[0] * Math.cos(yaw) - offset[2] * Math.sin(yaw);
            double rotatedZ = offset[0] * Math.sin(yaw) + offset[2] * Math.cos(yaw);

            player.level().addParticle(
                    ParticleTypes.CLOUD,
                    pos.x + rotatedX,
                    pos.y + offset[1],
                    pos.z + rotatedZ,
                    0.0, -0.05, 0.0  // Slight downward velocity
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

    public static ClientTickEvent.Post getEvent() {
        return event;
    }

    public static void setEvent(ClientTickEvent.Post event) {
        FlightHandler.event = event;
    }
}
