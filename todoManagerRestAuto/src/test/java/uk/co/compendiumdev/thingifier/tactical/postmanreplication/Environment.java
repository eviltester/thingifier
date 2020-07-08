package uk.co.compendiumdev.thingifier.tactical.postmanreplication;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import spark.Spark;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.ThingifierRestServer;
import uk.co.compendiumdev.thingifier.application.examples.TodoManagerThingifier;
import uk.co.compendiumdev.thingifier.tactical.sparkstart.Port;

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
            thingifier.apiConfig().jsonOutput().compressRelationships(false);
            thingifier.apiConfig().jsonOutput().relationshipsUsesIdsIfAvailable(false);
            new ThingifierRestServer(args, "", thingifier);
            return "http://localhost:4567";
        }



        // TODO: incorporate browsermob proxy and allow configuration of all
        //  requests through a proxy file to output a HAR file of all requests for later review
    }
}
