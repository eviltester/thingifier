package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;

import java.util.*;
import java.util.stream.Stream;

public class MirrorRoutesTest {
    private static HttpMessageSender http;

    @BeforeAll
    static void createHttp() {
        // this uses the Environment to startup the spark app to
        // issue http tests and test the routing in spark
        http = new HttpMessageSender(Environment.getBaseUri());
    }

    static Stream simpleRoutingStatus() {
        List<Arguments> args = new ArrayList<>();

        // get
        args.add(Arguments.of(200, "get", "/mirror/request"));
        // 200 same as get
        args.add(Arguments.of(200, "head", "/mirror/request"));
        args.add(Arguments.of(204, "options", "/mirror/request"));
        // post
        args.add(Arguments.of(200, "post", "/mirror/request"));
        args.add(Arguments.of(200, "put", "/mirror/request"));
        args.add(Arguments.of(200, "delete", "/mirror/request"));
        args.add(Arguments.of(200, "patch", "/mirror/request"));
        args.add(Arguments.of(200, "trace", "/mirror/request"));

        args.add(Arguments.of(200, "get", "/mirror/request/bob"));
        // 200 same as get
        args.add(Arguments.of(200, "head", "/mirror/request/bob"));
        args.add(Arguments.of(204, "options", "/mirror/request/bob"));
        // post
        args.add(Arguments.of(200, "post", "/mirror/request/bob"));
        args.add(Arguments.of(200, "put", "/mirror/request/bob"));
        args.add(Arguments.of(200, "delete", "/mirror/request/bob"));
        args.add(Arguments.of(200, "patch", "/mirror/request/bob"));
        args.add(Arguments.of(200, "trace", "/mirror/request/bob"));
        // raw routes
        args.add(Arguments.of(200, "get", "/mirror/raw"));
        // 200 same as get
        args.add(Arguments.of(200, "head", "/mirror/raw"));
        args.add(Arguments.of(204, "options", "/mirror/raw"));
        // post
        args.add(Arguments.of(200, "post", "/mirror/raw"));
        args.add(Arguments.of(200, "put", "/mirror/raw"));
        args.add(Arguments.of(200, "delete", "/mirror/raw"));
        args.add(Arguments.of(200, "patch", "/mirror/raw"));
        args.add(Arguments.of(200, "trace", "/mirror/raw"));

        args.add(Arguments.of(200, "get", "/mirror/raw/bob"));
        // 200 same as get
        args.add(Arguments.of(200, "head", "/mirror/raw/bob"));
        args.add(Arguments.of(204, "options", "/mirror/raw/bob"));
        // post
        args.add(Arguments.of(200, "post", "/mirror/raw/bob"));
        args.add(Arguments.of(200, "put", "/mirror/raw/bob"));
        args.add(Arguments.of(200, "delete", "/mirror/raw/bob"));
        args.add(Arguments.of(200, "patch", "/mirror/raw/bob"));
        args.add(Arguments.of(200, "trace", "/mirror/raw/bob"));


        return args.stream();
    }

    @ParameterizedTest(name = "simple status routing expected {0} for {1} {2}")
    @MethodSource("simpleRoutingStatus")
    void simpleRoutingTest(int statusCode, String verb, String url) {
        final HttpResponseDetails response =
                http.send(url, verb);

        Assertions.assertEquals(statusCode, response.statusCode);
    }


    @Test
    void canGetJSONFormattedReflection() {

        http.clearHeaders();
        http.setHeader("Accept", "application/json");

        final HttpResponseDetails response = http.send("/mirror/request", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("application/json", response.getHeader("Content-Type"));
        Assertions.assertTrue(response.body.startsWith("{"));
    }

    @Test
    void canGetXMLFormattedReflection() {

        http.clearHeaders();
        http.setHeader("Accept", "application/xml");

        final HttpResponseDetails response = http.send("/mirror/request", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("application/xml", response.getHeader("Content-Type"));
        Assertions.assertTrue(response.body.startsWith("<message"));
    }

    @Test
    void canGetPlainTextFormattedReflection() {

        http.clearHeaders();
        http.setHeader("Accept", "text/plain");

        final HttpResponseDetails response = http.send("/mirror/request", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("text/plain", response.getHeader("Content-Type"));
        Assertions.assertTrue(response.body.startsWith("GET "));
    }

    @Test
    void rawIsAlwaysATextResponse() {

        http.clearHeaders();
        http.setHeader("Accept", "application/json");

        final HttpResponseDetails response = http.send("/mirror/raw", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("text/plain", response.getHeader("Content-Type"));
        Assertions.assertTrue(response.body.startsWith("GET "));
    }


    @Test
    void requestContentLengthIsCheckedForLength() {
        http.clearHeaders();

        HttpHeadersBlock headers = new HttpHeadersBlock();
        headers.put("Accept", "text/plain");

        final HttpResponseDetails response = http.send("/mirror/request", "post", headers.asMap(),
                                                stringOfLength(24000 + 1));

        Assertions.assertEquals(413, response.statusCode);
        Assertions.assertEquals("application/json", response.getHeader("Content-Type"));
        Assertions.assertTrue(response.body.contains("Error: Request too large, max allowed is 24000 bytes"));
    }

    private String stringOfLength(int length) {
        StringBuilder str = new StringBuilder();
        for (int currLen = 0; currLen < length; currLen++) {
            str.append('a');
        }
        return str.toString();
    }

}
