package net.blackredcoded.brassmanmod.network;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.config.FlightConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncFlightConfigPacket(CompoundTag configData) implements CustomPacketPayload {

    public static final Type<SyncFlightConfigPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, "sync_flight_config"));

    public static final StreamCodec<FriendlyByteBuf, SyncFlightConfigPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.COMPOUND_TAG, SyncFlightConfigPacket::configData,
                    SyncFlightConfigPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncFlightConfigPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Client-side: store the config data
            FlightConfig.CLIENT_CONFIG.load(packet.configData);
        });
    }
}
