package net.blackredcoded.brassmanmod.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class UpgradeRecipeCategory implements IRecipeCategory<UpgradeRecipeDisplay> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, "upgrading");
    public static final RecipeType<UpgradeRecipeDisplay> RECIPE_TYPE = new RecipeType<>(UID, UpgradeRecipeDisplay.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotDrawable;
    private final int width = 140;
    private final int height = 50;

    public UpgradeRecipeCategory(IGuiHelper guiHelper) {
        this.slotDrawable = guiHelper.getSlotDrawable();

        this.background = guiHelper.createBlankDrawable(width, height);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.BRASS_MODIFICATION_STATION.get()));
    }

    @Override
    public @NotNull RecipeType<UpgradeRecipeDisplay> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.literal("Armor Upgrading");
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, UpgradeRecipeDisplay recipe, @NotNull IFocusGroup focuses) {
        // Left slot: Base item
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 17)
                .addItemStacks(recipe.getBaseItems())
                .setBackground(slotDrawable, -1, -1);

        // Middle slot: Upgrade module (after plus sign)
        builder.addSlot(RecipeIngredientRole.INPUT, 52, 17)
                .addItemStack(recipe.getUpgradeModule())
                .setBackground(slotDrawable, -1, -1);

        // Right slot: Result (after arrow)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 113, 17)
                .addItemStacks(recipe.getResults())
                .setBackground(slotDrawable, -1, -1);
    }

    @Override
    public void draw(UpgradeRecipeDisplay recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        var poseStack = guiGraphics.pose();

        // Draw bigger plus sign with scaling
        poseStack.pushPose();
        poseStack.translate(34, 18, 0);
        poseStack.scale(2.0f, 2.0f, 1.0f);
        guiGraphics.drawString(font, "+", 0, 0, 0x999999, false);
        poseStack.popPose();

        // Draw arrow manually (simple text-based arrow)
        poseStack.pushPose();
        poseStack.translate(82, 18, 0);
        poseStack.scale(2.0f, 2.0f, 1.0f);
        guiGraphics.drawString(font, "→", 0, 0, 0x999999, false);
        poseStack.popPose();
    }
}
