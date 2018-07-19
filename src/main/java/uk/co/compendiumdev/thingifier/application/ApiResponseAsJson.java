package uk.co.compendiumdev.thingifier.application;

import com.google.gson.Gson;
import org.json.JSONObject;
import org.json.XML;
import uk.co.compendiumdev.thingifier.api.ApiResponse;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ApiResponseAsJson {
    private final ApiResponse apiResponse;

    public ApiResponseAsJson(ApiResponse apiResponse) {
        this.apiResponse = apiResponse;
    }

    public String getJson() {
        if(apiResponse.isErrorResponse()){
            return getErrorMessageJson(apiResponse.getErrorMessages());
        }
        // we always return an object
        // collections are named with their plural
        if(apiResponse.isCollection()){

            String output = JsonThing.asJson(apiResponse.getReturnedInstanceCollection());

            return output;

        }else{
            ThingInstance instance = apiResponse.getReturnedInstance();
            String output = JsonThing.jsonObjectWrapper(instance.getEntity().getName(), JsonThing.asJson(instance));

            return output;
        }
    }

    // error messages should always be plural to make it easier to parse
    public static String getErrorMessageJson(String errorMessage) {
        Collection<String> localErrorMessages = new ArrayList<>();
        localErrorMessages.add(errorMessage);
        return getErrorMessageJson(localErrorMessages);
    }

    public static String getErrorMessageJson(Collection<String> myErrorMessages) {
        Map errorResponseBody = new HashMap<String,Collection<String>>();
        errorResponseBody.put("errorMessages", myErrorMessages);
        return new Gson().toJson(errorResponseBody);

    }
}
