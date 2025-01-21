package uk.co.compendiumdev.simulator;

import io.restassured.RestAssured;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.simulator.payloads.Entities;
import uk.co.compendiumdev.simulator.payloads.EntityPayload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimulatorHttpTest extends RestAssuredBaseTest {

    /*
        Using Raw HTTP requests, check the Simulator endpoints and additional exercises

        Because it is a simulator the tests do not need to be run in step order to return expected values.

     */

    @BeforeAll
    static void logRestAssuredCalls(){
        RestAssured.filters();
    }

    @Test
    void step001_GetAllEntities() {

        Entities body = RestAssured.
                given().
                request(
                        Method.GET,
                        apiPath("/sim/entities")).
                then().
                statusCode(200).and().
                extract().body().as(Entities.class);


        Assertions.assertEquals(10, body.entities.size());

    }

    @Test
    void step002_GetSingleEntity() {

        EntityPayload entity = RestAssured.
                given().
                request(
                        Method.GET,
                        apiPath("/sim/entities/1")).
                then().
                statusCode(200).and().
                extract().body().as(EntityPayload.class);


        Assertions.assertEquals(1, entity.id);
        Assertions.assertEquals("entity number 1", entity.name);
        Assertions.assertEquals("", entity.description);

    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 6, 7, 8})
    void step002_bonus_GetAnotherSingleEntity(int id) {

        EntityPayload entity = RestAssured.
                given().
                request(
                        Method.GET,
                        apiPath("/sim/entities/" + id)).
                then().
                statusCode(200).and().
                extract().body().as(EntityPayload.class);


        Assertions.assertEquals(id, entity.id);
        Assertions.assertEquals("entity number " + id, entity.name);
        Assertions.assertEquals("", entity.description);

    }

    @Test
    void step003_FailToGetAnEntityBecauseIdDoesNotExist() {

        RestAssured.
                given().
                request(
                        Method.GET,
                        apiPath("/sim/entities/13")).
                then().
                statusCode(404);
    }


    @Test
    void step004_PostCreateEntity() {

        EntityPayload entity = RestAssured.
                given().body("{\"name\": \"bob\"}").
                request(
                        Method.POST,
                        apiPath("/sim/entities")).
                then().
                statusCode(201).and().
                header("Location", "/sim/entities/11").and().
                contentType("application/json").
                extract().body().as(EntityPayload.class);


        Assertions.assertEquals(11, entity.id);
        Assertions.assertEquals("bob", entity.name);
        Assertions.assertEquals("", entity.description);
    }

    @Test
    void step004_Note_PostCreateEntityAlwaysReturnsSameResponse() {

        EntityPayload entity = RestAssured.
                given().body("{\"name\": \"george\"}").
                request(
                        Method.POST,
                        apiPath("/sim/entities")).
                then().
                statusCode(201).and().
                header("Location", "/sim/entities/11").and().
                contentType("application/json").
                extract().body().as(EntityPayload.class);


        Assertions.assertEquals(11, entity.id);
        Assertions.assertEquals("bob", entity.name);
        Assertions.assertEquals("", entity.description);
    }

    @Test
    void step005_PostAmendEntity() {

        EntityPayload entity = RestAssured.
                given().body("{\"name\": \"eris\"}").
                request(
                        Method.POST,
                        apiPath("/sim/entities/10")).
                then().
                statusCode(200).and().
                contentType("application/json").
                extract().body().as(EntityPayload.class);


        Assertions.assertEquals(10, entity.id);
        Assertions.assertEquals("eris", entity.name);
        Assertions.assertEquals("", entity.description);
    }

    @Test
    void step006_PutAmendEntity() {

        EntityPayload entity = RestAssured.
                given().body("{\"name\": \"eris\"}").
                request(
                        Method.PUT,
                        apiPath("/sim/entities/10")).
                then().
                statusCode(200).and().
                contentType("application/json").
                extract().body().as(EntityPayload.class);


        Assertions.assertEquals(10, entity.id);
        Assertions.assertEquals("eris", entity.name);
        Assertions.assertEquals("", entity.description);
    }


    @Test
    void step007_DeleteEntity() {

        RestAssured.
                given().
                request(
                        Method.DELETE,
                        apiPath("/sim/entities/9")).
                then().
                statusCode(204);


        /* return a 404 because we just deleted it */

        RestAssured.
                given().
                request(
                        Method.GET,
                        apiPath("/sim/entities/9")).
                then().
                statusCode(404);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 6, 7, 8})
    void step007_bonus_cannotDeleteWithIdLessThan9(int id) {

        RestAssured.
                given().
                request(
                        Method.DELETE,
                        apiPath("/sim/entities/" + id)).
                then().
                statusCode(403); // forbidden 403
    }

    @Test
    void step007_bonus_cannotDeleteAllEntities() {

        RestAssured.
                given().
                request(
                        Method.DELETE,
                        apiPath("/sim/entities")).
                then().
                statusCode(405); // method not allowed 405

        // FAQ: why did I get a 404? see below...
    }

    @Test
    void step007_bonus_warningNoTrailingSlashToDeleteAllEntities() {

        // adding a trailing slash will create a different url i.e.
        // /sim/entities/<null> no id given so it cannot be found
        // resulting in a 404 message

        RestAssured.
                given().
                request(
                        Method.DELETE,
                        apiPath("/sim/entities/")).
                then().
                statusCode(404);
    }

    @Test
    void step007_bonus_failToDeleteAnEntityThatDoesNotExistUsingAnId() {

        RestAssured.
                given().
                request(
                        Method.DELETE,
                        apiPath("/sim/entities/56")).
                then().
                statusCode(404);
    }


    @Test
    void step008_useOptionsToFindOutWhatVerbsAreAllowed() {

        String[] verbs = RestAssured.
                given().
                request(
                        Method.OPTIONS,
                        apiPath("/sim/entities")).
                then().
                statusCode(204).and().
                extract().header("Allow").split(",");

        Assertions.assertEquals(5, verbs.length);

        List<String> verbsList = new ArrayList<>();

        for (String verb : verbs) {
            verbsList.add(verb.trim());
        }

        Assertions.assertTrue(verbsList.contains("GET"));
        Assertions.assertTrue(verbsList.contains("POST"));
        Assertions.assertTrue(verbsList.contains("PUT"));
        Assertions.assertTrue(verbsList.contains("HEAD"));
        Assertions.assertTrue(verbsList.contains("OPTIONS"));
    }

    @Test
    void step008_bonus_patchTriggersServerError() {

        RestAssured.
                given().
                request(
                        Method.PATCH,
                        apiPath("/sim/entities")).
                then().
                statusCode(501);

    }


    @Test
    void step009_headRequestReturnsHeadersFromGet() {

        Headers headHeaders = RestAssured.
                given().
                request(
                        Method.HEAD,
                        apiPath("/sim/entities")).
                then().
                statusCode(200).extract().headers();

        Headers getHeaders = RestAssured.
                given().
                request(
                        Method.GET,
                        apiPath("/sim/entities")).
                then().
                statusCode(200).extract().headers();

        Assertions.assertEquals(headHeaders.size(), getHeaders.size());

        // remove the variable headers that are server based rather than app based
        List<String> headersToSkipComparison = Arrays.asList("Report-To", "Reporting-Endpoints", "Connection", "Date");
        for (Header headHeader : headHeaders) {
            if (!headersToSkipComparison.contains(headHeader.getName())) {
                Assertions.assertEquals(
                    headHeaders.getValue(headHeader.getName()),
                    getHeaders.getValue(headHeader.getName()),
            "failed comparing " + headHeader.getName()
                );
            }
        }
    }
}

