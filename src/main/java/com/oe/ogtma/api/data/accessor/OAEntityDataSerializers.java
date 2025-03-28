package com.oe.ogtma.api.data.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class OAEntityDataSerializers {

    public static final EntityDataSerializer<AABB> AABB = EntityDataSerializer.simple(
            (buf, aabb) -> {
                buf.writeDouble(aabb.minX);
                buf.writeDouble(aabb.minY);
                buf.writeDouble(aabb.minZ);
                buf.writeDouble(aabb.maxX);
                buf.writeDouble(aabb.maxY);
                buf.writeDouble(aabb.maxZ);
            }, buf -> new AABB(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble()));
    public static final EntityDataSerializer<BlockPos[]> BLOCK_POS_ARRAY = EntityDataSerializer.simple(
            (buf, arr) -> {
                buf.writeInt(arr.length);
                for (var pos : arr) {
                    buf.writeBlockPos(pos);
                }
            }, buf -> {
                var arr = new BlockPos[buf.readInt()];
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = buf.readBlockPos();
                }
                return arr;
            });
    public static final EntityDataSerializer<Vec3> VEC3 = EntityDataSerializer.simple((buf, v) -> {
        buf.writeDouble(v.x);
        buf.writeDouble(v.y);
        buf.writeDouble(v.z);
    }, buf -> new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));

    public static void init() {
        EntityDataSerializers.registerSerializer(AABB);
        EntityDataSerializers.registerSerializer(BLOCK_POS_ARRAY);
        EntityDataSerializers.registerSerializer(VEC3);
    }
}
