package net.blackredcoded.brassmanmod.client;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.client.renderer.BrassArmorStandRenderer;
import net.blackredcoded.brassmanmod.client.renderer.DataLinkBlockEntityRenderer;
import net.blackredcoded.brassmanmod.client.renderer.FlyingSuitRenderer;
import net.blackredcoded.brassmanmod.entity.FlyingSuitEntity;
import net.blackredcoded.brassmanmod.registry.ModBlockEntities;
import net.blackredcoded.brassmanmod.registry.ModEntityTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

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
    }
}
