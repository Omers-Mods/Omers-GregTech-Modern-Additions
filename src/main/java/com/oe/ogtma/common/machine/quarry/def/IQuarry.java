package com.oe.ogtma.common.machine.quarry.def;

import net.minecraftforge.fluids.capability.IFluidHandler;

public interface IQuarry {

    QuarryMode getQuarryMode();

    void setQuarryMode(QuarryMode mode);

    QuarryFluidMode getQuarryFluidMode();

    void setQuarryFluidMode(QuarryFluidMode mode);

    boolean drainInput(boolean simulate);
    
    IFluidHandler getFluidHandler();
}
