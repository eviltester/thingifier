package uk.co.compendiumdev.thingifier.api.http.headers.headervalidator;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

class AcceptHeaderValidatorTest {

    @ParameterizedTest
    @MethodSource("additionalResponseRepresentationMediaTypes")
    void acceptsAdditionalResponseRepresentation(final String mediaType) {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        AcceptHeaderValidator validator = new AcceptHeaderValidator(config);

        Assertions.assertNull(validator.validate(mediaType));
    }

    @ParameterizedTest
    @MethodSource("xmlCompatibleResponseMediaTypes")
    void acceptsXmlCompatibleResponseRepresentation(final String mediaType) {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        AcceptHeaderValidator validator = new AcceptHeaderValidator(config);

        Assertions.assertNull(validator.validate(mediaType));
    }

    @ParameterizedTest
    @MethodSource("modelMatchingStructuredXmlResponseMediaTypes")
    void acceptsModelMatchingStructuredXmlResponseRepresentation(final String mediaType) {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        AcceptHeaderValidator validator =
                new AcceptHeaderValidator(config, List.of("todo", "todos"));

        Assertions.assertNull(validator.validate(mediaType));
    }

    @Test
    void rejectsUnsupportedHeaderWhenEnforced() {
        ThingifierApiConfig config = new ThingifierApiConfig("");

        ApiResponse response =
                new AcceptHeaderValidator(config).validate("application/problem+json");

        Assertions.assertEquals(406, response.getStatusCode());
        Assertions.assertTrue(response.getErrorMessages().contains("Unrecognised Accept Type"));
    }

    @Test
    void canFallThroughDisabledXmlToAdditionalRepresentation() {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        config.setApiToAllowXmlForResponses(false);

        ApiResponse response =
                new AcceptHeaderValidator(config).validate("application/xml, text/csv");

        Assertions.assertNull(response);
    }

    @Test
    void unsupportedMediaTypesAreIgnoredWhenSupportedAlternativeExists() {
        ThingifierApiConfig config = new ThingifierApiConfig("");

        ApiResponse response =
                new AcceptHeaderValidator(config)
                        .validate("application/problem+json, application/json;q=0.5");

        Assertions.assertNull(response);
    }

    @Test
    void rejectsStructuredJsonWildcardUntilConcreteRepresentationExists() {
        ThingifierApiConfig config = new ThingifierApiConfig("");

        ApiResponse response = new AcceptHeaderValidator(config).validate("application/*+json");

        Assertions.assertEquals(406, response.getStatusCode());
        Assertions.assertTrue(response.getErrorMessages().contains("Unrecognised Accept Type"));
    }

    @ParameterizedTest
    @MethodSource("unsupportedXmlMediaTypes")
    void rejectsUnsupportedXmlBasedMediaType(final String mediaType) {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        AcceptHeaderValidator validator = new AcceptHeaderValidator(config);

        ApiResponse response = validator.validate(mediaType);

        Assertions.assertEquals(406, response.getStatusCode(), mediaType);
        Assertions.assertTrue(
                response.getErrorMessages().contains("Unrecognised Accept Type"), mediaType);
    }

    @ParameterizedTest
    @MethodSource("xmlResponseMediaTypes")
    void xmlCompatibleResponseTypeHonorsDisabledXmlResponses(final String mediaType) {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        config.setApiToAllowXmlForResponses(false);
        AcceptHeaderValidator validator =
                new AcceptHeaderValidator(config, List.of("todo", "todos"));

        ApiResponse response = validator.validate(mediaType);

        Assertions.assertEquals(406, response.getStatusCode(), mediaType);
        Assertions.assertTrue(response.getErrorMessages().contains("XML not supported"));
    }

    @Test
    void qZeroCanFallThroughToSupportedAlternative() {
        ThingifierApiConfig config = new ThingifierApiConfig("");

        ApiResponse response =
                new AcceptHeaderValidator(config).validate("application/json;q=0, application/xml");

        Assertions.assertNull(response);
    }

    @Test
    void qZeroWithoutAlternativeIsNotAcceptable() {
        ThingifierApiConfig config = new ThingifierApiConfig("");

        ApiResponse response = new AcceptHeaderValidator(config).validate("application/json;q=0");

        Assertions.assertEquals(406, response.getStatusCode());
        Assertions.assertTrue(
                response.getErrorMessages().contains("No acceptable response type supported"));
    }

    @Test
    void qValuesChooseAllowedAlternativeEvenWhenItIsLowerInHeaderOrder() {
        ThingifierApiConfig config = new ThingifierApiConfig("");

        ApiResponse response =
                new AcceptHeaderValidator(config)
                        .validate("application/xml;q=0.1, application/json;q=0.9");

        Assertions.assertNull(response);
    }

    @Test
    void additionalJsonRepresentationsAreNotDisabledByJsonResponseConfig() {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        config.setApiToAllowJsonForResponses(false);

        ApiResponse response = new AcceptHeaderValidator(config).validate("application/json-seq");

        Assertions.assertNull(response);
    }

    private static Stream<String> additionalResponseRepresentationMediaTypes() {
        return Stream.of(
                "text/csv",
                "text/plain",
                "text/html",
                "application/x-ndjson",
                "application/jsonl",
                "application/json-seq",
                "text/tab-separated-values",
                "text/*");
    }

    private static Stream<String> xmlCompatibleResponseMediaTypes() {
        return Stream.of("application/xml", "text/xml");
    }

    private static Stream<String> modelMatchingStructuredXmlResponseMediaTypes() {
        return Stream.of("application/vnd.example.todo+xml", "application/*+xml");
    }

    private static Stream<String> unsupportedXmlMediaTypes() {
        return Stream.of(
                "application/problem+xml",
                "application/soap+xml",
                "application/xhtml+xml",
                "image/svg+xml",
                "application/atom+xml",
                "application/rss+xml");
    }

    private static Stream<String> xmlResponseMediaTypes() {
        return Stream.of("application/xml", "text/xml", "application/vnd.example.todo+xml");
    }
}
