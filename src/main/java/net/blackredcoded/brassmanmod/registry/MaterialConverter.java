package net.blackredcoded.brassmanmod.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import java.util.HashMap;
import java.util.Map;

public class MaterialConverter {

    // Material types
    public static final int BRASS = 0;
    public static final int ELECTRONICS = 1;
    public static final int GLASS = 2;

    // Material names for display
    public static final String[] MATERIAL_NAMES = {"Brass", "Electronics", "Glass"};

    // Map: Item -> [brass, electronics, glass]
    private static final Map<Item, int[]> CONVERSIONS = new HashMap<>();

    static {
        // Brass conversions
        register(Items.COPPER_INGOT, 50, 0, 0); // Brass Ingot (using copper as placeholder)
        register(Items.IRON_NUGGET, 5, 0, 0); // Brass Nugget (placeholder)

        // Electronics conversions
        register(Items.REDSTONE, 0, 5, 0);
        register(Items.REPEATER, 0, 15, 0);
        register(Items.COMPARATOR, 0, 20, 0);

        // Glass conversions
        register(Items.GLASS, 0, 0, 10);
        register(Items.GLASS_PANE, 0, 0, 5);

        // Complex items (multiple materials)
        register(Items.CLOCK, 25, 30, 5); // Precision Mechanism placeholder
        register(Items.PISTON, 15, 20, 0);
    }

    private static void register(Item item, int brass, int electronics, int glass) {
        CONVERSIONS.put(item, new int[]{brass, electronics, glass});
    }

    public static int[] getMaterials(Item item) {
        return CONVERSIONS.getOrDefault(item, new int[]{0, 0, 0});
    }

    public static boolean canConvert(Item item) {
        return CONVERSIONS.containsKey(item);
    }
}
