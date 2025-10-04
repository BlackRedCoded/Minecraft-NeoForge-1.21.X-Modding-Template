package net.blackredcoded.brassmanmod.network;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.blockentity.AirCompressorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ConvertMaterialsPacket(BlockPos pos) implements CustomPacketPayload {

    public static final Type<ConvertMaterialsPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, "convert_materials"));

    public static final StreamCodec<FriendlyByteBuf, ConvertMaterialsPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ConvertMaterialsPacket::pos,
                    ConvertMaterialsPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ConvertMaterialsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.level().getBlockEntity(packet.pos()) instanceof AirCompressorBlockEntity compressor) {
                    compressor.convertInputToMaterials();
                }
            }
        });
    }

    public static void send(BlockPos pos) {
        ModNetworking.sendToServer(new ConvertMaterialsPacket(pos));
    }
}
