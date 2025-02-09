package com.oe.ogtma.config.category;

import dev.toma.configuration.config.Configurable;

public class FeatureConfigs {

    @Configurable
    @Configurable.Comment({ "Whether to enable quarries", "Default: true" })
    public boolean enableQuarry = true;
    @Configurable
    @Configurable.Comment({ "Whether to enable auto-mufflers", "Default: true" })
    public boolean enableAutoMufflers = true;
}
