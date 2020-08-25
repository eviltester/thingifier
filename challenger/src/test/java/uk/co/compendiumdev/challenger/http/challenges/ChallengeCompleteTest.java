package uk.co.compendiumdev.challenger.http.challenges;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengeMain;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenger.http.http.HttpResponseDetails;
import uk.co.compendiumdev.challenger.http.http.HttpMessageSender;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

import java.util.HashMap;
import java.util.Map;

public class ChallengeCompleteTest{

    private static Challengers challengers;
    private static ChallengerAuthData challenger;
    private static HttpMessageSender http;

    private static Map<String, String> headers;
    private static Map<String, String> x_challenger_header;
    private static Map<String, String> content_application_json;
    private static Map<String, String> accept_xml_header;
    private static Map<String, String> accept_json_header;
    private static Map<String, String> content_application_xml;
    private static ChallengerAuthData newChallenger;

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

        content_application_xml = new HashMap<>();
        content_application_xml.put("Content-Type", "application/xml");

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
            if(newChallenger!=null &&
                    challenge==CHALLENGE.CREATE_NEW_CHALLENGER){
                if(newChallenger.statusOfChallenge(challenge)){
                    continue;
                }
            }
            if(!challenger.statusOfChallenge(challenge)){
                remainingChallengeCount++;
                System.out.println(challenge.toString());
            }
        }

        System.out.print(
            String.format(
                "%d challenges left to complete",
                remainingChallengeCount));

        if(remainingChallengeCount>0){
            Assertions.fail();
        }
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
                http.send("/todos/" + todo.getFieldValue("id").asString(),
                        "POST", headers,
                        "{\"title\":\"mytodo\",\"description\":\"a todo\"}");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_UPDATE_TODO));
    }

    @Test
    public void canPostTodosAsJsonAndAcceptXmlPass() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.putAll(content_application_json);
        headers.putAll(accept_xml_header);

        //{"title":"mytodo","description":"a todo","doneStatus":false}
        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "{\"title\":\"mytodo\",\"description\":\"a todo\",\"doneStatus\":false}");

        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_CREATE_JSON_ACCEPT_XML));
    }

    @Test
    public void canPostTodosAsXmlAndAcceptJsonPass() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.putAll(content_application_xml);
        headers.putAll(accept_json_header);

        //<todo><title>mytodo</title><description>a todo</description><doneStatus>false</doneStatus></todo>
        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "<todo><title>mytodo</title><description>a todo</description><doneStatus>false</doneStatus></todo>");

        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_CREATE_XML_ACCEPT_JSON));
    }


    @Test
    public void canPostTodosAsXml() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.putAll(content_application_xml);
        headers.putAll(accept_xml_header);

        //<todo><title>mytodo</title><description>a todo</description><doneStatus>false</doneStatus></todo>
        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "<todo><title>mytodo</title><description>a todo</description><doneStatus>false</doneStatus></todo>");

        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_CREATE_XML));
    }

    @Test
    public void canPostTodosWithInvalidContentType() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.put("Content-type", "application/x-www-form-urlencoded");

        //<todo><title>mytodo</title><description>a todo</description><doneStatus>false</doneStatus></todo>
        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "<todo><title>mytodo</title><description>a todo</description><doneStatus>false</doneStatus></todo>");

        Assertions.assertEquals(415, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_TODOS_415));
    }


    @Test
    public void canDeleteTodosPass() {

        final Thing todos = ChallengeMain.getChallenger().getThingifier().getThingNamed("todo");
        final ThingInstance todo = todos.createManagedInstance();

        headers.clear();
        headers.putAll(x_challenger_header);

        final HttpResponseDetails response =
                http.send("/todos/" + todo.getFieldValue("id").asString(),
                        "DELETE", headers, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.DELETE_A_TODO));
    }

    @Test
    public void canGetSpecificTodoPass() {

        final Thing todos = ChallengeMain.getChallenger().getThingifier().getThingNamed("todo");
        final ThingInstance todo = todos.createManagedInstance();

        final HttpResponseDetails response =
                http.send("/todos/" + todo.getFieldValue("id").asString(),
                        "GET", x_challenger_header, "");

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

    @Test
    public void canGetHeartbeatPass() {

        final HttpResponseDetails response =
                http.send("/heartbeat", "GET", x_challenger_header, "");

        Assertions.assertEquals(204, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_HEARTBEAT_204));
    }

    /*
        SECRET TOKEN
     */

    @Test
    public void canCreateSecretToken401() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.put("Authorization","basic YWRtaW46YWRtaW4="); // admin:admin

        final HttpResponseDetails response =
                http.send("/secret/token", "POST", headers, "");

        Assertions.assertEquals(401, response.statusCode);
        Assertions.assertTrue(challenger.
                statusOfChallenge(CHALLENGE.CREATE_SECRET_TOKEN_401));
    }

    @Test
    public void canCreateSecretToken201() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.put("Authorization","basic YWRtaW46cGFzc3dvcmQ="); // admin:password

        final HttpResponseDetails response =
                http.send("/secret/token", "POST", headers, "");

        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertTrue(challenger.
                statusOfChallenge(CHALLENGE.CREATE_SECRET_TOKEN_201));
    }

    /**
     * SECRET NOTE
     */

    @Test
    public void canNotGetSecretNoteWhenBadAuth403() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.put("X-AUTH-TOKEN",challenger.getXAuthToken() + "bob");

        final HttpResponseDetails response =
                http.send("/secret/note", "GET", headers, "");

        Assertions.assertEquals(403, response.statusCode);
        Assertions.assertTrue(challenger.
                statusOfChallenge(CHALLENGE.GET_SECRET_NOTE_403));
    }

    @Test
    public void canNotGetSecretNoteWhenNoAuth401() {

        headers.clear();
        headers.putAll(x_challenger_header);

        final HttpResponseDetails response =
                http.send("/secret/note", "GET", headers, "");

        Assertions.assertEquals(401, response.statusCode);
        Assertions.assertTrue(challenger.
                statusOfChallenge(CHALLENGE.GET_SECRET_NOTE_401));
    }

    @Test
    public void canGetSecretNoteWhenAuthToken200() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.put("X-AUTH-TOKEN",challenger.getXAuthToken());

        final HttpResponseDetails response =
                http.send("/secret/note", "GET", headers, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.
                statusOfChallenge(CHALLENGE.GET_SECRET_NOTE_200));
    }

    @Test
    public void canGetSecretNoteWhenBearerAuthToken200() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.put("Authorization","bearer " + challenger.getXAuthToken());

        final HttpResponseDetails response =
                http.send("/secret/note", "GET", headers, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.
                statusOfChallenge(CHALLENGE.GET_SECRET_NOTE_BEARER_200));
    }

    @Test
    public void canPostSecretNoteWhenAuth200() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.put("X-AUTH-TOKEN",challenger.getXAuthToken());

        //{"note":"bob"}
        final HttpResponseDetails response =
                http.send("/secret/note", "POST", headers,
                        "{\"note\":\"bob\"}");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.
                statusOfChallenge(CHALLENGE.POST_SECRET_NOTE_200));
    }

    @Test
    public void canPostSecretNoteWhenBearerAuth200() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.put("Authorization","bearer " + challenger.getXAuthToken());

        //{"note":"bob"}
        final HttpResponseDetails response =
                http.send("/secret/note", "POST", headers,
                        "{\"note\":\"bob\"}");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.
                statusOfChallenge(CHALLENGE.POST_SECRET_NOTE_BEARER_200));
    }


    @Test
    public void cannotPostSecretNoteWhenNoAuth401() {

        headers.clear();
        headers.putAll(x_challenger_header);

        //{"note":"bob"}
        final HttpResponseDetails response =
                http.send("/secret/note", "POST", headers,
                        "{\"note\":\"bob\"}");

        Assertions.assertEquals(401, response.statusCode);
        Assertions.assertTrue(challenger.
                statusOfChallenge(CHALLENGE.POST_SECRET_NOTE_401));
    }

    @Test
    public void cannotPostSecretNoteWhenWrongAuth403() {

        headers.clear();
        headers.putAll(x_challenger_header);
        headers.put("X-AUTH-TOKEN",challenger.getXAuthToken() + "bob");

        //{"note":"bob"}
        final HttpResponseDetails response =
                http.send("/secret/note", "POST", headers,
                        "{\"note\":\"bob\"}");

        Assertions.assertEquals(403, response.statusCode);
        Assertions.assertTrue(challenger.
                statusOfChallenge(CHALLENGE.POST_SECRET_NOTE_403));
    }

    @Test
    public void canCreateANewChallenger() {

        headers.clear();

        //{"note":"bob"}
        final HttpResponseDetails response =
                http.send("/challenger", "POST", headers, "");

        Assertions.assertEquals(201, response.statusCode);

        final String challengerCode = response.getHeader("x-challenger");

        newChallenger = ChallengeMain.getChallenger().
                    getChallengers().getChallenger(challengerCode);

        Assertions.assertTrue(newChallenger.
                statusOfChallenge(CHALLENGE.CREATE_NEW_CHALLENGER));
    }

    @Test
    public void canDeleteAllTodos() {

        final Thing todos = ChallengeMain.getChallenger().getThingifier().getThingNamed("todo");

        for(ThingInstance instance : todos.getInstances()){
            final HttpResponseDetails response =
                    http.send("/todos/" + instance.getFieldValue("id").asString(),
                            "DELETE",
                            x_challenger_header, "");
        }

        Assertions.assertTrue(challenger.
                statusOfChallenge(CHALLENGE.DELETE_ALL_TODOS));

        // add some todos in case this is not the last test

        todos.createManagedInstance();
        todos.createManagedInstance();
        todos.createManagedInstance();
        todos.createManagedInstance();

    }


}



