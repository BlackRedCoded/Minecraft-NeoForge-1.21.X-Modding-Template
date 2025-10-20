package net.blackredcoded.brassmanmod.client;

import net.blackredcoded.brassmanmod.registry.ModItems;
import net.blackredcoded.brassmanmod.util.ArmorStyleHelper;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = "brassmanmod", value = Dist.CLIENT)
public class BrassManItemProperties {

    @SubscribeEvent
    public static void registerItemProperties(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register for all brass man armor pieces
            registerArmorStyleProperty(ModItems.BRASS_MAN_CHESTPLATE.get());
            registerArmorStyleProperty(ModItems.BRASS_MAN_HELMET.get());
            registerArmorStyleProperty(ModItems.BRASS_MAN_LEGGINGS.get());
            registerArmorStyleProperty(ModItems.BRASS_MAN_BOOTS.get());
        });
    }

    private static void registerArmorStyleProperty(Item item) {
        ItemProperties.register(item,
                ResourceLocation.fromNamespaceAndPath("brassmanmod", "style"),
                (stack, level, entity, seed) -> {
                    String style = ArmorStyleHelper.getArmorStyle(stack);
                    return switch (style) {
                        case ArmorStyleHelper.AQUA -> 1.0f;      // Iced Brass
                        case ArmorStyleHelper.DARK_AQUA -> 2.0f;   // Ocean Brass
                        case ArmorStyleHelper.FLAMING -> 3.0f;    // Flaming Brass
                        default -> 0.0f; // Default brass
                    };
                }
        );
    }
}
