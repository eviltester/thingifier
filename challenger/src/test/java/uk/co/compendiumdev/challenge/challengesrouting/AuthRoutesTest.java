package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.challenge.ChallengeMain;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenger.http.http.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.http.HttpResponseDetails;
import uk.co.compendiumdev.sparkstart.Environment;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

public class AuthRoutesTest {
    private static HttpMessageSender http;
    private static ChallengerAuthData challenger;

    @BeforeAll
    static void createHttp(){
        // this uses the Environment to startup the spark app to
        // issue http tests and test the routing in spark
        http = new HttpMessageSender(Environment.getBaseUri());
        challenger = ChallengeMain.getChallenger().getChallengers().createNewChallenger();
    }

    static Stream simpleRoutingStatus(){
        List<Arguments> args = new ArrayList<>();

        // get
        args.add(Arguments.of(405, "head", "/secret/note"));
        args.add(Arguments.of(405, "options", "/secret/note"));
        // post
        args.add(Arguments.of(405, "put", "/secret/note"));
        args.add(Arguments.of(405, "delete", "/secret/note"));
        args.add(Arguments.of(405, "patch", "/secret/note"));
        args.add(Arguments.of(405, "trace", "/secret/note"));

        args.add(Arguments.of(405, "get", "/secret/token"));
        args.add(Arguments.of(405, "head", "/secret/token"));
        args.add(Arguments.of(405, "options", "/secret/token"));
        // post
        args.add(Arguments.of(405, "put", "/secret/token"));
        args.add(Arguments.of(405, "delete", "/secret/token"));
        args.add(Arguments.of(405, "patch", "/secret/token"));
        args.add(Arguments.of(405, "trace", "/secret/token"));
        return args.stream();
    }



    @ParameterizedTest(name = "simple status routing expected {0} for {1} {2}")
    @MethodSource("simpleRoutingStatus")
    void simpleRoutingTest(int statusCode, String verb, String url){
        final HttpResponseDetails response =
                http.send(url, verb);

        Assertions.assertEquals(statusCode, response.statusCode);
    }

    static Stream simpleRoutingStatusForToken(){
        List<Arguments> args = new ArrayList<>();

        args.add(Arguments.of(405, "get", "/secret/token"));
        args.add(Arguments.of(405, "head", "/secret/token"));
        args.add(Arguments.of(405, "options", "/secret/token"));
        // post
        args.add(Arguments.of(405, "put", "/secret/token"));
        args.add(Arguments.of(405, "delete", "/secret/token"));
        args.add(Arguments.of(405, "patch", "/secret/token"));
        args.add(Arguments.of(405, "trace", "/secret/token"));
        return args.stream();
    }

    @ParameterizedTest(name = "simple status routing expected {0} for {1} {2}")
    @MethodSource("simpleRoutingStatusForToken")
    void simpleRoutingTestAuthenticatedForToken(int statusCode, String verb, String url){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        // admin:password YWRtaW46cGFzc3dvcmQ=
        http.setHeader("Authentication", "basic YWRtaW46cGFzc3dvcmQ=");

        final HttpResponseDetails response =
                http.send(url, verb);

        Assertions.assertEquals(statusCode, response.statusCode);
    }

    @Test
    void noProcessingWhenNoBasicAuth(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        // admin:password YWRtaW46cGFzc3dvcmQ=
        http.setHeader("Authentication", "basic " + base64("wrong:wrong"));

        final HttpResponseDetails response = http.send("/secret/token", "post");

        Assertions.assertEquals(401, response.statusCode);
        Assertions.assertEquals("Basic realm=\"User Visible Realm\"", response.getHeader("WWW-Authenticate"));
        Assertions.assertNull(response.getHeader("X-AUTH-TOKEN"));
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
    }

    @Test
    void noProcessingWhenPassBasicAuthButNoChallenger(){

        http.clearHeaders();
        //http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        // admin:password YWRtaW46cGFzc3dvcmQ=
        http.setHeader("Authorization", "basic " + base64("admin:password"));

        final HttpResponseDetails response = http.send("/secret/token", "post");

        Assertions.assertEquals(401, response.statusCode);
        Assertions.assertEquals("Challenger not recognised", response.getHeader("X-CHALLENGER"));
        Assertions.assertNull(response.getHeader("X-AUTH-TOKEN"));
    }

    @Test
    void noProcessingWhenPassBasicAuthButUnknownChallenger(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", "bobobobobobobobob");
        // admin:password YWRtaW46cGFzc3dvcmQ=
        http.setHeader("Authorization", "basic " + base64("admin:password"));

        final HttpResponseDetails response = http.send("/secret/token", "post");

        Assertions.assertEquals(401, response.statusCode);
        Assertions.assertEquals("Challenger not recognised", response.getHeader("X-CHALLENGER"));
        Assertions.assertNull(response.getHeader("X-AUTH-TOKEN"));
    }

    @Test
    void whenChallengerIsValidThenRevealTheAuthToken(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        // admin:password YWRtaW46cGFzc3dvcmQ=
        http.setHeader("Authorization", "basic " + base64("admin:password"));

        final HttpResponseDetails response = http.send("/secret/token", "post");

        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertEquals(challenger.getXAuthToken(),
                response.getHeader("X-AUTH-TOKEN"));
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
    }

    static String base64(String convertMe){
        final Base64.Encoder base64 = Base64.getEncoder();
        return base64.encodeToString(convertMe.getBytes());
    }

    @Test
    void cannotGetSecretNoteWhenNoAuthToken(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        http.setHeader("Authorization", "basic " + base64("admin:password"));

        final HttpResponseDetails response = http.send("/secret/note", "get");

        Assertions.assertEquals(401, response.statusCode);
        Assertions.assertNull(
                response.getHeader("X-AUTH-TOKEN"));
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
    }

    @Test
    void cannotGetSecretNoteWhenNoValueForAuthToken(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        http.setHeader("X-AUTH-TOKEN", "");
        http.setHeader("Authorization", "basic " + base64("admin:password"));

        final HttpResponseDetails response = http.send("/secret/note", "get");

        Assertions.assertEquals(401, response.statusCode);
        Assertions.assertNull(
                response.getHeader("X-AUTH-TOKEN"));
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
    }

    @Test
    void cannotGetSecretNoteWhenChallengerNotRecognised(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger() + "wrong");
        http.setHeader("X-AUTH-TOKEN", challenger.getXAuthToken());
        http.setHeader("Authorization", "basic " + base64("admin:password"));

        final HttpResponseDetails response = http.send("/secret/note", "get");

        Assertions.assertEquals(401, response.statusCode);
        Assertions.assertNull(
                response.getHeader("X-AUTH-TOKEN"));
        Assertions.assertEquals("Challenger not recognised",
                response.getHeader("X-CHALLENGER"));
    }

    @Test
    void cannotGetSecretNoteWrongAuthTokenForChallenger(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        http.setHeader("X-AUTH-TOKEN", challenger.getXAuthToken()+  "wrong");
        http.setHeader("Authorization", "basic " + base64("admin:password"));

        final HttpResponseDetails response = http.send("/secret/note", "get");

        Assertions.assertEquals(403, response.statusCode);
        Assertions.assertNull(
                response.getHeader("X-AUTH-TOKEN"));
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
    }

    @Test
    void canGetSecretNote(){

        final ChallengerAuthData aNewChallenger = ChallengeMain.getChallenger().getChallengers().createNewChallenger();

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", aNewChallenger.getXChallenger());
        http.setHeader("X-AUTH-TOKEN", aNewChallenger.getXAuthToken());
        http.setHeader("Authorization", "basic " + base64("admin:password"));

        final HttpResponseDetails response = http.send("/secret/note", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("application/json",
                response.getHeader("Content-Type"));
        Assertions.assertEquals(aNewChallenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
        //{"note":""}
        Assertions.assertEquals("{\"note\":\"\"}", response.body);
    }


    @Test
    void cannotPostSecretNoteWhenNoAuthToken(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        http.setHeader("Authorization", "basic " + base64("admin:password"));

        final HttpResponseDetails response = http.send("/secret/note", "post");

        Assertions.assertEquals(401, response.statusCode);
        Assertions.assertNull(
                response.getHeader("X-AUTH-TOKEN"));
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
    }

    @Test
    void cannotPostSecretNoteWhenNoValueForAuthToken(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        http.setHeader("X-AUTH-TOKEN", "");
        http.setHeader("Authorization", "basic " + base64("admin:password"));

        final HttpResponseDetails response = http.send("/secret/note", "post");

        Assertions.assertEquals(401, response.statusCode);
        Assertions.assertNull(
                response.getHeader("X-AUTH-TOKEN"));
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
    }

    @Test
    void cannotPostSecretNoteWhenChallengerNotRecognised(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger() + "wrong");
        http.setHeader("X-AUTH-TOKEN", challenger.getXAuthToken());
        http.setHeader("Authorization", "basic " + base64("admin:password"));

        final HttpResponseDetails response = http.send("/secret/note", "post");

        Assertions.assertEquals(401, response.statusCode);
        Assertions.assertNull(
                response.getHeader("X-AUTH-TOKEN"));
        Assertions.assertEquals("Challenger not recognised",
                response.getHeader("X-CHALLENGER"));
    }

    @Test
    void cannotPostSecretNoteWrongAuthTokenForChallenger(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        http.setHeader("X-AUTH-TOKEN", challenger.getXAuthToken()+  "wrong");
        http.setHeader("Authorization", "basic " + base64("admin:password"));

        final HttpResponseDetails response = http.send("/secret/note", "post");

        Assertions.assertEquals(403, response.statusCode);
        Assertions.assertNull(
                response.getHeader("X-AUTH-TOKEN"));
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
    }

    @Test
    void canPostSecretNote(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        http.setHeader("X-AUTH-TOKEN", challenger.getXAuthToken());
        http.setHeader("Authorization", "basic " + base64("admin:password"));

        //{"note":"hello"}
        final HttpResponseDetails response = http.post("/secret/note", "{\"note\":\"hello\"}");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertNull(
                response.getHeader("X-AUTH-TOKEN"));
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
        Assertions.assertEquals("application/json",
                response.getHeader("Content-Type"));


        Assertions.assertEquals("{\"note\":\"hello\"}", response.body);

        // and get to check

        final HttpResponseDetails getresponse = http.get("/secret/note");
        Assertions.assertEquals("{\"note\":\"hello\"}", getresponse.body);

    }

    @Test
    void cannotPostSecretNoteIfMalformedJson(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        http.setHeader("X-AUTH-TOKEN", challenger.getXAuthToken());
        http.setHeader("Authorization", "basic " + base64("admin:password"));

        //{"note":"hello"}
        final HttpResponseDetails response = http.post("/secret/note", "{\"note\":\"hello\"");

        Assertions.assertEquals(400, response.statusCode);
        Assertions.assertNull(
                response.getHeader("X-AUTH-TOKEN"));
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
        Assertions.assertEquals("application/json",
                response.getHeader("Content-Type"));

        Assertions.assertTrue(response.body.contains("errorMessages"));

    }

    @Test
    void cannotPostSecretNoteIfNoNote(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        http.setHeader("X-AUTH-TOKEN", challenger.getXAuthToken());
        http.setHeader("Authorization", "basic " + base64("admin:password"));

        final HttpResponseDetails response = http.post("/secret/note", "{}");

        Assertions.assertEquals(400, response.statusCode);
        Assertions.assertNull(
                response.getHeader("X-AUTH-TOKEN"));
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
        Assertions.assertEquals("application/json",
                response.getHeader("Content-Type"));
    }

    @Test
    void cannotPostSecretNoteIfNoBody(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        http.setHeader("X-AUTH-TOKEN", challenger.getXAuthToken());
        http.setHeader("Authorization", "basic " + base64("admin:password"));

        final HttpResponseDetails response = http.post("/secret/note", "");

        Assertions.assertEquals(400, response.statusCode);
        Assertions.assertNull(
                response.getHeader("X-AUTH-TOKEN"));
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
        Assertions.assertEquals("application/json",
                response.getHeader("Content-Type"));
    }

    @Test
    void cannotPostSecretNoteIfBodyIsNotNote(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        http.setHeader("X-AUTH-TOKEN", challenger.getXAuthToken());
        http.setHeader("Authorization", "basic " + base64("admin:password"));

        //{"name":"bob"}
        final HttpResponseDetails response = http.post("/secret/note", "{\"name\":\"bob\"}");

        Assertions.assertEquals(400, response.statusCode);
        Assertions.assertNull(
                response.getHeader("X-AUTH-TOKEN"));
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
        Assertions.assertEquals("application/json",
                response.getHeader("Content-Type"));
    }

    // can get secret note if we have a valid bearer token
    @Test
    void canGetSecretNoteIfValidBearerToken(){

        final ChallengerAuthData aNewChallenger = ChallengeMain.getChallenger().getChallengers().createNewChallenger();

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", aNewChallenger.getXChallenger());
        //http.setHeader("X-AUTH-TOKEN", aNewChallenger.getXAuthToken());
        http.setHeader("Authorization", "bearer " + aNewChallenger.getXAuthToken());

        final HttpResponseDetails response = http.send("/secret/note", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("application/json",
                response.getHeader("Content-Type"));
        Assertions.assertEquals(aNewChallenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
        //{"note":""}
        Assertions.assertEquals("{\"note\":\"\"}", response.body);
    }
    // can not get secret note if given bearer token is not valid
    @Test
    void canNotGetSecretNoteWhenInValidBearerToken(){

        final ChallengerAuthData aNewChallenger = ChallengeMain.getChallenger().getChallengers().createNewChallenger();

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", aNewChallenger.getXChallenger());
        //http.setHeader("X-AUTH-TOKEN", aNewChallenger.getXAuthToken());
        http.setHeader("Authorization", "bearer not" + aNewChallenger.getXAuthToken());

        final HttpResponseDetails response = http.send("/secret/note", "get");

        Assertions.assertEquals(403, response.statusCode);
        Assertions.assertEquals("application/json",
                response.getHeader("Content-Type"));
        Assertions.assertEquals(aNewChallenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
    }

    // get secret note bearer token takes precedence over x-auth-token
    @Test
    void getSecretNoteTakesPrecendenceOverAuthToken(){

        final ChallengerAuthData aNewChallenger = ChallengeMain.getChallenger().getChallengers().createNewChallenger();

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", aNewChallenger.getXChallenger());
        http.setHeader("X-AUTH-TOKEN", aNewChallenger.getXAuthToken()+"bob");
        http.setHeader("Authorization", "bearer " + aNewChallenger.getXAuthToken());

        final HttpResponseDetails response = http.send("/secret/note", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("application/json",
                response.getHeader("Content-Type"));
        Assertions.assertEquals(aNewChallenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
        //{"note":""}
        Assertions.assertEquals("{\"note\":\"\"}", response.body);
    }
    // get secret note bearer token takes precedence over x-auth-token even if it is invalid
    @Test
    void getSecretNoteBearerTakesPrecendenceOverAuthTokenEvenIfInvalid(){

        final ChallengerAuthData aNewChallenger = ChallengeMain.getChallenger().getChallengers().createNewChallenger();

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", aNewChallenger.getXChallenger());
        http.setHeader("X-AUTH-TOKEN", aNewChallenger.getXAuthToken());
        http.setHeader("Authorization", "bearer not" + aNewChallenger.getXAuthToken());

        final HttpResponseDetails response = http.send("/secret/note", "get");

        Assertions.assertEquals(403, response.statusCode);
        Assertions.assertEquals("application/json",
                response.getHeader("Content-Type"));
        Assertions.assertEquals(aNewChallenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
    }


    @Test
    void canPostSecretNoteUsingBearerAuth(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        //http.setHeader("X-AUTH-TOKEN", challenger.getXAuthToken());
        http.setHeader("Authorization", "bearer " + challenger.getXAuthToken());

        //{"note":"hello"}
        final HttpResponseDetails response = http.post("/secret/note", "{\"note\":\"hello\"}");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertNull(
                response.getHeader("X-AUTH-TOKEN"));
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
        Assertions.assertEquals("application/json",
                response.getHeader("Content-Type"));


        Assertions.assertEquals("{\"note\":\"hello\"}", response.body);

        // and get to check

        final HttpResponseDetails getresponse = http.get("/secret/note");
        Assertions.assertEquals("{\"note\":\"hello\"}", getresponse.body);
    }

    @Test
    void canPostSecretNoteUsingBearerAuthTakingPrecedence(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        http.setHeader("X-AUTH-TOKEN", challenger.getXAuthToken() + "bob");
        http.setHeader("Authorization", "bearer " + challenger.getXAuthToken());

        //{"note":"hello"}
        final HttpResponseDetails response = http.post("/secret/note", "{\"note\":\"hello\"}");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertNull(
                response.getHeader("X-AUTH-TOKEN"));
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
        Assertions.assertEquals("application/json",
                response.getHeader("Content-Type"));


        Assertions.assertEquals("{\"note\":\"hello\"}", response.body);

        // and get to check

        final HttpResponseDetails getresponse = http.get("/secret/note");
        Assertions.assertEquals("{\"note\":\"hello\"}", getresponse.body);
    }

    @Test
    void cannotPostSecretNoteUsingIncorrectBearerAuthTakingPrecedence(){

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challenger.getXChallenger());
        http.setHeader("X-AUTH-TOKEN", challenger.getXAuthToken());
        http.setHeader("Authorization", "bearer not" + challenger.getXAuthToken());

        //{"note":"hello"}
        final HttpResponseDetails response = http.post("/secret/note", "{\"note\":\"hello\"}");

        Assertions.assertEquals(403, response.statusCode);
        Assertions.assertNull(
                response.getHeader("X-AUTH-TOKEN"));
        Assertions.assertEquals(challenger.getXChallenger(),
                response.getHeader("X-CHALLENGER"));
        Assertions.assertEquals("application/json",
                response.getHeader("Content-Type"));
        Assertions.assertEquals("", response.body);
    }


}
