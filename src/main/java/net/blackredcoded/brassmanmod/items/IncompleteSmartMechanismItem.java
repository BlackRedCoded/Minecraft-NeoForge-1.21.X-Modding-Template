package net.blackredcoded.brassmanmod.items;

import com.simibubi.create.AllDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class IncompleteSmartMechanismItem extends Item {

    public IncompleteSmartMechanismItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        // Check if Create's sequenced assembly data component exists
        var assemblyData = stack.get(AllDataComponents.SEQUENCED_ASSEMBLY);
        return assemblyData != null && assemblyData.progress() > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        // Get the sequenced assembly data component
        var assemblyData = stack.get(AllDataComponents.SEQUENCED_ASSEMBLY);

        if (assemblyData == null) {
            return 0;
        }

        // Get progress (0.0 to 1.0)
        float progress = assemblyData.progress();

        // Map progress (0.0-1.0) to bar width (0-13)
        return Math.round(progress * 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        // Get progress to calculate gradient color
        var assemblyData = stack.get(AllDataComponents.SEQUENCED_ASSEMBLY);

        if (assemblyData == null) {
            return 0xFFAA00; // Orange/yellow starting color
        }

        float progress = assemblyData.progress();

        // Interpolate between orange (0xFFAA00) and cyan (0x00FFFF)
        // Start: RGB(255, 170, 0) - Orange/Yellow
        // End: RGB(0, 255, 255) - Cyan

        int startR = 255;
        int startG = 170;
        int startB = 0;

        int endR = 0;
        int endG = 255;
        int endB = 255;

        // Linear interpolation for each color channel
        int r = (int) (startR + (endR - startR) * progress);
        int g = (int) (startG + (endG - startG) * progress);
        int b = (int) (startB + (endB - startB) * progress);

        // Combine RGB into single hex color
        return (r << 16) | (g << 8) | b;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // Create handles tooltips automatically
    }
}
