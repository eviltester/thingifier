package uk.co.compendiumdev.challenge.challengehooks;

import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.AcceptContentTypeParser;
import uk.co.compendiumdev.thingifier.api.http.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;
import java.util.Collection;



public class ChallengerApiResponseHook implements HttpApiResponseHook {

    private final Challengers challengers;
    private final Thingifier thingifier;

    public ChallengerApiResponseHook(final Challengers challengers, Thingifier thingifier) {
        this.challengers = challengers;
        this.thingifier = thingifier;
    }

    @Override
    public HttpApiResponse run(final HttpApiRequest request,
                               final HttpApiResponse response,
                               final ThingifierApiConfig config) {

        ChallengerAuthData challenger = challengers.getChallenger(request.getHeader("X-CHALLENGER"));

        if(challenger==null){
            // cannot track challenges
            return null;
        }

        // READ
        if(request.getVerb() == HttpApiRequest.VERB.GET &&
                request.getPath().matches("todos/.*") &&
                response.getStatusCode()==200){
            challengers.pass(challenger, CHALLENGE.GET_TODO);
        }

        if(request.getVerb() == HttpApiRequest.VERB.GET &&
                request.getPath().matches("todos/.*") &&
                response.getStatusCode()==404){
            challengers.pass(challenger,CHALLENGE.GET_TODO_404);
        }

        final AcceptHeaderParser acceptParser = new AcceptHeaderParser(request.getHeader("accept"));
        final AcceptContentTypeParser contentTypeParser = new AcceptContentTypeParser(request.getHeader("content-type"));

        if(request.getVerb() == HttpApiRequest.VERB.GET &&
                request.getPath().contentEquals("todos") &&
                request.getQueryParams().size()==0 &&
                acceptParser.hasAskedForXML() &&
                response.getType().contentEquals("application/xml") &&
                response.getStatusCode()==200
        ){
            challengers.pass(challenger,CHALLENGE.GET_ACCEPT_XML);
        }

        if(request.getVerb() == HttpApiRequest.VERB.GET &&
                request.getPath().contentEquals("todos") &&
                request.getQueryParams().size()==0 &&
                acceptParser.hasAskedForJSON() &&
                response.getType().contentEquals("application/json") &&
                response.getStatusCode()==200
        ){
            challengers.pass(challenger,CHALLENGE.GET_ACCEPT_JSON);
        }

        if(request.getVerb() == HttpApiRequest.VERB.GET &&
                request.getPath().contentEquals("todos") &&
                request.getQueryParams().size()==0 &&
                acceptParser.missingAcceptHeader() &&
                response.getType().contentEquals("application/json") &&
                response.getStatusCode()==200
        ){
            challengers.pass(challenger,CHALLENGE.GET_JSON_BY_DEFAULT_NO_ACCEPT);
        }

        if(request.getVerb() == HttpApiRequest.VERB.GET &&
                request.getPath().contentEquals("todos") &&
                request.getQueryParams().size()==0 &&
                !acceptParser.isSupportedHeader() &&
                response.getStatusCode()==406
        ){
            challengers.pass(challenger,CHALLENGE.GET_UNSUPPORTED_ACCEPT_406);
        }

        if(request.getVerb() == HttpApiRequest.VERB.GET &&
                request.getPath().contentEquals("todos") &&
                request.getQueryParams().size()==0 &&
                acceptParser.hasAskedForANY() &&
                response.getType().contentEquals("application/json") &&
                response.getStatusCode()==200
        ){
            challengers.pass(challenger,CHALLENGE.GET_ACCEPT_ANY_DEFAULT_JSON);
        }

        if(request.getVerb() == HttpApiRequest.VERB.GET &&
                request.getPath().contentEquals("todos") &&
                request.getQueryParams().size()==0 &&
                acceptParser.hasAskedForXML() &&
                acceptParser.hasAskedForJSON() &&
                acceptParser.hasAPreferenceForXml() &&
                response.getType().contentEquals("application/xml") &&
                response.getStatusCode()==200
        ){
            challengers.pass(challenger,CHALLENGE.GET_ACCEPT_XML_PREFERRED);
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
                challengers.pass(challenger,CHALLENGE.GET_TODOS_FILTERED);
            }
        }



        // CREATE
        if(request.getVerb() == HttpApiRequest.VERB.POST &&
                request.getPath().matches("todos") &&
                response.getStatusCode()==201){
            challengers.pass(challenger,CHALLENGE.POST_TODOS);
        }

        if(request.getVerb() == HttpApiRequest.VERB.POST &&
                request.getPath().matches("todos") &&
                contentTypeParser.isXML() &&
                response.getType().contentEquals("application/xml") &&
                response.getStatusCode()==201){
            challengers.pass(challenger,CHALLENGE.POST_CREATE_XML);
        }

        if(request.getVerb() == HttpApiRequest.VERB.POST &&
                request.getPath().matches("todos") &&
                contentTypeParser.isJSON() &&
                acceptParser.hasAskedForJSON() &&
                response.getType().contentEquals("application/json") &&
                response.getStatusCode()==201){
            challengers.pass(challenger,CHALLENGE.POST_CREATE_JSON);
        }

        if(request.getVerb() == HttpApiRequest.VERB.POST &&
                request.getPath().matches("todos") &&
                contentTypeParser.isJSON() &&
                response.getType().contentEquals("application/xml") &&
                response.getStatusCode()==201){
            challengers.pass(challenger,CHALLENGE.POST_CREATE_JSON_ACCEPT_XML);
        }

        if(request.getVerb() == HttpApiRequest.VERB.POST &&
                request.getPath().matches("todos") &&
                acceptParser.hasAskedForJSON() &&
                contentTypeParser.isXML() &&
                response.getType().contentEquals("application/json") &&
                response.getStatusCode()==201){
            challengers.pass(challenger,CHALLENGE.POST_CREATE_XML_ACCEPT_JSON);
        }



        if(request.getVerb() == HttpApiRequest.VERB.POST &&
                request.getPath().matches("todos") &&
                response.getStatusCode()==400 &&
                collate(response.apiResponse().getErrorMessages()).contains(
                        "Failed Validation: doneStatus should be BOOLEAN")){
            challengers.pass(challenger,CHALLENGE.POST_TODOS_BAD_DONE_STATUS);
        }

        if(request.getVerb() == HttpApiRequest.VERB.POST &&
                request.getPath().matches("todos") &&
                response.getStatusCode()==415){
            challengers.pass(challenger,CHALLENGE.POST_TODOS_415);
        }

        // UPDATE
        if(request.getVerb() == HttpApiRequest.VERB.POST &&
                request.getPath().matches("todos/.*") &&
                response.getStatusCode()==200){
            challengers.pass(challenger,CHALLENGE.POST_UPDATE_TODO);
        }


        // DELETE
        if(request.getVerb() == HttpApiRequest.VERB.DELETE &&
                request.getPath().matches("todos/.*") &&
                response.getStatusCode()==200){
            challengers.pass(challenger,CHALLENGE.DELETE_A_TODO);
        }

        if(request.getVerb() == HttpApiRequest.VERB.DELETE &&
                request.getPath().matches("todos/.*") &&
                response.getStatusCode()==200 &&
                thingifier.getThingWithPluralNamed("todos").countInstances()==0){
            challengers.pass(challenger,CHALLENGE.DELETE_ALL_TODOS);
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
