package net.blackredcoded.brassmanmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blackredcoded.brassmanmod.blockentity.CompressorNetworkTerminalBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Quaternionf;

public class CompressorNetworkTerminalBlockEntityRenderer implements BlockEntityRenderer<CompressorNetworkTerminalBlockEntity> {
    private final ItemRenderer itemRenderer;

    public CompressorNetworkTerminalBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(CompressorNetworkTerminalBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack frequencyItem = blockEntity.getFrequencyItem();

        if (frequencyItem.isEmpty()) {
            return;
        }

        Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction backFace = facing.getOpposite();

        poseStack.pushPose();

        // Position on the BACK face - OUTSIDE the block!
        switch (backFace) {
            case NORTH -> {
                poseStack.translate(0.5, 0.5, -0.01);
                poseStack.mulPose(new Quaternionf().rotationXYZ(0, 0, 0));
            }
            case SOUTH -> {
                poseStack.translate(0.5, 0.5, 1.01);
                poseStack.mulPose(new Quaternionf().rotationXYZ(0, (float)Math.PI, 0));
            }
            case WEST -> {
                poseStack.translate(-0.01, 0.5, 0.5);
                poseStack.mulPose(new Quaternionf().rotationXYZ(0, (float)Math.PI / 2, 0));
            }
            case EAST -> {
                poseStack.translate(1.01, 0.5, 0.5);
                poseStack.mulPose(new Quaternionf().rotationXYZ(0, -(float)Math.PI / 2, 0));
            }
        }

        // Scale uniformly - item sticks out like Data Link
        poseStack.scale(0.35f, 0.35f, 0.35f);

        // Render the item
        this.itemRenderer.renderStatic(frequencyItem, ItemDisplayContext.FIXED, packedLight,
                packedOverlay, poseStack, buffer, blockEntity.getLevel(), 0);

        poseStack.popPose();
    }
}
