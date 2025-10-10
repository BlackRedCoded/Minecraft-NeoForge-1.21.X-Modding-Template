package net.blackredcoded.brassmanmod.network;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.blockentity.AirCompressorBlockEntity;
import net.blackredcoded.brassmanmod.blockentity.BrassArmorStandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
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
                    // Get the armor stand position (one block above)
                    BlockPos armorStandPos = packet.pos().above();
                    BlockEntity armorStandEntity = player.level().getBlockEntity(armorStandPos);

                    // Repair the armor
                    switch (packet.slot()) {
                        case 0 -> compressor.repairHelmet();   // Slot 0 = Helmet
                        case 1 -> compressor.repairChestplate(); // Slot 1 = Chestplate
                        case 2 -> compressor.repairLeggings();   // Slot 2 = Leggings
                        case 3 -> compressor.repairBoots();      // Slot 3 = Boots
                    }

                    // Mark the armor stand as changed and sync to client
                    if (armorStandEntity instanceof BrassArmorStandBlockEntity armorStand) {
                        armorStand.setChanged();
                        player.level().sendBlockUpdated(armorStandPos, armorStand.getBlockState(), armorStand.getBlockState(), 3);
                    }

                    // Also update the compressor (for material values)
                    compressor.setChanged();
                    player.level().sendBlockUpdated(packet.pos(), compressor.getBlockState(), compressor.getBlockState(), 3);
                }
            }
        });
    }

    public static void send(BlockPos pos, int slot) {
        ModNetworking.sendToServer(new RepairArmorPacket(pos, slot));
    }
}
