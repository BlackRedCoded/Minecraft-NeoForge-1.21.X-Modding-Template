package net.blackredcoded.brassmanmod.registry;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.menu.AirCompressorMenu;
import net.blackredcoded.brassmanmod.menu.ModificationStationMenu;
import net.blackredcoded.brassmanmod.screen.CustomizationStationScreenHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, BrassManMod.MOD_ID);

    public static final Supplier<MenuType<AirCompressorMenu>> AIR_COMPRESSOR_MENU =
            MENU_TYPES.register("air_compressor",
                    () -> IMenuTypeExtension.create(AirCompressorMenu::new));

    public static final Supplier<MenuType<ModificationStationMenu>> MODIFICATION_STATION =
            MENU_TYPES.register("modification_station",
                    () -> new MenuType<>(ModificationStationMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<CustomizationStationScreenHandler>> CUSTOMIZATION_STATION =
            MENU_TYPES.register("customization_station",
                    () -> new MenuType<>(CustomizationStationScreenHandler::new, FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
