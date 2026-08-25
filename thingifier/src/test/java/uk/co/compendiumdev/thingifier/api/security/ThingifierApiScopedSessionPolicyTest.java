package uk.co.compendiumdev.thingifier.api.security;

import static uk.co.compendiumdev.thingifier.api.security.DataScopeCreationPolicy.ENSURE_EXISTS;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleHookRegistry;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

class ThingifierApiScopedSessionPolicyTest {

    @Test
    void missingCredentialOnAnonymousReadUsesDefaultScope() {
        final Thingifier thingifier = todoModel();
        createTodo(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME, "default todo");
        createTodo(thingifier, "tenant-one", "tenant todo");
        final AtomicInteger resolverCalls = new AtomicInteger();
        scopedSession(thingifier)
                .authenticateWith(
                        context -> {
                            resolverCalls.incrementAndGet();
                            return ThingifierApiScopedSessionResult.unauthenticated();
                        })
                .allowAnonymousDefaultScopeForReads();

        final ApiResponse response =
                thingifier
                        .api()
                        .get("todos", new QueryFilterParams(), headersWithSession("tenant-one"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("default todo", firstReturnedTodoTitle(response));
        Assertions.assertEquals(0, resolverCalls.get());
    }

    @Test
    void missingCredentialOnAnonymousReadUsesConfiguredScope() {
        final Thingifier thingifier = todoModel();
        createTodo(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME, "default todo");
        createTodo(thingifier, "anonymous-scope", "anonymous todo");
        scopedSession(thingifier)
                .authenticateWith(this::validScopedSession)
                .allowAnonymousReadsUsingDataScope("anonymous-scope");

        final ApiResponse response =
                thingifier
                        .api()
                        .get("todos", new QueryFilterParams(), headersWithSession("tenant-one"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("anonymous todo", firstReturnedTodoTitle(response));
    }

    @Test
    void anonymousReadResolverChoosesDataScope() {
        final Thingifier thingifier = todoModel();
        createTodo(thingifier, "anonymous-scope", "anonymous todo");
        final AtomicReference<String> seenPath = new AtomicReference<>();
        scopedSession(thingifier)
                .authenticateWith(this::validScopedSession)
                .allowAnonymousReadsUsingDataScope(
                        context -> {
                            seenPath.set(context.path());
                            return ThingifierApiDataScopeSelection.useDataScope("anonymous-scope");
                        });

        final ApiResponse response =
                thingifier.api().get("todos", new QueryFilterParams(), new HttpHeadersBlock());

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("todos", seenPath.get());
        Assertions.assertEquals("anonymous todo", firstReturnedTodoTitle(response));
    }

    @Test
    void anonymousReadEnsuresConfiguredScopeExists() {
        final Thingifier thingifier = todoModel();
        scopedSession(thingifier)
                .authenticateWith(this::validScopedSession)
                .allowAnonymousReadsUsingDataScope("anonymous-scope", ENSURE_EXISTS);

        final ApiResponse response =
                thingifier.api().get("todos", new QueryFilterParams(), new HttpHeadersBlock());

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(thingifier.getStore("anonymous-scope"));
        Assertions.assertEquals(0, todoCount(thingifier, "anonymous-scope"));
    }

    @Test
    void anonymousReadMissingConfiguredScopeReturns404() {
        final Thingifier thingifier = todoModel();
        scopedSession(thingifier)
                .authenticateWith(this::validScopedSession)
                .allowAnonymousReadsUsingDataScope("anonymous-scope");

        final ApiResponse response =
                thingifier.api().get("todos", new QueryFilterParams(), new HttpHeadersBlock());

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals(
                "Could not find data scope anonymous-scope",
                response.getErrorMessages().iterator().next());
    }

    @Test
    void routeLevelDefaultScopeOverrideIgnoresConfiguredAnonymousScope() {
        final Thingifier thingifier = todoModel();
        createTodo(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME, "default todo");
        createTodo(thingifier, "anonymous-scope", "anonymous todo");
        scopedSession(thingifier)
                .authenticateWith(this::validScopedSession)
                .allowAnonymousReadsUsingDataScope("anonymous-scope");
        thingifier.apiSpec().route(RoutingVerb.GET, "/todos").allowAnonymousUsingDefaultScope();

        final ApiResponse response =
                thingifier.api().get("todos", new QueryFilterParams(), new HttpHeadersBlock());

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("default todo", firstReturnedTodoTitle(response));
    }

    @Test
    void validHeaderCredentialOnReadUsesSelectedDataScope() {
        final Thingifier thingifier = todoModel();
        createTodo(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME, "default todo");
        createTodo(thingifier, "tenant-one", "tenant todo");
        scopedSession(thingifier)
                .authenticateWith(this::validScopedSession)
                .allowAnonymousDefaultScopeForReads();

        final ApiResponse response =
                thingifier
                        .api()
                        .get(
                                "todos",
                                new QueryFilterParams(),
                                headersWithScopedCredential("valid-session"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("tenant todo", firstReturnedTodoTitle(response));
    }

    @Test
    void invalidCredentialOnAnonymousReadRejects() {
        final Thingifier thingifier = todoModel();
        createTodo(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME, "default todo");
        scopedSession(thingifier)
                .authenticateWith(this::validScopedSession)
                .allowAnonymousDefaultScopeForReads();

        final ApiResponse response =
                thingifier
                        .api()
                        .get(
                                "todos",
                                new QueryFilterParams(),
                                headersWithScopedCredential("bad-session"));

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertEquals("Unauthorized", response.getErrorMessages().iterator().next());
    }

    @Test
    void invalidCredentialUsesConfiguredInvalidResponse() {
        final Thingifier thingifier = todoModel();
        scopedSession(thingifier)
                .authenticateWith(this::validScopedSession)
                .requireAuthenticatedScopeForWrites()
                .onInvalidCredential(403, "Invalid scoped session");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                headersWithScopedCredential("bad-session"));

        Assertions.assertEquals(403, response.getStatusCode());
        Assertions.assertEquals(
                "Invalid scoped session", response.getErrorMessages().iterator().next());
        Assertions.assertEquals(0, todoCount(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME));
    }

    @Test
    void missingCredentialOnProtectedWriteReturnsConfiguredResponse() {
        final Thingifier thingifier = todoModel();
        scopedSession(thingifier)
                .authenticateWith(this::validScopedSession)
                .requireAuthenticatedScopeForWrites()
                .onMissingRequiredCredential(401, "Missing scoped session");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                new HttpHeadersBlock());

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertEquals(
                "Missing scoped session", response.getErrorMessages().iterator().next());
        Assertions.assertEquals(0, todoCount(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME));
    }

    @Test
    void validCredentialOnProtectedWriteWritesToSelectedDataScope() {
        final Thingifier thingifier = todoModel();
        scopedSession(thingifier)
                .authenticateWith(this::validScopedSession)
                .requireAuthenticatedScopeForWrites();

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"tenant todo\"}"),
                                headersWithScopedCredential("valid-session"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(0, todoCount(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME));
        Assertions.assertEquals(1, todoCount(thingifier, "tenant-one"));
    }

    @Test
    void routeLevelRequireOverridesAnonymousReadDefault() {
        final Thingifier thingifier = todoModel();
        createTodo(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME, "default todo");
        scopedSession(thingifier)
                .authenticateWith(this::validScopedSession)
                .allowAnonymousDefaultScopeForReads();
        thingifier.apiSpec().route(RoutingVerb.GET, "/todos").requireScopedSession("challenger");

        final ApiResponse response =
                thingifier.api().get("todos", new QueryFilterParams(), new HttpHeadersBlock());

        Assertions.assertEquals(401, response.getStatusCode());
    }

    @Test
    void routeLevelAnonymousDefaultScopeOverridesProtectedWriteDefault() {
        final Thingifier thingifier = todoModel();
        scopedSession(thingifier)
                .authenticateWith(this::validScopedSession)
                .requireAuthenticatedScopeForWrites();
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").allowAnonymousUsingDefaultScope();

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"default write\"}"),
                                new HttpHeadersBlock());

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(1, todoCount(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME));
    }

    @Test
    void routeLevelDisablePreservesLegacySessionHeaderScope() {
        final Thingifier thingifier = todoModel();
        scopedSession(thingifier)
                .authenticateWith(this::validScopedSession)
                .requireAuthenticatedScopeForWrites();
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").disableScopedSession();

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"legacy session write\"}"),
                                headersWithSession("tenant-one"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(0, todoCount(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME));
        Assertions.assertEquals(1, todoCount(thingifier, "tenant-one"));
    }

    @Test
    void queryParameterCredentialCanSelectDataScope() {
        final Thingifier thingifier = todoModel();
        createTodo(thingifier, "tenant-one", "tenant todo");
        thingifier
                .apiSpec()
                .scopedSession("querySession")
                .fromQueryParam("challenger")
                .authenticateWith(this::validScopedSession);
        thingifier.apiSpec().route(RoutingVerb.GET, "/todos").requireScopedSession("querySession");
        final QueryFilterParams queryParams = new QueryFilterParams();
        queryParams.put("challenger", "valid-session");

        final ApiResponse response =
                thingifier.api().get("todos", queryParams, new HttpHeadersBlock());

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("tenant todo", firstReturnedTodoTitle(response));
    }

    @Test
    void cookieCredentialCanSelectDataScope() {
        final Thingifier thingifier = todoModel();
        createTodo(thingifier, "tenant-one", "tenant todo");
        thingifier
                .apiSpec()
                .scopedSession("cookieSession")
                .fromCookie("CHALLENGER")
                .authenticateWith(this::validScopedSession);
        thingifier.apiSpec().route(RoutingVerb.GET, "/todos").requireScopedSession("cookieSession");

        final ApiResponse response =
                thingifier
                        .api()
                        .get(
                                "todos",
                                new QueryFilterParams(),
                                headersWithCookie("other=x; CHALLENGER=valid-session"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("tenant todo", firstReturnedTodoTitle(response));
    }

    @Test
    void authorizerReceivesScopedSessionSelectedScopeAndPrincipal() {
        final Thingifier thingifier = todoModel();
        final AtomicReference<String> seenDataScope = new AtomicReference<>();
        final AtomicReference<Object> seenPrincipal = new AtomicReference<>();
        scopedSession(thingifier).authenticateWith(this::validScopedSession);
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/todos")
                .requireScopedSession("challenger")
                .authorizeWith(
                        context -> {
                            seenDataScope.set(context.dataScopeName());
                            seenPrincipal.set(context.principal());
                            return ThingifierApiAuthorizationResult.authorized();
                        });

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"tenant todo\"}"),
                                headersWithScopedCredential("valid-session"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals("tenant-one", seenDataScope.get());
        Assertions.assertEquals("scoped-principal", seenPrincipal.get());
    }

    @Test
    void authorizerReceivesAnonymousDataScope() {
        final Thingifier thingifier = todoModel();
        final AtomicReference<String> seenDataScope = new AtomicReference<>();
        final AtomicReference<Object> seenPrincipal = new AtomicReference<>();
        scopedSession(thingifier)
                .authenticateWith(this::validScopedSession)
                .allowAnonymousReadsUsingDataScope("anonymous-scope", ENSURE_EXISTS);
        thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/todos")
                .authorizeWith(
                        context -> {
                            seenDataScope.set(context.dataScopeName());
                            seenPrincipal.set(context.principal());
                            return ThingifierApiAuthorizationResult.authorized();
                        });

        final ApiResponse response =
                thingifier.api().get("todos", new QueryFilterParams(), new HttpHeadersBlock());

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("anonymous-scope", seenDataScope.get());
        Assertions.assertNull(seenPrincipal.get());
    }

    @Test
    void fixedRouteUsesAnonymousDataScope() {
        final Thingifier thingifier = todoModel();
        createTodo(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME, "default todo");
        createTodo(thingifier, "anonymous-scope", "anonymous fixed todo");
        scopedSession(thingifier)
                .authenticateWith(this::validScopedSession)
                .allowAnonymousReadsUsingDataScope("anonymous-scope");
        thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/fixed/todo")
                .mapsToEntity("todo")
                .withFixedIdentifier("1");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(new HttpApiRequest("/fixed/todo"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "anonymous fixed todo",
                response.apiResponse().getReturnedInstance().getFieldValue("title").asString());
    }

    @Test
    void explicitRouteAuthDataScopeOverridesScopedSessionDataScope() {
        final Thingifier thingifier = todoModel();
        scopedSession(thingifier).authenticateWith(this::validScopedSession);
        thingifier
                .apiSpec()
                .authenticator(
                        "tenantToken",
                        context ->
                                ThingifierApiAuthenticationResult.authenticated("route-principal")
                                        .useDataScope("route-tenant", ENSURE_EXISTS));
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/todos")
                .requireScopedSession("challenger")
                .secureWithBearerAuth("tenantToken");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"route tenant todo\"}"),
                                headersWithScopedAndBearer("valid-session", "valid-token"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(0, todoCount(thingifier, "tenant-one"));
        Assertions.assertEquals(1, todoCount(thingifier, "route-tenant"));
    }

    @Test
    void explicitRouteAuthFailureStopsAfterScopedSessionSucceeds() {
        final Thingifier thingifier = todoModel();
        scopedSession(thingifier).authenticateWith(this::validScopedSession);
        thingifier
                .apiSpec()
                .authenticator(
                        "tenantToken",
                        context -> ThingifierApiAuthenticationResult.rejected(403, "Forbidden"));
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/todos")
                .requireScopedSession("challenger")
                .secureWithBearerAuth("tenantToken");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                headersWithScopedAndBearer("valid-session", "valid-token"));

        Assertions.assertEquals(403, response.getStatusCode());
        Assertions.assertEquals(0, todoCount(thingifier, "tenant-one"));
    }

    @Test
    void validatorReceivesScopedSessionSelectedStore() {
        final Thingifier thingifier = todoModel();
        final AtomicReference<ThingStore> seenStore = new AtomicReference<>();
        thingifier
                .getDefinitionNamed("todo")
                .withDomainValidation(
                        context -> {
                            seenStore.set(context.store());
                            return new ValidationReport();
                        });
        scopedSession(thingifier).authenticateWith(this::validScopedSession);
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").requireScopedSession("challenger");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"tenant todo\"}"),
                                headersWithScopedCredential("valid-session"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertSame(thingifier.getStore("tenant-one"), seenStore.get());
    }

    @Test
    void operationCallbackReceivesScopedSessionPrincipalAndScope() {
        final Thingifier thingifier = todoModel();
        final AtomicReference<String> seenDataScope = new AtomicReference<>();
        final AtomicReference<Object> seenPrincipal = new AtomicReference<>();
        scopedSession(thingifier).authenticateWith(this::validScopedSession);
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/todos")
                .requireScopedSession("challenger")
                .afterSuccessfulOperation(
                        (context, result) -> {
                            seenDataScope.set(context.dataScopeName());
                            seenPrincipal.set(context.authenticatedPrincipal("challenger"));
                        });

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"tenant todo\"}"),
                                headersWithScopedCredential("valid-session"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals("tenant-one", seenDataScope.get());
        Assertions.assertEquals("scoped-principal", seenPrincipal.get());
    }

    @Test
    void httpBodyParsedHookReceivesScopedSessionSelectedScope() {
        final Thingifier thingifier = todoModel();
        thingifier.apiConfig().setFrom(new ThingifierApiConfig("/api"));
        final AtomicReference<String> seenDataScope = new AtomicReference<>();
        final ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        hooks.registerBodyParsedHook(context -> seenDataScope.set(context.dataScopeName()));
        scopedSession(thingifier).authenticateWith(this::validScopedSession);
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/api/todos")
                .requireScopedSession("challenger");

        final HttpApiResponse response =
                ThingifierHttpApi.withHookRegistries(thingifier, null, hooks)
                        .post(
                                jsonPost("/api/todos", "{\"title\":\"tenant todo\"}")
                                        .addHeader("X-CHALLENGER", "valid-session"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals("tenant-one", seenDataScope.get());
    }

    @Test
    void validCredentialWithoutDataScopeSelectionPreservesSessionHeaderScope() {
        final Thingifier thingifier = todoModel();
        scopedSession(thingifier)
                .authenticateWith(
                        context -> ThingifierApiScopedSessionResult.authenticated("principal"))
                .requireAuthenticatedScopeForWrites();

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"tenant todo\"}"),
                                headersWithSessionAndScoped("tenant-one", "valid-session"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(0, todoCount(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME));
        Assertions.assertEquals(1, todoCount(thingifier, "tenant-one"));
    }

    @Test
    void requiredScopedSessionHeaderIsDocumentedAsApiKeySecurity() {
        final Thingifier thingifier = todoModel();
        scopedSession(thingifier)
                .authenticateWith(this::validScopedSession)
                .requireAuthenticatedScopeForWrites();
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.setThingifier(thingifier);

        final OpenAPI openApi =
                new uk.co.compendiumdev.thingifier.swaggerizer.Swaggerizer(apiDefn).swagger();

        final SecurityScheme scheme =
                openApi.getComponents().getSecuritySchemes().get("challenger");
        Assertions.assertNotNull(scheme);
        Assertions.assertEquals(SecurityScheme.Type.APIKEY, scheme.getType());
        Assertions.assertEquals(SecurityScheme.In.HEADER, scheme.getIn());
        Assertions.assertEquals("X-CHALLENGER", scheme.getName());
        Assertions.assertEquals(
                "challenger",
                openApi.getPaths()
                        .get("/todos")
                        .getPost()
                        .getSecurity()
                        .get(0)
                        .keySet()
                        .iterator()
                        .next());
    }

    private ThingifierApiScopedSessionDefinition scopedSession(final Thingifier thingifier) {
        return thingifier.apiSpec().scopedSession("challenger").fromHeader("X-CHALLENGER");
    }

    private ThingifierApiScopedSessionResult validScopedSession(
            final ThingifierApiScopedSessionContext context) {
        if ("valid-session".equals(context.credential())) {
            return ThingifierApiScopedSessionResult.authenticated("scoped-principal")
                    .useDataScope("tenant-one", ENSURE_EXISTS);
        }
        return ThingifierApiScopedSessionResult.unauthenticated();
    }

    private Thingifier todoModel() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition todo = thingifier.defineThing("todo", "todos", 5);
        todo.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        todo.addField(Field.is("title", FieldType.STRING).makeMandatory());
        return thingifier;
    }

    private EntityInstance createTodo(
            final Thingifier thingifier, final String dataScopeName, final String title) {
        thingifier.getERmodel().createInstanceDatabaseIfNotExisting(dataScopeName);
        final EntityDefinition todo = thingifier.getDefinitionNamed("todo");
        return thingifier
                .getStore(dataScopeName)
                .entities()
                .create(EntityInstanceDraft.forEntity(todo).withField("title", title));
    }

    private int todoCount(final Thingifier thingifier, final String dataScopeName) {
        return thingifier.listThingInstancesNamed("todos", dataScopeName).size();
    }

    private String firstReturnedTodoTitle(final ApiResponse response) {
        return response.getReturnedInstanceCollection().get(0).getFieldValue("title").asString();
    }

    private BodyParser parser(final Thingifier thingifier, final String body) {
        return new BodyParser(
                new HttpApiRequest("/request")
                        .addHeader("Content-Type", "application/json")
                        .setBody(body),
                thingifier.getThingNames());
    }

    private HttpApiRequest jsonPost(final String path, final String body) {
        return new HttpApiRequest(path).addHeader("Content-Type", "application/json").setBody(body);
    }

    private HttpHeadersBlock headersWithScopedCredential(final String credential) {
        HttpHeadersBlock headers = new HttpHeadersBlock();
        headers.put("X-CHALLENGER", credential);
        return headers;
    }

    private HttpHeadersBlock headersWithSession(final String dataScopeName) {
        HttpHeadersBlock headers = new HttpHeadersBlock();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, dataScopeName);
        return headers;
    }

    private HttpHeadersBlock headersWithSessionAndScoped(
            final String dataScopeName, final String credential) {
        HttpHeadersBlock headers = headersWithSession(dataScopeName);
        headers.put("X-CHALLENGER", credential);
        return headers;
    }

    private HttpHeadersBlock headersWithScopedAndBearer(
            final String scopedCredential, final String bearerToken) {
        HttpHeadersBlock headers = headersWithScopedCredential(scopedCredential);
        headers.put("Authorization", "Bearer " + bearerToken);
        return headers;
    }

    private HttpHeadersBlock headersWithCookie(final String cookieHeader) {
        HttpHeadersBlock headers = new HttpHeadersBlock();
        headers.put("Cookie", cookieHeader);
        return headers;
    }
}
