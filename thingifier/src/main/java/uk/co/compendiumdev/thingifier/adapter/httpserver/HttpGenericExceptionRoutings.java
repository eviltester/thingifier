package uk.co.compendiumdev.thingifier.adapter.httpserver;

import java.util.List;

public class HttpGenericExceptionRoutings {

    public HttpGenericExceptionRoutings() {

        // TODO: this is too permissive since it creates an HTTP end point that would also cover GUI
        // it should only be "*" if the api config root is missing
        // TODO : allow this to be overwritten by config
        // nothing else is supported

        SimpleHttpRouteCreator.routeStatus(
                404,
                "*",
                true,
                List.of("head", "get", "options", "put", "post", "patch", "delete"));
    }
}
