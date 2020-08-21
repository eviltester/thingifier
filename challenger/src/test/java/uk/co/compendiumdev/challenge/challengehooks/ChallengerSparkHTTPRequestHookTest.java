package uk.co.compendiumdev.challenge.challengehooks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import spark.Request;
import spark.Response;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Disabled("migrating to internal hooks rather than spark hooks")
public class ChallengerSparkHTTPRequestHookTest {

    @Test
    public void inMultUserModeWeNeedAnXChallengerHeaderToTrackChallenges(){

        Challengers challengers = new Challengers();
        challengers.setMultiPlayerMode();

        final ChallengerSparkHTTPRequestHook hook = new ChallengerSparkHTTPRequestHook(challengers);

        Request request = mock(Request.class);
        Response response = mock(Response.class);
        when(request.headers("X-CHALLENGER")).thenReturn(null);
        when(request.requestMethod()).thenReturn("GET");
        when(request.pathInfo()).thenReturn("/challenges");
        hook.run(request, response);

        // nothing changes - assert on this
        // we passed the GET /challenges for default user
        Assertions.assertFalse(
                challengers.SINGLE_PLAYER.statusOfChallenge
                        (CHALLENGE.GET_CHALLENGES));
    }

    @Test
    public void inMultUserModeAddingAnXChallengerHeaderWillTouchTheChallenger() throws NoSuchFieldException, IllegalAccessException {

        Challengers challengers = new Challengers();
        final ChallengerAuthData challenger = challengers.createNewChallenger();
        long initialTouchTime = challenger.getLastAccessed();

        // risk that this test is intermittent if it all happens in the same millisecond
        // if so could 'wait' in the test, or 'hack the object to have a different last accessed time'
        // set private variable that test will 'touch'
//        Field lastAccessedField = ChallengerAuthData.class.getDeclaredField("lastAccessed");
//        lastAccessedField.setAccessible(true);
//        lastAccessedField.set(challenger, 0L);
//        Assertions.assertEquals(0, challenger.getLastAccessed());

        challengers.setMultiPlayerMode();

        long preTouchTime = System.currentTimeMillis();

        final ChallengerSparkHTTPRequestHook hook = new ChallengerSparkHTTPRequestHook(challengers);

        Request request = mock(Request.class);
        Response response = mock(Response.class);
        when(request.headers("X-CHALLENGER")).thenReturn(challenger.getXChallenger());
        when(request.requestMethod()).thenReturn("GET");
        when(request.pathInfo()).thenReturn("/challenges");
        hook.run(request, response);

        // nothing changes - assert on this
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

        final ChallengerSparkHTTPRequestHook hook = new ChallengerSparkHTTPRequestHook(challengers);

        Request request = mock(Request.class);
        Response response = mock(Response.class);
        when(request.headers("X-CHALLENGER")).thenReturn("bobobobobobobobobobo");
        when(request.requestMethod()).thenReturn("GET");
        when(request.pathInfo()).thenReturn("/challenges");
        hook.run(request, response);

        // nothing changes - assert on this
        // we passed the GET /challenges for default user
        Assertions.assertFalse(
                challengers.SINGLE_PLAYER.statusOfChallenge
                        (CHALLENGE.GET_CHALLENGES));
    }

    @Test
    public void inSinglePlayerModeNoHeaderMeansUseDefaultChallenger(){

        Challengers challengers = new Challengers();

        final ChallengerSparkHTTPRequestHook hook = new ChallengerSparkHTTPRequestHook(challengers);

        Request request = mock(Request.class);
        Response response = mock(Response.class);
        when(request.headers("X-CHALLENGER")).thenReturn(null);
        when(request.requestMethod()).thenReturn("GET");
        when(request.pathInfo()).thenReturn("/challenges");

        hook.run(request, response);

        // we passed the GET /challenges for default user
        Assertions.assertTrue(
                challengers.SINGLE_PLAYER.statusOfChallenge
                        (CHALLENGE.GET_CHALLENGES));
    }

//    @Test
//    public void inMultiUserModeCanGetHeartbeat(){
//
//        Challengers challengers = new Challengers();
//        challengers.setMultiPlayerMode();
//        final ChallengerAuthData challenger =
//                challengers.createNewChallenger();
//
//        final ChallengerSparkHTTPRequestHook hook = new
//                ChallengerSparkHTTPRequestHook(challengers);
//
//        Request request = mock(Request.class);
//        Response response = mock(Response.class);
//        when(request.headers("X-CHALLENGER")).
//                thenReturn(challenger.getXChallenger());
//        when(request.requestMethod()).thenReturn("GET");
//        when(request.pathInfo()).thenReturn("/heartbeat");
//        hook.run(request, response);
//
//        // we passed the GET /challenges for default user
//        Assertions.assertTrue(
//                challenger.statusOfChallenge
//                        (CHALLENGE.GET_HEARTBEAT_204));
//    }

}
