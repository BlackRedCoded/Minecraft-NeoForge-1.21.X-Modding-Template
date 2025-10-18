package net.blackredcoded.brassmanmod.client.screen;

import net.blackredcoded.brassmanmod.menu.CustomizationStationMenu;
import net.blackredcoded.brassmanmod.network.ApplyArmorStylePacket;
import net.blackredcoded.brassmanmod.registry.ModItems;
import net.blackredcoded.brassmanmod.util.ArmorStyleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class CustomizationStationScreen extends AbstractContainerScreen<CustomizationStationMenu> {

    private Button brassButton;
    private Button aquaButton;
    private Button darkAquaButton;
    private Button flamingButton;
    private Button purchaseButton;

    private int previewStyle = 0;

    public CustomizationStationScreen(CustomizationStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 10000;
        this.titleLabelY = 5;
        this.previewStyle = menu.getSelectedStyle();
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 2x2 grid with MUCH MORE SPACING
        int col1X = x + 60;  // Left column
        int col2X = x + 120; // Right column (60px apart)
        int row1Y = y + 25;  // Top row
        int row2Y = y + 75;  // Bottom row (50px apart)

        // Top left - Brass
        brassButton = Button.builder(Component.literal("Brass"), btn -> selectStyle(0))
                .bounds(col1X, row1Y, 56, 20).build();

        // Top right - Aqua
        aquaButton = Button.builder(Component.literal("Iced"), btn -> selectStyle(1))
                .bounds(col2X, row1Y, 56, 20).build();

        // Bottom left - Dark Aqua
        darkAquaButton = Button.builder(Component.literal("Ocean"), btn -> selectStyle(2))
                .bounds(col1X, row2Y, 56, 20).build();

        // Bottom right - Flaming
        flamingButton = Button.builder(Component.literal("Flaming"), btn -> selectStyle(3))
                .bounds(col2X, row2Y, 55, 20).build();

        // Wide purchase button at bottom
        purchaseButton = Button.builder(Component.literal("Apply Style"), btn -> purchaseStyle())
                .bounds(x + 88, y + 140, 100, 20).build();

        addRenderableWidget(brassButton);
        addRenderableWidget(aquaButton);
        addRenderableWidget(darkAquaButton);
        addRenderableWidget(flamingButton);
        addRenderableWidget(purchaseButton);
    }

    private void selectStyle(int styleIndex) {
        previewStyle = styleIndex;
        menu.setSelectedStyle(styleIndex);
    }

    private void purchaseStyle() {
        String style = menu.getSelectedStyleName();
        PacketDistributor.sendToServer(new ApplyArmorStylePacket(style));
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Draw background
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFFC6C6C6);

        // Draw border
        guiGraphics.fill(x, y, x + this.imageWidth, y + 1, 0xFF8B8B8B);
        guiGraphics.fill(x, y, x + 1, y + this.imageHeight, 0xFF8B8B8B);
        guiGraphics.fill(x + this.imageWidth - 1, y, x + this.imageWidth, y + this.imageHeight, 0xFFFFFFFF);
        guiGraphics.fill(x, y + this.imageHeight - 1, x + this.imageWidth, y + this.imageHeight, 0xFFFFFFFF);

        // Draw 3D armor preview (left side)
        renderArmorPreview(guiGraphics, x + 40, y + 75);

        // Draw cost displays in 2x2 grid below buttons with more spacing
        int col1X = x + 60;
        int col2X = x + 125;
        int row1Y = y + 45;
        int row2Y = y + 95;

        drawCostDisplay(guiGraphics, col1X, row1Y, ArmorStyleHelper.BRASS);
        drawCostDisplay(guiGraphics, col2X, row1Y, ArmorStyleHelper.AQUA);
        drawCostDisplay(guiGraphics, col1X, row2Y, ArmorStyleHelper.DARK_AQUA);
        drawCostDisplay(guiGraphics, col2X, row2Y, ArmorStyleHelper.FLAMING);
    }

    private void renderArmorPreview(GuiGraphics guiGraphics, int x, int y) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        String style = getStyleName(previewStyle);
        ItemStack helmet = new ItemStack(ModItems.BRASS_MAN_HELMET.get());
        ItemStack chestplate = new ItemStack(ModItems.BRASS_MAN_CHESTPLATE.get());
        ItemStack leggings = new ItemStack(ModItems.BRASS_MAN_LEGGINGS.get());
        ItemStack boots = new ItemStack(ModItems.BRASS_MAN_BOOTS.get());

        ArmorStyleHelper.setArmorStyle(helmet, style);
        ArmorStyleHelper.setArmorStyle(chestplate, style);
        ArmorStyleHelper.setArmorStyle(leggings, style);
        ArmorStyleHelper.setArmorStyle(boots, style);

        ItemStack oldHelmet = player.getItemBySlot(EquipmentSlot.HEAD).copy();
        ItemStack oldChest = player.getItemBySlot(EquipmentSlot.CHEST).copy();
        ItemStack oldLegs = player.getItemBySlot(EquipmentSlot.LEGS).copy();
        ItemStack oldBoots = player.getItemBySlot(EquipmentSlot.FEET).copy();

        player.setItemSlot(EquipmentSlot.HEAD, helmet);
        player.setItemSlot(EquipmentSlot.CHEST, chestplate);
        player.setItemSlot(EquipmentSlot.LEGS, leggings);
        player.setItemSlot(EquipmentSlot.FEET, boots);

        // SAVE old head rotation
        float oldXRot = player.getXRot();
        float oldYHeadRot = player.yHeadRot;

        // SET head to look straight
        player.setXRot(0.0f);      // Pitch (up/down) - 0 = straight ahead
        player.yHeadRot = 0.0f;    // Yaw (left/right) - 0 = facing forward

        // FIXED: Perfect upright standing position - no tilt, no lean, looking straight ahead!
        InventoryScreen.renderEntityInInventory(guiGraphics, x - 10, y + 50, 50,
                new org.joml.Vector3f(0, 0, 0),
                new org.joml.Quaternionf().rotationXYZ(
                        (float)Math.PI,
                        (float)Math.PI -355f,
                        0f),
                null, player);

        // RESTORE old head rotation
        player.setXRot(oldXRot);
        player.yHeadRot = oldYHeadRot;

        player.setItemSlot(EquipmentSlot.HEAD, oldHelmet);
        player.setItemSlot(EquipmentSlot.CHEST, oldChest);
        player.setItemSlot(EquipmentSlot.LEGS, oldLegs);
        player.setItemSlot(EquipmentSlot.FEET, oldBoots);
    }

    private void drawCostDisplay(GuiGraphics guiGraphics, int x, int y, String style) {
        ItemStack[] costs = menu.getStyleCosts(style);
        Player player = Minecraft.getInstance().player;

        // FIRST: Render all items
        for (int i = 0; i < 3 && i < costs.length; i++) {
            ItemStack cost = costs[i];
            int slotX = x + (i * 17);
            guiGraphics.renderItem(cost, slotX, y);
        }

        // SECOND: Render text on top with pose stack manipulation
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 200); // Move text to higher layer

        for (int i = 0; i < 3 && i < costs.length; i++) {
            ItemStack cost = costs[i];
            int slotX = x + (i * 17);

            boolean hasEnough = hasEnoughItems(player, cost);
            int color = hasEnough ? 0x009000 : 0x900000;
            String countText = String.valueOf(cost.getCount());
            guiGraphics.drawString(this.font, countText, slotX + 12, y + 12, color, false);
        }

        guiGraphics.pose().popPose();
    }

    private boolean hasEnoughItems(Player player, ItemStack required) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, required)) {
                count += stack.getCount();
            }
        }
        return count >= required.getCount();
    }

    private String getStyleName(int index) {
        return switch (index) {
            case 1 -> ArmorStyleHelper.AQUA;
            case 2 -> ArmorStyleHelper.DARK_AQUA;
            case 3 -> ArmorStyleHelper.FLAMING;
            default -> ArmorStyleHelper.BRASS;
        };
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(1.2F, 1.2F, 1.2F);
        guiGraphics.drawString(this.font, this.title,
                (int)(this.titleLabelX / 1.2F), (int)(this.titleLabelY / 1.2F), 0x404040, false);
        guiGraphics.pose().popPose();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        String currentStyle = ArmorStyleHelper.getArmorStyle(
                Minecraft.getInstance().player.getItemBySlot(EquipmentSlot.CHEST));

        brassButton.active = !ArmorStyleHelper.BRASS.equals(currentStyle);
        aquaButton.active = !ArmorStyleHelper.AQUA.equals(currentStyle);
        darkAquaButton.active = !ArmorStyleHelper.DARK_AQUA.equals(currentStyle);
        flamingButton.active = !ArmorStyleHelper.FLAMING.equals(currentStyle);
    }
}
