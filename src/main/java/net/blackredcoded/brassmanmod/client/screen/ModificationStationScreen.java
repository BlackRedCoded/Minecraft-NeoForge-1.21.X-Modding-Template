package net.blackredcoded.brassmanmod.client.screen;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.menu.ModificationStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ModificationStationScreen extends AbstractContainerScreen<ModificationStationMenu> {

    public ModificationStationScreen(ModificationStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 5;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Draw background
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFFC6C6C6);

        // Draw border
        guiGraphics.fill(x, y, x + this.imageWidth, y + 1, 0xFF8B8B8B);
        guiGraphics.fill(x, y, x + 1, y + this.imageHeight, 0xFF8B8B8B);
        guiGraphics.fill(x + this.imageWidth - 1, y, x + this.imageWidth, y + this.imageHeight, 0xFFFFFFFF);
        guiGraphics.fill(x, y + this.imageHeight - 1, x + this.imageWidth, y + this.imageHeight, 0xFFFFFFFF);

        // Draw slot backgrounds
        renderSlot(guiGraphics, x + 26, y + 46);  // Armor slot
        renderSlot(guiGraphics, x + 75, y + 46);  // Upgrade slot
        renderSlot(guiGraphics, x + 133, y + 46); // Result slot

        var poseStack = guiGraphics.pose();

        // Draw bigger plus sign with scaling
        poseStack.pushPose();
        poseStack.translate(205, 90, 0);
        poseStack.scale(2.0f, 2.0f, 1.0f);
        guiGraphics.drawString(font, "+", 0, 0, 0x999999, false); // Light gray Plus
        poseStack.popPose();

        // Draw arrow manually (simple text-based arrow)
        poseStack.pushPose();
        poseStack.translate(260, 90, 0);
        poseStack.scale(2.0f, 2.0f, 1.0f);
        guiGraphics.drawString(font, "→", 0, 0, 0x999999, false); // Light gray Arrow
        poseStack.popPose();

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
        // Render title with larger scale
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(1.2F, 1.2F, 1.2F);
        guiGraphics.drawString(this.font, this.title,
                (int)(this.titleLabelX / 1.2F), (int)(this.titleLabelY / 1.2F), 0x404040, false);
        guiGraphics.pose().popPose();

        // Render "Inventory" label
        guiGraphics.drawString(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
