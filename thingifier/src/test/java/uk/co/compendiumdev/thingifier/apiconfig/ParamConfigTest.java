package uk.co.compendiumdev.thingifier.apiconfig;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParamConfigTest {

    @Test
    public void pagingDefaultsAreEnabledWithConfiguredLimits() {
        ParamConfig config = new ParamConfig();

        Assertions.assertTrue(config.willAllowPagingThroughUrlParams());
        Assertions.assertEquals(10, config.defaultPagingLimit());
        Assertions.assertEquals(20, config.maxPagingLimit());
    }

    @Test
    public void canCopyPagingConfig() {
        ParamConfig source = new ParamConfig();
        source.setAllowPagingThroughUrlParams(false);
        source.setDefaultPagingLimit(3);
        source.setMaxPagingLimit(7);

        ParamConfig target = new ParamConfig();
        target.setFrom(source);

        Assertions.assertFalse(target.willAllowPagingThroughUrlParams());
        Assertions.assertEquals(3, target.defaultPagingLimit());
        Assertions.assertEquals(7, target.maxPagingLimit());
    }
}
