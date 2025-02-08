package com.oe.ogtma.common.entity.quarry;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import com.oe.ogtma.api.data.accessor.OAEntityDataSerializers;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class QuarryDrillEntity extends Entity {

    public static final BlockPos[] NO_TARGET = new BlockPos[0];

    protected static final EntityDataAccessor<Integer> TIER = SynchedEntityData.defineId(QuarryDrillEntity.class,
            EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Boolean> ONLINE = SynchedEntityData.defineId(QuarryDrillEntity.class,
            EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<BlockPos[]> TARGETS = SynchedEntityData.defineId(QuarryDrillEntity.class,
            OAEntityDataSerializers.BLOCK_POS_ARRAY);
    protected static final EntityDataAccessor<BlockPos> QUARRY_POS = SynchedEntityData.defineId(QuarryDrillEntity.class,
            EntityDataSerializers.BLOCK_POS);
    protected static final EntityDataAccessor<BlockPos> MOVE_TARGET = SynchedEntityData
            .defineId(QuarryDrillEntity.class, EntityDataSerializers.BLOCK_POS);
    protected static final EntityDataAccessor<Boolean> SHOULD_TARGET_AIR = SynchedEntityData
            .defineId(QuarryDrillEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Integer> TARGET_AIR_COLOR = SynchedEntityData
            .defineId(QuarryDrillEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<AABB> QUARRY_BOX = SynchedEntityData.defineId(QuarryDrillEntity.class,
            OAEntityDataSerializers.AABB);
    protected int noPhysicsTicks = 0;

    public QuarryDrillEntity(EntityType<? extends QuarryDrillEntity> entityType, Level level) {
        super(entityType, level);
        noCulling = true;
    }

    @Override
    public void tick() {
        moveTowardsTarget();

        move(MoverType.SELF, getDeltaMovement());
        solveCollisions();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    protected void moveTowardsTarget() {
        var target = getMoveTarget();
        if (target.equals(getQuarryPos())) {
            setDeltaMovement(Vec3.ZERO);
        } else {
            var movementVector = new Vec3(target.getX() + .5 - getX(), 0, target.getZ() + .5 - getZ());
            var bb = getBoundingBox();
            if (bb.minY > target.getY() + 3) {
                setBoundingBox(bb.setMinY(target.getY() + 3));
            }
            setDeltaMovement(movementVector.scale(.08));
        }
    }

    protected void solveCollisions() {
        if (this.noPhysicsTicks <= 0) {
            if (!getQuarryBox().intersect(getBoundingBox()).equals(getBoundingBox()) ||
                    (this.horizontalCollision && this.verticalCollision)) {
                this.noPhysics = true;
                this.noPhysicsTicks = 21;
            } else {
                this.noPhysics = false;
            }
        } else {
            this.noPhysicsTicks--;
        }
    }

    public int getTier() {
        return entityData.get(TIER);
    }

    public void setTier(int tier) {
        entityData.set(TIER, tier);
    }

    public BlockPos[] getTargets() {
        return entityData.get(TARGETS);
    }

    public void setTargets(@Nullable BlockPos[] targets) {
        if (targets == null) {
            targets = NO_TARGET;
        }
        entityData.set(TARGETS, targets);
    }

    public AABB getQuarryBox() {
        return entityData.get(QUARRY_BOX);
    }

    public void setQuarryBox(AABB quarryBox) {
        entityData.set(QUARRY_BOX, quarryBox);
    }

    public BlockPos getQuarryPos() {
        return entityData.get(QUARRY_POS);
    }

    public void setQuarryPos(BlockPos blockPos) {
        entityData.set(QUARRY_POS, blockPos);
        setMoveTarget(blockPos);
    }

    public BlockPos getMoveTarget() {
        return entityData.get(MOVE_TARGET);
    }

    public void setMoveTarget(BlockPos blockPos) {
        entityData.set(MOVE_TARGET, blockPos);
    }

    public boolean shouldTargetAir() {
        return entityData.get(SHOULD_TARGET_AIR);
    }

    public void setTargetAir(boolean targetAir) {
        entityData.set(SHOULD_TARGET_AIR, targetAir);
    }

    public int getAirColor() {
        return entityData.get(TARGET_AIR_COLOR);
    }

    public void setAirColor(int color) {
        entityData.set(TARGET_AIR_COLOR, color);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(TIER, 0);
        entityData.define(ONLINE, false);
        entityData.define(TARGETS, NO_TARGET);
        entityData.define(QUARRY_POS, blockPosition());
        entityData.define(MOVE_TARGET, blockPosition());
        entityData.define(SHOULD_TARGET_AIR, false);
        entityData.define(TARGET_AIR_COLOR, 1);
        entityData.define(QUARRY_BOX, getDimensions(getPose()).makeBoundingBox(position()));
    }

    @Override
    public boolean isColliding(BlockPos pPos, BlockState pState) {
        return false;
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return true;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    public SoundSource getSoundSource() {
        return SoundSource.BLOCKS;
    }
}
