package uk.co.compendiumdev.thingifier.api.http.headers.headervalidator;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

class AcceptHeaderValidatorTest {

    @Test
    void acceptsAdditionalResponseRepresentations() {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        AcceptHeaderValidator validator = new AcceptHeaderValidator(config);

        Assertions.assertNull(validator.validate("text/csv"));
        Assertions.assertNull(validator.validate("text/plain"));
        Assertions.assertNull(validator.validate("text/html"));
        Assertions.assertNull(validator.validate("application/x-ndjson"));
        Assertions.assertNull(validator.validate("application/jsonl"));
        Assertions.assertNull(validator.validate("application/json-seq"));
        Assertions.assertNull(validator.validate("text/tab-separated-values"));
        Assertions.assertNull(validator.validate("text/*"));
    }

    @Test
    void acceptsXmlCompatibleResponseRepresentations() {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        AcceptHeaderValidator validator = new AcceptHeaderValidator(config);

        Assertions.assertNull(validator.validate("application/xml"));
        Assertions.assertNull(validator.validate("text/xml"));
    }

    @Test
    void acceptsModelMatchingStructuredXmlResponseRepresentations() {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        AcceptHeaderValidator validator =
                new AcceptHeaderValidator(config, List.of("todo", "todos"));

        Assertions.assertNull(validator.validate("application/vnd.example.todo+xml"));
        Assertions.assertNull(validator.validate("application/*+xml"));
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

    @Test
    void rejectsUnsupportedXmlBasedMediaTypes() {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        AcceptHeaderValidator validator = new AcceptHeaderValidator(config);

        for (String mediaType :
                List.of(
                        "application/problem+xml",
                        "application/soap+xml",
                        "application/xhtml+xml",
                        "image/svg+xml",
                        "application/atom+xml",
                        "application/rss+xml")) {
            ApiResponse response = validator.validate(mediaType);

            Assertions.assertEquals(406, response.getStatusCode(), mediaType);
            Assertions.assertTrue(
                    response.getErrorMessages().contains("Unrecognised Accept Type"), mediaType);
        }
    }

    @Test
    void xmlCompatibleResponseTypesHonorDisabledXmlResponses() {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        config.setApiToAllowXmlForResponses(false);
        AcceptHeaderValidator validator =
                new AcceptHeaderValidator(config, List.of("todo", "todos"));

        for (String mediaType :
                List.of("application/xml", "text/xml", "application/vnd.example.todo+xml")) {
            ApiResponse response = validator.validate(mediaType);

            Assertions.assertEquals(406, response.getStatusCode(), mediaType);
            Assertions.assertTrue(response.getErrorMessages().contains("XML not supported"));
        }
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
}
