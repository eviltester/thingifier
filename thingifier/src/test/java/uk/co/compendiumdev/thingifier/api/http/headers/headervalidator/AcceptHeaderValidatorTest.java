package uk.co.compendiumdev.thingifier.api.http.headers.headervalidator;

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
    }

    @Test
    void rejectsUnsupportedHeaderWhenEnforced() {
        ThingifierApiConfig config = new ThingifierApiConfig("");

        ApiResponse response = new AcceptHeaderValidator(config).validate("text/*");

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
    void additionalJsonRepresentationsAreNotDisabledByJsonResponseConfig() {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        config.setApiToAllowJsonForResponses(false);

        ApiResponse response = new AcceptHeaderValidator(config).validate("application/json-seq");

        Assertions.assertNull(response);
    }
}
