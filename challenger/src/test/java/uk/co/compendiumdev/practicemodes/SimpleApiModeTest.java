package uk.co.compendiumdev.practicemodes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.sparkstart.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SimpleApiModeTest {

    private static HttpMessageSender http;


    @BeforeAll
    static void createHttp(){
        // this uses the Environment to startup the spark app to
        // issue http tests and test the routing in spark
        http = new HttpMessageSender(Environment.getBaseUri());
    }

    static Stream simpleRoutingStatus(){
        List<Arguments> args = new ArrayList<>();


        args.add(Arguments.of(200, "get", "/simpleapi/items"));
        args.add(Arguments.of(200, "head", "/simpleapi/items"));
        args.add(Arguments.of(204, "options", "/simpleapi/items"));
        args.add(Arguments.of(405, "patch", "/simpleapi/items"));
        args.add(Arguments.of(405, "trace", "/simpleapi/items"));
        args.add(Arguments.of(405, "delete", "/simpleapi/items"));
        args.add(Arguments.of(405, "put", "/simpleapi/items"));
        args.add(Arguments.of(400, "post", "/simpleapi/items"));


        return args.stream();
    }

    @ParameterizedTest(name = "simple status routing expected {0} for {1} {2}")
    @MethodSource("simpleRoutingStatus")
    void simpleRoutingTest(int statusCode, String verb, String url){
        final HttpResponseDetails response =
                http.send(url, verb);

        Assertions.assertEquals(statusCode, response.statusCode);
    }

}
