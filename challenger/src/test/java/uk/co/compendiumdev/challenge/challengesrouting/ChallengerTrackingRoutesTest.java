package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.challenge.ChallengeMain;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.sparkstart.Environment;

import java.util.*;
import java.util.stream.Stream;

public class ChallengerTrackingRoutesTest {

    private static HttpMessageSender http;
    private static ChallengerAuthData challenger;

    @BeforeAll
    static void createHttp(){
        Environment.stop();

        // this uses the Environment to startup the spark app to
        // issue http tests and test the routing in spark
        // test this in multi user mode
        http = new HttpMessageSender(Environment.getBaseUri(false));
        //challenger = Environment.getNewChallenger();
        challenger = ChallengeMain.getChallenger().getChallengers().createNewChallenger();
    }

    @AfterAll
    static void tearDownEnv(){
        Environment.stop();
    }

    @Test
    void canGetAnExistingChallenger(){
        http.clearHeaders();

        final HttpResponseDetails response =
                http.send("/challenger/" + challenger.getXChallenger(), "get");

        Assertions.assertEquals(200, response.statusCode);
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

        Assertions.assertEquals(200, response.statusCode); // now returns the status details
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
    }

    @Test
    void will404WhenGivenChallengerNotExists(){
        http.clearHeaders();

        final HttpResponseDetails response =
                http.send("/challenger/" + "bob", "get");

        Assertions.assertEquals(404, response.statusCode);
        Assertions.assertEquals(XChallengerHeader.NOT_FOUND_ERROR_MESSAGE,
                response.getHeader("X-CHALLENGER"));
    }

    @Test
    void will404WhenNoGivenChallenger(){
        http.clearHeaders();

        final HttpResponseDetails response =
                http.send("/challenger/", "get");

        Assertions.assertEquals(404, response.statusCode);
        Assertions.assertEquals(XChallengerHeader.NOT_FOUND_ERROR_MESSAGE,
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
        Assertions.assertEquals(XChallengerHeader.NOT_FOUND_ERROR_MESSAGE,
                response.getHeader("X-CHALLENGER"));
    }

    @Test
    void postWithoutChallengerHeaderShouldCreateChallenger(){
        http.clearHeaders();

        final HttpResponseDetails response =
                http.send("/challenger", "post");

        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertNotEquals(XChallengerHeader.NOT_FOUND_ERROR_MESSAGE,
                response.getHeader("X-CHALLENGER"));

        final String guid = response.getHeader("X-CHALLENGER");

        Assertions.assertNotNull(ChallengeMain.getChallenger().
                getChallengers().getChallenger(guid));

    }

    static Stream<Arguments> simpleRoutingStatus(){
        List<Arguments> args = new ArrayList<>();

        args.add(Arguments.of(405, "head", "/challenger"));
        args.add(Arguments.of(204, "options", "/challenger"));
        args.add(Arguments.of(405, "put", "/challenger"));
        args.add(Arguments.of(405, "delete", "/challenger"));
        args.add(Arguments.of(405, "patch", "/challenger"));
        args.add(Arguments.of(405, "trace", "/challenger"));
        return args.stream();
    }

    @ParameterizedTest(name = "simple status routing expected {0} for {1} {2} with challenger as header")
    @MethodSource("simpleRoutingStatus")
    void simpleRoutingTestChallengerRouting(int statusCode, String verb, String url){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());

        final HttpResponseDetails response =
                http.send(url, verb);

        Assertions.assertEquals(statusCode, response.statusCode);
    }

    static Stream<Arguments> simpleRoutingStatusSpecific(){
        List<Arguments> args = new ArrayList<>();

        args.add(Arguments.of(200, "head", "/challenger"));
        args.add(Arguments.of(204, "options", "/challenger"));
        args.add(Arguments.of(400, "put", "/challenger")); // because we don't pass in the details
        args.add(Arguments.of(405, "post", "/challenger"));
        args.add(Arguments.of(405, "delete", "/challenger"));
        args.add(Arguments.of(405, "patch", "/challenger"));
        args.add(Arguments.of(405, "trace", "/challenger"));
        return args.stream();
    }

    @ParameterizedTest(name = "simple status routing expected for specific challenger {0} for {1} {2}/id")
    @MethodSource("simpleRoutingStatusSpecific")
    void simpleRoutingTestSpecificChallengerRouting(int statusCode, String verb, String url){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());

        final HttpResponseDetails response =
                http.send(url + "/" + challenger.getXChallenger(), verb);

        Assertions.assertEquals(statusCode, response.statusCode);
    }


    // todo: allow configuration of the ip address limiting to enable this test
    @Disabled("ip address limiting it not enabled")
    @Test
    void canOnlyCreateACertainNumberOfChallengersPerIp(){
        http.clearHeaders();

        // invalidate any existing challengers by making them out of date
        Challengers challengers = ChallengeMain.getChallenger().getChallengers();
        Set<String> challengersGuids = challengers.getChallengerGuids();
        for(String aGuid : challengersGuids){
            ChallengerAuthData challenger = challengers.getChallenger(aGuid);
            challenger.setAsExpired();
        }

        ChallengerAuthData challengerToResend = ChallengeMain.getChallenger().getChallengers().createNewChallenger();
        challengerToResend.touch();


        int challengersToCreate = 110; // 9 of these will be rejected
        HttpResponseDetails response=null;

        Map<String,String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");

        while(challengersToCreate>0) {
            challengerToResend.setXChallengerGUID(UUID.randomUUID().toString());
            response = http.send("/challenger/" + challengerToResend.getXChallenger(), "put",  headers, challengerToResend.asJson());
            challengersToCreate--;
        }

        Assertions.assertEquals(429, response.statusCode);
        Assertions.assertEquals(101, ChallengeMain.getChallenger().getChallengers().getChallengerGuids().size());
    }
}
