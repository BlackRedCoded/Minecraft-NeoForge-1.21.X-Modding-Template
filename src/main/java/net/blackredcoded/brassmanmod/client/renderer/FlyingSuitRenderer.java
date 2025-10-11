package net.blackredcoded.brassmanmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.blackredcoded.brassmanmod.entity.FlyingSuitEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class FlyingSuitRenderer extends EntityRenderer<FlyingSuitEntity> {

    public FlyingSuitRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FlyingSuitEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        poseStack.pushPose();

        poseStack.scale(1.2f, 1.2f, 1.2f);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));

        var itemRenderer = Minecraft.getInstance().getItemRenderer();

        ItemStack helmet = entity.getHelmet();
        ItemStack chestplate = entity.getChestplate();
        ItemStack leggings = entity.getLeggings();
        ItemStack boots = entity.getBoots();

        if (!helmet.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0, 0.6, 0);
            itemRenderer.renderStatic(helmet, ItemDisplayContext.FIXED, packedLight,
                    OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
            poseStack.popPose();
        }

        if (!chestplate.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0, 0.2, 0);
            itemRenderer.renderStatic(chestplate, ItemDisplayContext.FIXED, packedLight,
                    OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
            poseStack.popPose();
        }

        if (!leggings.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0, -0.2, 0);
            itemRenderer.renderStatic(leggings, ItemDisplayContext.FIXED, packedLight,
                    OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
            poseStack.popPose();
        }

        if (!boots.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0, -0.6, 0);
            itemRenderer.renderStatic(boots, ItemDisplayContext.FIXED, packedLight,
                    OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(FlyingSuitEntity entity) {
        return null;
    }
}
