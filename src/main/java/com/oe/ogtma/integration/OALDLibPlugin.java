package com.oe.ogtma.integration;

import com.lowdragmc.lowdraglib.plugin.ILDLibPlugin;
import com.lowdragmc.lowdraglib.plugin.LDLibPlugin;

import com.oe.ogtma.common.data.OASyncedFieldAccessors;

@LDLibPlugin
public class OALDLibPlugin implements ILDLibPlugin {

    @Override
    public void onLoad() {
        OASyncedFieldAccessors.init();
    }
}
