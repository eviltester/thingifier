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

public class ChallengesRoutesTest {

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
    void canChallengesForAnExistingChallenger(){
        http.clearHeaders();

        http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        final HttpResponseDetails response =
                http.send("/challenges", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));

        Assertions.assertEquals(
                    "/gui/challenges/" +
                        challenger.getXChallenger(),
                response.getHeader("Location"));

        Assertions.assertNotNull(response.body);
        Assertions.assertTrue(response.body.length()>200);

    }

    @Test
    void canOptionsChallenges(){
        http.clearHeaders();

        //http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        final HttpResponseDetails response =
                http.send("/challenges", "options");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("GET, HEAD, OPTIONS",
                response.getHeader("Allow"));

        Assertions.assertEquals("application/json",
                response.getHeader("Content-Type"));
    }


    static Stream simpleRoutingStatus(){
        List<Arguments> args = new ArrayList<>();

        args.add(Arguments.of(200, "head", "/challenges"));
        args.add(Arguments.of(200, "options", "/challenges"));
        args.add(Arguments.of(405, "put", "/challenges"));
        args.add(Arguments.of(405, "post", "/challenges"));
        args.add(Arguments.of(405, "delete", "/challenges"));
        args.add(Arguments.of(405, "patch", "/challenges"));
        args.add(Arguments.of(405, "trace", "/challenges"));
        return args.stream();
    }

    @ParameterizedTest(name = "simple status routing expected {0} for {1} {2}")
    @MethodSource("simpleRoutingStatus")
    void simpleRoutingTestChallengesRouting(int statusCode, String verb, String url){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());

        final HttpResponseDetails response =
                http.send(url, verb);

        Assertions.assertEquals(statusCode, response.statusCode);
    }

}
