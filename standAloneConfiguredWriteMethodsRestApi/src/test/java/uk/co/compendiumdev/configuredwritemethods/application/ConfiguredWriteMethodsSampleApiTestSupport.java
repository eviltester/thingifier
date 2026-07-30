package uk.co.compendiumdev.configuredwritemethods.application;

import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.JSON_MERGE_PATCH_RFC7396;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.JSON_PATCH_RFC6902;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.PARTIAL_JSON_UPDATE;

import io.swagger.v3.oas.models.OpenAPI;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.examples.ConfiguredWriteMethodsThingifier;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.swaggerizer.Swaggerizer;

final class ConfiguredWriteMethodsSampleApiTestSupport {

    private ConfiguredWriteMethodsSampleApiTestSupport() {}

    static Thingifier sample() {
        return new ConfiguredWriteMethodsThingifier().get();
    }

    static ThingifierHttpApi httpApi() {
        return httpApiFor(sample());
    }

    static ThingifierHttpApi httpApiFor(final Thingifier thingifier) {
        return new ThingifierHttpApi(thingifier);
    }

    static ApiRoutingDefinition documentation() {
        return new ApiRoutingDefinitionDocGenerator(sample()).generate("");
    }

    static OpenAPI swagger() {
        return new Swaggerizer(new ThingifierApiDocumentationDefn().setThingifier(sample()))
                .swagger();
    }

    static EntityInstance createStoredNote(final Thingifier thingifier) {
        final EntityDefinition note = thingifier.getDefinitionNamed("note");
        return thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(note)
                                .withField("title", "Existing")
                                .withField("description", "sample"));
    }

    static EntityInstance currentNote(final Thingifier thingifier, final String id) {
        return thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entityQueries()
                .findByPrimaryKey(thingifier.getDefinitionNamed("note"), id);
    }

    static ApiResponse post(final Thingifier thingifier, final String path, final String body) {
        return thingifier.api().post(path, parser(thingifier, body), new HttpHeadersBlock());
    }

    static ApiResponse put(final Thingifier thingifier, final String path, final String body) {
        return thingifier.api().put(path, parser(thingifier, body), new HttpHeadersBlock());
    }

    static ApiResponse patch(final Thingifier thingifier, final String path, final String body) {
        return patch(thingifier, path, body, PARTIAL_JSON_UPDATE.mediaType());
    }

    static ApiResponse patch(
            final Thingifier thingifier,
            final String path,
            final String body,
            final String contentType) {
        HttpHeadersBlock headers = new HttpHeadersBlock();
        headers.put("Content-Type", contentType);
        return thingifier.api().patch(path, body, headers);
    }

    static HttpApiRequest jsonRequest(final String path, final String verb, final String body) {
        return jsonRequest(path, verb, body, PARTIAL_JSON_UPDATE.mediaType());
    }

    static HttpApiRequest jsonRequest(
            final String path, final String verb, final String body, final String contentType) {
        return new HttpApiRequest(path)
                .setVerb(verb)
                .addHeader("Content-Type", contentType)
                .addHeader("Accept", "application/json")
                .setBody(body);
    }

    static String noteJson(final String title) {
        return "{\"title\":\"" + title + "\",\"description\":\"sample\"}";
    }

    static String patchFormatsHeader() {
        return String.join(
                ", ",
                PARTIAL_JSON_UPDATE.mediaType(),
                JSON_MERGE_PATCH_RFC7396.mediaType(),
                JSON_PATCH_RFC6902.mediaType());
    }

    static RoutingDefinition route(
            final ApiRoutingDefinition definition, final RoutingVerb verb, final String url) {
        return definition.definitions().stream()
                .filter(route -> route.verb() == verb)
                .filter(route -> route.url().equals(url))
                .findFirst()
                .orElseThrow();
    }

    private static BodyParser parser(final Thingifier thingifier, final String body) {
        return new BodyParser(
                new HttpApiRequest("/request").setBody(body), thingifier.getThingNames());
    }
}
