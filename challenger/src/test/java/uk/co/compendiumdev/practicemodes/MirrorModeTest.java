package uk.co.compendiumdev.practicemodes;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MirrorModeTest {

    private static HttpMessageSender http;

    @BeforeAll
    static void createHttp() {
        // this uses the Environment to startup the spark app to
        // issue http tests and test the routing in spark
        http = new HttpMessageSender(Environment.getBaseUri());
    }

    @Test
    public void canGetMirrorRequestDefaults(){

        final HttpResponseDetails response =
                http.send("/mirror/request", "GET", Map.of(), "");

        Assertions.assertEquals(200, response.statusCode);

        // by default the response is application/json
        Assertions.assertEquals("application/json",response.getHeader("content-type"));
        Assertions.assertTrue(response.body.contains("{\"details\":"));
        // check some basic items in the response

        Assertions.assertTrue(response.body.contains("Parsed Query Params"));
        Assertions.assertTrue(response.body.contains("Raw Headers"));
        Assertions.assertTrue(response.body.contains("Body"));
    }

    @Test
    public void canGetMirrorRequestAsText(){

        final HttpResponseDetails response =
                http.send("/mirror/request", "GET", Map.of("Accept", "text/plain"), "");

        Assertions.assertEquals(200, response.statusCode);


        Assertions.assertEquals("text/plain",response.getHeader("content-type"));
        Assertions.assertTrue(response.body.startsWith("GET "));
        Assertions.assertFalse(response.body.contains("{\"details\":"));

        // check some basic items in the response
        Assertions.assertTrue(response.body.contains("Parsed Query Params"));
        Assertions.assertTrue(response.body.contains("Raw Headers"));
        Assertions.assertTrue(response.body.contains("Body"));
    }

    @Test
    public void canGetMirrorRequestAsJson(){

        final HttpResponseDetails response =
                http.send("/mirror/request", "GET", Map.of("Accept", "application/json"), "");

        Assertions.assertEquals(200, response.statusCode);

        Assertions.assertEquals("application/json",response.getHeader("content-type"));
        Assertions.assertTrue(response.body.startsWith("{"));
        Assertions.assertTrue(response.body.contains("{\"details\":"));

        // check some basic items in the response
        Assertions.assertTrue(response.body.contains("Parsed Query Params"));
        Assertions.assertTrue(response.body.contains("Raw Headers"));
        Assertions.assertTrue(response.body.contains("Body"));
    }


    @Test
    public void canGetMirrorRequestAsXml(){

        final HttpResponseDetails response =
                http.send("/mirror/request", "GET", Map.of("Accept", "application/xml"), "");

        Assertions.assertEquals(200, response.statusCode);

        Assertions.assertEquals("application/xml",response.getHeader("content-type"));
        Assertions.assertTrue(response.body.startsWith("<message"));
        Assertions.assertTrue(response.body.contains("<messageDetails>"));
        Assertions.assertTrue(response.body.contains("<details>"));
        Assertions.assertTrue(response.body.contains("</details>"));
        Assertions.assertTrue(response.body.contains("</messageDetails>"));
        Assertions.assertTrue(response.body.contains("&#xa;"));

        // check some basic items in the response
        Assertions.assertTrue(response.body.contains("Parsed Query Params"));
        Assertions.assertTrue(response.body.contains("Raw Headers"));
        Assertions.assertTrue(response.body.contains("Body"));
    }

    static Stream<Arguments> simpleEndPointMirrorRequestRoutingStatus(){
        List<Arguments> args = new ArrayList<>();

        args.add(Arguments.of(200, "get", "/mirror/request"));
        args.add(Arguments.of(200, "head", "/mirror/request"));
        args.add(Arguments.of(204, "options", "/mirror/request"));
        args.add(Arguments.of(200, "put", "/mirror/request"));
        args.add(Arguments.of(200, "post", "/mirror/request"));
        args.add(Arguments.of(200, "delete", "/mirror/request"));
        args.add(Arguments.of(200, "trace", "/mirror/request"));
        return args.stream();
    }



    @ParameterizedTest(name = "can hit mirror mode with multiple verbs {0} for {1} {2}")
    @MethodSource("simpleEndPointMirrorRequestRoutingStatus")
    void simpleMirrorRequestRoutingTest(int statusCode, String verb, String url) {
        final HttpResponseDetails response =
                http.send(url, verb);

        Assertions.assertEquals(statusCode, response.statusCode);
    }


    static Stream<Arguments> simpleEndPointMirrorRawRoutingStatus(){
        List<Arguments> args = new ArrayList<>();

        args.add(Arguments.of(200, "get", "/mirror/raw"));
        args.add(Arguments.of(200, "head", "/mirror/raw"));
        args.add(Arguments.of(204, "options", "/mirror/raw"));
        args.add(Arguments.of(200, "put", "/mirror/raw"));
        args.add(Arguments.of(200, "post", "/mirror/raw"));
        args.add(Arguments.of(200, "delete", "/mirror/raw"));
        args.add(Arguments.of(200, "trace", "/mirror/raw"));
        return args.stream();
    }



    @ParameterizedTest(name = "can hit mirror mode with multiple verbs {0} for {1} {2}")
    @MethodSource("simpleEndPointMirrorRawRoutingStatus")
    void simpleMirrorRawRoutingTest(int statusCode, String verb, String url) {
        final HttpResponseDetails response =
                http.send(url, verb);

        Assertions.assertEquals(statusCode, response.statusCode);
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


    static Stream<Arguments> acceptHeaders(){
        List<Arguments> args = new ArrayList<>();

        args.add(Arguments.of("application/xml"));
        args.add(Arguments.of("application/json"));
        args.add(Arguments.of("text/html"));
        args.add(Arguments.of("text/plain"));
        args.add(Arguments.of("unknown/format"));
        return args.stream();
    }

    @ParameterizedTest(name = "can hit mirror raw with any valid accept and get text like {0}")
    @MethodSource("acceptHeaders")
    public void canOnlyGetMirrorRawAsText(String accept){

        final HttpResponseDetails response =
                http.send("/mirror/raw", "GET", Map.of("Accept", accept), "");

        Assertions.assertEquals(200, response.statusCode);


        Assertions.assertEquals("text/plain",response.getHeader("content-type"));
        Assertions.assertFalse(response.body.contains("{\"details\":"));

        // check some basic items in the response
        Assertions.assertTrue(response.body.contains("Parsed Query Params"));
        Assertions.assertTrue(response.body.contains("Raw Headers"));
        Assertions.assertTrue(response.body.contains("Body"));
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
