package net.blackredcoded.brassmanmod.registry;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.menu.*;
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

    public static final Supplier<MenuType<CompressorNetworkTerminalMenu>> COMPRESSOR_NETWORK_TERMINAL_MENU =
            MENU_TYPES.register("compressor_network_terminal",
                    () -> IMenuTypeExtension.create(CompressorNetworkTerminalMenu::new));

    public static final Supplier<MenuType<RemoteSuitMenu>> REMOTE_SUIT_MENU =
            MENU_TYPES.register("remote_suit_menu",
                    () -> IMenuTypeExtension.create(RemoteSuitMenu::new));

    public static final Supplier<MenuType<KineticMotorMenu>> KINETIC_MOTOR_MENU =
            MENU_TYPES.register("kinetic_motor_menu",
                    () -> IMenuTypeExtension.create(KineticMotorMenu::new));

    public static final Supplier<MenuType<CustomizationStationMenu>> CUSTOMIZATION_STATION =
            MENU_TYPES.register("customization_station_menu",
                    () -> new MenuType<>(CustomizationStationMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
