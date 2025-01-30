package com.oe.ogtma.common.entity.quarry;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import com.oe.ogtma.api.data.accessor.OAEntityDataSerializers;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class QuarryDrillEntity extends Entity {

    public static final Set<QuarryDrillEntity> DRILLS = new HashSet<>();
    public static final BlockPos[] NO_TARGET = new BlockPos[0];

    protected static final EntityDataAccessor<Boolean> ONLINE = SynchedEntityData.defineId(QuarryDrillEntity.class,
            EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<BlockPos[]> TARGETS = SynchedEntityData.defineId(QuarryDrillEntity.class,
            OAEntityDataSerializers.BLOCK_POS_ARRAY);
    protected static final EntityDataAccessor<BlockPos> QUARRY_POS = SynchedEntityData.defineId(QuarryDrillEntity.class,
            EntityDataSerializers.BLOCK_POS);
    protected static final EntityDataAccessor<BlockPos> MOVE_TARGET = SynchedEntityData
            .defineId(QuarryDrillEntity.class, EntityDataSerializers.BLOCK_POS);
    protected static final EntityDataAccessor<AABB> CULL_BOX = SynchedEntityData.defineId(QuarryDrillEntity.class,
            OAEntityDataSerializers.AABB);

    public QuarryDrillEntity(EntityType<? extends QuarryDrillEntity> entityType, Level level) {
        super(entityType, level);
        DRILLS.add(this);
        noCulling = true;
    }

    @Override
    public void tick() {
        moveTowardsTarget();

        move(MoverType.SELF, getDeltaMovement());
    }

    protected void moveTowardsTarget() {
        var target = getMoveTarget();
        if (target.equals(getQuarryPos())) {
            setDeltaMovement(Vec3.ZERO);
        } else {
            var movementVector = position().vectorTo(target.above(3).getCenter());
            if (movementVector.length() < .25) {
                setDeltaMovement(movementVector.scale(.5));
            } else {
                setDeltaMovement(movementVector.normalize().scale(.5));
            }
        }
    }

    @Override
    protected AABB makeBoundingBox() {
        var aabb = super.makeBoundingBox();
        var quarryBox = getQuarryBox();
        if (!quarryBox.intersects(aabb)) {
            setQuarryBox(quarryBox.move(position().subtract(quarryBox.getCenter())));
        }
        return aabb;
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
        return entityData.get(CULL_BOX);
    }

    public void setQuarryBox(AABB quarryBox) {
        entityData.set(CULL_BOX, quarryBox);
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

    @Override
    protected void defineSynchedData() {
        entityData.define(ONLINE, false);
        entityData.define(TARGETS, NO_TARGET);
        entityData.define(QUARRY_POS, blockPosition());
        entityData.define(MOVE_TARGET, blockPosition());
        entityData.define(CULL_BOX, getDimensions(getPose()).makeBoundingBox(position()));
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
    public void remove(RemovalReason pReason) {
        DRILLS.remove(this);
        super.remove(pReason);
    }

    @Override
    public void onClientRemoval() {
        DRILLS.remove(this);
        super.onClientRemoval();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}
}
