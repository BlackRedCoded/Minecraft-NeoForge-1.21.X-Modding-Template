package net.blackredcoded.brassmanmod.network;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.blockentity.AirCompressorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RenameCompressorPacket(BlockPos pos, Component name) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RenameCompressorPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, "rename_compressor"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RenameCompressorPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    RenameCompressorPacket::pos,
                    ComponentSerialization.STREAM_CODEC,
                    RenameCompressorPacket::name,
                    RenameCompressorPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(BlockPos pos, Component name) {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new RenameCompressorPacket(pos, name));
    }

    public static void handle(RenameCompressorPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                BlockEntity be = serverPlayer.level().getBlockEntity(packet.pos);

                if (be instanceof AirCompressorBlockEntity compressor) {
                    compressor.setCustomName(packet.name);
                }
            }
        });
    }
}
