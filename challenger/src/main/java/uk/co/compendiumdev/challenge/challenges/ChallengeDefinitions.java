package uk.co.compendiumdev.challenge.challenges;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerConfig;
import uk.co.compendiumdev.challenge.challenges.definitions.*;

import java.util.*;

public class ChallengeDefinitions {

    Logger logger = LoggerFactory.getLogger(ChallengeDefinitions.class);

    private final List<ChallengeSection> sections;
    Map<CHALLENGE, ChallengeDefinitionData> challengeData;
    List<ChallengeDefinitionData> orderedChallenges;

    public Collection<ChallengeDefinitionData> getChallenges() {
        return orderedChallenges;
    }

    public Collection<CHALLENGE> getDefinedChallenges() {
        return challengeData.keySet();
    }

    public Collection<ChallengeSection> getChallengeSections() {
        return sections;
    }



    private String renderChallengeNumber(int challengeOrder){
        return String.format("%02d", challengeOrder);
    }

    public ChallengeDefinitions(ChallengerConfig config){

        challengeData = new HashMap<>();
        orderedChallenges = new ArrayList<>();
        sections = new ArrayList<>();

        int challengeOrder = 1;
        ChallengeDefinitionData aChallenge;


        ChallengeSection getStarted = new ChallengeSection("Getting Started",
                "If you want to track your challenge progress, in multi-user mode then you need to solve the challenges in this section to generate a unique ID that we can associate your progress with.");
        sections.add(getStarted);

        // create a challenger to persist challenge sessions
        storeChallengeAs(CHALLENGE.CREATE_NEW_CHALLENGER, ChallengerChallenges.createChallenger201(challengeOrder++), getStarted);



        ChallengeSection firstChallenge = new ChallengeSection("First Real Challenge",
                "For your first challenge, get the list of challenges. You'll be able to use this to see your progress in your API Client, as well as using the GUI.");
        sections.add(firstChallenge);

        // GET all challenges as a list
        storeChallengeAs(CHALLENGE.GET_CHALLENGES, GetChallenges.getChallenges200(challengeOrder++), firstChallenge);


        ChallengeSection getChallenges = new ChallengeSection("GET Challenges",
                "To retrieve, or read information from an API we issue GET requests. This section has a bunch of GET request challenges to try out.");
        sections.add(getChallenges);

        storeChallengeAs(CHALLENGE.GET_TODOS, GetChallenges.getTodos200(challengeOrder++), getChallenges);
        storeChallengeAs(CHALLENGE.GET_TODOS_NOT_PLURAL_404, GetChallenges.getTodos404(challengeOrder++), getChallenges);
        storeChallengeAs(CHALLENGE.GET_TODO, GetChallenges.getTodo200(challengeOrder++), getChallenges);
        storeChallengeAs(CHALLENGE.GET_TODO_404, GetChallenges.getTodo404(challengeOrder++), getChallenges);
        storeChallengeAs(CHALLENGE.GET_TODOS_FILTERED, GetChallenges.getTodosFiltered200(challengeOrder++), getChallenges);


        // HEAD
        ChallengeSection headChallenges = new ChallengeSection("HEAD Challenges",
                "A HEAD request, is like a GET request, but only returns the headers and status code.");
        sections.add(headChallenges);

        storeChallengeAs(CHALLENGE.GET_HEAD_TODOS, HeadChallenges.headTodos200(challengeOrder++), headChallenges);


        // CREATE with POST
        ChallengeSection postCreateChallenges = new ChallengeSection("Creation Challenges with POST",
                "A POST request can be used to create and update data, these challenges are to 'create' data. As a Hint, if you are not sure what the message body should be, try copying in the response from the associated GET request, and amending it.");
        sections.add(postCreateChallenges);

        storeChallengeAs(CHALLENGE.POST_TODOS, PostChallenges.postTodos201(challengeOrder++), postCreateChallenges);
        storeChallengeAs(CHALLENGE.POST_TODOS_BAD_DONE_STATUS, PostChallenges.postTodosBadDoneStatus400(challengeOrder++), postCreateChallenges);
        storeChallengeAs(CHALLENGE.POST_TODOS_TOO_LONG_TITLE_LENGTH, PostChallenges.postTodosTitleTooLong400(challengeOrder++), postCreateChallenges);
        storeChallengeAs(CHALLENGE.POST_TODOS_TOO_LONG_DESCRIPTION_LENGTH, PostChallenges.postTodosDescriptionTooLong400(challengeOrder++), postCreateChallenges);
        storeChallengeAs(CHALLENGE.POST_MAX_OUT_TITLE_DESCRIPTION_LENGTH, PostChallenges.postTodosMaxTitleDescriptionTooLong400(challengeOrder++), postCreateChallenges);
        storeChallengeAs(CHALLENGE.POST_TODOS_TOO_LONG_PAYLOAD_SIZE, PostChallenges.postTodosPayloadTooLong400(challengeOrder++), postCreateChallenges);
        storeChallengeAs(CHALLENGE.POST_TODOS_INVALID_EXTRA_FIELD, PostChallenges.postTodosInvalidExtraField400(challengeOrder++), postCreateChallenges);



        // CREATE with PUT
        ChallengeSection putCreateChallenges = new ChallengeSection("Creation Challenges with PUT",
                "A PUT request can often used to create and update data. The todo application we are using has automatically generated ids, so you cannot use PUT to create. As a Hint, if you are not sure what the message body should be, try copying in the response from the associated GET request, and amending it.");
        sections.add(putCreateChallenges);

        storeChallengeAs(CHALLENGE.PUT_TODOS_400, PutChallenges.putTodosId400(challengeOrder++), putCreateChallenges);



        // UPDATE with POST
        ChallengeSection postUpdateChallenges = new ChallengeSection("Update Challenges with POST",
                "Use a POST request to amend something that already exists. These are 'partial' content updates so you usually don't need to have all details of the entity in the request, e.g. you could just update a title, or a description, or a status");
        sections.add(postUpdateChallenges);

        storeChallengeAs(CHALLENGE.POST_UPDATE_TODO, PostChallenges.postTodosId200(challengeOrder++), postUpdateChallenges);
        storeChallengeAs(CHALLENGE.POST_TODOS_404, PostChallenges.postTodosId404(challengeOrder++), postUpdateChallenges);



        // UPDATE with PUT
        ChallengeSection putUpdateChallenges = new ChallengeSection("Update Challenges with PUT",
                "A PUT request can be used to amend data. REST Put requests are idempotent, they provide the same result each time.");
        sections.add(putUpdateChallenges);

        storeChallengeAs(CHALLENGE.PUT_TODOS_FULL_200, PutChallenges.putTodosIdFull200(challengeOrder++), putUpdateChallenges);
        storeChallengeAs(CHALLENGE.PUT_TODOS_PARTIAL_200, PutChallenges.putTodosIdPartial200(challengeOrder++), putUpdateChallenges);
        storeChallengeAs(CHALLENGE.PUT_TODOS_MISSING_TITLE_400, PutChallenges.putTodosIdNoTitle400(challengeOrder++), putUpdateChallenges);
        storeChallengeAs(CHALLENGE.PUT_TODOS_400_NO_AMEND_ID, PutChallenges.putTodosIdNonMatchedIdsAmend400(challengeOrder++), putUpdateChallenges);



        // DELETE
        ChallengeSection deleteChallenges = new ChallengeSection("DELETE Challenges",
                "Use a DELETE request to delete an entity. Since this is an extreme request, normally you have to be logged in or authenticated, but we wanted to make life easier for you so we cover authentication later. Anyone can delete To Do items without authentication in this system.");
        sections.add(deleteChallenges);

        storeChallengeAs(CHALLENGE.DELETE_A_TODO, DeleteChallenges.deleteTodosId200(challengeOrder++), deleteChallenges);



        // OPTIONS
        ChallengeSection optionsChallenges = new ChallengeSection("OPTIONS Challenges",
                "Use an OPTIONS verb and check the `Allow` header, this will show you what verbs are allowed to be used on an endpoint. When you test APIs it is worth checking to see if all the verbs listed are allowed or not.");
        sections.add(optionsChallenges);

        storeChallengeAs(CHALLENGE.OPTIONS_TODOS, OptionsChallenges.optionsTodos200(challengeOrder++), optionsChallenges);



        ChallengeSection acceptChallenges = new ChallengeSection("Accept Challenges",
                "The `Accept` header, tells the server what format you want the response to be in. By changing the `Accept` header you can specify JSON or XML.");
        sections.add(acceptChallenges);

        storeChallengeAs(CHALLENGE.GET_ACCEPT_XML, GetChallenges.getTodosAcceptXML200(challengeOrder++), acceptChallenges);
        storeChallengeAs(CHALLENGE.GET_ACCEPT_JSON, GetChallenges.getTodosAcceptJson200(challengeOrder++), acceptChallenges);
        storeChallengeAs(CHALLENGE.GET_ACCEPT_ANY_DEFAULT_JSON, GetChallenges.getTodosAcceptAny200(challengeOrder++), acceptChallenges);
        storeChallengeAs(CHALLENGE.GET_ACCEPT_XML_PREFERRED, GetChallenges.getTodosPreferAcceptXML200(challengeOrder++), acceptChallenges);
        storeChallengeAs(CHALLENGE.GET_JSON_BY_DEFAULT_NO_ACCEPT, GetChallenges.getTodosNoAccept200(challengeOrder++), acceptChallenges);
        storeChallengeAs(CHALLENGE.GET_UNSUPPORTED_ACCEPT_406, GetChallenges.getTodosUnavailableAccept406(challengeOrder++), acceptChallenges);




        ChallengeSection contentTypeChallenges = new ChallengeSection("Content-Type Challenges",
                "The `Content-Type` header, tells the server what format type your 'body' content is, e.g. are you sending XML or JSON.");
        sections.add(contentTypeChallenges);

        storeChallengeAs(CHALLENGE.POST_CREATE_XML, PostChallenges.postCreateTodoWithXMLAcceptXML(challengeOrder++), contentTypeChallenges);
        storeChallengeAs(CHALLENGE.POST_CREATE_JSON, PostChallenges.postCreateTodoWithJsonAcceptJson(challengeOrder++), contentTypeChallenges);
        storeChallengeAs(CHALLENGE.POST_TODOS_415, PostChallenges.postCreateUnsupportedContentType415(challengeOrder++), contentTypeChallenges);




        // adjust the take a break challenges based on the app configuration
        ChallengeSection restoreChallenger = new ChallengeSection("Fancy a Break? Restore your session",
                "Your challenge progress can be saved, and as long as you remember you challenger ID you can restore it. Leaving a challenger idle in the system for more than 10 minutes will remove the challenger from memory. Challenger status and the todos database can be saved to, and restored from, the browser localStorage.");
        sections.add(restoreChallenger);

        // if persistence layer is set to cloud or file then the following apply
        if(config.persistenceLayer.willAutoSaveChallengerStatusToPersistenceLayer() && !config.single_player_mode) {
            storeChallengeAs(CHALLENGE.GET_RESTORE_EXISTING_CHALLENGER, ChallengerChallenges.getRestoreExistingChallenger200(challengeOrder++), restoreChallenger);
            storeChallengeAs(CHALLENGE.POST_RESTORE_EXISTING_CHALLENGER, ChallengerChallenges.postRestoreExistingChallenger200(challengeOrder++), restoreChallenger);
        }

        // GET the restorable version of challenger progress via api
        storeChallengeAs(CHALLENGE.GET_RESTORABLE_CHALLENGER_PROGRESS_STATUS, ChallengerChallenges.getRestorableExistingChallengerProgress200(challengeOrder++), restoreChallenger);
        // PUT to restore challenger progress via api
        storeChallengeAs(CHALLENGE.PUT_RESTORABLE_CHALLENGER_PROGRESS_STATUS, ChallengerChallenges.putRestoreChallengerProgress200(challengeOrder++), restoreChallenger);

        // the create with PUT is only valid in multi-user mode, pass in the mode and exclude this challenge
        if(!config.single_player_mode) {
            storeChallengeAs(CHALLENGE.PUT_NEW_RESTORED_CHALLENGER_PROGRESS_STATUS, ChallengerChallenges.putRestoreChallengerProgress201(challengeOrder++), restoreChallenger);
        }

        // GET the restorable version of todos database via api
        storeChallengeAs(CHALLENGE.GET_RESTORABLE_TODOS, ChallengerChallenges.getRestorableTodos200(challengeOrder++), restoreChallenger);

        // PUT to restore version of todos via api
        storeChallengeAs(CHALLENGE.PUT_RESTORABLE_TODOS, ChallengerChallenges.putRestorableTodos204(challengeOrder++), restoreChallenger);





        // POST mixed content and accept
        ChallengeSection mixAcceptContentTypeChallenges = new ChallengeSection("Mix Accept and Content-Type Challenges",
                "We can mix the `Accept` and `Content-Type` headers so that we can send JSON but receive XML. These challenges encourage you to explore some combinations.");
        sections.add(mixAcceptContentTypeChallenges);

        storeChallengeAs(CHALLENGE.POST_CREATE_XML_ACCEPT_JSON, PostChallenges.postTodosXmlToJson201(challengeOrder++), mixAcceptContentTypeChallenges);
        storeChallengeAs(CHALLENGE.POST_CREATE_JSON_ACCEPT_XML, PostChallenges.postTodosJsonToXml201(challengeOrder++), mixAcceptContentTypeChallenges);




        ChallengeSection miscStatusCodes = new ChallengeSection("Status Code Challenges",
                "Status-codes are essential to understand, so we created some challenges that help you trigger more status codes. Remember to review httpstatuses.com to learn what the status codes mean.");
        sections.add(miscStatusCodes);

        storeChallengeAs(CHALLENGE.DELETE_HEARTBEAT_405, StatusCodeChallenges.methodNotAllowed405UsingDelete(challengeOrder++), miscStatusCodes);
        storeChallengeAs(CHALLENGE.PATCH_HEARTBEAT_500, StatusCodeChallenges.serverError500UsingPatch(challengeOrder++), miscStatusCodes);
        storeChallengeAs(CHALLENGE.TRACE_HEARTBEAT_501, StatusCodeChallenges.notImplemented501UsingTrace(challengeOrder++), miscStatusCodes);
        storeChallengeAs(CHALLENGE.GET_HEARTBEAT_204, StatusCodeChallenges.noContent204UsingGet(challengeOrder++), miscStatusCodes);

        ChallengeSection methodOverrideChallenges = new ChallengeSection("HTTP Method Override Challenges",
                "Some HTTP Clients can not send all verbs e.g. PATCH, DELETE, PUT. Use an X-HTTP-Method-Override header to simulate these with a POST request");
        sections.add(methodOverrideChallenges);

        storeChallengeAs(CHALLENGE.OVERRIDE_DELETE_HEARTBEAT_405, StatusCodeChallenges.overridePostToDeleteFor405(challengeOrder++), methodOverrideChallenges);
        storeChallengeAs(CHALLENGE.OVERRIDE_PATCH_HEARTBEAT_500, StatusCodeChallenges.overridePostToPatchFor500(challengeOrder++), methodOverrideChallenges);
        storeChallengeAs(CHALLENGE.OVERRIDE_TRACE_HEARTBEAT_501, StatusCodeChallenges.overridePostToTraceFor501(challengeOrder++), methodOverrideChallenges);



        // authorization and authentication
        ChallengeSection authenticationChallenges = new ChallengeSection("Authentication Challenges",
                "Authentication is telling the system who you are. In multi-user mode you are already doing that with the X-CHALLENGER header, but we have added an extra level of security on the /secret section. So first Authenticate with Basic Authentication to find out the token to use for authorisation for later challenges.");
        sections.add(authenticationChallenges);

        storeChallengeAs(CHALLENGE.CREATE_SECRET_TOKEN_401, SecretTokenChallenges.createSecretTokenNotAuthenticated401(challengeOrder++), authenticationChallenges);
        storeChallengeAs(CHALLENGE.CREATE_SECRET_TOKEN_201, SecretTokenChallenges.createSecretTokenAuthenticated201(challengeOrder++), authenticationChallenges);




        ChallengeSection authorizationChallenges = new ChallengeSection("Authorization Challenges",
                "Once the system knows who you are, authorization is if you have the correct level of access. In these challenges the authorization is granted using a custom API header X-AUTH-TOKEN or using a Bearer Authorization header.");
        sections.add(authorizationChallenges);

        storeChallengeAs(CHALLENGE.GET_SECRET_NOTE_403, SecretTokenChallenges.forbiddenNotAuthorized403(challengeOrder++), authorizationChallenges);
        storeChallengeAs(CHALLENGE.GET_SECRET_NOTE_401, SecretTokenChallenges.invalidRequestNoAuthHeader401(challengeOrder++), authorizationChallenges);
        storeChallengeAs(CHALLENGE.GET_SECRET_NOTE_200, SecretTokenChallenges.authorizedGet200(challengeOrder++), authorizationChallenges);
        storeChallengeAs(CHALLENGE.POST_SECRET_NOTE_200, SecretTokenChallenges.authorizedUpdate200(challengeOrder++), authorizationChallenges);
        storeChallengeAs(CHALLENGE.POST_SECRET_NOTE_401, SecretTokenChallenges.postMissingTokenAuth401(challengeOrder++), authorizationChallenges);
        storeChallengeAs(CHALLENGE.POST_SECRET_NOTE_403, SecretTokenChallenges.postInvalidTokenAuth401(challengeOrder++), authorizationChallenges);
        storeChallengeAs(CHALLENGE.GET_SECRET_NOTE_BEARER_200, SecretTokenChallenges.getWithValidBearerToken200(challengeOrder++), authorizationChallenges);
        storeChallengeAs(CHALLENGE.POST_SECRET_NOTE_BEARER_200, SecretTokenChallenges.postUpdateWithValidBearerToken200(challengeOrder++), authorizationChallenges);



        // misc

        ChallengeSection miscChallenges = new ChallengeSection("Miscellaneous Challenges",
                "We left these challenges to the end because they seemed fun, but... different.");
        sections.add(miscChallenges);


        storeChallengeAs(CHALLENGE.DELETE_ALL_TODOS, MiscChallenges.deleteAllTodos200(challengeOrder++), miscChallenges);
        storeChallengeAs(CHALLENGE.POST_ALL_TODOS, MiscChallenges.postAllTodos201(challengeOrder++), miscChallenges);






        Set challengeNames = new HashSet();
        for(ChallengeDefinitionData challenge : orderedChallenges){
            logger.info("Setup Challenge: " + challenge.name);
            challengeNames.add(challenge.name);
        }
        int sectionChallengesCount = 0;
        for(ChallengeSection section : sections){
            sectionChallengesCount += section.getChallenges().size();
        }
        if(sectionChallengesCount!= orderedChallenges.size()) {
            throw new RuntimeException(
                    "Number challenges in sections, does not match number of challenges" +
                            ", possibly forgot to add section or challenge to section");
        }
        if(challengeNames.size()!= orderedChallenges.size()) {
            throw new RuntimeException(
                    "Number of names, does not match number of challenges" +
                            ", possible duplicate name");
        }
    }

    private ChallengeDefinitionData storeChallengeAs(
            final CHALLENGE id,
            final ChallengeDefinitionData challenge,
            ChallengeSection section) {

        challengeData.put(id, challenge);
        orderedChallenges.add(challenge);
        section.addChallenge(challenge);

        return challenge;
    }


    public CHALLENGE getChallenge(final String name) {
        for(Map.Entry<CHALLENGE, ChallengeDefinitionData>challenge : challengeData.entrySet()){
            if(challenge.getValue().name.contentEquals(name)){
                return challenge.getKey();
            }
        }
        return null;
    }
}
