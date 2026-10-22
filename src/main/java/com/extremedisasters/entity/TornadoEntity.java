package com.extremedisasters.entity;

import com.extremedisasters.config.DisasterConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TornadoEntity extends Entity {
    private static final int FORM_TICKS = 100;
    private static final int DISSIPATE_TICKS = 160;
    private int age;
    private int lifetime = 2400;
    private float influenceRadius = 12.0F;
    private float intensity = 1.0F;
    private float driftAngle;
    private int pickupCooldown;

    public TornadoEntity(EntityType<? extends TornadoEntity> type, Level level) { super(type, level); }

    public void configure(int lifetime, float radius, float intensity) {
        this.lifetime = Math.max(200, lifetime);
        this.influenceRadius = Math.max(4.0F, radius);
        this.intensity = Math.max(0.25F, Math.min(5.0F, intensity));
    }

    public float getVisualScale(float partialTick) {
        float t = age + partialTick;
        float grow = Math.min(1.0F, t / FORM_TICKS);
        float fade = Math.min(1.0F, Math.max(0.0F, (lifetime - t) / DISSIPATE_TICKS));
        return Math.min(grow, fade);
    }

    public float getInfluenceRadius() { return influenceRadius; }
    public float getIntensity() { return intensity; }
    public int getAge() { return age; }
    public int getLifetime() { return lifetime; }

    @Override
    public void tick() {
        super.tick();
        age++;
        if (pickupCooldown > 0) pickupCooldown--;
        if (level().isClientSide) return;
        if (!DisasterConfig.ENABLED.get() || age >= lifetime || !isAlive()) { discard(); return; }
        if (tickCount % 80 == 0) driftAngle += (random.nextFloat() - 0.5F) * 0.45F;
        double speed = 0.025 + 0.012 * intensity;
        setDeltaMovement(Math.cos(driftAngle) * speed, 0.0, Math.sin(driftAngle) * speed);
        move(MoverType.SELF, getDeltaMovement());
        if (tickCount % 5 == 0) applyWindForces();
        if (DisasterConfig.BLOCK_PICKUP.get() && pickupCooldown == 0 && tickCount % 8 == 0) {
            pickupOneBlock((ServerLevel) level());
            pickupCooldown = 4;
        }
    }

    private void applyWindForces() {
        double radius = influenceRadius;
        AABB area = getBoundingBox().inflate(radius, 14.0, radius);
        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, area, entity -> entity.isAlive());
        for (LivingEntity entity : entities) {
            if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) continue;
            double dx = getX() - entity.getX();
            double dz = getZ() - entity.getZ();
            double distance = Math.max(1.0, Math.sqrt(dx * dx + dz * dz));
            if (distance > radius) continue;
            double strength = (1.0 - distance / radius) * intensity;
            double pull = entity instanceof Player ? DisasterConfig.PLAYER_PULL.get() : DisasterConfig.MOB_PULL.get();
            Vec3 current = entity.getDeltaMovement();
            double tangentX = -dz / distance;
            double tangentZ = dx / distance;
            double lift = Math.min(0.12, 0.012 * strength);
            entity.setDeltaMovement(current.x + dx / distance * pull * strength + tangentX * pull * 0.8 * strength,
                    Math.min(0.32, current.y + lift),
                    current.z + dz / distance * pull * strength + tangentZ * pull * 0.8 * strength);
        }
    }

    private void pickupOneBlock(ServerLevel serverLevel) {
        int radius = Math.max(3, (int) influenceRadius);
        int dx = random.nextInt(radius * 2 + 1) - radius;
        int dz = random.nextInt(radius * 2 + 1) - radius;
        if (dx * dx + dz * dz > radius * radius) return;
        int dy = random.nextInt(5) - 2;
        BlockPos pos = BlockPos.containing(getX() + dx, getY() + dy, getZ() + dz);
        if (!serverLevel.isLoaded(pos) || pos.equals(blockPosition())) return;
        Vec3 from = new Vec3(getX(), getY() + 1.0, getZ());
        BlockHitResult visibility = serverLevel.clip(new ClipContext(from, Vec3.atCenterOf(pos), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (visibility.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK && !visibility.getBlockPos().equals(pos)) return;
        BlockState state = serverLevel.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.BEDROCK) || state.is(Blocks.BARRIER) || state.is(Blocks.END_PORTAL) || state.is(Blocks.END_GATEWAY)) return;
        if (state.hasBlockEntity() || !state.getFluidState().isEmpty() || state.getDestroySpeed(serverLevel, pos) < 0.0F) return;
        int active = serverLevel.getEntitiesOfClass(TornadoDebrisEntity.class, getBoundingBox().inflate(influenceRadius + 48.0),
                debris -> debris.getOwnerId().equals(getUUID())).size();
        if (active >= DisasterConfig.MAX_ACTIVE_DEBRIS.get()) return;
        TornadoDebrisEntity debris = new TornadoDebrisEntity(ModEntities.TORNADO_DEBRIS.get(), serverLevel);
        debris.setOwnerId(getUUID());
        debris.setCarriedState(state);
        double angle = random.nextDouble() * Math.PI * 2.0;
        double orbit = 1.5 + random.nextDouble() * influenceRadius * 0.45;
        debris.setPos(getX() + Math.cos(angle) * orbit, getY() + 1.0 + random.nextDouble() * 3.0, getZ() + Math.sin(angle) * orbit);
        debris.setDeltaMovement(-Math.sin(angle) * 0.18, 0.18 + random.nextDouble() * 0.14, Math.cos(angle) * 0.18);
        if (!serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3)) return;
        if (!serverLevel.addFreshEntity(debris)) {
            serverLevel.setBlock(pos, state, 3);
            return;
        }
        serverLevel.levelEvent(2001, pos, Block.getId(state));
    }

    @Override protected void defineSynchedData() {}
    @Override protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        age = tag.getInt("Age"); lifetime = tag.getInt("Lifetime"); influenceRadius = tag.getFloat("Radius"); intensity = tag.getFloat("Intensity"); driftAngle = tag.getFloat("DriftAngle");
    }
    @Override protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        tag.putInt("Age", age); tag.putInt("Lifetime", lifetime); tag.putFloat("Radius", influenceRadius); tag.putFloat("Intensity", intensity); tag.putFloat("DriftAngle", driftAngle);
    }
    @Override public boolean isPickable() { return false; }
}
