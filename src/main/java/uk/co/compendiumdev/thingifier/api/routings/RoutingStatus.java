package uk.co.compendiumdev.thingifier.api.routings;

public class RoutingStatus {
    private int returnedStatusCode;
    private boolean returnedFromCall;

    private RoutingStatus() {
        this.returnedFromCall = true;
        this.returnedStatusCode = 0;
    }

    public static RoutingStatus returnedFromCall() {
        return new RoutingStatus().setReturnedFromCall(true);
    }

    public static RoutingStatus returnValue(final int status) {
        return new RoutingStatus().setStatusCode(status).setReturnedFromCall(false);
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
}
