package net.blackredcoded.brassmanmod.network;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GrantAdvancementPacket(String advancementPath) implements CustomPacketPayload {
    public static final Type<GrantAdvancementPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, "grant_advancement"));

    public static final StreamCodec<FriendlyByteBuf, GrantAdvancementPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    GrantAdvancementPacket::advancementPath,
                    GrantAdvancementPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(GrantAdvancementPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            ResourceLocation advancementId = ResourceLocation.parse(packet.advancementPath);
            AdvancementHolder advancement = player.server.getAdvancements().get(advancementId);

            if (advancement != null) {
                AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);

                // Grant all remaining criteria
                for (String criterion : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(advancement, criterion);
                }
            }
        });
    }
}
