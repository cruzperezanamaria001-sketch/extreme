package com.extremedisasters.client;

import com.extremedisasters.ExtremeDisasters;
import com.extremedisasters.entity.ModEntities;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExtremeDisasters.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.TORNADO.get(), TornadoRenderer::new);
        event.registerEntityRenderer(ModEntities.TORNADO_DEBRIS.get(), TornadoDebrisRenderer::new);
    }

    private ClientModEvents() {}
}
