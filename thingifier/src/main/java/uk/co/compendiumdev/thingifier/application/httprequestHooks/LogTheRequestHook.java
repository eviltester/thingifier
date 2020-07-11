package uk.co.compendiumdev.thingifier.application.httprequestHooks;

import spark.Request;
import spark.Response;

public class LogTheRequestHook implements RequestHook {
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
