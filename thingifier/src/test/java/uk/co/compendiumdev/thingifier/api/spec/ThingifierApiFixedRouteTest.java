package uk.co.compendiumdev.thingifier.api.spec;

import static uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation.UPDATE;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleHookRegistry;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteRegistry;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteVerb;
import uk.co.compendiumdev.thingifier.adapter.httpserver.ThingifierHttpApiRoutings;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticationContext;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticationResult;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthorizationResult;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.swaggerizer.Swaggerizer;

class ThingifierApiFixedRouteTest {

    @Test
    void fixedRouteDefinitionUsesPublicPathWithoutUrlParameter() {
        final Thingifier thingifier = secretModel();
        thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/secret/note")
                .mapsToEntity("secretnote")
                .withFixedIdentifier("note")
                .defaultEntityView("SecretNoteResponse");

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("");
        final RoutingDefinition route = route(definition, RoutingVerb.GET, "secret/note");

        Assertions.assertTrue(route.hasFixedIdentifierMapping());
        Assertions.assertEquals("secretnote", route.fixedEntityName());
        Assertions.assertEquals("note", route.fixedIdentifier());
        Assertions.assertFalse(route.hasRequestUrlParams());
        Assertions.assertEquals("SecretNoteResponse", route.getReturnPayloadFor(200));
    }

    @Test
    void fixedRouteDefinitionKeepsConfiguredApiPrefix() {
        final Thingifier thingifier = secretModel();
        thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/secret/note")
                .mapsToEntity("secretnote")
                .withFixedIdentifier("note");

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api");

        Assertions.assertNotNull(route(definition, RoutingVerb.GET, "api/secret/note"));
    }

    @Test
    void fixedGetRouteGeneratesHeadDefinition() {
        final Thingifier thingifier = secretModel();
        getSecretNoteRoute(thingifier);

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("");
        final RoutingDefinition route = route(definition, RoutingVerb.HEAD, "secret/note");

        Assertions.assertTrue(route.hasFixedIdentifierMapping());
        Assertions.assertEquals("secretnote", route.fixedEntityName());
        Assertions.assertEquals("note", route.fixedIdentifier());
        Assertions.assertFalse(route.hasRequestUrlParams());
    }

    @Test
    void fixedGetRouteGeneratesOptionsAllowHeader() {
        final Thingifier thingifier = secretModel();
        getSecretNoteRoute(thingifier);

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("");
        final RoutingDefinition route = route(definition, RoutingVerb.OPTIONS, "secret/note");

        Assertions.assertEquals("Allow", route.header());
        Assertions.assertEquals("OPTIONS, GET, HEAD", route.headerValue());
    }

    @Test
    void fixedRoutesAtSamePublicPathShareOptionsAllowHeader() {
        final Thingifier thingifier = secretModel();
        getSecretNoteRoute(thingifier);
        postSecretNoteRoute(thingifier);

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("");
        final List<RoutingDefinition> routes =
                routes(definition, RoutingVerb.OPTIONS, "secret/note");

        Assertions.assertEquals(1, routes.size());
        Assertions.assertEquals("OPTIONS, GET, HEAD, POST", routes.get(0).headerValue());
    }

    @Test
    void fixedOptionsRouteIsRegisteredForHttpServer() {
        final Thingifier thingifier = secretModel();
        getSecretNoteRoute(thingifier);
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
                                                    && route.path().equals("secret/note")));
        } finally {
            HttpRouteRegistry.clearCurrent();
        }
    }

    @Test
    void methodNotAllowedFixedRouteIsNotAdvertisedInOptionsAllowHeader() {
        final Thingifier thingifier = secretModel();
        getSecretNoteRoute(thingifier);
        postSecretNoteRoute(thingifier).methodNotAllowed();

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("");

        Assertions.assertEquals(
                "OPTIONS, GET, HEAD",
                route(definition, RoutingVerb.OPTIONS, "secret/note").headerValue());
    }

    @Test
    void openApiDocumentsFixedRouteWithoutIdentifierParameter() {
        final Thingifier thingifier = secretModel();
        thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/secret/note")
                .mapsToEntity("secretnote")
                .withFixedIdentifier("note");
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.setThingifier(thingifier);
        apiDefn.setPathPrefix("/api");

        final OpenAPI openApi = new Swaggerizer(apiDefn).swagger();
        final PathItem path = openApi.getPaths().get("/api/secret/note");

        Assertions.assertNotNull(path);
        final Operation get = path.getGet();
        Assertions.assertTrue(path.getParameters() == null || path.getParameters().isEmpty());
        Assertions.assertTrue(get.getParameters() == null || get.getParameters().isEmpty());
    }

    @Test
    void getFixedRouteReturnsConfiguredInstance() {
        final Thingifier thingifier = secretModel();
        getSecretNoteRoute(thingifier);
        createSecretNote(thingifier, "note", "stored text", "internal-token");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secret/note"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "note", response.apiResponse().getReturnedInstance().getPrimaryKeyValue());
        Assertions.assertTrue(response.getBody().contains("stored text"));
    }

    @Test
    void routeResponseViewHidesInternalFieldsForFixedRoute() {
        final Thingifier thingifier = secretModel();
        getSecretNoteRoute(thingifier).defaultEntityView("SecretNoteResponse");
        createSecretNote(thingifier, "note", "visible text", "internal-token");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secret/note"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("visible text"));
        Assertions.assertFalse(response.getBody().contains("internal"));
        Assertions.assertFalse(response.getBody().contains("internal-token"));
    }

    @Test
    void getFixedRouteCanUseIdentifierThatMatchesAnotherEntityName() {
        final Thingifier thingifier = secretModel();
        thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/secret/token")
                .mapsToEntity("secretnote")
                .withFixedIdentifier("token");
        createSecretNote(thingifier, "token", "token-shaped note", "internal-token");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secret/token"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "token", response.apiResponse().getReturnedInstance().getPrimaryKeyValue());
        Assertions.assertTrue(response.getBody().contains("token-shaped note"));
    }

    @Test
    void headFixedRouteUsesConfiguredInstance() {
        final Thingifier thingifier = secretModel();
        thingifier
                .apiSpec()
                .route(RoutingVerb.HEAD, "/secret/note")
                .mapsToEntity("secretnote")
                .withFixedIdentifier("note");
        createSecretNote(thingifier, "note", "stored text", "internal-token");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).head(jsonRequest("/secret/note"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("", response.getBody());
        Assertions.assertFalse(response.getHeaders().get("Content-Length").isEmpty());
    }

    @Test
    void headUsesFixedGetRouteWhenHeadIsNotDeclared() {
        final Thingifier thingifier = secretModel();
        getSecretNoteRoute(thingifier);
        createSecretNote(thingifier, "note", "stored text", "internal-token");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).head(jsonRequest("/secret/note"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("", response.getBody());
    }

    @Test
    void missingFixedRouteReturns404ByDefault() {
        final Thingifier thingifier = secretModel();
        getSecretNoteRoute(thingifier);

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secret/note"));

        Assertions.assertEquals(404, response.getStatusCode());
    }

    @Test
    void postFixedRouteUpdatesConfiguredInstance() {
        final Thingifier thingifier = secretModel();
        postSecretNoteRoute(thingifier);
        createSecretNote(thingifier, "note", "before", "internal-token");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(jsonPost("/secret/note", "{\"text\":\"after\"}"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "after", secretNote(thingifier, "note").getFieldValue("text").asString());
    }

    @Test
    void postMissingFixedRouteReturns404ByDefault() {
        final Thingifier thingifier = secretModel();
        postSecretNoteRoute(thingifier);

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(jsonPost("/secret/note", "{\"text\":\"after\"}"));

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertNull(secretNote(thingifier, "note"));
    }

    @Test
    void postFixedRouteRejectsConflictingIdentifier() {
        final Thingifier thingifier = secretModel();
        postSecretNoteRoute(thingifier);
        createSecretNote(thingifier, "note", "before", "internal-token");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(jsonPost("/secret/note", "{\"id\":\"token\",\"text\":\"after\"}"));

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertEquals(
                "before", secretNote(thingifier, "note").getFieldValue("text").asString());
    }

    @Test
    void fixedRouteHonoursAuthSelectedDataScope() {
        final Thingifier thingifier = secretModel();
        createSecretNote(thingifier, "note", "default text", "default-token");
        createSecretNote(thingifier, "challenger", "note", "tenant text", "tenant-token");
        thingifier.apiSpec().authenticator("secretNoteToken", this::tenantTokenAuthenticator);
        getSecretNoteRoute(thingifier).secureWithBearerAuth("secretNoteToken");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .get(
                                jsonRequest("/secret/note")
                                        .addHeader("Authorization", "Bearer valid-token"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("tenant text"));
        Assertions.assertFalse(response.getBody().contains("default text"));
    }

    @Test
    void authorizerReceivesFixedTargetDetails() {
        final Thingifier thingifier = secretModel();
        createSecretNote(thingifier, "challenger", "note", "tenant text", "tenant-token");
        final AtomicReference<String> seenEntity = new AtomicReference<>();
        final AtomicReference<String> seenIdentifier = new AtomicReference<>();
        final AtomicReference<String> seenDataScope = new AtomicReference<>();
        thingifier.apiSpec().authenticator("secretNoteToken", this::tenantTokenAuthenticator);
        getSecretNoteRoute(thingifier)
                .secureWithBearerAuth("secretNoteToken")
                .authorizeWith(
                        context -> {
                            seenEntity.set(context.targetEntity().getName());
                            seenIdentifier.set(context.targetIdentifier());
                            seenDataScope.set(context.dataScopeName());
                            return ThingifierApiAuthorizationResult.authorized();
                        });

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .get(
                                jsonRequest("/secret/note")
                                        .addHeader("Authorization", "Bearer valid-token"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("secretnote", seenEntity.get());
        Assertions.assertEquals("note", seenIdentifier.get());
        Assertions.assertEquals("challenger", seenDataScope.get());
    }

    @Test
    void bodyParsedHookReceivesFixedRouteDetails() {
        final Thingifier thingifier = secretModel();
        postSecretNoteRoute(thingifier);
        createSecretNote(thingifier, "note", "before", "internal-token");
        final AtomicReference<String> seenEntity = new AtomicReference<>();
        final AtomicReference<String> seenIdentifier = new AtomicReference<>();
        final ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        hooks.registerBodyParsedHook(
                context -> {
                    seenEntity.set(context.targetEntity().getName());
                    seenIdentifier.set(context.targetIdentifier());
                });

        final HttpApiResponse response =
                ThingifierHttpApi.withHookRegistries(thingifier, null, hooks)
                        .post(jsonPost("/secret/note", "{\"text\":\"after\"}"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("secretnote", seenEntity.get());
        Assertions.assertEquals("note", seenIdentifier.get());
    }

    @Test
    void ensureExistsCreatesMissingFixedResource() {
        final Thingifier thingifier = secretModel();
        thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/secret/note")
                .mapsToEntity("secretnote")
                .withFixedIdentifier("note", FixedResourcePolicy.ENSURE_EXISTS);

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secret/note"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("note", secretNote(thingifier, "note").getPrimaryKeyValue());
    }

    @Test
    void generatedInstanceRouteStillWorksAlongsideFixedRoute() {
        final Thingifier thingifier = secretModel();
        getSecretNoteRoute(thingifier);
        createSecretNote(thingifier, "note", "generated route text", "internal-token");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secretnotes/note"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("generated route text"));
    }

    @Test
    void fixedIdentifierRejectsParameterizedRoutePaths() {
        final Thingifier thingifier = secretModel();

        final IllegalArgumentException thrown =
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                thingifier
                                        .apiSpec()
                                        .route(RoutingVerb.GET, "/secret/{kind}")
                                        .mapsToEntity("secretnote")
                                        .withFixedIdentifier("note"));

        Assertions.assertEquals(
                "fixed identifier routes must not contain path parameters", thrown.getMessage());
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

    private ThingifierApiAuthenticationResult tenantTokenAuthenticator(
            final ThingifierApiAuthenticationContext context) {
        if ("valid-token".equals(context.bearerToken())) {
            return ThingifierApiAuthenticationResult.authenticated("tenant-principal")
                    .useDataScope("challenger");
        }
        return ThingifierApiAuthenticationResult.rejected("Invalid bearer token");
    }

    private Thingifier secretModel() {
        final Thingifier thingifier = new Thingifier();

        final EntityDefinition note = thingifier.defineThing("secretnote", "secretnotes", 10);
        note.addAsPrimaryKeyField(Field.is("id", FieldType.STRING));
        note.addField(Field.is("text", FieldType.STRING));
        note.addField(Field.is("internal", FieldType.STRING));
        note.defineView("SecretNoteResponse").hideResponseFields("internal");

        final EntityDefinition token = thingifier.defineThing("token", "tokens", 10);
        token.addAsPrimaryKeyField(Field.is("id", FieldType.STRING));
        token.addField(Field.is("value", FieldType.STRING));

        return thingifier;
    }

    private EntityInstance createSecretNote(
            final Thingifier thingifier,
            final String id,
            final String text,
            final String internal) {
        return createSecretNote(
                thingifier, EntityRelModel.DEFAULT_DATABASE_NAME, id, text, internal);
    }

    private EntityInstance createSecretNote(
            final Thingifier thingifier,
            final String dataScopeName,
            final String id,
            final String text,
            final String internal) {
        ensureDataScopeExists(thingifier, dataScopeName);
        final EntityDefinition note = thingifier.getDefinitionNamed("secretnote");
        return storeFor(thingifier, dataScopeName)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(note)
                                .withField("id", id)
                                .withField("text", text)
                                .withField("internal", internal));
    }

    private EntityInstance secretNote(final Thingifier thingifier, final String id) {
        return secretNote(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME, id);
    }

    private EntityInstance secretNote(
            final Thingifier thingifier, final String dataScopeName, final String id) {
        return storeFor(thingifier, dataScopeName)
                .entityQueries()
                .findByPrimaryKey(thingifier.getDefinitionNamed("secretnote"), id);
    }

    private ThingStore storeFor(final Thingifier thingifier, final String dataScopeName) {
        return thingifier.getStore(dataScopeName);
    }

    private void ensureDataScopeExists(final Thingifier thingifier, final String dataScopeName) {
        thingifier.getERmodel().createInstanceDatabaseIfNotExisting(dataScopeName);
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
