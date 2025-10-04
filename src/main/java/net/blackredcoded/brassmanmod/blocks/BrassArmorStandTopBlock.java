package net.blackredcoded.brassmanmod.blocks;

import net.blackredcoded.brassmanmod.blockentity.BrassArmorStandBlockEntity;
import net.blackredcoded.brassmanmod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class BrassArmorStandTopBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(5, 0, 7, 7, 5, 9),
            Block.box(9, 0, 7, 11, 5, 9),
            Block.box(2, 5, 6.5, 14, 8, 9.5),
            Block.box(7, 8, 7, 9, 14, 9)
    );
    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
            Block.box(9, 0, 7, 11, 5, 9),
            Block.box(5, 0, 7, 7, 5, 9),
            Block.box(2, 5, 6.5, 14, 8, 9.5),
            Block.box(7, 8, 7, 9, 14, 9)
    );
    private static final VoxelShape SHAPE_EAST = Shapes.or(
            Block.box(7, 0, 5, 9, 5, 7),
            Block.box(7, 0, 9, 9, 5, 11),
            Block.box(6.5, 5, 2, 9.5, 8, 14),
            Block.box(7, 8, 7, 9, 14, 9)
    );
    private static final VoxelShape SHAPE_WEST = Shapes.or(
            Block.box(7, 0, 9, 9, 5, 11),
            Block.box(7, 0, 5, 9, 5, 7),
            Block.box(6.5, 5, 2, 9.5, 8, 14),
            Block.box(7, 8, 7, 9, 14, 9)
    );

    public BrassArmorStandTopBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST  -> SHAPE_WEST;
            case EAST  -> SHAPE_EAST;
            default    -> SHAPE_NORTH;
        };
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos basePos = pos.below();
        BlockState baseState = level.getBlockState(basePos);
        if (baseState.getBlock() instanceof BrassArmorStandBaseBlock baseBlock) {
            BlockEntity be = level.getBlockEntity(basePos);
            if (be instanceof BrassArmorStandBlockEntity stand) {
                // Correct argument order: player, stand, hit, basePos, isTopBlock
                return baseBlock.handleInteraction(player, stand, hit, basePos, true);
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockPos basePos = pos.below();
            BlockState baseState = level.getBlockState(basePos);
            if (baseState.getBlock() == ModBlocks.BRASS_ARMOR_STAND.get()) {
                if (level.getBlockEntity(basePos) instanceof BrassArmorStandBlockEntity stand) {
                    for (int i = 0; i < 4; i++) {
                        ItemStack armor = stand.getArmor(i);
                        if (!armor.isEmpty()) {
                            Block.popResource(level, pos, armor);
                        }
                    }
                }
                Block.dropResources(baseState, level, basePos);
                level.destroyBlock(basePos, false);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder params) {
        return List.of(); // Drops handled by base block
    }
}
