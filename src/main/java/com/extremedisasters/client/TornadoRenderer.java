package com.extremedisasters.client;

import com.extremedisasters.entity.TornadoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class TornadoRenderer extends EntityRenderer<TornadoEntity> {
    private static final ResourceLocation PARTICLE = new ResourceLocation("minecraft", "textures/particle/generic_0.png");
    private static final int SIDES = 32;
    private static final int RINGS = 18;

    public TornadoRenderer(EntityRendererProvider.Context context) { super(context); }

    @Override
    public void render(TornadoEntity tornado, float entityYaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        float scale = tornado.getVisualScale(partialTick);
        if (scale <= 0.01F) return;
        pose.pushPose();
        pose.translate(0.0, 0.05, 0.0);
        VertexConsumer out = buffers.getBuffer(RenderType.entityTranslucent(PARTICLE));
        float height = 25.0F * scale;
        float rotation = (tornado.getAge() + partialTick) * 0.035F;
        for (int ring = 0; ring < RINGS; ring++) {
            float low = ring / (float) RINGS;
            float high = (ring + 1) / (float) RINGS;
            float y0 = low * height;
            float y1 = high * height;
            float r0 = (0.7F + 7.0F * low) * scale;
            float r1 = (0.7F + 7.0F * high) * scale;
            float twist0 = rotation + low * 2.2F;
            float twist1 = rotation + high * 2.2F;
            int alpha = (int) (105 * (1.0F - low * 0.48F) * scale);
            for (int side = 0; side < SIDES; side++) {
                float a0 = side * (float) (Math.PI * 2.0 / SIDES);
                float a1 = (side + 1) * (float) (Math.PI * 2.0 / SIDES);
                float[] p00 = point(r0, y0, a0 + twist0);
                float[] p01 = point(r0, y0, a1 + twist0);
                float[] p10 = point(r1, y1, a0 + twist1);
                float[] p11 = point(r1, y1, a1 + twist1);
                float shade = 0.72F + 0.28F * (float) Math.sin(a0 * 3.0F + rotation + low * 5.0F);
                int red = (int) (105 * shade);
                int green = (int) (119 * shade);
                int blue = (int) (126 * shade);
                vertex(out, pose, p00, 0.0F, low, red, green, blue, alpha, light);
                vertex(out, pose, p01, 1.0F, low, red, green, blue, alpha, light);
                vertex(out, pose, p11, 1.0F, high, red, green, blue, alpha, light);
                vertex(out, pose, p10, 0.0F, high, red, green, blue, alpha, light);
            }
        }
        pose.popPose();
        super.render(tornado, entityYaw, partialTick, pose, buffers, light);
    }

    private static float[] point(float radius, float y, float angle) {
        return new float[] {(float) Math.cos(angle) * radius, y, (float) Math.sin(angle) * radius};
    }

    private static void vertex(VertexConsumer out, PoseStack pose, float[] p, float u, float v, int r, int g, int b, int a, int light) {
        out.vertex(pose.last().pose(), p[0], p[1], p[2]).color(r, g, b, a).uv(u, v)
                .overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(pose.last().normal(), 0.0F, 1.0F, 0.0F).endVertex();
    }

    @Override public ResourceLocation getTextureLocation(TornadoEntity entity) { return PARTICLE; }
}
