package com.oe.ogtma.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.oe.ogtma.OGTMA;
import com.oe.ogtma.common.block.marker.WallMarkerBlock;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Objects;

public class MarkerBlockEntity extends BlockEntity {

    public static final Map<Direction, Vector3f> DIRECTION_OFFSET = Maps.immutableEnumMap(ImmutableMap.of(
            Direction.NORTH, new Vector3f(.5f, .7f, .75f),
            Direction.SOUTH, new Vector3f(.5f, .7f, .25f),
            Direction.WEST, new Vector3f(.75f, .7f, .5f),
            Direction.EAST, new Vector3f(.25f, .7f, .5f),
            Direction.DOWN, new Vector3f(.5f, .6f, .5f),
            Direction.UP, new Vector3f(0, 0, 0)));

    public static final String POSITIONS_TAG = "Positions";

    @Getter
    protected final Vector3f[] positions = new Vector3f[Direction.Axis.values().length];
    @Getter
    protected final Vector3f selfPos;

    public MarkerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.selfPos = new Vector3f(pos.getX(), pos.getY(), pos.getZ())
                .add(DIRECTION_OFFSET.get(getOrientation()));
        for (int i = 0; i < positions.length; i++) {
            set(i);
        }
    }

    public void set(int index) {
        set(index, selfPos, true);
    }

    public void set(int index, Vector3f pos, boolean copy) {
        set(index, copy ? new Vector3f(pos) : pos);
    }

    public void set(int index, Vector3f pos) {
        if (positions[index] != null) {
            if (positions[index].equals(pos)) {
                return;
            }
            positions[index].set(pos);
        } else {
            positions[index] = pos;
        }
        OGTMA.LOGGER.info("[{}] Setting {} to {}", pos.equals(selfPos) ? "Clear" : "Set", index, positions[index]);
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            setChanged();
        }
    }

    public Direction getOrientation() {
        return Objects.requireNonNullElse((Direction) getBlockState().getValues().get(WallMarkerBlock.FACING),
                Direction.DOWN);
    }

    public boolean isEmpty(int index) {
        return selfPos.equals(positions[index]);
    }

    public boolean isEmpty() {
        for (int i = 0; i < positions.length; i++) {
            if (!isEmpty(i)) {
                return false;
            }
        }
        return true;
    }

    public int count() {
        var count = 0;
        for (int i = 0; i < positions.length; i++) {
            if (!isEmpty(i)) {
                count++;
            }
        }
        return count;
    }

    public void updateConnections() {
        if (level == null) {
            return;
        }
        for (int i = 0; i < positions.length; i++) {
            if (isEmpty(i)) {
                continue;
            }
            var pos = positions[i];
            if (!(level.getBlockEntity(BlockPos.containing(pos.x, pos.y, pos.z)) instanceof MarkerBlockEntity)) {
                set(i);
            }
        }
    }

    public void connect() {
        if (level == null) {
            return;
        }
        for (var axis : Direction.Axis.values()) {
            var run = true;
            for (int offset = 1; run && offset < 128; offset++) {
                for (int sign = -1; sign <= 1; sign += 2) {
                    var pos = getBlockPos().relative(axis, offset * sign);
                    if (level.getBlockEntity(pos) instanceof MarkerBlockEntity marker) {
                        if (marker.isEmpty()) {
                            set(axis.ordinal(), marker.selfPos, true);
                            marker.set(axis.ordinal(), selfPos, true);
                            run = false;
                            break;
                        }
                    }
                }
            }
        }
    }

    public void disconnect() {
        if (level != null) {
            for (int i = 0; i < Direction.Axis.values().length; i++) {
                if (isEmpty(i)) {
                    continue;
                }
                var pos = positions[i];
                if (level
                        .getBlockEntity(BlockPos.containing(pos.x, pos.y, pos.z)) instanceof MarkerBlockEntity marker) {
                    marker.set(i);
                }
                set(i);
            }
        }
    }

    public void interact() {
        OGTMA.LOGGER.info("Interacting");
        if (isEmpty()) {
            OGTMA.LOGGER.info("Connecting...");
            connect();
        } else {
            OGTMA.LOGGER.info("Disconnecting...");
            disconnect();
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        OGTMA.LOGGER.info("Loading marker data");
        super.load(tag);
        if (tag.contains(POSITIONS_TAG)) {
            var loaded = tag.getCompound(POSITIONS_TAG);
            for (int i = 0; i < positions.length; i++) {
                var vecTag = loaded.getCompound("Ind" + i);
                var vec = new Vector3f(vecTag.getFloat("X"), vecTag.getFloat("Y"), vecTag.getFloat("Z"));
                set(i, vec);
            }
        } else {
            for (int i = 0; i < positions.length; i++) {
                set(i);
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (isEmpty()) {
            return;
        }
        var pos = new CompoundTag();
        tag.put(POSITIONS_TAG, pos);
        for (int i = 0; i < positions.length; i++) {
            var vecTag = new CompoundTag();
            pos.put("Ind" + i, vecTag);
            vecTag.putFloat("X", positions[i].x);
            vecTag.putFloat("Y", positions[i].y);
            vecTag.putFloat("Z", positions[i].z);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        var tag = super.getUpdateTag();

        saveAdditional(tag);

        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
