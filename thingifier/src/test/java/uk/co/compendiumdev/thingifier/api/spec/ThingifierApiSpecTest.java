package uk.co.compendiumdev.thingifier.api.spec;

import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.JSON_MERGE_PATCH_RFC7396;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.JSON_PATCH_RFC6902;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.PARTIAL_JSON_UPDATE;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiSecuritySpec;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

class ThingifierApiSpecTest {

    @Test
    void emptyApiSpecLeavesGeneratedRoutesUnchanged() {
        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(model()).generate("/api");

        final RoutingDefinition route = route(definition, RoutingVerb.POST, "api/todos");
        Assertions.assertFalse(route.isDisabled());
        Assertions.assertFalse(route.isHiddenFromDocumentation());
        Assertions.assertFalse(route.isSecuredByBearerAuth());
        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, POST, QUERY",
                route(definition, RoutingVerb.OPTIONS, "api/todos").headerValue());
    }

    @Test
    void apiSpecCanSecureAndOverrideGeneratedRouteDocumentation() {
        final Thingifier thingifier = model();
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/api/projects/{projectId}/tasks")
                .secureWithBearerAuth()
                .addDocumentation("create a task through the project API")
                .requestPayload("create_todo");

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api");
        final RoutingDefinition route =
                route(definition, RoutingVerb.POST, "api/projects/:id/tasks");

        Assertions.assertTrue(route.isSecuredByBearerAuth());
        Assertions.assertEquals("create a task through the project API", route.getDocumentation());
        Assertions.assertEquals("create_todo", route.getRequestPayload());
    }

    @Test
    void namedBasicRouteRecordsDocumentationAndRuntimeEnforcement() {
        final Thingifier thingifier = model();
        final ThingifierApiRouteRule rule =
                thingifier
                        .apiSpec()
                        .route(RoutingVerb.POST, "/api/todos")
                        .secureWithBasicAuth("adminPassword");

        Assertions.assertTrue(rule.isSecuredByBasicAuth());
        Assertions.assertEquals("adminPassword", rule.basicAuthSchemeName());
        Assertions.assertTrue(rule.hasBasicAuthEnforcement());
        Assertions.assertEquals("adminPassword", rule.basicAuthEnforcementSchemeName());
    }

    @Test
    void basicSecuritySpecStoresConfiguredRealm() {
        final ThingifierApiSecuritySpec securitySpec = new ThingifierApiSecuritySpec();

        securitySpec.basic("adminPassword", "User Visible Realm");

        Assertions.assertTrue(securitySpec.hasBasic("adminPassword"));
        Assertions.assertEquals("User Visible Realm", securitySpec.basicRealm("adminPassword"));
    }

    @Test
    void apiKeySecuritySpecStoresConfiguredHeader() {
        final ThingifierApiSecuritySpec securitySpec = new ThingifierApiSecuritySpec();

        securitySpec.apiKey("authToken", "X-AUTH-TOKEN");

        Assertions.assertTrue(securitySpec.hasApiKey("authToken"));
        Assertions.assertEquals("X-AUTH-TOKEN", securitySpec.apiKeyHeaderName("authToken"));
    }

    @Test
    void namedApiKeyRouteRecordsDocumentationAndRuntimeEnforcement() {
        final Thingifier thingifier = model();
        final ThingifierApiRouteRule rule =
                thingifier
                        .apiSpec()
                        .route(RoutingVerb.POST, "/api/todos")
                        .secureWithApiKey("authToken");

        Assertions.assertTrue(rule.isSecuredByApiKeyAuth());
        Assertions.assertEquals("authToken", rule.apiKeyAuthSchemeName());
        Assertions.assertTrue(rule.hasApiKeyAuthEnforcement());
        Assertions.assertEquals("authToken", rule.apiKeyAuthEnforcementSchemeName());
    }

    @Test
    void alternativeAuthRouteRecordsOrderedRuntimeSchemes() {
        final Thingifier thingifier = model();
        final ThingifierApiRouteRule rule =
                thingifier
                        .apiSpec()
                        .route(RoutingVerb.POST, "/api/todos")
                        .secureWithAnyOf("secretBearer", "secretApiKey");

        Assertions.assertTrue(rule.hasAuthEnforcement());
        Assertions.assertEquals(
                java.util.List.of("secretBearer", "secretApiKey"),
                rule.authEnforcementSchemeNames());
        Assertions.assertFalse(rule.hasBearerAuthEnforcement());
        Assertions.assertFalse(rule.hasApiKeyAuthEnforcement());
    }

    @Test
    void routeRecordsSingleInstanceResponseShape() {
        final Thingifier thingifier = model();
        final ThingifierApiRouteRule rule =
                thingifier
                        .apiSpec()
                        .route(RoutingVerb.GET, "/api/todos/{id}")
                        .respondWithSingleInstance();

        Assertions.assertTrue(rule.hasResponseShapeOverride());
        Assertions.assertEquals(ResponseShape.SINGLE_INSTANCE, rule.responseShape());
    }

    @Test
    void routeResponseShapeIsCopiedToGeneratedRouteDefinition() {
        final Thingifier thingifier = model();
        thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/api/todos/{id}")
                .responseShape(ResponseShape.SINGLE_INSTANCE);

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api");
        final RoutingDefinition route = route(definition, RoutingVerb.GET, "api/todos/:id");

        Assertions.assertTrue(route.hasResponseShapeOverride());
        Assertions.assertEquals(ResponseShape.SINGLE_INSTANCE, route.responseShape());
    }

    @Test
    void namedBasicAuthClearsBearerEnforcementOnRoute() {
        final Thingifier thingifier = model();
        final ThingifierApiRouteRule rule =
                thingifier
                        .apiSpec()
                        .route(RoutingVerb.POST, "/api/todos")
                        .secureWithBearerAuth("cartToken")
                        .secureWithBasicAuth("adminPassword");

        Assertions.assertTrue(rule.hasBasicAuthEnforcement());
        Assertions.assertFalse(rule.hasBearerAuthEnforcement());
    }

    @Test
    void namedBearerAuthClearsBasicEnforcementOnRoute() {
        final Thingifier thingifier = model();
        final ThingifierApiRouteRule rule =
                thingifier
                        .apiSpec()
                        .route(RoutingVerb.POST, "/api/todos")
                        .secureWithBasicAuth("adminPassword")
                        .secureWithBearerAuth("cartToken");

        Assertions.assertFalse(rule.hasBasicAuthEnforcement());
        Assertions.assertTrue(rule.hasBearerAuthEnforcement());
    }

    @Test
    void namedApiKeyAuthClearsOtherEnforcementOnRoute() {
        final Thingifier thingifier = model();
        final ThingifierApiRouteRule rule =
                thingifier
                        .apiSpec()
                        .route(RoutingVerb.POST, "/api/todos")
                        .secureWithBasicAuth("adminPassword")
                        .secureWithBearerAuth("cartToken")
                        .secureWithApiKey("authToken");

        Assertions.assertFalse(rule.hasBasicAuthEnforcement());
        Assertions.assertFalse(rule.hasBearerAuthEnforcement());
        Assertions.assertTrue(rule.hasApiKeyAuthEnforcement());
    }

    @Test
    void namedBearerAuthClearsApiKeyEnforcementOnRoute() {
        final Thingifier thingifier = model();
        final ThingifierApiRouteRule rule =
                thingifier
                        .apiSpec()
                        .route(RoutingVerb.POST, "/api/todos")
                        .secureWithApiKey("authToken")
                        .secureWithBearerAuth("cartToken");

        Assertions.assertFalse(rule.hasApiKeyAuthEnforcement());
        Assertions.assertTrue(rule.hasBearerAuthEnforcement());
    }

    @Test
    void namedBasicAuthClearsApiKeyEnforcementOnRoute() {
        final Thingifier thingifier = model();
        final ThingifierApiRouteRule rule =
                thingifier
                        .apiSpec()
                        .route(RoutingVerb.POST, "/api/todos")
                        .secureWithApiKey("authToken")
                        .secureWithBasicAuth("adminPassword");

        Assertions.assertFalse(rule.hasApiKeyAuthEnforcement());
        Assertions.assertTrue(rule.hasBasicAuthEnforcement());
    }

    @Test
    void disabledGeneratedRouteIsHiddenAndRemovedFromOptions() {
        final Thingifier thingifier = model();
        thingifier.apiSpec().route(RoutingVerb.POST, "/api/todos").disable();

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api");

        final RoutingDefinition post = route(definition, RoutingVerb.POST, "api/todos");
        Assertions.assertTrue(post.isDisabled());
        Assertions.assertTrue(post.isHiddenFromDocumentation());
        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, QUERY",
                route(definition, RoutingVerb.OPTIONS, "api/todos").headerValue());
    }

    @Test
    void disabledGeneratedRouteReturns404FromInternalApi() {
        final Thingifier thingifier = model();
        thingifier.apiConfig().setFrom(new ThingifierApiConfig("/api"));
        thingifier.apiSpec().route(RoutingVerb.POST, "/api/todos").disable();

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).post(new HttpApiRequest("/api/todos"));

        Assertions.assertEquals(404, response.getStatusCode());
    }

    @Test
    void pathLevelMethodNotAllowedMarksSelectedRoutesAsStatic405() {
        final Thingifier thingifier = model();
        thingifier
                .apiSpec()
                .route("/api/todos")
                .methodNotAllowed(RoutingVerb.POST, RoutingVerb.PUT);

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api");

        Assertions.assertEquals(
                405, route(definition, RoutingVerb.POST, "api/todos").status().value());
        Assertions.assertEquals(
                405, route(definition, RoutingVerb.PUT, "api/todos").status().value());
    }

    @Test
    void methodNotAllowedDoesNotDisableGeneratedRoute() {
        final Thingifier thingifier = model();
        thingifier.apiSpec().route("/api/todos").methodNotAllowed(RoutingVerb.POST);

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api");
        final RoutingDefinition post = route(definition, RoutingVerb.POST, "api/todos");

        Assertions.assertFalse(post.isDisabled());
        Assertions.assertFalse(post.isHiddenFromDocumentation());
    }

    @Test
    void methodNotAllowedRemovesVerbFromOptionsAllowHeader() {
        final Thingifier thingifier = model();
        thingifier.apiSpec().route("/api/todos").methodNotAllowed(RoutingVerb.POST);

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api");

        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, QUERY",
                route(definition, RoutingVerb.OPTIONS, "api/todos").headerValue());
    }

    @Test
    void methodNotAllowedReusesExistingVerbRouteRule() {
        final Thingifier thingifier = model();
        thingifier.apiSpec().route(RoutingVerb.POST, "/api/todos").secureWithBearerAuth();
        thingifier.apiSpec().route("/api/todos").methodNotAllowed(RoutingVerb.POST);

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api");
        final RoutingDefinition post = route(definition, RoutingVerb.POST, "api/todos");

        Assertions.assertEquals(405, post.status().value());
        Assertions.assertTrue(post.isSecuredByBearerAuth());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/todos/{todoId}", "/api/todos/:todoId"})
    void methodNotAllowedMatchesPathParameterStyles(final String pathPattern) {
        final Thingifier thingifier = model();
        thingifier.apiSpec().route(pathPattern).methodNotAllowed(RoutingVerb.POST);

        Assertions.assertTrue(
                thingifier
                        .apiSpec()
                        .isMethodNotAllowed(RoutingVerb.POST, "/api/todos/123", "/api"));
    }

    @Test
    void directApiMethodNotAllowedReturns405WithoutMutation() {
        final Thingifier thingifier = model();
        thingifier.apiSpec().route("/todos").methodNotAllowed(RoutingVerb.POST);

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                new HttpHeadersBlock());

        Assertions.assertEquals(405, response.getStatusCode());
        Assertions.assertEquals(
                "Method Not Allowed", response.getErrorMessages().iterator().next());
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void httpApiMethodNotAllowedReturns405WithoutMutation() {
        final Thingifier thingifier = model();
        thingifier.apiConfig().setFrom(new ThingifierApiConfig("/api"));
        thingifier.apiSpec().route("/api/todos").methodNotAllowed(RoutingVerb.POST);

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(jsonRequest("/api/todos", "{\"title\":\"blocked\"}"));

        Assertions.assertEquals(405, response.getStatusCode());
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void methodNotAllowedRejectsEmptyVerbList() {
        final Thingifier thingifier = model();

        final IllegalArgumentException thrown =
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> thingifier.apiSpec().route("/api/todos").methodNotAllowed());

        Assertions.assertEquals("methodNotAllowed requires at least one verb", thrown.getMessage());
    }

    @Test
    void methodNotAllowedRejectsNullVerbList() {
        final Thingifier thingifier = model();

        final IllegalArgumentException thrown =
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                thingifier
                                        .apiSpec()
                                        .route("/api/todos")
                                        .methodNotAllowed((RoutingVerb[]) null));

        Assertions.assertEquals("methodNotAllowed requires at least one verb", thrown.getMessage());
    }

    @Test
    void apiSpecCanDisableAllRoutesForAnEntity() {
        final Thingifier thingifier = model();
        thingifier.apiSpec().disableEntityRoutes("/api/todos");

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api");

        Assertions.assertTrue(route(definition, RoutingVerb.GET, "api/todos").isDisabled());
        Assertions.assertTrue(route(definition, RoutingVerb.POST, "api/todos").isDisabled());
        Assertions.assertTrue(route(definition, RoutingVerb.GET, "api/todos/:id").isDisabled());
        Assertions.assertTrue(route(definition, RoutingVerb.PUT, "api/todos/:id").isDisabled());
    }

    @Test
    void apiSpecCanDisableAllRoutesForARelationship() {
        final Thingifier thingifier = model();
        thingifier.apiSpec().disableRelationshipRoutes("/api/projects", "tasks");

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api");

        Assertions.assertTrue(
                route(definition, RoutingVerb.GET, "api/projects/:id/tasks").isDisabled());
        Assertions.assertTrue(
                route(definition, RoutingVerb.POST, "api/projects/:id/tasks").isDisabled());
        Assertions.assertTrue(
                route(definition, RoutingVerb.DELETE, "api/projects/:id/tasks/:relatedId")
                        .isDisabled());
        Assertions.assertTrue(
                route(definition, RoutingVerb.OPTIONS, "api/projects/:id/tasks/:relatedId")
                        .isDisabled());
    }

    @Test
    void apiSpecCanHideAllRoutesForARelationshipWithoutDisablingThem() {
        final Thingifier thingifier = model();
        thingifier.apiSpec().hideRelationshipRoutes("/api/projects", "tasks");

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api");

        final RoutingDefinition get = route(definition, RoutingVerb.GET, "api/projects/:id/tasks");
        Assertions.assertTrue(get.isHiddenFromDocumentation());
        Assertions.assertFalse(get.isDisabled());

        final RoutingDefinition delete =
                route(definition, RoutingVerb.DELETE, "api/projects/:id/tasks/:relatedId");
        Assertions.assertTrue(delete.isHiddenFromDocumentation());
        Assertions.assertFalse(delete.isDisabled());
    }

    @Test
    void apiSpecCanBindEntityViewsToGeneratedRoutes() {
        final Thingifier thingifier = viewModel();
        thingifier.apiSpec().route(RoutingVerb.POST, "/api/items").entityView("PublicItem");

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api");
        final RoutingDefinition route = route(definition, RoutingVerb.POST, "api/items");

        Assertions.assertEquals("create_PublicItem", route.getRequestPayload());
        Assertions.assertEquals("PublicItem", route.getReturnPayloadFor(201));
        Assertions.assertTrue(route.hasRequestEntityView());
        Assertions.assertEquals("PublicItem", route.getResponseEntityViewFor(201));
    }

    @Test
    void entityViewsFilterResponsesAndRejectDisallowedInputFieldsAtRuntime() {
        final Thingifier thingifier = viewModel();
        thingifier.apiConfig().setFrom(new ThingifierApiConfig("/api"));
        thingifier.apiSpec().route(RoutingVerb.POST, "/api/items").entityView("PublicItem");
        final ThingifierHttpApi api = new ThingifierHttpApi(thingifier);

        final HttpApiResponse createResponse =
                api.post(
                        jsonRequest(
                                "/api/items",
                                "{\"name\":\"visible\",\"secret\":\"stored but hidden\"}"));

        Assertions.assertEquals(201, createResponse.getStatusCode());
        Assertions.assertTrue(createResponse.getBody().contains("visible"));
        Assertions.assertFalse(createResponse.getBody().contains("secret"));
        Assertions.assertFalse(createResponse.getBody().contains("stored but hidden"));

        final HttpApiResponse getResponse = api.get(new HttpApiRequest("/api/items/1"));
        Assertions.assertEquals(200, getResponse.getStatusCode());
        Assertions.assertTrue(getResponse.getBody().contains("stored but hidden"));

        final HttpApiResponse disallowedResponse =
                api.post(
                        jsonRequest("/api/items", "{\"name\":\"visible\",\"forbidden\":\"nope\"}"));

        Assertions.assertEquals(422, disallowedResponse.getStatusCode());
        Assertions.assertTrue(disallowedResponse.getBody().contains("forbidden"));
    }

    @Test
    void patchRequestEntityViewsRejectDisallowedInputFieldsAtRuntime() {
        final Thingifier thingifier = viewModel();
        thingifier.apiConfig().setFrom(new ThingifierApiConfig("/api"));
        thingifier
                .apiConfig()
                .writeMethods()
                .entities()
                .patchCan(PARTIAL_JSON_UPDATE, JSON_MERGE_PATCH_RFC7396, JSON_PATCH_RFC6902);
        thingifier
                .apiSpec()
                .route(RoutingVerb.PATCH, "/api/items/{id}")
                .requestEntityView("PublicItem");
        final EntityInstance item = createItem(thingifier, "visible", "stored", "original");
        final ThingifierHttpApi api = new ThingifierHttpApi(thingifier);
        final String path = "/api/items/" + item.getPrimaryKeyValue();

        final HttpApiResponse partialJsonResponse =
                api.patch(
                        patchRequest(
                                path,
                                "{\"forbidden\":\"partial\"}",
                                PARTIAL_JSON_UPDATE.mediaType()));
        final HttpApiResponse mergePatchResponse =
                api.patch(
                        patchRequest(
                                path,
                                "{\"forbidden\":\"merge\"}",
                                JSON_MERGE_PATCH_RFC7396.mediaType()));
        final HttpApiResponse jsonPatchPathResponse =
                api.patch(
                        patchRequest(
                                path,
                                "[{\"op\":\"replace\",\"path\":\"/forbidden\",\"value\":\"json patch\"}]",
                                JSON_PATCH_RFC6902.mediaType()));
        final HttpApiResponse jsonPatchFromResponse =
                api.patch(
                        patchRequest(
                                path,
                                "[{\"op\":\"copy\",\"from\":\"/forbidden\",\"path\":\"/name\"}]",
                                JSON_PATCH_RFC6902.mediaType()));
        final HttpApiResponse jsonPatchRootCopyResponse =
                api.patch(
                        patchRequest(
                                path,
                                "[{\"op\":\"add\",\"path\":\"/template\","
                                        + "\"value\":{\"name\":\"copied\",\"forbidden\":\"copy\"}},"
                                        + "{\"op\":\"copy\",\"from\":\"/template\",\"path\":\"\"}]",
                                JSON_PATCH_RFC6902.mediaType()));
        final HttpApiResponse jsonPatchRootMoveResponse =
                api.patch(
                        patchRequest(
                                path,
                                "[{\"op\":\"add\",\"path\":\"/template\","
                                        + "\"value\":{\"name\":\"moved\",\"forbidden\":\"move\"}},"
                                        + "{\"op\":\"move\",\"from\":\"/template\",\"path\":\"\"}]",
                                JSON_PATCH_RFC6902.mediaType()));
        final HttpApiResponse jsonPatchWithNullBodyResponse =
                api.patch(patchRequestWithNullBody(path, JSON_PATCH_RFC6902.mediaType()));

        Assertions.assertEquals(422, partialJsonResponse.getStatusCode());
        Assertions.assertEquals(422, mergePatchResponse.getStatusCode());
        Assertions.assertEquals(422, jsonPatchPathResponse.getStatusCode());
        Assertions.assertEquals(422, jsonPatchFromResponse.getStatusCode());
        Assertions.assertEquals(422, jsonPatchRootCopyResponse.getStatusCode());
        Assertions.assertEquals(422, jsonPatchRootMoveResponse.getStatusCode());
        Assertions.assertEquals(400, jsonPatchWithNullBodyResponse.getStatusCode());
        Assertions.assertTrue(partialJsonResponse.getBody().contains("forbidden"));
        Assertions.assertTrue(jsonPatchRootCopyResponse.getBody().contains("forbidden"));
        Assertions.assertTrue(jsonPatchRootMoveResponse.getBody().contains("forbidden"));
        Assertions.assertTrue(
                jsonPatchWithNullBodyResponse.getBody().contains("JSON Patch"),
                jsonPatchWithNullBodyResponse.getBody());
        Assertions.assertEquals("original", currentItemField(thingifier, item, "forbidden"));
        Assertions.assertEquals("visible", currentItemField(thingifier, item, "name"));

        final HttpApiResponse allowedMergePatchResponse =
                api.patch(
                        patchRequest(
                                path,
                                "{\"name\":\"updated\"}",
                                JSON_MERGE_PATCH_RFC7396.mediaType()));

        Assertions.assertEquals(200, allowedMergePatchResponse.getStatusCode());
        Assertions.assertEquals("updated", currentItemField(thingifier, item, "name"));
        Assertions.assertEquals("original", currentItemField(thingifier, item, "forbidden"));
    }

    private Thingifier model() {
        Thingifier thingifier = new Thingifier();

        EntityDefinition project = thingifier.defineThing("project", "projects", 5);
        project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        project.addField(Field.is("title", FieldType.STRING));

        EntityDefinition todo = thingifier.defineThing("todo", "todos", 5);
        todo.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        todo.addField(Field.is("title", FieldType.STRING).makeMandatory());

        thingifier
                .defineRelationship(project, todo, "tasks", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_ONE(), "project");

        return thingifier;
    }

    private Thingifier viewModel() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition item = thingifier.defineThing("item", "items", 5);
        item.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        item.addField(Field.is("name", FieldType.STRING).makeMandatory());
        item.addField(Field.is("secret", FieldType.STRING));
        item.addField(Field.is("forbidden", FieldType.STRING));
        item.addField(
                Field.is("template", FieldType.OBJECT)
                        .withField(Field.is("name", FieldType.STRING))
                        .withField(Field.is("forbidden", FieldType.STRING)));
        item.defineView("PublicItem")
                .hideRequestFields("secret")
                .hideResponseFields("secret")
                .disallowInputFields("forbidden");
        return thingifier;
    }

    private EntityInstance createItem(
            final Thingifier thingifier,
            final String name,
            final String secret,
            final String forbidden) {
        final EntityDefinition item = thingifier.getDefinitionNamed("item");
        return thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(item)
                                .withField("name", name)
                                .withField("secret", secret)
                                .withField("forbidden", forbidden));
    }

    private String currentItemField(
            final Thingifier thingifier, final EntityInstance item, final String fieldName) {
        return thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entityQueries()
                .findByQueryIdentifier(
                        thingifier.getDefinitionNamed("item"), item.getPrimaryKeyValue())
                .getFieldValue(fieldName)
                .asString();
    }

    private HttpApiRequest jsonRequest(final String path, final String body) {
        return new HttpApiRequest(path)
                .setVerb("POST")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .setBody(body);
    }

    private HttpApiRequest patchRequest(
            final String path, final String body, final String contentType) {
        return new HttpApiRequest(path)
                .addHeader("Content-Type", contentType)
                .addHeader("Accept", "application/json")
                .setBody(body);
    }

    private HttpApiRequest patchRequestWithNullBody(final String path, final String contentType) {
        return new HttpApiRequest(path)
                .addHeader("Content-Type", contentType)
                .addHeader("Accept", "application/json")
                .setBody(null);
    }

    private BodyParser parser(final Thingifier thingifier, final String body) {
        return new BodyParser(
                new HttpApiRequest("/request").setBody(body), thingifier.getThingNames());
    }

    private int todoCount(final Thingifier thingifier) {
        return thingifier
                .api()
                .get("todos", new QueryFilterParams(), new HttpHeadersBlock())
                .getReturnedInstanceCollection()
                .size();
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
