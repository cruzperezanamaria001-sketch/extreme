package com.extremedisasters.entity;

import com.extremedisasters.config.DisasterConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class TornadoDebrisEntity extends Entity {
    private static final net.minecraft.network.syncher.EntityDataAccessor<BlockState> CARRIED_STATE =
            net.minecraft.network.syncher.SynchedEntityData.defineId(TornadoDebrisEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BLOCK_STATE);
    private static final int MAX_FLIGHT_TICKS = 240;
    private UUID ownerId = new UUID(0L, 0L);
    private int age;
    private float orbitAngle;
    private float orbitRadius = 4.0F;

    public TornadoDebrisEntity(EntityType<? extends TornadoDebrisEntity> type, Level level) { super(type, level); }
    @Override protected void defineSynchedData() { entityData.define(CARRIED_STATE, Blocks.AIR.defaultBlockState()); }
    public void setCarriedState(BlockState state) { entityData.set(CARRIED_STATE, state); }
    public BlockState getCarriedState() { return entityData.get(CARRIED_STATE); }
    public void setOwnerId(UUID id) { ownerId = id; }
    public UUID getOwnerId() { return ownerId; }

    @Override public void tick() {
        super.tick();
        age++;
        if (level().isClientSide) return;
        if (getCarriedState().isAir()) { discard(); return; }
        TornadoEntity owner = level().getEntitiesOfClass(TornadoEntity.class, getBoundingBox().inflate(64.0), e -> e.getUUID().equals(ownerId)).stream().findFirst().orElse(null);
        if (owner != null && age < 80) {
            orbitAngle += 0.16F;
            Vec3 target = new Vec3(owner.getX() + Math.cos(orbitAngle) * orbitRadius, owner.getY() + 1.5 + Math.sin(age * 0.10) * 1.0, owner.getZ() + Math.sin(orbitAngle) * orbitRadius);
            Vec3 velocity = target.subtract(position()).scale(0.12);
            setDeltaMovement(velocity.x, velocity.y + 0.015, velocity.z);
        } else {
            setDeltaMovement(getDeltaMovement().x * 0.99, getDeltaMovement().y - 0.04, getDeltaMovement().z * 0.99);
        }
        move(MoverType.SELF, getDeltaMovement());
        if (DisasterConfig.DEBRIS_DAMAGE.get() && tickCount % 3 == 0) {
            for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.2), e -> e.isAlive() && !e.getUUID().equals(ownerId))) {
                target.hurt(damageSources().thrown(this, owner), Math.max(1.0F, 2.0F * (float) Math.sqrt(Math.max(0.2, getDeltaMovement().length()))));
            }
        }
        if (onGround() || horizontalCollision || verticalCollision || age > MAX_FLIGHT_TICKS || getY() < level().getMinBuildHeight()) settleOrDrop();
    }

    private void settleOrDrop() {
        BlockState state = getCarriedState();
        if (state.isAir()) { discard(); return; }
        BlockPos landing = BlockPos.containing(getX(), getY(), getZ());
        BlockPos[] candidates = {landing, landing.above(), landing.below()};
        boolean placed = false;
        for (BlockPos target : candidates) {
            if (!level().isLoaded(target) || !level().getBlockState(target).canBeReplaced() || !state.canSurvive(level(), target)) continue;
            if (level().setBlock(target, state, 3)) { placed = true; break; }
        }
        if (!placed && getY() >= level().getMinBuildHeight() && getY() < level().getMaxBuildHeight()) {
            net.minecraft.world.entity.item.ItemEntity item = new net.minecraft.world.entity.item.ItemEntity(level(), getX(), getY(), getZ(), new net.minecraft.world.item.ItemStack(state.getBlock()));
            level().addFreshEntity(item);
        }
        discard();
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) ownerId = tag.getUUID("Owner");
        age = tag.getInt("Age"); orbitAngle = tag.getFloat("OrbitAngle"); orbitRadius = tag.getFloat("OrbitRadius");
        if (tag.contains("BlockState", 10)) setCarriedState(net.minecraft.nbt.NbtUtils.readBlockState(level().holderLookup(net.minecraft.core.registries.Registries.BLOCK), tag.getCompound("BlockState")));
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putUUID("Owner", ownerId); tag.putInt("Age", age); tag.putFloat("OrbitAngle", orbitAngle); tag.putFloat("OrbitRadius", orbitRadius);
        tag.put("BlockState", net.minecraft.nbt.NbtUtils.writeBlockState(getCarriedState()));
    }
    @Override public boolean isPickable() { return true; }
}
