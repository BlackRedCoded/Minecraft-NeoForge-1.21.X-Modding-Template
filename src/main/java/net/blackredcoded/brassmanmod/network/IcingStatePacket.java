package net.blackredcoded.brassmanmod.network;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record IcingStatePacket(boolean isIced) implements CustomPacketPayload {
    public static final Type<IcingStatePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, "icing_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, IcingStatePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, IcingStatePacket::isIced,
                    IcingStatePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(IcingStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // Store icing state in player persistent data
                player.getPersistentData().putBoolean("BrassManIced", packet.isIced);
            }
        });
    }
}
