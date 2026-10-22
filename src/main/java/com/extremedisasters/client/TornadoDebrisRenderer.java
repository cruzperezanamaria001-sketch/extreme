package com.extremedisasters.client;

import com.extremedisasters.entity.TornadoDebrisEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class TornadoDebrisRenderer extends EntityRenderer<TornadoDebrisEntity> {
    public TornadoDebrisRenderer(EntityRendererProvider.Context context) { super(context); }

    @Override
    public void render(TornadoDebrisEntity entity, float entityYaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.translate(-0.5, 0.0, -0.5);
        net.minecraft.client.Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                entity.getCarriedState(), pose, buffers, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        super.render(entity, entityYaw, partialTick, pose, buffers, light);
    }

    @Override public ResourceLocation getTextureLocation(TornadoDebrisEntity entity) { return net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation(); }
}
