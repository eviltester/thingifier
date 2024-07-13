package uk.co.compendiumdev.challenger.restassured._11_accept_challenges;


import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todos;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C029GetTodosJsonNoAccept200Test extends RestAssuredBaseTest {


    @Test
    void canGetTodosAsJSONWhenNoAcceptHeaderSent(){

        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("").  // best I can do with RestAssured, setting header to "" - includes header but with no value
                        get(apiPath("/todos")).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                extract().response();

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /todos (200) no accept").status);

        // should be able to parse with GSON if JSON response
        new Gson().fromJson(response.body().asString(), Todos.class);
    }

}
