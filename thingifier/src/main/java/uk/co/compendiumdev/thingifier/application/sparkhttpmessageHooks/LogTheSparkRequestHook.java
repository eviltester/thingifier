package uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks;

import spark.Request;
import spark.Response;

public class LogTheSparkRequestHook implements SparkRequestResponseHook {
    @Override
    public void run(final Request request, final Response response) {
        try {

                System.out.println("**REQUEST**");
                System.out.println(request.url());
                System.out.println(request.pathInfo());
                System.out.println(request.body());

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
