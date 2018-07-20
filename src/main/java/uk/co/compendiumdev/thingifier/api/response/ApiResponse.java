package uk.co.compendiumdev.thingifier.api.response;

import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.*;

public class ApiResponse {
    public static final String GUID_HEADER = "X-Thing-Instance-GUID";

    private final int statusCode;
    private boolean hasBody;
    // instead of storing a json as the body, store the things to return
    // let getBody do the conversion to json or xml
    List<ThingInstance> thingsToReturn;
    // isCollection true, return as collection, false, return as instance
    private boolean isCollection;
    // isErrorResponse true, return the stored collection of error messages
    private boolean isErrorResponse;
    private Collection<String> errorMessages;

    private Map<String, String> headers;
    private ThingDefinition typeOfResults;


    private ApiResponse(int statusCode) {
        this.statusCode = statusCode;
        headers = new HashMap<>();
        thingsToReturn = new ArrayList();
        isCollection = true;
        isErrorResponse=false;
        errorMessages = new ArrayList<>();
        hasBody = false;
    }

    private ApiResponse(int statusCode, boolean isError, Collection<String> errorMessages) {
        this(statusCode);
        isErrorResponse=isError;
        this.hasBody=true;
        this.errorMessages.addAll(errorMessages);
    }

    public int getStatusCode() {
        return this.statusCode;
    }




    public static ApiResponse success() {
        return new ApiResponse(200);
    }

    public ApiResponse returnSingleInstance(ThingInstance instance) {
        this.isCollection = false;
        thingsToReturn.clear();
        thingsToReturn.add(instance);
        andThisHasABody();
        return this;
    }

    public ApiResponse returnInstanceCollection(List<ThingInstance> items) {
        thingsToReturn.clear();
        thingsToReturn.addAll(items);
        isCollection=true;
        andThisHasABody();
        return this;
    }



    /*
            HEADERS
     */

    private ApiResponse setHeader(String headername, String value) {
        this.headers.put(headername, value);
        return this;
    }

    public String getHeaderValue(String headername) {
        return headers.get(headername);
    }

    private ApiResponse setLocationHeader(String location) {
        return setHeader("Location", location);
    }

    public Set<Map.Entry<String, String>> getHeaders(){
        return headers.entrySet();
    }





    /*
            SPECIAL CASE RESPONSES
     */

    public static ApiResponse created(ThingInstance thingInstance) {
        ApiResponse response = new ApiResponse(201);

        if(thingInstance!=null){
            response.returnSingleInstance(thingInstance);
            response.setLocationHeader(thingInstance.getEntity().getName() + "/" + thingInstance.getGUID()).
                    setHeader(ApiResponse.GUID_HEADER, thingInstance.getGUID());
            response.andThisHasABody();
        }

        return response;
    }

    private ApiResponse andThisHasABody() {
        this.hasBody = true;
        return this;
    }




    /*
            ERROR MESSAGES
     */




    public static ApiResponse error404(String errorMessage) {
        return error(404, errorMessage);
    }

    public static ApiResponse error(int statusCode, String errorMessage) {
        Collection<String> localErrorMessages = new ArrayList<>();
        localErrorMessages.add(errorMessage);
        return error(statusCode, localErrorMessages);
    }

    public static ApiResponse error(int statusCode, Collection<String> errorMessages) {
        return new ApiResponse(statusCode, true, errorMessages);
    }

    public boolean isErrorResponse() {
        return isErrorResponse;
    }


    public Collection<String> getErrorMessages() {
        return errorMessages;
    }





    public ThingInstance getReturnedInstance() {
        if(isCollection){
            throw new IllegalStateException("response contains a collection, not an instance");
        }

        return thingsToReturn.get(0);
    }

    public List<ThingInstance> getReturnedInstanceCollection() {
        if(!isCollection){
            throw new IllegalStateException("response contains an instance, not a collection");
        }
        return thingsToReturn;
    }


    public boolean isCollection() {
        return isCollection;
    }

    public ApiResponse resultContainsType(ThingDefinition thingDefinition) {
        if(thingDefinition!=null){
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
}
