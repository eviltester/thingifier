package uk.co.compendiumdev.thingifier.api.spec;

import static uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation.UPDATE;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.AUTO_INCREMENT;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

import io.swagger.v3.oas.models.OpenAPI;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteRegistry;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteVerb;
import uk.co.compendiumdev.thingifier.adapter.httpserver.ThingifierHttpApiRoutings;
import uk.co.compendiumdev.thingifier.api.callbacks.ThingifierApiOperationContext;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.validation.ApiOperationValidationContext;
import uk.co.compendiumdev.thingifier.api.validation.ApiOperationValidationResult;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.swaggerizer.Swaggerizer;

class ThingifierApiMountTest {

    @Test
    void visibleMountProjectsGeneratedRoutesIntoDocumentation() {
        final Thingifier thingifier = todoModel();
        thingifier.apiSpec().mount("api").at("/api").includeRoutes("/todos/**");

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("");

        Assertions.assertNotNull(route(definition, RoutingVerb.GET, "api/todos"));
        Assertions.assertTrue(routes(definition, RoutingVerb.GET, "todos").isEmpty());
    }

    @Test
    void includeRoutesLimitsMountedDocumentationToMatchingCanonicalRoutes() {
        final Thingifier thingifier = todoAndProjectModel();
        thingifier.apiSpec().mount("api").at("/api").includeRoutes("/todos/**");

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("");

        Assertions.assertNotNull(route(definition, RoutingVerb.GET, "api/todos"));
        Assertions.assertTrue(routes(definition, RoutingVerb.GET, "api/projects").isEmpty());
    }

    @Test
    void hiddenMountDoesNotCreateDocumentedAlias() {
        final Thingifier thingifier = todoModel();
        thingifier.apiSpec().mount("api").at("/api").includeRoutes("/todos/**");
        thingifier.apiSpec().mount("legacy").at("/").includeRoutes("/todos/**").hideFromDocs();

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("");

        Assertions.assertNotNull(route(definition, RoutingVerb.GET, "api/todos"));
        Assertions.assertTrue(routes(definition, RoutingVerb.GET, "todos").isEmpty());
    }

    @Test
    void openApiDocumentsMountedPublicPathOnly() {
        final Thingifier thingifier = todoModel();
        thingifier.apiSpec().mount("api").at("/api").includeRoutes("/todos/**");
        thingifier.apiSpec().mount("legacy").at("/").includeRoutes("/todos/**").hideFromDocs();
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.setThingifier(thingifier);
        apiDefn.setPathPrefix("");

        final OpenAPI openApi = new Swaggerizer(apiDefn).swagger();

        Assertions.assertNotNull(openApi.getPaths().get("/api/todos"));
        Assertions.assertNull(openApi.getPaths().get("/todos"));
    }

    @ParameterizedTest
    @CsvSource({
        "api/todos, /api/todos",
        "api/todos/:id, /api/todos/:id",
    })
    void mountedOptionsRouteDocumentationUsesPublicPath(
            final String routeUrl, final String documentedPath) {
        final Thingifier thingifier = todoModel();
        thingifier.apiSpec().mount("api").at("/api").includeRoutes("/todos/**");

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("");

        Assertions.assertEquals(
                "show all Options for endpoint of " + documentedPath,
                route(definition, RoutingVerb.OPTIONS, routeUrl).getDocumentation());
    }

    @ParameterizedTest
    @CsvSource({
        "/api/todos, /api/todos",
        "/api/todos/{id}, /api/todos/:id",
    })
    void openApiMountedOptionsSummaryUsesPublicPath(
            final String openApiPath, final String documentedPath) {
        final Thingifier thingifier = todoModel();
        thingifier.apiSpec().mount("api").at("/api").includeRoutes("/todos/**");
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.setThingifier(thingifier);
        apiDefn.setPathPrefix("");

        final OpenAPI openApi = new Swaggerizer(apiDefn).swagger();

        Assertions.assertEquals(
                "show all Options for endpoint of " + documentedPath,
                openApi.getPaths().get(openApiPath).getOptions().getSummary());
    }

    @Test
    void mountedFixedRoutesGenerateOptionsAllowHeader() {
        final Thingifier thingifier = secretModel();
        getSecretNoteRoute(thingifier);
        postSecretNoteRoute(thingifier);
        thingifier.apiSpec().mount("api").at("/api").includeRoutes("/secret/**");

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("");

        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, POST",
                route(definition, RoutingVerb.OPTIONS, "api/secret/note").headerValue());
    }

    @Test
    void mountedFixedOptionsRouteDocumentationUsesPublicPath() {
        final Thingifier thingifier = secretModel();
        getSecretNoteRoute(thingifier);
        thingifier.apiSpec().mount("api").at("/api").includeRoutes("/secret/**");

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("");

        Assertions.assertEquals(
                "return supported verbs for fixed route /api/secret/note",
                route(definition, RoutingVerb.OPTIONS, "api/secret/note").getDocumentation());
    }

    @Test
    void mountedFixedOptionsRouteIsRegisteredForHttpServer() {
        final Thingifier thingifier = secretModel();
        getSecretNoteRoute(thingifier);
        thingifier.apiSpec().mount("api").at("/api").includeRoutes("/secret/**");
        final HttpRouteRegistry registry = new HttpRouteRegistry();
        HttpRouteRegistry.use(registry);

        try {
            final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
            apiDefn.setThingifier(thingifier);
            apiDefn.setPathPrefix("");
            new ThingifierHttpApiRoutings(thingifier, apiDefn);

            Assertions.assertTrue(
                    registry.routes().stream()
                            .anyMatch(
                                    route ->
                                            route.verb() == HttpRouteVerb.OPTIONS
                                                    && route.path().equals("api/secret/note")));
        } finally {
            HttpRouteRegistry.clearCurrent();
        }
    }

    @Test
    void mountedFixedRouteUsesInternalTargetAndCallbackSeesPublicPath() {
        final Thingifier thingifier = secretModel();
        createSecretNote(thingifier, "note", "mounted note");
        final AtomicReference<ThingifierApiOperationContext> seenContext = new AtomicReference<>();
        getSecretNoteRoute(thingifier)
                .afterResponse((context, response) -> seenContext.set(context));
        thingifier
                .apiSpec()
                .mount("api")
                .at("/api")
                .includeRoutes("/secret/**")
                .rewriteLocationHeadersToMount();

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/api/secret/note"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("mounted note"));
        Assertions.assertEquals("/api/secret/note", seenContext.get().publicPath());
        Assertions.assertEquals("/secret/note", seenContext.get().internalPath());
        Assertions.assertEquals("/api/secret/note", seenContext.get().mountedPath());
        Assertions.assertEquals("api", seenContext.get().mountName().orElseThrow());
        Assertions.assertEquals("/api", seenContext.get().mountPrefix());
        Assertions.assertEquals("note", seenContext.get().targetIdentifier().orElseThrow());
    }

    @Test
    void mountedCreateRewritesRelativeLocationHeaderToActiveMount() {
        final Thingifier thingifier = todoModel();
        thingifier
                .apiSpec()
                .mount("api")
                .at("/api")
                .includeRoutes("/todos/**")
                .rewriteLocationHeadersToMount();

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(jsonPost("/api/todos", "{\"title\":\"new todo\"}"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertTrue(response.getHeaders().get("Location").startsWith("/api/todos/"));
    }

    @Test
    void mountedOperationValidatorReceivesPublicAndInternalPaths() {
        final Thingifier thingifier = todoModel();
        final AtomicReference<ApiOperationValidationContext> seenContext = new AtomicReference<>();
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/todos")
                .withApiOperationValidator(
                        "captureMountedPaths",
                        context -> {
                            seenContext.set(context);
                            return ApiOperationValidationResult.accept();
                        });
        thingifier.apiSpec().mount("api").at("/api").includeRoutes("/todos/**");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(jsonPost("/api/todos", "{\"title\":\"new todo\"}"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals("api/todos", seenContext.get().publicPath());
        Assertions.assertEquals("api/todos", seenContext.get().mountedPath());
        Assertions.assertEquals("todos", seenContext.get().internalPath());
        Assertions.assertEquals("api", seenContext.get().mountName());
        Assertions.assertEquals("/api", seenContext.get().mountPrefix());
    }

    private Thingifier todoModel() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition todo = thingifier.defineThing("todo", "todos", 5);
        todo.addAsPrimaryKeyField(Field.is("id", AUTO_INCREMENT));
        todo.addField(Field.is("title", STRING).makeMandatory());
        return thingifier;
    }

    private Thingifier todoAndProjectModel() {
        final Thingifier thingifier = todoModel();
        final EntityDefinition project = thingifier.defineThing("project", "projects", 5);
        project.addAsPrimaryKeyField(Field.is("id", AUTO_INCREMENT));
        project.addField(Field.is("name", STRING));
        return thingifier;
    }

    private Thingifier secretModel() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition note = thingifier.defineThing("secretnote", "secretnotes", 10);
        note.addAsPrimaryKeyField(Field.is("id", STRING));
        note.addField(Field.is("text", STRING));
        return thingifier;
    }

    private ThingifierApiRouteRule getSecretNoteRoute(final Thingifier thingifier) {
        return thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/secret/note")
                .mapsToEntity("secretnote")
                .withFixedIdentifier("note");
    }

    private ThingifierApiRouteRule postSecretNoteRoute(final Thingifier thingifier) {
        return thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/secret/note")
                .mapsToEntity("secretnote")
                .withFixedIdentifier("note")
                .entityCan(UPDATE);
    }

    private void createSecretNote(final Thingifier thingifier, final String id, final String text) {
        final EntityDefinition note = thingifier.getDefinitionNamed("secretnote");
        thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(note)
                                .withField("id", id)
                                .withField("text", text));
    }

    private HttpApiRequest jsonRequest(final String path) {
        return new HttpApiRequest(path).addHeader("Accept", "application/json");
    }

    private HttpApiRequest jsonPost(final String path, final String body) {
        return new HttpApiRequest(path)
                .setVerb("POST")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .setBody(body);
    }

    private RoutingDefinition route(
            final ApiRoutingDefinition definition, final RoutingVerb verb, final String url) {
        return definition.definitions().stream()
                .filter(route -> route.verb() == verb)
                .filter(route -> route.url().equals(url))
                .findFirst()
                .orElseThrow();
    }

    private List<RoutingDefinition> routes(
            final ApiRoutingDefinition definition, final RoutingVerb verb, final String url) {
        return definition.definitions().stream()
                .filter(route -> route.verb() == verb)
                .filter(route -> route.url().equals(url))
                .toList();
    }
}
