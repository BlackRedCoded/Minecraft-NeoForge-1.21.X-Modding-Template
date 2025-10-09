package net.blackredcoded.brassmanmod.client;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.client.screen.AirCompressorScreen;
import net.blackredcoded.brassmanmod.client.screen.ModificationStationScreen;
import net.blackredcoded.brassmanmod.registry.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid=BrassManMod.MOD_ID, value=Dist.CLIENT)
public class ModScreens {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent e) {
        e.register(ModMenuTypes.AIR_COMPRESSOR_MENU.get(), AirCompressorScreen::new);
        e.register(ModMenuTypes.MODIFICATION_STATION.get(), ModificationStationScreen::new);
    }
}
