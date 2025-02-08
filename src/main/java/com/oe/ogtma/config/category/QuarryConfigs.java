package com.oe.ogtma.config.category;

import dev.toma.configuration.config.Configurable;

public class QuarryConfigs {

    @Configurable
    @Configurable.Comment({ " Render quarry drill entity (small performance impact) ", "Default: true" })
    public boolean renderQuarryDrill = true;
}
