package uk.co.compendiumdev.challenger.restassured;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.ErrorMessages;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CanCreateTodosWithPOSTTest extends RestAssuredBaseTest {

    @Test
    void canCreateATodoWithPost(){

        Todo createMe = new Todo();
        createMe.title = "my name " + System.currentTimeMillis();
        createMe.description = "my description " + System.currentTimeMillis();
        createMe.doneStatus = true;

        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(createMe).
                post(apiPath("/todos")).
                then().
                statusCode(201).
                contentType(ContentType.JSON).
                extract().response();

        Todo createdTodo = response.body().as(Todo.class);

        Assertions.assertNotSame(createMe, createdTodo);
        Assertions.assertEquals(createMe.title, createdTodo.title);
        Assertions.assertEquals(createMe.description, createdTodo.description);
        Assertions.assertEquals(createMe.doneStatus, createdTodo.doneStatus);

        // not much I can check on the id
        Assertions.assertNotNull(createdTodo.id);
        Assertions.assertTrue(createdTodo.id>0);

        // GET on Location header should return the to do location but just check the format
        Assertions.assertEquals(
                "todos/" + createdTodo.id,
                    response.header("Location"));

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (201)").status);

    }

    @Test
    void can400NotCreateATodoWithInvalidDoneStatusPost(){

        Todo createMe = new Todo();
        createMe.title = "my name " + System.currentTimeMillis();
        createMe.description = "my description " + System.currentTimeMillis();

        // cannot create an invalid status with an invalid boolean value so...
        // createMe.doneStatus = true;
        // work with the JSON to create a 'bad' payload

        final JsonElement createMeJson = new Gson().toJsonTree(createMe);
        createMeJson.getAsJsonObject().
                addProperty("doneStatus", "truthy");


        RestAssured.
            given().
                header("X-CHALLENGER", xChallenger).
            accept("application/json").
            contentType("application/json").
            body(createMeJson.toString()).
            post(apiPath( "/todos")).
            then().
            statusCode(400).
            contentType(ContentType.JSON);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (400) doneStatus").status);

    }

    @Test
    void can400NotCreateATodoWithTitleTooLong(){

        Todo createMe = new Todo();
        // max length on title is 50
        createMe.title = "*3*5*7*9*12*15*18*21*24*27*30*33*36*39*42*45*48*51*";
        createMe.description = "my description " + System.currentTimeMillis();

        final JsonElement createMeJson = new Gson().toJsonTree(createMe);

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(createMeJson.toString()).
                post(apiPath( "/todos")).
                then().
                statusCode(400).
                contentType(ContentType.JSON);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (400) title too long").status);

    }

    @Test
    void can400NotCreateATodoWithAnExtraField(){

        Todo createMe = new Todo();
        // max length on title is 50
        createMe.title = "my title";
        createMe.description = "my description " + System.currentTimeMillis();

        final JsonElement createMeJson = new Gson().toJsonTree(createMe);
        createMeJson.getAsJsonObject().
                addProperty("extrafield", "cannot add");

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(createMeJson.toString()).
                post(apiPath( "/todos")).
                then().
                statusCode(400).
                contentType(ContentType.JSON);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (400) extra").status);

    }

    @Test
    void can400NotCreateATodoBecausePayloadIsTooLarge(){

        Todo createMe = new Todo();
        createMe.title = "my title";
        createMe.description = "my description " + System.currentTimeMillis();

        // add an extra field which makes payload too large
        final JsonElement createMeJson = new Gson().toJsonTree(createMe);
        createMeJson.getAsJsonObject().
                addProperty("blowoutpayload", stringOfLength(5000));

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(createMeJson.toString()).
                post(apiPath( "/todos")).
                then().
                statusCode(413).
                contentType(ContentType.JSON);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (413) content too long").status);

    }

    private String stringOfLength(int length) {
        StringBuilder str = new StringBuilder();
        for (int currLen = 0; currLen < length; currLen++) {
            str.append('a');
        }
        return str.toString();
    }

    @Test
    void can400NotCreateATodoWithDescriptionTooLing(){

        Todo createMe = new Todo();
        createMe.title = "just right";
        // max length on title is 200
        createMe.description = "*3*5*7*10*13*16*19*22*25*28*31*34*37*40*43*46*49*" +
                "52*55*58*61*64*67*70*73*76*79*82*85*88*91*94*97*101*105*109*113*" +
                "117*121*125*129*133*137*141*145*149*153*157*" +
                "161*165*169*173*177*181*185*189*193*197*201*";

        final JsonElement createMeJson = new Gson().toJsonTree(createMe);

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(createMeJson.toString()).
                post(apiPath( "/todos")).
                then().
                statusCode(400).
                contentType(ContentType.JSON);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (400) description too long").status);
    }

    @Test
    void can415CreateATodoWithInvalidContentType(){

        Todo todo = new Todo();
        todo.doneStatus = true;
        todo.title = "invalid content type";
        todo.description = "invalid content";

        String payload = new Gson().toJson(todo);

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType(ContentType.BINARY).
                body(payload.getBytes()).
                post(apiPath( "/todos")).
                then().
                statusCode(415).
                contentType(ContentType.JSON);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (415)").status);

    }

    @Test
    void canCreateATodoWithMaxTitleAndDescriptionLengths(){

        Todo todo = new Todo();
        todo.doneStatus = true;
        todo.title = "2*4*6*8*11*14*17*20*23*26*29*32*35*38*41*44*47*50*";
        todo.description =
                "*3*5*7*9*12*15*18*21*24*27*30*33*36*39*42*45*48*51*" +
                "54*57*60*63*66*69*72*75*78*81*84*87*90*93*96*100*" +
                "104*108*112*116*120*124*128*132*136*140*144*148*" +
                "152*156*160*164*168*172*176*180*184*188*192*196*200*";

        String payload = new Gson().toJson(todo);

        Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(payload.getBytes()).
                post(apiPath("/todos")).
                then().
                statusCode(201).
                contentType(ContentType.JSON).
                and().extract().response();

        Todo createdTodo = response.body().as(Todo.class);

        Assertions.assertEquals(todo.title, createdTodo.title);
        Assertions.assertEquals(todo.description, createdTodo.description);

        Assertions.assertEquals(50, createdTodo.title.length());
        Assertions.assertEquals(200, createdTodo.description.length());

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (201) max out content").status);

    }
    @Test
    void canCreateATodoWithXMLPost(){

        Todo createMe = new Todo();
        createMe.title = "my name " + System.currentTimeMillis();
        createMe.description = "my description " + System.currentTimeMillis();
        createMe.doneStatus = true;

        // if not using RestAssured to convert objects then create String payloads
//        String xml = String.format(
//                "<todo><title>%s</title><description>%s</description>" +
//                        "<doneStatus>%b</doneStatus></todo>",
//                createMe.title, createMe.description, createMe.doneStatus);

        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/xml").
                contentType("application/xml").
                body(createMe).
                post(apiPath("/todos")).
                then().
                statusCode(201).
                contentType(ContentType.XML).
                extract().response();

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos XML").status);


        // GET on Location header to return the to do and check values
        String locationHeader = response.getHeader("Location");
        String pattern = "todos/(.*)";
        Pattern getId = Pattern.compile(pattern);
        Matcher matcher = getId.matcher(locationHeader);
        matcher.find();

        String id = matcher.group(1);
        final TodosApi api = new TodosApi();
        final Todo created = api.getTodo(id);

        Assertions.assertEquals(createMe.title, created.title);
        Assertions.assertEquals(createMe.description, created.description);
        Assertions.assertEquals(createMe.doneStatus, created.doneStatus);

    }

    @Test
    void canCreateATodoWithJSONPost(){

        Todo createMe = new Todo();
        createMe.title = "my name " + System.currentTimeMillis();
        createMe.description = "my description " + System.currentTimeMillis();
        createMe.doneStatus = true;

        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(createMe).
                post(apiPath("/todos")).
                then().
                statusCode(201).
                contentType(ContentType.JSON).
                extract().response();

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos JSON").status);


        // GET on Location header to return the to do and check values
        String locationHeader = response.getHeader("Location");
        String pattern = "todos/(.*)";
        Pattern getId = Pattern.compile(pattern);
        Matcher matcher = getId.matcher(locationHeader);
        matcher.find();

        String id = matcher.group(1);
        final TodosApi api = new TodosApi();
        final Todo created = api.getTodo(id);

        Assertions.assertEquals(createMe.title, created.title);
        Assertions.assertEquals(createMe.description, created.description);
        Assertions.assertEquals(createMe.doneStatus, created.doneStatus);

    }

    @Test
    void canCreateATodoWithJSONAcceptingXMLPost(){

        Todo createMe = new Todo();
        createMe.title = "json name " + System.currentTimeMillis();
        createMe.description = "json description " + System.currentTimeMillis();
        createMe.doneStatus = true;

        final Todo todo = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/xml").
                contentType("application/json").
                body(createMe).
                post(apiPath("/todos")).
                then().
                statusCode(201).
                contentType(ContentType.XML).
                extract().response().as(Todo.class);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos JSON to XML").status);

        Assertions.assertEquals(createMe.title, todo.title);
        Assertions.assertEquals(createMe.description, todo.description);
        Assertions.assertEquals(createMe.doneStatus, todo.doneStatus);

    }

    @Test
    void canCreateATodoWithXMLAcceptingJsonPost(){

        Todo createMe = new Todo();
        createMe.title = "xml name " + System.currentTimeMillis();
        createMe.description = "xml description " + System.currentTimeMillis();
        createMe.doneStatus = true;

        final Todo todo = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/xml").
                body(createMe).
                post(apiPath("/todos")).
                then().
                statusCode(201).
                contentType(ContentType.JSON).
                extract().response().as(Todo.class);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos XML to JSON").status);

        Assertions.assertEquals(createMe.title, todo.title);
        Assertions.assertEquals(createMe.description, todo.description);
        Assertions.assertEquals(createMe.doneStatus, todo.doneStatus);

    }

    @Test
    void canCreateAllTodosAndMaxOutTheLimit(){

        Todo createMe = new Todo();
        createMe.title = "my title";
        createMe.description = "my description";

        int todoNumber=0;
        Response response=null;

        List<Integer> idsToDelete = new ArrayList<>();

        // could get todos then count them and add only the expected number
        // this would also allow us to delete some if there were already the max

        do{
            createMe.title = "my title " + todoNumber;
            response = RestAssured.
                    given().
                    header("X-CHALLENGER", xChallenger).
                    accept("application/json").
                    contentType("application/json").
                    body(createMe).
                    post(apiPath("/todos")).then().
                    extract().response();

            if(response.statusCode()==201) {
                Todo createdTodo = response.as(Todo.class);
                idsToDelete.add(createdTodo.id);
            }

            if(response.statusCode()!=201 && response.statusCode()!=400){
                Assertions.fail("Unexpected status code received during add all todos " + response.statusCode());
            }

        }while(response.statusCode()!=400);

        ErrorMessages messages = response.as(ErrorMessages.class);

        Assertions.assertTrue(messages.errorMessages.contains("ERROR: Cannot add instance, maximum limit of 20 reached"));

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (201) all").status);

        // now delete those todos we created
        idsToDelete.forEach( (id) -> {
                RestAssured.
                        given().
                        header("X-CHALLENGER", xChallenger).
                        accept("application/json").
                        contentType("application/json").
                        delete(apiPath("/todos/" + id)).
                        then().
                        statusCode(200);
        });

    }
}
