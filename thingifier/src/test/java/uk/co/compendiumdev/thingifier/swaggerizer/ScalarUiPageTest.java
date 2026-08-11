package uk.co.compendiumdev.thingifier.swaggerizer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

class ScalarUiPageTest {

    @Test
    void includesScalarAssetsAndOpenApiConfiguration() {
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.setTitle("Thingifier API");

        final String html = scalarHtml(apiDefn);

        Assertions.assertTrue(html.contains("https://cdn.jsdelivr.net/npm/@scalar/api-reference"));
        Assertions.assertTrue(html.contains("Scalar.createApiReference"));
        Assertions.assertTrue(html.contains("#scalar-api-reference"));
        Assertions.assertTrue(html.contains("sources: ["));
        Assertions.assertTrue(html.contains("url: \"/mirror/docs/openapi.json\""));
        Assertions.assertTrue(html.contains("title: \"OpenAPI 3.1 default\""));
        Assertions.assertTrue(html.contains("default: true"));
        Assertions.assertTrue(html.contains("scalarThingifierIsLocalHost"));
        Assertions.assertTrue(html.contains("\"localhost\", \"127.0.0.1\", \"0.0.0.0\", \"::1\""));
        Assertions.assertTrue(html.contains("const scalarThingifierHideClientButton = false;"));
        Assertions.assertTrue(
                html.contains(
                        "hideClientButton: scalarThingifierHideClientButton ||"
                                + " scalarThingifierIsLocalHost"));
        Assertions.assertTrue(html.contains("showDeveloperTools: \"never\""));
        Assertions.assertTrue(html.contains("/mirror/docs/openapi-3.0.json"));
        Assertions.assertTrue(html.contains("/mirror/docs/openapi-3.1.json"));
        Assertions.assertTrue(html.contains("/mirror/docs/openapi-3.2.json"));
        Assertions.assertTrue(html.contains("agent: {disabled: true}"));
        Assertions.assertTrue(html.contains("Thingifier API Scalar UI"));
    }

    @Test
    void canHideClientButtonForAllHosts() {
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.scalarUi().hideClientButton(true);

        final String html = scalarHtml(apiDefn);

        Assertions.assertTrue(html.contains("const scalarThingifierHideClientButton = true;"));
        Assertions.assertTrue(
                html.contains(
                        "hideClientButton: scalarThingifierHideClientButton ||"
                                + " scalarThingifierIsLocalHost"));
    }

    @Test
    void canShowDeveloperTools() {
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.scalarUi().showDeveloperTools(true);

        final String html = scalarHtml(apiDefn);

        Assertions.assertTrue(html.contains("showDeveloperTools: \"always\""));
    }

    private String scalarHtml(final ThingifierApiDocumentationDefn apiDefn) {
        return new ScalarUiPage(
                        apiDefn,
                        new DefaultGUIHTML(),
                        "/mirror/docs/openapi.json",
                        "/mirror/docs/openapi-3.0.json",
                        "/mirror/docs/openapi-3.1.json",
                        "/mirror/docs/openapi-3.2.json",
                        "/mirror/docs",
                        "/mirror/docs/scalar-ui")
                .html();
    }
}
