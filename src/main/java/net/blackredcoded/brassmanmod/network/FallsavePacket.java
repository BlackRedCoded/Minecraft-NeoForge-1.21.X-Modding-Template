package net.blackredcoded.brassmanmod.network;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.blockentity.AirCompressorBlockEntity;
import net.blackredcoded.brassmanmod.blockentity.BrassArmorStandBlockEntity;
import net.blackredcoded.brassmanmod.config.FlightConfig;
import net.blackredcoded.brassmanmod.entity.FlyingSuitEntity;
import net.blackredcoded.brassmanmod.items.*;
import net.blackredcoded.brassmanmod.util.ArmorUpgradeHelper;
import net.blackredcoded.brassmanmod.util.CompressorRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record FallsavePacket() implements CustomPacketPayload {

    public static final Type<FallsavePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, "fallsave"));

    public static final StreamCodec<FriendlyByteBuf, FallsavePacket> STREAM_CODEC =
            StreamCodec.unit(new FallsavePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FallsavePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            if (player.getPersistentData().getBoolean("BrassManIced")) return; // Don't execute fallsave if suit is iced

            FlightConfig.PlayerFlightData config = FlightConfig.get(player);

            // Track if fallsave actually did something
            boolean fallsaveActivated = false;

            // Check if player has chestplate currently
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            boolean hasChestplate = chestplate.getItem() instanceof BrassManChestplateItem;

            // If player already has chestplate with air, activate flight/hover immediately
            if (hasChestplate) {
                BrassManChestplateItem brass = (BrassManChestplateItem) chestplate.getItem();
                int currentAir = brass.air(chestplate);
                if (currentAir > 0) {
                    // Enable flight if fallsaveFlight is true
                    if (config.fallsaveFlight) {
                        FlightConfig.setFlightEnabled(player, true);
                        fallsaveActivated = true; // Flight was enabled!
                    }

                    // Enable hover if fallsaveHover is true
                    if (config.fallsaveHover) {
                        FlightConfig.setHoverEnabled(player, true);
                        fallsaveActivated = true; // Hover was enabled!
                    }
                }
            }

            // Auto-call suit (works even without chestplate!)
            if (config.fallsaveCallSuit && hasJarvisAccess(player)) {
                // Pass config settings to callBestSuit so it can enable flight/hover when suit arrives
                boolean suitCalled = callBestSuit(player, config.fallsaveFlight, config.fallsaveHover);
                if (suitCalled) {
                    fallsaveActivated = true; // Suit was successfully called!
                }
            }

            // ONLY award achievement if fallsave actually did something
            if (fallsaveActivated) {
                player.getAdvancements().award(
                        player.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("brassmanmod", "brass_man/emergency_landing")),
                        "fallsave"
                );
            }
        });
    }

    private static boolean hasJarvisAccess(ServerPlayer player) {
        return JarvisCommunicatorItem.hasJarvis(player);
    }

    // ADDED: Brass suit validation
    private static boolean isBrassSuit(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
        // At minimum, must have brass chestplate to fly
        if (!(chestplate.getItem() instanceof BrassManChestplateItem)) {
            return false;
        }

        // Check other pieces - they should be brass armor or empty
        if (!helmet.isEmpty() && !(helmet.getItem() instanceof BrassManHelmetItem)) {
            return false;
        }

        if (!leggings.isEmpty() && !(leggings.getItem() instanceof BrassManLeggingsItem)) {
            return false;
        }

        return boots.isEmpty() || boots.getItem() instanceof BrassManBootsItem;
    }

    private static boolean callBestSuit(ServerPlayer player, boolean enableFlight, boolean enableHover) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return false;

        // Check if player already has a chestplate with air
        ItemStack currentChestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (currentChestplate.getItem() instanceof BrassManChestplateItem brass) {
            int currentAir = brass.air(currentChestplate);
            if (currentAir > 0) {
                player.sendSystemMessage(Component.literal("JARVIS: Current suit still has air (" + currentAir + "). No need for backup.")
                        .withStyle(ChatFormatting.YELLOW));
                return false;
            }
        }

        // Get all compressors THIS PLAYER placed
        Set<BlockPos> playerCompressors = CompressorRegistry.getPlayerCompressors(player);
        if (playerCompressors.isEmpty()) {
            player.sendSystemMessage(Component.literal("JARVIS: No compressors found. Place some Air Compressors first!")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        List<SuitData> availableSuits = new ArrayList<>();
        BlockPos playerPos = player.blockPosition();

        // Check each of the player's compressors
        for (BlockPos compressorPos : playerCompressors) {
            // Verify compressor still exists
            BlockEntity be = serverLevel.getBlockEntity(compressorPos);
            if (!(be instanceof AirCompressorBlockEntity)) continue;

            // Check armor stand above compressor
            BlockPos standPos = compressorPos.above();
            BlockEntity standBE = serverLevel.getBlockEntity(standPos);
            if (standBE instanceof BrassArmorStandBlockEntity armorStand) {
                ItemStack helmet = armorStand.getArmor(0);
                ItemStack chestplate = armorStand.getArmor(1);
                ItemStack leggings = armorStand.getArmor(2);
                ItemStack boots = armorStand.getArmor(3);

                // Only accept brass suits!
                if (!isBrassSuit(helmet, chestplate, leggings, boots)) {
                    continue;
                }

                // NEW: Check Remote Assembly Level (must be Stage 1 or higher)
                int remoteLevel = ArmorUpgradeHelper.getRemoteAssemblyLevel(chestplate);
                if (remoteLevel < 1) {
                    continue; // Skip suits without Remote Assembly upgrade
                }

                // Check if there's any armor at all
                boolean hasArmor = !helmet.isEmpty() || !chestplate.isEmpty() ||
                        !leggings.isEmpty() || !boots.isEmpty();
                if (hasArmor) {
                    // Calculate total charge
                    int totalCharge = 0;
                    if (chestplate.getItem() instanceof BrassManChestplateItem brassChest) {
                        totalCharge = brassChest.air(chestplate) + brassChest.power(chestplate);
                    }

                    availableSuits.add(new SuitData(
                            compressorPos,
                            standPos,
                            totalCharge,
                            armorStand,
                            playerPos.distSqr(compressorPos)
                    ));
                }
            }
        }

        if (availableSuits.isEmpty()) {
            player.sendSystemMessage(Component.literal("JARVIS: No remotely callable suits available! (Suits need Remote Assembly Protocol ⭐⭐)")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        // Find best suit: Most charged, if tie then closest
        SuitData bestSuit = availableSuits.stream()
                .max((a, b) -> {
                    int chargeCompare = Integer.compare(a.totalCharge, b.totalCharge);
                    if (chargeCompare != 0) return chargeCompare;
                    return Double.compare(b.distanceSquared, a.distanceSquared);
                })
                .orElse(null);

        if (bestSuit == null) return false;

        // Get armor from stand
        ItemStack helmet = bestSuit.armorStand.getArmor(0);
        ItemStack chestplate = bestSuit.armorStand.getArmor(1);
        ItemStack leggings = bestSuit.armorStand.getArmor(2);
        ItemStack boots = bestSuit.armorStand.getArmor(3);

    // Spawn flying suit - Now with smart positioning!
        FlyingSuitEntity flyingSuit = new FlyingSuitEntity(
                serverLevel, bestSuit.standPos, player,
                helmet, chestplate, leggings, boots
        );
        serverLevel.addFreshEntity(flyingSuit);

    // Clear armor from stand
        bestSuit.armorStand.setArmor(0, ItemStack.EMPTY);
        bestSuit.armorStand.setArmor(1, ItemStack.EMPTY);
        bestSuit.armorStand.setArmor(2, ItemStack.EMPTY);
        bestSuit.armorStand.setArmor(3, ItemStack.EMPTY);

    // Enable flight/hover IMMEDIATELY
        if (enableFlight) {
            FlightConfig.setFlightEnabled(player, true);
            player.sendSystemMessage(Component.literal("JARVIS: Flight enabled.")
                    .withStyle(ChatFormatting.AQUA));
        }
        if (enableHover) {
            FlightConfig.setHoverEnabled(player, true);
            player.sendSystemMessage(Component.literal("JARVIS: Hover enabled.")
                    .withStyle(ChatFormatting.AQUA));
        }

    // Calculate distance to give player feedback
        double distance = Math.sqrt(bestSuit.distanceSquared);
        String distanceInfo = distance > 128 ? " (Spawning nearby)" : " (En route from base)";

        player.sendSystemMessage(Component.literal("JARVIS: Emergency brass suit deployed!" + distanceInfo + " (Charge: " + bestSuit.totalCharge + ")")
                .withStyle(ChatFormatting.GREEN));
        return true; // SUCCESS!

    }

    private record SuitData(BlockPos compressorPos, BlockPos standPos, int totalCharge,
                            BrassArmorStandBlockEntity armorStand, double distanceSquared) {}
}
