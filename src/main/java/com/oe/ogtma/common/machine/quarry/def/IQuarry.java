package com.oe.ogtma.common.machine.quarry.def;

import com.oe.ogtma.api.area.QuarryArea;

public interface IQuarry {

    QuarryMode getQuarryMode();

    void setQuarryMode(QuarryMode mode);

    default void toggleQuarryMode() {
        toggleQuarryMode(true);
    }

    default void toggleQuarryMode(boolean plus) {
        var modes = QuarryMode.values();
        setQuarryMode(modes[(getQuarryMode().ordinal() + (plus ? 1 : -1)) % modes.length]);
    }

    QuarryFluidMode getQuarryFluidMode();

    void setQuarryFluidMode(QuarryFluidMode mode);

    default void toggleQuarryFluidMode() {
        toggleQuarryFluidMode(true);
    }

    default void toggleQuarryFluidMode(boolean plus) {
        var modes = QuarryFluidMode.values();
        setQuarryFluidMode(modes[(getQuarryFluidMode().ordinal() + (plus ? 1 : -1)) % modes.length]);
    }

    int getSpeed();

    int getFortune();

    QuarryArea getArea();
}
