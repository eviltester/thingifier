package uk.co.compendiumdev.thingifier.api.callbacks;

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
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticationResult;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthorizationResult;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

class RouteAfterResponseCallbackTest {

    @Test
    void afterResponseReceivesFinalNegotiatedContentType() {
        final Thingifier thingifier = taskModelWithOneTask();
        final AtomicReference<ThingifierApiFinalResponse> seenResponse = new AtomicReference<>();
        thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/tasks")
                .afterResponse((context, response) -> seenResponse.set(response));

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .get(new HttpApiRequest("/tasks").addHeader("Accept", "application/xml"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("application/xml", response.getType());
        Assertions.assertEquals("application/xml", seenResponse.get().contentType());
        Assertions.assertEquals(
                "application/xml", seenResponse.get().headers().get("Content-Type"));
        Assertions.assertTrue(seenResponse.get().body().orElseThrow().contains("<tasks>"));
        Assertions.assertEquals(200, seenResponse.get().apiResponse().getStatusCode());
    }

    @Test
    void afterResponseReceivesFixedRouteTargetDetails() {
        final Thingifier thingifier = secretModel();
        createSecretNote(thingifier, "note", "visible");
        final AtomicReference<ThingifierApiOperationContext> seenContext = new AtomicReference<>();
        getSecretNoteRoute(thingifier)
                .afterResponse((context, response) -> seenContext.set(context));

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secret/note"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("/secret/note", seenContext.get().publicPath());
        Assertions.assertEquals("/secret/note", seenContext.get().matchedRoutePattern());
        Assertions.assertEquals("secretnote", seenContext.get().targetEntityName().orElseThrow());
        Assertions.assertEquals("note", seenContext.get().targetIdentifier().orElseThrow());
    }

    @Test
    void afterResponseRunsForMissingCredentialAuthFailure() {
        final Thingifier thingifier = taskModelWithOneTask();
        final AtomicReference<ThingifierApiOperationContext> seenContext = new AtomicReference<>();
        final AtomicReference<ThingifierApiFinalResponse> seenResponse = new AtomicReference<>();
        thingifier
                .apiSpec()
                .authenticator(
                        "todoToken",
                        context -> ThingifierApiAuthenticationResult.authenticated("principal"));
        thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/tasks")
                .secureWithBearerAuth("todoToken")
                .afterResponse(
                        (context, response) -> {
                            seenContext.set(context);
                            seenResponse.set(response);
                        });

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/tasks"));

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertEquals(401, seenResponse.get().statusCode());
        Assertions.assertNull(seenContext.get().authenticatedPrincipal());
    }

    @Test
    void afterResponseRunsForAuthorizerDeniedResponse() {
        final Thingifier thingifier = taskModelWithOneTask();
        final AtomicReference<ThingifierApiOperationContext> seenContext = new AtomicReference<>();
        final AtomicReference<ThingifierApiFinalResponse> seenResponse = new AtomicReference<>();
        thingifier
                .apiSpec()
                .authenticator(
                        "todoToken",
                        context ->
                                ThingifierApiAuthenticationResult.authenticated("valid-principal"));
        thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/tasks")
                .secureWithBearerAuth("todoToken")
                .authorizeWith(context -> ThingifierApiAuthorizationResult.forbidden())
                .afterResponse(
                        (context, response) -> {
                            seenContext.set(context);
                            seenResponse.set(response);
                        });

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .get(
                                jsonRequest("/tasks")
                                        .addHeader("Authorization", "Bearer valid-token"));

        Assertions.assertEquals(403, response.getStatusCode());
        Assertions.assertEquals(403, seenResponse.get().statusCode());
        Assertions.assertEquals("valid-principal", seenContext.get().authenticatedPrincipal());
    }

    @Test
    void throwingAfterResponseCallbackPreservesAlreadyRenderedResponse() {
        final Thingifier thingifier = taskModelWithOneTask();
        thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/tasks")
                .afterResponse(
                        "explode",
                        (context, response) -> {
                            throw new IllegalStateException("boom");
                        });

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/tasks"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("Task"));
    }

    @Test
    void afterResponseRunsBeforeLegacyHttpResponseHook() {
        final Thingifier thingifier = taskModelWithOneTask();
        final List<String> calls = new ArrayList<>();
        thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/tasks")
                .afterResponse((context, response) -> calls.add("after-response"));
        final HttpApiResponseHook legacyHook =
                (request, response, config) -> {
                    calls.add("legacy-response-hook");
                    return null;
                };

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier, null, List.of(legacyHook))
                        .get(jsonRequest("/tasks"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(List.of("after-response", "legacy-response-hook"), calls);
    }

    @Test
    void afterResponseDoesNotRunForUnmatchedRoute() {
        final Thingifier thingifier = taskModelWithOneTask();
        final AtomicInteger callbackCount = new AtomicInteger();
        thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/tasks")
                .afterResponse((context, response) -> callbackCount.incrementAndGet());

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/unknown"));

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals(0, callbackCount.get());
    }

    private Thingifier taskModelWithOneTask() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("id", AUTO_INCREMENT));
        task.addField(Field.is("title", STRING));
        thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(EntityInstanceDraft.forEntity(task).withField("title", "Task"));
        return thingifier;
    }

    private ThingifierApiRouteRule getSecretNoteRoute(final Thingifier thingifier) {
        return thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/secret/note")
                .mapsToEntity("secretnote")
                .withFixedIdentifier("note");
    }

    private Thingifier secretModel() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition note = thingifier.defineThing("secretnote", "secretnotes", 10);
        note.addAsPrimaryKeyField(Field.is("id", STRING));
        note.addField(Field.is("text", STRING));
        return thingifier;
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
}
