package uk.co.compendiumdev.sparkstart;


import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import spark.Spark;
import uk.co.compendiumdev.todolist.application.Main;

public class Environment {

    public static String getEnv(String urlPath){
        return  getBaseUri() + urlPath;
    }

    public static String getBaseUri() {

        // return environment if want to run externally
//        if(true)
//            return "https://somethingwhichhoststheapi.com";

        // switch rest assured logging on
        RestAssured.filters(
                new RequestLoggingFilter(),
                new ResponseLoggingFilter());

        // if not running then start the spark
        if(Port.inUse("localhost", 4567)) {
            return "http://localhost:4567";
        }else{
            //start it up
            Spark.port(4567);
            String [] args;

            // todo support configuring to write tests against different versions
            args = "".split(",");

            Main.main(args);

            // wait till running
            int maxtries=10;
            while(!Port.inUse("localhost", 4567)){
                maxtries--;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return "http://localhost:4567";
        }

        // TODO: incorporate browsermob proxy and allow configuration of all
        //  requests through a proxy file to output a HAR file of all requests for later review
    }
}
