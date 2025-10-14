package net.blackredcoded.brassmanmod.client.screen;

import net.blackredcoded.brassmanmod.menu.KineticMotorMenu;
import net.blackredcoded.brassmanmod.util.BatteryHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class KineticMotorScreen extends AbstractContainerScreen<KineticMotorMenu> {

    public KineticMotorScreen(KineticMotorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
        this.inventoryLabelY = this.imageHeight - 94;
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

        // Battery slot
        renderSlot(guiGraphics, x + 79, y + 34);

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

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);

        // Player inventory label
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);

        // Battery stats
        // Battery stats
        ItemStack battery = this.menu.slots.get(0).getItem();  // Changed from getSlots() to slots
        if (BatteryHelper.isBatteryItem(battery)) {
            int charge = BatteryHelper.getBatteryCharge(battery);
            int maxCharge = BatteryHelper.getMaxBatteryCharge(battery);

            String chargeText = "Energy: " + charge + "/" + maxCharge + " SU";
            guiGraphics.drawString(this.font, chargeText, 8, 20, 0x404040, false);

            // Show percentage
            if (maxCharge > 0) {
                int percent = (int) ((float) charge / maxCharge * 100);
                String percentText = "Charge: " + percent + "%";
                guiGraphics.drawString(this.font, percentText, 8, 30, 0x404040, false);
            }

            // Runtime estimate (at max load of 16 SU/sec)
            if (charge > 0) {
                int secondsRemaining = charge / 16;
                int minutes = secondsRemaining / 60;
                int seconds = secondsRemaining % 60;
                String runtimeText = String.format("Runtime: ~%d:%02d", minutes, seconds);
                guiGraphics.drawString(this.font, runtimeText, 8, 55, 0x404040, false);
            }
        } else {
            guiGraphics.drawString(this.font, "No Battery Inserted", 8, 20, 0xFF0000, false);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
