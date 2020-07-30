package uk.co.compendiumdev.thingifier.api.routings;

public class RoutingStatus {
    private int returnedStatusCode;
    private boolean returnedFromCall;
    private String description;

    private RoutingStatus() {
        this.returnedFromCall = true;
        this.returnedStatusCode = 0;
        this.description="";
    }

    public static RoutingStatus returnedFromCall() {
        return new RoutingStatus().setReturnedFromCall(true);
    }

    public static RoutingStatus returnValue(final int status) {
        return new RoutingStatus().setStatusCode(status).setReturnedFromCall(false);
    }

    public static RoutingStatus returnValue(final int status, final String statusDescription) {
        return new RoutingStatus().
                    setStatusCode(status).
                    setStatusDescription(statusDescription).
                    setReturnedFromCall(false);
    }

    private RoutingStatus setStatusDescription(final String statusDescription) {

        if(statusDescription != null && statusDescription.trim().length() >0){
            this.description = statusDescription.trim();
        }

        return this;
    }

    private RoutingStatus setStatusCode(final int status) {
        this.returnedStatusCode = status;
        return this;
    }

    private RoutingStatus setReturnedFromCall(final boolean isReturned) {
        this.returnedFromCall = isReturned;
        return this;
    }

    public boolean isReturnedFromCall() {
        return this.returnedFromCall;
    }

    public int value() {
        return returnedStatusCode;
    }

    public String description(){
        if(description.length()>0){
            return description;
        }
        switch (returnedStatusCode){
            case 200:
                return "OK";
            case 201:
                return "OK, Created";
            case 204:
                return "OK, No Content";
            case 400:
                return "Error processing request";
            case 404:
                return "Not Found";
            case 405:
                return "Method not allowed";
            default:
                return "Standard Status Code Meaning";
        }
    }
}
