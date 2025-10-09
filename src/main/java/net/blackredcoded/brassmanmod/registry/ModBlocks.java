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

    public static final Supplier<Block> AIR_COMPRESSOR = BLOCKS.register("air_compressor",
            () -> new AirCompressorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final Supplier<Block> BRASS_ARMOR_STAND = BLOCKS.register("brass_armor_stand",
            () -> new BrassArmorStandBaseBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(0.8f, 2.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final Supplier<Block> BRASS_ARMOR_STAND_TOP = BLOCKS.register("brass_armor_stand_top",
            () -> new BrassArmorStandTopBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(0.8f, 2.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

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

    public static void registerBlockItems(DeferredRegister<Item> items) {
        items.register("air_compressor",
                () -> new BlockItem(AIR_COMPRESSOR.get(), new Item.Properties()));
        items.register("brass_armor_stand",
                () -> new BlockItem(BRASS_ARMOR_STAND.get(), new Item.Properties()));
        items.register("brass_modification_station",
                () -> new BlockItem(BRASS_MODIFICATION_STATION.get(), new Item.Properties()));
    }
}
