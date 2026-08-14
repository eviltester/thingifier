package uk.co.compendiumdev.thingifier.api.http.headers.headervalidator;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

class ContentTypeHeaderValidatorTest {

    @Test
    void acceptsSupportedXmlContentTypes() {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        ContentTypeHeaderValidator validator =
                new ContentTypeHeaderValidator(config, List.of("todo", "todos"));

        Assertions.assertNull(validator.validate("application/xml"));
        Assertions.assertNull(validator.validate("text/xml"));
    }

    @Test
    void acceptsModelMatchingStructuredXmlContentTypes() {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        ContentTypeHeaderValidator validator =
                new ContentTypeHeaderValidator(config, List.of("todo", "todos"));

        Assertions.assertNull(validator.validate("application/vnd.example.todo+xml"));
        Assertions.assertNull(
                validator.validate("application/vnd.example.todo+xml; charset=utf-8"));
    }

    @Test
    void rejectsUnsupportedXmlBasedContentTypes() {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        ContentTypeHeaderValidator validator = new ContentTypeHeaderValidator(config);

        for (String mediaType :
                List.of(
                        "application/problem+xml",
                        "application/soap+xml",
                        "application/xhtml+xml",
                        "image/svg+xml",
                        "application/atom+xml",
                        "application/rss+xml",
                        "application/*+xml")) {
            ApiResponse response = validator.validate(mediaType);

            Assertions.assertEquals(415, response.getStatusCode(), mediaType);
            Assertions.assertTrue(
                    response.getErrorMessages()
                            .contains("Unsupported Content Type - " + mediaType),
                    mediaType);
        }
    }

    @Test
    void textXmlDoesNotBypassDisabledXmlContentValidation() {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        config.setDefaultContentTypeAsJson(true);
        config.setApiToAllowXmlForContentType(false);
        ContentTypeHeaderValidator validator =
                new ContentTypeHeaderValidator(config, List.of("todo", "todos"));

        for (String mediaType :
                List.of("application/xml", "text/xml", "application/vnd.example.todo+xml")) {
            ApiResponse response = validator.validate(mediaType);

            Assertions.assertEquals(415, response.getStatusCode(), mediaType);
            Assertions.assertTrue(response.getErrorMessages().contains("XML Not Supported"));
        }
    }
}
