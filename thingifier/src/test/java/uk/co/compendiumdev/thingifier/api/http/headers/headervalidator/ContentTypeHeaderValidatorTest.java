package uk.co.compendiumdev.thingifier.api.http.headers.headervalidator;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

class ContentTypeHeaderValidatorTest {

    @ParameterizedTest
    @MethodSource("supportedXmlContentTypes")
    void acceptsSupportedXmlContentType(final String contentType) {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        ContentTypeHeaderValidator validator =
                new ContentTypeHeaderValidator(config, List.of("todo", "todos"));

        Assertions.assertNull(validator.validate(contentType));
    }

    @ParameterizedTest
    @MethodSource("modelMatchingStructuredXmlContentTypes")
    void acceptsModelMatchingStructuredXmlContentType(final String contentType) {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        ContentTypeHeaderValidator validator =
                new ContentTypeHeaderValidator(config, List.of("todo", "todos"));

        Assertions.assertNull(validator.validate(contentType));
    }

    @ParameterizedTest
    @MethodSource("unsupportedXmlBasedContentTypes")
    void rejectsUnsupportedXmlBasedContentType(final String mediaType) {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        ContentTypeHeaderValidator validator = new ContentTypeHeaderValidator(config);

        ApiResponse response = validator.validate(mediaType);

        Assertions.assertEquals(415, response.getStatusCode(), mediaType);
        Assertions.assertTrue(
                response.getErrorMessages().contains("Unsupported Content Type - " + mediaType),
                mediaType);
    }

    @ParameterizedTest
    @MethodSource("xmlContentTypes")
    void xmlContentTypeDoesNotBypassDisabledXmlContentValidation(final String mediaType) {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        config.setDefaultContentTypeAsJson(true);
        config.setApiToAllowXmlForContentType(false);
        ContentTypeHeaderValidator validator =
                new ContentTypeHeaderValidator(config, List.of("todo", "todos"));

        ApiResponse response = validator.validate(mediaType);

        Assertions.assertEquals(415, response.getStatusCode(), mediaType);
        Assertions.assertTrue(response.getErrorMessages().contains("XML Not Supported"));
    }

    private static Stream<String> supportedXmlContentTypes() {
        return Stream.of("application/xml", "text/xml");
    }

    private static Stream<String> modelMatchingStructuredXmlContentTypes() {
        return Stream.of(
                "application/vnd.example.todo+xml",
                "application/vnd.example.todo+xml; charset=utf-8");
    }

    private static Stream<String> unsupportedXmlBasedContentTypes() {
        return Stream.of(
                "application/problem+xml",
                "application/soap+xml",
                "application/xhtml+xml",
                "image/svg+xml",
                "application/atom+xml",
                "application/rss+xml",
                "application/*+xml");
    }

    private static Stream<String> xmlContentTypes() {
        return Stream.of("application/xml", "text/xml", "application/vnd.example.todo+xml");
    }
}
