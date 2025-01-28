package com.oe.ogtma.api.utility;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class FilterUtil {

    public static final Predicate<ItemStack> BATTERY_SLOT = item -> GTCapabilityHelper.getElectricItem(item) != null ||
            (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE && GTCapabilityHelper.getForgeEnergyItem(item) != null);
}
