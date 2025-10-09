package net.blackredcoded.brassmanmod.network;

import io.netty.buffer.ByteBuf;
import net.blackredcoded.brassmanmod.BrassManMod;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ApplyDyePacket(int color, boolean confirm) implements CustomPacketPayload {
    public static final Type<ApplyDyePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, "apply_dye"));

    public static final StreamCodec<ByteBuf, ApplyDyePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ApplyDyePacket::color,
            ByteBufCodecs.BOOL,
            ApplyDyePacket::confirm,
            ApplyDyePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // This is the handle method that ModNetworking is looking for
    public static void handle(ApplyDyePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            // Handle the packet logic here
            if (packet.confirm()) {
                // Apply actual dye to armor
                // TODO: Implement armor dyeing logic
            } else {
                // Just preview the color
                // TODO: Implement preview logic
            }
        });
    }
}
