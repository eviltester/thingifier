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

public class C028GetTodosAcceptXmlPref200Test extends RestAssuredBaseTest {

    @Test
    void canGetTodosAsPreferredXML(){

        // ask for multiple but prefer xml
        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/xml, application/json").
                get(apiPath("/todos")).
                then().
                statusCode(200).
                contentType(ContentType.XML).
                extract().response();


        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /todos (200) XML pref").status);

        // XML in response
        Assertions.assertTrue(response.body().asString().contains("<todos>"));
        Assertions.assertTrue(response.body().asString().contains("</todos>"));

    }

}
