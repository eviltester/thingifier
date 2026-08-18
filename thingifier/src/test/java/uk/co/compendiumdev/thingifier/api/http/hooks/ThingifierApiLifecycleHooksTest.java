package uk.co.compendiumdev.thingifier.api.http.hooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.hooks.HookScope;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleHookRegistry;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiHookRegistry;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;
import uk.co.compendiumdev.thingifier.application.command.CreateThingCommand;
import uk.co.compendiumdev.thingifier.application.query.ReadCollectionQuery;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

class ThingifierApiLifecycleHooksTest {

    @ParameterizedTest
    @EnumSource(
            value = ThingifierHttpApi.HttpVerb.class,
            names = {"GET", "HEAD", "QUERY", "POST", "PUT", "PATCH", "DELETE"})
    void routeMatchedHookRunsForDynamicThingifierOperation(final ThingifierHttpApi.HttpVerb verb) {
        Thingifier thingifier = todoThingifier();
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        AtomicInteger callCount = new AtomicInteger();
        hooks.registerRouteMatchedHook(context -> callCount.incrementAndGet());

        perform(thingifier, hooks, verb);

        Assertions.assertEquals(1, callCount.get());
    }

    @Test
    void globalScopedLifecycleHookRunsForEveryApiRequest() {
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        AtomicInteger callCount = new AtomicInteger();
        hooks.registerRouteMatchedHook(context -> callCount.incrementAndGet());
        ThingifierHttpApi api = api(todoThingifier(), hooks);

        api.get(new HttpApiRequest("/todos"));
        api.get(new HttpApiRequest("/projects"));

        Assertions.assertEquals(2, callCount.get());
    }

    @Test
    void endpointScopedLifecycleHookRunsOnlyForMatchingPath() {
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        AtomicInteger callCount = new AtomicInteger();
        hooks.registerRouteMatchedHook("todos", context -> callCount.incrementAndGet());
        ThingifierHttpApi api = api(todoThingifier(), hooks);

        api.get(new HttpApiRequest("/todos"));
        api.get(new HttpApiRequest("/projects"));

        Assertions.assertEquals(1, callCount.get());
    }

    @Test
    void verbScopedLifecycleHookRunsOnlyForMatchingVerb() {
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        AtomicInteger callCount = new AtomicInteger();
        hooks.registerRouteMatchedHook(
                HookScope.verbs(RoutingVerb.POST), context -> callCount.incrementAndGet());
        ThingifierHttpApi api = api(todoThingifier(), hooks);

        api.get(new HttpApiRequest("/todos"));
        api.post(jsonRequest("/todos", "{\"title\":\"created\"}"));

        Assertions.assertEquals(1, callCount.get());
    }

    @Test
    void endpointAndVerbScopedLifecycleHookRequiresBothToMatch() {
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        AtomicInteger callCount = new AtomicInteger();
        hooks.registerRouteMatchedHook(
                "todos", List.of(RoutingVerb.POST), context -> callCount.incrementAndGet());
        ThingifierHttpApi api = api(todoThingifier(), hooks);

        api.get(new HttpApiRequest("/todos"));
        api.post(jsonRequest("/projects", "{\"title\":\"project\"}"));
        api.post(jsonRequest("/todos", "{\"title\":\"todo\"}"));

        Assertions.assertEquals(1, callCount.get());
    }

    @Test
    void endpointScopedLifecycleHookMatchesPathParameter() {
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        AtomicInteger callCount = new AtomicInteger();
        hooks.registerRouteMatchedHook("todos/{guid}", context -> callCount.incrementAndGet());

        api(todoThingifier(), hooks).get(new HttpApiRequest("/todos/123"));

        Assertions.assertEquals(1, callCount.get());
    }

    @Test
    void routeMatchedContextExposesCollectionRouteTarget() {
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        AtomicReference<String> targetEntity = new AtomicReference<>();
        hooks.registerRouteMatchedHook(
                context -> targetEntity.set(context.targetEntity().getName()));

        api(todoThingifier(), hooks).get(new HttpApiRequest("/todos"));

        Assertions.assertEquals("todo", targetEntity.get());
    }

    @Test
    void routeMatchedContextExposesInstanceRouteIdentifier() {
        Thingifier thingifier = todoThingifier();
        EntityInstance todo = createTodo(thingifier, "inspect me");
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        AtomicReference<String> targetIdentifier = new AtomicReference<>();
        hooks.registerRouteMatchedHook(context -> targetIdentifier.set(context.targetIdentifier()));

        api(thingifier, hooks).get(new HttpApiRequest("/todos/" + todo.getPrimaryKeyValue()));

        Assertions.assertEquals(todo.getPrimaryKeyValue(), targetIdentifier.get());
    }

    @Test
    void routeMatchedContextExposesRelationshipCollectionDetails() {
        Thingifier thingifier = todoThingifier();
        EntityInstance project = createProject(thingifier, "project");
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        AtomicReference<String> parentEntity = new AtomicReference<>();
        AtomicReference<String> parentIdentifier = new AtomicReference<>();
        AtomicReference<String> relationshipName = new AtomicReference<>();
        AtomicReference<String> targetEntity = new AtomicReference<>();
        hooks.registerRouteMatchedHook(
                context -> {
                    parentEntity.set(context.parentEntity().getName());
                    parentIdentifier.set(context.parentIdentifier());
                    relationshipName.set(context.relationshipName());
                    targetEntity.set(context.targetEntity().getName());
                });

        api(thingifier, hooks)
                .get(new HttpApiRequest("/projects/" + project.getPrimaryKeyValue() + "/tasks"));

        Assertions.assertEquals("project", parentEntity.get());
        Assertions.assertEquals(project.getPrimaryKeyValue(), parentIdentifier.get());
        Assertions.assertEquals("tasks", relationshipName.get());
        Assertions.assertEquals("todo", targetEntity.get());
    }

    @Test
    void routeMatchedContextExposesRelationshipInstanceChildIdentifier() {
        Thingifier thingifier = todoThingifier();
        EntityInstance project = createProject(thingifier, "project");
        EntityInstance todo = createTodo(thingifier, "task");
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        AtomicReference<String> childIdentifier = new AtomicReference<>();
        hooks.registerRouteMatchedHook(context -> childIdentifier.set(context.childIdentifier()));

        api(thingifier, hooks)
                .get(
                        new HttpApiRequest(
                                "/projects/"
                                        + project.getPrimaryKeyValue()
                                        + "/tasks/"
                                        + todo.getPrimaryKeyValue()));

        Assertions.assertEquals(todo.getPrimaryKeyValue(), childIdentifier.get());
    }

    @Test
    void bodyParsedHookCanChangeParsedFieldsBeforeCreateValidation() {
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        hooks.registerBodyParsedHook(
                context ->
                        context.replaceBodyFields(
                                ApiBodyFields.fromMap(Map.of("title", "from body hook"))));

        HttpApiResponse response = api(todoThingifier(), hooks).post(jsonRequest("/todos", "{}"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals("from body hook", returnedTitle(response));
    }

    @Test
    void beforeValidationHookCanReplaceWriteCommandBeforeThingifierValidation() {
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        hooks.registerBeforeValidationHook(
                context ->
                        context.replaceWriteCommand(
                                new CreateThingCommand(
                                        "todo",
                                        List.of(new NamedValue("title", "from command hook")),
                                        List.of(),
                                        true)));

        HttpApiResponse response = api(todoThingifier(), hooks).post(jsonRequest("/todos", "{}"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals("from command hook", returnedTitle(response));
    }

    @Test
    void afterValidationHookCanRejectValidWrite() {
        Thingifier thingifier = todoThingifier();
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        hooks.registerAfterValidationHook(
                context -> context.replaceValidationResult(ThingCommandResult.error("blocked")));

        HttpApiResponse response =
                api(thingifier, hooks).post(jsonRequest("/todos", "{\"title\":\"valid\"}"));

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void afterValidationHookCanReplaceValidationFailureWithValidCommand() {
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        hooks.registerAfterValidationHook(
                context -> {
                    context.replaceWriteCommand(
                            new CreateThingCommand(
                                    "todo",
                                    List.of(new NamedValue("title", "after validation")),
                                    List.of(),
                                    true));
                    context.clearValidationResult();
                });

        HttpApiResponse response = api(todoThingifier(), hooks).post(jsonRequest("/todos", "{}"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals("after validation", returnedTitle(response));
    }

    @Test
    void beforeActionHookCanReplaceReadQueryBeforeExecution() {
        Thingifier thingifier = todoThingifier();
        createTodo(thingifier, "visible");
        createTodo(thingifier, "hidden");
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        hooks.registerBeforeActionHook(
                context ->
                        context.replaceReadQuery(
                                new ReadCollectionQuery("todo", queryParams("title", "visible"))));
        HttpApiRequest request =
                new HttpApiRequest("/todos").setFilterableQueryParams("title=hidden");

        HttpApiResponse response = api(thingifier, hooks).get(request);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("visible", returnedCollectionTitle(response));
    }

    @Test
    void beforeActionHookCanReplaceWriteCommandBeforeExecution() {
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        hooks.registerBeforeActionHook(
                context ->
                        context.replaceWriteCommand(
                                new CreateThingCommand(
                                        "todo",
                                        List.of(new NamedValue("title", "from action hook")),
                                        List.of(),
                                        true)));

        HttpApiResponse response =
                api(todoThingifier(), hooks).post(jsonRequest("/todos", "{\"title\":\"valid\"}"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals("from action hook", returnedTitle(response));
    }

    @Test
    void afterActionHookCanInspectCreatedResultAndMutateApiResponse() {
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        AtomicReference<String> createdTitle = new AtomicReference<>();
        hooks.registerAfterActionHook(
                context -> {
                    createdTitle.set(
                            context.writeCommandResult()
                                    .getInstance()
                                    .getFieldValue("title")
                                    .asString());
                    context.apiResponse().setHeader("X-Hook-Phase", "after-action");
                });

        HttpApiResponse response =
                api(todoThingifier(), hooks).post(jsonRequest("/todos", "{\"title\":\"created\"}"));

        Assertions.assertEquals("created", createdTitle.get());
        Assertions.assertEquals("after-action", response.getHeaders().get("X-Hook-Phase"));
    }

    @Test
    void afterActionHookCanInspectUpdatedResult() {
        Thingifier thingifier = todoThingifier();
        EntityInstance todo = createTodo(thingifier, "before");
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        AtomicReference<String> updatedTitle = new AtomicReference<>();
        hooks.registerAfterActionHook(
                context ->
                        updatedTitle.set(
                                context.writeCommandResult()
                                        .getInstance()
                                        .getFieldValue("title")
                                        .asString()));

        HttpApiResponse response =
                api(thingifier, hooks)
                        .post(
                                jsonRequest(
                                        "/todos/" + todo.getPrimaryKeyValue(),
                                        "{\"title\":\"updated\"}"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("updated", updatedTitle.get());
    }

    @Test
    void afterActionHookCanInspectDeletedResult() {
        Thingifier thingifier = todoThingifier();
        EntityInstance todo = createTodo(thingifier, "delete me");
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        AtomicBoolean deletedSuccessfully = new AtomicBoolean(false);
        hooks.registerAfterActionHook(
                context -> deletedSuccessfully.set(context.writeCommandResult().isSuccessful()));

        HttpApiResponse response =
                api(thingifier, hooks)
                        .delete(new HttpApiRequest("/todos/" + todo.getPrimaryKeyValue()));

        Assertions.assertEquals(204, response.getStatusCode());
        Assertions.assertTrue(deletedSuccessfully.get());
    }

    @Test
    void afterActionHookCanInspectReadResultAndMutateApiResponse() {
        Thingifier thingifier = todoThingifier();
        createTodo(thingifier, "read me");
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        AtomicInteger resultSize = new AtomicInteger();
        hooks.registerAfterActionHook(
                context -> {
                    resultSize.set(context.readQueryResult().getListEntityInstances().size());
                    context.apiResponse().setHeader("X-Read-Hook", "inspected");
                });

        HttpApiResponse response = api(thingifier, hooks).get(new HttpApiRequest("/todos"));

        Assertions.assertEquals(1, resultSize.get());
        Assertions.assertEquals("inspected", response.getHeaders().get("X-Read-Hook"));
    }

    @Test
    void afterActionHookChangingWriteResultToErrorRollsBackCommand() {
        Thingifier thingifier = todoThingifier();
        ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        hooks.registerAfterActionHook(
                context -> context.replaceWriteCommandResult(ThingCommandResult.error("rollback")));

        HttpApiResponse response =
                api(thingifier, hooks).post(jsonRequest("/todos", "{\"title\":\"created\"}"));

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void lifecycleHooksRunBetweenExistingRequestAndResponseHooks() {
        ThingifierApiLifecycleHookRegistry lifecycleHooks =
                new ThingifierApiLifecycleHookRegistry();
        List<String> calls = new ArrayList<>();
        lifecycleHooks.registerRouteMatchedHook(context -> calls.add("route matched"));
        lifecycleHooks.registerBodyParsedHook(context -> calls.add("body parsed"));
        lifecycleHooks.registerBeforeValidationHook(context -> calls.add("before validation"));
        lifecycleHooks.registerAfterValidationHook(context -> calls.add("after validation"));
        lifecycleHooks.registerBeforeActionHook(context -> calls.add("before action"));
        lifecycleHooks.registerAfterActionHook(context -> calls.add("after action"));
        HttpApiHookRegistry httpHooks = new HttpApiHookRegistry();
        httpHooks.registerRequestHook(
                (request, config) -> {
                    calls.add("request");
                    return null;
                });
        httpHooks.registerResponseHook(
                (request, response, config) -> {
                    calls.add("response");
                    return null;
                });

        ThingifierHttpApi.withHookRegistries(todoThingifier(), httpHooks, lifecycleHooks)
                .get(new HttpApiRequest("/todos"));

        Assertions.assertEquals(
                List.of(
                        "request",
                        "route matched",
                        "body parsed",
                        "before validation",
                        "after validation",
                        "before action",
                        "after action",
                        "response"),
                calls);
    }

    @Test
    void existingRequestHookShortCircuitSkipsLifecycleHooks() {
        ThingifierApiLifecycleHookRegistry lifecycleHooks =
                new ThingifierApiLifecycleHookRegistry();
        AtomicInteger callCount = new AtomicInteger();
        lifecycleHooks.registerRouteMatchedHook(context -> callCount.incrementAndGet());
        HttpApiHookRegistry httpHooks = new HttpApiHookRegistry();
        httpHooks.registerRequestHook(
                (request, config) ->
                        new HttpApiResponse(
                                null, ApiResponse.error(409, "existing hook"), null, config));

        HttpApiResponse response =
                ThingifierHttpApi.withHookRegistries(todoThingifier(), httpHooks, lifecycleHooks)
                        .get(new HttpApiRequest("/todos"));

        Assertions.assertEquals(409, response.getStatusCode());
        Assertions.assertEquals(0, callCount.get());
    }

    private void perform(
            final Thingifier thingifier,
            final ThingifierApiLifecycleHookRegistry hooks,
            final ThingifierHttpApi.HttpVerb verb) {
        ThingifierHttpApi api = api(thingifier, hooks);
        switch (verb) {
            case GET:
                api.get(new HttpApiRequest("/todos"));
                break;
            case HEAD:
                api.head(new HttpApiRequest("/todos"));
                break;
            case QUERY:
                api.queryRequest(new HttpApiRequest("/todos"));
                break;
            case POST:
                api.post(jsonRequest("/todos", "{\"title\":\"created\"}"));
                break;
            case PUT:
                api.put(jsonRequest("/todos/123", "{\"title\":\"replaced\"}"));
                break;
            case PATCH:
                api.patch(jsonRequest("/todos/123", "{\"title\":\"patched\"}"));
                break;
            case DELETE:
                api.delete(new HttpApiRequest("/todos/123"));
                break;
            default:
                throw new IllegalArgumentException("Unsupported test verb " + verb);
        }
    }

    private ThingifierHttpApi api(
            final Thingifier thingifier, final ThingifierApiLifecycleHookRegistry hooks) {
        return new ThingifierHttpApi(thingifier, null, null, hooks);
    }

    private Thingifier todoThingifier() {
        Thingifier thingifier = TodoManagerModel.definedAsThingifier();
        thingifier.apiConfig().setApiToEnforceAcceptHeaderForResponses(false);
        thingifier.apiConfig().setApiToEnforceContentTypeForRequests(false);
        return thingifier;
    }

    private HttpApiRequest jsonRequest(final String path, final String body) {
        return new HttpApiRequest(path).addHeader("Content-Type", "application/json").setBody(body);
    }

    private String returnedTitle(final HttpApiResponse response) {
        return response.apiResponse().getReturnedInstance().getFieldValue("title").asString();
    }

    private String returnedCollectionTitle(final HttpApiResponse response) {
        return response.apiResponse()
                .getReturnedInstanceCollection()
                .get(0)
                .getFieldValue("title")
                .asString();
    }

    private EntityInstance createTodo(final Thingifier thingifier, final String title) {
        return create(thingifier, "todo", title);
    }

    private EntityInstance createProject(final Thingifier thingifier, final String title) {
        return create(thingifier, "project", title);
    }

    private EntityInstance create(
            final Thingifier thingifier, final String entityName, final String title) {
        EntityDefinition entity = thingifier.getDefinitionNamed(entityName);
        return storeFor(thingifier)
                .entities()
                .create(EntityInstanceDraft.forEntity(entity).withField("title", title));
    }

    private int todoCount(final Thingifier thingifier) {
        return storeFor(thingifier).entityQueries().count(thingifier.getDefinitionNamed("todo"));
    }

    private QueryFilterParams queryParams(final String fieldName, final String fieldValue) {
        QueryFilterParams params = new QueryFilterParams();
        params.put(fieldName, fieldValue);
        return params;
    }

    private ThingStore storeFor(final Thingifier thingifier) {
        return thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
    }
}
