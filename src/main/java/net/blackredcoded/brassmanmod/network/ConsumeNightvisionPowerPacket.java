package net.blackredcoded.brassmanmod.network;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.items.BrassManChestplateItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ConsumeNightvisionPowerPacket(int amount) implements CustomPacketPayload {

    public static final Type<ConsumeNightvisionPowerPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, "consume_nightvision_power"));

    public static final StreamCodec<FriendlyByteBuf, ConsumeNightvisionPowerPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, ConsumeNightvisionPowerPacket::amount,
                    ConsumeNightvisionPowerPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ConsumeNightvisionPowerPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
                if (chestplate.getItem() instanceof BrassManChestplateItem brass) {
                    // Use the instance method that applies efficiency
                    brass.consumePower(chestplate, packet.amount);
                }
            }
        });
    }
}
