package com.oe.ogtma.api.utility;

import net.minecraft.server.level.ServerLevel;

import com.google.common.math.Stats;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TimeUtil {

    @SuppressWarnings("UnstableApiUsage")
    public static double getMeanTickTime(@NotNull ServerLevel level) {
        return Stats.meanOf(Objects.requireNonNull(level.getServer()).tickTimes) * 1.0E-6D;
    }
}
