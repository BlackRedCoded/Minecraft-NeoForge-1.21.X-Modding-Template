package net.blackredcoded.brassmanmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.blackredcoded.brassmanmod.entity.FlyingArmorPieceEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class FlyingArmorPieceRenderer extends EntityRenderer<FlyingArmorPieceEntity> {

    public FlyingArmorPieceRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FlyingArmorPieceEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        ItemStack armorPiece = entity.getArmorPiece();
        if (armorPiece.isEmpty()) return;

        poseStack.pushPose();

        // Rotate the armor piece for visual effect
        float rotation = (entity.tickCount + partialTick) * 10.0f;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.scale(1.5f, 1.5f, 1.5f);

        // Render the item
        Minecraft.getInstance().getItemRenderer().renderStatic(
                armorPiece,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FlyingArmorPieceEntity entity) {
        return null; // Not used since we render items
    }
}
