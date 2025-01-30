package com.oe.ogtma.api.utility;

import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3f;

public class MathUtils {

    public static Pair<Vector3f, Float> getRotationAxisAndAngle(Vector3f from, Vector3f to) {
        var angle = (float) Math.acos(from.dot(to));
        return Pair.of(from.cross(to).normalize(), angle);
    }
}
