package net.blackredcoded.brassmanmod.network;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = BrassManMod.MOD_ID)
public class ModNetworking {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        // Server to Client packets (playToServer)
        registrar.playToServer(
                ConvertMaterialsPacket.TYPE,
                ConvertMaterialsPacket.STREAM_CODEC,
                ConvertMaterialsPacket::handle
        );

        registrar.playToServer(
                RepairArmorPacket.TYPE,
                RepairArmorPacket.STREAM_CODEC,
                RepairArmorPacket::handle
        );

        registrar.playToServer(
                ConsumeAirPacket.TYPE,
                ConsumeAirPacket.STREAM_CODEC,
                ConsumeAirPacket::handle
        );

        registrar.playToServer(
                FallsavePacket.TYPE,
                FallsavePacket.STREAM_CODEC,
                FallsavePacket::handle
        );

        registrar.playToServer(
                ConsumeNightvisionPowerPacket.TYPE,
                ConsumeNightvisionPowerPacket.STREAM_CODEC,
                ConsumeNightvisionPowerPacket::handle
        );

        registrar.playToClient(
                SyncFlightConfigPacket.TYPE,
                SyncFlightConfigPacket.STREAM_CODEC,
                SyncFlightConfigPacket::handle
        );
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}
