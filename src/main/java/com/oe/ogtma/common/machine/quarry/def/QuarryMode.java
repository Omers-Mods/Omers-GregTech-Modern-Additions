package com.oe.ogtma.common.machine.quarry.def;

import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import com.oe.ogtma.OGTMA;
import lombok.Getter;

@Getter
public enum QuarryMode implements EnumSelectorWidget.SelectableEnum {

    VERTICAL,
    HORIZONTAL;

    private static final String lang = "ogtma.option.quarry_mode.";
    private static final String rl = "textures/gui/button/quarry_mode/";

    QuarryMode() {
        var name = name().toLowerCase();
        this.tooltip = lang + name;
        this.icon = new ResourceTexture(OGTMA.id(rl + name + ".png"));
    }

    private final String tooltip;
    private final IGuiTexture icon;

    public static QuarryMode next(int ordinal) {
        return values()[ordinal % values().length];
    }
}
