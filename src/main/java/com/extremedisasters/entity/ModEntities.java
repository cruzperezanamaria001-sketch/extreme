package com.extremedisasters.entity;

import com.extremedisasters.ExtremeDisasters;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ExtremeDisasters.MOD_ID);

    public static final RegistryObject<EntityType<TornadoEntity>> TORNADO = ENTITY_TYPES.register("tornado",
            () -> EntityType.Builder.<TornadoEntity>of(TornadoEntity::new, MobCategory.MISC)
                    .sized(1.0F, 2.0F).clientTrackingRange(128).updateInterval(2).setShouldReceiveVelocityUpdates(true)
                    .build("extremedis:tornado"));
    public static final RegistryObject<EntityType<TornadoDebrisEntity>> TORNADO_DEBRIS = ENTITY_TYPES.register("tornado_debris",
            () -> EntityType.Builder.<TornadoDebrisEntity>of(TornadoDebrisEntity::new, MobCategory.MISC)
                    .sized(0.65F, 0.65F).clientTrackingRange(64).updateInterval(1).setShouldReceiveVelocityUpdates(true)
                    .build("extremedis:tornado_debris"));

    private ModEntities() {}
}
