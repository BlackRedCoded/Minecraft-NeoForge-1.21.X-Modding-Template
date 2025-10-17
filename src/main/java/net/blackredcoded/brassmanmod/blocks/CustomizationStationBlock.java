package net.blackredcoded.brassmanmod.blocks;

import net.blackredcoded.brassmanmod.menu.CustomizationStationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class CustomizationStationBlock extends Block {

    public CustomizationStationBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level,
                                                        @NotNull BlockPos pos, @NotNull Player player,
                                                        @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide) {
            player.openMenu(getMenuProvider(level, pos));
        }
        return InteractionResult.SUCCESS;
    }

    private MenuProvider getMenuProvider(Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (id, inventory, player) -> new CustomizationStationMenu(id, inventory),
                Component.literal("Armor Customization Station")
        );
    }
}
