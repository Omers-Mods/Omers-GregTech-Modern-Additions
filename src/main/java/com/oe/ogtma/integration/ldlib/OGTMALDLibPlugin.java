package com.oe.ogtma.integration.ldlib;

import com.lowdragmc.lowdraglib.plugin.ILDLibPlugin;
import com.lowdragmc.lowdraglib.plugin.LDLibPlugin;

import com.oe.ogtma.common.data.SyncedFieldAccessors;

@LDLibPlugin
public class OGTMALDLibPlugin implements ILDLibPlugin {

    @Override
    public void onLoad() {
        SyncedFieldAccessors.init();
    }
}
