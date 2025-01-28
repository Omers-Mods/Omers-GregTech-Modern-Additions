package com.oe.ogtma.common.machine.quarry.def;

import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import com.oe.ogtma.OGTMA;
import lombok.Getter;

@Getter
public enum QuarryFluidMode implements EnumSelectorWidget.SelectableEnum {

    COLLECT,
    STOP,
    VOID;

    private static final String lang = "ogtma.option.quarry_fluid_mode.";
    private static final String rl = "textures/gui/button/quarry_fluid_mode/";

    QuarryFluidMode() {
        var name = name().toLowerCase();
        tooltip = lang + name;
        icon = new ResourceTexture(OGTMA.id(rl + name + ".png"));
    }

    private final String tooltip;
    private final IGuiTexture icon;

    public static QuarryFluidMode get(int ordinal) {
        return values()[ordinal % values().length];
    }
}
