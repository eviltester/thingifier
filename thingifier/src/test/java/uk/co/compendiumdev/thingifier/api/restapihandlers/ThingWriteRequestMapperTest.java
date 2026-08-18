package uk.co.compendiumdev.thingifier.api.restapihandlers;

import static uk.co.compendiumdev.thingifier.apiconfig.PutIdentifierPolicy.DISALLOWED;
import static uk.co.compendiumdev.thingifier.apiconfig.PutIdentifierPolicy.MANDATORY;
import static uk.co.compendiumdev.thingifier.apiconfig.PutIdentifierPolicy.OPTIONAL;
import static uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation.UPDATE_CONNECTED;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingWriteRequestMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingWriteRequestMapping;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierSchemaCatalog;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRouteMapper;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.apiconfig.EntityWriteMethodConfig;
import uk.co.compendiumdev.thingifier.application.command.AmendThingCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DeleteThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DisconnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.RelateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.ReplaceThingCommand;
import uk.co.compendiumdev.thingifier.application.command.UpdateConnectedRelationshipCommand;
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
                mapperFor(thingifier)
                        .mapPost(routeFor(thingifier, "task"), parserFor("title", "Task"));

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
                                routeFor(
                                        thingifier,
                                        String.format("task/%s", task.getPrimaryKeyValue())),
                                parserFor("title", "Patched"));

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
                                routeFor(
                                        thingifier,
                                        String.format("task/%s", task.getPrimaryKeyValue())),
                                parserFor("title", "Replaced"));

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getCommand() instanceof ReplaceThingCommand);
    }

    @Test
    public void mapsPutInstanceToPutCommand() {
        Thingifier thingifier = stringKeyThingifier();

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier)
                        .mapPut(routeFor(thingifier, "note/n-1"), parserFor("title", "Created"));

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getCommand() instanceof ReplaceThingCommand);
    }

    @Test
    public void defaultPutMappingRejectsCollectionRoutes() {
        Thingifier thingifier = stringKeyThingifier();

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier)
                        .mapPut(
                                routeFor(thingifier, "notes"),
                                parserFor("title", "Missing identifier"));

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(405, mapping.getError().statusCode());
        Assertions.assertEquals(
                List.of("Cannot create root level entity with a PUT"),
                mapping.getError().messages());
    }

    @Test
    public void mapsPutCollectionToReplaceCommandWhenPayloadIdentifierIsAllowed() {
        Thingifier thingifier = stringKeyThingifier();
        EntityWriteMethodConfig config = new EntityWriteMethodConfig().putIdentifierInUri(OPTIONAL);

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier, config)
                        .mapPut(
                                routeFor(thingifier, "notes"),
                                parserFor(Map.of("key", "n-1", "title", "Created")));

        Assertions.assertFalse(mapping.isError());
        ReplaceThingCommand command = (ReplaceThingCommand) mapping.getCommand();
        Assertions.assertEquals("n-1", command.getIdentifier());
    }

    @Test
    public void putMappingRejectsInstanceRoutesWhenUriIdentifierIsDisallowed() {
        Thingifier thingifier = stringKeyThingifier();
        EntityWriteMethodConfig config =
                new EntityWriteMethodConfig().putIdentifierInUri(DISALLOWED);

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier, config)
                        .mapPut(
                                routeFor(thingifier, "notes/n-1"),
                                parserFor(Map.of("key", "n-1", "title", "Blocked")));

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(405, mapping.getError().statusCode());
        Assertions.assertEquals(
                List.of("Cannot identify entity with URI for PUT"), mapping.getError().messages());
    }

    @Test
    public void putMappingRejectsMissingMandatoryPayloadIdentifier() {
        Thingifier thingifier = stringKeyThingifier();
        EntityWriteMethodConfig config =
                new EntityWriteMethodConfig().putIdentifierInPayload(MANDATORY);

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier, config)
                        .mapPut(routeFor(thingifier, "notes/n-1"), parserFor("title", "Blocked"));

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(422, mapping.getError().statusCode());
        Assertions.assertEquals(
                List.of("PUT payload must include identifier field key"),
                mapping.getError().messages());
    }

    @Test
    public void putMappingRejectsDisallowedPayloadIdentifier() {
        Thingifier thingifier = stringKeyThingifier();
        EntityWriteMethodConfig config =
                new EntityWriteMethodConfig().putIdentifierInPayload(DISALLOWED);

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier, config)
                        .mapPut(
                                routeFor(thingifier, "notes/n-1"),
                                parserFor(Map.of("key", "n-1", "title", "Blocked")));

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(422, mapping.getError().statusCode());
        Assertions.assertEquals(
                List.of("PUT payload must not include identifier field key"),
                mapping.getError().messages());
    }

    @Test
    public void postInstanceMapsToUnresolvedAmendCommand() {
        Thingifier thingifier = taskProjectThingifier();

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier)
                        .mapPost(
                                routeFor(thingifier, "task/missing"),
                                parserFor("title", "Patched"));

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getCommand() instanceof AmendThingCommand);
    }

    @Test
    public void mapsRelationshipPostWithExistingChildToRelateCommand() {
        Thingifier thingifier = taskProjectThingifier();
        EntityInstance task = createTask(thingifier, "Task");
        EntityInstance project = createProject(thingifier, "Project");

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier)
                        .mapPost(
                                routeFor(
                                        thingifier,
                                        String.format(
                                                "project/%s/tasks", project.getPrimaryKeyValue())),
                                parserFor("guid", task.getPrimaryKeyValue()));

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getCommand() instanceof RelateThingCommand);
    }

    @Test
    public void mapsRelationshipPostWithoutChildKeyToRelateCommand() {
        Thingifier thingifier = taskProjectThingifier();
        EntityInstance project = createProject(thingifier, "Project");

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier)
                        .mapPost(
                                routeFor(
                                        thingifier,
                                        String.format(
                                                "project/%s/tasks", project.getPrimaryKeyValue())),
                                parserFor("title", "New task"));

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getCommand() instanceof RelateThingCommand);
    }

    @Test
    public void mapsRelationshipPostWithConnectedChildToUpdateCommandWhenConfigured() {
        Thingifier thingifier = taskProjectThingifier();
        ThingStore store = storeFor(thingifier);
        EntityInstance task = createTask(thingifier, "Task");
        EntityInstance project = createProject(thingifier, "Project");
        store.relationships().connect(project, "tasks", task);

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier, Set.of(UPDATE_CONNECTED))
                        .mapPost(
                                routeFor(
                                        thingifier,
                                        String.format(
                                                "project/%s/tasks", project.getPrimaryKeyValue())),
                                parserFor(
                                        Map.of(
                                                "guid",
                                                task.getPrimaryKeyValue(),
                                                "title",
                                                "Updated")));

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getCommand() instanceof UpdateConnectedRelationshipCommand);
    }

    @Test
    public void mapsDeleteInstanceToDeleteCommand() {
        Thingifier thingifier = taskProjectThingifier();
        EntityInstance task = createTask(thingifier, "Task");

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier)
                        .mapDelete(
                                routeFor(
                                        thingifier,
                                        String.format("task/%s", task.getPrimaryKeyValue())));

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
                                routeFor(
                                        thingifier,
                                        String.format(
                                                "project/%s/tasks/%s",
                                                project.getPrimaryKeyValue(),
                                                task.getPrimaryKeyValue())));

        Assertions.assertFalse(mapping.isError());
        Assertions.assertTrue(mapping.getCommand() instanceof DisconnectRelationshipCommand);
    }

    @Test
    public void mapsUnrecognisedPostToBadRequest() {
        Thingifier thingifier = taskProjectThingifier();

        ThingWriteRequestMapping mapping =
                mapperFor(thingifier)
                        .mapPost(
                                routeFor(thingifier, "not-understood"), parserFor("title", "Nope"));

        Assertions.assertTrue(mapping.isError());
        Assertions.assertEquals(400, mapping.getError().statusCode());
        Assertions.assertEquals(
                List.of("Your request was not understood"), mapping.getError().messages());
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
        return new ThingWriteRequestMapper(new ThingifierSchemaCatalog(thingifier));
    }

    private ThingWriteRequestMapper mapperFor(
            final Thingifier thingifier, final EntityWriteMethodConfig config) {
        return new ThingWriteRequestMapper(new ThingifierSchemaCatalog(thingifier), config);
    }

    private ThingWriteRequestMapper mapperFor(
            final Thingifier thingifier,
            final Set<uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation>
                    relationshipOperations) {
        return new ThingWriteRequestMapper(
                new ThingifierSchemaCatalog(thingifier),
                thingifier.apiDefaults().writeMethods().entities(),
                relationshipOperations,
                ThingifierRequestContext.from(thingifier, new HttpHeadersBlock()));
    }

    private ThingStore storeFor(final Thingifier thingifier) {
        return thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    private ThingRoute routeFor(final Thingifier thingifier, final String url) {
        return new ThingRouteMapper(new ThingifierSchemaCatalog(thingifier)).map(url);
    }

    private ApiBodyFields parserFor(final String fieldName, final String fieldValue) {
        Map<String, Object> body = new HashMap<>();
        body.put(fieldName, fieldValue);
        return parserFor(body);
    }

    private ApiBodyFields parserFor(final Map<String, Object> body) {
        return ApiBodyFields.fromMap(body);
    }
}
