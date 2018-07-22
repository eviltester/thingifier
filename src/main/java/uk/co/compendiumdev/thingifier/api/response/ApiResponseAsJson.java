package uk.co.compendiumdev.thingifier.api.response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class ApiResponseAsJson {
    private final ApiResponse apiResponse;

    public ApiResponseAsJson(final ApiResponse apiResponse) {
        this.apiResponse = apiResponse;
    }

    public String getJson() {

        if (!apiResponse.hasABody()) {
            return "";
        }

        if (apiResponse.isErrorResponse()) {
            return getErrorMessageJson(apiResponse.getErrorMessages());
        }
        // we always return an object
        // collections are named with their plural
        if (apiResponse.isCollection()) {

            String output = JsonThing.asJson(apiResponse.getReturnedInstanceCollection());

            return output;

        } else {
            ThingInstance instance = apiResponse.getReturnedInstance();

            final JsonObject retObj = new JsonObject();
            retObj.add(instance.getEntity().getName(), JsonThing.asJsonObject(instance));

            return retObj.toString();
        }
    }

    // error messages should always be plural to make it easier to parse
    public static String getErrorMessageJson(final String errorMessage) {
        Collection<String> localErrorMessages = new ArrayList<>();
        localErrorMessages.add(errorMessage);
        return getErrorMessageJson(localErrorMessages);
    }

    public static String getErrorMessageJson(final Collection<String> myErrorMessages) {
        Map errorResponseBody = new HashMap<String, Collection<String>>();
        errorResponseBody.put("errorMessages", myErrorMessages);
        return new Gson().toJson(errorResponseBody);

    }
}
