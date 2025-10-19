package net.blackredcoded.brassmanmod.network;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.items.BrassManChestplateItem;
import net.blackredcoded.brassmanmod.util.ArmorUpgradeHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ConsumeAirPacket(int amount) implements CustomPacketPayload {

    public static final Type<ConsumeAirPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, "consume_air"));

    public static final StreamCodec<FriendlyByteBuf, ConsumeAirPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, ConsumeAirPacket::amount,
                    ConsumeAirPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ConsumeAirPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
                if (chestplate.getItem() instanceof BrassManChestplateItem brass) {

                    // DEBUG: Check efficiency upgrades
                    int airEffCount = ArmorUpgradeHelper.getUpgradeCount(chestplate, ArmorUpgradeHelper.AIR_EFFICIENCY);
                    float multiplier = ArmorUpgradeHelper.getAirEfficiencyMultiplier(chestplate);
                    int baseAmount = packet.amount;

                    // Use the instance method that applies efficiency
                    BrassManChestplateItem.consumeAir(chestplate, packet.amount);
                }
            }
        });
    }
}
