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

import com.oe.ogtma.OGTMA;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class MarkerBlockEntity extends BlockEntity {

    public static final String POSITIONS_TAG = "Positions";

    @Getter
    protected final BlockPos.MutableBlockPos[] positions = new BlockPos.MutableBlockPos[Direction.Axis.values().length];
    protected final long selfPos = getBlockPos().asLong();

    public MarkerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);

        for (int i = 0; i < positions.length; i++) {
            set(i);
        }
    }

    public void set(int index) {
        set(index, selfPos);
    }

    public void set(int index, long packedPos) {
        if (positions[index] != null) {
            if (positions[index].asLong() == packedPos) {
                return;
            }
            positions[index].set(packedPos);
        } else {
            positions[index] = BlockPos.of(packedPos).mutable();
        }
        OGTMA.LOGGER.info("[{}] Setting {} to {}", packedPos == selfPos ? "Clear" : "Set", index, positions[index]);
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            setChanged();
        }
    }

    public boolean isEmpty(int index) {
        return selfPos == positions[index].asLong();
    }

    public boolean isEmpty() {
        for (var pos : positions) {
            if (pos.asLong() != selfPos) {
                return false;
            }
        }
        return true;
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
            if (!(level.getBlockEntity(pos) instanceof MarkerBlockEntity)) {
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
                if (level.getBlockEntity(positions[i]) instanceof MarkerBlockEntity marker) {
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
    public void load(@NotNull CompoundTag tag) {
        OGTMA.LOGGER.info("Loading marker data");
        super.load(tag);
        if (tag.contains(POSITIONS_TAG)) {
            var positions = tag.getLongArray(POSITIONS_TAG);
            for (int i = 0; i < positions.length; i++) {
                set(i, positions[i]);
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
        tag.putLongArray(POSITIONS_TAG, Arrays.stream(positions).mapToLong(BlockPos::asLong).toArray());
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
