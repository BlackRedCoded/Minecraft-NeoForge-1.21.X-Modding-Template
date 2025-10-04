package net.blackredcoded.brassmanmod;

import net.blackredcoded.brassmanmod.client.BrassArmorHudOverlay;
import net.blackredcoded.brassmanmod.commands.JarvisCommand;
import net.blackredcoded.brassmanmod.commands.SetStatsCommand;
import net.blackredcoded.brassmanmod.registry.ModBlockEntities;
import net.blackredcoded.brassmanmod.registry.ModBlocks;
import net.blackredcoded.brassmanmod.registry.ModItems;
import net.blackredcoded.brassmanmod.registry.ModMenuTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
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
                    .icon(() -> new ItemStack(ModItems.BRASS_CHESTPLATE.get()))
                    .displayItems((parameters, output) -> {
                        // Armor
                        output.accept(ModItems.BRASS_HELMET.get());
                        output.accept(ModItems.BRASS_CHESTPLATE.get());
                        output.accept(ModItems.BRASS_LEGGINGS.get());
                        output.accept(ModItems.BRASS_BOOTS.get());

                        // Special Items
                        output.accept(ModItems.JARVIS_COMMUNICATOR.get());
                        output.accept(ModItems.BRASS_PNEUMATIC_CORE.get());

                        // Upgrade Modules
                        output.accept(ModItems.POWER_CELL_UPGRADE.get());
                        output.accept(ModItems.AIR_TANK_UPGRADE.get());
                        output.accept(ModItems.SPEED_AMPLIFIER_UPGRADE.get());
                        output.accept(ModItems.AIR_EFFICIENCY_UPGRADE.get());
                        output.accept(ModItems.POWER_EFFICIENCY_UPGRADE.get());

                        // Blocks
                        output.accept(ModBlocks.AIR_COMPRESSOR.get());
                        output.accept(ModBlocks.BRASS_ARMOR_STAND.get());
                        output.accept(ModBlocks.BRASS_MODIFICATION_STATION.get());
                        // Note: BRASS_ARMOR_STAND_TOP is not added - it's auto-placed
                    })
                    .build()
    );

    public BrassManMod(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::registerGuiOverlays);
        }

        NeoForge.EVENT_BUS.register(this);
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
}
