package net.blackredcoded.brassmanmod.client.screen;

import net.blackredcoded.brassmanmod.blockentity.CompressorNetworkTerminalBlockEntity;
import net.blackredcoded.brassmanmod.client.TabletBatteryManager;
import net.blackredcoded.brassmanmod.network.DrainTabletBatteryPacket;
import net.blackredcoded.brassmanmod.network.OpenRemoteCompressorPacket;
import net.blackredcoded.brassmanmod.util.BatteryHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TabletTerminalScreen extends Screen {

    private static final int IMAGE_WIDTH = 276;
    private static final int IMAGE_HEIGHT = 175;
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTONS_PER_PAGE = 12;
    private static final int COLUMN_SPACING = 130;

    // Status indicator textures
    private static final ResourceLocation STATUS_GREEN = ResourceLocation.fromNamespaceAndPath("brassmanmod", "textures/gui/status_green.png");
    private static final ResourceLocation STATUS_RED = ResourceLocation.fromNamespaceAndPath("brassmanmod", "textures/gui/status_red.png");

    private final BlockPos terminalPos;
    private int currentPage = 0;
    private int maxPages = 1;

    private Button nextPageButton;
    private Button prevPageButton;

    // Cached data from terminal
    private List<BlockPos> compressorPositions = new ArrayList<>();
    private List<Component> compressorNames = new ArrayList<>();
    private List<Boolean> compressorPowerStatus = new ArrayList<>();

    public TabletTerminalScreen(ItemStack tablet, BlockPos terminalPos) {
        super(Component.literal("Compressor Network Terminal"));
        this.terminalPos = terminalPos;
    }

    @Override
    public void tick() {
        super.tick();
        // This just updates the screen every tick so the battery display refreshes
        // The actual battery draining is handled by TabletBatteryManager
    }

    @Override
    protected void init() {
        super.init();

        // Get fresh tablet from player's hand
        ItemStack tablet = getFreshTablet();
        if (tablet != null) {
            TabletBatteryManager.startTabletUsage(tablet);
        }

        // Load data from terminal block entity
        loadTerminalData();

        int leftPos = (this.width - IMAGE_WIDTH) / 2;
        int topPos = (this.height - IMAGE_HEIGHT) / 2;

        maxPages = (int) Math.ceil(compressorPositions.size() / 12.0);
        if (maxPages == 0) maxPages = 1;

        // Create 12 buttons (6 rows x 2 columns)
        for (int i = 0; i < BUTTONS_PER_PAGE; i++) {
            int column = i / 6;
            int row = i % 6;
            int buttonX = leftPos + 12 + (column * COLUMN_SPACING);
            int buttonY = topPos + 20 + (row * 22);

            final int globalIndex = currentPage * BUTTONS_PER_PAGE + i;

            Component buttonLabel;
            boolean enabled;
            if (globalIndex < compressorPositions.size() && globalIndex < compressorNames.size()) {
                buttonLabel = compressorNames.get(globalIndex);
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
                .bounds(leftPos + 3, topPos + IMAGE_HEIGHT - 23, 20, 20)
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
                .bounds(leftPos + IMAGE_WIDTH - 23, topPos + IMAGE_HEIGHT - 23, 20, 20)
                .build()
        );
        nextPageButton.active = currentPage < maxPages - 1;
    }

    private ItemStack getFreshTablet() {
        if (minecraft != null && minecraft.player != null) {
            ItemStack mainHand = minecraft.player.getMainHandItem();
            if (mainHand.getItem() instanceof net.blackredcoded.brassmanmod.items.CompressorNetworkTabletItem) {
                return mainHand;
            }
        }
        return null;
    }

    /**
     * Load compressor data from the terminal block entity
     */
    private void loadTerminalData() {
        if (minecraft.level != null && minecraft.level.getBlockEntity(terminalPos) instanceof CompressorNetworkTerminalBlockEntity terminal) {
            compressorPositions = terminal.getConnectedCompressors();
            compressorNames = terminal.getCompressorNames();
            compressorPowerStatus = terminal.getCompressorPowerStatus();
        } else {
            // Terminal not found - clear data
            compressorPositions.clear();
            compressorNames.clear();
            compressorPowerStatus.clear();
        }
    }

    private void onCompressorButtonClicked(int index) {
        if (index >= 0 && index < compressorPositions.size()) {
            BlockPos compressorPos = compressorPositions.get(index);

            // Send packet to open compressor GUI
            OpenRemoteCompressorPacket.send(compressorPos);

            // Drain battery (1 point per GUI open)
            DrainTabletBatteryPacket.send();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Background
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int x = (this.width - IMAGE_WIDTH) / 2;
        int y = (this.height - IMAGE_HEIGHT) / 2;

        // Tablet GUI background
        renderTabletBackground(guiGraphics, x, y);

        // Render buttons
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render status indicators ON TOP
        renderStatusIndicators(guiGraphics, x, y);

        // Title and page number
        guiGraphics.drawString(this.font, this.title, x + 8, y + 8, 0xFF00FFFF, false);

        Component pageText = Component.literal("Page " + (currentPage + 1) + "/" + maxPages);
        int textWidth = this.font.width(pageText);
        guiGraphics.drawString(this.font, pageText, x + (IMAGE_WIDTH - textWidth) / 2, y + IMAGE_HEIGHT - 17, 0x404040, false);

        // Battery indicator - GET FRESH FROM PLAYER'S HAND using BatteryHelper
        ItemStack freshTablet = getFreshTablet();
        if (freshTablet != null && BatteryHelper.isBatteryItem(freshTablet)) {
            int battery = BatteryHelper.getBatteryCharge(freshTablet);
            int maxBattery = BatteryHelper.getMaxBatteryCharge(freshTablet);
            Component batteryText = Component.literal("Battery: " + battery + "%");
            guiGraphics.drawString(this.font, batteryText, x + IMAGE_WIDTH - 80, y + 8, 0xFFFF00, false); // Yellow
        }
    }

    private void renderTabletBackground(GuiGraphics guiGraphics, int x, int y) {
        // Darker background for tablet (different from terminal block)
        guiGraphics.fill(x, y, x + IMAGE_WIDTH, y + IMAGE_HEIGHT, 0xFF404040); //LeftX -10 = wider, TopY - 10 = higher, RightX -10 = shorter, BottomY -10 = shorter

        // Cyan border (tech look)
        guiGraphics.fill(x, y, x + IMAGE_WIDTH, y + 1, 0xFF00FFFF); // Left/Top/Right/Bottom
        guiGraphics.fill(x, y, x + 1, y + IMAGE_HEIGHT, 0xFF00FFFF); // Left/Top/Right/Bottom
        guiGraphics.fill(x + IMAGE_WIDTH - 1, y, x + IMAGE_WIDTH, y + IMAGE_HEIGHT, 0xFF00FFFF); // Left/Top/Right/Bottom
        guiGraphics.fill(x, y + IMAGE_HEIGHT - 1, x + IMAGE_WIDTH, y + IMAGE_HEIGHT, 0xFF00FFFF); // Left/Top/Right/Bottom
    }

    private void renderStatusIndicators(GuiGraphics guiGraphics, int screenX, int screenY) {
        for (int i = 0; i < BUTTONS_PER_PAGE; i++) {
            final int globalIndex = currentPage * BUTTONS_PER_PAGE + i;
            if (globalIndex >= compressorPositions.size()) break;

            int column = i / 6;
            int row = i % 6;
            int buttonX = screenX + 8 + (column * COLUMN_SPACING);
            int buttonY = screenY + 20 + (row * 22);

            int iconX = buttonX + 3;
            int iconY = buttonY + 2;

            boolean hasPower = globalIndex < compressorPowerStatus.size() && compressorPowerStatus.get(globalIndex);
            ResourceLocation texture = hasPower ? STATUS_GREEN : STATUS_RED;

            // Draw status icon with scaling
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(iconX, iconY, 0);
            guiGraphics.pose().scale(2.0F, 2.0F, 1.0F);
            guiGraphics.blit(texture, 0, 0, 0, 0, 8, 8, 8, 8);
            guiGraphics.pose().popPose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Don't pause game
    }

    @Override
    public void onClose() {
        // Stop tracking tablet usage
        TabletBatteryManager.stopTabletUsage();
        super.onClose();
    }
}
