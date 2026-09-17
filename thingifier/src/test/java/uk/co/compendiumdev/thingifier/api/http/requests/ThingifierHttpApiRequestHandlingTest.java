package uk.co.compendiumdev.thingifier.api.http.requests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public class ThingifierHttpApiRequestHandlingTest {

    private Thingifier getTestThingifier() {

        Thingifier thingifier = new Thingifier();
        thingifier.apiConfig().setApiToEnforceAcceptHeaderForResponses(false);
        EntityDefinition defn = thingifier.getERmodel().createEntityDefinition("thing", "things");
        defn.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        defn.addField(Field.is("title", FieldType.STRING));

        return thingifier;
    }

    @Test
    public void aGetRequestWillCreateNewSessionWithDatabase() {

        Thingifier thingifier = getTestThingifier();

        final ThingifierHttpApi api = new ThingifierHttpApi(thingifier, null, null);

        final Map<String, String> headers = new HashMap<>();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "other_things");

        final HttpApiResponse response = api.get(new HttpApiRequest("/things").setHeaders(headers));

        // add a thing
        final EntityDefinition thing =
                thingifier.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("thing");
        final EntityInstance existingInstance =
                thingifier
                        .getStore("other_things")
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(thing)
                                        .withField("title", "My Title" + System.nanoTime()));

        final HttpApiResponse response2 =
                api.get(
                        new HttpApiRequest("/things/" + existingInstance.getPrimaryKeyValue())
                                .setHeaders(headers));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(200, response2.getStatusCode());
    }

    @Test
    public void aRequestWillCreateNewSessionWithDatabaseUsingDefaultData() {

        Thingifier thingifier = getTestThingifier();

        RepositoryDataPopulator dataPopulator =
                (schema, repository) -> {
                    String[] titles = {"thing1", "thing2", "thing3"};
                    EntityDefinition thing = schema.getEntityDefinitionNamed("thing");

                    for (String thingTitle : titles) {
                        repository
                                .entities()
                                .create(
                                        EntityInstanceDraft.forEntity(thing)
                                                .withField("title", thingTitle));
                    }
                };
        thingifier.setDataGenerator(dataPopulator);

        // populate default database
        dataPopulator.populate(
                thingifier.getERmodel().getSchema(),
                thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME));

        final ThingifierHttpApi api = new ThingifierHttpApi(thingifier, null, null);

        final Map<String, String> headers = new HashMap<>();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "other_things");

        final HttpApiResponse response = api.get(new HttpApiRequest("/things").setHeaders(headers));

        Assertions.assertEquals(200, response.getStatusCode());

        EntityDefinition thingInstances =
                thingifier.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("thing");

        Assertions.assertEquals(
                3, thingifier.getStore("other_things").entityQueries().count(thingInstances));
    }

    @Test
    public void aPostRequestWillCreateNewSessionWithDatabase() {

        Thingifier thingifier = getTestThingifier();

        final ThingifierHttpApi api = new ThingifierHttpApi(thingifier, null, null);

        final Map<String, String> headers = new HashMap<>();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "other_things");
        headers.put("content-type", "application/json");

        final HttpApiResponse response =
                api.post(
                        new HttpApiRequest("/things")
                                .setHeaders(headers)
                                .setBody("{\"title\":\"thing1\"}"));

        // check added a thing to db
        Assertions.assertEquals(201, response.getStatusCode());

        EntityDefinition thingInstances =
                thingifier.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("thing");

        Assertions.assertEquals(
                1, thingifier.getStore("other_things").entityQueries().count(thingInstances));

        Assertions.assertEquals(
                0,
                thingifier
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(
                                thingifier
                                        .getERmodel()
                                        .getSchema()
                                        .getDefinitionWithSingularOrPluralNamed("thing")));
    }

    @Test
    public void generatedHttpApiRequestsAreSerializedOnTheThingifierModel() throws Exception {
        // TODO: currently we synchronise at HttpAPI boundary to prevent GET happening before POST
        // work is complete - this should only really be necessary for inmemory persistence - need
        // to evaluate and push this lower when trying to scale because this might slow and impact
        // the disk and database based persistence backed APIs
        Thingifier thingifier = getTestThingifier();
        CountDownLatch firstRequestEnteredHook = new CountDownLatch(1);
        CountDownLatch releaseFirstRequest = new CountDownLatch(1);
        CountDownLatch secondRequestEnteredHook = new CountDownLatch(1);
        AtomicInteger hookCallCount = new AtomicInteger();
        AtomicBoolean hooksRanInsideThingifierLock = new AtomicBoolean(true);

        HttpApiRequestHook blockingHook =
                (request, config) -> {
                    if (!Thread.holdsLock(thingifier)) {
                        hooksRanInsideThingifierLock.set(false);
                    }

                    int callNumber = hookCallCount.incrementAndGet();
                    if (callNumber == 1) {
                        firstRequestEnteredHook.countDown();
                        waitForLatch(releaseFirstRequest, "Timed out waiting to release POST");
                    } else if (callNumber == 2) {
                        secondRequestEnteredHook.countDown();
                    }

                    return null;
                };
        ThingifierHttpApi api = new ThingifierHttpApi(thingifier, List.of(blockingHook), null);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<HttpApiResponse> postResponse =
                    executor.submit(
                            () -> api.post(jsonRequest("/things", "{\"title\":\"thing1\"}")));

            Assertions.assertTrue(
                    firstRequestEnteredHook.await(1, TimeUnit.SECONDS),
                    "POST did not enter the HTTP API hook");

            Future<HttpApiResponse> getResponse =
                    executor.submit(() -> api.get(new HttpApiRequest("/things")));

            Assertions.assertFalse(
                    secondRequestEnteredHook.await(200, TimeUnit.MILLISECONDS),
                    "GET entered generated API processing before POST completed");

            releaseFirstRequest.countDown();

            Assertions.assertEquals(201, postResponse.get(1, TimeUnit.SECONDS).getStatusCode());
            HttpApiResponse actualGetResponse = getResponse.get(1, TimeUnit.SECONDS);

            Assertions.assertTrue(
                    secondRequestEnteredHook.await(1, TimeUnit.SECONDS),
                    "GET did not enter generated API processing after POST completed");
            Assertions.assertEquals(200, actualGetResponse.getStatusCode());
            Assertions.assertTrue(actualGetResponse.getBody().contains("thing1"));
            Assertions.assertTrue(
                    hooksRanInsideThingifierLock.get(),
                    "Generated API hook ran outside the Thingifier model lock");
        } finally {
            releaseFirstRequest.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    public void strictPostRejectsNumericValueForStringField() {
        Thingifier thingifier = strictTypedThingifier();
        ThingifierHttpApi api = new ThingifierHttpApi(thingifier, null, null);

        HttpApiResponse response = api.post(jsonRequest("/things", "{\"title\":2}"));

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertTrue(
                response.getBody().contains("title should be STRING but was INTEGER"));
        Assertions.assertEquals(
                0,
                thingifier
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(thingifier.getDefinitionNamed("thing")));
    }

    @Test
    public void strictPostRejectsDecimalForIntegerAndAcceptsIntegerForFloat() {
        Thingifier thingifier = strictTypedThingifier();
        ThingifierHttpApi api = new ThingifierHttpApi(thingifier, null, null);

        HttpApiResponse decimalInteger = api.post(jsonRequest("/things", "{\"priority\":2.0}"));
        HttpApiResponse integerFloat = api.post(jsonRequest("/things", "{\"amount\":2}"));

        Assertions.assertEquals(422, decimalInteger.getStatusCode());
        Assertions.assertTrue(
                decimalInteger.getBody().contains("priority should be INTEGER but was NUMERIC"));
        Assertions.assertEquals(201, integerFloat.getStatusCode());
        Assertions.assertEquals(
                1,
                thingifier
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(thingifier.getDefinitionNamed("thing")));
    }

    @Test
    public void lenientPostStillAcceptsNumericValueForStringField() {
        Thingifier thingifier = strictTypedThingifier();
        thingifier.apiConfig().setApiToEnforceDeclaredTypesInInput(false);
        ThingifierHttpApi api = new ThingifierHttpApi(thingifier, null, null);

        HttpApiResponse response = api.post(jsonRequest("/things", "{\"title\":2}"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(
                "2",
                response.apiResponse().getReturnedInstance().getFieldValue("title").asString());
    }

    @Test
    public void strictPutRejectsNumericValueForStringField() {
        Thingifier thingifier = strictTypedThingifier();
        ThingifierHttpApi api = new ThingifierHttpApi(thingifier, null, null);
        EntityInstance existing = createThing(thingifier, "Original");

        HttpApiResponse response =
                api.put(jsonRequest("/things/" + existing.getPrimaryKeyValue(), "{\"title\":2}"));

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertTrue(
                response.getBody().contains("title should be STRING but was INTEGER"));
        Assertions.assertEquals(
                "Original",
                thingifier
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .findByQueryIdentifier(
                                thingifier.getDefinitionNamed("thing"),
                                existing.getPrimaryKeyValue())
                        .getFieldValue("title")
                        .asString());
    }

    @Test
    public void strictPatchRejectsNumericValueForStringField() {
        Thingifier thingifier = strictTypedThingifier();
        thingifier
                .apiConfig()
                .writeMethods()
                .entities()
                .patchCan(EntityPatchUpdateStyle.PARTIAL_JSON_UPDATE);
        ThingifierHttpApi api = new ThingifierHttpApi(thingifier, null, null);
        EntityInstance existing = createThing(thingifier, "Original");

        HttpApiResponse response =
                api.patch(jsonRequest("/things/" + existing.getPrimaryKeyValue(), "{\"title\":2}"));

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertTrue(
                response.getBody().contains("title should be STRING but was INTEGER"));
        Assertions.assertEquals(
                "Original",
                thingifier
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .findByQueryIdentifier(
                                thingifier.getDefinitionNamed("thing"),
                                existing.getPrimaryKeyValue())
                        .getFieldValue("title")
                        .asString());
    }

    @Test
    public void patchHonoursJsonContentTypePolicyForPartialJsonUpdate() {
        Thingifier thingifier = strictTypedThingifier();
        thingifier.apiConfig().setApiToAllowJsonForContentType(false);
        thingifier
                .apiConfig()
                .writeMethods()
                .entities()
                .patchCan(EntityPatchUpdateStyle.PARTIAL_JSON_UPDATE);
        ThingifierHttpApi api = new ThingifierHttpApi(thingifier, null, null);
        EntityInstance existing = createThing(thingifier, "Original");

        HttpApiResponse response =
                api.patch(
                        jsonRequest(
                                "/things/" + existing.getPrimaryKeyValue(),
                                "{\"title\":\"Patched\"}"));

        Assertions.assertEquals(
                thingifier.apiConfig().statusCodes().contentTypeNotSupported(),
                response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("JSON Not Supported"));
        Assertions.assertEquals("Original", currentThingTitle(thingifier, existing));
    }

    @Test
    public void patchContentTypePolicyAllowsConfiguredPatchMediaTypes() {
        Thingifier thingifier = strictTypedThingifier();
        thingifier.apiConfig().setApiToAllowJsonForContentType(false);
        thingifier
                .apiConfig()
                .writeMethods()
                .entities()
                .patchCan(
                        EntityPatchUpdateStyle.JSON_MERGE_PATCH_RFC7396,
                        EntityPatchUpdateStyle.JSON_PATCH_RFC6902);
        ThingifierHttpApi api = new ThingifierHttpApi(thingifier, null, null);
        EntityInstance existing = createThing(thingifier, "Original");
        String path = "/things/" + existing.getPrimaryKeyValue();

        HttpApiResponse mergePatchResponse =
                api.patch(
                        patchRequest(
                                path,
                                "{\"title\":\"Merged\"}",
                                EntityPatchUpdateStyle.JSON_MERGE_PATCH_RFC7396.mediaType()));

        Assertions.assertEquals(200, mergePatchResponse.getStatusCode());
        Assertions.assertEquals("Merged", currentThingTitle(thingifier, existing));

        HttpApiResponse jsonPatchResponse =
                api.patch(
                        patchRequest(
                                path,
                                "[{\"op\":\"replace\",\"path\":\"/title\",\"value\":\"Patched\"}]",
                                EntityPatchUpdateStyle.JSON_PATCH_RFC6902.mediaType()));

        Assertions.assertEquals(200, jsonPatchResponse.getStatusCode());
        Assertions.assertEquals("Patched", currentThingTitle(thingifier, existing));
    }

    @Test
    public void aDeleteRequestWillCreateNewSessionWithDatabase() {

        Thingifier thingifier = getTestThingifier();

        final ThingifierHttpApi api = new ThingifierHttpApi(thingifier, null, null);

        final Map<String, String> headers = new HashMap<>();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "other_things");

        final HttpApiResponse response =
                api.delete(new HttpApiRequest("/things/1").setHeaders(headers));

        // check added a thing to db
        Assertions.assertEquals(404, response.getStatusCode());

        EntityDefinition thingInstances =
                thingifier.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("thing");

        EntityInstance anInstance =
                thingifier
                        .getStore("other_things")
                        .entities()
                        .create(EntityInstanceDraft.forEntity(thingInstances));

        Assertions.assertEquals(
                1, thingifier.getStore("other_things").entityQueries().count(thingInstances));

        final HttpApiResponse actualDeleteResponse =
                api.delete(
                        new HttpApiRequest("/things/" + anInstance.getPrimaryKeyValue())
                                .setHeaders(headers));
        Assertions.assertEquals(204, actualDeleteResponse.getStatusCode());

        Assertions.assertEquals(
                0, thingifier.getStore("other_things").entityQueries().count(thingInstances));

        Assertions.assertEquals(
                0,
                thingifier
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(
                                thingifier
                                        .getERmodel()
                                        .getSchema()
                                        .getDefinitionWithSingularOrPluralNamed("thing")));
    }

    // OPTIONS
    // etc.

    private Thingifier strictTypedThingifier() {
        Thingifier thingifier = new Thingifier();
        thingifier.apiConfig().setApiToEnforceAcceptHeaderForResponses(false);
        EntityDefinition defn = thingifier.getERmodel().createEntityDefinition("thing", "things");
        defn.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        defn.addField(Field.is("title", FieldType.STRING));
        defn.addField(Field.is("priority", FieldType.INTEGER));
        defn.addField(Field.is("amount", FieldType.FLOAT));
        return thingifier;
    }

    private EntityInstance createThing(final Thingifier thingifier, final String title) {
        EntityDefinition thing = thingifier.getDefinitionNamed("thing");
        return thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(EntityInstanceDraft.forEntity(thing).withField("title", title));
    }

    private String currentThingTitle(final Thingifier thingifier, final EntityInstance instance) {
        return thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entityQueries()
                .findByQueryIdentifier(
                        thingifier.getDefinitionNamed("thing"), instance.getPrimaryKeyValue())
                .getFieldValue("title")
                .asString();
    }

    private HttpApiRequest jsonRequest(final String path, final String body) {
        return new HttpApiRequest(path)
                .setHeaders(Map.of("content-type", "application/json"))
                .setBody(body);
    }

    private HttpApiRequest patchRequest(
            final String path, final String body, final String contentType) {
        return new HttpApiRequest(path)
                .setHeaders(Map.of("content-type", contentType))
                .setBody(body);
    }

    private void waitForLatch(final CountDownLatch latch, final String timeoutMessage) {
        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                throw new AssertionError(timeoutMessage);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(timeoutMessage, e);
        }
    }
}
