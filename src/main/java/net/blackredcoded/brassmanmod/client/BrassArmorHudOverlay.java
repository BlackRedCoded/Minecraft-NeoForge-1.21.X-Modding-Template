package net.blackredcoded.brassmanmod.client;

import net.blackredcoded.brassmanmod.config.FlightConfig;
import net.blackredcoded.brassmanmod.items.BrassChestplateItem;
import net.blackredcoded.brassmanmod.items.BrassHelmetItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class BrassArmorHudOverlay implements LayeredDraw.Layer {

    private static final Minecraft minecraft = Minecraft.getInstance();

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (minecraft.player == null) return;

        ItemStack helmet = minecraft.player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(helmet.getItem() instanceof BrassHelmetItem)) return;

        ItemStack chestplate = minecraft.player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chestplate.getItem() instanceof BrassChestplateItem brassChest)) return;

        int power = brassChest.power(chestplate);
        int air = brassChest.air(chestplate);

        if (power <= 0) return;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        // FIXED: Pass chestplate to get upgraded max values
        renderPowerAndAir(guiGraphics, power, air, chestplate, screenWidth, screenHeight);
        renderArmorDurability(guiGraphics, minecraft.player, screenWidth, screenHeight);
        renderFlightConfig(guiGraphics, minecraft.player, screenWidth, screenHeight);
        renderEntityInfo(guiGraphics, screenWidth, screenHeight);
    }

    // FIXED: Added chestplate parameter
    private void renderPowerAndAir(GuiGraphics guiGraphics, int power, int air, ItemStack chestplate, int width, int height) {
        int x = 7;
        int y = 7;

        // Get upgraded max values
        int maxPower = BrassChestplateItem.getMaxPower(chestplate);
        int maxAir = BrassChestplateItem.getMaxAir(chestplate);

        guiGraphics.fill(x - 2, y - 2, x + 108, y + 45, 0xAA000000);

        guiGraphics.drawString(minecraft.font, "POWER: " + power + "/" + maxPower, x, y, 0x00FFFF, true);
        drawBar(guiGraphics, x, y + 12, 100, 6, power, maxPower, 0xFF0088FF);

        guiGraphics.drawString(minecraft.font, "AIR: " + air + "/" + maxAir, x, y + 25, 0x00FFFF, true);
        drawBar(guiGraphics, x, y + 37, 100, 6, air, maxAir, 0xFF00FF88);
    }

    private void renderArmorDurability(GuiGraphics guiGraphics, Player player, int width, int height) {
        int x = 10;
        int y = height - 90;
        int armorCount = 0;

        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
            ItemStack armor = player.getItemBySlot(slot);
            if (!armor.isEmpty() && armor.getMaxDamage() > 0) {
                armorCount++;
            }
        }

        int bgHeight = 17 + (armorCount * 18);
        guiGraphics.fill(x - 2, y - 2, x + 140, y + bgHeight, 0xAA000000);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(1.3F, 1.3F, 1.3F);
        guiGraphics.drawString(minecraft.font, "ARMOR", (int)(x / 1.3F), (int)(y / 1.3F), 0xFFFFFF, true);
        guiGraphics.pose().popPose();

        y += 15;

        EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        String[] armorNames = {"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"};

        for (int i = 0; i < 4; i++) {
            ItemStack armor = player.getItemBySlot(armorSlots[i]);
            if (!armor.isEmpty()) {
                int maxDur = armor.getMaxDamage();
                if (maxDur > 0) {
                    int dur = maxDur - armor.getDamageValue();
                    guiGraphics.drawString(minecraft.font, armorNames[i] + ": " + dur + "/" + maxDur, x, y, 0xFFFFFF, true);
                    drawBar(guiGraphics, x, y + 10, 130, 4, dur, maxDur, 0xFFFFAA00);
                    y += 18;
                }
            }
        }
    }

    private void renderFlightConfig(GuiGraphics guiGraphics, Player player, int width, int height) {
        FlightConfig.PlayerFlightData config = FlightConfig.get(player);

        int x = width - 100;
        int y = height - 55;

        guiGraphics.fill(x - 2, y - 2, x + 98, y + 52, 0xAA000000);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(1.3F, 1.3F, 1.3F);
        guiGraphics.drawString(minecraft.font, "FLIGHT", (int)(x / 1.3F), (int)(y / 1.3F), 0x00FFFF, true);
        guiGraphics.pose().popPose();

        y += 15;

        guiGraphics.drawString(minecraft.font, "Speed: " + config.flightSpeed + "%", x, y, 0xFFFFFF, true);
        y += 12;

        String flightStatus = config.flightEnabled ? "ON" : "OFF";
        int flightColor = config.flightEnabled ? 0x00FF00 : 0xFF0000;
        guiGraphics.drawString(minecraft.font, "Flight: " + flightStatus, x, y, flightColor, true);
        y += 12;

        String hoverStatus = config.hoverEnabled ? "ON" : "OFF";
        int hoverColor = config.hoverEnabled ? 0x00FF00 : 0xFF0000;
        guiGraphics.drawString(minecraft.font, "Hover: " + hoverStatus, x, y, hoverColor, true);
    }

    private void renderEntityInfo(GuiGraphics guiGraphics, int width, int height) {
        HitResult hitResult = minecraft.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hitResult;
            Entity entity = entityHit.getEntity();

            if (entity instanceof LivingEntity living) {
                int x = width - 140;
                int y = 7;

                guiGraphics.fill(x - 2, y - 2, x + 138, y + 30, 0xAA000000);

                String name = entity.getName().getString();
                guiGraphics.drawString(minecraft.font, "TARGET: " + name, x, y, 0xFF5555, true);

                float health = living.getHealth();
                float maxHealth = living.getMaxHealth();
                String healthText = "HP: " + formatHealth(health) + "/" + formatHealth(maxHealth);
                guiGraphics.drawString(minecraft.font, healthText, x, y + 12, 0xFF5555, true);

                ResourceLocation entityType = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                String modId = entityType.getNamespace();
                String modName = getModName(modId);
                guiGraphics.drawString(minecraft.font, "From: " + modName, x, y + 24, 0xAAAAAA, true);
            }
        }
    }

    private String formatHealth(float health) {
        if (health == (int) health) {
            return String.valueOf((int) health);
        } else {
            return String.format(java.util.Locale.US, "%.1f", health);
        }
    }

    private void drawBar(GuiGraphics guiGraphics, int x, int y, int width, int height, int current, int max, int color) {
        if (max <= 0) return;

        guiGraphics.fill(x, y, x + width, y + height, 0xFF222222);
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y, 0xFF666666);
        guiGraphics.fill(x - 1, y + height, x + width + 1, y + height + 1, 0xFF666666);
        guiGraphics.fill(x - 1, y, x, y + height, 0xFF666666);
        guiGraphics.fill(x + width, y, x + width + 1, y + height, 0xFF666666);

        float percentage = Math.max(0.0f, Math.min(1.0f, (float) current / (float) max));
        int fillWidth = Math.max(1, (int) (percentage * width));

        if (current > 0 && fillWidth > 0) {
            guiGraphics.fill(x, y, x + fillWidth, y + height, color);
        }
    }

    private String getModName(String modId) {
        return switch (modId) {
            case "minecraft" -> "Minecraft";
            case "create" -> "Create";
            default -> modId.substring(0, 1).toUpperCase() + modId.substring(1);
        };
    }
}
