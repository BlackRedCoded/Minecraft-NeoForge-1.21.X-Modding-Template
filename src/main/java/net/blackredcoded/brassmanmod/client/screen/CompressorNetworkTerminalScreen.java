package net.blackredcoded.brassmanmod.client.screen;

import net.blackredcoded.brassmanmod.menu.CompressorNetworkTerminalMenu;
import net.blackredcoded.brassmanmod.network.OpenRemoteCompressorPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class CompressorNetworkTerminalScreen extends AbstractContainerScreen<CompressorNetworkTerminalMenu> {
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTONS_PER_PAGE = 12;
    private static final int COLUMN_SPACING = 130;

    private int currentPage = 0;
    private int maxPages = 1;
    private Button nextPageButton;
    private Button prevPageButton;
    private static final ResourceLocation STATUS_GREEN = ResourceLocation.fromNamespaceAndPath("brassmanmod", "textures/gui/status_green.png");
    private static final ResourceLocation STATUS_RED = ResourceLocation.fromNamespaceAndPath("brassmanmod", "textures/gui/status_red.png");

    public CompressorNetworkTerminalScreen(CompressorNetworkTerminalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 276;
        this.imageHeight = 166;
        this.inventoryLabelY = 10000;
        this.titleLabelY = 8;
    }

    @Override
    protected void init() {
        super.init();

        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;

        List<BlockPos> positions = menu.getCompressorPositions();
        List<Component> names = menu.getCompressorNames();
        List<Boolean> powerStatus = menu.getCompressorPowerStatus(); // NEW: Get power status

        maxPages = (int) Math.ceil(positions.size() / 12.0);
        if (maxPages == 0) maxPages = 1;

        // Create 12 buttons (6 rows x 2 columns)
        for (int i = 0; i < BUTTONS_PER_PAGE; i++) {
            int column = i / 6;
            int row = i % 6;

            int buttonX = leftPos + 8 + (column * COLUMN_SPACING);
            int buttonY = topPos + 20 + (row * 22);

            final int globalIndex = currentPage * BUTTONS_PER_PAGE + i;

            Component buttonLabel;
            boolean enabled;

            if (globalIndex < positions.size() && globalIndex < names.size()) {
                buttonLabel = names.get(globalIndex);
                enabled = true;
            } else {
                buttonLabel = Component.literal("---");
                enabled = false;
            }

            this.addRenderableWidget(Button.builder(
                            buttonLabel,
                            btn -> onCompressorButtonClicked(globalIndex))
                    .bounds(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build()
            ).active = enabled;
        }

        // Previous Page Button
        prevPageButton = this.addRenderableWidget(Button.builder(
                        Component.literal("<"),
                        btn -> {
                            if (currentPage > 0) {
                                currentPage--;
                                rebuildWidgets();
                            }
                        })
                .bounds(leftPos + 8, topPos + imageHeight - 28, 20, 20)
                .build()
        );
        prevPageButton.active = currentPage > 0;

        // Next Page Button
        nextPageButton = this.addRenderableWidget(Button.builder(
                        Component.literal(">"),
                        btn -> {
                            if (currentPage < maxPages - 1) {
                                currentPage++;
                                rebuildWidgets();
                            }
                        })
                .bounds(leftPos + imageWidth - 28, topPos + imageHeight - 28, 20, 20)
                .build()
        );
        nextPageButton.active = currentPage < maxPages - 1;
    }

    private void onCompressorButtonClicked(int index) {
        List<BlockPos> positions = menu.getCompressorPositions();

        if (index >= 0 && index < positions.size()) {
            BlockPos compressorPos = positions.get(index);
            OpenRemoteCompressorPacket.send(compressorPos);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Background
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFFC6C6C6);

        // Border
        guiGraphics.fill(x, y, x + this.imageWidth, y + 1, 0xFF8B8B8B);
        guiGraphics.fill(x, y, x + 1, y + this.imageHeight, 0xFF8B8B8B);
        guiGraphics.fill(x + this.imageWidth - 1, y, x + this.imageWidth, y + this.imageHeight, 0xFFFFFFFF);
        guiGraphics.fill(x, y + this.imageHeight - 1, x + this.imageWidth, y + this.imageHeight, 0xFFFFFFFF);

        // NEW: Draw status indicators on buttons
        renderStatusIndicators(guiGraphics, x, y);
    }

    private void renderStatusIndicators(GuiGraphics guiGraphics, int screenX, int screenY) {
        List<Boolean> powerStatus = menu.getCompressorPowerStatus();
        List<BlockPos> positions = menu.getCompressorPositions();

        for (int i = 0; i < BUTTONS_PER_PAGE; i++) {
            final int globalIndex = currentPage * BUTTONS_PER_PAGE + i;

            if (globalIndex >= positions.size()) break;

            int column = i / 6;
            int row = i % 6;

            // Position ON the button
            int buttonX = screenX + 8 + (column * COLUMN_SPACING);
            int buttonY = screenY + 20 + (row * 22);

            // Icon position: left side of button, vertically centered
            int iconX = buttonX + 3;
            int iconY = buttonY + 2;

            // Get power status
            boolean hasPower = globalIndex < powerStatus.size() && powerStatus.get(globalIndex);
            ResourceLocation texture = hasPower ? STATUS_GREEN : STATUS_RED;

            // PROPER SCALING: Use pose stack
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(iconX, iconY, 0);
            guiGraphics.pose().scale(2.0F, 2.0F, 1.0F); // 2x scale
            guiGraphics.blit(texture, 0, 0, 0, 0, 8, 8, 8, 8);
            guiGraphics.pose().popPose();
        }
    }

    // Helper method to draw a filled circle
    private void drawCircle(GuiGraphics guiGraphics, int centerX, int centerY, int radius, int color) {
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + y * y <= radius * radius) {
                    guiGraphics.fill(centerX + x, centerY + y, centerX + x + 1, centerY + y + 1, color);
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);

        Component pageText = Component.literal("Page " + (currentPage + 1) + "/" + maxPages);
        int textWidth = this.font.width(pageText);
        guiGraphics.drawString(this.font, pageText, (this.imageWidth - textWidth) / 2, this.imageHeight - 24, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        // NEW: Render status dots AFTER everything else (so they're on top)
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        renderStatusIndicators(guiGraphics, x, y);
    }
}
