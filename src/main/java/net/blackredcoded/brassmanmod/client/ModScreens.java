package net.blackredcoded.brassmanmod.client;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.client.screen.*;
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
        e.register(ModMenuTypes.COMPRESSOR_NETWORK_TERMINAL_MENU.get(), CompressorNetworkTerminalScreen::new);
        e.register(ModMenuTypes.REMOTE_SUIT_MENU.get(), RemoteSuitScreen::new);
        e.register(ModMenuTypes.KINETIC_MOTOR_MENU.get(), KineticMotorScreen::new);
        e.register(ModMenuTypes.CUSTOMIZATION_STATION.get(), CustomizationStationScreen::new);
    }
}
