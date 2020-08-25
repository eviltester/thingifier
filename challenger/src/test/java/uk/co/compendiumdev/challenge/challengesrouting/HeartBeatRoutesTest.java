package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.challenger.http.http.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.http.HttpResponseDetails;
import uk.co.compendiumdev.sparkstart.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class HeartBeatRoutesTest {
    private static HttpMessageSender http;

    @BeforeAll
    static void createHttp(){
        // this uses the Environment to startup the spark app to
        // issue http tests and test the routing in spark
        http = new HttpMessageSender(Environment.getBaseUri());
    }

    @Test
    public void optionsOnHeartbeat() {

        final HttpResponseDetails response =
                http.send("/heartbeat", "OPTIONS");

        Assertions.assertEquals(204, response.statusCode);
        Assertions.assertEquals("GET, HEAD, OPTIONS",
                response.getHeader("Allow"));
    }

    static Stream simpleRoutingStatus(){
        List<Arguments> args = new ArrayList<>();

        args.add(Arguments.of(204, "get"));
        args.add(Arguments.of(204, "head"));
        args.add(Arguments.of(405, "post"));
        args.add(Arguments.of(405, "delete"));
        args.add(Arguments.of(405, "put"));
        args.add(Arguments.of(500, "patch"));
        args.add(Arguments.of(501, "trace"));
        return args.stream();
    }

    @ParameterizedTest(name = "simple status routing expected {0} for {1}")
    @MethodSource("simpleRoutingStatus")
    void simpleRoutingTest(int statusCode, String verb){
        final HttpResponseDetails response =
                http.send("/heartbeat", verb);

        Assertions.assertEquals(statusCode, response.statusCode);
    }
}
