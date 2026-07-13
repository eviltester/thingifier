package uk.co.compendiumdev.thingifier.api.restapihandlers;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.application.command.AmendThingCommand;
import uk.co.compendiumdev.thingifier.application.command.ConnectExistingRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateAndConnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DeleteThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DisconnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public class ThingWriteRequestMapperTest {

    @Test
    public void mapsPostCollectionToCreateCommand() {
        Thingifier thingifier = taskProjectThingifier();
        ThingWriteRequestMapping mapping =
                mapperFor(thingifier).mapPost("task", parserFor(thingifier, "title", "Task"));

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getCommand() instanceof CreateThingCommand);
    }

    @Test
    public void mapsPostInstanceToPatchAmendCommand() {
        Thingifier thingifier = taskProjectThingifier();
        EntityInstance task = createTask(thingifier, "Task");

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier)
                        .mapPost(
                                String.format("task/%s", task.getPrimaryKeyValue()),
                                parserFor(thingifier, "title", "Patched"));

        Assertions.assertFalse(mapping.isError());
        AmendThingCommand command = (AmendThingCommand) mapping.getCommand();
        Assertions.assertFalse(command.shouldReplaceExistingFieldsAndRelationships());
    }

    @Test
    public void mapsPutExistingInstanceToReplaceAmendCommand() {
        Thingifier thingifier = taskProjectThingifier();
        EntityInstance task = createTask(thingifier, "Task");

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier)
                        .mapPut(
                                String.format("task/%s", task.getPrimaryKeyValue()),
                                parserFor(thingifier, "title", "Replaced"));

        Assertions.assertFalse(mapping.isError());
        AmendThingCommand command = (AmendThingCommand) mapping.getCommand();
        Assertions.assertTrue(command.shouldReplaceExistingFieldsAndRelationships());
    }

    @Test
    public void mapsPutMissingStringKeyInstanceToCreateCommand() {
        Thingifier thingifier = stringKeyThingifier();

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier).mapPut("note/n-1", parserFor(thingifier, "title", "Created"));

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getCommand() instanceof CreateThingCommand);
    }

    @Test
    public void rejectsPutCreateWhenBodyPrimaryKeyDoesNotMatchUrlKey() {
        Thingifier thingifier = stringKeyThingifier();
        Map<String, String> body = new HashMap<>();
        body.put("key", "other");
        body.put("title", "Created");

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier).mapPut("note/n-1", parserFor(thingifier, body));

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(400, mapping.getErrorResponse().getStatusCode());
    }

    @Test
    public void rejectsPutCreateWhenEntityHasAutoFields() {
        Thingifier thingifier = taskProjectThingifier();

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier)
                        .mapPut("task/manual", parserFor(thingifier, "title", "Created"));

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(400, mapping.getErrorResponse().getStatusCode());
    }

    @Test
    public void postMissingInstanceMapsToNotFound() {
        Thingifier thingifier = taskProjectThingifier();

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier)
                        .mapPost("task/missing", parserFor(thingifier, "title", "Patched"));

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(404, mapping.getErrorResponse().getStatusCode());
    }

    @Test
    public void mapsRelationshipPostWithExistingChildToConnectCommand() {
        Thingifier thingifier = taskProjectThingifier();
        EntityInstance task = createTask(thingifier, "Task");
        EntityInstance project = createProject(thingifier, "Project");

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier)
                        .mapPost(
                                String.format("project/%s/tasks", project.getPrimaryKeyValue()),
                                parserFor(thingifier, "guid", task.getPrimaryKeyValue()));

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getCommand() instanceof ConnectExistingRelationshipCommand);
    }

    @Test
    public void mapsRelationshipPostWithoutChildKeyToCreateAndConnectCommand() {
        Thingifier thingifier = taskProjectThingifier();
        EntityInstance project = createProject(thingifier, "Project");

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier)
                        .mapPost(
                                String.format("project/%s/tasks", project.getPrimaryKeyValue()),
                                parserFor(thingifier, "title", "New task"));

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getCommand() instanceof CreateAndConnectRelationshipCommand);
    }

    @Test
    public void mapsDeleteInstanceToDeleteCommand() {
        Thingifier thingifier = taskProjectThingifier();
        EntityInstance task = createTask(thingifier, "Task");

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier)
                        .mapDelete(String.format("task/%s", task.getPrimaryKeyValue()));

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getCommand() instanceof DeleteThingCommand);
    }

    @Test
    public void mapsDeleteRelationshipPathToDisconnectCommand() {
        Thingifier thingifier = taskProjectThingifier();
        ThingStore store = storeFor(thingifier);
        EntityInstance task = createTask(thingifier, "Task");
        EntityInstance project = createProject(thingifier, "Project");
        store.relationships().connect(project, "tasks", task);

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier)
                        .mapDelete(
                                String.format(
                                        "project/%s/tasks/%s",
                                        project.getPrimaryKeyValue(), task.getPrimaryKeyValue()));

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getCommand() instanceof DisconnectRelationshipCommand);
    }

    @Test
    public void mapsUnrecognisedPostToBadRequest() {
        Thingifier thingifier = taskProjectThingifier();

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier)
                        .mapPost("not-understood", parserFor(thingifier, "title", "Nope"));

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(400, mapping.getErrorResponse().getStatusCode());
    }

    private Thingifier taskProjectThingifier() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        task.addField(Field.is("title", FieldType.STRING));

        EntityDefinition project = thingifier.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        project.addField(Field.is("title", FieldType.STRING));

        thingifier
                .defineRelationship(task, project, "task-of", Cardinality.ONE_TO_ONE())
                .whenReversed(Cardinality.ONE_TO_MANY(), "tasks");
        return thingifier;
    }

    private Thingifier stringKeyThingifier() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition note = thingifier.defineThing("note", "notes");
        note.addAsPrimaryKeyField(Field.is("key", FieldType.STRING));
        note.addField(Field.is("title", FieldType.STRING));
        return thingifier;
    }

    private EntityInstance createTask(final Thingifier thingifier, final String title) {
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        return storeFor(thingifier)
                .entities()
                .create(EntityInstanceDraft.forEntity(task).withField("title", title));
    }

    private EntityInstance createProject(final Thingifier thingifier, final String title) {
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        return storeFor(thingifier)
                .entities()
                .create(EntityInstanceDraft.forEntity(project).withField("title", title));
    }

    private ThingWriteRequestMapper mapperFor(final Thingifier thingifier) {
        return new ThingWriteRequestMapper(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    private ThingStore storeFor(final Thingifier thingifier) {
        return thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    private BodyParser parserFor(
            final Thingifier thingifier, final String fieldName, final String fieldValue) {
        Map<String, String> body = new HashMap<>();
        body.put(fieldName, fieldValue);
        return parserFor(thingifier, body);
    }

    private BodyParser parserFor(final Thingifier thingifier, final Map<String, String> body) {
        HttpApiRequest request = new HttpApiRequest("/path").setBody(new Gson().toJson(body));
        return new BodyParser(request, thingifier.getThingNames());
    }
}
