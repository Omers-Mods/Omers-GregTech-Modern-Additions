package com.oe.ogtma.api.area;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import com.oe.ogtma.common.blockentity.MarkerBlockEntity;
import lombok.*;

import java.util.Arrays;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Area implements INBTSerializable<CompoundTag> {

    private static final String AREA_TAG = "Area";

    protected int minX, maxX, minY, maxY, minZ, maxZ;

    public Area(MarkerBlockEntity marker) {
        var pos = marker.getBlockPos();
        minX = maxX = pos.getX();
        minY = maxY = pos.getY();
        minZ = maxZ = pos.getZ();
        for (var v : marker.getPositions()) {
            var x = (int) v.x;
            var y = (int) v.y;
            var z = (int) v.z;
            if (x < minX) {
                minX = x;
            } else if (maxX < x) {
                maxX = x;
            }
            if (y < minY) {
                minY = y;
            } else if (maxY < y) {
                maxY = y;
            }
            if (z < minZ) {
                minZ = z;
            } else if (maxZ < z) {
                maxZ = z;
            }
        }
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
}
