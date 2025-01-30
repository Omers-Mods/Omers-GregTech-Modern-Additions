package com.oe.ogtma.api.area;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.INBTSerializable;

import com.oe.ogtma.common.machine.quarry.QuarryMachine;
import com.oe.ogtma.common.machine.quarry.def.IQuarry;
import com.oe.ogtma.common.machine.quarry.def.QuarryMode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

@MethodsReturnNonnullByDefault
@NoArgsConstructor
public class QuarryArea extends Area implements Iterable<BlockPos> {

    protected AABB cachedViewBox;
    @Setter
    protected IQuarry quarry;

    public QuarryArea(IQuarry quarry) {
        this.quarry = quarry;
    }

    public AABB getViewBox() {
        if (cachedViewBox == null) {
            var minY = quarry == null || quarry.getQuarryStage() != QuarryMachine.QUARRYING ? this.minY :
                    quarry.getLevel().getMinBuildHeight() + 1;
            cachedViewBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        }
        return cachedViewBox;
    }

    @NotNull
    @Override
    public QuarryAreaIterator iterator() {
        return new QuarryAreaIterator(this);
    }

    public static class QuarryAreaIterator implements Iterator<BlockPos>, INBTSerializable<CompoundTag> {

        @Getter
        protected int minX, minY, minZ, maxX, maxY, maxZ;
        @Getter
        @Setter
        protected int x, y, z;
        protected boolean xPos, zPos;
        @Getter
        @Setter
        protected boolean yPriority;
        protected boolean first;

        public QuarryAreaIterator(QuarryArea area) {
            var quarrying = area.quarry.getQuarryStage() == QuarryMachine.QUARRYING;
            minX = area.getMinX() + (quarrying ? 1 : 0);
            minY = quarrying ? area.quarry.getLevel().getMinBuildHeight() : area.getMinY();
            minZ = area.getMinZ() + (quarrying ? 1 : 0);
            maxX = area.getMaxX() - (quarrying ? 1 : 0);
            y = maxY = quarrying ? area.getMinY() : area.getMaxY();
            maxZ = area.getMaxZ() - (quarrying ? 1 : 0);
            yPriority = area.quarry.getQuarryMode() == QuarryMode.VERTICAL;
            var pos = area.quarry.getPos();
            x = Mth.clamp(pos.getX(), minX, maxX);
            z = Mth.clamp(pos.getZ(), minZ, maxZ);
            xPos = x == minX;
            zPos = z == minZ;
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

        // todo: make it chunk based (dig out a chunk then move to the next)
        @Override
        public boolean hasNext() {
            return x != nextX() || y != nextY() || z != nextZ();
        }

        @Override
        public BlockPos next() {
            if (first) {
                first = false;
            } else if (yPriority) {
                var nextY = nextY();
                if (nextY == y) {
                    y = maxY;
                    var nextX = nextX();
                    if (nextX == x) {
                        xPos = !xPos;
                        z = nextZ();
                    } else {
                        x = nextX;
                    }
                } else {
                    y = nextY;
                }
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
            if (yPriority) {
                tag.putBoolean("YPriority", true);
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
                xPos = tag.getBoolean("XPos");
                zPos = tag.getBoolean("ZPos");
                yPriority = tag.getBoolean("YPriority");
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuarryArea)) return false;
        return super.equals(o);
    }
}
