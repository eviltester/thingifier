package uk.co.compendiumdev.challenge;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.AcceptContentTypeParser;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.domain.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

import java.io.File;
import java.util.Collection;

import static uk.co.compendiumdev.challenge.Challenges.CHALLENGE.*;

public class ChallengerApiResponseHook implements HttpApiResponseHook {

    private final Challenges challenges;
    private final Thingifier thingifier;

    public ChallengerApiResponseHook(final Challenges challenges, Thingifier thingifier) {
        this.challenges = challenges;
        this.thingifier = thingifier;
    }

    @Override
    public HttpApiResponse run(final HttpApiRequest request,
                               final HttpApiResponse response,
                               final ThingifierApiConfig config) {

        // READ
        if(request.getVerb() == HttpApiRequest.VERB.GET &&
                request.getPath().matches("todos/.*") &&
                response.getStatusCode()==200){
            challenges.pass(GET_TODO);
        }

        if(request.getVerb() == HttpApiRequest.VERB.GET &&
                request.getPath().matches("todos/.*") &&
                response.getStatusCode()==404){
            challenges.pass(GET_TODO_404);
        }

        if(request.getVerb() == HttpApiRequest.VERB.GET &&
                request.getPath().contentEquals("todos") &&
                request.getQueryParams().size()==0 &&
                new AcceptContentTypeParser(request.getHeader("accept")).isXML() &&
                response.getType().contentEquals("application/xml") &&
                response.getStatusCode()==200
        ){
            challenges.pass(GET_ACCEPT_XML);
        }

        if(request.getVerb() == HttpApiRequest.VERB.GET &&
                request.getPath().contentEquals("todos") &&
                request.getQueryParams().size()==0 &&
                new AcceptContentTypeParser(request.getHeader("accept")).isJSON() &&
                response.getType().contentEquals("application/json") &&
                response.getStatusCode()==200
        ){
            challenges.pass(GET_ACCEPT_JSON);
        }

        if(request.getVerb() == HttpApiRequest.VERB.GET &&
                request.getPath().contentEquals("todos") &&
                request.getQueryParams().containsKey("doneStatus") &&
                request.getQueryParams().get("doneStatus").contentEquals("true") &&
                response.getStatusCode()==200){
            // only pass if there are done and not done todos
            final Thing thing = thingifier.getThingNamed("todo");
            final ThingInstance aDoneThing = thing.findInstanceByField(FieldValue.is("doneStatus", "true"));
            final ThingInstance aNotDoneThing = thing.findInstanceByField(FieldValue.is("doneStatus", "false"));
            if(aDoneThing!=null && aNotDoneThing!=null) {
                challenges.pass(GET_TODOS_FILTERED);
            }
        }



        // CREATE
        if(request.getVerb() == HttpApiRequest.VERB.POST &&
                request.getPath().matches("todos") &&
                response.getStatusCode()==201){
            challenges.pass(POST_TODOS);
        }

        if(request.getVerb() == HttpApiRequest.VERB.POST &&
                request.getPath().matches("todos") &&
                response.getStatusCode()==400 &&
                collate(response.apiResponse().getErrorMessages()).contains(
                        "Failed Validation: doneStatus should be BOOLEAN")){
            challenges.pass(POST_TODOS_BAD_DONE_STATUS);
        }

        if(request.getVerb() == HttpApiRequest.VERB.POST &&
                request.getPath().matches("todos") &&
                response.getStatusCode()==415){
            challenges.pass(POST_TODOS_415);
        }

        // UPDATE
        if(request.getVerb() == HttpApiRequest.VERB.POST &&
                request.getPath().matches("todos/.*") &&
                response.getStatusCode()==200){
            challenges.pass(POST_UPDATE_TODO);
        }


        // DELETE
        if(request.getVerb() == HttpApiRequest.VERB.DELETE &&
                request.getPath().matches("todos/.*") &&
                response.getStatusCode()==200){
            challenges.pass(DELETE_A_TODO);
        }

        if(request.getVerb() == HttpApiRequest.VERB.DELETE &&
                request.getPath().matches("todos/.*") &&
                response.getStatusCode()==200 &&
                thingifier.getThingWithPluralNamed("todos").countInstances()==0){
            challenges.pass(DELETE_ALL_TODOS);
        }



        // TODO: challenge - complete all challenges in the minimum number of requests
        // TODO: challenge - complete all challenges

        // do not interfere with api and return null
        return null;
    }

    String collate(Collection<String> strings){
        String collated = "";
        for(String string : strings){
            collated = collated + " " + string;
        }
        return collated;
    }
}
