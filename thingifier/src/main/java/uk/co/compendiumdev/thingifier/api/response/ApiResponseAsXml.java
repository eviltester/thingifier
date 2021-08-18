package uk.co.compendiumdev.thingifier.api.response;

import uk.co.compendiumdev.thingifier.api.http.bodyparser.xml.StringToXML;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.XmlThing;

import java.util.*;

final public class ApiResponseAsXml {
    private final ApiResponse apiResponse;
    private final JsonThing jsonThing;
    private final XmlThing xmlThing;

    public ApiResponseAsXml(final ApiResponse apiResponse, final JsonThing aJsonThing) {
        this.apiResponse = apiResponse;
        this.jsonThing = aJsonThing;
        this.xmlThing = new XmlThing(jsonThing);
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


            List<EntityInstance> thingsToReturn = apiResponse.getReturnedInstanceCollection();

            if (thingsToReturn.size() == 0) {
                // when an XML response is asked for, but the collection is empty then we don't know what to return and {}
                // would be returned but- ApiResponse should know the Thing that is in the collection
                EntityDefinition defn = apiResponse.getTypeOfThingReturned();
                if (defn != null) {
                    return StringToXML.getEmptyElement(defn.getPlural());
                } else {
                    // todo should probably throw an exception
                    return "";
                    //throw new IllegalStateException("Do not know type of thing returned");
                }

            }

            // could default to JSON in case the xml conversion fails
            //  jsonThing.asJsonTypedArrayWithContentsUntyped(thingsToReturn, apiResponse.getTypeOfThingReturned().getPlural());
            String output ="";

            // xml output via JSON
            try {
                if (thingsToReturn.size() > 0) {

                    output = xmlThing.getCollectionOfThings(thingsToReturn, apiResponse.getTypeOfThingReturned());
                }
            } catch (Exception e) {
                // TODO: if this happens then the status code is going to be wrong, should probably throw an exception instead
                output = getErrorMessageXml(e.getMessage());
            }

            System.out.println(output);

            return output;
        } else {
            EntityInstance instance = apiResponse.getReturnedInstance();

            String output = "";

            try {
                output = xmlThing.getSingleObjectXml(instance);
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
        return StringToXML.getStringCollectionAsXml(
                "errorMessages", "errorMessage", myErrorMessages);
    }
}
