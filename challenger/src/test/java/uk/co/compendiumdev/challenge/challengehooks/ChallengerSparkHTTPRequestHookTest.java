package uk.co.compendiumdev.challenge.challengehooks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import spark.Request;
import spark.Response;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.challengers.Challengers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChallengerSparkHTTPRequestHookTest {

    @Test
    public void canTriggerAHookTest(){

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
    public void inSinglePlayerModeNoHeaderMeansDefaultChallenger(){

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
}
