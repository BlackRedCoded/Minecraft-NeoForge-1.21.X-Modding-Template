package net.blackredcoded.brassmanmod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.blackredcoded.brassmanmod.network.ApplyDyePacket;
import net.blackredcoded.brassmanmod.network.ModNetworking;
import net.blackredcoded.brassmanmod.screen.CustomizationStationScreenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CustomizationStationScreen extends AbstractContainerScreen<CustomizationStationScreenHandler> {
    private static final Item[] DYE_ITEMS = {
            Items.BARRIER,
            Items.WHITE_DYE, Items.ORANGE_DYE, Items.MAGENTA_DYE, Items.LIGHT_BLUE_DYE,
            Items.YELLOW_DYE, Items.LIME_DYE, Items.PINK_DYE, Items.GRAY_DYE,
            Items.LIGHT_GRAY_DYE, Items.CYAN_DYE, Items.PURPLE_DYE, Items.BLUE_DYE,
            Items.BROWN_DYE, Items.GREEN_DYE, Items.RED_DYE, Items.BLACK_DYE
    };

    private final LivingEntity playerModel = Minecraft.getInstance().player;
    private float rotationAngle = 0f;
    private String selectedPiece = null;
    private Integer selectedColor = null;

    public CustomizationStationScreen(CustomizationStationScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title);
        this.imageWidth = 220;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("Confirm"), btn -> {
            if (selectedPiece != null && selectedColor != null) {
                ModNetworking.sendToServer(new ApplyDyePacket(selectedColor, true));
            }
        }).bounds(leftPos + 150, topPos + 120, 60, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);

        int startX = x + 100, startY = y + 20, gap = 22, slotSize = 18;

        // Render 20 slots (5x4 grid)
        for (int i = 0; i < 20; i++) {
            int col = i % 5, row = i / 5;
            int px = startX + col * gap, py = startY + row * gap;

            // Only render actual items for indices 0-16
            if (i < DYE_ITEMS.length) {
                Item item = DYE_ITEMS[i];

                // Slot background
                g.fill(px, py, px + slotSize, py + slotSize, 0xFF555555);

                // Render item icon
                g.renderItem(new ItemStack(item), px + 1, py + 1);

                // Get count from menu
                int count = (item == Items.BARRIER ? 0 : menu.getDyeCount((DyeItem) item));

                // Draw count in lower right corner (like vanilla)
                String countStr = String.valueOf(count);
                int textColor = count == 0 ? 0xFFFF5555 : 0xFFFFFFFF; // Red if 0, white otherwise
                g.drawString(font, countStr, px + slotSize - font.width(countStr) - 1, py + slotSize - 9, textColor, true);

                // Highlight selection - FIXED: Added bounds check
                boolean sel = (i == 0 && selectedColor != null && selectedColor == 0)
                        || (selectedColor != null && i > 0 && i < DYE_ITEMS.length
                        && selectedColor == ((DyeItem) DYE_ITEMS[i]).getDyeColor().getTextureDiffuseColor());

                if (sel) {
                    g.fill(px - 1, py - 1, px + slotSize + 1, py, 0xFFFFFFFF);
                    g.fill(px - 1, py + slotSize, px + slotSize + 1, py + slotSize + 1, 0xFFFFFFFF);
                    g.fill(px - 1, py, px, py + slotSize, 0xFFFFFFFF);
                    g.fill(px + slotSize, py, px + slotSize + 1, py + slotSize, 0xFFFFFFFF);
                }
            } else {
                // Empty slot for indices 17-19
                g.fill(px, py, px + slotSize, py + slotSize, 0xFF333333);
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        renderBackground(g, mouseX, mouseY, partialTicks);
        super.render(g, mouseX, mouseY, partialTicks);
        renderPlayerModelWithArmor(g, partialTicks);
        renderTooltip(g, mouseX, mouseY);
    }

    private void renderPlayerModelWithArmor(GuiGraphics g, float partialTicks) {
        rotationAngle += partialTicks * 0.5f;  // original double-head speed on Y axis
        if (rotationAngle >= 360f) rotationAngle -= 360f;

        int cx = leftPos + 50, cy = topPos + 65, scale = 50;
        InventoryScreen.renderEntityInInventoryFollowsAngle(
                g,
                cx - scale, cy - scale,
                cx + scale, cy + scale,
                scale,
                0.0625f,
                rotationAngle,
                rotationAngle * 2,  // head rotates twice as fast
                playerModel
        );
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 6, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int x = leftPos, y = topPos;
        int cx = x + 50, cy = y + 65, scale = 50;

        // Armor piece selection
        if (mx >= cx - scale && mx <= cx + scale && my >= cy - scale && my <= cy + scale) {
            double ry = my - (cy - scale);
            double seg = (2.0 * scale) / 4.0;
            if (ry < seg) selectedPiece = "helmet";
            else if (ry < 2 * seg) selectedPiece = "chestplate";
            else if (ry < 3 * seg) selectedPiece = "leggings";
            else selectedPiece = "boots";
            return true;
        }

        // Dye grid click - FIXED: Added bounds check
        int startX = x + 100, startY = y + 20, gap = 22;
        int col = (int) ((mx - startX) / gap), row = (int) ((my - startY) / gap);
        if (col >= 0 && col < 5 && row >= 0 && row < 4) {
            int idx = row * 5 + col;
            // Only process if within DYE_ITEMS array bounds
            if (idx < DYE_ITEMS.length && DYE_ITEMS[idx] != null) {
                if (idx == 0) {
                    selectedColor = 0;
                } else {
                    selectedColor = ((DyeItem) DYE_ITEMS[idx]).getDyeColor().getTextureDiffuseColor();
                }
                ModNetworking.sendToServer(new ApplyDyePacket(selectedColor, false));
            }
            return true;
        }

        return super.mouseClicked(mx, my, button);
    }
}
