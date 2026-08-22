package uk.co.compendiumdev.thingifier.api.validation;

import static uk.co.compendiumdev.thingifier.api.security.DataScopeCreationPolicy.ENSURE_EXISTS;
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
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticationResult;
import uk.co.compendiumdev.thingifier.api.spec.FixedResourcePolicy;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

class ApiOperationValidationTest {

    @Test
    void routeRuleStoresNamedOperationValidatorsInRegistrationOrder() {
        final Thingifier thingifier = todoModel();
        final ApiOperationValidator accept = context -> ApiOperationValidationResult.accept();

        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/todos")
                .withApiOperationValidator("first", accept)
                .withApiOperationValidator("second", accept);

        final List<ApiOperationValidatorDefinition> validators =
                thingifier
                        .apiSpec()
                        .ruleFor(RoutingVerb.POST, "/todos", "")
                        .orElseThrow()
                        .apiOperationValidators();

        Assertions.assertEquals("first", validators.get(0).name());
        Assertions.assertEquals("second", validators.get(1).name());
    }

    @Test
    void requiredBodyFieldsValidatorRejectsMissingRequestField() {
        final Thingifier thingifier = todoModel();
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/todos")
                .withApiOperationValidator(
                        "requiresApiToken",
                        ApiOperationValidators.requireBodyFields("apiToken")
                                .onMissing(422, "apiToken is required for this operation"));

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(jsonRequest("/todos", "{\"title\":\"walk\"}"));

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("apiToken is required"));
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void operationValidatorRejectionStopsEntityValidationAndMutation() {
        final Thingifier thingifier = todoModel();
        final AtomicInteger instanceValidatorCalls = new AtomicInteger();
        thingifier
                .getDefinitionNamed("todo")
                .withInstanceValidation(
                        context -> {
                            instanceValidatorCalls.incrementAndGet();
                            return new ValidationReport();
                        });
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/todos")
                .withApiOperationValidator(
                        "routeGate",
                        context ->
                                ApiOperationValidationResult.reject(
                                        409, "operation rejected before model validation"));

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(jsonRequest("/todos", "{\"title\":\"walk\"}"));

        Assertions.assertEquals(409, response.getStatusCode());
        Assertions.assertEquals(0, instanceValidatorCalls.get());
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void operationValidatorsRunInOrderAndStopAfterFirstRejection() {
        final Thingifier thingifier = todoModel();
        final List<String> calls = new ArrayList<>();
        thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/todos")
                .withApiOperationValidator(
                        "first",
                        context -> {
                            calls.add("first");
                            return ApiOperationValidationResult.accept();
                        })
                .withApiOperationValidator(
                        "second",
                        context -> {
                            calls.add("second");
                            return ApiOperationValidationResult.reject(403, "not for this route");
                        })
                .withApiOperationValidator(
                        "third",
                        context -> {
                            calls.add("third");
                            return ApiOperationValidationResult.accept();
                        });

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(new HttpApiRequest("/todos"));

        Assertions.assertEquals(403, response.getStatusCode());
        Assertions.assertEquals(List.of("first", "second"), calls);
    }

    @Test
    void authenticationFailureSkipsOperationValidators() {
        final Thingifier thingifier = todoModel();
        final AtomicInteger validatorCalls = new AtomicInteger();
        thingifier
                .apiSpec()
                .authenticator(
                        "todoToken",
                        context ->
                                ThingifierApiAuthenticationResult.authenticated("todo-principal"));
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/todos")
                .secureWithBearerAuth("todoToken")
                .withApiOperationValidator(
                        "counter",
                        context -> {
                            validatorCalls.incrementAndGet();
                            return ApiOperationValidationResult.accept();
                        });

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(jsonRequest("/todos", "{\"title\":\"walk\"}"));

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertEquals(0, validatorCalls.get());
    }

    @Test
    void invalidJsonSkipsOperationValidators() {
        final Thingifier thingifier = todoModel();
        final AtomicInteger validatorCalls = new AtomicInteger();
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/todos")
                .withApiOperationValidator(
                        "counter",
                        context -> {
                            validatorCalls.incrementAndGet();
                            return ApiOperationValidationResult.accept();
                        });

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).post(jsonRequest("/todos", "{\"title\""));

        Assertions.assertEquals(400, response.getStatusCode());
        Assertions.assertEquals(0, validatorCalls.get());
    }

    @Test
    void methodNotAllowedSkipsOperationValidators() {
        final Thingifier thingifier = todoModel();
        final AtomicInteger validatorCalls = new AtomicInteger();
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/todos")
                .methodNotAllowed()
                .withApiOperationValidator(
                        "counter",
                        context -> {
                            validatorCalls.incrementAndGet();
                            return ApiOperationValidationResult.accept();
                        });

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(jsonRequest("/todos", "{\"title\":\"walk\"}"));

        Assertions.assertEquals(405, response.getStatusCode());
        Assertions.assertEquals(0, validatorCalls.get());
    }

    @Test
    void directApiPostEnforcesOperationValidator() {
        final Thingifier thingifier = todoModel();
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/todos")
                .withApiOperationValidator(
                        "directGate",
                        context ->
                                ApiOperationValidationResult.reject(
                                        451, "direct operation rejected"));

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"walk\"}"),
                                new HttpHeadersBlock());

        Assertions.assertEquals(451, response.getStatusCode());
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void httpFixedRouteContextIncludesAuthSelectedScopeAndResolvedTarget() {
        final Thingifier thingifier = secretNoteModel();
        ensureDataScopeExists(thingifier, "tenant-one");
        createSecretNote(thingifier, "tenant-one", "original");
        final AtomicReference<ApiOperationValidationContext> seenContext = new AtomicReference<>();
        thingifier
                .apiSpec()
                .authenticator(
                        "secretToken",
                        context ->
                                ThingifierApiAuthenticationResult.authenticated("secret-principal")
                                        .useDataScope("tenant-one", ENSURE_EXISTS));
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/secret/note")
                .mapsToEntity("secretnote")
                .withFixedIdentifier("note", FixedResourcePolicy.RETURN_404)
                .entityCan(UPDATE)
                .entityView("PublicNote")
                .secureWithBearerAuth("secretToken")
                .withApiOperationValidator(
                        "captureContext",
                        context -> {
                            seenContext.set(context);
                            return ApiOperationValidationResult.accept();
                        });

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(
                                jsonRequest("/secret/note", "{\"body\":\"updated\"}")
                                        .addHeader("Authorization", "Bearer valid-token"));

        final ApiOperationValidationContext context = seenContext.get();
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(context);
        Assertions.assertEquals(RoutingVerb.POST, context.verb());
        Assertions.assertEquals("secret/note", context.publicPath());
        Assertions.assertEquals("secretnote", context.targetEntityName());
        Assertions.assertEquals("note", context.targetIdentifier());
        Assertions.assertEquals("UPDATE", context.operationType());
        Assertions.assertEquals("secret-principal", context.authenticatedPrincipal());
        Assertions.assertEquals("tenant-one", context.dataScopeName());
        Assertions.assertSame(thingifier.getStore("tenant-one"), context.store());
        Assertions.assertTrue(context.headers().get("Authorization").startsWith("Bearer "));
        Assertions.assertEquals("updated", context.requestBody().asStringMap().get("body"));
        Assertions.assertTrue(context.rawBody().contains("updated"));
        Assertions.assertEquals("PublicNote", context.requestEntityView());
        Assertions.assertEquals("PublicNote", context.responseEntityView());
        Assertions.assertEquals("updated", secretNoteBody(thingifier, "tenant-one"));
    }

    private Thingifier todoModel() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition todo = thingifier.defineThing("todo", "todos", 5);
        todo.addAsPrimaryKeyField(Field.is("id", AUTO_INCREMENT));
        todo.addField(Field.is("title", STRING).makeMandatory());
        return thingifier;
    }

    private Thingifier secretNoteModel() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition note = thingifier.defineThing("secretnote", "secretnotes", 1);
        note.addAsPrimaryKeyField(Field.is("id", STRING));
        note.addField(Field.is("body", STRING));
        note.addField(Field.is("internal", STRING));
        note.defineView("PublicNote").hideResponseFields("internal");
        return thingifier;
    }

    private void ensureDataScopeExists(final Thingifier thingifier, final String dataScopeName) {
        thingifier.getERmodel().createInstanceDatabaseIfNotExisting(dataScopeName);
    }

    private void createSecretNote(
            final Thingifier thingifier, final String dataScopeName, final String body) {
        storeFor(thingifier, dataScopeName)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(thingifier.getDefinitionNamed("secretnote"))
                                .withField("id", "note")
                                .withField("body", body)
                                .withField("internal", "hidden"));
    }

    private String secretNoteBody(final Thingifier thingifier, final String dataScopeName) {
        final EntityInstance note =
                storeFor(thingifier, dataScopeName)
                        .entityQueries()
                        .findByQueryIdentifier(thingifier.getDefinitionNamed("secretnote"), "note");
        return note.getFieldValue("body").asString();
    }

    private int todoCount(final Thingifier thingifier) {
        return storeFor(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME)
                .entityQueries()
                .count(thingifier.getDefinitionNamed("todo"));
    }

    private ThingStore storeFor(final Thingifier thingifier, final String dataScopeName) {
        return thingifier.getStore(dataScopeName);
    }

    private HttpApiRequest jsonRequest(final String path, final String body) {
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
