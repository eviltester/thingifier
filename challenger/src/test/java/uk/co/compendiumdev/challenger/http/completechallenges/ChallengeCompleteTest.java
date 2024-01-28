package uk.co.compendiumdev.challenger.http.completechallenges;

import org.junit.jupiter.api.*;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengeMain;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenger.http.http.HttpResponseDetails;
import uk.co.compendiumdev.challenger.http.http.HttpMessageSender;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.HashMap;
import java.util.Map;

public abstract class ChallengeCompleteTest{

    public static boolean isEnvironmentSet = false;
    public static Challengers challengers;
    public static ChallengerAuthData challenger;
    public static HttpMessageSender http;

    private static ChallengerAuthData newChallenger;

    public Map<String, String> getXChallengerHeader(String guid){
        Map<String, String> xchallenger_header = new HashMap<>();
        xchallenger_header.put("X-CHALLENGER", guid);
        return xchallenger_header;
    }

    @BeforeAll
    public static void controlEnv(){
        Environment.stop();
    }

    abstract boolean getIsSinglePlayerMode();

    public void createEnvironmentAndChallengerIfNecessary(){

        if(!isEnvironmentSet){
            Environment.getBaseUri(getIsSinglePlayerMode());
            isEnvironmentSet=true;
        }

        if(challengers==null){
            challengers = ChallengeMain.getChallenger().getChallengers();
        }

        if(this.challenger==null){
            if(getIsSinglePlayerMode()){
                challenger = challengers.SINGLE_PLAYER;
            }else {
                challenger = challengers.createNewChallenger();
            }
        }

        if(http==null){
            http = new HttpMessageSender(Environment.getBaseUri(getIsSinglePlayerMode()));
        }
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

        Environment.stop();
        isEnvironmentSet = false;
        challengers = null;
        challenger = null;
        http = null;

        newChallenger=null;
    }

    @BeforeEach
    public void setup(){

        createEnvironmentAndChallengerIfNecessary();
    }

    @Test
    public void canGetChallengesPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        final HttpResponseDetails response =
                http.send("/challenges", "GET", x_challenger_header, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_CHALLENGES));

    }

    @Test
    public void canGetTodosPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        final HttpResponseDetails response =
                http.send("/todos", "GET", x_challenger_header, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_TODOS));
    }

    @Test
    public void canGetTodosNoAcceptHeaderPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Accept","");

        final HttpResponseDetails response =
                http.send("/todos", "GET", headers, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_JSON_BY_DEFAULT_NO_ACCEPT));
    }

    @Test
    public void canGet404TodoPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        final HttpResponseDetails response =
                http.send("/todo", "GET", x_challenger_header, "");

        Assertions.assertEquals(404, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_TODOS_NOT_PLURAL_404));
    }

    @Test
    public void canHeadTodosPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        final HttpResponseDetails response =
                http.send("/todos", "HEAD", x_challenger_header, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_HEAD_TODOS));
    }

    @Test
    public void canOptionsTodosPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

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

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");

        //{"title":"mytodo","description":"a todo","doneStatus":false}
        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "{\"title\":\"mytodo\",\"description\":\"a todo\",\"doneStatus\":false}");

        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_TODOS));
    }

    @Test
    public void canPostTodosFailValidationPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");

        //{"title":"mytodo","description":"a todo","doneStatus":"bob"}
        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "{\"title\":\"mytodo\",\"description\":\"a todo\",\"doneStatus\":\"bob\"}");

        Assertions.assertEquals(400, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_TODOS_BAD_DONE_STATUS));
    }

    @Test
    public void canPostTodosUpdatePass() {

        final EntityInstanceCollection todos = ChallengeMain.getChallenger().getThingifier().getThingInstancesNamed("todo", challenger.getXChallenger());
        final EntityInstance todo = todos.createManagedInstance();

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");

        final HttpResponseDetails response =
                http.send("/todos/" + todo.getFieldValue("id").asString(),
                        "POST", headers,
                        "{\"title\":\"mytodo\",\"description\":\"a todo\"}");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_UPDATE_TODO));
    }

    @Test
    public void canPostTodosAsJsonAndAcceptXmlPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/xml");

        //{"title":"mytodo","description":"a todo","doneStatus":false}
        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "{\"title\":\"mytodo\",\"description\":\"a todo\",\"doneStatus\":false}");

        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_CREATE_JSON_ACCEPT_XML));
    }

    @Test
    public void canPostTodosAsJsonAndAcceptJSONPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");

        //{"title":"mytodo","description":"a todo","doneStatus":false}
        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "{\"title\":\"mytodo\",\"description\":\"a todo\",\"doneStatus\":false}");

        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_CREATE_JSON));
    }

    @Test
    public void canPostTodosAsXmlAndAcceptJsonPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/xml");
        headers.put("Accept", "application/json");

        //<todo><title>mytodo</title><description>a todo</description><doneStatus>false</doneStatus></todo>
        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "<todo><title>mytodo</title><description>a todo</description><doneStatus>false</doneStatus></todo>");

        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_CREATE_XML_ACCEPT_JSON));
    }


    @Test
    public void canPostTodosAsXml() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/xml");
        headers.put("Accept", "application/xml");

        //<todo><title>mytodo</title><description>a todo</description><doneStatus>false</doneStatus></todo>
        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "<todo><title>mytodo</title><description>a todo</description><doneStatus>false</doneStatus></todo>");

        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_CREATE_XML));
    }

    @Test
    public void canPostTodosWithInvalidContentType() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
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

        final EntityInstanceCollection todos = ChallengeMain.getChallenger().getThingifier().getThingInstancesNamed("todo", challenger.getXChallenger());
        final EntityInstance todo = todos.createManagedInstance();

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);

        final HttpResponseDetails response =
                http.send("/todos/" + todo.getFieldValue("id").asString(),
                        "DELETE", headers, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.DELETE_A_TODO));
    }

    @Test
    public void canGetSpecificTodoPass() {

        final EntityInstanceCollection todos = ChallengeMain.getChallenger().getThingifier().getThingInstancesNamed("todo", challenger.getXChallenger());
        final EntityInstance todo = todos.createManagedInstance();

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());


        final HttpResponseDetails response =
                http.send("/todos/" + todo.getFieldValue("id").asString(),
                        "GET", x_challenger_header, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_TODO));
    }

    @Test
    public void canGetTodosXMLPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Accept", "application/xml");

        final HttpResponseDetails response =
                http.send("/todos", "GET",
                        headers, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_ACCEPT_XML));
    }

    @Test
    public void canGetTodoJsonPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Accept", "application/json");

        final HttpResponseDetails response =
                http.send("/todos" , "GET",
                        headers, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_ACCEPT_JSON));
    }

    @Test
    public void canGetTodoAnyPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
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

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Accept","application/xml, application/json");

        final HttpResponseDetails response =
                http.send("/todos" , "GET", headers, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_ACCEPT_XML_PREFERRED));

    }

    @Test
    public void canGetTodosNotAcceptGzip() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Accept","application/gzip");

        final HttpResponseDetails response =
                http.send("/todos" , "GET", headers, "");

        Assertions.assertEquals(406, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_UNSUPPORTED_ACCEPT_406));
    }

    @Test
    public void todo404pass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        final HttpResponseDetails response =
                http.send("/todos/guiddoesnotexist", "GET", x_challenger_header, "");

        Assertions.assertEquals(404, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_TODO_404));
    }

    @Test
    public void canFilterTodoPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        final EntityInstanceCollection todos = ChallengeMain.getChallenger().getThingifier().getThingInstancesNamed("todo", challenger.getXChallenger());

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

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        final HttpResponseDetails response =
                http.send("/heartbeat", "DELETE", x_challenger_header, "");

        Assertions.assertEquals(405, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.DELETE_HEARTBEAT_405));
    }

    @Test
    public void can500PatchHeartbeatPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        final HttpResponseDetails response =
                http.send("/heartbeat", "PATCH", x_challenger_header, "");

        Assertions.assertEquals(500, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.PATCH_HEARTBEAT_500));
    }

    @Test
    public void can501TraceHeartbeatPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        final HttpResponseDetails response =
                http.send("/heartbeat", "TRACE", x_challenger_header, "");

        Assertions.assertEquals(501, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.TRACE_HEARTBEAT_501));
    }

    @Test
    public void canGetHeartbeatPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

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

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
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

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
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

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
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

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);

        final HttpResponseDetails response =
                http.send("/secret/note", "GET", headers, "");

        Assertions.assertEquals(401, response.statusCode);
        Assertions.assertTrue(challenger.
                statusOfChallenge(CHALLENGE.GET_SECRET_NOTE_401));
    }

    @Test
    public void canGetSecretNoteWhenAuthToken200() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
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

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
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

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("X-AUTH-TOKEN",challenger.getXAuthToken());
        headers.put("Content-Type","application/json");

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

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Authorization","bearer " + challenger.getXAuthToken());
        headers.put("Content-Type","application/json");

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

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type","application/json");

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

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("X-AUTH-TOKEN",challenger.getXAuthToken() + "bob");
        headers.put("Content-Type","application/json");

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

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();

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

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());


        final EntityInstanceCollection todos = ChallengeMain.getChallenger().getThingifier().getThingInstancesNamed("todo", challenger.getXChallenger());

        for(EntityInstance instance : todos.getInstances()){
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



