package uk.co.compendiumdev.challenge.challengehooks;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.ContentTypeHeaderParser;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.EntityInstanceQuery;

public class ChallengerApiResponseHook implements HttpApiResponseHook {

    Logger logger = LoggerFactory.getLogger(ChallengerApiResponseHook.class);

    private final Challengers challengers;
    private final Thingifier thingifier;

    public ChallengerApiResponseHook(final Challengers challengers, Thingifier thingifier) {
        this.challengers = challengers;
        this.thingifier = thingifier;
    }

    @Override
    public HttpApiResponse run(
            final HttpApiRequest request,
            final HttpApiResponse response,
            final ThingifierApiConfig config) {

        ChallengerAuthData challenger =
                challengers.getChallenger(request.getHeader("X-CHALLENGER"));

        if (challenger == null) {
            // cannot track challenges
            return null;
        }

        // READ
        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().matches("todos/.*")
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_TODO);
        }

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().matches("todos/.*")
                && response.getStatusCode() == 404) {
            challengers.pass(challenger, CHALLENGE.GET_TODO_404);
        }

        final AcceptHeaderParser acceptParser = new AcceptHeaderParser(request.getHeader("accept"));
        final ContentTypeHeaderParser contentTypeParser =
                new ContentTypeHeaderParser(request.getHeader("content-type"));

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && request.getQueryParams().isEmpty()
                && acceptParser.hasAskedForXML()
                && response.getType().contentEquals("application/xml")
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_ACCEPT_XML);
        }

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && request.getQueryParams().isEmpty()
                && acceptParser.hasAskedForJSON()
                && response.getType().contentEquals("application/json")
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_ACCEPT_JSON);
        }

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && request.getQueryParams().isEmpty()
                && acceptParser.missingAcceptHeader()
                && response.getType().contentEquals("application/json")
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_JSON_BY_DEFAULT_NO_ACCEPT);
        }

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && request.getQueryParams().isEmpty()
                && !acceptParser.isSupportedHeader()
                && response.getStatusCode() == 406) {
            challengers.pass(challenger, CHALLENGE.GET_UNSUPPORTED_ACCEPT_406);
        }

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && request.getQueryParams().isEmpty()
                && acceptParser.hasAskedForANY()
                && response.getType().contentEquals("application/json")
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_ACCEPT_ANY_DEFAULT_JSON);
        }

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && request.getQueryParams().isEmpty()
                && acceptParser.hasAskedForXML()
                && acceptParser.hasAskedForJSON()
                && acceptParser.hasAPreferenceForXml()
                && response.getType().contentEquals("application/xml")
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_ACCEPT_XML_PREFERRED);
        }

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && request.getQueryParams().containsKey("doneStatus")
                && request.getQueryParams().get("doneStatus").contentEquals("true")
                && response.getStatusCode() == 200) {
            // only pass if there are done and not done todos
            final EntityInstance aDoneThing = findTodoByField(challenger, "doneStatus", "true");
            final EntityInstance aNotDoneThing = findTodoByField(challenger, "doneStatus", "false");
            if (aDoneThing != null && aNotDoneThing != null) {
                challengers.pass(challenger, CHALLENGE.GET_TODOS_FILTERED);
            }
        }

        // CREATE
        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && response.getStatusCode() == 201) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos/.*")
                && response.getStatusCode() == 404) {
            for (String errorMessage : response.apiResponse().getErrorMessages()) {
                if (errorMessage.startsWith("No such todo entity instance with id ==")) {
                    challengers.pass(challenger, CHALLENGE.POST_TODOS_404);
                }
            }
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && response.getStatusCode() == 201) {

            try {

                String location = response.getHeaders().get("Location");
                String[] locationParts = location.split("/");

                if (locationParts.length > 2) {
                    // to check it is an int
                    Integer.parseInt(locationParts[2]);
                    EntityInstance aTodo = findTodoByIdentifier(challenger, locationParts[2]);
                    if (aTodo != null
                            && aTodo.getFieldValue("title").asString().length() == 50
                            && aTodo.getFieldValue("description").asString().length() == 200) {
                        challengers.pass(
                                challenger, CHALLENGE.POST_MAX_OUT_TITLE_DESCRIPTION_LENGTH);
                    }
                }
            } catch (Exception e) {
                logger.warn("Error checking post todos 201 for max length ", e);
            }
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && contentTypeParser.isXML()
                && response.getType().contentEquals("application/xml")
                && response.getStatusCode() == 201) {
            challengers.pass(challenger, CHALLENGE.POST_CREATE_XML);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && contentTypeParser.isJSON()
                && acceptParser.hasAskedForJSON()
                && response.getType().contentEquals("application/json")
                && response.getStatusCode() == 201) {
            challengers.pass(challenger, CHALLENGE.POST_CREATE_JSON);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && contentTypeParser.isJSON()
                && response.getType().contentEquals("application/xml")
                && response.getStatusCode() == 201) {
            challengers.pass(challenger, CHALLENGE.POST_CREATE_JSON_ACCEPT_XML);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && acceptParser.hasAskedForJSON()
                && contentTypeParser.isXML()
                && response.getType().contentEquals("application/json")
                && response.getStatusCode() == 201) {
            challengers.pass(challenger, CHALLENGE.POST_CREATE_XML_ACCEPT_JSON);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && response.getStatusCode() == 400
                && collate(response.apiResponse().getErrorMessages())
                        .contains("Failed Validation: doneStatus should be BOOLEAN")) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_BAD_DONE_STATUS);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                &&
                // trap when creating or amending
                request.getPath().startsWith("todo")
                && response.getStatusCode() == 400
                && collate(response.apiResponse().getErrorMessages())
                        .contains(
                                "Failed Validation: Maximum allowable length exceeded for title - maximum allowed is 50")) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_TOO_LONG_TITLE_LENGTH);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                &&
                // trap when creating or amending
                request.getPath().startsWith("todo")
                && response.getStatusCode() == 400
                && collate(response.apiResponse().getErrorMessages())
                        .contains(
                                "Failed Validation: Maximum allowable length exceeded for description - maximum allowed is 200")) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_TOO_LONG_DESCRIPTION_LENGTH);
        }

        // POST to create too many todos
        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().contentEquals("todos")
                && response.getStatusCode() == 400
                && collate(response.apiResponse().getErrorMessages())
                        .contains("ERROR: Cannot add instance, maximum limit of 20 reached")) {
            challengers.pass(challenger, CHALLENGE.POST_ALL_TODOS);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && response.getStatusCode() == 415) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_415);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && response.getStatusCode() == 413
                && collate(response.apiResponse().getErrorMessages())
                        .toLowerCase()
                        .contains("request body too large")) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_TOO_LONG_PAYLOAD_SIZE);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && response.getStatusCode() == 400
                && collate(response.apiResponse().getErrorMessages())
                        .contains("Could not find field:")) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_INVALID_EXTRA_FIELD);
        }

        // UPDATE
        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos/.*")
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.POST_UPDATE_TODO);
        }

        // DELETE
        if (request.getVerb() == HttpApiRequest.VERB.DELETE
                && request.getPath().matches("todos/.*")
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.DELETE_A_TODO);
        }

        if (request.getVerb() == HttpApiRequest.VERB.DELETE
                && request.getPath().matches("todos/.*")
                && response.getStatusCode() == 200
                && countTodos(challenger) == 0) {
            challengers.pass(challenger, CHALLENGE.DELETE_ALL_TODOS);
        }

        // do not interfere with api and return null
        return null;
    }

    private EntityInstance findTodoByField(
            final ChallengerAuthData challenger, final String fieldName, final String fieldValue) {
        EntityDefinition todo = todoDefinition();
        EntityInstanceQuery query = queryFor(challenger);
        if (todo == null || query == null) {
            return null;
        }
        return query.findByField(todo, fieldName, fieldValue);
    }

    private EntityInstance findTodoByIdentifier(
            final ChallengerAuthData challenger, final String identifier) {
        EntityDefinition todo = todoDefinition();
        EntityInstanceQuery query = queryFor(challenger);
        if (todo == null || query == null) {
            return null;
        }
        return query.findByQueryIdentifier(todo, identifier);
    }

    private int countTodos(final ChallengerAuthData challenger) {
        EntityDefinition todo = todoDefinition();
        EntityInstanceQuery query = queryFor(challenger);
        if (todo == null || query == null) {
            return -1;
        }
        return query.count(todo);
    }

    private EntityInstanceQuery queryFor(final ChallengerAuthData challenger) {
        return thingifier.getStore(challenger.getXChallenger()).entityQueries();
    }

    private EntityDefinition todoDefinition() {
        return thingifier.getDefinitionNamed("todo");
    }

    String collate(Collection<String> strings) {
        StringBuilder collated = new StringBuilder();
        for (String string : strings) {
            collated.append(string);
            collated.append(" ");
        }
        return collated.toString();
    }
}
