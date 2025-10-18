package net.blackredcoded.brassmanmod.registry;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.items.*;
import net.blackredcoded.brassmanmod.items.upgrades.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, BrassManMod.MOD_ID);

    // Armor
    public static final Supplier<Item> BRASS_MAN_HELMET = ITEMS.register("brass_man_helmet",
            () -> new BrassManHelmetItem(ModArmorMaterials.BRASS_MAN, new Item.Properties()));

    public static final Supplier<Item> BRASS_MAN_CHESTPLATE = ITEMS.register("brass_man_chestplate",
            () -> new BrassManChestplateItem(new Item.Properties()));

    public static final Supplier<Item> BRASS_MAN_LEGGINGS = ITEMS.register("brass_man_leggings",
            () -> new BrassManLeggingsItem(ModArmorMaterials.BRASS_MAN, new Item.Properties()));

    public static final Supplier<Item> BRASS_MAN_BOOTS = ITEMS.register("brass_man_boots",
            () -> new BrassManBootsItem(ModArmorMaterials.BRASS_MAN, new Item.Properties()));

    // === BRASS ARMOR (Regular) ===
    public static final DeferredHolder<Item, ArmorItem> BRASS_HELMET = ITEMS.register("brass_helmet",
            () -> new ArmorItem(ModArmorMaterials.BRASS, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(20))));

    public static final DeferredHolder<Item, ArmorItem> BRASS_CHESTPLATE = ITEMS.register("brass_chestplate",
            () -> new ArmorItem(ModArmorMaterials.BRASS, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(20))));

    public static final DeferredHolder<Item, ArmorItem> BRASS_LEGGINGS = ITEMS.register("brass_leggings",
            () -> new ArmorItem(ModArmorMaterials.BRASS, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(20))));

    public static final DeferredHolder<Item, ArmorItem> BRASS_BOOTS = ITEMS.register("brass_boots",
            () -> new ArmorItem(ModArmorMaterials.BRASS, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(20))));

    // Special Items
    public static final Supplier<Item> JARVIS_COMMUNICATOR = ITEMS.register("jarvis_communicator",
            () -> new JarvisCommunicatorItem(ModArmorMaterials.COMMUNICATOR, new Item.Properties()));

    public static final Supplier<Item> COMPRESSOR_NETWORK_TABLET = ITEMS.register("compressor_network_tablet",
            () -> new CompressorNetworkTabletItem(new Item.Properties().stacksTo(1).durability(100)));

    public static final DeferredHolder<Item, Item> KINETIC_BATTERY = ITEMS.register("kinetic_battery",
            () -> new KineticBatteryItem());

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

    public static final Supplier<Item> QUICK_CHARGING_UPGRADE = ITEMS.register("quick_charging_upgrade",
            () -> new QuickChargeUpgradeItem(new Item.Properties()));
    public static final Supplier<Item> REMOTE_ASSEMBLY_UPGRADE = ITEMS.register("remote_assembly_upgrade",
            () -> new RemoteAssemblyUpgradeItem(new Item.Properties()));

    // === CRAFTING COMPONENTS ===
    public static final DeferredHolder<Item, Item> PNEUMATIC_CORE = ITEMS.register("pneumatic_core",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> POWER_CORE = ITEMS.register("power_core",
            () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> COMPACT_MECHANISM = ITEMS.register("compact_mechanism",
            () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> KINETIC_CIRCUIT = ITEMS.register("kinetic_circuit",
            () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> SMART_MECHANISM = ITEMS.register("smart_mechanism",
            () -> new Item(new Item.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        ModBlocks.registerBlockItems(ITEMS);
    }
}
