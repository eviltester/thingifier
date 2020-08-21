package uk.co.compendiumdev.challenge.challengehooks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;

import java.lang.reflect.Field;

public class ChallengerInternalHttpRequestHookTest {

    @Test
    public void inMultUserModeWeNeedAnXChallengerHeaderToTrackChallenges(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();

        final ChallengerInternalHTTPRequestHook hook = new ChallengerInternalHTTPRequestHook(challengers);

        final HttpApiRequest request = new
                HttpApiRequest("/challenges").
                        setVerb(HttpApiRequest.VERB.GET);

        hook.run(request);

        Assertions.assertFalse(
                challengers.SINGLE_PLAYER.statusOfChallenge
                        (CHALLENGE.GET_CHALLENGES));
    }

    @Test
    public void inMultUserModeAddingAnXChallengerHeaderWillTouchTheChallenger() throws NoSuchFieldException, IllegalAccessException {

        Challengers challengers = new Challengers();
        final ChallengerAuthData challenger = challengers.createNewChallenger();

        // risk that this test is intermittent if it all happens in the same millisecond
        // if so could 'wait' in the test, or 'hack the object to have a different last accessed time'
        // set private variable that test will 'touch'
        // when using mock at spark level, did not need to 'hack' the object
        Field lastAccessedField = ChallengerAuthData.class.getDeclaredField("lastAccessed");
        lastAccessedField.setAccessible(true);
        lastAccessedField.set(challenger, 0L);
        Assertions.assertEquals(0, challenger.getLastAccessed());

        long initialTouchTime = challenger.getLastAccessed();

        challengers.setMultiPlayerMode();

        long preTouchTime = System.currentTimeMillis();

        final ChallengerInternalHTTPRequestHook hook = new ChallengerInternalHTTPRequestHook(challengers);

        final HttpApiRequest request = new
                HttpApiRequest("/challenges").
                setVerb(HttpApiRequest.VERB.GET);
        request.addHeader("X-CHALLENGER", challenger.getXChallenger());

        hook.run(request);

        // we passed the GET /challenges for default user
        Assertions.assertTrue(
                challenger.statusOfChallenge
                        (CHALLENGE.GET_CHALLENGES));

        Assertions.assertNotEquals(0, challenger.getLastAccessed());
        Assertions.assertNotEquals(initialTouchTime, challenger.getLastAccessed());
        Assertions.assertTrue(challenger.getLastAccessed() >= preTouchTime);
    }


    @Test
    public void inMultUserModeWeNeedAnXChallengerHeaderThatExistsToTrackChallenges(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();

        final ChallengerInternalHTTPRequestHook hook = new ChallengerInternalHTTPRequestHook(challengers);

        final HttpApiRequest request = new
                HttpApiRequest("/challenges").
                setVerb(HttpApiRequest.VERB.GET).
                addHeader("X-CHALLENGER",
                "bobobobobobobobobobo");

        hook.run(request);

        // nothing changes - assert on this
        // we passed the GET /challenges for default user
        Assertions.assertFalse(
                challengers.SINGLE_PLAYER.statusOfChallenge
                        (CHALLENGE.GET_CHALLENGES));
    }

    @Test
    public void inSinglePlayerModeNoHeaderMeansUseDefaultChallenger(){

        Challengers challengers = new Challengers();

        final ChallengerInternalHTTPRequestHook hook = new ChallengerInternalHTTPRequestHook(challengers);

        final HttpApiRequest request = new
                HttpApiRequest("/challenges").
                setVerb(HttpApiRequest.VERB.GET);

        hook.run(request);

        // we passed the GET /challenges for default user
        Assertions.assertTrue(
                challengers.SINGLE_PLAYER.statusOfChallenge
                        (CHALLENGE.GET_CHALLENGES));
    }


    @Test
    public void canGetHeartbeatInMultiPlayer(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();
        final ChallengerAuthData challenger =
                challengers.createNewChallenger();

        ChallengerInternalHTTPRequestHook hook =
                new ChallengerInternalHTTPRequestHook(challengers);

        HttpApiRequest request =
                new HttpApiRequest("/heartbeat").
                        setVerb("GET");
        request.addHeader("X-CHALLENGER",
                        challenger.getXChallenger());
        hook.run(request);

        Assertions.assertTrue(
                challenger.statusOfChallenge
                        (CHALLENGE.GET_HEARTBEAT_204));
    }

    @Test
    public void canTraceHeartbeatInMultiPlayer(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();
        final ChallengerAuthData challenger =
                challengers.createNewChallenger();

        ChallengerInternalHTTPRequestHook hook =
                new ChallengerInternalHTTPRequestHook(challengers);

        Assertions.assertFalse(
                challenger.statusOfChallenge
                        (CHALLENGE.TRACE_HEARTBEAT_501));

        HttpApiRequest request =
                new HttpApiRequest("/heartbeat").
                        setVerb("TRACE");
        request.addHeader("X-CHALLENGER",
                challenger.getXChallenger());
        hook.run(request);

        Assertions.assertTrue(
                challenger.statusOfChallenge
                        (CHALLENGE.TRACE_HEARTBEAT_501));
    }

    @Test
    public void canDeleteHeartbeatInMultiPlayer(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();
        final ChallengerAuthData challenger =
                challengers.createNewChallenger();

        ChallengerInternalHTTPRequestHook hook =
                new ChallengerInternalHTTPRequestHook(challengers);

        Assertions.assertFalse(
                challenger.statusOfChallenge
                        (CHALLENGE.DELETE_HEARTBEAT_405));

        HttpApiRequest request =
                new HttpApiRequest("/heartbeat").
                        setVerb("DELETE");
        request.addHeader("X-CHALLENGER",
                challenger.getXChallenger());
        hook.run(request);

        Assertions.assertTrue(
                challenger.statusOfChallenge
                        (CHALLENGE.DELETE_HEARTBEAT_405));
    }

    @Test
    public void canPatchHeartbeatInMultiPlayer(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();
        final ChallengerAuthData challenger =
                challengers.createNewChallenger();

        ChallengerInternalHTTPRequestHook hook =
                new ChallengerInternalHTTPRequestHook(challengers);

        Assertions.assertFalse(
                challenger.statusOfChallenge
                        (CHALLENGE.PATCH_HEARTBEAT_500));

        HttpApiRequest request =
                new HttpApiRequest("/heartbeat").
                        setVerb("PATCH");
        request.addHeader("X-CHALLENGER",
                challenger.getXChallenger());
        hook.run(request);

        Assertions.assertTrue(
                challenger.statusOfChallenge
                        (CHALLENGE.PATCH_HEARTBEAT_500));
    }

}
