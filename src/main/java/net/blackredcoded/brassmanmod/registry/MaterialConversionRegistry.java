package net.blackredcoded.brassmanmod.registry;

/**
 * Central registry for all item-to-material conversion rates.
 * Edit this file to add, remove, or modify conversion rates.
 *
 * Format: registerByName("mod_id:item_name", brass, electronics, glass)
 *         register(Items.ITEM_NAME, brass, electronics, glass)
 */
public class MaterialConversionRegistry {
    public static void registerConversions(MaterialConverter.RegistrationHelper helper) {
        // CREATE MOD ITEMS
        // --- Brass Materials ---
        helper.registerByName("create:brass_ingot", 15, 0, 0);      // Main brass source
        helper.registerByName("create:brass_nugget", 5, 0, 0);      // Small brass amount
        helper.registerByName("create:brass_sheet", 10, 0, 0);       // Brass sheet
        helper.registerByName("create:brass_block", 135, 0, 0);      // 9 ingots worth

        helper.registerByName("create:brass_casing", 10, 0, 0);     // Brass casing
        helper.registerByName("create:brass_tunnel", 10, 0, 0);     // Brass tunnel
        helper.registerByName("create:brass_funnel", 5, 0, 0);      // Brass funnel

        // --- Electronics / Mechanisms ---
        helper.registerByName("create:precision_mechanism", 15, 25, 0);   // Complex mechanism
        helper.registerByName("create:electron_tube", 0, 15, 0);         // Electronics component
        helper.registerByName("create:clockwork_mechanism", 3, 12, 0);   // Mechanism with brass

        helper.registerByName("create:mechanical_arm", 50, 50, 0);        // Advanced mechanical
        helper.registerByName("create:mechanical_saw", 0, 15, 0);       // Mechanical tool
        helper.registerByName("create:mechanical_drill", 0, 15, 0);     // Mechanical tool
        helper.registerByName("create:mechanical_harvester", 0, 5, 0); // Mechanical tool
        helper.registerByName("create:mechanical_plough", 0, 5, 0);    // Mechanical tool

        helper.registerByName("create:deployer", 15, 20, 0);             // Complex mechanism
        helper.registerByName("create:mechanical_crafter", 25, 25, 0);    // Crafter mechanism

        // --- Copper (can be converted to brass) ---
        helper.registerByName("create:copper_sheet", 5, 0, 0);           // Copper sheet

        // ============================================
        // MINECRAFT ITEMS
        // ============================================

        // --- Electronics (Redstone) ---
        helper.registerVanilla("REPEATER", 0, 10, 0);             // Redstone device
        helper.registerVanilla("COMPARATOR", 0, 15, 0);          // Advanced redstone
        helper.registerVanilla("OBSERVER", 0, 10, 0);            // Redstone sensor
        helper.registerVanilla("PISTON", 0, 5, 0);               // Mechanical + redstone
        helper.registerVanilla("STICKY_PISTON", 0, 5, 0);        // More complex
        helper.registerVanilla("DAYLIGHT_DETECTOR", 0, 10, 5);   // Sensor + glass

        // --- Glass ---
        helper.registerVanilla("GLASS", 0, 0, 10);               // Standard glass
        helper.registerVanilla("GLASS_PANE", 0, 0, 5);           // Small glass
        helper.registerVanilla("TINTED_GLASS", 0, 0, 5);         // Special glass
        helper.registerVanilla("WHITE_STAINED_GLASS", 0, 0, 10); // Colored glass
        helper.registerVanilla("ORANGE_STAINED_GLASS", 0, 0, 10);
        helper.registerVanilla("MAGENTA_STAINED_GLASS", 0, 0, 10);
        helper.registerVanilla("LIGHT_BLUE_STAINED_GLASS", 0, 0, 10);
        helper.registerVanilla("YELLOW_STAINED_GLASS", 0, 0, 10);
        helper.registerVanilla("LIME_STAINED_GLASS", 0, 0, 10);
        helper.registerVanilla("PINK_STAINED_GLASS", 0, 0, 10);
        helper.registerVanilla("GRAY_STAINED_GLASS", 0, 0, 10);
        helper.registerVanilla("LIGHT_GRAY_STAINED_GLASS", 0, 0, 10);
        helper.registerVanilla("CYAN_STAINED_GLASS", 0, 0, 10);
        helper.registerVanilla("PURPLE_STAINED_GLASS", 0, 0, 10);
        helper.registerVanilla("BLUE_STAINED_GLASS", 0, 0, 10);
        helper.registerVanilla("BROWN_STAINED_GLASS", 0, 0, 10);
        helper.registerVanilla("GREEN_STAINED_GLASS", 0, 0, 10);
        helper.registerVanilla("RED_STAINED_GLASS", 0, 0, 10);
        helper.registerVanilla("BLACK_STAINED_GLASS", 0, 0, 10);

        // --- Metal Items (brass alternative) ---
        helper.registerVanilla("COPPER_INGOT", 5, 0, 0);         // Half brass value
        helper.registerVanilla("GOLD_INGOT", 10, 0, 0);           // Higher value

        // --- Misc Items ---
        helper.registerVanilla("CLOCK", 5, 10, 0);                // Gold + redstone
        helper.registerVanilla("COMPASS", 0, 5, 0);              // Iron + redstone
        helper.registerVanilla("HOPPER", 0, 5, 0);              // Iron mechanism
        helper.registerVanilla("DROPPER", 0, 5, 0);              // Cobble + redstone
        helper.registerVanilla("DISPENSER", 0, 10, 0);           // Cobble + bow + redstone

        // ============================================
        // CUSTOM ITEMS HERE
        // ============================================

        // Example:
        helper.registerByName("brassmanmod:kinetic_battery", 25, 30, 5);
    }
}
