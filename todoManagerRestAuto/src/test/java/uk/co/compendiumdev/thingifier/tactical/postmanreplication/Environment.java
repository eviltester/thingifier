package uk.co.compendiumdev.thingifier.tactical.postmanreplication;

import spark.Spark;
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
        // if not running then start the spark
        if(Port.inUse("localhost", 4567)) {
            return "http://localhost:4567";
        }else{
            //start it up
            Spark.port(4567);
            String [] args = {};
            new ThingifierRestServer(args, "", new TodoManagerThingifier().get());
            return "http://localhost:4567";
        }
    }
}
