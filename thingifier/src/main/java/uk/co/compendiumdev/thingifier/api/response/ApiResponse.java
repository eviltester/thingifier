package uk.co.compendiumdev.thingifier.api.response;

import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.api.ApiUrls;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

import java.util.*;


public final class ApiResponse {
    public static final String GUID_HEADER = "X-Thing-Instance-GUID";
    public static final String ID_HEADER = "X-Thing-Instance-ID";

    private final int statusCode;
    private boolean hasBody;
    // instead of storing a json as the body, store the things to return
    // let getBody do the conversion to json or xml
    private List<ThingInstance> thingsToReturn;
    // isCollection true, return as collection, false, return as instance
    private boolean isCollection;
    // isErrorResponse true, return the stored collection of error messages
    private boolean isErrorResponse;
    private Collection<String> errorMessages;

    private Map<String, String> headers;
    private ThingDefinition typeOfResults;


    private ApiResponse(final int aStatusCode) {
        this.statusCode = aStatusCode;
        headers = new HashMap<>();
        thingsToReturn = new ArrayList();
        isCollection = false;
        isErrorResponse = false;
        errorMessages = new ArrayList<>();
        hasBody = false;
    }

    private ApiResponse(final int aStatusCode, final boolean isError, final Collection<String> theErrorMessages) {
        this(aStatusCode);
        isErrorResponse = isError;
        if (isError) {
            isCollection = false;
        }
        this.hasBody = true;
        this.errorMessages.addAll(theErrorMessages);
    }

    public int getStatusCode() {
        return this.statusCode;
    }


    public static ApiResponse success() {
        return new ApiResponse(200);
    }

    public ApiResponse returnSingleInstance(final ThingInstance instance) {
        this.isCollection = false;
        thingsToReturn.clear();
        thingsToReturn.add(instance);
        typeOfResults = instance.getEntity();
        this.hasBody = true;
        return this;
    }

    public ApiResponse returnInstanceCollection(final List<ThingInstance> items) {
        thingsToReturn.clear();
        thingsToReturn.addAll(items);
        isCollection = true;
        if (items.size() > 0) {
            typeOfResults = items.get(0).getEntity();
        }
        this.hasBody = true;
        return this;
    }



    /*
            HEADERS
     */

    private ApiResponse setHeader(final String headername, final String value) {
        this.headers.put(headername, value);
        return this;
    }

    public String getHeaderValue(final String headername) {
        return headers.get(headername);
    }

    private ApiResponse setLocationHeader(final String location) {
        return setHeader("Location", location);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }





    /*
            SPECIAL CASE RESPONSES
     */

    public static ApiResponse created(final ThingInstance thingInstance, ThingifierApiConfig apiConfig) {
        ApiResponse response = new ApiResponse(201);

        if (thingInstance != null) {
            response.returnSingleInstance(thingInstance);

            response.setLocationHeader(
                    new ApiUrls(apiConfig).
                            getCreatedLocationHeader(thingInstance));

            if(apiConfig.willResponsesShowGuids()) {
                response.setHeader(ApiResponse.GUID_HEADER, thingInstance.getGUID());
            }
            response.hasBody = true;
        }

        return response;
    }




    /*
            ERROR MESSAGES
     */


    public static ApiResponse error404(final String errorMessage) {
        return error(404, errorMessage);
    }

    public static ApiResponse error(final int statusCode, final String errorMessage) {
        Collection<String> localErrorMessages = new ArrayList<>();
        localErrorMessages.add(errorMessage);
        return error(statusCode, localErrorMessages);
    }

    public static ApiResponse error(final int statusCode, final Collection<String> errorMessages) {
        return new ApiResponse(statusCode, true, errorMessages);
    }

    public boolean isErrorResponse() {
        return isErrorResponse;
    }


    public Collection<String> getErrorMessages() {
        return errorMessages;
    }


    public ThingInstance getReturnedInstance() {
        if (isCollection) {
            throw new IllegalStateException("response contains a collection, not an instance");
        }

        return thingsToReturn.get(0);
    }

    public List<ThingInstance> getReturnedInstanceCollection() {
        if (!isCollection) {
            throw new IllegalStateException("response contains an instance, not a collection");
        }
        return thingsToReturn;
    }


    public boolean isCollection() {
        return isCollection;
    }

    public ApiResponse resultContainsType(final ThingDefinition thingDefinition) {
        if (thingDefinition != null) {
            this.typeOfResults = thingDefinition;
        }
        return this;
    }

    public ThingDefinition getTypeOfThingReturned() {
        return typeOfResults;
    }

    public boolean hasABody() {
        return this.hasBody;
    }

    public ApiResponse addToErrorMessages(final String message) {
        errorMessages.add(message);
        return this;
    }

    public void clearBody() {
        this.hasBody = false;
    }
}
