package net.blackredcoded.brassmanmod.client.renderer;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.util.ArmorStyleHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class BrassArmorRenderer {

    public static ResourceLocation getArmorTexture(ItemStack stack, EquipmentSlot slot, boolean isInner) {
        String style = ArmorStyleHelper.getArmorStyle(stack);
        String layer = isInner ? "layer_2" : "layer_1";

        String textureName = switch (style) {
            case ArmorStyleHelper.AQUA -> "aquabrass_man";
            case ArmorStyleHelper.DARK_AQUA -> "darkaquabrass_man";
            case ArmorStyleHelper.FLAMING -> "flamingbrass_man";
            default -> "brass_man";
        };

        return ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID,
                "textures/models/armor/" + textureName + "_" + layer + ".png");
    }
}
