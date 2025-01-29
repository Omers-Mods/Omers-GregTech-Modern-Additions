package com.oe.ogtma.api.area;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

import com.oe.ogtma.common.machine.quarry.QuarryMachine;
import com.oe.ogtma.common.machine.quarry.def.IQuarry;
import com.oe.ogtma.common.machine.quarry.def.QuarryMode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

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
    public Iterator<BlockPos> iterator() {
        return new iterator(this);
    }

    public static class iterator implements Iterator<BlockPos> {

        @Getter
        protected int minX, minY, minZ, maxX, maxY, maxZ;
        @Getter
        @Setter
        protected int x, y, z;
        protected boolean xPos, zPos;
        @Getter
        @Setter
        protected boolean yPriority;

        public iterator(QuarryArea area) {
            minX = area.getMinX();
            minY = area.quarry.getQuarryStage() == QuarryMachine.QUARRYING ?
                    area.quarry.getLevel().getMinBuildHeight() : area.getMinY();
            minZ = area.getMinZ();
            maxX = area.getMaxX();
            y = maxY = area.getMaxY();
            maxZ = area.getMaxZ();
            yPriority = area.quarry.getQuarryMode() == QuarryMode.VERTICAL;
            var pos = area.quarry.getPos();
            x = Mth.clamp(pos.getX(), minX, maxX);
            z = Mth.clamp(pos.getZ(), minZ, maxZ);
            xPos = x == minX;
            zPos = z == minZ;
        }

        public int nextX() {
            if (xPos) {
                var p = x + 1;
                return p >= maxX ? x : p;
            } else {
                var n = x - 1;
                return n <= minX ? x : n;
            }
        }

        public int nextY() {
            var n = y - 1;
            return n <= minY ? y : n;
        }

        public int nextZ() {
            if (zPos) {
                var p = z + 1;
                return p >= maxZ ? z : p;
            } else {
                var n = z - 1;
                return n <= minZ ? z : n;
            }
        }

        // todo: make it chunk based (dig out a chunk then move to the next)
        @Override
        public boolean hasNext() {
            return x != nextX() || y != nextY() || z != nextZ();
        }

        @Override
        public BlockPos next() {
            if (yPriority) {
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
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuarryArea)) return false;
        return super.equals(o);
    }
}
