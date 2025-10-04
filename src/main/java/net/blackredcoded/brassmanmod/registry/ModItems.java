package net.blackredcoded.brassmanmod.registry;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.items.*;
import net.blackredcoded.brassmanmod.items.upgrades.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, BrassManMod.MOD_ID);

    // Core Items
    public static final Supplier<Item> BRASS_PNEUMATIC_CORE = ITEMS.register("brass_pneumatic_core",
            () -> new BrassPneumaticCoreItem(new Item.Properties()));

    // Armor
    public static final Supplier<Item> BRASS_HELMET = ITEMS.register("brass_helmet",
            () -> new BrassHelmetItem(ModArmorMaterials.BRASS, new Item.Properties()));

    public static final Supplier<Item> BRASS_CHESTPLATE = ITEMS.register("brass_chestplate",
            () -> new BrassChestplateItem(new Item.Properties()));

    public static final Supplier<Item> BRASS_LEGGINGS = ITEMS.register("brass_leggings",
            () -> new BrassLeggingsItem(ModArmorMaterials.BRASS, new Item.Properties()));

    public static final Supplier<Item> BRASS_BOOTS = ITEMS.register("brass_boots",
            () -> new BrassBootsItem(ModArmorMaterials.BRASS, new Item.Properties()));

    // Special Items
    public static final Supplier<Item> JARVIS_COMMUNICATOR = ITEMS.register("jarvis_communicator",
            () -> new JarvisCommunicatorItem(ModArmorMaterials.COMMUNICATOR, new Item.Properties()));

    // Upgrade Modules
    public static final Supplier<Item> POWER_CELL_UPGRADE = ITEMS.register("power_cell_upgrade",
            () -> new PowerCellUpgradeItem(new Item.Properties()));

    public static final Supplier<Item> AIR_TANK_UPGRADE = ITEMS.register("air_tank_upgrade",
            () -> new AirTankUpgradeItem(new Item.Properties()));

    public static final Supplier<Item> SPEED_AMPLIFIER_UPGRADE = ITEMS.register("speed_amplifier_upgrade",
            () -> new SpeedAmplifierUpgradeItem(new Item.Properties()));

    public static final Supplier<Item> AIR_EFFICIENCY_UPGRADE = ITEMS.register("air_efficiency_upgrade",
            () -> new AirEfficiencyUpgradeItem(new Item.Properties()));

    public static final Supplier<Item> POWER_EFFICIENCY_UPGRADE = ITEMS.register("power_efficiency_upgrade",
            () -> new PowerEfficiencyUpgradeItem(new Item.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        ModBlocks.registerBlockItems(ITEMS);
    }
}
