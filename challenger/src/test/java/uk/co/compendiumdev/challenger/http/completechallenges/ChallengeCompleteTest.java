package uk.co.compendiumdev.challenger.http.completechallenges;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengeMain;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.payloads.Todos;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public abstract class ChallengeCompleteTest{

    static Logger logger = LoggerFactory.getLogger(ChallengeCompleteTest.class);

    public static boolean isEnvironmentSet = false;
    public static int challengesOffset = 0;
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
    public static void controlEnvStart(){
        Environment.stop();
    }

    @AfterAll
    public static void controlEnvStop(){
        Environment.stop();
    }

    abstract boolean getIsSinglePlayerMode();
    abstract int getNumberOfChallengesToFail();

    public void createEnvironmentAndChallengerIfNecessary(){

        challengesOffset = getNumberOfChallengesToFail();

        if(!isEnvironmentSet){
            Environment.getBaseUri(getIsSinglePlayerMode(), true);
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
            http = new HttpMessageSender(Environment.getBaseUri(getIsSinglePlayerMode(), true));
        }
    }

    // After all - check that all challenges are complete
    @AfterAll
    static void alldone(){

        int remainingChallengeCount = 0;

        // only compare with defined challenges, not all challenges
        for(CHALLENGE challenge : ChallengeMain.getChallenger().getChallengers().getDefinedChallenges()){
            if(newChallenger!=null &&
                    challenge==CHALLENGE.CREATE_NEW_CHALLENGER){
                if(newChallenger.statusOfChallenge(challenge)){
                    continue;
                }
            }
            if(!challenger.statusOfChallenge(challenge)){
                remainingChallengeCount++;
                logger.warn("Still to complete challenge " + challenge.toString());
            }
        }

        logger.error("{} challenges left to complete", remainingChallengeCount);

        if(remainingChallengeCount-challengesOffset>0){
            Assertions.fail(String.format(
                    "%d challenges left to complete",
                    remainingChallengeCount));
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

        // initial challenge had options as 200, so we have a hack to keep that
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

        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "{\"title\":\"mytodo\",\"description\":\"a todo\",\"doneStatus\":false}");

        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_TODOS));
    }

    @Test
    public void canPutTodos400FailCreatePass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");

        // try to create a to do but fail because the AUTO fields mean we can't control the id
        final HttpResponseDetails response =
                http.send("/todos/200", "PUT", headers,
                        "{\"id\":200, \"title\":\"mytodo\",\"description\":\"a todo\",\"doneStatus\":false}");

        Assertions.assertEquals(400, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.PUT_TODOS_400));
    }

    @Test
    public void canPutTodosFull200AmendPass() {

        final EntityInstanceCollection todos = ChallengeMain.getChallenger().getThingifier().getThingInstancesNamed("todo", challenger.getXChallenger());

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");

        EntityInstance aTodo = new ArrayList<>(todos.getInstances()).get(0);

        // amend a to do successfully
        final HttpResponseDetails response =
                http.send("/todos/" + aTodo.getPrimaryKeyValue(),
                        "PUT", headers,
                        "{\"title\":\"my put todo\",\"description\":\"a put description\",\"doneStatus\":true}");
        // complete payload to avoid defaults

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.PUT_TODOS_FULL_200));


    }

    @Test
    public void canPutTodosPartial200AmendPass() {

        final EntityInstanceCollection todos = ChallengeMain.getChallenger().getThingifier().getThingInstancesNamed("todo", challenger.getXChallenger());

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");

        EntityInstance aTodo = new ArrayList<>(todos.getInstances()).get(0);

        // amend a to do successfully
        final HttpResponseDetails response =
                http.send("/todos/" + aTodo.getPrimaryKeyValue(),
                        "PUT", headers,
                        "{\"title\":\"my put todo\"}");
        // only title is mandatory the rest would be set to defaults

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.PUT_TODOS_PARTIAL_200));
    }

    @Test
    public void canPutTodos200MissingTitleAmendPass() {

        final EntityInstanceCollection todos = ChallengeMain.getChallenger().getThingifier().getThingInstancesNamed("todo", challenger.getXChallenger());

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");

        EntityInstance aTodo = new ArrayList<>(todos.getInstances()).get(0);

        // amend a to do unsuccessfully
        final HttpResponseDetails response =
                http.send("/todos/" + aTodo.getPrimaryKeyValue(),
                        "PUT", headers,
                        "{\"description\":\"my description\"}");
        // title is mandatory so this will fail

        Assertions.assertEquals(400, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.PUT_TODOS_MISSING_TITLE_400));
    }

    @Test
    public void canNotPutTodos400ChangeIdAmendPass() {

        final EntityInstanceCollection todos = ChallengeMain.getChallenger().getThingifier().getThingInstancesNamed("todo", challenger.getXChallenger());

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");

        EntityInstance aTodo = new ArrayList<>(todos.getInstances()).get(0);

        // amend a to do unsuccessfully
        final HttpResponseDetails response =
                http.send("/todos/" + aTodo.getPrimaryKeyValue(),
                        "PUT", headers,
                        String.format("{\"id\":%d, \"description\":\"my description\"}", Integer.parseInt(aTodo.getPrimaryKeyValue()+1))
                );
        // title is mandatory so this will fail

        Assertions.assertEquals(400, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.PUT_TODOS_400_NO_AMEND_ID));
    }

    @Test
    public void canPostTodos404Pass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");

        // try to create a to do item but fail because the AUTO fields mean we can't control the id
        final HttpResponseDetails response =
                http.send("/todos/2004567", "POST", headers,
                        "{\"id\":2004567, \"title\":\"mytodo\",\"description\":\"a todo\",\"doneStatus\":false}");

        Assertions.assertEquals(404, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_TODOS_404));
    }

    @Test
    public void canPostTodosFailValidationPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");

        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "{\"title\":\"mytodo\",\"description\":\"a todo\",\"doneStatus\":\"bob\"}");

        Assertions.assertEquals(400, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_TODOS_BAD_DONE_STATUS));
    }

    @Test
    public void canPostTodosFailTitleLenValidationPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");

        // TODO: should send back multiple error messages to allow all validations to fail in one request e.g. todo status fails validation before title
        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "{\"title\":\"*3*5*7*9*12*15*18*21*24*27*30*33*36*39*42*45*48*51*\",\"description\":\"a todo\"}");

        Assertions.assertEquals(400, response.statusCode);
        Assertions.assertTrue(response.body.contains("Failed Validation: Maximum allowable length exceeded for title - maximum allowed is 50"));
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_TODOS_TOO_LONG_TITLE_LENGTH));
    }

    @Test
    public void canPostTodosFailDescriptionLenValidationPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");

        String twoHundredAndOneChars = "*3*5*7*10*13*16*19*22*25*28*31*34*37*40*43*46*49*52*55*58*61*" +
                "64*67*70*73*76*79*82*85*88*91*94*97*101*105*109*113*117*121*" +
                "125*129*133*137*141*145*149*153*157*161*165*169*173*177*181*185*189*193*197*201*";

        // TODO: should send back multiple error messages to allow all validations to fail in one request e.g. todo status fails validation before title
        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "{\"title\":\"*3*5*7*9*12*15*18*21*24*27*30*33*36*39*42*45*48*50\",\"description\":\"" + twoHundredAndOneChars + "\"}");

        Assertions.assertEquals(400, response.statusCode);
        Assertions.assertTrue(response.body.contains("Failed Validation: Maximum allowable length exceeded for description - maximum allowed is 200"));
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_TODOS_TOO_LONG_DESCRIPTION_LENGTH));
    }

    @Test
    public void canPostTodosWithMaxTitleAndDescriptionLengths() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");

        // TODO: should send back multiple error messages to allow all validations to fail in one request e.g. todo status fails validation before title
        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "{\"title\":\"" + stringOfLength(50) + "\",\"description\":\"" + stringOfLength(200) + "\"}");

        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_MAX_OUT_TITLE_DESCRIPTION_LENGTH));
    }
    @Test
    public void canPostTodosFailPayloadLenValidationPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");

        String fiveThousandChars = stringOfLength(5000);

        // TODO: should send back multiple error messages to allow all validations to fail in one request e.g. todo status fails validation before title
        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "{\"title\":\"title\",\"description\":\"" + fiveThousandChars + "\"}");

        Assertions.assertEquals(413, response.statusCode);
        Assertions.assertTrue(response.body.contains("Request body too large, max allowed is 5000 bytes"));
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_TODOS_TOO_LONG_PAYLOAD_SIZE));
    }

    @Test
    public void canPostTodosFailExtraField() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");

        String fiveThousandChars = stringOfLength(5000);

        // TODO: should send back multiple error messages to allow all validations to fail in one request e.g. todo status fails validation before title
        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "{\"title\":\"title\",\"description\":\"description\", \"priority\": \"urgent\"}");

        Assertions.assertEquals(400, response.statusCode);
        Assertions.assertTrue(response.body.contains("Could not find field: priority"));
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_TODOS_INVALID_EXTRA_FIELD));
    }

    private String stringOfLength(final int desiredLength) {
        String ofLength = "";
        while(ofLength.length()<desiredLength){
            ofLength = ofLength + "a";
        }

        return ofLength;
    }

    @Test
    public void canPostTodosUpdatePass() {

        ensureAtMostXTodoAvailable(10);

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

        final HttpResponseDetails response =
                http.send("/todos", "POST", headers,
                        "<todo><title>mytodo</title><description>a todo</description><doneStatus>false</doneStatus></todo>");

        Assertions.assertEquals(415, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_TODOS_415));
    }

    @Test
    public void canPostToAddMaxTodosPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");

        HttpResponseDetails response=null;

        int maxTodos = 20;
        // will create 21 which will max out
        for(int todoCount=0; todoCount<=maxTodos; todoCount++){
             response =
                    http.send("/todos", "POST", headers,
                            "{\"title\":\"mytodo\",\"description\":\"a todo\",\"doneStatus\":false}");
        }


        Assertions.assertEquals(400, response.statusCode);
        Assertions.assertTrue(response.body.contains("ERROR: Cannot add instance, maximum limit of 20 reached"));
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_ALL_TODOS));
    }

    public void ensureAtMostXTodoAvailable(int x){
        final EntityInstanceCollection todos = ChallengeMain.getChallenger().getThingifier().getThingInstancesNamed("todo", challenger.getXChallenger());
        if(todos.countInstances()>x){
            for(int delCount = todos.countInstances()-x; delCount > 0; delCount--) {
                todos.deleteInstance((EntityInstance) (todos.getInstances().toArray()[0]));
            }
        }
    }

    @Test
    public void canDeleteTodosPass() {

        ensureAtMostXTodoAvailable(10);

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
    public void canUseMethodOverrideHeaderToPatch500() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("X-HTTP-Method-Override", "PATCH");
        headers.put("Content-Type","application/json");

        //{"note":"bob"}
        final HttpResponseDetails response =
                http.send("/heartbeat", "POST", headers, "");

        Assertions.assertEquals(500, response.statusCode);
        Assertions.assertTrue(challenger.
                statusOfChallenge(CHALLENGE.OVERRIDE_PATCH_HEARTBEAT_500));
    }

    @Test
    public void canUseMethodOverrideHeaderToDelete405() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("X-HTTP-Method-Override", "DELETE");
        headers.put("Content-Type","application/json");

        //{"note":"bob"}
        final HttpResponseDetails response =
                http.send("/heartbeat", "POST", headers, "");

        Assertions.assertEquals(405, response.statusCode);
        Assertions.assertTrue(challenger.
                statusOfChallenge(CHALLENGE.OVERRIDE_DELETE_HEARTBEAT_405));
    }

    @Test
    public void canUseMethodOverrideHeaderToTrace501() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("X-HTTP-Method-Override", "TRACE");
        headers.put("Content-Type","application/json");

        //{"note":"bob"}
        final HttpResponseDetails response =
                http.send("/heartbeat", "POST", headers, "");

        Assertions.assertEquals(501, response.statusCode);
        Assertions.assertTrue(challenger.
                statusOfChallenge(CHALLENGE.OVERRIDE_TRACE_HEARTBEAT_501));
    }

    @Test
    public void canCreateANewChallenger() {

        // no headers means create a new challenger
        Map<String, String> headers = new HashMap<>();

        //{"note":"bob"}
        final HttpResponseDetails response =
                http.send("/challenger", "POST", headers, "");

        Assertions.assertEquals(201, response.statusCode);

        final String challengerCode = response.getHeader("x-challenger");

        // add to the new challenger to allow counting
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

    @Test
    public void canGetChallengerToRestorePass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        final HttpResponseDetails response =
                http.send("/challenger/" + challenger.getXChallenger(), "GET", x_challenger_header, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_RESTORABLE_CHALLENGER_PROGRESS_STATUS));

    }

    @Test
    public void canRestoreAnExistingChallengerPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        final HttpResponseDetails response =
                http.send("/challenger/" + challenger.getXChallenger(), "GET", x_challenger_header, "");

        final HttpResponseDetails restoreResponse =
                http.send("/challenger/" + challenger.getXChallenger(), "PUT", x_challenger_header, response.body);

        Assertions.assertEquals(200, restoreResponse.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.PUT_RESTORABLE_CHALLENGER_PROGRESS_STATUS));
    }

    @Test
    public void canGetTodosDatabaseToRestorePass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        if(!challenger.getXChallenger().equals("rest-api-challenges-single-player")) {
            ChallengeMain.getChallenger().getThingifier().ensureCreatedAndPopulatedInstanceDatabaseNamed(challenger.getXChallenger());
        }

        final HttpResponseDetails response =
                http.send("/challenger/database/" + challenger.getXChallenger(), "GET", x_challenger_header, "");


        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_RESTORABLE_TODOS));

        Todos todos = new Gson().fromJson(response.body, Todos.class);

    }


    @Test
    public void canRestoreTodosDatabaseToPass() {

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        if(!challenger.getXChallenger().equals("rest-api-challenges-single-player")) {
            ChallengeMain.getChallenger().getThingifier().ensureCreatedAndPopulatedInstanceDatabaseNamed(challenger.getXChallenger());
        }

//        final HttpResponseDetails response =
//                http.send("/challenger/database/" + challenger.getXChallenger(), "GET", x_challenger_header, "");

        Todos newTodos = new Todos();
        Todo aTodo = new Todo();
        aTodo.title = "amended for put";
        aTodo.id = 1;
        aTodo.description="describe me";
        newTodos.todos = new ArrayList<>();
        newTodos.todos.add(aTodo);

        final HttpResponseDetails putresponse =
                http.send("/challenger/database/" + challenger.getXChallenger(), "PUT", x_challenger_header,  new Gson().toJson(newTodos));

        Assertions.assertEquals(204, putresponse.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.PUT_RESTORABLE_TODOS));

        final HttpResponseDetails getAgainResponse =
                http.send("/challenger/database/" + challenger.getXChallenger(), "GET", x_challenger_header, "");

        Todos todosAfterAmend = new Gson().fromJson(getAgainResponse.body, Todos.class);
        Assertions.assertEquals(newTodos.todos.get(0).title, todosAfterAmend.todos.get(0).title);
    }




}



