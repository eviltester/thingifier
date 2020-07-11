package uk.co.compendiumdev.thingifier.application.httprequestHooks;

import spark.Request;
import spark.Response;

public class LogTheResponseHook implements RequestHook {
    @Override
    public void run(final Request request, final Response response) {
        try {
                System.out.println("**RESPONSE**");
                System.out.println(response.status());
                System.out.println(response.body());
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
