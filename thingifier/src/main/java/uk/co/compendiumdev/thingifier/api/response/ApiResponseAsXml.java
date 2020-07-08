package uk.co.compendiumdev.thingifier.api.response;

import org.json.JSONObject;
import org.json.XML;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;

import java.util.*;

final public class ApiResponseAsXml {
    private final ApiResponse apiResponse;
    private final JsonThing jsonThing;

    public ApiResponseAsXml(final ApiResponse apiResponse, final JsonThing aJsonThing) {
        this.apiResponse = apiResponse;
        this.jsonThing = aJsonThing;
    }

    public String getXml() {

        if (!apiResponse.hasABody()) {
            return "";
        }

        if (apiResponse.isErrorResponse()) {
            return getErrorMessageXml(apiResponse.getErrorMessages());
        }
        // we always return an object
        // collections are named with their plural
        if (apiResponse.isCollection()) {


            List<ThingInstance> thingsToReturn = apiResponse.getReturnedInstanceCollection();

            if (thingsToReturn.size() == 0) {
                // when an XML response is asked for, but the collection is empty then we don't know what to return and {}
                // would be returned but- ApiResponse should know the Thing that is in the collection
                ThingDefinition defn = apiResponse.getTypeOfThingReturned();
                if (defn != null) {
                    return String.format("<%1$s></%1$s>", defn.getPlural());
                } else {
                    // TODO: consider if this should possibly be an illegalstate exception because we need an entity type for null xml
                    return "";
                }

            }

            // default JSON in case the xml conversion fails
            String output = jsonThing.asJsonTypedArrayWithContentsUntyped(thingsToReturn, apiResponse.getTypeOfThingReturned().getPlural());

            // xml output via JSON
            try {
                if (thingsToReturn.size() > 0) {

                    String parseForXMLOutput = jsonThing.asJsonTypedArrayWithContentsTyped(thingsToReturn, apiResponse.getTypeOfThingReturned());

                    output = XML.toString(new JSONObject(parseForXMLOutput));

                    // TODO: workaround for this seems like a bug in XML.toString, but work around it at the moment
                    // i.e. it outputs <todos><todo>...</todo></todos><todos><todo>...</todo></todos>
                    output = output.replace(String.format("</%1$s><%1$s>", thingsToReturn.get(0).getEntity().getPlural()), "");
                }
            } catch (Exception e) {
                // TODO: if this happens then the status code is going to be wrong, should probably throw an exception instead
                output = getErrorMessageXml(e.getMessage());
            }

            System.out.println(output);

            return output;
        } else {
            ThingInstance instance = apiResponse.getReturnedInstance();

            String output = jsonThing.asNamedJsonObject(instance).toString();

            // experimental xml output
            try {
                String parseForXMLOutput = output;
                //System.out.println(parseForXMLOutput);
                output = XML.toString(new JSONObject(parseForXMLOutput));

            } catch (Exception e) {
                // TODO: if this happens then the status code is going to be wrong
                output = getErrorMessageXml(e.getMessage());
            }

            System.out.println(output);

            return output;
        }
    }

    public static String getErrorMessageXml(final String errorMessage) {
        Collection<String> localErrorMessages = new ArrayList<>();
        localErrorMessages.add(errorMessage);
        return getErrorMessageXml(localErrorMessages);
    }

    public static String getErrorMessageXml(final Collection<String> myErrorMessages) {
        Map errorResponseBody = new HashMap<String, Collection<String>>();
        errorResponseBody.put("errorMessage", myErrorMessages);
        return XML.toString(new JSONObject(errorResponseBody), "errorMessages");

    }
}
