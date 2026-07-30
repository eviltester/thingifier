package uk.co.compendiumdev.thingifier.adapter.javalin;

import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.JSON_PATCH_RFC6902;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.PARTIAL_JSON_UPDATE;

import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteRegistry;
import uk.co.compendiumdev.thingifier.adapter.httpserver.ThingifierHttpApiRoutings;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.apiconfig.ApiDocsConfig;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfiles;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
class JavalinThingifierPatchRoutesTest {

    private Thingifier thingifier;
    private JavalinHttpServer server;
    private String baseUrl;

    @BeforeAll
    void startServer() throws Exception {
        thingifier = patchableNotesThingifier();
        HttpRouteRegistry registry = registerThingifierApiRoutes(thingifier);
        int port = availablePort();
        server = new JavalinHttpServer(port, "/public", registry);
        server.start();
        baseUrl = "http://localhost:" + port + "/api";
    }

    @BeforeEach
    void clearBusinessData() {
        thingifier.clearAllData();
    }

    @AfterAll
    void stopServer() {
        try {
            if (server != null) {
                server.close();
            }
        } finally {
            HttpRouteRegistry.clearCurrent();
        }
    }

    @Test
    void postCreatesANoteThroughTheThingifierServerRoutes() throws Exception {
        EntityInstance createdNote = createNoteThroughServer("Original", "Keep");

        Assertions.assertEquals("Original", createdNote.getFieldValue("title").asString());
        Assertions.assertEquals("Keep", createdNote.getFieldValue("description").asString());
    }

    @Test
    void optionsAdvertisesPatchFormatsForPatchableEntityInstances() throws Exception {
        EntityInstance createdNote = createNoteThroughServer("Original", "Keep");

        HttpResponse<String> options =
                request(
                        "OPTIONS",
                        baseUrl + "/notes/" + createdNote.getPrimaryKeyValue(),
                        "",
                        null);

        Assertions.assertEquals(204, options.statusCode());
        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, POST, PUT, PATCH, DELETE",
                options.headers().firstValue("Allow").orElse(""));
        Assertions.assertEquals(
                patchFormatsHeader(), options.headers().firstValue("Accept-Patch").orElse(""));
    }

    @Test
    void unsupportedPatchContentTypeReturns415AndDoesNotChangeTheNote() throws Exception {
        EntityInstance createdNote = createNoteThroughServer("Original", "Keep");

        HttpResponse<String> response =
                request(
                        "PATCH",
                        baseUrl + "/notes/" + createdNote.getPrimaryKeyValue(),
                        "{\"title\":\"Should Not Change\"}",
                        "application/merge-patch+json");

        Assertions.assertEquals(415, response.statusCode());
        Assertions.assertEquals(
                patchFormatsHeader(), response.headers().firstValue("Accept-Patch").orElse(""));
        Assertions.assertEquals(
                "Unsupported PATCH Content Type",
                json(response).getJSONArray("errorMessages").getString(0));
        Assertions.assertEquals(
                "Original",
                currentNote(createdNote.getPrimaryKeyValue()).getFieldValue("title").asString());
        Assertions.assertEquals(
                "Keep",
                currentNote(createdNote.getPrimaryKeyValue())
                        .getFieldValue("description")
                        .asString());
    }

    @Test
    void partialJsonPatchUpdatesOnlyTheProvidedFieldsThroughTheServer() throws Exception {
        EntityInstance createdNote = createNoteThroughServer("Original", "Keep");

        HttpResponse<String> response =
                request(
                        "PATCH",
                        baseUrl + "/notes/" + createdNote.getPrimaryKeyValue(),
                        "{\"title\":\"Partial Updated\"}",
                        PARTIAL_JSON_UPDATE.mediaType());

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(
                "application/json", response.headers().firstValue("Content-Type").orElse(""));
        Assertions.assertEquals("Partial Updated", json(response).getString("title"));
        Assertions.assertEquals("Keep", json(response).getString("description"));
        Assertions.assertEquals(
                "Partial Updated",
                currentNote(createdNote.getPrimaryKeyValue()).getFieldValue("title").asString());
        Assertions.assertEquals(
                "Keep",
                currentNote(createdNote.getPrimaryKeyValue())
                        .getFieldValue("description")
                        .asString());
    }

    @Test
    void jsonPatchUpdatesTheTargetThroughTheServer() throws Exception {
        EntityInstance createdNote = createNoteThroughServer("Original", "Keep");

        HttpResponse<String> response =
                request(
                        "PATCH",
                        baseUrl + "/notes/" + createdNote.getPrimaryKeyValue(),
                        "[{\"op\":\"replace\",\"path\":\"/description\",\"value\":\"JSON Patch Updated\"}]",
                        JSON_PATCH_RFC6902.mediaType());

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("Original", json(response).getString("title"));
        Assertions.assertEquals("JSON Patch Updated", json(response).getString("description"));
        Assertions.assertEquals(
                "Original",
                currentNote(createdNote.getPrimaryKeyValue()).getFieldValue("title").asString());
        Assertions.assertEquals(
                "JSON Patch Updated",
                currentNote(createdNote.getPrimaryKeyValue())
                        .getFieldValue("description")
                        .asString());
    }

    private EntityInstance createNoteThroughServer(final String title, final String description)
            throws Exception {
        HttpResponse<String> created =
                request(
                        "POST",
                        baseUrl + "/notes",
                        "{\"title\":\"" + title + "\",\"description\":\"" + description + "\"}",
                        PARTIAL_JSON_UPDATE.mediaType());

        Assertions.assertEquals(201, created.statusCode());
        Assertions.assertEquals(title, json(created).getString("title"));
        Assertions.assertEquals(description, json(created).getString("description"));

        EntityInstance createdNote = onlyNote();
        Assertions.assertEquals(title, createdNote.getFieldValue("title").asString());
        Assertions.assertEquals(description, createdNote.getFieldValue("description").asString());
        return createdNote;
    }

    private HttpResponse<String> request(
            final String method, final String url, final String body, final String contentType)
            throws Exception {
        HttpRequest.Builder builder =
                HttpRequest.newBuilder(new URI(url)).header("Accept", "application/json");
        if (contentType != null) {
            builder.header("Content-Type", contentType);
        }
        return HttpClient.newHttpClient()
                .send(
                        builder.method(
                                        method,
                                        body == null || body.isEmpty()
                                                ? HttpRequest.BodyPublishers.noBody()
                                                : HttpRequest.BodyPublishers.ofString(body))
                                .build(),
                        HttpResponse.BodyHandlers.ofString());
    }

    private JSONObject json(final HttpResponse<String> response) {
        return new JSONObject(response.body());
    }

    private String patchFormatsHeader() {
        return String.join(", ", PARTIAL_JSON_UPDATE.mediaType(), JSON_PATCH_RFC6902.mediaType());
    }

    private Thingifier patchableNotesThingifier() {
        Thingifier thingifier =
                new Thingifier(
                        new EntityRelModel(),
                        new ThingifierApiConfig("api"),
                        new ThingifierApiConfigProfiles(),
                        "",
                        "",
                        new ApiDocsConfig());
        EntityDefinition note = thingifier.defineThing("note", "notes");
        note.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        note.addField(Field.is("title", FieldType.STRING).makeMandatory());
        note.addField(Field.is("description", FieldType.STRING));
        thingifier
                .apiConfig()
                .writeMethods()
                .entities()
                .patchCan(PARTIAL_JSON_UPDATE, JSON_PATCH_RFC6902);
        return thingifier;
    }

    private HttpRouteRegistry registerThingifierApiRoutes(final Thingifier thingifier) {
        HttpRouteRegistry registry = new HttpRouteRegistry();
        HttpRouteRegistry.use(registry);
        ThingifierApiDocumentationDefn apiDefn =
                new ThingifierApiDocumentationDefn().setThingifier(thingifier);
        apiDefn.setPathPrefix("api");
        new ThingifierHttpApiRoutings(thingifier, apiDefn);
        return registry;
    }

    private EntityInstance currentNote(final String id) {
        return thingifier
                .api()
                .get("notes/" + id, new QueryFilterParams(), new HttpHeadersBlock())
                .getReturnedInstance();
    }

    private EntityInstance onlyNote() {
        return thingifier
                .api()
                .get("notes", new QueryFilterParams(), new HttpHeadersBlock())
                .getReturnedInstanceCollection()
                .get(0);
    }

    private int availablePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
