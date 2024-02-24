package uk.co.compendiumdev.thingifier.api.response;

import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.api.ApiUrls;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.*;


public final class ApiResponse {

    // TODO: instance GUID or instance-id should actually be the primary key
    public static final String PRIMARY_KEY_HEADER = "X-Thing-Instance-Primary-Key";

    private final int statusCode;
    private boolean hasBody;
    // instead of storing a json as the body, store the things to return
    // let getBody do the conversion to json or xml
    private List<EntityInstance> thingsToReturn;
    // isCollection true, return as collection, false, return as instance
    private boolean isCollection;
    // isErrorResponse true, return the stored collection of error messages
    private boolean isErrorResponse;
    private Collection<String> errorMessages;

    private HttpHeadersBlock headers;
    private EntityDefinition typeOfResults;
    private String body;


    public ApiResponse(final int aStatusCode) {
        this.statusCode = aStatusCode;
        headers = new HttpHeadersBlock();
        thingsToReturn = new ArrayList();
        isCollection = false;
        isErrorResponse = false;
        errorMessages = new ArrayList<>();
        hasBody = false;
        body=null;
    }

    public ApiResponse(final int aStatusCode, final boolean isError, final Collection<String> theErrorMessages) {
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

    public ApiResponse returnSingleInstance(final EntityInstance instance) {
        this.isCollection = false;
        thingsToReturn.clear();
        thingsToReturn.add(instance);
        typeOfResults = instance.getEntity();
        this.hasBody = true;
        return this;
    }

    public ApiResponse returnInstanceCollection(final List<EntityInstance> items) {
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

    public ApiResponse setHeader(final String headername, final String value) {
        this.headers.put(headername, value);
        return this;
    }

    public String getHeaderValue(final String headername) {
        return headers.get(headername);
    }

    public ApiResponse setLocationHeader(final String location) {
        return setHeader("Location", location);
    }

    public HttpHeadersBlock getHeaders() {
        return headers;
    }





    /*
            SPECIAL CASE RESPONSES
     */

    public static ApiResponse created(final EntityInstance thingInstance, ThingifierApiConfig apiConfig) {
        ApiResponse response = new ApiResponse(201);

        if (thingInstance != null) {
            response.returnSingleInstance(thingInstance);

            response.setLocationHeader(
                    new ApiUrls(apiConfig).
                            getCreatedLocationHeader(thingInstance));

            if(apiConfig.willResponsesShowPrimaryKeyHeader()) {
                response.setHeader(ApiResponse.PRIMARY_KEY_HEADER, thingInstance.getPrimaryKeyValue());
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


    public EntityInstance getReturnedInstance() {
        if (isCollection) {
            throw new IllegalStateException("response contains a collection, not an instance");
        }

        return thingsToReturn.get(0);
    }

    public List<EntityInstance> getReturnedInstanceCollection() {
        if (!isCollection) {
            throw new IllegalStateException("response contains an instance, not a collection");
        }
        return thingsToReturn;
    }


    public boolean isCollection() {
        return isCollection;
    }

    public ApiResponse resultContainsType(final EntityDefinition thingDefinition) {
        if (thingDefinition != null) {
            this.typeOfResults = thingDefinition;
        }
        return this;
    }

    public EntityDefinition getTypeOfThingReturned() {
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
        this.body=null;
    }

    public void setBody(final String bodyDetails) {
        this.hasBody=true;
        this.body = bodyDetails;
    }

    public boolean hasABodyOverride() {
        return this.body!=null;
    }

    public String getBody() {
        return this.body;
    }
}
