package uk.co.compendiumdev.thingifier.application.httprouting;

import spark.Request;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseError;
import uk.co.compendiumdev.thingifier.spark.SimpleSparkRouteCreator;

import java.util.List;

import static spark.Spark.exception;

public class SparkHttpGenericExceptionRoutings {

    public SparkHttpGenericExceptionRoutings(){

        // TODO: this is too permissive since it creates an HTTP end point that would also cover GUI
        // it should only be "*" if the api config root is missing
        // TODO : allow this to be overwritten by config
        // nothing else is supported

        SimpleSparkRouteCreator.routeStatus(404, "*", true, List.of("head", "get", "options", "put", "post", "patch", "delete"));

        exception(RuntimeException.class, (e, request, response) -> {
            response.status(400);
            response.body(getExceptionErrorResponse(e, request));
        });

        exception(Exception.class, (e, request, response) -> {
            response.status(500);
            response.body(getExceptionErrorResponse(e, request));
        });
    }

    private String getExceptionErrorResponse(final Exception e, final Request request) {
        if(e.getMessage()==null) {
            return ApiResponseError.asAppropriate(request.headers("Accept"), e.toString());
        }else{
            return ApiResponseError.asAppropriate(request.headers("Accept"), e.getMessage());
        }
    }
}
