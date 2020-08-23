package uk.co.compendiumdev.challenge;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

public class BasicAuthHeaderParserTest {

    // admin:password YWRtaW46cGFzc3dvcmQ=

    @Test
    void canParseUsernamePassword(){
        final BasicAuthHeaderParser parser = new BasicAuthHeaderParser("basic YWRtaW46cGFzc3dvcmQ=");

        Assertions.assertTrue(
                parser.matches(
                        "admin", "password")
        );
    }

    @Test
    void canNotMatchIfParseEmpty(){
        final BasicAuthHeaderParser parser = new BasicAuthHeaderParser("");

        Assertions.assertFalse(
                parser.matches(
                        "admin", "password")
        );
    }

    @Test
    void canNotMatchIfParseNull(){
        final BasicAuthHeaderParser parser = new BasicAuthHeaderParser(null);

        Assertions.assertFalse(
                parser.matches(
                        "admin", "password")
        );
    }

    @Test
    void canParseUsernamePasswordSpacingNotAnIssue(){
        final BasicAuthHeaderParser parser = new BasicAuthHeaderParser("    basic      YWRtaW46cGFzc3dvcmQ=    ");

        Assertions.assertTrue(
                parser.matches(
                        "admin", "password")
        );
    }

    @Test
    void canParseUsernamePasswordToFindNoMatch(){
        final BasicAuthHeaderParser parser = new BasicAuthHeaderParser("basic YWRtaW46cGFzc3dvcmQ=");

        Assertions.assertTrue(
                parser.matches(
                        "admin", "password")
        );
    }

    @Test
    void failsWhenNotBasic(){
        final BasicAuthHeaderParser parser = new BasicAuthHeaderParser("basics YWRtaW46cGFzc3dvcmQ=");

        Assertions.assertFalse(
                parser.matches(
                        "admin", "password")
        );
    }


    static Stream invalidMatchingValues(){
        List<Arguments> args = new ArrayList<>();

        args.add(Arguments.of(null, "password"));
        args.add(Arguments.of("admin", null));
        args.add(Arguments.of(null, null));
        args.add(Arguments.of("", ""));
        args.add(Arguments.of("admin", ""));
        args.add(Arguments.of("", "password"));
        args.add(Arguments.of("wrong", "password"));
        args.add(Arguments.of("admin", "wrong"));
        return args.stream();
    }

    @ParameterizedTest(name = "invalid when asked to match on {0}:{1} instead of admin:password")
    @MethodSource("invalidMatchingValues")
    void failsWhenNoUsernameOrPasswordInMatch(String username, String password){
        // admin:password YWRtaW46cGFzc3dvcmQ=
        final BasicAuthHeaderParser parser =
                new BasicAuthHeaderParser("basic YWRtaW46cGFzc3dvcmQ=");

        Assertions.assertFalse(
                parser.matches(
                        username, password)
        );
    }

    static Stream invalidHeaderValues(){
        List<Arguments> args = new ArrayList<>();

        args.add(Arguments.of("not basic", "basics YWRtaW46cGFzc3dvcmQ="));
        args.add(Arguments.of("no basic", "YWRtaW46cGFzc3dvcmQ="));
        args.add(Arguments.of("no basic but spaces", "    YWRtaW46cGFzc3dvcmQ="));
        args.add(Arguments.of("wrong order", "YWRtaW46cGFzc3dvcmQ= basic"));
        args.add(Arguments.of("basic but wrong password",
                                "basic " + base64("admin:pass")));
        args.add(Arguments.of("no username", "basic " + base64(":password")));
        args.add(Arguments.of("no password", "basic " + base64("admin:")));
        args.add(Arguments.of("no colon", "basic " + base64("adminpassword")));
        args.add(Arguments.of("no values, just colon", "basic " + base64(":")));
        args.add(Arguments.of("no values", "basic " + base64("")));
        args.add(Arguments.of("just basic", "basic"));
        return args.stream();
    }

    @ParameterizedTest(name = "invalid when {0}")
    @MethodSource("invalidHeaderValues")
    void failsWhenHeaderDoesNotMatch(String reason, String header){
        final BasicAuthHeaderParser parser = new BasicAuthHeaderParser(header);

        Assertions.assertFalse(
                parser.matches(
                        "admin", "password")
        );
    }

    // admin:password YWRtaW46cGFzc3dvcmQ=
    // admin:pass YWRtaW46cGFzcw==
    @Test
    void canTrustBase64Conversion(){
        Assertions.assertEquals("YWRtaW46cGFzc3dvcmQ=", base64("admin:password"));
        Assertions.assertEquals("YWRtaW46cGFzcw==", base64("admin:pass"));
    }

    static String base64(String convertMe){
        final Base64.Encoder base64 = Base64.getEncoder();
        return base64.encodeToString(convertMe.getBytes());
    }

}
