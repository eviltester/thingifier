package uk.co.compendiumdev.thingifier.swaggerizer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

class SwaggerUiPageTest {

    @Test
    void includesCopyForAiAssetsAndOpenApiConfiguration() {
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.setSwaggerUiTitle("Thingifier API");

        final String html =
                new SwaggerUiPage(
                                apiDefn,
                                new DefaultGUIHTML(),
                                "/mirror/docs/openapi.json",
                                "/mirror/docs",
                                "/mirror/docs/swagger",
                                "/mirror/docs/swagger-ui")
                        .html();

        Assertions.assertTrue(html.contains("/css/swagger-copy-for-ai.css"));
        Assertions.assertTrue(html.contains("/js/swagger-copy-for-ai.js"));
        Assertions.assertTrue(html.contains("window.thingifierSwaggerCopyForAi"));
        Assertions.assertTrue(html.contains("openApiUrl: \"/mirror/docs/openapi.json\""));
    }
}
