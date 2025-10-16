package net.blackredcoded.brassmanmod;

import net.blackredcoded.brassmanmod.entity.SentryArmorEntity;
import net.blackredcoded.brassmanmod.registry.ModEntityTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = BrassManMod.MOD_ID)
public class ModEntityEvents {

    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.SENTRY_ARMOR.get(), SentryArmorEntity.createAttributes().build());
    }
}
