package com.oe.ogtma.common.blockentity.marker;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.oe.ogtma.OGTMA;
import com.oe.ogtma.common.block.marker.WallMarkerBlock;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MarkerBlockEntity extends BlockEntity {

    public static final Map<Direction, Vec3> DIRECTION_OFFSET = Maps.immutableEnumMap(ImmutableMap.of(
            Direction.NORTH, new Vec3(.5, .7, .75),
            Direction.SOUTH, new Vec3(.5, .7, .25),
            Direction.WEST, new Vec3(.75, .7, .5),
            Direction.EAST, new Vec3(.25, .7, .5),
            Direction.DOWN, new Vec3(.5, .6, .5),
            Direction.UP, new Vec3(0, 0, 0)));

    public static final String POSITIONS_TAG = "Positions";
    public static final String DIRTY_TAG = "Dirty";

    @Getter
    protected final Vec3[] positions = new Vec3[Direction.Axis.values().length];
    protected byte dirty = 0;
    @Getter
    protected final Vec3 selfPos;

    public MarkerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        var offset = DIRECTION_OFFSET.get(getOrientation());
        this.selfPos = new Vec3(pos.getX() + offset.x, pos.getY() + offset.y, pos.getZ() + offset.z);
        for (int i = 0; i < positions.length; i++) {
            set(i);
        }
    }

    public Vec3 get(Direction.Axis axis) {
        return get(axis.ordinal());
    }

    public Vec3 get(int index) {
        return positions[index];
    }

    public void set(int index) {
        set(index, selfPos);
    }

    public void set(int index, Vec3 pos) {
        if (pos.equals(positions[index])) {
            return;
        }

        positions[index] = pos;
        dirty |= (byte) (1 << index);
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

    public boolean hasConnections(Direction.Axis... axes) {
        for (var axis : axes) {
            if (positions[axis.ordinal()].equals(selfPos)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param axes directional markers to form a cube from
     * @return {@code int[6]} containing minimum x,y,z followed by maximum x,y,z
     */
    public int[] getArea(Direction.Axis... axes) {
        var pos = getBlockPos();
        int x0, x1, y0, y1, z0, z1;
        x0 = x1 = pos.getX();
        y0 = y1 = pos.getY();
        z0 = z1 = pos.getZ();
        for (var direction : axes) {
            var v = positions[direction.ordinal()];
            if (v.equals(selfPos)) {
                continue;
            }
            x0 = Math.min(x0, (int) v.x);
            y0 = Math.min(y0, (int) v.y);
            z0 = Math.min(z0, (int) v.z);
            x1 = Math.max(x1, (int) v.x);
            y1 = Math.max(y1, (int) v.y);
            z1 = Math.max(z1, (int) v.z);
        }
        return new int[] { x0, y0, z0, x1, y1, z1 };
    }

    public void updateConnections() {
        if (level == null || level.isClientSide || level.getGameTime() % 10 != 0) {
            return;
        }
        OGTMA.LOGGER.info("Updating connections...");
        for (int i = 0; i < positions.length; i++) {
            OGTMA.LOGGER.info("Connection axis {}", Direction.Axis.values()[i]);
            if (isEmpty(i)) {
                OGTMA.LOGGER.info("Is empty");
                continue;
            }
            var pos = positions[i];
            if (!(level.getBlockEntity(BlockPos.containing(pos.x, pos.y, pos.z)) instanceof MarkerBlockEntity)) {
                OGTMA.LOGGER.info("Isn't a marker anymore");
                set(i);
            }
        }
    }

    public void destroy(Direction.Axis... axes) {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (var axis : axes) {
            if (isEmpty(axis.ordinal())) {
                continue;
            }
            var pos = get(axis);
            serverLevel.destroyBlock(BlockPos.containing(pos.x, pos.y, pos.z), true);
        }
        serverLevel.destroyBlock(getBlockPos(), true);
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
                            set(axis.ordinal(), marker.selfPos);
                            marker.set(axis.ordinal(), selfPos);
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
        if (isEmpty()) {
            connect();
        } else {
            disconnect();
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(POSITIONS_TAG)) {
            var buffer = ByteBuffer.wrap(tag.getByteArray(POSITIONS_TAG));
            if (tag.contains(DIRTY_TAG)) {
                var dirty = tag.getByte(DIRTY_TAG);
                for (int i = 0; i < positions.length; i++) {
                    if ((dirty & (1 << i)) > 0) {
                        set(i, new Vec3(buffer.getDouble(), buffer.getDouble(), buffer.getDouble()));
                    }
                }
            } else {
                for (int i = 0; i < positions.length; i++) {
                    set(i, new Vec3(buffer.getDouble(), buffer.getDouble(), buffer.getDouble()));
                }
            }
        } else {
            for (int i = 0; i < positions.length; i++) {
                set(i);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (isEmpty()) {
            return;
        }
        var buffer = ByteBuffer.allocate(positions.length * 3 * Double.SIZE / 8);
        for (var pos : positions) {
            buffer.putDouble(pos.x).putDouble(pos.y).putDouble(pos.z);
        }
        var posTag = new ByteArrayTag(buffer.array());
        tag.put(POSITIONS_TAG, posTag);
    }

    protected void saveEfficient(CompoundTag tag) {
        super.saveAdditional(tag);
        var count = 0;
        for (int i = 0; i < positions.length; i++) {
            if ((dirty & (1 << i)) != 0) {
                count++;
            }
        }
        var buffer = ByteBuffer.allocate(count * 3 * Double.SIZE / 8);
        for (int i = 0; i < positions.length; i++) {
            if ((dirty & (1 << i)) != 0) {
                var pos = positions[i];
                buffer.putDouble(pos.x).putDouble(pos.y).putDouble(pos.z);
            }
        }
        tag.putByte(DIRTY_TAG, dirty);
        tag.putByteArray(POSITIONS_TAG, buffer.array());
        dirty = 0;
    }

    @Override
    public CompoundTag getUpdateTag() {
        var tag = super.getUpdateTag();

        if (dirty != 0) {
            saveEfficient(tag);
        } else {
            saveAdditional(tag);
        }

        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
