package uk.co.compendiumdev.challenge.challengehooks;

import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.InternalHttpResponse;

import java.lang.reflect.Field;

public class ChallengerInternalHttpResponseHookTest {

    @Test
    public void inMultUserModeWeNeedAnNonNullXChallengerHeaderOrSeeUnknownChallengerHeader(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();

        final ChallengerInternalHTTPResponseHook hook = new ChallengerInternalHTTPResponseHook(challengers);

        final HttpApiRequest request = new
                HttpApiRequest("/challenges").
                        setVerb(HttpApiRequest.VERB.GET);

        final InternalHttpResponse response = new
                InternalHttpResponse();

        hook.run(request, response);

        Assertions.assertTrue(response.getHeader("X-CHALLENGER").startsWith("UNKNOWN CHALLENGER"));
    }

    public void inMultUserModeWeNeedAValidXChallengerHeaderOrSeeUnknownChallengerHeader(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();

        final ChallengerInternalHTTPResponseHook hook = new ChallengerInternalHTTPResponseHook(challengers);

        final HttpApiRequest request = new
                HttpApiRequest("/challenges").
                setVerb(HttpApiRequest.VERB.GET).addHeader("X-CHALLENGER", "bob");

        final InternalHttpResponse response = new
                InternalHttpResponse();

        hook.run(request, response);

        Assertions.assertTrue(response.getHeader("X-CHALLENGER").startsWith("UNKNOWN CHALLENGER"));
    }



    @Test
    public void canPostChallengerToCreateAChallenger(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();

        ChallengerInternalHTTPResponseHook hook =
                new ChallengerInternalHTTPResponseHook(challengers);

        HttpApiRequest request =
                new HttpApiRequest("/challenger").
                        setVerb("POST");

        final ChallengerAuthData challenger = challengers.createNewChallenger();

        InternalHttpResponse response =
                new InternalHttpResponse().setStatus(201).
                        setHeader("X-CHALLENGER", challenger.getXChallenger());

        hook.run(request,response);

        Assertions.assertNotNull(response.getHeader("X-CHALLENGER"));

        String guid = response.getHeader("X-CHALLENGER");
        final ChallengerAuthData achallenger = challengers.getChallenger(guid);

        Assertions.assertTrue(
                achallenger.statusOfChallenge
                        (CHALLENGE.CREATE_NEW_CHALLENGER));
    }

    @Test
    public void givenAValidChallengerAddTheGuidIntoTheResponse(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();

        ChallengerInternalHTTPResponseHook hook =
                new ChallengerInternalHTTPResponseHook(challengers);

        final ChallengerAuthData challenger = challengers.createNewChallenger();

        HttpApiRequest request =
                new HttpApiRequest("/challenger").
                        setVerb("GET").addHeader("X-CHALLENGER", challenger.getXChallenger());

        InternalHttpResponse response =
                new InternalHttpResponse().setStatus(200);

        hook.run(request,response);

        Assertions.assertNotNull(response.getHeader("X-CHALLENGER"));

        String guid = response.getHeader("X-CHALLENGER");
        final ChallengerAuthData achallenger = challengers.getChallenger(guid);

        Assertions.assertEquals(achallenger.getXChallenger(),
                            response.getHeader("X-CHALLENGER"));
    }

    @Test
    public void getTodoWhichReturns404PassesChallenge(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();

        ChallengerInternalHTTPResponseHook hook =
                new ChallengerInternalHTTPResponseHook(challengers);

        final ChallengerAuthData challenger = challengers.createNewChallenger();

        HttpApiRequest request =
                new HttpApiRequest("/todo").
                        setVerb("GET")
                        .addHeader("X-CHALLENGER", challenger.getXChallenger());

        InternalHttpResponse response =
                new InternalHttpResponse().setStatus(404);

        hook.run(request,response);

        Assertions.assertTrue(
                challenger.statusOfChallenge(
                        CHALLENGE.GET_TODOS_NOT_PLURAL_404)
        );
    }

    @Test
    public void optionsOnTodosPassesChallenge(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();

        ChallengerInternalHTTPResponseHook hook =
                new ChallengerInternalHTTPResponseHook(challengers);

        final ChallengerAuthData challenger = challengers.createNewChallenger();

        HttpApiRequest request =
                new HttpApiRequest("/todos").
                        setVerb("OPTIONS")
                        .addHeader("X-CHALLENGER", challenger.getXChallenger());

        InternalHttpResponse response =
                new InternalHttpResponse().setStatus(200);

        hook.run(request,response);

        Assertions.assertTrue(
                challenger.statusOfChallenge(
                        CHALLENGE.OPTIONS_TODOS)
        );
    }

    @Test
    public void postSecretTokenWrongAuthorizationChallenge(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();

        ChallengerInternalHTTPResponseHook hook =
                new ChallengerInternalHTTPResponseHook(challengers);

        final ChallengerAuthData challenger = challengers.createNewChallenger();

        HttpApiRequest request =
                new HttpApiRequest("/secret/token").
                        setVerb("POST")
                        .addHeader("Authorization", "ididntcheck")
                        .addHeader("X-CHALLENGER", challenger.getXChallenger());

        InternalHttpResponse response =
                new InternalHttpResponse().setStatus(401);

        hook.run(request,response);

        Assertions.assertTrue(
                challenger.statusOfChallenge(
                        CHALLENGE.CREATE_SECRET_TOKEN_401)
        );
    }

    @Test
    public void postSecretTokenCorrectAuthorizationChallenge(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();

        ChallengerInternalHTTPResponseHook hook =
                new ChallengerInternalHTTPResponseHook(challengers);

        final ChallengerAuthData challenger = challengers.createNewChallenger();

        HttpApiRequest request =
                new HttpApiRequest("/secret/token").
                        setVerb("POST")
                        .addHeader("Authorization", "ididntcheck")
                        .addHeader("X-CHALLENGER", challenger.getXChallenger());

        InternalHttpResponse response =
                new InternalHttpResponse().setStatus(201);

        hook.run(request,response);

        Assertions.assertTrue(
                challenger.statusOfChallenge(
                        CHALLENGE.CREATE_SECRET_TOKEN_201)
        );
    }

    @Test
    public void getNoteWithValidXAuthToken(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();

        ChallengerInternalHTTPResponseHook hook =
                new ChallengerInternalHTTPResponseHook(challengers);

        final ChallengerAuthData challenger = challengers.createNewChallenger();

        HttpApiRequest request =
                new HttpApiRequest("/secret/note").
                        setVerb("GET")
                        .addHeader("X-AUTH-TOKEN", "nocheck")
                        .addHeader("X-CHALLENGER", challenger.getXChallenger());

        InternalHttpResponse response =
                new InternalHttpResponse().setStatus(403);

        hook.run(request,response);

        Assertions.assertTrue(
                challenger.statusOfChallenge(
                        CHALLENGE.GET_SECRET_NOTE_403)
        );
    }

    @Test
    public void getNoteWithNoAuthTokenFailChallenge(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();

        ChallengerInternalHTTPResponseHook hook =
                new ChallengerInternalHTTPResponseHook(challengers);

        final ChallengerAuthData challenger = challengers.createNewChallenger();

        HttpApiRequest request =
                new HttpApiRequest("/secret/note").
                        setVerb("GET")
                        .addHeader("X-CHALLENGER", challenger.getXChallenger());

        InternalHttpResponse response =
                new InternalHttpResponse().setStatus(401);

        hook.run(request,response);

        Assertions.assertTrue(
                challenger.statusOfChallenge(
                        CHALLENGE.GET_SECRET_NOTE_401)
        );
    }

    @Test
    public void cannotAmendNoteWithInvalidAuthTokenFailChallenge(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();

        ChallengerInternalHTTPResponseHook hook =
                new ChallengerInternalHTTPResponseHook(challengers);

        final ChallengerAuthData challenger = challengers.createNewChallenger();

        HttpApiRequest request =
                new HttpApiRequest("/secret/note").
                        setVerb("POST")
                        .addHeader("X-AUTH-TOKEN", "nocheck")
                        .addHeader("X-CHALLENGER", challenger.getXChallenger())
                        .setBody("{\"note\":\"bob\"");

        InternalHttpResponse response =
                new InternalHttpResponse().setStatus(403);

        hook.run(request,response);

        Assertions.assertTrue(
                challenger.statusOfChallenge(
                        CHALLENGE.POST_SECRET_NOTE_403)
        );
    }

    @Test
    public void forbiddenAmendNOAuthTokenForNote(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();

        ChallengerInternalHTTPResponseHook hook =
                new ChallengerInternalHTTPResponseHook(challengers);

        final ChallengerAuthData challenger = challengers.createNewChallenger();

        HttpApiRequest request =
                new HttpApiRequest("/secret/note").
                        setVerb("POST")
                        .addHeader("X-CHALLENGER", challenger.getXChallenger())
                        .setBody("{\"note\":\"bob\"");

        InternalHttpResponse response =
                new InternalHttpResponse().setStatus(401);

        hook.run(request,response);

        Assertions.assertTrue(
                challenger.statusOfChallenge(
                        CHALLENGE.POST_SECRET_NOTE_401)
        );
    }

    @Test
    public void canAmendNoteUsingAuthTokenPass(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();

        ChallengerInternalHTTPResponseHook hook =
                new ChallengerInternalHTTPResponseHook(challengers);

        final ChallengerAuthData challenger = challengers.createNewChallenger();

        HttpApiRequest request =
                new HttpApiRequest("/secret/note").
                        setVerb("POST")
                        .addHeader("X-AUTH-TOKEN", "validtoken")
                        .addHeader("X-CHALLENGER", challenger.getXChallenger())
                        .setBody("{\"note\":\"bob\"");

        InternalHttpResponse response =
                new InternalHttpResponse().setStatus(200);

        hook.run(request,response);

        Assertions.assertTrue(
                challenger.statusOfChallenge(
                        CHALLENGE.POST_SECRET_NOTE_200)
        );
    }

    @Test
    public void canGetNoteUsingAuthTokenPass() {

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();

        ChallengerInternalHTTPResponseHook hook =
                new ChallengerInternalHTTPResponseHook(challengers);

        final ChallengerAuthData challenger = challengers.createNewChallenger();

        HttpApiRequest request =
                new HttpApiRequest("/secret/note").
                        setVerb("GET")
                        .addHeader("X-AUTH-TOKEN", "validtoken")
                        .addHeader("X-CHALLENGER", challenger.getXChallenger());

        InternalHttpResponse response =
                new InternalHttpResponse().setStatus(200);

        hook.run(request, response);

        Assertions.assertTrue(
                challenger.statusOfChallenge(
                        CHALLENGE.GET_SECRET_NOTE_200)
        );
    }
    // todo create an internal set of tests which pass the challenges 'for real'
}
