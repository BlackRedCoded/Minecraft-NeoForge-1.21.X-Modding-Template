package net.blackredcoded.brassmanmod.network;

import io.netty.buffer.ByteBuf;
import net.blackredcoded.brassmanmod.menu.RemoteSuitMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenRemoteCompressorPacket(BlockPos compressorPos) implements CustomPacketPayload {
    public static final Type<OpenRemoteCompressorPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("brassmanmod", "open_remote_compressor"));

    public static final StreamCodec<ByteBuf, OpenRemoteCompressorPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpenRemoteCompressorPacket::compressorPos,
            OpenRemoteCompressorPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(BlockPos pos) {
        PacketDistributor.sendToServer(new OpenRemoteCompressorPacket(pos));
    }

    public static void handle(OpenRemoteCompressorPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // Open Remote Suit Menu with proper buffer writing
                player.openMenu(new SimpleMenuProvider(
                        (containerId, playerInventory, p) ->
                                new RemoteSuitMenu(containerId, playerInventory, packet.compressorPos),
                        Component.literal("Remote Suit Management")
                ), buf -> buf.writeBlockPos(packet.compressorPos));
            }
        });
    }
}
