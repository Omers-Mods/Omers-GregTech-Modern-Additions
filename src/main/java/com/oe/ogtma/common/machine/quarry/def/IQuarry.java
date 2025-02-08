package com.oe.ogtma.common.machine.quarry.def;

import net.minecraftforge.fluids.capability.IFluidHandler;

import com.oe.ogtma.api.area.QuarryArea;
import com.oe.ogtma.common.entity.quarry.QuarryDrillEntity;

public interface IQuarry {

    int getQuarryStage();

    void setQuarryStage(int stage);

    QuarryFluidMode getQuarryFluidMode();

    void setQuarryFluidMode(QuarryFluidMode mode);

    boolean drainInput(boolean simulate);

    IFluidHandler getFluidHandler();

    QuarryArea getArea();

    QuarryDrillEntity getDrill();

    int getVoltageTier();

    void loadChunk(int x, int z);

    void unloadChunk(int x, int z);
}
