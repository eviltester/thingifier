package uk.co.compendiumdev.thingifier.api.response;

import static uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation.UPDATE;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.AUTO_INCREMENT;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.DefaultThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.RouteApiResponsePolicyApplier;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticationResult;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthorizationResult;
import uk.co.compendiumdev.thingifier.api.spec.ResponseShape;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

class RouteApiResponsePolicyTest {

    @Test
    void successPolicyCanOverrideStatusAndSuppressBodyForFixedUpdateRoute() {
        final Thingifier thingifier = secretModel();
        postSecretTokenRoute(thingifier).onSuccess().status(201).suppressBody();
        createSecretToken(thingifier, "token", "issued-value");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(jsonPost("/secret/token", "{\"value\":\"new-value\"}"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals("", response.getBody());
        Assertions.assertEquals(
                "new-value", secretToken(thingifier, "token").getFieldValue("value").asString());
    }

    @Test
    void successPolicyCanAddHeaderFromReturnedInstanceField() {
        final Thingifier thingifier = secretModel();
        getSecretTokenRoute(thingifier)
                .onSuccess()
                .addInstanceFieldAsHeader("X-Secret-Token", "value");
        createSecretToken(thingifier, "token", "issued-value");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secret/token"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("issued-value", response.getHeaders().get("X-Secret-Token"));
    }

    @Test
    void instanceFieldHeaderIsSkippedWhenReturnedInstanceDoesNotHaveTheField() {
        final Thingifier thingifier = secretModel();
        getSecretTokenRoute(thingifier)
                .onSuccess()
                .addInstanceFieldAsHeader("X-Missing", "missing");
        createSecretToken(thingifier, "token", "issued-value");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secret/token"));

        Assertions.assertFalse(response.getHeaders().headerExists("X-Missing"));
    }

    @Test
    void routePolicyDoesNotAffectAnotherRouteReturningTheSameInstance() {
        final Thingifier thingifier = secretModel();
        getSecretTokenRoute(thingifier)
                .onSuccess()
                .addInstanceFieldAsHeader("X-Secret-Token", "value");
        createSecretToken(thingifier, "token", "issued-value");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secrettokens/token"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertFalse(response.getHeaders().headerExists("X-Secret-Token"));
    }

    @Test
    void successPolicyCanSelectResponseEntityView() {
        final Thingifier thingifier = secretModel();
        getSecretNoteRoute(thingifier).onSuccess().bodyUsingEntityView("PublicNote");
        createSecretNote(thingifier, "note", "visible", "hidden-internal-value");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secret/note"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("visible"));
        Assertions.assertFalse(response.getBody().contains("internal"));
        Assertions.assertFalse(response.getBody().contains("hidden-internal-value"));
    }

    @Test
    void errorPolicyCanReplaceGeneratedErrorBody() {
        final Thingifier thingifier = secretModel();
        getSecretNoteRoute(thingifier).onError(404).bodyText("not here");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secret/note"));

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals("not here", response.getBody());
    }

    @Test
    void validationPolicyWinsOverStatusSpecificErrorPolicy() {
        final Thingifier thingifier = todoModel();
        final ThingifierApiRouteRule route = thingifier.apiSpec().route(RoutingVerb.POST, "/todos");
        route.onError(422).bodyText("generic");
        route.onValidationError().status(499).bodyText("validation");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).post(jsonPost("/todos", "{}"));

        Assertions.assertEquals(499, response.getStatusCode());
        Assertions.assertEquals("validation", response.getBody());
    }

    @Test
    void errorPolicyCanSuppressEarlyAcceptHeaderResponseBody() {
        final Thingifier thingifier = secretModel();
        thingifier.apiConfig().setApiToAllowJsonForResponses(false);
        getSecretNoteRoute(thingifier).onError(406).suppressBody();

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .get(
                                new HttpApiRequest("/secret/note")
                                        .addHeader("Accept", "application/json"));

        Assertions.assertEquals(406, response.getStatusCode());
        Assertions.assertEquals("", response.getBody());
    }

    @Test
    void errorPolicyCanRemoveGeneratedAuthChallengeHeader() {
        final Thingifier thingifier = secretModel();
        protectSecretTokenWithBearer(thingifier).onError(401).removeHeader("WWW-Authenticate");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secret/token"));

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertFalse(response.getHeaders().headerExists("WWW-Authenticate"));
    }

    @Test
    void conditionalExactHeaderPolicyAppliesAfterDefaultErrorPolicy() {
        final Thingifier thingifier = secretModel();
        final ThingifierApiRouteRule route = protectSecretTokenWithBasic(thingifier);
        route.onError(401)
                .header("WWW-Authenticate", "Basic realm=\"User Visible Realm\"")
                .bodyText("default body");
        route.onErrorWhen(401)
                .whenRequestHeader("X-Embedded-Client", "true")
                .removeHeader("WWW-Authenticate")
                .suppressBody();

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .get(jsonRequest("/secret/token").addHeader("X-Embedded-Client", "true"));

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertFalse(response.getHeaders().headerExists("WWW-Authenticate"));
        Assertions.assertEquals("", response.getBody());
    }

    @Test
    void conditionalExactHeaderPolicyIsSkippedWhenHeaderValueDiffers() {
        final Thingifier thingifier = secretModel();
        final ThingifierApiRouteRule route = protectSecretTokenWithBasic(thingifier);
        route.onError(401)
                .header("WWW-Authenticate", "Basic realm=\"User Visible Realm\"")
                .bodyText("default body");
        route.onErrorWhen(401)
                .whenRequestHeader("X-Embedded-Client", "true")
                .removeHeader("WWW-Authenticate")
                .suppressBody();

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .get(jsonRequest("/secret/token").addHeader("X-Embedded-Client", "false"));

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertEquals(
                "Basic realm=\"User Visible Realm\"",
                response.getHeaders().get("WWW-Authenticate"));
        Assertions.assertEquals("default body", response.getBody());
    }

    @Test
    void conditionalHeaderPresentPolicyAppliesWhenHeaderExists() {
        final Thingifier thingifier = secretModel();
        final ThingifierApiRouteRule route = getSecretNoteRoute(thingifier);
        route.onError(404).bodyText("not found");
        route.onErrorWhen(404).whenRequestHeaderPresent("X-Quiet").suppressBody();

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .get(jsonRequest("/secret/note").addHeader("X-Quiet", "true"));

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals("", response.getBody());
    }

    @Test
    void conditionalHeaderMissingPolicyAppliesWhenHeaderIsAbsent() {
        final Thingifier thingifier = secretModel();
        final ThingifierApiRouteRule route = getSecretNoteRoute(thingifier);
        route.onError(404).bodyText("not found");
        route.onErrorWhen(404).whenRequestHeaderMissing("X-Debug").bodyText("quiet missing");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secret/note"));

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals("quiet missing", response.getBody());
    }

    @Test
    void multipleConditionalPoliciesApplyInDeclarationOrder() {
        final Thingifier thingifier = secretModel();
        final ThingifierApiRouteRule route = getSecretNoteRoute(thingifier);
        route.onError(404).header("X-Stage", "default");
        route.onErrorWhen(404).whenRequestHeaderPresent("X-Mode").header("X-Stage", "first");
        route.onErrorWhen(404).whenRequestHeaderPresent("X-Mode").header("X-Stage", "second");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .get(jsonRequest("/secret/note").addHeader("X-Mode", "test"));

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals("second", response.getHeaders().get("X-Stage"));
    }

    @Test
    void errorPolicyRunsForAuthorizerRejection() {
        final Thingifier thingifier = secretModel();
        protectSecretTokenWithBearer(thingifier)
                .authorizeWith(context -> ThingifierApiAuthorizationResult.forbidden())
                .onError(403)
                .suppressBody();

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .get(jsonRequest("/secret/token").addHeader("Authorization", "Bearer ok"));

        Assertions.assertEquals(403, response.getStatusCode());
        Assertions.assertEquals("", response.getBody());
    }

    @Test
    void conditionalPolicyCanRemoveCustomAuthenticatorRejectionHeader() {
        final Thingifier thingifier = secretModel();
        thingifier
                .apiSpec()
                .authenticator(
                        "secretBearer",
                        context -> {
                            final ApiResponse response =
                                    ApiResponse.error(401, "custom rejection")
                                            .setHeader("WWW-Authenticate", "Custom");
                            return ThingifierApiAuthenticationResult.rejected(response);
                        });
        final ThingifierApiRouteRule route =
                getSecretTokenRoute(thingifier).secureWithBearerAuth("secretBearer");
        route.onError(401).header("X-Default-Policy", "applied");
        route.onErrorWhen(401)
                .whenRequestHeaderPresent("X-No-Challenge")
                .removeHeader("WWW-Authenticate")
                .suppressBody();

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .get(
                                jsonRequest("/secret/token")
                                        .addHeader("Authorization", "Bearer rejected")
                                        .addHeader("X-No-Challenge", "true"));

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertFalse(response.getHeaders().headerExists("WWW-Authenticate"));
        Assertions.assertEquals("applied", response.getHeaders().get("X-Default-Policy"));
        Assertions.assertEquals("", response.getBody());
    }

    @Test
    void responseHookReceivesPolicyShapedAuthFailureResponse() {
        final Thingifier thingifier = secretModel();
        protectSecretTokenWithBearer(thingifier)
                .onError(401)
                .removeHeader("WWW-Authenticate")
                .suppressBody();
        final AtomicReference<HttpApiResponse> observedResponse = new AtomicReference<>();
        final HttpApiResponseHook hook =
                (request, response, config) -> {
                    observedResponse.set(response);
                    return null;
                };

        new ThingifierHttpApi(thingifier, null, List.of(hook)).get(jsonRequest("/secret/token"));

        Assertions.assertNotNull(observedResponse.get());
        Assertions.assertEquals(401, observedResponse.get().getStatusCode());
        Assertions.assertFalse(
                observedResponse.get().getHeaders().headerExists("WWW-Authenticate"));
        Assertions.assertEquals("", observedResponse.get().getBody());
    }

    @Test
    void directApiAppliesConditionalErrorPolicyUsingRequestHeaders() {
        final Thingifier thingifier = secretModel();
        getSecretNoteRoute(thingifier)
                .onErrorWhen(404)
                .whenRequestHeaderPresent("X-Quiet")
                .suppressBody();
        final HttpHeadersBlock headers = new HttpHeadersBlock();
        headers.put("X-Quiet", "true");

        final ApiResponse response =
                thingifier.api().get("secret/note", new QueryFilterParams(), headers);

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertFalse(response.hasABody());
    }

    @Test
    void conditionalErrorPolicyHeadersAreAddedToGeneratedRouteDocumentation() {
        final Thingifier thingifier = secretModel();
        getSecretTokenRoute(thingifier)
                .onErrorWhen(401)
                .whenRequestHeaderPresent("X-Client")
                .header("WWW-Authenticate", "Bearer");

        final RoutingDefinition route =
                route(
                        new ApiRoutingDefinitionDocGenerator(thingifier).generate(""),
                        RoutingVerb.GET,
                        "secret/token");

        Assertions.assertTrue(
                route.getPossibleStatusReponses().stream()
                        .anyMatch(status -> status.value() == 401));
        Assertions.assertEquals("Bearer", route.getResponseHeaderValue("WWW-Authenticate"));
    }

    @Test
    void directApiAppliesSuccessPolicy() {
        final Thingifier thingifier = secretModel();
        getSecretTokenRoute(thingifier)
                .onSuccess()
                .addInstanceFieldAsHeader("X-Secret-Token", "value");
        createSecretToken(thingifier, "token", "issued-value");

        final ApiResponse response =
                thingifier
                        .api()
                        .get("secret/token", new QueryFilterParams(), new HttpHeadersBlock());

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("issued-value", response.getHeaders().get("X-Secret-Token"));
    }

    @Test
    void singleInstanceShapeRejectsMultipleReturnedInstances() {
        final Thingifier thingifier = secretModel();
        getSecretNoteRoute(thingifier).respondWithSingleInstance();
        final EntityInstance first = createSecretNote(thingifier, "note", "first", "internal");
        final EntityInstance second = createSecretNote(thingifier, "other", "second", "internal");

        final ApiResponse response =
                new RouteApiResponsePolicyApplier(new DefaultThingifierApiRuntime(thingifier))
                        .apply(
                                RoutingVerb.GET,
                                "secret/note",
                                ApiResponse.success()
                                        .returnInstanceCollection(List.of(first, second)),
                                null);

        Assertions.assertEquals(500, response.getStatusCode());
        Assertions.assertTrue(response.isErrorResponse());
        Assertions.assertTrue(
                response.getErrorMessages()
                        .contains(
                                "Route secret/note is configured for a single instance response but returned 2 instances"));
    }

    @Test
    void collectionShapeWrapsSingleReturnedInstanceForFixedRoute() {
        final Thingifier thingifier = secretModel();
        getSecretNoteRoute(thingifier).responseShape(ResponseShape.COLLECTION);
        createSecretNote(thingifier, "note", "visible", "internal");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("/secret/note"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.apiResponse().isCollection());
        Assertions.assertTrue(response.getBody().contains("secretnotes"));
    }

    @Test
    void responsePolicyHeadersAreAddedToGeneratedRouteDocumentation() {
        final Thingifier thingifier = secretModel();
        getSecretTokenRoute(thingifier)
                .onSuccess()
                .status(201)
                .header("X-Static", "configured")
                .addInstanceFieldAsHeader("X-Secret-Token", "value");

        final RoutingDefinition route =
                route(
                        new ApiRoutingDefinitionDocGenerator(thingifier).generate(""),
                        RoutingVerb.GET,
                        "secret/token");

        Assertions.assertTrue(
                route.getPossibleStatusReponses().stream()
                        .anyMatch(status -> status.value() == 201));
        Assertions.assertEquals("configured", route.getResponseHeaderValue("X-Static"));
        Assertions.assertEquals(
                "Value from returned instance field value",
                route.getResponseHeaderValue("X-Secret-Token"));
    }

    private ThingifierApiRouteRule getSecretNoteRoute(final Thingifier thingifier) {
        return thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/secret/note")
                .mapsToEntity("secretnote")
                .withFixedIdentifier("note");
    }

    private ThingifierApiRouteRule getSecretTokenRoute(final Thingifier thingifier) {
        return thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/secret/token")
                .mapsToEntity("secrettoken")
                .withFixedIdentifier("token");
    }

    private ThingifierApiRouteRule postSecretTokenRoute(final Thingifier thingifier) {
        return thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/secret/token")
                .mapsToEntity("secrettoken")
                .withFixedIdentifier("token")
                .entityCan(UPDATE);
    }

    private ThingifierApiRouteRule protectSecretTokenWithBearer(final Thingifier thingifier) {
        thingifier
                .apiSpec()
                .authenticator(
                        "secretBearer",
                        context -> ThingifierApiAuthenticationResult.authenticated("principal"));
        return getSecretTokenRoute(thingifier).secureWithBearerAuth("secretBearer");
    }

    private ThingifierApiRouteRule protectSecretTokenWithBasic(final Thingifier thingifier) {
        thingifier.apiSpec().security().basic("secretBasic", "User Visible Realm");
        thingifier
                .apiSpec()
                .authenticator(
                        "secretBasic",
                        context -> ThingifierApiAuthenticationResult.authenticated("principal"));
        return getSecretTokenRoute(thingifier).secureWithBasicAuth("secretBasic");
    }

    private Thingifier secretModel() {
        final Thingifier thingifier = new Thingifier();

        final EntityDefinition note = thingifier.defineThing("secretnote", "secretnotes", 10);
        note.addAsPrimaryKeyField(Field.is("id", STRING));
        note.addField(Field.is("text", STRING));
        note.addField(Field.is("internal", STRING));
        note.defineView("PublicNote").hideResponseFields("internal");

        final EntityDefinition token = thingifier.defineThing("secrettoken", "secrettokens", 10);
        token.addAsPrimaryKeyField(Field.is("id", STRING));
        token.addField(Field.is("value", STRING));

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
            final String id,
            final String text,
            final String internal) {
        final EntityDefinition note = thingifier.getDefinitionNamed("secretnote");
        return thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(note)
                                .withField("id", id)
                                .withField("text", text)
                                .withField("internal", internal));
    }

    private EntityInstance createSecretToken(
            final Thingifier thingifier, final String id, final String value) {
        final EntityDefinition token = thingifier.getDefinitionNamed("secrettoken");
        return thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(token)
                                .withField("id", id)
                                .withField("value", value));
    }

    private EntityInstance secretToken(final Thingifier thingifier, final String id) {
        return thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entityQueries()
                .findByPrimaryKey(thingifier.getDefinitionNamed("secrettoken"), id);
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
}
