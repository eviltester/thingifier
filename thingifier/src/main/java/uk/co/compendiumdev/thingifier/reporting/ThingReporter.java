package uk.co.compendiumdev.thingifier.reporting;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.reporting.ERModelReport;


public class ThingReporter {

    private Thingifier thingifier;

    public ThingReporter(Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    public String basicReport() {

        return new ERModelReport(thingifier.getERmodel()).
                        asMarkdown();
    }
}
