package uk.co.compendiumdev.configuredwritemethods.application;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.examples.ConfiguredWriteMethodsThingifier;
import uk.co.compendiumdev.thingifier.swaggerizer.Swaggerizer;

class ConfiguredWriteMethodsSampleApiTest {

    @Test
    void configuredMethodsAreEnforcedOverHttpApi() {
        final Thingifier thingifier = sample();
        final ThingifierHttpApi httpApi = new ThingifierHttpApi(thingifier);

        final HttpApiResponse create = httpApi.post(jsonRequest("notes", "POST", noteJson("One")));
        final String id = create.apiResponse().getReturnedInstance().getPrimaryKeyValue();

        Assertions.assertEquals(201, create.getStatusCode());
        Assertions.assertEquals(
                405,
                httpApi.post(jsonRequest("notes/" + id, "POST", "{\"title\":\"Blocked\"}"))
                        .getStatusCode());
        Assertions.assertEquals(
                200,
                httpApi.patch(jsonRequest("notes/" + id, "PATCH", "{\"title\":\"Patched\"}"))
                        .getStatusCode());
        Assertions.assertEquals(
                200,
                httpApi.put(jsonRequest("notes/" + id, "PUT", "{\"title\":\"Put\"}"))
                        .getStatusCode());
        Assertions.assertEquals(
                405,
                httpApi.put(jsonRequest("notes/999", "PUT", "{\"title\":\"Missing\"}"))
                        .getStatusCode());
    }

    @Test
    void directApiReceivesTheSamePolicyResponses() {
        final Thingifier thingifier = sample();
        final ApiResponse create = post(thingifier, "notes", noteJson("One"));
        final String id = create.getReturnedInstance().getPrimaryKeyValue();

        Assertions.assertEquals(201, create.getStatusCode());
        Assertions.assertEquals(
                405, post(thingifier, "notes/" + id, "{\"title\":\"Blocked\"}").getStatusCode());

        final ApiResponse patch =
                patch(thingifier, "notes/" + id, "{\"title\":\"Patched Direct\"}");
        Assertions.assertEquals(200, patch.getStatusCode());
        Assertions.assertEquals(
                "Patched Direct", patch.getReturnedInstance().getFieldValue("title").asString());

        final ApiResponse put = put(thingifier, "notes/" + id, "{\"title\":\"Put Direct\"}");
        Assertions.assertEquals(200, put.getStatusCode());
        Assertions.assertEquals(
                "Put Direct", put.getReturnedInstance().getFieldValue("title").asString());
        Assertions.assertEquals(
                405, put(thingifier, "notes/999", "{\"title\":\"Missing\"}").getStatusCode());
    }

    @Test
    void documentationAndSwaggerExposeOnlyAllowedWriteMethods() {
        final Thingifier thingifier = sample();
        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("");

        Assertions.assertTrue(
                route(definition, RoutingVerb.POST, "notes").status().isReturnedFromCall());
        Assertions.assertEquals(
                405, route(definition, RoutingVerb.POST, "notes/:id").status().value());
        Assertions.assertEquals(405, route(definition, RoutingVerb.PUT, "notes").status().value());
        Assertions.assertEquals(
                405, route(definition, RoutingVerb.PATCH, "notes").status().value());
        Assertions.assertTrue(
                route(definition, RoutingVerb.PATCH, "notes/:id").status().isReturnedFromCall());
        Assertions.assertTrue(
                route(definition, RoutingVerb.PUT, "notes/:id").status().isReturnedFromCall());
        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, POST, QUERY",
                route(definition, RoutingVerb.OPTIONS, "notes").headerValue());
        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, PUT, PATCH, DELETE",
                route(definition, RoutingVerb.OPTIONS, "notes/:id").headerValue());

        final OpenAPI openApi =
                new Swaggerizer(new ThingifierApiDocumentationDefn().setThingifier(thingifier))
                        .swagger();
        final PathItem collection = openApi.getPaths().get("/notes");
        final PathItem instance = openApi.getPaths().get("/notes/{id}");

        Assertions.assertNotNull(collection.getPost());
        Assertions.assertNull(collection.getPut());
        Assertions.assertNull(collection.getPatch());
        Assertions.assertNull(instance.getPost());
        Assertions.assertNotNull(instance.getPatch());
        Assertions.assertNotNull(instance.getPut());
    }

    private Thingifier sample() {
        return new ConfiguredWriteMethodsThingifier().get();
    }

    private ApiResponse post(final Thingifier thingifier, final String path, final String body) {
        return thingifier.api().post(path, parser(thingifier, body), new HttpHeadersBlock());
    }

    private ApiResponse put(final Thingifier thingifier, final String path, final String body) {
        return thingifier.api().put(path, parser(thingifier, body), new HttpHeadersBlock());
    }

    private ApiResponse patch(final Thingifier thingifier, final String path, final String body) {
        return thingifier.api().patch(path, parser(thingifier, body), new HttpHeadersBlock());
    }

    private BodyParser parser(final Thingifier thingifier, final String body) {
        return new BodyParser(
                new HttpApiRequest("/request").setBody(body), thingifier.getThingNames());
    }

    private HttpApiRequest jsonRequest(final String path, final String verb, final String body) {
        return new HttpApiRequest(path)
                .setVerb(verb)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .setBody(body);
    }

    private String noteJson(final String title) {
        return "{\"title\":\"" + title + "\",\"description\":\"sample\"}";
    }

    private RoutingDefinition route(
            final ApiRoutingDefinition definition, final RoutingVerb verb, final String url) {
        return definition.definitions().stream()
                .filter(route -> route.verb() == verb)
                .filter(route -> route.url().equals(url))
                .findFirst()
                .orElseThrow();
    }
}
