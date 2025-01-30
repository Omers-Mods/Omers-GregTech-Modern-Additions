package com.oe.ogtma.common.machine.quarry.def;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.oe.ogtma.api.area.QuarryArea;
import com.oe.ogtma.common.entity.quarry.QuarryDrillEntity;

public interface IQuarry {

    int getQuarryStage();

    void setQuarryStage(int stage);

    QuarryMode getQuarryMode();

    void setQuarryMode(QuarryMode mode);

    QuarryFluidMode getQuarryFluidMode();

    void setQuarryFluidMode(QuarryFluidMode mode);

    boolean drainInput(boolean simulate);

    IFluidHandler getFluidHandler();

    QuarryArea getArea();

    QuarryDrillEntity getDrill();

    Level getLevel();

    BlockPos getPos();

    int getVoltageTier();
}
