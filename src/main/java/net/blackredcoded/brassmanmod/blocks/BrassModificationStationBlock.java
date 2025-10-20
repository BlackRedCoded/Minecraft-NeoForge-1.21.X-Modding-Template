package net.blackredcoded.brassmanmod.blocks;

import com.mojang.serialization.MapCodec;
import net.blackredcoded.brassmanmod.menu.ModificationStationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BrassModificationStationBlock extends Block {

    public static final MapCodec<BrassModificationStationBlock> CODEC = simpleCodec(BrassModificationStationBlock::new);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

    public BrassModificationStationBlock(Properties properties) {
        super(Properties.of()
                .strength(3, 6)
                .requiresCorrectToolForDrops()
                .sound(SoundType.ANVIL)
                .noOcclusion());
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer) {
            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, playerEntity) -> new ModificationStationMenu(containerId, playerInventory),
                    Component.literal("Brass Modification Station")
            ));
        }
        return ItemInteractionResult.SUCCESS;
    }
}
