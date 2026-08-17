package uk.co.compendiumdev.thingifier.api.response;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

/**
 * Renders an {@link ApiResponse} as JSON Lines or an RFC 7464 JSON text sequence.
 *
 * <p>Each entity is serialized individually, so the renderer resolves the response view for each
 * instance rather than assuming a single global view.
 */
public final class ApiResponseAsJsonLines {

    private static final String RECORD_SEPARATOR = "\u001E";

    private final ApiResponse apiResponse;
    private final JsonThing jsonThing;

    public ApiResponseAsJsonLines(final ApiResponse apiResponse, final JsonThing jsonThing) {
        this.apiResponse = apiResponse;
        this.jsonThing = jsonThing;
    }

    /**
     * Serializes the response as newline-delimited JSON objects.
     *
     * @return JSON Lines body, or an empty string when there are no records
     */
    public String getJsonLines() {
        List<String> jsonObjects = jsonObjects();
        if (jsonObjects.isEmpty()) {
            return "";
        }
        return String.join("\n", jsonObjects) + "\n";
    }

    /**
     * Serializes the response as a JSON text sequence with record separators.
     *
     * @return JSON sequence body, or an empty string when there are no records
     */
    public String getJsonSequence() {
        List<String> jsonObjects = jsonObjects();
        if (jsonObjects.isEmpty()) {
            return "";
        }

        StringBuilder sequence = new StringBuilder();
        for (String jsonObject : jsonObjects) {
            sequence.append(RECORD_SEPARATOR).append(jsonObject).append("\n");
        }
        return sequence.toString();
    }

    /**
     * Builds the individual JSON objects used by both streaming representations.
     *
     * @return serialized JSON objects without line separators
     */
    private List<String> jsonObjects() {
        List<String> objects = new ArrayList<>();
        if (!apiResponse.hasABody()) {
            return objects;
        }

        if (apiResponse.isErrorResponse()) {
            for (String message : apiResponse.getErrorMessages()) {
                JsonObject error = new JsonObject();
                error.addProperty("errorMessage", message);
                objects.add(error.toString());
            }
            return objects;
        }

        if (apiResponse.isCollection()) {
            for (EntityInstance instance : apiResponse.getReturnedInstanceCollection()) {
                objects.add(jsonFor(instance));
            }
            return objects;
        }

        if (apiResponse.hasReturnedDraft()) {
            objects.add(
                    jsonThing
                            .asJsonObject(
                                    apiResponse.getReturnedDraft(),
                                    apiResponse.responseViewFor(
                                            apiResponse.getReturnedDraft().getEntity()))
                            .toString());
            return objects;
        }

        objects.add(jsonFor(apiResponse.getReturnedInstance()));
        return objects;
    }

    /**
     * Serializes one persisted instance with its applicable response view.
     *
     * @param instance instance to serialize
     * @return JSON object text
     */
    private String jsonFor(final EntityInstance instance) {
        return jsonThing
                .asJsonObject(
                        instance,
                        apiResponse.getRelationshipRepository(),
                        apiResponse.responseViewFor(instance.getEntity()))
                .toString();
    }
}
