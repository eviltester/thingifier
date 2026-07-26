package uk.co.compendiumdev.thingifier.htmlgui.htmlgen;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;

class RestApiDocumentationGeneratorTest {

    @Test
    void apiDocumentationShowsSwaggerUiLinkByDefault() {
        final Thingifier thingifier = new Thingifier();
        final ThingifierApiDocumentationDefn apiDocDefn = new ThingifierApiDocumentationDefn();

        final String docs =
                new RestApiDocumentationGenerator(thingifier, new DefaultGUIHTML())
                        .getApiDocumentation(
                                new ApiRoutingDefinition(),
                                List.of(),
                                apiDocDefn,
                                "/mirror",
                                "https://example.com/mirror/docs");

        Assertions.assertTrue(docs.contains("href='/mirror/docs/swagger-ui'"));
        Assertions.assertTrue(docs.contains("Open Swagger UI"));
        Assertions.assertTrue(docs.contains("<li>OpenAPI v 3.0 JSON"));
        Assertions.assertTrue(docs.contains("<li>OpenAPI v 3.1 JSON"));
        Assertions.assertTrue(docs.contains("<li>OpenAPI v 3.2 JSON"));
        Assertions.assertTrue(
                docs.contains("href='/mirror/docs/openapi-3.2.json'>[standard validation]</a>"));
        Assertions.assertTrue(docs.contains("href='/mirror/docs/openapi-3.2.json?download'"));
        Assertions.assertTrue(docs.contains("href='/mirror/docs/openapi-3.2.json?permissive'"));
        Assertions.assertTrue(
                docs.contains("href='/mirror/docs/openapi-3.2.json?permissive&amp;download'"));
        Assertions.assertFalse(docs.contains("download normal swagger file"));
        Assertions.assertFalse(docs.contains("download swagger file with less validation"));
        Assertions.assertFalse(docs.contains("Add <code>?download</code>"));
    }

    @Test
    void apiDocumentationCanHideSwaggerUiLink() {
        final Thingifier thingifier = new Thingifier();
        final ThingifierApiDocumentationDefn apiDocDefn =
                new ThingifierApiDocumentationDefn().setShowSwaggerUiLink(false);

        final String docs =
                new RestApiDocumentationGenerator(thingifier, new DefaultGUIHTML())
                        .getApiDocumentation(
                                new ApiRoutingDefinition(),
                                List.of(),
                                apiDocDefn,
                                "/mirror",
                                "https://example.com/mirror/docs");

        Assertions.assertFalse(docs.contains("href='/mirror/docs/swagger-ui'"));
        Assertions.assertFalse(docs.contains("Open Swagger UI"));
        Assertions.assertFalse(docs.contains("href='/mirror/docs/swagger'"));
        Assertions.assertTrue(docs.contains("<li>OpenAPI v 3.0 JSON"));
    }
}
