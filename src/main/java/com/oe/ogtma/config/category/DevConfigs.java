package com.oe.ogtma.config.category;

import dev.toma.configuration.config.Configurable;

public class DevConfigs {

    @Configurable
    @Configurable.Comment({ "Enable debug loaded chunk overlay", "Default: false" })
    public boolean loadedChunkOverlay = false;
}
