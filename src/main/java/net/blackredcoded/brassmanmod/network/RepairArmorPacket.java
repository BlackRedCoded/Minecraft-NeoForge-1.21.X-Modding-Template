package net.blackredcoded.brassmanmod.network;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.blockentity.AirCompressorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RepairArmorPacket(BlockPos pos, int slot) implements CustomPacketPayload {

    public static final Type<RepairArmorPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, "repair_armor"));

    public static final StreamCodec<FriendlyByteBuf, RepairArmorPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, RepairArmorPacket::pos,
                    ByteBufCodecs.INT, RepairArmorPacket::slot,
                    RepairArmorPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RepairArmorPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.level().getBlockEntity(packet.pos()) instanceof AirCompressorBlockEntity compressor) {
                    // FIXED: Correct slot mapping
                    switch (packet.slot()) {
                        case 0 -> compressor.repairBoots();    // Slot 0 = Boots
                        case 1 -> compressor.repairLeggings(); // Slot 1 = Leggings
                        case 2 -> compressor.repairChestplate(); // Slot 2 = Chestplate
                        case 3 -> compressor.repairHelmet();   // Slot 3 = Helmet
                    }
                }
            }
        });
    }

    public static void send(BlockPos pos, int slot) {
        ModNetworking.sendToServer(new RepairArmorPacket(pos, slot));
    }
}
