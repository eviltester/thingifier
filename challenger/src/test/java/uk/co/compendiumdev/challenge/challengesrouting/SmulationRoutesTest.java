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

public class SmulationRoutesTest {
    private static HttpMessageSender http;


    @BeforeAll
    static void createHttp(){
        // this uses the Environment to startup the spark app to
        // issue http tests and test the routing in spark
        http = new HttpMessageSender(Environment.getBaseUri());
    }

    static Stream simpleRoutingStatus(){
        List<Arguments> args = new ArrayList<>();


        args.add(Arguments.of(200, "get", "/sim/entities"));
        args.add(Arguments.of(200, "head", "/sim/entities"));
        args.add(Arguments.of(204, "options", "/sim/entities"));
        args.add(Arguments.of(501, "patch", "/sim/entities"));
        args.add(Arguments.of(501, "trace", "/sim/entities"));

        args.add(Arguments.of(200, "get", "/sim/entities/1"));
        args.add(Arguments.of(200, "head", "/sim/entities/1"));
        args.add(Arguments.of(204, "options", "/sim/entities/1"));
        args.add(Arguments.of(501, "patch", "/sim/entities/1"));
        args.add(Arguments.of(501, "trace", "/sim/entities/1"));

        return args.stream();
    }

    @ParameterizedTest(name = "simple status routing expected {0} for {1} {2}")
    @MethodSource("simpleRoutingStatus")
    void simpleRoutingTest(int statusCode, String verb, String url){
        final HttpResponseDetails response =
                http.send(url, verb);

        Assertions.assertEquals(statusCode, response.statusCode);
    }

    @Test
    void canSimulateGetOfEntitiesJSON(){

        http.clearHeaders();
        http.setHeader("Accept", "application/json");

        HttpResponseDetails response = http.get("/sim/entities");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("application/json",response.getHeader("Content-Type"));
        Assertions.assertTrue(response.body.startsWith("{\"entities\":[{"));
        Assertions.assertTrue(response.body.contains("id\":1,"));
        Assertions.assertTrue(response.body.contains("id\":2,"));
        Assertions.assertTrue(response.body.contains("id\":3,"));
        Assertions.assertTrue(response.body.contains("id\":4,"));
        Assertions.assertTrue(response.body.contains("id\":5,"));
        Assertions.assertTrue(response.body.contains("id\":6,"));
        Assertions.assertTrue(response.body.contains("id\":7,"));
        Assertions.assertTrue(response.body.contains("id\":8,"));
        Assertions.assertTrue(response.body.contains("id\":9,"));
        Assertions.assertTrue(response.body.contains("id\":10,"));

        // 11 is in the thingifier but should not be returned
        Assertions.assertFalse(response.body.contains("id\":11,"));
    }

    @Test
    void canSimulateGetOfEntitiesXML(){

        http.clearHeaders();
        http.setHeader("Accept", "application/xml");

        HttpResponseDetails response = http.get("/sim/entities");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("application/xml",response.getHeader("Content-Type"));
        Assertions.assertTrue(response.body.startsWith("<entities><entity>"));
        Assertions.assertTrue(response.body.contains("<id>1</id>"));
        Assertions.assertTrue(response.body.contains("<id>2</id>"));
        Assertions.assertTrue(response.body.contains("<id>3</id>"));
        Assertions.assertTrue(response.body.contains("<id>4</id>"));
        Assertions.assertTrue(response.body.contains("<id>5</id>"));
        Assertions.assertTrue(response.body.contains("<id>6</id>"));
        Assertions.assertTrue(response.body.contains("<id>7</id>"));
        Assertions.assertTrue(response.body.contains("<id>8</id>"));
        Assertions.assertTrue(response.body.contains("<id>9</id>"));
        Assertions.assertTrue(response.body.contains("<id>10</id>"));

        // 11 is in the thingifier but should not be returned
        Assertions.assertFalse(response.body.contains("<id>11</id>"));
    }

    @Test
    void canSimulateHeadOfEntitiesJSON(){

        http.clearHeaders();
        http.setHeader("Accept", "application/json");

        HttpResponseDetails response = http.head("/sim/entities");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("application/json",response.getHeader("Content-Type"));
        Assertions.assertTrue(response.body.equals(""));

    }

    @Test
    void canSimulateHeadOfEntitiesXML(){

        http.clearHeaders();
        http.setHeader("Accept", "application/xml");

        HttpResponseDetails response = http.head("/sim/entities");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("application/xml",response.getHeader("Content-Type"));
        Assertions.assertTrue(response.body.equals(""));
    }


    @Test
    void canSimulateCreateAndGetOfEntity11(){

        http.clearHeaders();
        http.setHeader("Accept", "application/json");

        HttpResponseDetails response = http.post("/sim/entities", "{\"name\":\"bob\"}");

        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertEquals("application/json",response.getHeader("Content-Type"));
        Assertions.assertEquals("entities/11", response.getHeader("Location"));
        String entity11 = "{\"id\":11,\"name\":\"bob\",\"description\":\"\"}";
        Assertions.assertEquals(entity11, response.body);

        response = http.get("/sim/entities/11");
        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals(entity11, response.body);
    }


    @Test
    void canSimulateDeleteOfEntity9() {

        http.clearHeaders();
        http.setHeader("Accept", "application/json");

        HttpResponseDetails response = http.delete("/sim/entities/9");
        Assertions.assertEquals(204, response.statusCode);

        response = http.get("/sim/entities/9");
        Assertions.assertEquals(404, response.statusCode);

    }

    @Test
    void canSimulateGetEntityId(){
        http.clearHeaders();
        http.setHeader("Accept", "application/json");

        // cannot get something not in the thingifier
        HttpResponseDetails response = http.get("/sim/entities/23");
        Assertions.assertEquals(404, response.statusCode);

        // can get something  in the thingifier
        response = http.get("/sim/entities/3");
        Assertions.assertEquals(200, response.statusCode);

        // can get fake TODO: create an amendment test and move this there
        response = http.get("/sim/entities/10");
        Assertions.assertEquals(200, response.statusCode);
        String entity10 = "{\"id\":10,\"name\":\"eris\",\"description\":\"\"}";
        Assertions.assertEquals(entity10, response.body);
    }

    @Test
    void canSimulateHeadEntityId(){
        http.clearHeaders();
        http.setHeader("Accept", "application/json");

        // cannot get something not in the thingifier
        HttpResponseDetails response = http.head("/sim/entities/23");
        Assertions.assertEquals(404, response.statusCode);
        Assertions.assertEquals("", response.body);
        
        // can get something  in the thingifier
        response = http.head("/sim/entities/3");
        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("", response.body);

        // can get fake TODO: create an amendment test and move this there
        response = http.head("/sim/entities/10");
        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("", response.body);
    }

    @Test
    void canNotDeleteOtherEntities() {

        http.clearHeaders();
        http.setHeader("Accept", "application/json");

        HttpResponseDetails response = http.delete("/sim/entities/1");
        Assertions.assertEquals(403, response.statusCode);

        response = http.delete("/sim/entities/8");
        Assertions.assertEquals(403, response.statusCode);
    }

}
