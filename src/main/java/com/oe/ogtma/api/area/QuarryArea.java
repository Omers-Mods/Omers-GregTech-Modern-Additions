package com.oe.ogtma.api.area;

import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.INBTSerializable;

import com.oe.ogtma.common.machine.quarry.QuarryMachine;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

@MethodsReturnNonnullByDefault
@NoArgsConstructor
@Accessors(fluent = true)
@Data
public class QuarryArea extends Area implements Iterable<Iterator<BlockPos>> {

    protected AABB cachedViewBox;
    protected int minBuildHeight = 0;
    protected boolean quarrying = false;
    protected int qX, qY, qZ;

    public AABB getViewBox() {
        if (cachedViewBox == null) {
            cachedViewBox = new AABB(minX, quarrying ? minBuildHeight : minY, minZ, maxX, maxY, maxZ);
        }
        return cachedViewBox;
    }

    public void setFromQuarry(QuarryMachine quarry) {
        this.minBuildHeight = quarry.getLevel().getMinBuildHeight();
        this.quarrying = quarry.getQuarryStage() == QuarryMachine.QUARRYING;
        var pos = quarry.getPos();
        this.qX = pos.getX();
        this.qY = pos.getY();
        this.qZ = pos.getZ();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = super.serializeNBT();
        tag.putInt("MinBuild", minBuildHeight);
        tag.putBoolean("Quarrying", quarrying);
        tag.putIntArray("QuarryPos", new int[] { qX, qY, qZ });
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        this.minBuildHeight = tag.getInt("MinBuild");
        this.quarrying = tag.getBoolean("Quarrying");
        var qPos = tag.getIntArray("QuarryPos");
        if (qPos.length == 3) {
            this.qX = qPos[0];
            this.qY = qPos[1];
            this.qZ = qPos[2];
        }
    }

    @NotNull
    @Override
    public AreaChunkIterator iterator() {
        return new AreaChunkIterator(this);
    }

    public static class ChunkBlockIterator implements Iterator<BlockPos>, INBTSerializable<CompoundTag>,
                                           ITagSerializable<CompoundTag> {

        @Getter
        protected int minX, minY, minZ, maxX, maxY, maxZ;
        @Getter
        @Setter
        protected int x, y, z;
        protected boolean xPos, zPos;
        protected boolean first;

        public ChunkBlockIterator(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean xPos,
                                  boolean zPos) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.y = this.maxY = maxY;
            this.maxZ = maxZ;
            this.xPos = xPos;
            this.zPos = zPos;
            x = xPos ? minX : maxX;
            z = zPos ? minZ : maxZ;
            first = true;
        }

        public int nextX() {
            if (xPos) {
                var p = x + 1;
                return p > maxX ? x : p;
            } else {
                var n = x - 1;
                return n < minX ? x : n;
            }
        }

        public int nextY() {
            var n = y - 1;
            return n < minY ? y : n;
        }

        public int nextZ() {
            if (zPos) {
                var p = z + 1;
                return p > maxZ ? z : p;
            } else {
                var n = z - 1;
                return n < minZ ? z : n;
            }
        }

        @Override
        public boolean hasNext() {
            return x != nextX() || y != nextY() || z != nextZ();
        }

        @Override
        public BlockPos next() {
            if (first) {
                first = false;
            } else {
                var nextX = nextX();
                if (nextX == x) {
                    xPos = !xPos;
                    var nextZ = nextZ();
                    if (nextZ == z) {
                        zPos = !zPos;
                        y = nextY();
                    } else {
                        z = nextZ;
                    }
                } else {
                    x = nextX;
                }
            }
            return new BlockPos(x, y, z);
        }

        @Override
        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            tag.putIntArray("Values", new int[] { x, y, z, minX, minY, minZ, maxX, maxY, maxZ });
            if (xPos) {
                tag.putBoolean("XPos", true);
            }
            if (zPos) {
                tag.putBoolean("ZPos", true);
            }
            if (first) {
                tag.putBoolean("First", true);
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            var values = tag.getIntArray("Values");
            if (values.length == 9) {
                x = values[0];
                y = values[1];
                z = values[2];
                minX = values[3];
                minY = values[4];
                minZ = values[5];
                maxX = values[6];
                maxY = values[7];
                maxZ = values[8];
            }
            xPos = tag.getBoolean("XPos");
            zPos = tag.getBoolean("ZPos");
            first = tag.getBoolean("First");
        }
    }

    public static class AreaChunkIterator implements Iterator<Iterator<BlockPos>>, INBTSerializable<CompoundTag>,
                                          ITagSerializable<CompoundTag> {

        protected int minX, minY, minZ, maxX, maxY, maxZ;
        protected int x, z;
        protected boolean xPos, zPos;
        protected boolean first;

        public AreaChunkIterator(QuarryArea area) {
            minX = area.minX + (area.quarrying ? 1 : 0);
            minY = area.quarrying ? area.minBuildHeight : area.minY;
            minZ = area.minZ + (area.quarrying ? 1 : 0);
            maxX = area.maxX - (area.quarrying ? 1 : 0);
            maxY = area.quarrying ? area.minY - 1 : area.maxY;
            maxZ = area.maxZ - (area.quarrying ? 1 : 0);
            x = Mth.clamp(area.qX, minX, maxX);
            z = Mth.clamp(area.qZ, minZ, maxZ);
            xPos = x == minX;
            zPos = z == minZ;
            first = true;
        }

        public int nextX() {
            if (xPos) {
                var p = x + 16;
                return (p >> 4) > (maxX >> 4) ? x : p;
            } else {
                var n = x - 16;
                return (n >> 4) < (minX >> 4) ? x : n;
            }
        }

        public int nextZ() {
            if (zPos) {
                var p = z + 16;
                return (p >> 4) > (maxZ >> 4) ? z : p;
            } else {
                var n = z - 16;
                return (n >> 4) < (minZ >> 4) ? z : n;
            }
        }

        @Override
        public boolean hasNext() {
            return x != nextX() || z != nextZ();
        }

        @Override
        public ChunkBlockIterator next() {
            if (first) {
                first = false;
            } else {
                var nextX = nextX();
                if (nextX == x) {
                    xPos = !xPos;
                    z = nextZ();
                } else {
                    x = nextX;
                }
            }
            var chunkPos = new ChunkPos(x >> 4, z >> 4);
            var minX = Math.max(this.minX, chunkPos.getMinBlockX());
            var minZ = Math.max(this.minZ, chunkPos.getMinBlockZ());
            var maxX = Math.min(this.maxX, chunkPos.getMaxBlockX());
            var maxZ = Math.min(this.maxZ, chunkPos.getMaxBlockZ());
            return new ChunkBlockIterator(minX, minY, minZ, maxX, maxY, maxZ, xPos, zPos);
        }

        public boolean isEdge(BlockPos pos) {
            return isEdge(pos.getX(), pos.getY(), pos.getZ());
        }

        public boolean isEdge(int x, int y, int z) {
            var isEdgeX = x == minX || x == maxX;
            var isEdgeY = y == minY || y == maxY;
            if (isEdgeX && isEdgeY) {
                return true;
            }
            var isEdgeZ = z == minZ || z == maxZ;
            return isEdgeZ && (isEdgeX || isEdgeY);
        }

        @Override
        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            tag.putIntArray("Values", new int[] { minX, minY, minZ, maxX, maxY, maxZ, x, z });
            if (xPos) {
                tag.putBoolean("XPos", true);
            }
            if (zPos) {
                tag.putBoolean("ZPos", true);
            }
            if (first) {
                tag.putBoolean("First", true);
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("Values")) {
                var values = tag.getIntArray("Values");
                if (values.length == 8) {
                    minX = values[0];
                    minY = values[1];
                    minZ = values[2];
                    maxX = values[3];
                    maxY = values[4];
                    maxZ = values[5];
                    x = values[6];
                    z = values[7];
                }
            }
        }
    }

    @Override
    public final boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof QuarryArea area)) return false;
        if (!super.equals(object)) return false;

        return minBuildHeight == area.minBuildHeight && quarrying == area.quarrying && qX == area.qX && qY == area.qY &&
                qZ == area.qZ;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + minBuildHeight;
        result = 31 * result + Boolean.hashCode(quarrying);
        result = 31 * result + qX;
        result = 31 * result + qY;
        result = 31 * result + qZ;
        return result;
    }
}
