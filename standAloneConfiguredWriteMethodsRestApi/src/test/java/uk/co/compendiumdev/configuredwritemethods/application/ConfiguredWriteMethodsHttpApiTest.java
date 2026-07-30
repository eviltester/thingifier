package uk.co.compendiumdev.configuredwritemethods.application;

import static uk.co.compendiumdev.configuredwritemethods.application.ConfiguredWriteMethodsSampleApiTestSupport.createStoredNote;
import static uk.co.compendiumdev.configuredwritemethods.application.ConfiguredWriteMethodsSampleApiTestSupport.currentNote;
import static uk.co.compendiumdev.configuredwritemethods.application.ConfiguredWriteMethodsSampleApiTestSupport.httpApi;
import static uk.co.compendiumdev.configuredwritemethods.application.ConfiguredWriteMethodsSampleApiTestSupport.httpApiFor;
import static uk.co.compendiumdev.configuredwritemethods.application.ConfiguredWriteMethodsSampleApiTestSupport.jsonRequest;
import static uk.co.compendiumdev.configuredwritemethods.application.ConfiguredWriteMethodsSampleApiTestSupport.noteJson;
import static uk.co.compendiumdev.configuredwritemethods.application.ConfiguredWriteMethodsSampleApiTestSupport.sample;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.JSON_MERGE_PATCH_RFC7396;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.JSON_PATCH_RFC6902;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;

class ConfiguredWriteMethodsHttpApiTest {

    @Test
    void postNotesCreatesANote() {
        final HttpApiResponse response =
                httpApi().post(jsonRequest("notes", "POST", noteJson("One")));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(
                "One",
                response.apiResponse().getReturnedInstance().getFieldValue("title").asString());
        Assertions.assertEquals(
                "sample",
                response.apiResponse()
                        .getReturnedInstance()
                        .getFieldValue("description")
                        .asString());
    }

    @Test
    void postExistingNoteIsBlocked() {
        final Thingifier thingifier = sample();
        final String id = createStoredNote(thingifier).getPrimaryKeyValue();

        final HttpApiResponse response =
                httpApiFor(thingifier)
                        .post(jsonRequest("notes/" + id, "POST", "{\"title\":\"Blocked\"}"));

        Assertions.assertEquals(405, response.getStatusCode());
        Assertions.assertEquals(
                "Existing", currentNote(thingifier, id).getFieldValue("title").asString());
    }

    @Test
    void partialJsonPatchUpdatesTheExistingNote() {
        final Thingifier thingifier = sample();
        final String id = createStoredNote(thingifier).getPrimaryKeyValue();

        final HttpApiResponse response =
                httpApiFor(thingifier)
                        .patch(jsonRequest("notes/" + id, "PATCH", "{\"title\":\"Patched\"}"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "Patched",
                response.apiResponse().getReturnedInstance().getFieldValue("title").asString());
        Assertions.assertEquals(
                "sample",
                response.apiResponse()
                        .getReturnedInstance()
                        .getFieldValue("description")
                        .asString());
    }

    @Test
    void mergePatchUpdatesTheExistingNote() {
        final Thingifier thingifier = sample();
        final String id = createStoredNote(thingifier).getPrimaryKeyValue();

        final HttpApiResponse response =
                httpApiFor(thingifier)
                        .patch(
                                jsonRequest(
                                        "notes/" + id,
                                        "PATCH",
                                        "{\"description\":\"Merged\"}",
                                        JSON_MERGE_PATCH_RFC7396.mediaType()));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "Existing",
                response.apiResponse().getReturnedInstance().getFieldValue("title").asString());
        Assertions.assertEquals(
                "Merged",
                response.apiResponse()
                        .getReturnedInstance()
                        .getFieldValue("description")
                        .asString());
    }

    @Test
    void jsonPatchUpdatesTheExistingNote() {
        final Thingifier thingifier = sample();
        final String id = createStoredNote(thingifier).getPrimaryKeyValue();

        final HttpApiResponse response =
                httpApiFor(thingifier)
                        .patch(
                                jsonRequest(
                                        "notes/" + id,
                                        "PATCH",
                                        "[{\"op\":\"replace\",\"path\":\"/title\",\"value\":\"Json Patch\"}]",
                                        JSON_PATCH_RFC6902.mediaType()));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "Json Patch",
                response.apiResponse().getReturnedInstance().getFieldValue("title").asString());
        Assertions.assertEquals(
                "sample",
                response.apiResponse()
                        .getReturnedInstance()
                        .getFieldValue("description")
                        .asString());
    }

    @Test
    void putExistingNoteUpdatesTheNote() {
        final Thingifier thingifier = sample();
        final String id = createStoredNote(thingifier).getPrimaryKeyValue();

        final HttpApiResponse response =
                httpApiFor(thingifier)
                        .put(jsonRequest("notes/" + id, "PUT", "{\"title\":\"Put\"}"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "Put",
                response.apiResponse().getReturnedInstance().getFieldValue("title").asString());
    }

    @Test
    void putMissingNoteIsBlockedBecausePutCannotCreate() {
        final HttpApiResponse response =
                httpApi().put(jsonRequest("notes/999", "PUT", "{\"title\":\"Missing\"}"));

        Assertions.assertEquals(405, response.getStatusCode());
    }
}
