package net.blackredcoded.brassmanmod.network;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.items.CompressorNetworkTabletItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LinkTabletPacket(BlockPos terminalPos) implements CustomPacketPayload {

    public static final Type<LinkTabletPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, "link_tablet"));

    public static final StreamCodec<FriendlyByteBuf, LinkTabletPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    LinkTabletPacket::terminalPos,
                    LinkTabletPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(LinkTabletPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack tablet = player.getMainHandItem();
                if (tablet.getItem() instanceof CompressorNetworkTabletItem) {
                    CompressorNetworkTabletItem.linkToTerminal(tablet, packet.terminalPos());
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("Tablet linked to terminal!"), true);
                }
            }
        });
    }

    public static void send(BlockPos terminalPos) {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new LinkTabletPacket(terminalPos));
    }
}
