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

public class BearerAuthHeaderParserTest {

    // Authorization: Bearer token

    @Test
    void canParseBearerToken(){
        final BearerAuthHeaderParser parser = new BearerAuthHeaderParser(
                "bearer mytoken");

        Assertions.assertTrue(parser.isValid());
        Assertions.assertEquals("mytoken", parser.getToken());
    }

    @Test
    void canNotMatchIfParseEmpty(){
        final BearerAuthHeaderParser parser = new BearerAuthHeaderParser("");

        Assertions.assertFalse(parser.isValid());
    }

    @Test
    void canNotMatchIfParseNull(){
        final BearerAuthHeaderParser parser = new BearerAuthHeaderParser(null);

        Assertions.assertFalse(parser.isValid());
    }

    @Test
    void canParseHeaderSpacingNotAnIssue(){
        final BearerAuthHeaderParser parser = new BearerAuthHeaderParser("    bearer      mytoken    ");

        Assertions.assertTrue(parser.isValid());
        Assertions.assertEquals("mytoken", parser.getToken());
    }

    @Test
    void canParseHeaderCaseNotAnIssue(){
        final BearerAuthHeaderParser parser = new BearerAuthHeaderParser("BeaRer mytoken");

        Assertions.assertTrue(parser.isValid());
        Assertions.assertEquals("mytoken", parser.getToken());
    }

    static Stream invalidHeaderValues(){
        List<Arguments> args = new ArrayList<>();

        args.add(Arguments.of("not bearer", "basic YWRtaW46cGFzc3dvcmQ="));
        args.add(Arguments.of("no bearer", "mytoken"));
        args.add(Arguments.of("no bearer but spaces", "    mytoken"));
        args.add(Arguments.of("wrong order", "mytoken bearer"));
        args.add(Arguments.of("no token", "bearer "));
        args.add(Arguments.of("just bearer", "bearer"));
        return args.stream();
    }

    @ParameterizedTest(name = "invalid when {0}")
    @MethodSource("invalidHeaderValues")
    void failsWhenHeaderDoesNotMatch(String reason, String header){
        final BearerAuthHeaderParser parser = new BearerAuthHeaderParser(header);

        Assertions.assertFalse(parser.isValid());
    }
}
