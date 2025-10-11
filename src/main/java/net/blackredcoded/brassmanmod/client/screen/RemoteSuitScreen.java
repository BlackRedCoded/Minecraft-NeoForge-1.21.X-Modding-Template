package net.blackredcoded.brassmanmod.client.screen;

import net.blackredcoded.brassmanmod.items.BrassChestplateItem;
import net.blackredcoded.brassmanmod.menu.RemoteSuitMenu;
import net.blackredcoded.brassmanmod.network.CallSuitPacket;
import net.blackredcoded.brassmanmod.network.RepairArmorPacket;
import net.blackredcoded.brassmanmod.registry.MaterialConverter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RemoteSuitScreen extends AbstractContainerScreen<RemoteSuitMenu> {

    private int selectedArmorSlot = -1;
    private int hoveredArmorSlot = -1;

    public RemoteSuitScreen(RemoteSuitMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 100;
        this.imageWidth = 176;
        this.inventoryLabelY = 1000; // Hide inventory label
        this.titleLabelY = 5;
    }

    @Override
    protected void init() {
        super.init();
        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;

        // Repair Button
        this.addRenderableWidget(Button.builder(
                        Component.literal("Repair"),
                        button -> {
                            if (selectedArmorSlot >= 0 && selectedArmorSlot <= 3) {
                                RepairArmorPacket.send(menu.getCompressorPos(), selectedArmorSlot);
                            }
                        })
                .bounds(leftPos + 8, topPos + 73, 60, 18)
                .build());

        // Call Suit Button
        this.addRenderableWidget(Button.builder(
                        Component.literal("Call Suit"),
                        button -> {
                            // Check if any armor exists on the stand
                            ItemStack[] armorStacks = menu.getArmorStacks();
                            boolean hasArmor = false;
                            for (ItemStack stack : armorStacks) {
                                if (!stack.isEmpty()) {
                                    hasArmor = true;
                                    break;
                                }
                            }

                            if (hasArmor) {
                                CallSuitPacket packet = new CallSuitPacket(menu.getCompressorPos());
                                PacketDistributor.sendToServer(packet);
                                this.onClose(); // Close GUI after calling suit
                            } else {
                                if (minecraft != null && minecraft.player != null) {
                                    minecraft.player.displayClientMessage(
                                            Component.literal("No armor on stand!").withStyle(style -> style.withColor(0xFF0000)),
                                            true
                                    );
                                }
                            }
                        })
                .bounds(leftPos + 108, topPos + 73, 60, 18)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Main background
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFFC6C6C6);

        // Border
        guiGraphics.fill(x, y, x + this.imageWidth, y + 1, 0xFF8B8B8B);
        guiGraphics.fill(x, y, x + 1, y + this.imageHeight, 0xFF8B8B8B);
        guiGraphics.fill(x + this.imageWidth - 1, y, x + this.imageWidth, y + this.imageHeight, 0xFFFFFFFF);
        guiGraphics.fill(x, y + this.imageHeight - 1, x + this.imageWidth, y + this.imageHeight, 0xFFFFFFFF);

        // Armor slots
        hoveredArmorSlot = -1;
        for (int i = 0; i < 4; i++) {
            int slotX = x + 151;
            int slotY = y + 3 + (i * 20);
            renderArmorSlot(guiGraphics, slotX, slotY, i);
            if (mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18) {
                hoveredArmorSlot = i;
            }
        }
    }

    private void renderArmorSlot(GuiGraphics guiGraphics, int x, int y, int slot) {
        // Slot background
        guiGraphics.fill(x, y, x + 18, y + 1, 0xFF373737);
        guiGraphics.fill(x, y, x + 1, y + 18, 0xFF373737);
        guiGraphics.fill(x + 17, y, x + 18, y + 18, 0xFFFFFFFF);
        guiGraphics.fill(x, y + 17, x + 18, y + 18, 0xFFFFFFFF);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);

        // Highlight selected slot
        if (selectedArmorSlot == slot) {
            guiGraphics.fill(x, y, x + 18, y + 18, 0x8800FF00);
        }

        // Draw vertical bars for chestplate (slot 1)
        if (slot == 1) {
            ItemStack chestplate = menu.getArmorStacks()[1];
            if (chestplate.getItem() instanceof BrassChestplateItem chestItem) {
                int air = chestItem.air(chestplate);
                int maxAir = BrassChestplateItem.getMaxAir(chestplate);
                int power = chestItem.power(chestplate);
                int maxPower = BrassChestplateItem.getMaxPower(chestplate);

                int airBarHeight = maxAir > 0 ? (int) ((float) air / maxAir * 16) : 0;
                int powerBarHeight = maxPower > 0 ? (int) ((float) power / maxPower * 16) : 0;

                // LEFT BAR: Air (Cyan)
                int leftBarX = x - 4;
                int barY = y + 1;
                guiGraphics.fill(leftBarX - 1, barY - 1, leftBarX + 3, barY + 17, 0xFFFFFFFF);
                guiGraphics.fill(leftBarX, barY, leftBarX + 2, barY + 16, 0xFF444444);
                if (airBarHeight > 0) {
                    guiGraphics.fill(leftBarX, barY + 16 - airBarHeight, leftBarX + 2, barY + 16, 0xFF00FFFF);
                }

                // RIGHT BAR: Power (Yellow)
                int rightBarX = x + 20;
                guiGraphics.fill(rightBarX - 1, barY - 1, rightBarX + 3, barY + 17, 0xFFFFFFFF);
                guiGraphics.fill(rightBarX, barY, rightBarX + 2, barY + 16, 0xFF444444);
                if (powerBarHeight > 0) {
                    guiGraphics.fill(rightBarX, barY + 16 - powerBarHeight, rightBarX + 2, barY + 16, 0xFFFFD700);
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Render title
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Render armor items
        ItemStack[] armorStacks = menu.getArmorStacks();
        for (int i = 0; i < 4; i++) {
            ItemStack stack = armorStacks[i];
            if (!stack.isEmpty()) {
                guiGraphics.renderItem(stack, x + 152, y + 4 + (i * 20));
                guiGraphics.renderItemDecorations(this.font, stack, x + 152, y + 4 + (i * 20));
            }
        }

        // Render material counts with cost overlays
        int brass = this.menu.getMaterial(MaterialConverter.BRASS);
        int electronics = this.menu.getMaterial(MaterialConverter.ELECTRONICS);
        int glass = this.menu.getMaterial(MaterialConverter.GLASS);

        int[] costs = null;
        if (selectedArmorSlot >= 0 && selectedArmorSlot <= 3) {
            costs = calculateRepairCost(selectedArmorSlot);
        }

        String brassText = "Brass: " + brass;
        String electronicsText = "Electronics: " + electronics;
        String glassText = "Glass: " + glass;

        guiGraphics.drawString(this.font, brassText, x + 8, y + 18, 0x404040, false);
        guiGraphics.drawString(this.font, electronicsText, x + 8, y + 32, 0x404040, false);
        guiGraphics.drawString(this.font, glassText, x + 8, y + 46, 0x404040, false);

        int brassTextWidth = this.font.width(brassText);
        int electronicsTextWidth = this.font.width(electronicsText);
        int glassTextWidth = this.font.width(glassText);

        if (costs != null) {
            if (costs[0] > 0) {
                guiGraphics.drawString(this.font, " -" + costs[0], x + 8 + brassTextWidth, y + 18, 0xFFCC0000, false);
            }
            if (costs[1] > 0) {
                guiGraphics.drawString(this.font, " -" + costs[1], x + 8 + electronicsTextWidth, y + 32, 0xFFCC0000, false);
            }
            if (costs[2] > 0) {
                guiGraphics.drawString(this.font, " -" + costs[2], x + 8 + glassTextWidth, y + 46, 0xFFCC0000, false);
            }
        }

        // Render armor tooltip if hovering
        if (hoveredArmorSlot >= 0 && hoveredArmorSlot <= 3) {
            ItemStack hoveredArmor = armorStacks[hoveredArmorSlot];
            if (!hoveredArmor.isEmpty()) {
                List<Component> tooltip = getArmorTooltip(hoveredArmor, hoveredArmorSlot);
                guiGraphics.renderTooltip(this.font, tooltip, hoveredArmor.getTooltipImage(), mouseX, mouseY);
            }
        }
    }

    private List<Component> getArmorTooltip(ItemStack armor, int slot) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(armor.getHoverName());

        int damage = armor.getDamageValue();
        int maxDurability = armor.getMaxDamage();
        int remaining = maxDurability - damage;

        tooltip.add(Component.literal("Durability: " + remaining + "/" + maxDurability)
                .withStyle(style -> style.withColor(damage > 0 ? 0xFFAAAA : 0xAAAAAA)));

        if (slot == 1 && armor.getItem() instanceof BrassChestplateItem chestItem) {
            int air = chestItem.air(armor);
            int maxAir = BrassChestplateItem.getMaxAir(armor);
            int power = chestItem.power(armor);
            int maxPower = BrassChestplateItem.getMaxPower(armor);

            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Air: " + air + "/" + maxAir)
                    .withStyle(style -> style.withColor(0x00FFFF)));
            tooltip.add(Component.literal("Power: " + power + "/" + maxPower)
                    .withStyle(style -> style.withColor(0xFFD700)));
        }

        return tooltip;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        for (int i = 0; i < 4; i++) {
            int slotX = x + 151;
            int slotY = y + 3 + (i * 20);
            if (mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18) {
                selectedArmorSlot = i;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int[] calculateRepairCost(int slot) {
        ItemStack stack = menu.getArmorStacks()[slot];
        if (stack.isEmpty() || !stack.isDamaged()) {
            return null;
        }

        int damageTaken = stack.getDamageValue();
        int maxDurability = stack.getMaxDamage();
        double damagePercent = (double) damageTaken / maxDurability;

        int brassCost, electronicsCost, glassCost;

        switch (slot) {
            case 0:
                brassCost = (int) Math.ceil(damagePercent * 60 / 5) * 5;
                electronicsCost = (int) Math.ceil(damagePercent * 120 / 5) * 5;
                glassCost = (int) Math.ceil(damagePercent * 30 / 5) * 5;
                break;
            case 1:
                brassCost = (int) Math.ceil(damagePercent * 120 / 5) * 5;
                electronicsCost = (int) Math.ceil(damagePercent * 180 / 5) * 5;
                glassCost = (int) Math.ceil(damagePercent * 10 / 5) * 5;
                break;
            case 2:
                brassCost = (int) Math.ceil(damagePercent * 100 / 5) * 5;
                electronicsCost = (int) Math.ceil(damagePercent * 150 / 5) * 5;
                glassCost = 0;
                break;
            case 3:
                brassCost = (int) Math.ceil(damagePercent * 50 / 5) * 5;
                electronicsCost = (int) Math.ceil(damagePercent * 90 / 5) * 5;
                glassCost = 0;
                break;
            default:
                return null;
        }

        return new int[]{brassCost, electronicsCost, glassCost};
    }
}
