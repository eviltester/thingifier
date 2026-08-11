package uk.co.compendiumdev.thingifier.api.restapihandlers;

import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.JSON_MERGE_PATCH_RFC7396;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.JSON_PATCH_RFC6902;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.PARTIAL_JSON_UPDATE;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation.CREATE;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation.UPDATE;
import static uk.co.compendiumdev.thingifier.apiconfig.PutIdentifierPolicy.DISALLOWED;
import static uk.co.compendiumdev.thingifier.apiconfig.PutIdentifierPolicy.MANDATORY;
import static uk.co.compendiumdev.thingifier.apiconfig.PutIdentifierPolicy.OPTIONAL;
import static uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation.CONNECT_EXISTING;
import static uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation.CREATE_AND_CONNECT;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public class WriteMethodPolicyTest {

    @Test
    public void defaultsKeepPostAndPutCreateAndUpdateWithPatchUnsupported() {
        Thingifier thingifier = stringIdNotes();

        Assertions.assertEquals(
                201, post(thingifier, "notes", noteJson("one", "One")).getStatusCode());
        Assertions.assertEquals(
                200, post(thingifier, "notes/one", "{\"title\":\"Changed\"}").getStatusCode());
        Assertions.assertEquals(
                201, put(thingifier, "notes/two", "{\"title\":\"Two\"}").getStatusCode());
        Assertions.assertEquals(
                405, patch(thingifier, "notes/one", "{\"title\":\"Patch\"}").getStatusCode());
    }

    @Test
    public void postCanBeLimitedToCreateOnlyOrUpdateOnlyOrUnsupported() {
        Thingifier createOnly = stringIdNotes();
        createOnly.apiConfig().writeMethods().entities().postCan(CREATE);
        post(createOnly, "notes", noteJson("one", "One"));
        Assertions.assertEquals(
                405, post(createOnly, "notes/one", "{\"title\":\"Blocked\"}").getStatusCode());

        Thingifier updateOnly = stringIdNotes();
        updateOnly.apiConfig().writeMethods().entities().postCan(UPDATE);
        EntityInstance note = createNote(updateOnly, "one", "One");
        Assertions.assertEquals(
                405, post(updateOnly, "notes", noteJson("two", "Two")).getStatusCode());
        Assertions.assertEquals(
                200,
                post(updateOnly, "notes/" + note.getPrimaryKeyValue(), "{\"title\":\"Changed\"}")
                        .getStatusCode());

        Thingifier unsupported = stringIdNotes();
        unsupported.apiConfig().writeMethods().entities().postCan();
        Assertions.assertEquals(
                405, post(unsupported, "notes", noteJson("one", "One")).getStatusCode());
    }

    @Test
    public void putUsesExistingTargetStateToResolveCreateOrUpdate() {
        Thingifier updateOnly = stringIdNotes();
        updateOnly.apiConfig().writeMethods().entities().putCan(UPDATE);
        createNote(updateOnly, "one", "One");

        Assertions.assertEquals(
                200, put(updateOnly, "notes/one", "{\"title\":\"Changed\"}").getStatusCode());
        Assertions.assertEquals(
                405, put(updateOnly, "notes/two", "{\"title\":\"Two\"}").getStatusCode());

        Thingifier createOnly = stringIdNotes();
        createNote(createOnly, "one", "One");
        createOnly.apiConfig().writeMethods().entities().putCan(CREATE);

        Assertions.assertEquals(
                405, put(createOnly, "notes/one", "{\"title\":\"Changed\"}").getStatusCode());
        Assertions.assertEquals(
                201, put(createOnly, "notes/two", "{\"title\":\"Two\"}").getStatusCode());
    }

    @Test
    public void defaultPutIdentifierPolicyRequiresUriIdentifier() {
        Thingifier thingifier = stringIdNotes();

        ApiResponse response = put(thingifier, "notes", noteJson("one", "One"));

        Assertions.assertEquals(405, response.getStatusCode());
        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, POST, QUERY", response.getHeaderValue("Allow"));
        Assertions.assertEquals(0, noteCount(thingifier));
    }

    @Test
    public void putCanUsePayloadIdentifierOnCollectionRoutes() {
        Thingifier thingifier = stringIdNotes();
        thingifier.apiConfig().writeMethods().entities().putIdentifierInUri(OPTIONAL);
        createNote(thingifier, "one", "One");

        ApiResponse updated = put(thingifier, "notes", noteJson("one", "Changed"));
        ApiResponse created = put(thingifier, "notes", noteJson("two", "Two"));

        Assertions.assertEquals(200, updated.getStatusCode());
        Assertions.assertEquals("Changed", currentTitle(thingifier, "one"));
        Assertions.assertEquals(201, created.getStatusCode());
        Assertions.assertEquals("Two", currentTitle(thingifier, "two"));
    }

    @Test
    public void putRejectsUriIdentifierWhenDisallowed() {
        Thingifier thingifier = stringIdNotes();
        thingifier.apiConfig().writeMethods().entities().putIdentifierInUri(DISALLOWED);

        ApiResponse response = put(thingifier, "notes/one", noteJson("one", "One"));

        Assertions.assertEquals(405, response.getStatusCode());
        Assertions.assertFalse(response.getHeaderValue("Allow").contains("PUT"));
        Assertions.assertEquals(0, noteCount(thingifier));
    }

    @Test
    public void putRejectsMissingMandatoryPayloadIdentifierEvenWithUriIdentifier() {
        Thingifier thingifier = stringIdNotes();
        thingifier.apiConfig().writeMethods().entities().putIdentifierInPayload(MANDATORY);
        createNote(thingifier, "one", "One");

        ApiResponse response = put(thingifier, "notes/one", "{\"title\":\"Blocked\"}");

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertTrue(
                response.getErrorMessages()
                        .contains("PUT payload must include identifier field id"));
        Assertions.assertEquals("One", currentTitle(thingifier, "one"));
    }

    @Test
    public void putRejectsDisallowedPayloadIdentifierButStillAllowsUriIdentifier() {
        Thingifier thingifier = stringIdNotes();
        thingifier.apiConfig().writeMethods().entities().putIdentifierInPayload(DISALLOWED);
        createNote(thingifier, "one", "One");

        ApiResponse rejected = put(thingifier, "notes/one", noteJson("one", "Blocked"));
        ApiResponse accepted = put(thingifier, "notes/one", "{\"title\":\"Changed\"}");

        Assertions.assertEquals(422, rejected.getStatusCode());
        Assertions.assertTrue(
                rejected.getErrorMessages()
                        .contains("PUT payload must not include identifier field id"));
        Assertions.assertEquals(200, accepted.getStatusCode());
        Assertions.assertEquals("Changed", currentTitle(thingifier, "one"));
    }

    @Test
    public void putKeepsExistingMismatchResponseWhenUriAndPayloadIdentifiersDiffer() {
        Thingifier thingifier = stringIdNotes();

        ApiResponse response = put(thingifier, "notes/route-key", noteJson("body-key", "Blocked"));

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertTrue(
                response.getErrorMessages()
                        .contains(
                                "Cannot create note with PUT as key does not match body value "
                                        + "route-key != body-key"));
        Assertions.assertEquals(0, noteCount(thingifier));
    }

    @Test
    public void collectionPutRespectsCreateAndUpdateOperationPolicy() {
        Thingifier updateOnly = stringIdNotes();
        updateOnly.apiConfig().writeMethods().entities().putIdentifierInUri(OPTIONAL);
        updateOnly.apiConfig().writeMethods().entities().putCan(UPDATE);
        createNote(updateOnly, "one", "One");

        Assertions.assertEquals(
                200, put(updateOnly, "notes", noteJson("one", "Changed")).getStatusCode());
        Assertions.assertEquals(
                405, put(updateOnly, "notes", noteJson("two", "Two")).getStatusCode());

        Thingifier createOnly = stringIdNotes();
        createOnly.apiConfig().writeMethods().entities().putIdentifierInUri(OPTIONAL);
        createOnly.apiConfig().writeMethods().entities().putCan(CREATE);
        createNote(createOnly, "one", "One");

        Assertions.assertEquals(
                405, put(createOnly, "notes", noteJson("one", "Changed")).getStatusCode());
        Assertions.assertEquals(
                201, put(createOnly, "notes", noteJson("two", "Two")).getStatusCode());
    }

    @Test
    public void patchCanBeEnabledForEntityInstanceUpdates() {
        Thingifier thingifier = stringIdNotes();
        thingifier.apiConfig().writeMethods().entities().patchCan(PARTIAL_JSON_UPDATE);
        createNote(thingifier, "one", "One");

        ApiResponse response = patch(thingifier, "notes/one", "{\"title\":\"Patched\"}");

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "Patched", response.getReturnedInstance().getFieldValue("title").asString());
    }

    @Test
    public void patchIsRejectedForEntityCollectionsEvenWhenInstancePatchIsAllowed() {
        Thingifier thingifier = stringIdNotes();
        thingifier.apiConfig().writeMethods().entities().patchCan(PARTIAL_JSON_UPDATE);

        ApiResponse response = patch(thingifier, "notes", "{\"title\":\"No Collection Patch\"}");

        Assertions.assertEquals(405, response.getStatusCode());
        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, POST, QUERY", response.getHeaderValue("Allow"));
        Assertions.assertTrue(response.getErrorMessages().contains("Method Not Allowed"));
        Assertions.assertEquals(0, noteCount(thingifier));
    }

    @Test
    public void patchRequiresConfiguredContentTypeWhenMethodIsAllowed() {
        Thingifier thingifier = stringIdNotes();
        thingifier.apiConfig().writeMethods().entities().patchCan(PARTIAL_JSON_UPDATE);
        createNote(thingifier, "one", "One");

        ApiResponse missingContentType =
                thingifier
                        .api()
                        .patch("notes/one", "{\"title\":\"Blocked\"}", new HttpHeadersBlock());

        HttpHeadersBlock mergePatchHeaders = new HttpHeadersBlock();
        mergePatchHeaders.put("Content-Type", JSON_MERGE_PATCH_RFC7396.mediaType());
        ApiResponse unsupportedContentType =
                thingifier.api().patch("notes/one", "{\"title\":\"Blocked\"}", mergePatchHeaders);

        Assertions.assertEquals(415, missingContentType.getStatusCode());
        Assertions.assertEquals(
                PARTIAL_JSON_UPDATE.mediaType(), missingContentType.getHeaderValue("Accept-Patch"));
        Assertions.assertEquals(415, unsupportedContentType.getStatusCode());
        Assertions.assertEquals(
                PARTIAL_JSON_UPDATE.mediaType(),
                unsupportedContentType.getHeaderValue("Accept-Patch"));
        Assertions.assertEquals(
                "One", currentTitle(thingifier, "one"), "Unsupported PATCH must not amend data");
        Assertions.assertTrue(
                unsupportedContentType
                        .getErrorMessages()
                        .contains("Unsupported PATCH Content Type"));
    }

    @Test
    public void patchContentTypeCanIncludeParameters() {
        Thingifier thingifier = stringIdNotes();
        thingifier.apiConfig().writeMethods().entities().patchCan(PARTIAL_JSON_UPDATE);
        createNote(thingifier, "one", "One");

        HttpHeadersBlock headers = new HttpHeadersBlock();
        headers.put("Content-Type", "application/json; charset=utf-8");
        ApiResponse response =
                thingifier.api().patch("notes/one", "{\"title\":\"Patched\"}", headers);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "Patched", response.getReturnedInstance().getFieldValue("title").asString());
    }

    @Test
    public void bodyParserPatchEntryPointPreservesJsonPatchArrayBodies() {
        Thingifier thingifier = stringIdNotes();
        thingifier.apiConfig().writeMethods().entities().patchCan(JSON_PATCH_RFC6902);
        createNote(thingifier, "one", "One", "Original");

        HttpHeadersBlock headers = new HttpHeadersBlock();
        headers.put("Content-Type", JSON_PATCH_RFC6902.mediaType());
        ApiResponse response =
                thingifier
                        .api()
                        .patch(
                                "notes/one",
                                parser(
                                        thingifier,
                                        "[{\"op\":\"replace\",\"path\":\"/description\",\"value\":\"From BodyParser\"}]"),
                                headers);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "From BodyParser",
                response.getReturnedInstance().getFieldValue("description").asString());
        Assertions.assertEquals("One", currentTitle(thingifier, "one"));
    }

    @Test
    public void jsonMergePatchCanUpdateAndRemoveFields() {
        Thingifier thingifier = stringIdNotes();
        thingifier.apiConfig().writeMethods().entities().patchCan(JSON_MERGE_PATCH_RFC7396);
        createNote(thingifier, "one", "One", "remove me");

        ApiResponse response =
                patch(
                        thingifier,
                        "notes/one",
                        "{\"title\":\"Merged\",\"description\":null}",
                        JSON_MERGE_PATCH_RFC7396.mediaType());

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "Merged", response.getReturnedInstance().getFieldValue("title").asString());
        Assertions.assertEquals(
                "", response.getReturnedInstance().getFieldValue("description").asString());
    }

    @Test
    public void jsonMergePatchRejectsRootReplacementForEntityResources() {
        Thingifier thingifier = stringIdNotes();
        thingifier.apiConfig().writeMethods().entities().patchCan(JSON_MERGE_PATCH_RFC7396);
        createNote(thingifier, "one", "One");

        ApiResponse response =
                patch(
                        thingifier,
                        "notes/one",
                        "\"not an entity object\"",
                        JSON_MERGE_PATCH_RFC7396.mediaType());

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertEquals(
                "One",
                thingifier
                        .api()
                        .get("notes/one", new QueryFilterParams(), new HttpHeadersBlock())
                        .getReturnedInstance()
                        .getFieldValue("title")
                        .asString());
    }

    @Test
    public void malformedPatchDocumentsAreRejectedWithoutChangingTheEntity() {
        Thingifier thingifier = stringIdNotes();
        thingifier
                .apiConfig()
                .writeMethods()
                .entities()
                .patchCan(PARTIAL_JSON_UPDATE, JSON_MERGE_PATCH_RFC7396, JSON_PATCH_RFC6902);
        createNote(thingifier, "one", "One", "Original");

        ApiResponse partialArray =
                patch(thingifier, "notes/one", "[{\"title\":\"Not an entity object\"}]");
        ApiResponse malformedPartial = patch(thingifier, "notes/one", "{\"title\":");
        ApiResponse malformedMerge =
                patch(thingifier, "notes/one", "{\"title\":", JSON_MERGE_PATCH_RFC7396.mediaType());
        ApiResponse malformedJsonPatch =
                patch(thingifier, "notes/one", "[{\"op\":", JSON_PATCH_RFC6902.mediaType());
        ApiResponse jsonPatchObject =
                patch(
                        thingifier,
                        "notes/one",
                        "{\"op\":\"replace\",\"path\":\"/title\",\"value\":\"Wrong shape\"}",
                        JSON_PATCH_RFC6902.mediaType());

        Assertions.assertEquals(400, partialArray.getStatusCode());
        Assertions.assertTrue(
                partialArray
                        .getErrorMessages()
                        .contains("PATCH partial JSON update document must be an object"));
        Assertions.assertEquals(400, malformedPartial.getStatusCode());
        Assertions.assertTrue(
                malformedPartial.getErrorMessages().contains("Malformed JSON document"));
        Assertions.assertEquals(400, malformedMerge.getStatusCode());
        Assertions.assertTrue(
                malformedMerge.getErrorMessages().contains("Malformed JSON Merge Patch document"));
        Assertions.assertEquals(400, malformedJsonPatch.getStatusCode());
        Assertions.assertTrue(
                malformedJsonPatch.getErrorMessages().contains("Malformed JSON Patch document"));
        Assertions.assertEquals(400, jsonPatchObject.getStatusCode());
        Assertions.assertTrue(
                jsonPatchObject
                        .getErrorMessages()
                        .contains("JSON Patch document must be an array of operations"));
        Assertions.assertEquals("One", currentTitle(thingifier, "one"));
        Assertions.assertEquals("Original", currentDescription(thingifier, "one"));
    }

    @Test
    public void emptyPartialJsonPatchIsANoOpForTheTargetEntity() {
        Thingifier thingifier = stringIdNotes();
        thingifier.apiConfig().writeMethods().entities().patchCan(PARTIAL_JSON_UPDATE);
        createNote(thingifier, "one", "One", "Original");

        ApiResponse response = patch(thingifier, "notes/one", "");

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "One", response.getReturnedInstance().getFieldValue("title").asString());
        Assertions.assertEquals(
                "Original", response.getReturnedInstance().getFieldValue("description").asString());
        Assertions.assertEquals("One", currentTitle(thingifier, "one"));
        Assertions.assertEquals("Original", currentDescription(thingifier, "one"));
    }

    @Test
    public void patchRequiresPrimaryKeyRoutableEntities() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition log = thingifier.defineThing("log", "logs");
        log.addField(Field.is("title", FieldType.STRING).makeMandatory());
        thingifier.apiConfig().writeMethods().entities().patchCan(PARTIAL_JSON_UPDATE);

        ApiResponse response = patch(thingifier, "logs/anything", "{\"title\":\"No identity\"}");

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertTrue(
                response.getErrorMessages()
                        .contains("Entity log does not have a primary key defined"));
        Assertions.assertEquals(
                0,
                thingifier
                        .api()
                        .get("logs", new QueryFilterParams(), new HttpHeadersBlock())
                        .getReturnedInstanceCollection()
                        .size());
    }

    @Test
    public void rfcPatchStylesReturnNotFoundForMissingEntityAndDoNotCreateIt() {
        Thingifier thingifier = stringIdNotes();
        thingifier
                .apiConfig()
                .writeMethods()
                .entities()
                .patchCan(JSON_MERGE_PATCH_RFC7396, JSON_PATCH_RFC6902);

        ApiResponse mergePatch =
                patch(
                        thingifier,
                        "notes/missing",
                        "{\"title\":\"Should not exist\"}",
                        JSON_MERGE_PATCH_RFC7396.mediaType());
        ApiResponse jsonPatch =
                patch(
                        thingifier,
                        "notes/missing",
                        "[{\"op\":\"replace\",\"path\":\"/title\",\"value\":\"Should not exist\"}]",
                        JSON_PATCH_RFC6902.mediaType());

        Assertions.assertEquals(404, mergePatch.getStatusCode());
        Assertions.assertTrue(
                mergePatch
                        .getErrorMessages()
                        .contains("No such note entity instance with id == missing found"));
        Assertions.assertEquals(404, jsonPatch.getStatusCode());
        Assertions.assertTrue(
                jsonPatch
                        .getErrorMessages()
                        .contains("No such note entity instance with id == missing found"));
        Assertions.assertEquals(
                404,
                thingifier
                        .api()
                        .get("notes/missing", new QueryFilterParams(), new HttpHeadersBlock())
                        .getStatusCode());
    }

    @Test
    public void jsonPatchWholeResourceResultMustStillBeAnEntityObject() {
        Thingifier thingifier = stringIdNotes();
        thingifier.apiConfig().writeMethods().entities().patchCan(JSON_PATCH_RFC6902);
        createNote(thingifier, "one", "One", "Original");

        ApiResponse response =
                patch(
                        thingifier,
                        "notes/one",
                        "[{\"op\":\"replace\",\"path\":\"\",\"value\":\"not an entity object\"}]",
                        JSON_PATCH_RFC6902.mediaType());

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertTrue(
                response.getErrorMessages()
                        .contains("PATCH result for entity resources must be an object"));
        Assertions.assertEquals("One", currentTitle(thingifier, "one"));
        Assertions.assertEquals("Original", currentDescription(thingifier, "one"));
    }

    @Test
    public void jsonPatchCanApplyOperationsAndFailedPatchIsAtomic() {
        Thingifier thingifier = stringIdNotes();
        thingifier.apiConfig().writeMethods().entities().patchCan(JSON_PATCH_RFC6902);
        createNote(thingifier, "one", "One", "Original");

        ApiResponse failed =
                patch(
                        thingifier,
                        "notes/one",
                        "[{\"op\":\"replace\",\"path\":\"/title\",\"value\":\"Changed\"},"
                                + "{\"op\":\"test\",\"path\":\"/description\",\"value\":\"Wrong\"}]",
                        JSON_PATCH_RFC6902.mediaType());

        Assertions.assertEquals(409, failed.getStatusCode());
        Assertions.assertEquals(
                "One",
                thingifier
                        .api()
                        .get("notes/one", new QueryFilterParams(), new HttpHeadersBlock())
                        .getReturnedInstance()
                        .getFieldValue("title")
                        .asString());

        ApiResponse applied =
                patch(
                        thingifier,
                        "notes/one",
                        "[{\"op\":\"replace\",\"path\":\"/title\",\"value\":\"Changed\"},"
                                + "{\"op\":\"remove\",\"path\":\"/description\"}]",
                        JSON_PATCH_RFC6902.mediaType());

        Assertions.assertEquals(200, applied.getStatusCode());
        Assertions.assertEquals(
                "Changed", applied.getReturnedInstance().getFieldValue("title").asString());
        Assertions.assertEquals(
                "", applied.getReturnedInstance().getFieldValue("description").asString());
    }

    @Test
    public void routeOverrideWinsOverEntityOverrideWhichWinsOverGlobalConfig() {
        Thingifier thingifier = stringIdNotes();
        thingifier.apiConfig().writeMethods().entities().postCan(CREATE);
        thingifier.apiSpec().entityPostCan("/notes", UPDATE);
        thingifier.apiSpec().route(RoutingVerb.POST, "/notes/{id}").entityCan();
        createNote(thingifier, "one", "One");

        Assertions.assertEquals(
                405, post(thingifier, "notes/one", "{\"title\":\"Blocked\"}").getStatusCode());

        Thingifier entityOverride = stringIdNotes();
        entityOverride.apiConfig().writeMethods().entities().postCan(CREATE);
        entityOverride.apiSpec().entityPostCan("/notes", UPDATE);
        createNote(entityOverride, "one", "One");

        Assertions.assertEquals(
                200, post(entityOverride, "notes/one", "{\"title\":\"Allowed\"}").getStatusCode());
    }

    @Test
    public void relationshipPostCanBeLimitedByOperation() {
        Thingifier createOnly = relationshipModel();
        createOnly.apiConfig().writeMethods().relationships().postCan(CREATE_AND_CONNECT);
        EntityInstance project = createProject(createOnly, "Project");
        EntityInstance task = createTask(createOnly, "Existing");

        Assertions.assertEquals(
                201,
                post(
                                createOnly,
                                "projects/" + project.getPrimaryKeyValue() + "/tasks",
                                "{\"title\":\"New\"}")
                        .getStatusCode());
        ApiResponse blockedConnectExisting =
                post(
                        createOnly,
                        "projects/" + project.getPrimaryKeyValue() + "/tasks",
                        "{\"id\":" + task.getPrimaryKeyValue() + "}");
        Assertions.assertEquals(405, blockedConnectExisting.getStatusCode());
        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, POST, QUERY", blockedConnectExisting.getHeaderValue("Allow"));

        Thingifier connectOnly = relationshipModel();
        connectOnly.apiConfig().writeMethods().relationships().postCan(CONNECT_EXISTING);
        EntityInstance otherProject = createProject(connectOnly, "Project");
        EntityInstance otherTask = createTask(connectOnly, "Existing");

        ApiResponse blockedCreateAndConnect =
                post(
                        connectOnly,
                        "projects/" + otherProject.getPrimaryKeyValue() + "/tasks",
                        "{\"title\":\"New\"}");
        Assertions.assertEquals(405, blockedCreateAndConnect.getStatusCode());
        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, POST, QUERY", blockedCreateAndConnect.getHeaderValue("Allow"));
        Assertions.assertEquals(
                201,
                post(
                                connectOnly,
                                "projects/" + otherProject.getPrimaryKeyValue() + "/tasks",
                                "{\"id\":" + otherTask.getPrimaryKeyValue() + "}")
                        .getStatusCode());
    }

    @Test
    public void relationshipDeleteCanDisableDisconnect() {
        Thingifier thingifier = relationshipModel();
        thingifier.apiConfig().writeMethods().relationships().deleteCan();
        EntityInstance project = createProject(thingifier, "Project");
        EntityInstance task = createTask(thingifier, "Task");
        post(
                thingifier,
                "projects/" + project.getPrimaryKeyValue() + "/tasks",
                "{\"id\":" + task.getPrimaryKeyValue() + "}");

        ApiResponse response =
                thingifier
                        .api()
                        .delete(
                                "projects/"
                                        + project.getPrimaryKeyValue()
                                        + "/tasks/"
                                        + task.getPrimaryKeyValue(),
                                new HttpHeadersBlock());

        Assertions.assertEquals(405, response.getStatusCode());
    }

    @Test
    public void generatedDocsReflectConfiguredEntityPolicy() {
        Thingifier thingifier = autoIdNotes();
        thingifier.apiConfig().writeMethods().entities().postCan(CREATE);
        thingifier.apiConfig().writeMethods().entities().patchCan(PARTIAL_JSON_UPDATE);
        thingifier.apiConfig().writeMethods().entities().putCan(UPDATE);

        ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("");

        Assertions.assertTrue(
                route(definition, RoutingVerb.POST, "notes").status().isReturnedFromCall());
        Assertions.assertEquals(
                405, route(definition, RoutingVerb.POST, "notes/:id").status().value());
        Assertions.assertTrue(
                route(definition, RoutingVerb.PATCH, "notes/:id").status().isReturnedFromCall());
        Assertions.assertTrue(
                route(definition, RoutingVerb.PUT, "notes/:id").status().isReturnedFromCall());
        Assertions.assertEquals(
                Set.of(200, 404, 422, 409),
                statusCodes(route(definition, RoutingVerb.PUT, "notes/:id")));
        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, POST, QUERY",
                route(definition, RoutingVerb.OPTIONS, "notes").headerValue());
        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, PUT, PATCH, DELETE",
                route(definition, RoutingVerb.OPTIONS, "notes/:id").headerValue());
        Assertions.assertEquals(
                PARTIAL_JSON_UPDATE.mediaType(),
                route(definition, RoutingVerb.OPTIONS, "notes/:id")
                        .getResponseHeaderValue("Accept-Patch"));
    }

    @Test
    public void routePatchStyleOverrideWinsOverEntityOverrideWhichWinsOverGlobalConfig() {
        Thingifier routeOverride = stringIdNotes();
        routeOverride.apiConfig().writeMethods().entities().patchCan(PARTIAL_JSON_UPDATE);
        routeOverride.apiSpec().entityPatchCan("/notes", JSON_MERGE_PATCH_RFC7396);
        routeOverride
                .apiSpec()
                .route(RoutingVerb.PATCH, "/notes/{id}")
                .entityPatchCan(JSON_PATCH_RFC6902);
        createNote(routeOverride, "one", "One");

        Assertions.assertEquals(
                415, patch(routeOverride, "notes/one", "{\"title\":\"Blocked\"}").getStatusCode());
        Assertions.assertEquals(
                200,
                patch(
                                routeOverride,
                                "notes/one",
                                "[{\"op\":\"replace\",\"path\":\"/title\",\"value\":\"Allowed\"}]",
                                JSON_PATCH_RFC6902.mediaType())
                        .getStatusCode());

        Thingifier entityOverride = stringIdNotes();
        entityOverride.apiConfig().writeMethods().entities().patchCan(PARTIAL_JSON_UPDATE);
        entityOverride.apiSpec().entityPatchCan("/notes", JSON_MERGE_PATCH_RFC7396);
        createNote(entityOverride, "one", "One");

        Assertions.assertEquals(
                415, patch(entityOverride, "notes/one", "{\"title\":\"Blocked\"}").getStatusCode());
        Assertions.assertEquals(
                200,
                patch(
                                entityOverride,
                                "notes/one",
                                "{\"title\":\"Allowed\"}",
                                JSON_MERGE_PATCH_RFC7396.mediaType())
                        .getStatusCode());
    }

    @Test
    public void generatedDocsReflectPutCreateCapabilities() {
        Thingifier createOnly = autoIdNotes();
        createOnly.apiConfig().writeMethods().entities().putCan(CREATE);

        Thingifier createAndUpdate = autoIdNotes();
        createAndUpdate.apiConfig().writeMethods().entities().putCan(CREATE, UPDATE);

        Thingifier unsupported = autoIdNotes();
        unsupported.apiConfig().writeMethods().entities().putCan();

        Assertions.assertEquals(
                Set.of(201, 422, 409),
                statusCodes(
                        route(
                                new ApiRoutingDefinitionDocGenerator(createOnly).generate(""),
                                RoutingVerb.PUT,
                                "notes/:id")));
        Assertions.assertEquals(
                Set.of(201, 200, 404, 422, 409),
                statusCodes(
                        route(
                                new ApiRoutingDefinitionDocGenerator(createAndUpdate).generate(""),
                                RoutingVerb.PUT,
                                "notes/:id")));
        Assertions.assertEquals(
                405,
                route(
                                new ApiRoutingDefinitionDocGenerator(unsupported).generate(""),
                                RoutingVerb.PUT,
                                "notes/:id")
                        .status()
                        .value());
    }

    @Test
    public void generatedDocsReflectPutIdentifierLocationPolicy() {
        Thingifier collectionPut = autoIdNotes();
        collectionPut.apiConfig().writeMethods().entities().putIdentifierInUri(OPTIONAL);

        ApiRoutingDefinition collectionPutDefinition =
                new ApiRoutingDefinitionDocGenerator(collectionPut).generate("");

        Assertions.assertTrue(
                route(collectionPutDefinition, RoutingVerb.PUT, "notes")
                        .status()
                        .isReturnedFromCall());
        Assertions.assertEquals(
                Set.of(201, 200, 404, 422, 409),
                statusCodes(route(collectionPutDefinition, RoutingVerb.PUT, "notes")));
        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, POST, QUERY, PUT",
                route(collectionPutDefinition, RoutingVerb.OPTIONS, "notes").headerValue());

        Thingifier uriDisallowed = autoIdNotes();
        uriDisallowed.apiConfig().writeMethods().entities().putIdentifierInUri(DISALLOWED);

        ApiRoutingDefinition uriDisallowedDefinition =
                new ApiRoutingDefinitionDocGenerator(uriDisallowed).generate("");

        Assertions.assertTrue(
                route(uriDisallowedDefinition, RoutingVerb.PUT, "notes")
                        .status()
                        .isReturnedFromCall());
        Assertions.assertEquals(
                405, route(uriDisallowedDefinition, RoutingVerb.PUT, "notes/:id").status().value());
        Assertions.assertFalse(
                route(uriDisallowedDefinition, RoutingVerb.OPTIONS, "notes/:id")
                        .headerValue()
                        .contains("PUT"));
    }

    @Test
    public void httpApiAndDirectApiSharePolicyResponses() {
        Thingifier thingifier = stringIdNotes();
        thingifier.apiConfig().writeMethods().entities().postCan(CREATE);
        createNote(thingifier, "one", "One");

        ApiResponse direct = post(thingifier, "notes/one", "{\"title\":\"Blocked\"}");
        ThingifierHttpApi httpApi = new ThingifierHttpApi(thingifier);
        int httpStatus =
                httpApi.post(jsonRequest("notes/one", "POST", "{\"title\":\"Blocked\"}"))
                        .getStatusCode();

        Assertions.assertEquals(405, direct.getStatusCode());
        Assertions.assertEquals(405, httpStatus);
    }

    private Thingifier stringIdNotes() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition note = thingifier.defineThing("note", "notes");
        note.addAsPrimaryKeyField(Field.is("id", FieldType.STRING));
        note.addField(Field.is("title", FieldType.STRING).makeMandatory());
        note.addField(Field.is("description", FieldType.STRING));
        return thingifier;
    }

    private Thingifier relationshipModel() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition project = thingifier.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        project.addField(Field.is("title", FieldType.STRING));

        EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        task.addField(Field.is("title", FieldType.STRING).makeMandatory());

        thingifier.defineRelationship(project, task, "tasks", Cardinality.ONE_TO_MANY());
        return thingifier;
    }

    private Thingifier autoIdNotes() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition note = thingifier.defineThing("note", "notes");
        note.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        note.addField(Field.is("title", FieldType.STRING).makeMandatory());
        return thingifier;
    }

    private EntityInstance createNote(
            final Thingifier thingifier, final String id, final String title) {
        return createNote(thingifier, id, title, null);
    }

    private EntityInstance createNote(
            final Thingifier thingifier,
            final String id,
            final String title,
            final String description) {
        EntityDefinition note = thingifier.getDefinitionNamed("note");
        EntityInstanceDraft draft =
                EntityInstanceDraft.forEntity(note).withField("id", id).withField("title", title);
        if (description != null) {
            draft.withField("description", description);
        }
        return thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME).entities().create(draft);
    }

    private EntityInstance createProject(final Thingifier thingifier, final String title) {
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        return thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(EntityInstanceDraft.forEntity(project).withField("title", title));
    }

    private EntityInstance createTask(final Thingifier thingifier, final String title) {
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        return thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(EntityInstanceDraft.forEntity(task).withField("title", title));
    }

    private ApiResponse post(final Thingifier thingifier, final String path, final String body) {
        return thingifier.api().post(path, parser(thingifier, body), new HttpHeadersBlock());
    }

    private ApiResponse put(final Thingifier thingifier, final String path, final String body) {
        return thingifier.api().put(path, parser(thingifier, body), new HttpHeadersBlock());
    }

    private ApiResponse patch(final Thingifier thingifier, final String path, final String body) {
        return patch(thingifier, path, body, PARTIAL_JSON_UPDATE.mediaType());
    }

    private ApiResponse patch(
            final Thingifier thingifier,
            final String path,
            final String body,
            final String contentType) {
        HttpHeadersBlock headers = new HttpHeadersBlock();
        headers.put("Content-Type", contentType);
        return thingifier.api().patch(path, body, headers);
    }

    private BodyParser parser(final Thingifier thingifier, final String body) {
        return new BodyParser(
                new HttpApiRequest("/request").setBody(body), thingifier.getThingNames());
    }

    private HttpApiRequest jsonRequest(final String path, final String verb, final String body) {
        return new HttpApiRequest(path)
                .setVerb(verb)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .setBody(body);
    }

    private String noteJson(final String id, final String title) {
        return "{\"id\":\"" + id + "\",\"title\":\"" + title + "\"}";
    }

    private int noteCount(final Thingifier thingifier) {
        return thingifier
                .api()
                .get("notes", new QueryFilterParams(), new HttpHeadersBlock())
                .getReturnedInstanceCollection()
                .size();
    }

    private String currentTitle(final Thingifier thingifier, final String id) {
        return currentNote(thingifier, id).getFieldValue("title").asString();
    }

    private String currentDescription(final Thingifier thingifier, final String id) {
        return currentNote(thingifier, id).getFieldValue("description").asString();
    }

    private EntityInstance currentNote(final Thingifier thingifier, final String id) {
        return thingifier
                .api()
                .get("notes/" + id, new QueryFilterParams(), new HttpHeadersBlock())
                .getReturnedInstance();
    }

    private RoutingDefinition route(
            final ApiRoutingDefinition definition, final RoutingVerb verb, final String url) {
        return definition.definitions().stream()
                .filter(route -> route.verb() == verb)
                .filter(route -> route.url().equals(url))
                .findFirst()
                .orElseThrow();
    }

    private Set<Integer> statusCodes(final RoutingDefinition route) {
        return route.getPossibleStatusReponses().stream()
                .map(status -> status.value())
                .collect(Collectors.toSet());
    }
}
