package net.blackredcoded.brassmanmod.blocks;

import com.mojang.serialization.MapCodec;
import net.blackredcoded.brassmanmod.blockentity.CustomizationStationBlockEntity;
import net.blackredcoded.brassmanmod.screen.CustomizationStationScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CustomizationStationBlock extends BaseEntityBlock {
    public static final MapCodec<CustomizationStationBlock> CODEC = simpleCodec(CustomizationStationBlock::new);

    public CustomizationStationBlock(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        MenuProvider provider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("Customization Station");
            }
            @Override
            public CustomizationStationScreenHandler createMenu(int id, Inventory inv, Player p) {
                return new CustomizationStationScreenHandler(id, inv, pos);
            }
        };
        if (player instanceof ServerPlayer sp) {
            sp.openMenu(provider);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CustomizationStationBlockEntity(pos, state);
    }
}
