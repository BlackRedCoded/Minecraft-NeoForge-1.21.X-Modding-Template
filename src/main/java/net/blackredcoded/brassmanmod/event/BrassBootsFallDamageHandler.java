package net.blackredcoded.brassmanmod.event;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.items.BrassBootsItem;
import net.blackredcoded.brassmanmod.items.BrassChestplateItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = BrassManMod.MOD_ID)
public class BrassBootsFallDamageHandler {

    @SubscribeEvent
    public static void onFallDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player)) return;

        DamageSource source = event.getSource();
        if (!source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)) return;

        // Check if wearing brass boots
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!(boots.getItem() instanceof BrassBootsItem)) return;

        // Check for chestplate with power
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chestplate.getItem() instanceof BrassChestplateItem brass)) return;

        int currentPower = brass.power(chestplate);
        if (currentPower <= 0) return;

        float originalDamage = event.getOriginalDamage();

        // Absorb up to 10 HP (5 hearts) of fall damage
        float damageToAbsorb = Math.min(originalDamage, 10.0f);

        // Calculate BASE power needed: 2 power per 1 HP
        int basePowerNeeded = (int) Math.ceil(damageToAbsorb * 2);

        // Use consumePower to apply efficiency automatically
        if (brass.consumePower(chestplate, basePowerNeeded)) {
            // Fully absorbed damage
            event.setNewDamage(originalDamage - damageToAbsorb);
        } else {
            // Partial absorption based on remaining power
            float absorbedDamage = currentPower / 2.0f;
            brass.setPower(chestplate, 0);
            event.setNewDamage(originalDamage - absorbedDamage);
        }
    }
}
