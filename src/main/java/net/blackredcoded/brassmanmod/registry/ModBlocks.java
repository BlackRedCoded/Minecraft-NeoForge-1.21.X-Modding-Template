package net.blackredcoded.brassmanmod.registry;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.blocks.AirCompressorBlock;
import net.blackredcoded.brassmanmod.blocks.BrassArmorStandBaseBlock;
import net.blackredcoded.brassmanmod.blocks.BrassArmorStandTopBlock;
import net.blackredcoded.brassmanmod.blocks.BrassModificationStationBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, BrassManMod.MOD_ID);

    // Air Compressor - proper mining speed
    public static final Supplier<Block> AIR_COMPRESSOR = BLOCKS.register("air_compressor",
            () -> new AirCompressorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion())); // CRITICAL FIX - Create kinetic blocks need this

    // FIXED: Fast mining like vanilla armor stand
    public static final Supplier<Block> BRASS_ARMOR_STAND = BLOCKS.register("brass_armor_stand",
            () -> new BrassArmorStandBaseBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(0.8f, 2.0f) // FIXED: Much faster mining (like vanilla armor stand)
                    .sound(SoundType.WOOD)
                    .noOcclusion())); // FIXED: No tool requirement - can break with hand

    // Top part - same fast mining
    public static final Supplier<Block> BRASS_ARMOR_STAND_TOP = BLOCKS.register("brass_armor_stand_top",
            () -> new BrassArmorStandTopBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(0.8f, 2.0f) // FIXED: Fast mining
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    // NEW: Brass Modification Station
    public static final Supplier<Block> BRASS_MODIFICATION_STATION = BLOCKS.register("brass_modification_station",
            () -> new BrassModificationStationBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f, 6.0f)
                    .sound(SoundType.ANVIL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    // Block items registration
    public static void registerBlockItems(DeferredRegister<Item> itemRegister) {
        itemRegister.register("air_compressor",
                () -> new BlockItem(AIR_COMPRESSOR.get(), new Item.Properties()));
        itemRegister.register("brass_armor_stand",
                () -> new BlockItem(BRASS_ARMOR_STAND.get(), new Item.Properties()));
        itemRegister.register("brass_modification_station",
                () -> new BlockItem(BRASS_MODIFICATION_STATION.get(), new Item.Properties()));
        // Note: Top block is not craftable - it's created automatically
    }
}
