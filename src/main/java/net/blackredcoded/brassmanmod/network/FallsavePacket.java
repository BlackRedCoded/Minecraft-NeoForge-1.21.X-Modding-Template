package net.blackredcoded.brassmanmod.network;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.blockentity.AirCompressorBlockEntity;
import net.blackredcoded.brassmanmod.blockentity.BrassArmorStandBlockEntity;
import net.blackredcoded.brassmanmod.config.FlightConfig;
import net.blackredcoded.brassmanmod.entity.FlyingSuitEntity;
import net.blackredcoded.brassmanmod.items.*;
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

            FlightConfig.PlayerFlightData config = FlightConfig.get(player);

            // Check if player has chestplate for flight/hover features
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chestplate.getItem() instanceof BrassChestplateItem brass) {
                // Enable flight if fallsaveFlight is true
                if (config.fallsaveFlight) {
                    FlightConfig.setFlightEnabled(player, true);
                }

                // Enable hover if fallsaveHover is true
                if (config.fallsaveHover) {
                    FlightConfig.setHoverEnabled(player, true);
                }
            }

            // Auto-call suit (works even without chestplate!)
            if (config.fallsaveCallSuit && hasJarvisAccess(player)) {
                callBestSuit(player);
            }
        });
    }

    private static boolean hasJarvisAccess(ServerPlayer player) {
        return JarvisCommunicatorItem.hasJarvis(player);
    }

    // ADDED: Brass suit validation
    private static boolean isBrassSuit(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
        // At minimum, must have brass chestplate to fly
        if (!(chestplate.getItem() instanceof BrassChestplateItem)) {
            return false;
        }

        // Check other pieces - they should be brass armor or empty
        if (!helmet.isEmpty() && !(helmet.getItem() instanceof BrassHelmetItem)) {
            return false;
        }
        if (!leggings.isEmpty() && !(leggings.getItem() instanceof BrassLeggingsItem)) {
            return false;
        }
        if (!boots.isEmpty() && !(boots.getItem() instanceof BrassBootsItem)) {
            return false;
        }

        return true;
    }

    private static void callBestSuit(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        // Check if player already has a chestplate with air
        ItemStack currentChestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (currentChestplate.getItem() instanceof BrassChestplateItem brass) {
            int currentAir = brass.air(currentChestplate);
            if (currentAir > 0) {
                player.sendSystemMessage(Component.literal("JARVIS: Current suit still has air (" + currentAir + "). No need for backup.")
                        .withStyle(ChatFormatting.YELLOW));
                return;
            }
        }

        // Get all compressors THIS PLAYER placed
        Set<BlockPos> playerCompressors = CompressorRegistry.getPlayerCompressors(player);

        if (playerCompressors.isEmpty()) {
            player.sendSystemMessage(Component.literal("JARVIS: No compressors found. Place some Air Compressors first!")
                    .withStyle(ChatFormatting.RED));
            return;
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

                // FIXED: Only accept brass suits!
                if (!isBrassSuit(helmet, chestplate, leggings, boots)) {
                    continue; // Skip non-brass suits
                }

                // Check if there's any armor at all
                boolean hasArmor = !helmet.isEmpty() || !chestplate.isEmpty() ||
                        !leggings.isEmpty() || !boots.isEmpty();

                if (hasArmor) {
                    // Calculate total charge
                    int totalCharge = 0;
                    if (chestplate.getItem() instanceof BrassChestplateItem brassChest) {
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
            player.sendSystemMessage(Component.literal("JARVIS: No brass suits available on your compressors!")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        // Find best suit: Most charged, if tie then closest
        SuitData bestSuit = availableSuits.stream()
                .max((a, b) -> {
                    int chargeCompare = Integer.compare(a.totalCharge, b.totalCharge);
                    if (chargeCompare != 0) return chargeCompare;
                    return Double.compare(b.distanceSquared, a.distanceSquared);
                })
                .orElse(null);

        if (bestSuit == null) return;

        // Get armor from stand
        ItemStack helmet = bestSuit.armorStand.getArmor(0);
        ItemStack chestplate = bestSuit.armorStand.getArmor(1);
        ItemStack leggings = bestSuit.armorStand.getArmor(2);
        ItemStack boots = bestSuit.armorStand.getArmor(3);

        // Spawn flying suit
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

        player.sendSystemMessage(Component.literal("JARVIS: Emergency brass suit deployed! (Charge: " + bestSuit.totalCharge + ")")
                .withStyle(ChatFormatting.GREEN));
    }

    private record SuitData(BlockPos compressorPos, BlockPos standPos, int totalCharge,
                            BrassArmorStandBlockEntity armorStand, double distanceSquared) {}
}
