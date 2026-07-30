package uk.co.compendiumdev.configuredwritemethods.application;

import static uk.co.compendiumdev.configuredwritemethods.application.ConfiguredWriteMethodsSampleApiTestSupport.createStoredNote;
import static uk.co.compendiumdev.configuredwritemethods.application.ConfiguredWriteMethodsSampleApiTestSupport.currentNote;
import static uk.co.compendiumdev.configuredwritemethods.application.ConfiguredWriteMethodsSampleApiTestSupport.noteJson;
import static uk.co.compendiumdev.configuredwritemethods.application.ConfiguredWriteMethodsSampleApiTestSupport.patch;
import static uk.co.compendiumdev.configuredwritemethods.application.ConfiguredWriteMethodsSampleApiTestSupport.post;
import static uk.co.compendiumdev.configuredwritemethods.application.ConfiguredWriteMethodsSampleApiTestSupport.put;
import static uk.co.compendiumdev.configuredwritemethods.application.ConfiguredWriteMethodsSampleApiTestSupport.sample;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.JSON_MERGE_PATCH_RFC7396;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.JSON_PATCH_RFC6902;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;

class ConfiguredWriteMethodsDirectApiTest {

    @Test
    void postNotesCreatesANote() {
        final Thingifier thingifier = sample();

        final ApiResponse response = post(thingifier, "notes", noteJson("One"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(
                "One", response.getReturnedInstance().getFieldValue("title").asString());
        Assertions.assertEquals(
                "sample", response.getReturnedInstance().getFieldValue("description").asString());
    }

    @Test
    void postExistingNoteIsBlocked() {
        final Thingifier thingifier = sample();
        final String id = createStoredNote(thingifier).getPrimaryKeyValue();

        final ApiResponse response = post(thingifier, "notes/" + id, "{\"title\":\"Blocked\"}");

        Assertions.assertEquals(405, response.getStatusCode());
        Assertions.assertEquals(
                "Existing", currentNote(thingifier, id).getFieldValue("title").asString());
    }

    @Test
    void partialJsonPatchUpdatesTheExistingNote() {
        final Thingifier thingifier = sample();
        final String id = createStoredNote(thingifier).getPrimaryKeyValue();

        final ApiResponse response =
                patch(thingifier, "notes/" + id, "{\"title\":\"Patched Direct\"}");

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "Patched Direct", response.getReturnedInstance().getFieldValue("title").asString());
        Assertions.assertEquals(
                "sample", response.getReturnedInstance().getFieldValue("description").asString());
    }

    @Test
    void mergePatchUpdatesTheExistingNote() {
        final Thingifier thingifier = sample();
        final String id = createStoredNote(thingifier).getPrimaryKeyValue();

        final ApiResponse response =
                patch(
                        thingifier,
                        "notes/" + id,
                        "{\"description\":\"Merged Direct\"}",
                        JSON_MERGE_PATCH_RFC7396.mediaType());

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "Existing", response.getReturnedInstance().getFieldValue("title").asString());
        Assertions.assertEquals(
                "Merged Direct",
                response.getReturnedInstance().getFieldValue("description").asString());
    }

    @Test
    void jsonPatchUpdatesTheExistingNote() {
        final Thingifier thingifier = sample();
        final String id = createStoredNote(thingifier).getPrimaryKeyValue();

        final ApiResponse response =
                patch(
                        thingifier,
                        "notes/" + id,
                        "[{\"op\":\"replace\",\"path\":\"/title\",\"value\":\"Json Patch Direct\"}]",
                        JSON_PATCH_RFC6902.mediaType());

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "Json Patch Direct",
                response.getReturnedInstance().getFieldValue("title").asString());
        Assertions.assertEquals(
                "sample", response.getReturnedInstance().getFieldValue("description").asString());
    }

    @Test
    void putExistingNoteUpdatesTheNote() {
        final Thingifier thingifier = sample();
        final String id = createStoredNote(thingifier).getPrimaryKeyValue();

        final ApiResponse response = put(thingifier, "notes/" + id, "{\"title\":\"Put Direct\"}");

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "Put Direct", response.getReturnedInstance().getFieldValue("title").asString());
    }

    @Test
    void putMissingNoteIsBlockedBecausePutCannotCreate() {
        final ApiResponse response = put(sample(), "notes/999", "{\"title\":\"Missing\"}");

        Assertions.assertEquals(405, response.getStatusCode());
    }
}
