package uk.co.compendiumdev.challenger.restassured.challenges;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengeMain;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenger.payloads.Challenge;
import uk.co.compendiumdev.challenger.restassured.http.HttpMessageSender;
import uk.co.compendiumdev.challenger.restassured.http.HttpResponseDetails;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

import java.util.HashMap;
import java.util.Map;

public class ChallengeCompleteTest{

    private static Challengers challengers;
    private static ChallengerAuthData challenger;
    private static HttpMessageSender http;
    private static Map<String, String> x_challenger_header;
    private static Map<String, String> content_application_json;
    private static Map<String, String> headers;
    private static Map<String, String> accept_xml_header;
    private static Map<String, String> accept_json_header;

    @BeforeAll
    public static void createAChallengerToUse(){
        Environment.getBaseUri();
        challengers = ChallengeMain.getChallenger().getChallengers();
        challenger = challengers.createNewChallenger();

        http = new HttpMessageSender(Environment.getBaseUri());
        x_challenger_header = new HashMap<>();
        x_challenger_header.put("X-CHALLENGER", challenger.getXChallenger());

        content_application_json = new HashMap<>();
        content_application_json.put("Content-Type", "application/json");

        accept_xml_header = new HashMap<>();
        accept_xml_header.put("Accept", "application/xml");

        accept_json_header = new HashMap<>();
        accept_json_header.put("Accept", "application/json");

        headers = new HashMap<>();
    }

    // After all - check that all challenges are complete
    @AfterAll
    static void alldone(){

        int remainingChallengeCount = 0;

        for(CHALLENGE challenge : CHALLENGE.values()){
            if(!challenger.statusOfChallenge(challenge)){
                remainingChallengeCount++;
                System.out.println(challenge.toString());
            }
        }

        System.out.print(
            String.format(
                "%d challenges left to complete",
                remainingChallengeCount));
    }

    @Test
    public void canGetChallengesPass() {

        final HttpResponseDetails response =
                http.send("/challenges", "GET", x_challenger_header, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_CHALLENGES));

    }

    @Test
    public void canGetTodosPass() {

        final HttpResponseDetails response =
                http.send("/todos", "GET", x_challenger_header, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_TODOS));
    }

    @Test
    public void canGetTodosNoAcceptHeaderPass() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.put("Accept","");

        final HttpResponseDetails response =
                http.send("/todos", "GET", headers, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_JSON_BY_DEFAULT_NO_ACCEPT));
    }

    @Test
    public void canGet404TodoPass() {

        final HttpResponseDetails response =
                http.send("/todo", "GET", x_challenger_header, "");

        Assertions.assertEquals(404, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_TODOS_NOT_PLURAL_404));
    }

    @Test
    public void canHeadTodosPass() {

        final HttpResponseDetails response =
                http.send("/todos", "HEAD", x_challenger_header, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_HEAD_TODOS));
    }

    @Test
    public void canOptionsTodosPass() {

        final HttpResponseDetails response =
                http.send("/todos", "OPTIONS", x_challenger_header, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.OPTIONS_TODOS));
    }

    /**
     * CREATE TODOS
     */
    @Test
    public void canPostTodosPass() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.putAll(content_application_json);

        //{"title":"mytodo","description":"a todo","doneStatus":false}
        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "{\"title\":\"mytodo\",\"description\":\"a todo\",\"doneStatus\":false}");

        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_TODOS));
    }

    @Test
    public void canPostTodosFailValidationPass() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.putAll(content_application_json);

        //{"title":"mytodo","description":"a todo","doneStatus":"bob"}
        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "{\"title\":\"mytodo\",\"description\":\"a todo\",\"doneStatus\":\"bob\"}");

        Assertions.assertEquals(400, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_TODOS_BAD_DONE_STATUS));
    }

    @Test
    public void canPostTodosUpdatePass() {

        final Thing todos = ChallengeMain.getChallenger().getThingifier().getThingNamed("todo");
        final ThingInstance todo = todos.createManagedInstance();

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.putAll(content_application_json);

        final HttpResponseDetails response =
                http.send("/todos/" + todo.getGUID(), "POST", headers,
                        "{\"title\":\"mytodo\",\"description\":\"a todo\"}");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_UPDATE_TODO));
    }

    @Test
    public void canDeleteTodosPass() {

        final Thing todos = ChallengeMain.getChallenger().getThingifier().getThingNamed("todo");
        final ThingInstance todo = todos.createManagedInstance();

        headers.clear();
        headers.putAll(x_challenger_header);

        final HttpResponseDetails response =
                http.send("/todos/" + todo.getGUID(), "DELETE", headers, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.DELETE_A_TODO));
    }

    @Test
    public void canGetSpecificTodoPass() {

        final Thing todos = ChallengeMain.getChallenger().getThingifier().getThingNamed("todo");
        final ThingInstance todo = todos.createManagedInstance();

        final HttpResponseDetails response =
                http.send("/todos/" + todo.getGUID(), "GET", x_challenger_header, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_TODO));
    }

    @Test
    public void canGetTodosXMLPass() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.putAll(accept_xml_header);

        final HttpResponseDetails response =
                http.send("/todos", "GET",
                        headers, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_ACCEPT_XML));
    }

    @Test
    public void canGetTodoJsonPass() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.putAll(accept_json_header);

        final HttpResponseDetails response =
                http.send("/todos" , "GET",
                        headers, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_ACCEPT_JSON));
    }

    @Test
    public void canGetTodoAnyPass() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.put("Accept","*/*");

        final HttpResponseDetails response =
                http.send("/todos" , "GET",
                        headers, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_ACCEPT_ANY_DEFAULT_JSON));
    }

    @Test
    public void canGetTodoXmlPreferredPass() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.put("Accept","application/xml, application/json");

        final HttpResponseDetails response =
                http.send("/todos" , "GET", headers, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_ACCEPT_XML_PREFERRED));

    }

    @Test
    public void canGetTodosNotAcceptGzip() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.put("Accept","application/gzip");

        final HttpResponseDetails response =
                http.send("/todos" , "GET", headers, "");

        Assertions.assertEquals(406, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_UNSUPPORTED_ACCEPT_406));
    }

    @Test
    public void todo404pass() {

        final HttpResponseDetails response =
                http.send("/todos/guiddoesnotexist", "GET", x_challenger_header, "");

        Assertions.assertEquals(404, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_TODO_404));
    }

    @Test
    public void canFilterTodoPass() {

        final Thing todos = ChallengeMain.getChallenger().getThingifier().getThingNamed("todo");

        todos.createManagedInstance().setValue("doneStatus", "true");
        todos.createManagedInstance().setValue("doneStatus", "false");

        final HttpResponseDetails response =
                http.send("/todos?doneStatus=true", "GET", x_challenger_header, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_TODOS_FILTERED));
    }


    /**
     * Heartbeat
     */

    @Test
    public void can405DeleteHeartbeatPass() {

        final HttpResponseDetails response =
                http.send("/heartbeat", "DELETE", x_challenger_header, "");

        Assertions.assertEquals(405, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.DELETE_HEARTBEAT_405));
    }

    @Test
    public void can500PatchHeartbeatPass() {

        final HttpResponseDetails response =
                http.send("/heartbeat", "PATCH", x_challenger_header, "");

        Assertions.assertEquals(500, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.PATCH_HEARTBEAT_500));
    }

    @Test
    public void can501TraceHeartbeatPass() {

        final HttpResponseDetails response =
                http.send("/heartbeat", "TRACE", x_challenger_header, "");

        Assertions.assertEquals(501, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.TRACE_HEARTBEAT_501));
    }


}



