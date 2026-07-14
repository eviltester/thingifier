package uk.co.compendiumdev.thingifier.adapter.bootstrap;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.spark.SparkHttpGenericExceptionRoutings;
import uk.co.compendiumdev.thingifier.adapter.spark.ThingifierAutoDocGenRouting;
import uk.co.compendiumdev.thingifier.adapter.spark.ThingifierHttpApiRoutings;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public final class ThingifierServerBootstrap {

    public ThingifierHttpApiRoutings startRestServer(
            final Thingifier thingifier,
            final ThingifierApiDocumentationDefn apiDefn,
            final DefaultGUIHTML guiManagement) {

        apiDefn.setThingifier(thingifier);

        new ThingifierAutoDocGenRouting(thingifier, apiDefn, guiManagement);
        ThingifierHttpApiRoutings restServer = new ThingifierHttpApiRoutings(thingifier, apiDefn);
        new SparkHttpGenericExceptionRoutings();

        return restServer;
    }
}
