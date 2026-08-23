package uk.co.compendiumdev.thingifier.api.callbacks;

import static uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation.UPDATE;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.AUTO_INCREMENT;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.security.DataScopeCreationPolicy;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticationResult;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

class RouteOperationCallbackTest {

    @Test
    void afterSuccessfulOperationReceivesFixedRouteUpdateContextAndInstance() {
        final Thingifier thingifier = secretModel();
        createSecretNote(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME, "note", "old");
        final AtomicReference<ThingifierApiOperationContext> seenContext = new AtomicReference<>();
        final AtomicReference<ThingifierApiOperationResult> seenResult = new AtomicReference<>();
        postSecretNoteRoute(thingifier)
                .afterSuccessfulOperation(
                        "sync-note",
                        (context, result) -> {
                            seenContext.set(context);
                            seenResult.set(result);
                        });

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(
                                jsonPost("/secret/note", "{\"text\":\"new\"}")
                                        .addHeader("X-Trace", "trace-1"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("/secret/note", seenContext.get().publicPath());
        Assertions.assertEquals("/secret/note", seenContext.get().matchedRoutePattern());
        Assertions.assertEquals("secretnote", seenContext.get().targetEntityName().orElseThrow());
        Assertions.assertEquals("note", seenContext.get().targetIdentifier().orElseThrow());
        Assertions.assertEquals(
                EntityRelModel.DEFAULT_DATABASE_NAME, seenContext.get().dataScopeName());
        Assertions.assertEquals("trace-1", seenContext.get().requestHeaders().get("X-Trace"));
        Assertions.assertEquals("{\"text\":\"new\"}", seenContext.get().rawRequestBody());
        Assertions.assertEquals(200, seenResult.get().statusCode());
        Assertions.assertTrue(seenResult.get().successful());
        Assertions.assertTrue(seenResult.get().updated());
        Assertions.assertEquals(
                "new", seenResult.get().singleInstance().getFieldValue("text").asString());
    }

    @Test
    void callbackReceivesAuthSelectedDataScopeAndPrincipal() {
        final Thingifier thingifier = secretModel();
        createSecretNote(thingifier, "tenant-one", "note", "tenant-old");
        thingifier
                .apiSpec()
                .authenticator(
                        "tenantToken",
                        context ->
                                ThingifierApiAuthenticationResult.authenticated("tenant-principal")
                                        .useDataScope(
                                                "tenant-one",
                                                DataScopeCreationPolicy.USE_EXISTING_ONLY));
        final AtomicReference<ThingifierApiOperationContext> seenContext = new AtomicReference<>();
        postSecretNoteRoute(thingifier)
                .secureWithBearerAuth("tenantToken")
                .afterSuccessfulOperation(
                        "capture-tenant", (context, result) -> seenContext.set(context));

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(
                                jsonPost("/secret/note", "{\"text\":\"tenant-new\"}")
                                        .addHeader("Authorization", "Bearer valid-token"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("tenant-one", seenContext.get().dataScopeName());
        Assertions.assertSame(thingifier.getStore("tenant-one"), seenContext.get().store());
        Assertions.assertEquals("tenant-principal", seenContext.get().authenticatedPrincipal());
        Assertions.assertEquals(
                "tenant-new",
                secretNote(thingifier, "tenant-one", "note").getFieldValue("text").asString());
    }

    @Test
    void afterSuccessfulOperationDoesNotRunForValidationFailure() {
        final Thingifier thingifier = todoModel();
        final AtomicInteger callbackCount = new AtomicInteger();
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/todos")
                .afterSuccessfulOperation((context, result) -> callbackCount.incrementAndGet());

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).post(jsonPost("/todos", "{}"));

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertEquals(0, callbackCount.get());
    }

    @Test
    void afterFailedOperationRunsForValidationFailure() {
        final Thingifier thingifier = todoModel();
        final AtomicReference<ThingifierApiOperationResult> seenResult = new AtomicReference<>();
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/todos")
                .afterFailedOperation(
                        "capture-failure", (context, result) -> seenResult.set(result));

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).post(jsonPost("/todos", "{}"));

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertEquals(422, seenResult.get().statusCode());
        Assertions.assertTrue(seenResult.get().failed());
    }

    @Test
    void statusCallbackRunsOnlyForMatchingFinalStatus() {
        final Thingifier thingifier = secretModel();
        final AtomicInteger callbackCount = new AtomicInteger();
        getSecretNoteRoute(thingifier)
                .afterStatus(404, (context, result) -> callbackCount.incrementAndGet());

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secret/note"));

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals(1, callbackCount.get());
    }

    @Test
    void multipleCallbacksRunInDeclarationOrder() {
        final Thingifier thingifier = secretModel();
        createSecretNote(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME, "note", "visible");
        final List<String> calls = new ArrayList<>();
        final ThingifierApiRouteRule route = getSecretNoteRoute(thingifier);
        route.afterOperation("first", (context, result) -> calls.add("first"));
        route.afterStatus(200, (context, result) -> calls.add("second"));
        route.afterSuccessfulOperation("third", (context, result) -> calls.add("third"));

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secret/note"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(List.of("first", "second", "third"), calls);
    }

    @Test
    void callbackRunsBeforeLegacyResponseHook() {
        final Thingifier thingifier = secretModel();
        createSecretNote(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME, "note", "visible");
        final List<String> calls = new ArrayList<>();
        getSecretNoteRoute(thingifier)
                .afterSuccessfulOperation("callback", (context, result) -> calls.add("callback"));
        final HttpApiResponseHook responseHook =
                (request, response, config) -> {
                    calls.add("response hook");
                    return null;
                };

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier, null, List.of(responseHook))
                        .get(jsonRequest("/secret/note"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(List.of("callback", "response hook"), calls);
    }

    @Test
    void throwingCallbackFailsRequestByDefault() {
        final Thingifier thingifier = secretModel();
        createSecretNote(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME, "note", "visible");
        getSecretNoteRoute(thingifier)
                .afterSuccessfulOperation(
                        "explode",
                        (context, result) -> {
                            throw new IllegalStateException("boom");
                        });

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secret/note"));

        Assertions.assertEquals(500, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("explode"));
    }

    @Test
    void logAndContinueCallbackFailurePreservesOriginalResponse() {
        final Thingifier thingifier = secretModel();
        createSecretNote(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME, "note", "visible");
        getSecretNoteRoute(thingifier)
                .afterSuccessfulOperation(
                        "best-effort",
                        (context, result) -> {
                            throw new IllegalStateException("boom");
                        })
                .onCallbackFailure(CallbackFailurePolicy.LOG_AND_CONTINUE);

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secret/note"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("visible"));
    }

    @Test
    void directApiInvokesRouteCallbacks() {
        final Thingifier thingifier = secretModel();
        createSecretNote(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME, "note", "old");
        final AtomicReference<ThingifierApiOperationResult> seenResult = new AtomicReference<>();
        postSecretNoteRoute(thingifier)
                .afterSuccessfulOperation("direct", (context, result) -> seenResult.set(result));

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "secret/note",
                                parser(thingifier, "{\"text\":\"new\"}"),
                                new HttpHeadersBlock());

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "new", seenResult.get().singleInstance().getFieldValue("text").asString());
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

    private Thingifier secretModel() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition note = thingifier.defineThing("secretnote", "secretnotes", 10);
        note.addAsPrimaryKeyField(Field.is("id", STRING));
        note.addField(Field.is("text", STRING));
        return thingifier;
    }

    private Thingifier todoModel() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition todo = thingifier.defineThing("todo", "todos", 10);
        todo.addAsPrimaryKeyField(Field.is("id", AUTO_INCREMENT));
        todo.addField(Field.is("title", STRING).makeMandatory());
        return thingifier;
    }

    private EntityInstance createSecretNote(
            final Thingifier thingifier,
            final String dataScopeName,
            final String id,
            final String text) {
        thingifier.getERmodel().createInstanceDatabaseIfNotExisting(dataScopeName);
        final EntityDefinition note = thingifier.getDefinitionNamed("secretnote");
        return thingifier
                .getStore(dataScopeName)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(note)
                                .withField("id", id)
                                .withField("text", text));
    }

    private EntityInstance secretNote(
            final Thingifier thingifier, final String dataScopeName, final String id) {
        return thingifier
                .getStore(dataScopeName)
                .entityQueries()
                .findByPrimaryKey(thingifier.getDefinitionNamed("secretnote"), id);
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

    private BodyParser parser(final Thingifier thingifier, final String body) {
        return new BodyParser(
                new HttpApiRequest("/request")
                        .addHeader("Content-Type", "application/json")
                        .setBody(body),
                thingifier.getThingNames());
    }
}
