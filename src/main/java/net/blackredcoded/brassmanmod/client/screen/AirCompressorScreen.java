package net.blackredcoded.brassmanmod.client.screen;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.menu.AirCompressorMenu;
import net.blackredcoded.brassmanmod.network.ConvertMaterialsPacket;
import net.blackredcoded.brassmanmod.network.RepairArmorPacket;
import net.blackredcoded.brassmanmod.registry.MaterialConverter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AirCompressorScreen extends AbstractContainerScreen<AirCompressorMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, "textures/gui/air_compressor.png");

    public AirCompressorScreen(AirCompressorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.imageWidth = 206;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 5;
    }

    @Override
    protected void init() {
        super.init();

        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;

        // Convert button - back to original position
        this.addRenderableWidget(Button.builder(
                        Component.literal("Convert"),
                        button -> ConvertMaterialsPacket.send(this.menu.getBlockEntity().getBlockPos()))
                .bounds(leftPos + 30, topPos + 48, 50, 20)
                .build());

        // Repair buttons - back to original positions
        this.addRenderableWidget(Button.builder(
                        Component.literal("Repair Helmet"),
                        button -> RepairArmorPacket.send(this.menu.getBlockEntity().getBlockPos(), 3))
                .bounds(leftPos + 110, topPos + 2, 85, 18)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.literal("Repair Chest"),
                        button -> RepairArmorPacket.send(this.menu.getBlockEntity().getBlockPos(), 2))
                .bounds(leftPos + 110, topPos + 22, 85, 18)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.literal("Repair Legs"),
                        button -> RepairArmorPacket.send(this.menu.getBlockEntity().getBlockPos(), 1))
                .bounds(leftPos + 110, topPos + 42, 85, 18)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.literal("Repair Boots"),
                        button -> RepairArmorPacket.send(this.menu.getBlockEntity().getBlockPos(), 0))
                .bounds(leftPos + 110, topPos + 62, 85, 18)
                .build());
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

        // Draw slot backgrounds - input slot at original position
        renderSlot(guiGraphics, x + 7, y + 49); // Input slot - original position

        // Player inventory slots - keep the current spacing
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

        // Render material counts - back to original positions
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        int brass = this.menu.getMaterial(MaterialConverter.BRASS);
        int electronics = this.menu.getMaterial(MaterialConverter.ELECTRONICS);
        int glass = this.menu.getMaterial(MaterialConverter.GLASS);

        guiGraphics.drawString(this.font, "Brass: " + brass, x + 8, y + 18, 0x404040, false);
        guiGraphics.drawString(this.font, "Electronics: " + electronics, x + 8, y + 28, 0x404040, false);
        guiGraphics.drawString(this.font, "Glass: " + glass, x + 8, y + 38, 0x404040, false);
    }
}
