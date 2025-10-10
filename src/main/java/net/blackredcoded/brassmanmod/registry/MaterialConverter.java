package net.blackredcoded.brassmanmod.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
        // Load all conversions from the registry
        RegistrationHelper helper = new RegistrationHelper();
        MaterialConversionRegistry.registerConversions(helper);
    }

    public static int[] getMaterials(Item item) {
        return CONVERSIONS.getOrDefault(item, new int[]{0, 0, 0});
    }

    public static boolean canConvert(Item item) {
        return CONVERSIONS.containsKey(item);
    }

    /**
     * Helper class for registering conversions in MaterialConversionRegistry
     */
    public static class RegistrationHelper {
        /**
         * Register a modded item by resource location (e.g., "create:brass_ingot")
         */
        public void registerByName(String itemId, int brass, int electronics, int glass) {
            try {
                ResourceLocation loc = ResourceLocation.parse(itemId);
                Item item = BuiltInRegistries.ITEM.get(loc);
                if (item != Items.AIR) {
                    CONVERSIONS.put(item, new int[]{brass, electronics, glass});
                }
            } catch (Exception e) {
                // Item not found or mod not loaded - skip silently
            }
        }

        /**
         * Register a vanilla Minecraft item by name (e.g., "REDSTONE", "GLASS")
         */
        public void registerVanilla(String itemName, int brass, int electronics, int glass) {
            try {
                Item item = (Item) Items.class.getField(itemName).get(null);
                CONVERSIONS.put(item, new int[]{brass, electronics, glass});
            } catch (Exception e) {
                // Item not found - skip silently
            }
        }
    }
}
