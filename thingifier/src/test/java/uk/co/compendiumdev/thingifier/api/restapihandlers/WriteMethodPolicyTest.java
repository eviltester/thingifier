package uk.co.compendiumdev.thingifier.api.restapihandlers;

import static uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation.CREATE;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation.UPDATE;
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
    public void patchCanBeEnabledForEntityInstanceUpdates() {
        Thingifier thingifier = stringIdNotes();
        thingifier.apiConfig().writeMethods().entities().patchCan(UPDATE);
        createNote(thingifier, "one", "One");

        ApiResponse response = patch(thingifier, "notes/one", "{\"title\":\"Patched\"}");

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                "Patched", response.getReturnedInstance().getFieldValue("title").asString());
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
        Assertions.assertEquals(
                405,
                post(
                                createOnly,
                                "projects/" + project.getPrimaryKeyValue() + "/tasks",
                                "{\"id\":" + task.getPrimaryKeyValue() + "}")
                        .getStatusCode());

        Thingifier connectOnly = relationshipModel();
        connectOnly.apiConfig().writeMethods().relationships().postCan(CONNECT_EXISTING);
        EntityInstance otherProject = createProject(connectOnly, "Project");
        EntityInstance otherTask = createTask(connectOnly, "Existing");

        Assertions.assertEquals(
                405,
                post(
                                connectOnly,
                                "projects/" + otherProject.getPrimaryKeyValue() + "/tasks",
                                "{\"title\":\"New\"}")
                        .getStatusCode());
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
        thingifier.apiConfig().writeMethods().entities().patchCan(UPDATE);
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
        EntityDefinition note = thingifier.getDefinitionNamed("note");
        return thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(note)
                                .withField("id", id)
                                .withField("title", title));
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
        return thingifier.api().patch(path, parser(thingifier, body), new HttpHeadersBlock());
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
