package net.blackredcoded.brassmanmod.client.screen;

import net.blackredcoded.brassmanmod.items.BrassManChestplateItem;
import net.blackredcoded.brassmanmod.network.CallSuitPacket;
import net.blackredcoded.brassmanmod.network.RenameCompressorPacket;
import net.minecraft.client.gui.components.EditBox;
import net.blackredcoded.brassmanmod.menu.AirCompressorMenu;
import net.blackredcoded.brassmanmod.network.ConvertMaterialsPacket;
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

public class AirCompressorScreen extends AbstractContainerScreen<AirCompressorMenu> {

    private int selectedArmorSlot = -1;
    private int hoveredArmorSlot = -1;
    private EditBox nameField;

    public AirCompressorScreen(AirCompressorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 5;
    }

    @Override
    protected void init() {
        super.init();
        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;

        // ===== NAME TEXT FIELD =====
        nameField = new EditBox(this.font, leftPos + 2, topPos + 2, 100, 12, Component.literal("Compressor Name"));
        nameField.setMaxLength(16);
        nameField.setValue(menu.getBlockEntity().getCustomName().getString());
        nameField.setTextColor(0xFFFFFF);
        nameField.setBordered(false);
        nameField.setResponder(this::onNameChanged);
        this.addRenderableWidget(nameField);

        // ===== EXISTING BUTTONS =====
        this.addRenderableWidget(Button.builder(
                        Component.literal("Convert"),
                        button -> ConvertMaterialsPacket.send(this.menu.getBlockEntity().getBlockPos()))
                .bounds(leftPos + 30, topPos + 43, 50, 18)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.literal("Repair"),
                        button -> {
                            if (selectedArmorSlot >= 0 && selectedArmorSlot <= 3) {
                                RepairArmorPacket.send(this.menu.getBlockEntity().getBlockPos(), selectedArmorSlot);
                            }
                        })
                .bounds(leftPos + 90, topPos + 43, 60, 18)
                .build());
    }

    private void onNameChanged(String newName) {
        if (!newName.trim().isEmpty()) {
            RenameCompressorPacket.send(menu.getBlockEntity().getBlockPos(), Component.literal(newName));
        }
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

        // Name field background
        guiGraphics.fill(x + 7, y + 5, x + 169, y + 28, 0xFFC6C6C6);

        // Input slot (slot 0)
        renderSlot(guiGraphics, x + 7, y + 43);

        // Charging slot (slot 1)
        renderSlot(guiGraphics, x + 127, y + 23);

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

        // Player inventory slots
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                renderSlot(guiGraphics, x + 7 + col * 18, y + 83 + row * 18);
            }
        }

        // Player hotbar slots
        for (int col = 0; col < 9; ++col) {
            renderSlot(guiGraphics, x + 7 + col * 18, y + 141);
        }
    }

    private void renderSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 1, 0xFF373737);
        guiGraphics.fill(x, y, x + 1, y + 18, 0xFF373737);
        guiGraphics.fill(x + 17, y, x + 18, y + 18, 0xFFFFFFFF);
        guiGraphics.fill(x, y + 17, x + 18, y + 18, 0xFFFFFFFF);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
    }

    private void renderArmorSlot(GuiGraphics guiGraphics, int x, int y, int slot) {
        guiGraphics.fill(x, y, x + 18, y + 1, 0xFF373737);
        guiGraphics.fill(x, y, x + 1, y + 18, 0xFF373737);
        guiGraphics.fill(x + 17, y, x + 18, y + 18, 0xFFFFFFFF);
        guiGraphics.fill(x, y + 17, x + 18, y + 18, 0xFFFFFFFF);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);

        if (selectedArmorSlot == slot) {
            guiGraphics.fill(x, y, x + 18, y + 18, 0x8800FF00);
        }

        // Draw vertical bars OUTSIDE the chestplate slot (slot 1)
        if (slot == 1) {
            ItemStack chestplate = menu.getArmorStacks()[1];
            if (chestplate.getItem() instanceof BrassManChestplateItem chestItem) {
                int air = menu.getArmorAir();
                int power = menu.getArmorPower();
                int maxAir = menu.getArmorMaxAir();
                int maxPower = menu.getArmorMaxPower();

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
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(1.2F, 1.2F, 1.2F);
        guiGraphics.pose().popPose();
        guiGraphics.drawString(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
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

        // Render armor tooltip if hovering
        if (hoveredArmorSlot >= 0 && hoveredArmorSlot <= 3) {
            ItemStack hoveredArmor = armorStacks[hoveredArmorSlot];
            if (!hoveredArmor.isEmpty()) {
                List<Component> tooltip = getArmorTooltip(hoveredArmor, hoveredArmorSlot);
                guiGraphics.renderTooltip(this.font, tooltip, hoveredArmor.getTooltipImage(), mouseX, mouseY);
            }
        } else {
            renderTooltip(guiGraphics, mouseX, mouseY);
        }

        // Render material counts with cost/gain overlays
        int brass = this.menu.getMaterial(MaterialConverter.BRASS);
        int electronics = this.menu.getMaterial(MaterialConverter.ELECTRONICS);
        int glass = this.menu.getMaterial(MaterialConverter.GLASS);

        int[] costs = null;
        if (selectedArmorSlot >= 0 && selectedArmorSlot <= 3) {
            costs = calculateRepairCost(selectedArmorSlot);
        }

        ItemStack inputItem = this.menu.getSlot(0).getItem();
        int[] gains = calculateMaterialGains(inputItem);

        String brassText = "Brass: " + brass;
        String electronicsText = "Electronics: " + electronics;
        String glassText = "Glass: " + glass;

        guiGraphics.drawString(this.font, brassText, x + 8, y + 12, 0x404040, false);
        guiGraphics.drawString(this.font, electronicsText, x + 8, y + 22, 0x404040, false);
        guiGraphics.drawString(this.font, glassText, x + 8, y + 32, 0x404040, false);

        int brassTextWidth = this.font.width(brassText);
        int electronicsTextWidth = this.font.width(electronicsText);
        int glassTextWidth = this.font.width(glassText);

        int currentXBrass = x + 8 + brassTextWidth;
        int currentXElectronics = x + 8 + electronicsTextWidth;
        int currentXGlass = x + 8 + glassTextWidth;

        if (costs != null) {
            if (costs[0] > 0) {
                String costText = " -" + costs[0];
                guiGraphics.drawString(this.font, costText, currentXBrass, y + 12, 0xFFCC0000, false);
                currentXBrass += this.font.width(costText);
            }
            if (costs[1] > 0) {
                String costText = " -" + costs[1];
                guiGraphics.drawString(this.font, costText, currentXElectronics, y + 22, 0xFFCC0000, false);
                currentXElectronics += this.font.width(costText);
            }
            if (costs[2] > 0) {
                String costText = " -" + costs[2];
                guiGraphics.drawString(this.font, costText, currentXGlass, y + 32, 0xFFCC0000, false);
                currentXGlass += this.font.width(costText);
            }
        }

        // FIXED: Show material gains in green (multiplied by stack count!)
        if (gains != null) {
            if (gains[0] > 0) {
                guiGraphics.drawString(this.font, " +" + gains[0], currentXBrass, y + 12, 0xFF00AA00, false);
            }
            if (gains[1] > 0) {
                guiGraphics.drawString(this.font, " +" + gains[1], currentXElectronics, y + 22, 0xFF00AA00, false);
            }
            if (gains[2] > 0) {
                guiGraphics.drawString(this.font, " +" + gains[2], currentXGlass, y + 32, 0xFF00AA00, false);
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

        if (slot == 1 && armor.getItem() instanceof BrassManChestplateItem chestItem) {
            int air = menu.getArmorAir();
            int power = menu.getArmorPower();
            int maxAir = menu.getArmorMaxAir();
            int maxPower = menu.getArmorMaxPower();

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

    // FIXED: Multiply by stack count!
    private int[] calculateMaterialGains(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        int[] materials = MaterialConverter.getMaterials(stack.getItem());
        if (materials[0] == 0 && materials[1] == 0 && materials[2] == 0) {
            return null;
        }

        // Multiply by stack count!
        int count = stack.getCount();
        return new int[]{
                materials[0] * count,
                materials[1] * count,
                materials[2] * count
        };
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (nameField != null && nameField.isFocused()) {
            if (nameField.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }

        if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (nameField.isFocused() && nameField.charTyped(codePoint, modifiers)) {
            return true;
        }

        return super.charTyped(codePoint, modifiers);
    }
}
