package net.blackredcoded.brassmanmod.registry;

import com.simibubi.create.Create;
import net.blackredcoded.brassmanmod.BrassManMod;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class ModArmorMaterials {

    public static final Holder<ArmorMaterial> BRASS = register("brass",
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 2);
                map.put(ArmorItem.Type.LEGGINGS, 5);
                map.put(ArmorItem.Type.CHESTPLATE, 6);
                map.put(ArmorItem.Type.HELMET, 2);
                map.put(ArmorItem.Type.BODY, 5);
            }),
            10,
            SoundEvents.ARMOR_EQUIP_IRON,
            1.0F,
            0.0F,
            () -> Ingredient.of(BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("create", "brass_ingot")
            ))
    );

    private static Holder<ArmorMaterial> register(String name, EnumMap<ArmorItem.Type, Integer> defense,
                                                  int enchantmentValue, Holder<SoundEvent> equipSound,
                                                  float toughness, float knockbackResistance,
                                                  Supplier<Ingredient> repairIngredient) {
        List<ArmorMaterial.Layer> layers = List.of(
                new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, name))
        );

        return Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL,
                ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, name),
                new ArmorMaterial(defense, enchantmentValue, equipSound, repairIngredient, layers,
                        toughness, knockbackResistance));
    }


    public static final Holder<ArmorMaterial> BRASS_MAN = register("brass_man",
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 2);
                map.put(ArmorItem.Type.LEGGINGS, 5);
                map.put(ArmorItem.Type.CHESTPLATE, 6);
                map.put(ArmorItem.Type.HELMET, 2);
                map.put(ArmorItem.Type.BODY, 5);
            }),
            15,
            SoundEvents.ARMOR_EQUIP_IRON,
            0.0F,
            0.0F,
            () -> Ingredient.of(Items.COPPER_INGOT),
            List.of(new ArmorMaterial.Layer(
                    ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, "brass_man")
            ))
    );

    // Separate material for communicator with different texture
    public static final Holder<ArmorMaterial> COMMUNICATOR = register("communicator",
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 0);
                map.put(ArmorItem.Type.LEGGINGS, 0);
                map.put(ArmorItem.Type.CHESTPLATE, 0);
                map.put(ArmorItem.Type.HELMET, 0); // No armor value, just wearable
                map.put(ArmorItem.Type.BODY, 0);
            }),
            0,
            SoundEvents.ARMOR_EQUIP_GENERIC,
            0.0F,
            0.0F,
            () -> Ingredient.of(Items.REDSTONE),
            List.of(new ArmorMaterial.Layer(
                    ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, "communicator")
            ))
    );

    private static Holder<ArmorMaterial> register(String name, EnumMap<ArmorItem.Type, Integer> defense,
                                                  int enchantmentValue, Holder<net.minecraft.sounds.SoundEvent> equipSound,
                                                  float toughness, float knockbackResistance,
                                                  java.util.function.Supplier<Ingredient> repairIngredient,
                                                  List<ArmorMaterial.Layer> layers) {
        EnumMap<ArmorItem.Type, Integer> defenseMap = new EnumMap<>(ArmorItem.Type.class);
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            defenseMap.put(type, defense.get(type));
        }

        return Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL,
                ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, name),
                new ArmorMaterial(defenseMap, enchantmentValue, equipSound, repairIngredient, layers,
                        toughness, knockbackResistance));
    }
}
