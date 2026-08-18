package uk.co.compendiumdev.thingifier.api.response;

import com.google.gson.Gson;
import java.util.*;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

/**
 * Renders an {@link ApiResponse} as a JSON response body.
 *
 * <p>The renderer asks the response for the view to use for each entity so route-specific views and
 * entity-level defaults are applied at serialization time.
 */
public final class ApiResponseAsJson {
    private final ApiResponse apiResponse;
    private final JsonThing jsonThing;

    public ApiResponseAsJson(final ApiResponse apiResponse, final JsonThing aJsonThing) {
        this.apiResponse = apiResponse;
        this.jsonThing = aJsonThing;
    }

    /**
     * Serializes the API response body to JSON.
     *
     * @return JSON response body, or an empty string when the response has no body
     */
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

            final List<EntityInstance> things = apiResponse.getReturnedInstanceCollection();

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
                final EntityDefinition entity =
                        apiResponse.getTypeOfThingReturned() == null
                                ? things.get(0).getEntity()
                                : apiResponse.getTypeOfThingReturned();
                output =
                        jsonThing.asJsonTypedArrayWithContentsUntyped(
                                apiResponse.getReturnedInstanceCollection(),
                                typeName,
                                apiResponse.getRelationshipRepository(),
                                apiResponse.responseViewFor(entity));
            } else {
                if (things.size() == 0) {
                    output = "{}";
                }
            }

            return output;

        } else {
            if (apiResponse.hasReturnedDraft()) {
                return jsonThing
                        .asJsonObject(
                                apiResponse.getReturnedDraft(),
                                apiResponse.responseViewFor(
                                        apiResponse.getReturnedDraft().getEntity()))
                        .toString();
            }
            EntityInstance instance = apiResponse.getReturnedInstance();

            // return JsonThing.asNamedJsonObject(instance).toString();
            return jsonThing
                    .asJsonObject(
                            instance,
                            apiResponse.getRelationshipRepository(),
                            apiResponse.responseViewFor(instance.getEntity()))
                    .toString();
        }
    }

    // error messages should always be plural to make it easier to parse
    /**
     * Serializes one error message using Thingifier's standard JSON error shape.
     *
     * @param errorMessage error message to include
     * @return JSON error response body
     */
    public static String getErrorMessageJson(final String errorMessage) {
        Collection<String> localErrorMessages = new ArrayList<>();
        localErrorMessages.add(errorMessage);
        return getErrorMessageJson(localErrorMessages);
    }

    /**
     * Serializes multiple error messages using Thingifier's standard JSON error shape.
     *
     * @param myErrorMessages error messages to include
     * @return JSON error response body
     */
    public static String getErrorMessageJson(final Collection<String> myErrorMessages) {
        Map errorResponseBody = new HashMap<String, Collection<String>>();
        errorResponseBody.put("errorMessages", myErrorMessages);
        return new Gson().toJson(errorResponseBody);
    }
}
