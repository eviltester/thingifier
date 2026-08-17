package uk.co.compendiumdev.thingifier.api.response;

import java.util.*;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.XmlThing;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.xml.StringToXML;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

/**
 * Renders an {@link ApiResponse} as an XML response body.
 *
 * <p>XML rendering follows the same response-view selection as JSON rendering so default entity
 * views and route response views hide or expose the same fields across representations.
 */
public final class ApiResponseAsXml {
    private final ApiResponse apiResponse;
    private final JsonThing jsonThing;
    private final XmlThing xmlThing;

    public ApiResponseAsXml(final ApiResponse apiResponse, final JsonThing aJsonThing) {
        this.apiResponse = apiResponse;
        this.jsonThing = aJsonThing;
        this.xmlThing = new XmlThing(jsonThing);
    }

    /**
     * Serializes the API response body to XML.
     *
     * @return XML response body, or an empty string when the response has no body
     */
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

            List<EntityInstance> thingsToReturn = apiResponse.getReturnedInstanceCollection();

            if (thingsToReturn.size() == 0) {
                // when an XML response is asked for, but the collection is empty then we don't know
                // what to return and {}
                // would be returned but- ApiResponse should know the Thing that is in the
                // collection
                EntityDefinition defn = apiResponse.getTypeOfThingReturned();
                if (defn != null) {
                    return StringToXML.getEmptyElement(defn.getPlural());
                } else {
                    // todo should probably throw an exception
                    return "";
                    // throw new IllegalStateException("Do not know type of thing returned");
                }
            }

            // could default to JSON in case the xml conversion fails
            //  jsonThing.asJsonTypedArrayWithContentsUntyped(thingsToReturn,
            // apiResponse.getTypeOfThingReturned().getPlural());
            // xml output via JSON
            String output;
            try {
                output =
                        xmlThing.getCollectionOfThings(
                                thingsToReturn,
                                apiResponse.getTypeOfThingReturned(),
                                apiResponse.getRelationshipRepository(),
                                apiResponse.responseViewFor(apiResponse.getTypeOfThingReturned()));
            } catch (Exception e) {
                // TODO: if this happens then the status code is going to be wrong, should probably
                // throw an exception instead
                output = getErrorMessageXml(e.getMessage());
            }

            System.out.println(output);

            return output;
        } else {
            if (apiResponse.hasReturnedDraft()) {
                return xmlThing.getSingleObjectXml(
                        apiResponse.getReturnedDraft(),
                        apiResponse.responseViewFor(apiResponse.getReturnedDraft().getEntity()));
            }
            EntityInstance instance = apiResponse.getReturnedInstance();

            String output;
            try {
                output =
                        xmlThing.getSingleObjectXml(
                                instance,
                                apiResponse.getRelationshipRepository(),
                                apiResponse.responseViewFor(instance.getEntity()));
            } catch (Exception e) {
                // TODO: if this happens then the status code is going to be wrong
                output = getErrorMessageXml(e.getMessage());
            }

            System.out.println(output);

            return output;
        }
    }

    /**
     * Serializes one error message using Thingifier's standard XML error shape.
     *
     * @param errorMessage error message to include
     * @return XML error response body
     */
    public static String getErrorMessageXml(final String errorMessage) {
        Collection<String> localErrorMessages = new ArrayList<>();
        localErrorMessages.add(errorMessage);
        return getErrorMessageXml(localErrorMessages);
    }

    /**
     * Serializes multiple error messages using Thingifier's standard XML error shape.
     *
     * @param myErrorMessages error messages to include
     * @return XML error response body
     */
    public static String getErrorMessageXml(final Collection<String> myErrorMessages) {
        return StringToXML.getStringCollectionAsXml(
                "errorMessages", "errorMessage", myErrorMessages);
    }
}
