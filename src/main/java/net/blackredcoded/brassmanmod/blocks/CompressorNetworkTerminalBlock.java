package net.blackredcoded.brassmanmod.blocks;

import com.mojang.serialization.MapCodec;
import net.blackredcoded.brassmanmod.blockentity.CompressorNetworkTerminalBlockEntity;
import net.blackredcoded.brassmanmod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CompressorNetworkTerminalBlock extends BaseEntityBlock {

    public static final MapCodec<CompressorNetworkTerminalBlock> CODEC = simpleCodec(CompressorNetworkTerminalBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public CompressorNetworkTerminalBlock(Properties properties) {
        super(Properties.of()
                .strength(2, 4)
                .requiresCorrectToolForDrops()
                .sound(SoundType.WOOD));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        // Check for tablet - NORMAL click to bind
        if (stack.getItem() instanceof net.blackredcoded.brassmanmod.items.CompressorNetworkTabletItem) {
            if (level.isClientSide) {
                net.blackredcoded.brassmanmod.network.LinkTabletPacket.send(pos);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // Check which side was clicked
        Direction clickedFace = hitResult.getDirection();
        Direction blockFacing = state.getValue(FACING);
        boolean isBackSide = clickedFace == blockFacing.getOpposite();

        if (isBackSide) {
            // Back side: Frequency item management
            if (level.getBlockEntity(pos) instanceof CompressorNetworkTerminalBlockEntity terminal) {
                // REMOVED the empty hand check from here - that's in useWithoutItem now
                // Only handle setting frequency when holding an item
                if (!stack.isEmpty()) {
                    // Holding item: Set frequency
                    if (!level.isClientSide) {
                        ItemStack copy = stack.copy();
                        copy.setCount(1);
                        terminal.setFrequencyItem(copy);
                        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }

        // Default: Open GUI (front/sides)
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {

        // Check which side was clicked
        Direction clickedFace = hitResult.getDirection();
        Direction blockFacing = state.getValue(FACING);
        boolean isBackSide = clickedFace == blockFacing.getOpposite();

        if (isBackSide) {
            // Back side with empty hand = clear frequency
            if (level.getBlockEntity(pos) instanceof CompressorNetworkTerminalBlockEntity terminal) {
                ItemStack frequencyItem = terminal.getFrequencyItem();
                if (!frequencyItem.isEmpty() && !level.isClientSide) {
                    terminal.setFrequencyItem(ItemStack.EMPTY);
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.SUCCESS;
            }
        } else {
            // Open GUI
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                if (level.getBlockEntity(pos) instanceof CompressorNetworkTerminalBlockEntity terminal) {
                    serverPlayer.openMenu(terminal, pos);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CompressorNetworkTerminalBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }

        return type == ModBlockEntities.COMPRESSOR_NETWORK_TERMINAL.get()
                ? (BlockEntityTicker<T>) (BlockEntityTicker<CompressorNetworkTerminalBlockEntity>) CompressorNetworkTerminalBlockEntity::tick
                : null;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
