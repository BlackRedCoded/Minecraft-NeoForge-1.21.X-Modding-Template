package net.blackredcoded.brassmanmod.registry;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.blockentity.AirCompressorBlockEntity;
import net.blackredcoded.brassmanmod.blockentity.BrassArmorStandBlockEntity;
import net.blackredcoded.brassmanmod.blockentity.CustomizationStationBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, BrassManMod.MOD_ID);

    public static final Supplier<BlockEntityType<BrassArmorStandBlockEntity>> BRASS_ARMOR_STAND =
            BLOCK_ENTITIES.register("brass_armor_stand", () ->
                    BlockEntityType.Builder.of(BrassArmorStandBlockEntity::new,
                            ModBlocks.BRASS_ARMOR_STAND.get()).build(null));

    public static final Supplier<BlockEntityType<AirCompressorBlockEntity>> AIR_COMPRESSOR =
            BLOCK_ENTITIES.register("air_compressor", () ->
                    BlockEntityType.Builder.of((pos, state) -> new AirCompressorBlockEntity(ModBlockEntities.AIR_COMPRESSOR.get(), pos, state),
                            ModBlocks.AIR_COMPRESSOR.get()).build(null));

    public static final Supplier<BlockEntityType<CustomizationStationBlockEntity>> CUSTOMIZATION_STATION =
            BLOCK_ENTITIES.register("customization_station", () ->
                    BlockEntityType.Builder.of(CustomizationStationBlockEntity::new,
                            ModBlocks.CUSTOMIZATION_STATION.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
