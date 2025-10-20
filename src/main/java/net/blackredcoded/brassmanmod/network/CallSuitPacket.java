package net.blackredcoded.brassmanmod.network;

import io.netty.buffer.ByteBuf;
import net.blackredcoded.brassmanmod.blockentity.AirCompressorBlockEntity;
import net.blackredcoded.brassmanmod.blockentity.BrassArmorStandBlockEntity;
import net.blackredcoded.brassmanmod.entity.FlyingSuitEntity;
import net.blackredcoded.brassmanmod.items.*;
import net.blackredcoded.brassmanmod.util.ArmorUpgradeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CallSuitPacket(BlockPos compressorPos) implements CustomPacketPayload {

    public static final Type<CallSuitPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("brassmanmod", "call_suit"));

    public static final StreamCodec<ByteBuf, CallSuitPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            CallSuitPacket::compressorPos,
            CallSuitPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
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

    public static void handle(CallSuitPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            BlockEntity be = player.level().getBlockEntity(packet.compressorPos);
            if (!(be instanceof AirCompressorBlockEntity compressor)) {
                player.displayClientMessage(Component.literal("Error: Compressor not found!"), true);
                return;
            }

            // Get armor stand above compressor
            BlockPos standPos = packet.compressorPos.above();
            BlockEntity standBE = player.level().getBlockEntity(standPos);
            if (!(standBE instanceof BrassArmorStandBlockEntity armorStand)) {
                player.displayClientMessage(Component.literal("Error: No armor stand found!"), true);
                return;
            }

            // Get armor from stand
            ItemStack helmet = armorStand.getArmor(0);
            ItemStack chestplate = armorStand.getArmor(1);
            ItemStack leggings = armorStand.getArmor(2);
            ItemStack boots = armorStand.getArmor(3);

            // Check if any armor exists
            if (helmet.isEmpty() && chestplate.isEmpty() && leggings.isEmpty() && boots.isEmpty()) {
                player.displayClientMessage(Component.literal("No armor on stand!"), true);
                return;
            }

            // FIXED: Validate it's a brass suit
            if (!isBrassSuit(helmet, chestplate, leggings, boots)) {
                player.displayClientMessage(
                        Component.literal("Only brass suits can be called! This stand has non-brass armor.")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return;
            }

            // NEW: Check Remote Assembly Level (must be Stage 1 or higher)
            int remoteLevel = ArmorUpgradeHelper.getRemoteAssemblyLevel(chestplate);
            if (remoteLevel < 1) {
                player.displayClientMessage(
                        Component.literal("⚠ Remote Assembly Not Installed!")
                                .withStyle(ChatFormatting.RED),
                        false
                );
                player.displayClientMessage(
                        Component.literal("This suit cannot be remotely called. Upgrade it at a Modification Station with Remote Assembly Protocol.")
                                .withStyle(ChatFormatting.GRAY),
                        false
                );
                return;
            }

            // Create flying suit entity
            FlyingSuitEntity flyingSuit = new FlyingSuitEntity(
                    player.level(), standPos, player,
                    helmet, chestplate, leggings, boots
            );
            player.level().addFreshEntity(flyingSuit);

            // Clear armor from stand
            armorStand.setArmor(0, ItemStack.EMPTY);
            armorStand.setArmor(1, ItemStack.EMPTY);
            armorStand.setArmor(2, ItemStack.EMPTY);
            armorStand.setArmor(3, ItemStack.EMPTY);

            String levelText = remoteLevel == 1 ? "⭐⭐ Remote Assembly" : "⭐⭐⭐ Field Assembly";
            player.displayClientMessage(
                    Component.literal("Brass suit incoming! (" + levelText + ")")
                            .withStyle(ChatFormatting.GREEN),
                    true
            );
        });
    }
}
