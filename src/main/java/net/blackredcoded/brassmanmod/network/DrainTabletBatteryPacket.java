package net.blackredcoded.brassmanmod.network;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.util.BatteryHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DrainTabletBatteryPacket() implements CustomPacketPayload {

    public static final Type<DrainTabletBatteryPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, "drain_tablet_battery"));

    public static final StreamCodec<FriendlyByteBuf, DrainTabletBatteryPacket> STREAM_CODEC =
            StreamCodec.unit(new DrainTabletBatteryPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DrainTabletBatteryPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack mainHand = player.getMainHandItem();

                // Check if holding a battery-powered item
                if (BatteryHelper.isBatteryItem(mainHand)) {
                    // Drain 1 battery
                    BatteryHelper.drainBattery(mainHand, 1);
                }
            }
        });
    }

    public static void send() {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new DrainTabletBatteryPacket());
    }
}
