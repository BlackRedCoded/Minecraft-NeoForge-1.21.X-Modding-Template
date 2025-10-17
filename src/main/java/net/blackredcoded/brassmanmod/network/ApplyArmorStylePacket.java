package net.blackredcoded.brassmanmod.network;

import io.netty.buffer.ByteBuf;
import net.blackredcoded.brassmanmod.menu.CustomizationStationMenu;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ApplyArmorStylePacket(String style) implements CustomPacketPayload {

    public static final Type<ApplyArmorStylePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("brassmanmod", "apply_armor_style")
    );

    public static final StreamCodec<ByteBuf, ApplyArmorStylePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ApplyArmorStylePacket::style,
            ApplyArmorStylePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ApplyArmorStylePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer) {
                if (serverPlayer.containerMenu instanceof CustomizationStationMenu menu) {
                    menu.purchaseStyle(serverPlayer, packet.style);
                }
            }
        });
    }
}
