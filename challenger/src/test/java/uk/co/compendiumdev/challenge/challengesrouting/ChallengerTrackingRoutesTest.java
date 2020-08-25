package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.challenge.ChallengeMain;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenger.http.http.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.http.HttpResponseDetails;
import uk.co.compendiumdev.sparkstart.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ChallengerTrackingRoutesTest {

    private static HttpMessageSender http;
    private static ChallengerAuthData challenger;

    @BeforeAll
    static void createHttp(){
        // this uses the Environment to startup the spark app to
        // issue http tests and test the routing in spark
        http = new HttpMessageSender(Environment.getBaseUri());
        challenger = ChallengeMain.getChallenger().getChallengers().createNewChallenger();
    }

    @Test
    void canGetAnExistingChallenger(){
        http.clearHeaders();

        final HttpResponseDetails response =
                http.send("/challenger/" + challenger.getXChallenger(), "get");

        Assertions.assertEquals(204, response.statusCode);
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
    }

    @Test
    void challengerHeaderShouldBeIgnored(){
        http.clearHeaders();
        final ChallengerAuthData newchallenger = ChallengeMain.getChallenger().getChallengers().createNewChallenger();
        http.setHeader("X-CHALLENGER", newchallenger.getXChallenger());

        final HttpResponseDetails response =
                http.send("/challenger/" + challenger.getXChallenger(), "get");

        Assertions.assertEquals(204, response.statusCode);
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
    }

    @Test
    void will404WhenGivenChallengerNotExists(){
        http.clearHeaders();

        final HttpResponseDetails response =
                http.send("/challenger/" + "bob", "get");

        Assertions.assertEquals(404, response.statusCode);
        Assertions.assertEquals("UNKNOWN CHALLENGER - Challenger not found",
                response.getHeader("X-CHALLENGER"));
    }

    @Test
    void will404WhenNoGivenChallenger(){
        http.clearHeaders();

        final HttpResponseDetails response =
                http.send("/challenger/", "get");

        Assertions.assertEquals(404, response.statusCode);
        Assertions.assertEquals("UNKNOWN CHALLENGER - Challenger not found",
                response.getHeader("X-CHALLENGER"));
    }

    @Test
    void postWithChallengerHeaderShouldReturnExistingChallengerIfAvail(){
        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());

        final HttpResponseDetails response =
                http.send("/challenger", "post");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
    }

    @Test
    void postWithChallengerHeaderShouldReturn404IfNotAvail(){
        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger() + "bob");

        final HttpResponseDetails response =
                http.send("/challenger", "post");

        Assertions.assertEquals(404, response.statusCode);
        Assertions.assertEquals("UNKNOWN CHALLENGER - Challenger not found",
                response.getHeader("X-CHALLENGER"));
    }

    @Test
    void postWithoutChallengerHeaderShouldCreateChallenger(){
        http.clearHeaders();

        final HttpResponseDetails response =
                http.send("/challenger", "post");

        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertNotEquals("UNKNOWN CHALLENGER - Challenger not found",
                response.getHeader("X-CHALLENGER"));

        final String guid = response.getHeader("X-CHALLENGER");

        Assertions.assertNotNull(ChallengeMain.getChallenger().
                getChallengers().getChallenger(guid));

    }

    static Stream simpleRoutingStatus(){
        List<Arguments> args = new ArrayList<>();

        args.add(Arguments.of(405, "head", "/challenger"));
        args.add(Arguments.of(405, "options", "/challenger"));
        args.add(Arguments.of(405, "put", "/challenger"));
        args.add(Arguments.of(405, "delete", "/challenger"));
        args.add(Arguments.of(405, "patch", "/challenger"));
        args.add(Arguments.of(405, "trace", "/challenger"));
        return args.stream();
    }

    @ParameterizedTest(name = "simple status routing expected {0} for {1} {2}")
    @MethodSource("simpleRoutingStatus")
    void simpleRoutingTestChallengerRouting(int statusCode, String verb, String url){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());

        final HttpResponseDetails response =
                http.send(url, verb);

        Assertions.assertEquals(statusCode, response.statusCode);
    }

    static Stream simpleRoutingStatusSpecific(){
        List<Arguments> args = new ArrayList<>();

        args.add(Arguments.of(405, "head", "/challenger"));
        args.add(Arguments.of(405, "options", "/challenger"));
        args.add(Arguments.of(405, "put", "/challenger"));
        args.add(Arguments.of(405, "post", "/challenger"));
        args.add(Arguments.of(405, "delete", "/challenger"));
        args.add(Arguments.of(405, "patch", "/challenger"));
        args.add(Arguments.of(405, "trace", "/challenger"));
        return args.stream();
    }

    @ParameterizedTest(name = "simple status routing expected for specific challenger {0} for {1} {2}")
    @MethodSource("simpleRoutingStatusSpecific")
    void simpleRoutingTestSpecificChallengerRouting(int statusCode, String verb, String url){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());

        final HttpResponseDetails response =
                http.send(url + "/" + challenger.getXChallenger(), verb);

        Assertions.assertEquals(statusCode, response.statusCode);
    }
}
