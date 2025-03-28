package com.oe.ogtma.api.area;

import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import com.oe.ogtma.common.blockentity.marker.MarkerBlockEntity;
import lombok.*;

import java.util.Arrays;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Area implements INBTSerializable<CompoundTag>, ITagSerializable<CompoundTag> {

    private static final String AREA_TAG = "Area";

    protected int minX, maxX, minY, maxY, minZ, maxZ;

    public void setFromMarker(MarkerBlockEntity marker, Direction.Axis... directions) {
        var coords = marker.getArea(directions);
        minX = coords[0];
        minY = coords[1];
        minZ = coords[2];
        maxX = coords[3];
        maxY = coords[4];
        maxZ = coords[5];
    }

    public boolean isEmpty() {
        return minX == maxX && minY == maxY && minZ == maxZ;
    }

    public int getXSize() {
        return maxX - minX;
    }

    public int getYSize() {
        return maxY - minY;
    }

    public int getZSize() {
        return maxZ - minZ;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putIntArray(AREA_TAG, Arrays.asList(minX, maxX, minY, maxY, minZ, maxZ));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        var arr = tag.getIntArray(AREA_TAG);
        if (arr.length >= 6) {
            minX = arr[0];
            maxX = arr[1];
            minY = arr[2];
            maxY = arr[3];
            minZ = arr[4];
            maxZ = arr[5];
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Area area)) return false;

        return getMinX() == area.getMinX() && getMaxX() == area.getMaxX() && getMinY() == area.getMinY() &&
                getMaxY() == area.getMaxY() && getMinZ() == area.getMinZ() && getMaxZ() == area.getMaxZ();
    }

    @Override
    public int hashCode() {
        int result = getMinX();
        result = 31 * result + getMaxX();
        result = 31 * result + getMinY();
        result = 31 * result + getMaxY();
        result = 31 * result + getMinZ();
        result = 31 * result + getMaxZ();
        return result;
    }
}
