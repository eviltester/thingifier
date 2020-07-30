package uk.co.compendiumdev.thingifier.tactical.postmanreplication;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import spark.Spark;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.application.ThingifierRestServer;
import uk.co.compendiumdev.thingifier.application.examples.TodoManagerThingifier;
import uk.co.compendiumdev.thingifier.htmlgui.DefaultGUIHTML;
import uk.co.compendiumdev.sparkstart.Port;

import java.util.ArrayList;
import java.util.List;

public class Environment {

    /**
     *  could just use `RestAssured.baseURI = Environment.getBaseUri();` instead
     */

    public static String getEnv(String urlPath){
        return  getBaseUri() + urlPath;
    }

    public static String getBaseUri() {

        // setup rest assured logging
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        // if not running then start the spark
        if(Port.inUse("localhost", 4567)) {
            return "http://localhost:4567";
        }else{
            //start it up
            Spark.port(4567);
            String [] args = {};
            final Thingifier thingifier = new TodoManagerThingifier().get();
            thingifier.apiConfig().setResponsesToShowGuids(true);
            thingifier.apiConfig().setUrlToShowIdsInUrlsIfAvailable(false);
            thingifier.apiConfig().setUrlToShowSingleInstancesAsPlural(true);
            thingifier.apiConfig().setResponsesToShowIdsIfAvailable(false);
            thingifier.apiConfig().jsonOutput().setCompressRelationships(false);
            thingifier.apiConfig().jsonOutput().setRelationshipsUseIdsIfAvailable(false);
            thingifier.apiConfig().jsonOutput().setConvertFieldsToDefinedTypes(false);

            ThingifierApiDefn apiDefn = new ThingifierApiDefn().setThingifier(thingifier);
            new ThingifierRestServer("", thingifier, apiDefn, new DefaultGUIHTML());
            return "http://localhost:4567";
        }



        // TODO: incorporate browsermob proxy and allow configuration of all
        //  requests through a proxy file to output a HAR file of all requests for later review
    }
}
