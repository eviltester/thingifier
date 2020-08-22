package uk.co.compendiumdev.challenger.restassured.challenges;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengeMain;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
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

        headers = new HashMap<>();
    }

    // After all - check that all challenges are complete

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
    public void canGetSpecificTodoPass() {

        final Thing todos = ChallengeMain.getChallenger().getThingifier().getThingNamed("todo");

        String todoGuid="";
        for(ThingInstance todo : todos.getInstances()){
            todoGuid = todo.getGUID();
            break;
        };

        final HttpResponseDetails response =
                http.send("/todos/" + todoGuid, "GET", x_challenger_header, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_TODO));
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
}



