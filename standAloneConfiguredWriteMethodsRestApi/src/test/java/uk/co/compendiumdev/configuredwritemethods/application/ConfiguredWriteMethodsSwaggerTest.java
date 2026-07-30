package uk.co.compendiumdev.configuredwritemethods.application;

import static uk.co.compendiumdev.configuredwritemethods.application.ConfiguredWriteMethodsSampleApiTestSupport.swagger;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.JSON_MERGE_PATCH_RFC7396;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.JSON_PATCH_RFC6902;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.PARTIAL_JSON_UPDATE;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfiguredWriteMethodsSwaggerTest {

    @Test
    void exposesPostCreateOnlyOnTheCollectionRoute() {
        final OpenAPI openApi = swagger();
        final PathItem collection = openApi.getPaths().get("/notes");

        Assertions.assertNotNull(collection.getPost());
        Assertions.assertNull(collection.getPut());
        Assertions.assertNull(collection.getPatch());
    }

    @Test
    void exposesPatchAndPutUpdateOnlyOnTheInstanceRoute() {
        final OpenAPI openApi = swagger();
        final PathItem instance = openApi.getPaths().get("/notes/{id}");

        Assertions.assertNull(instance.getPost());
        Assertions.assertNotNull(instance.getPatch());
        Assertions.assertNotNull(instance.getPut());
        Assertions.assertEquals(
                "patch a specific instance of note with a body containing the patch details",
                instance.getPatch().getSummary());
        Assertions.assertEquals(
                "patch a specific instance of note with a body containing the patch details",
                instance.getPatch().getDescription());
    }

    @Test
    void patchRequestBodyExposesAllConfiguredPatchMediaTypes() {
        final OpenAPI openApi = swagger();
        final PathItem instance = openApi.getPaths().get("/notes/{id}");

        Assertions.assertTrue(
                instance.getPatch()
                        .getRequestBody()
                        .getContent()
                        .containsKey(PARTIAL_JSON_UPDATE.mediaType()));
        Assertions.assertTrue(
                instance.getPatch()
                        .getRequestBody()
                        .getContent()
                        .containsKey(JSON_MERGE_PATCH_RFC7396.mediaType()));
        Assertions.assertTrue(
                instance.getPatch()
                        .getRequestBody()
                        .getContent()
                        .containsKey(JSON_PATCH_RFC6902.mediaType()));
    }
}
