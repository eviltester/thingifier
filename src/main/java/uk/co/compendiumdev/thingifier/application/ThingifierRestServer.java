package uk.co.compendiumdev.thingifier.application;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ApiResponse;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipDefinition;

import static spark.Spark.*;

public class ThingifierRestServer {

    private String justThePath(String path){
        if(path.startsWith("/")){
            return path.substring(1);
        }
        return path;
    }

    public ThingifierRestServer(String[] args, String path, Thingifier thingifier) {

        // configure it based on a thingifier

        // e.g. GET thing.name
        // GET thing.name/GUID
        // routings based around relationships

        before((request, response) -> {
            response.type("application/json");
        });


        // TODO: have a class that returns a set of rules VERB | URL | STATUS | HEADER
        // because all the verbs have common code and we only need to write each once, this then allows us to create custom routing in the model definition
        // e.g. allow POST to project/guid/todo to create a todo etc. which would be a bug, but we could define it as a buggy route for people to find

        for(Thing thing : thingifier.getThings()){

            final String aUrl = thing.definition().getName().toLowerCase();
            // we should be able to get things
            get(aUrl, (request, response) -> {
                ApiResponse apiResponse = thingifier.api().get(justThePath(request.pathInfo()));
                response.status(apiResponse.getStatusCode()); return apiResponse.getBody();});

            // we should be able to create things without a GUID
            post(aUrl, (request, response) -> {
                ApiResponse apiResponse = thingifier.api().post(justThePath(request.pathInfo()), request.body());
                response.status(apiResponse.getStatusCode()); return apiResponse.getBody();});

            options(aUrl, (request, response) -> {
                response.status(200); response.header("Allow", "OPTIONS, GET, POST"); return "";});

            head(aUrl, (request, response) -> {response.status(405); return "";});
            delete(aUrl, (request, response) -> {response.status(405); return "";});
            patch(aUrl, (request, response) -> {response.status(405); return "";});
            put(aUrl, (request, response) -> {response.status(405); return "";});


            final String aUrlWGuid = thing.definition().getName().toLowerCase()  + "/:guid";
            // we should be able to get specific things based on the GUID
            get(aUrlWGuid, (request, response) -> {
                ApiResponse apiResponse = thingifier.api().get(justThePath(request.pathInfo()));
                response.status(apiResponse.getStatusCode()); return apiResponse.getBody();});

            // we should be able to amend things with a GUID
            post(aUrlWGuid, (request, response) -> {
                ApiResponse apiResponse = thingifier.api().post(justThePath(request.pathInfo()), request.body());
                response.status(apiResponse.getStatusCode()); return apiResponse.getBody();});

            // we should be able to amend things with PUT and a GUID
            put(aUrlWGuid, (request, response) -> {
                ApiResponse apiResponse = thingifier.api().put(justThePath(request.pathInfo()), request.body());
                response.status(apiResponse.getStatusCode()); return apiResponse.getBody();});

            // we should be able to delete specific things
            delete(aUrlWGuid, (request, response) -> {
                ApiResponse apiResponse = thingifier.api().delete(justThePath(request.pathInfo()));
                response.status(apiResponse.getStatusCode()); return apiResponse.getBody();});

            options(aUrlWGuid, (request, response) -> {
                response.status(200); response.header("Allow", "OPTIONS, GET, POST, PUT, DELETE"); return "";});

            head(aUrlWGuid, (request, response) -> {response.status(405); return "";});
            patch(aUrlWGuid, (request, response) -> {response.status(405); return "";});


        }

        for(RelationshipDefinition relationship : thingifier.getRelationshipDefinitions()){
            // get all things for a relationship

            String aUrl = relationship.from().definition().getName() + "/:guid/" + relationship.getName();
            get(aUrl, (request, response) -> {
                ApiResponse apiResponse = thingifier.api().get(justThePath(request.pathInfo()));
                response.status(apiResponse.getStatusCode()); return apiResponse.getBody();});

            options(aUrl, (request, response) -> {
                response.status(200); response.header("Allow", "OPTIONS, GET"); return "";});

            // we can post if there is no guid as it will create the 'thing' and the relationship connection
            post(aUrl, (request, response) -> {
                ApiResponse apiResponse = thingifier.api().post(justThePath(request.pathInfo()), request.body());
                response.status(apiResponse.getStatusCode()); return apiResponse.getBody();});


            head(aUrl, (request, response) -> {response.status(405); return "";});
            delete(aUrl, (request, response) -> {response.status(405); return "";});
            patch(aUrl, (request, response) -> {response.status(405); return "";});
            put(aUrl, (request, response) -> {response.status(405); return "";});


            // we should be able to delete a relationship
            final String aUrlDelete = relationship.from().definition().getName() + "/:guid/" + relationship.getName() + "/:relatedguid";
            delete(aUrlDelete, (request, response) -> {
                ApiResponse apiResponse = thingifier.api().delete(justThePath(request.pathInfo()));
                response.status(apiResponse.getStatusCode()); return apiResponse.getBody();});

            options(aUrlDelete, (request, response) -> {
                response.status(200); response.header("Allow", "OPTIONS, DELETE"); return "";});

            head(aUrlDelete, (request, response) -> {response.status(405); return "";});
            get(aUrlDelete, (request, response) -> {response.status(405); return "";});
            patch(aUrlDelete, (request, response) -> {response.status(405); return "";});
            put(aUrlDelete, (request, response) -> {response.status(405); return "";});
            post(aUrlDelete, (request, response) -> {response.status(405); return "";});


        }

        // nothing else is supported
        head("*", (request, response) -> {response.status(404); return "";});
        get("*", (request, response) -> {response.status(404); return "";});
        options("*", (request, response) -> {response.status(404); return "";});
        put("*", (request, response) -> {response.status(404); return "";});
        post("*", (request, response) -> {response.status(404); return "";});
        patch("*", (request, response) -> {response.status(404); return "";});
        delete("*", (request, response) -> {response.status(404); return "";});

        exception(RuntimeException.class, (e, request, response) -> {
            response.status(400);
            response.body(ApiResponse.getErrorMessageJson(e.getMessage()));
        });

        exception(Exception.class, (e, request, response) -> {
            response.status(500);
            response.body(ApiResponse.getErrorMessageJson(e.getMessage()));
        });

    }
}
