package uk.co.compendiumdev.thingifier.api.response;

import com.google.gson.Gson;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;

import java.util.*;

public final class ApiResponseAsJson {
    private final ApiResponse apiResponse;
    private final JsonThing jsonThing;

    public ApiResponseAsJson(final ApiResponse apiResponse, final JsonThing aJsonThing) {
        this.apiResponse = apiResponse;
        this.jsonThing = aJsonThing;
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

            String output = "";

            final List<ThingInstance> things = apiResponse.getReturnedInstanceCollection();

            String typeName = "";

            if (apiResponse.getTypeOfThingReturned() != null) {
                typeName = apiResponse.getTypeOfThingReturned().getPlural();
            } else {

                // TODO check - do not think that this is ever possible anymore
                if (things.size() > 0) {
                    typeName = things.get(0).getEntity().getPlural();
                }
            }

            if (typeName.length() > 0) {
                output = jsonThing.asJsonTypedArrayWithContentsUntyped(apiResponse.getReturnedInstanceCollection(), typeName);
            } else {
                if (things.size() == 0) {
                    output = "{}";
                }
            }

            return output;

        } else {
            ThingInstance instance = apiResponse.getReturnedInstance();

            //return JsonThing.asNamedJsonObject(instance).toString();
            return jsonThing.asJsonObject(instance).toString();
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
