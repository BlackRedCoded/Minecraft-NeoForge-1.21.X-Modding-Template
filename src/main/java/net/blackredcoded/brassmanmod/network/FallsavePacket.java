package net.blackredcoded.brassmanmod.network;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.config.FlightConfig;
import net.blackredcoded.brassmanmod.items.BrassChestplateItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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
            if (context.player() instanceof ServerPlayer player) {
                ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
                if (chestplate.getItem() instanceof BrassChestplateItem brass) {
                    FlightConfig.PlayerFlightData config = FlightConfig.get(player);

                    int currentAir = brass.air(chestplate);
                    int currentPower = brass.power(chestplate);

                    // NEW: Power-to-Air conversion
                    int powerToConvert = config.fallsavePowerToAir;
                    if (powerToConvert > 0 && currentPower >= powerToConvert) {
                        int airToAdd = powerToConvert / 10; // 10 power = 1 air
                        brass.setAirAndPower(
                                chestplate,
                                Math.min(currentAir + airToAdd, BrassChestplateItem.getMaxAir(chestplate)),
                                currentPower - powerToConvert
                        );
                        currentAir = brass.air(chestplate);
                    }

                    // NEW: Enable flight if fallsaveFlight is true
                    if (config.fallsaveFlight) {
                        FlightConfig.setFlightEnabled(player, true);
                    }

                    // NEW: Enable hover if fallsaveHover is true
                    if (config.fallsaveHover) {
                        FlightConfig.setHoverEnabled(player, true);
                    }
                }
            }
        });
    }
}
