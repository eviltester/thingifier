package uk.co.compendiumdev.configuredwritemethods.application;

import static uk.co.compendiumdev.configuredwritemethods.application.ConfiguredWriteMethodsSampleApiTestSupport.documentation;
import static uk.co.compendiumdev.configuredwritemethods.application.ConfiguredWriteMethodsSampleApiTestSupport.patchFormatsHeader;
import static uk.co.compendiumdev.configuredwritemethods.application.ConfiguredWriteMethodsSampleApiTestSupport.route;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;

class ConfiguredWriteMethodsDocumentationTest {

    @Test
    void exposesPostCreateOnTheCollectionRoute() {
        final ApiRoutingDefinition definition = documentation();

        Assertions.assertTrue(
                route(definition, RoutingVerb.POST, "notes").status().isReturnedFromCall());
    }

    @Test
    void blocksPostUpdateOnTheInstanceRoute() {
        final ApiRoutingDefinition definition = documentation();

        Assertions.assertEquals(
                405, route(definition, RoutingVerb.POST, "notes/:id").status().value());
    }

    @Test
    void blocksPutAndPatchOnTheCollectionRoute() {
        final ApiRoutingDefinition definition = documentation();

        Assertions.assertEquals(405, route(definition, RoutingVerb.PUT, "notes").status().value());
        Assertions.assertEquals(
                405, route(definition, RoutingVerb.PATCH, "notes").status().value());
    }

    @Test
    void exposesPatchAndPutUpdateOnTheInstanceRoute() {
        final ApiRoutingDefinition definition = documentation();
        final RoutingDefinition patchRoute = route(definition, RoutingVerb.PATCH, "notes/:id");

        Assertions.assertTrue(patchRoute.status().isReturnedFromCall());
        Assertions.assertEquals(
                "patch a specific instance of note with a body containing the patch details",
                patchRoute.getDocumentation());
        Assertions.assertTrue(
                route(definition, RoutingVerb.PUT, "notes/:id").status().isReturnedFromCall());
    }

    @Test
    void collectionOptionsAllowOnlyTheCollectionMethods() {
        final ApiRoutingDefinition definition = documentation();

        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, POST, QUERY",
                route(definition, RoutingVerb.OPTIONS, "notes").headerValue());
    }

    @Test
    void instanceOptionsAllowOnlyTheInstanceMethods() {
        final ApiRoutingDefinition definition = documentation();

        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, PUT, PATCH, DELETE",
                route(definition, RoutingVerb.OPTIONS, "notes/:id").headerValue());
    }

    @Test
    void instanceOptionsExposeConfiguredPatchMediaTypes() {
        final ApiRoutingDefinition definition = documentation();

        Assertions.assertEquals(
                patchFormatsHeader(),
                route(definition, RoutingVerb.OPTIONS, "notes/:id")
                        .getResponseHeaderValue("Accept-Patch"));
    }
}
