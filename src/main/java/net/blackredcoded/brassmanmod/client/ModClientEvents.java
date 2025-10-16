package net.blackredcoded.brassmanmod.client;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.client.renderer.BrassArmorStandRenderer;
import net.blackredcoded.brassmanmod.client.renderer.CompressorNetworkTerminalBlockEntityRenderer;
import net.blackredcoded.brassmanmod.client.renderer.DataLinkBlockEntityRenderer;
import net.blackredcoded.brassmanmod.client.renderer.FlyingSuitRenderer;
import net.blackredcoded.brassmanmod.client.renderer.FlyingArmorPieceRenderer;
import net.blackredcoded.brassmanmod.client.renderer.SentryArmorRenderer;
import net.blackredcoded.brassmanmod.registry.ModBlockEntities;
import net.blackredcoded.brassmanmod.registry.ModEntityTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = BrassManMod.MOD_ID, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.BRASS_ARMOR_STAND.get(),
                BrassArmorStandRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.DATA_LINK.get(),
                DataLinkBlockEntityRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.FLYING_SUIT.get(),
                FlyingSuitRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.COMPRESSOR_NETWORK_TERMINAL.get(),
                CompressorNetworkTerminalBlockEntityRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.FLYING_SUIT.get(),
                FlyingSuitRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.FLYING_ARMOR_PIECE.get(),
                FlyingArmorPieceRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.SENTRY_ARMOR.get(),
                SentryArmorRenderer::new);
    }
}