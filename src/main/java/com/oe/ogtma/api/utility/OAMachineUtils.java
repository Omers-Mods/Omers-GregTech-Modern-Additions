package com.oe.ogtma.api.utility;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;

public class OAMachineUtils {

    public static Int2IntFunction inventorySizeScaling = tier -> (tier + 1) * (tier + 1);
    public static Int2IntFunction blocksPerIterationScaling = tier -> tier * tier + 1;
    public static Int2IntFunction quarrySpeedScaling = tier -> 120 / tier;
}
