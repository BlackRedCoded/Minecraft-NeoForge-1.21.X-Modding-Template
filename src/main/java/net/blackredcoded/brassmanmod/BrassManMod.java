package net.blackredcoded.brassmanmod;

import net.blackredcoded.brassmanmod.client.BrassArmorHudOverlay;
import net.blackredcoded.brassmanmod.commands.JarvisCommand;
import net.blackredcoded.brassmanmod.commands.SetStatsCommand;
import net.blackredcoded.brassmanmod.event.ArmorReturnHandler;
import net.blackredcoded.brassmanmod.registry.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Mod(BrassManMod.MOD_ID)
public class BrassManMod {
    public static final String MOD_ID = "brassmanmod";

    // Creative Tab Registry
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    // Custom Creative Tab
    public static final Supplier<CreativeModeTab> BRASS_MAN_TAB = CREATIVE_MODE_TABS.register("brass_man_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("Brass Man"))
                    .icon(() -> new ItemStack(ModItems.BRASS_MAN_HELMET.get()))
                    .displayItems((parameters, output) -> {
                        // Armor
                        output.accept(ModItems.BRASS_MAN_HELMET.get());
                        output.accept(ModItems.BRASS_MAN_CHESTPLATE.get());
                        output.accept(ModItems.BRASS_MAN_LEGGINGS.get());
                        output.accept(ModItems.BRASS_MAN_BOOTS.get());
                        output.accept(ModItems.BRASS_HELMET.get());
                        output.accept(ModItems.BRASS_CHESTPLATE.get());
                        output.accept(ModItems.BRASS_LEGGINGS.get());
                        output.accept(ModItems.BRASS_BOOTS.get());

                        // Special Items
                        output.accept(ModItems.JARVIS_COMMUNICATOR.get());
                        output.accept(ModItems.COMPRESSOR_NETWORK_TABLET.get());
                        output.accept(ModItems.PNEUMATIC_CORE.get());
                        output.accept(ModItems.POWER_CORE.get());
                        output.accept(ModItems.COMPACT_MECHANISM.get());
                        output.accept(ModItems.KINETIC_CIRCUIT.get());
                        output.accept(ModItems.KINETIC_BATTERY.get());

                        // Upgrade Modules
                        output.accept(ModItems.POWER_CELL_UPGRADE.get());
                        output.accept(ModItems.AIR_TANK_UPGRADE.get());
                        output.accept(ModItems.SPEED_AMPLIFIER_UPGRADE.get());
                        output.accept(ModItems.AIR_EFFICIENCY_UPGRADE.get());
                        output.accept(ModItems.POWER_EFFICIENCY_UPGRADE.get());
                        output.accept(ModItems.QUICK_CHARGING_UPGRADE.get());

                        // Blocks
                        output.accept(ModBlocks.AIR_COMPRESSOR.get());
                        output.accept(ModBlocks.BRASS_ARMOR_STAND.get());
                        output.accept(ModBlocks.DATA_LINK.get());
                        output.accept(ModBlocks.BRASS_MODIFICATION_STATION.get());
                        output.accept(ModBlocks.COMPRESSOR_NETWORK_TERMINAL.get());
                        output.accept(ModBlocks.KINETIC_MOTOR.get());
                    })
                    .build()
    );

    public BrassManMod(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModEntityTypes.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::registerGuiOverlays);
        }

        // Register event handlers
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(ArmorReturnHandler.class);
    }

    private void registerGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerBelow(
                ResourceLocation.withDefaultNamespace("hotbar"),
                ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, "brass_armor_hud"),
                new BrassArmorHudOverlay()
        );
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        JarvisCommand.register(event.getDispatcher());
        SetStatsCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            ArmorReturnHandler.tickArmorReturns(serverLevel);
        }
    }
}
