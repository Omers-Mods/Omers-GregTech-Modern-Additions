package com.oe.ogtma.api.area;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.INBTSerializable;

import com.oe.ogtma.common.blockentity.MarkerBlockEntity;
import lombok.NoArgsConstructor;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class QuarryArea extends Area {

    protected final int minBuildHeight;
    protected final int maxBuildHeight;
    protected final AABB viewBox;

    public QuarryArea(Level level, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        super(minX, maxX, minY, maxY, minZ, maxZ);
        this.minBuildHeight = level.getMinBuildHeight();
        this.maxBuildHeight = level.getMaxBuildHeight();
        this.viewBox = new AABB(minX, minBuildHeight, minZ, maxX, maxBuildHeight, maxZ);
    }

    public QuarryArea(MarkerBlockEntity marker) {
        super(marker);
        this.minBuildHeight = marker.getLevel().getMinBuildHeight();
        this.maxBuildHeight = marker.getLevel().getMaxBuildHeight();
        this.viewBox = new AABB(minX, minBuildHeight, minZ, maxX, maxBuildHeight, maxZ);
    }

    public class Iterators {

        @NoArgsConstructor
        public class Volume implements Iterator<Surface>, INBTSerializable<CompoundTag> {

            protected int y;
            protected boolean up;
            protected boolean ignoreLimit;

            public Volume(int y, boolean up, boolean ignoreLimit) {
                this.y = y;
                this.up = up;
                this.ignoreLimit = ignoreLimit;
            }

            @Override
            public boolean hasNext() {
                if (up) {
                    return (y + 1) <= (ignoreLimit ? maxBuildHeight : maxY);
                } else {
                    return (y - 1) > (ignoreLimit ? minBuildHeight : minY);
                }
            }

            @Override
            public Surface next() {
                return new Surface(minX, y, minZ, true, true);
            }

            @Override
            public CompoundTag serializeNBT() {
                var tag = new CompoundTag();
                tag.putInt("Y", y);
                tag.putBoolean("Positive", up);
                tag.putBoolean("IgnoreLimit", ignoreLimit);
                return tag;
            }

            @Override
            public void deserializeNBT(CompoundTag tag) {
                y = tag.getInt("Y");
                up = tag.getBoolean("Positive");
                ignoreLimit = tag.getBoolean("IgnoreLimit");
            }
        }

        @NoArgsConstructor
        public class Surface implements Iterator<BlockPos>, INBTSerializable<CompoundTag> {

            protected BlockPos.MutableBlockPos pos;
            protected boolean xPos;
            protected boolean zPos;

            public Surface(int x, int y, int z, boolean xPos, boolean zPos) {
                this.pos = new BlockPos.MutableBlockPos(x, y, z);
                this.xPos = xPos;
                this.zPos = zPos;
            }

            public int nextX() {
                if (xPos) {
                    var p = pos.getX() + 1;
                    return p >= maxX ? pos.getX() : p;
                } else {
                    var n = pos.getX() - 1;
                    return n <= minX ? pos.getX() : n;
                }
            }

            public int nextZ() {
                if (zPos) {
                    var p = pos.getZ() + 1;
                    return p >= maxZ ? pos.getZ() : p;
                } else {
                    var n = pos.getZ() - 1;
                    return n <= minZ ? pos.getZ() : n;
                }
            }

            @Override
            public boolean hasNext() {
                if (nextX() == pos.getX()) {
                    return nextZ() != pos.getZ();
                }
                return true;
            }

            @Override
            public BlockPos next() {
                var x = nextX();
                if (x == pos.getX()) {
                    xPos = !xPos;
                    var z = nextZ();
                    if (z == pos.getZ()) {
                        throw new NoSuchElementException();
                    }
                    pos.setZ(z);
                } else {
                    pos.setX(x);
                }
                return pos.immutable();
            }

            @Override
            public CompoundTag serializeNBT() {
                var tag = new CompoundTag();
                tag.putInt("X", pos.getX());
                tag.putInt("Y", pos.getY());
                tag.putInt("Z", pos.getZ());
                tag.putBoolean("XPositive", xPos);
                tag.putBoolean("ZPositive", zPos);
                return tag;
            }

            @Override
            public void deserializeNBT(CompoundTag tag) {
                pos = new BlockPos.MutableBlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
                xPos = tag.getBoolean("XPositive");
                zPos = tag.getBoolean("ZPositive");
            }
        }
    }
}
