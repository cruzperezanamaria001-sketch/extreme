package com.extremedisasters;

import com.extremedisasters.command.DisasterCommands;
import com.extremedisasters.config.DisasterConfig;
import com.extremedisasters.entity.ModEntities;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ExtremeDisasters.MOD_ID)
public final class ExtremeDisasters {
    public static final String MOD_ID = "extremedis";

    public ExtremeDisasters() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITY_TYPES.register(modBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, DisasterConfig.SPEC);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        DisasterCommands.register(event.getDispatcher());
    }
}
