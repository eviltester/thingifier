package uk.co.compendiumdev.thingifier.api.http.headers.headerparser;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class BasicAuthHeaderParserTest {

    @Test
    void identifiesBasicAuthScheme() {
        final BasicAuthHeaderParser parser =
                new BasicAuthHeaderParser("basic " + base64("admin:password"));

        Assertions.assertTrue(parser.isBasicAuth());
    }

    @Test
    void identifiesNonBasicAuthScheme() {
        final BasicAuthHeaderParser parser =
                new BasicAuthHeaderParser("Bearer " + base64("admin:password"));

        Assertions.assertFalse(parser.isBasicAuth());
    }

    @Test
    void validBasicHeaderExposesUsernameAndPassword() {
        final BasicAuthHeaderParser parser =
                new BasicAuthHeaderParser("basic " + base64("admin:password"));

        Assertions.assertTrue(parser.isValid());
        Assertions.assertEquals("admin", parser.username());
        Assertions.assertEquals("password", parser.password());
    }

    @Test
    void validBasicHeaderSplitsCredentialsOnFirstColon() {
        final BasicAuthHeaderParser parser =
                new BasicAuthHeaderParser("basic " + base64("admin:pa:ss"));

        Assertions.assertTrue(parser.isValid());
        Assertions.assertEquals("admin", parser.username());
        Assertions.assertEquals("pa:ss", parser.password());
    }

    @Test
    void validBasicHeaderSpacingDoesNotAffectParsing() {
        final BasicAuthHeaderParser parser =
                new BasicAuthHeaderParser("    basic      " + base64("admin:password") + "    ");

        Assertions.assertTrue(parser.isValid());
        Assertions.assertTrue(parser.matches("admin", "password"));
    }

    static Stream<Arguments> invalidBasicSyntaxValues() {
        return Stream.of(
                Arguments.of("missing header", null),
                Arguments.of("empty header", ""),
                Arguments.of("wrong scheme", "Bearer " + base64("admin:password")),
                Arguments.of("credentials without scheme", base64("admin:password")),
                Arguments.of("credentials before scheme", base64("admin:password") + " basic"),
                Arguments.of("missing credentials", "basic"),
                Arguments.of("extra token", "basic " + base64("admin:password") + " extra"),
                Arguments.of("malformed base64", "basic not-base64"),
                Arguments.of("missing colon", "basic " + base64("adminpassword")),
                Arguments.of("empty username", "basic " + base64(":password")),
                Arguments.of("empty password", "basic " + base64("admin:")),
                Arguments.of("empty username and password", "basic " + base64(":")),
                Arguments.of("empty decoded credentials", "basic " + base64("")));
    }

    @ParameterizedTest(name = "invalid when {0}")
    @MethodSource("invalidBasicSyntaxValues")
    void invalidBasicSyntaxIsNotValid(final String reason, final String header) {
        final BasicAuthHeaderParser parser = new BasicAuthHeaderParser(header);

        Assertions.assertFalse(parser.isValid());
    }

    @Test
    void matchesValidCredentials() {
        final BasicAuthHeaderParser parser =
                new BasicAuthHeaderParser("basic " + base64("admin:password"));

        Assertions.assertTrue(parser.matches("admin", "password"));
    }

    static Stream<Arguments> nonMatchingCredentialValues() {
        return Stream.of(
                Arguments.of(null, "password"),
                Arguments.of("admin", null),
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("admin", ""),
                Arguments.of("", "password"),
                Arguments.of("wrong", "password"),
                Arguments.of("admin", "wrong"));
    }

    @ParameterizedTest(name = "does not match expected {0}:{1}")
    @MethodSource("nonMatchingCredentialValues")
    void matchesRejectsUnexpectedExpectedCredentials(final String username, final String password) {
        final BasicAuthHeaderParser parser =
                new BasicAuthHeaderParser("basic " + base64("admin:password"));

        Assertions.assertFalse(parser.matches(username, password));
    }

    @ParameterizedTest(name = "does not match invalid header when {0}")
    @MethodSource("invalidBasicSyntaxValues")
    void matchesRejectsInvalidBasicSyntax(final String reason, final String header) {
        final BasicAuthHeaderParser parser = new BasicAuthHeaderParser(header);

        Assertions.assertFalse(parser.matches("admin", "password"));
    }

    @Test
    void canTrustBase64Conversion() {
        Assertions.assertEquals("YWRtaW46cGFzc3dvcmQ=", base64("admin:password"));
        Assertions.assertEquals("YWRtaW46cGFzcw==", base64("admin:pass"));
    }

    static String base64(final String convertMe) {
        return Base64.getEncoder().encodeToString(convertMe.getBytes(StandardCharsets.UTF_8));
    }
}
