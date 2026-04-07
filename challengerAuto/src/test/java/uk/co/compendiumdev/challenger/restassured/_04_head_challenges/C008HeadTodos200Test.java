package uk.co.compendiumdev.challenger.restassured._04_head_challenges;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class C008HeadTodos200Test extends RestAssuredBaseTest {

    @Test
    void canCheckHeadForTodos(){

        final Response headresponse = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                head(apiPath( "/todos")).
                then().
                statusCode(200).
                and().extract().response();

        // pass challenge
        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("HEAD /todos (200)").status);


        Assertions.assertTrue(headresponse.body().asString().equals(""),
                "Expected no Body for Head response");

        final Response todosgetresponse = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                get(apiPath( "/todos")).
                then().
                statusCode(200).
                and().extract().response();

        // compare headers
        List<String> failed = new ArrayList<>();
        for(Header header : todosgetresponse.headers().asList()){
            if(headresponse.headers().hasHeaderWithName(header.getName())){
                if(!headresponse.header(header.getName()).equals(header.getValue())){
                    String failedHeader = String.format("%s head: %s ... vs ... get: %s",
                            header.getName(),
                            headresponse.header(header.getName()), header.getValue());
                    failed.add(failedHeader);
                }
            }
        }

        Assertions.assertEquals(0, failed.size(), String.join("\n", failed));

    }

}