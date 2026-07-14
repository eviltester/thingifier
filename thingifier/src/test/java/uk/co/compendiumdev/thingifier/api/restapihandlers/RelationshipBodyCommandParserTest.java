package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.RelationshipBodyCommandParser;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.RelationshipBodyCommands;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingReadRequestMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierSchemaCatalog;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRouteMapper;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.application.command.RelationshipReference;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public class RelationshipBodyCommandParserTest {

    @Test
    public void parsesCompressedRelationshipAsValidationAndExecutionCommand() {
        Thingifier thingifier = taskProjectThingifier();
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        EntityInstance projectInstance =
                thingifier
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Project"));

        Map<String, String> body = new HashMap<>();
        body.put("title", "Task");
        body.put("task-of.guid", projectInstance.getPrimaryKeyValue());

        RelationshipBodyCommands commands =
                new RelationshipBodyCommandParser(new ThingifierSchemaCatalog(thingifier))
                        .parse(bodyFieldsFor(body).asFlattenedStringMap(), task);

        Assertions.assertTrue(commands.validationReport().isValid());
        Assertions.assertEquals(1, commands.relationshipEntries().size());

        List<RelationshipReference> references = commands.references();
        Assertions.assertEquals(1, references.size());
        Assertions.assertEquals("task-of", references.get(0).relationshipName());
        Assertions.assertEquals("guid", references.get(0).referenceFieldName());
        Assertions.assertEquals(
                projectInstance.getPrimaryKeyValue(), references.get(0).referenceValue());
    }

    @Test
    public void routeParserRecognisesRelationshipRoutesAtRestBoundary() {
        Thingifier thingifier = taskProjectThingifier();
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        EntityInstance projectInstance =
                thingifier
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Project"));

        ThingReadRequestMapper mapper =
                new ThingReadRequestMapper(new ThingifierSchemaCatalog(thingifier));
        ThingifierSchemaCatalog schema = new ThingifierSchemaCatalog(thingifier);

        Assertions.assertFalse(
                mapper.map(
                                new ThingRouteMapper(schema)
                                        .map(
                                                String.format(
                                                        "project/%s/tasks",
                                                        projectInstance.getPrimaryKeyValue())),
                                new QueryFilterParams())
                        .isError());
        Assertions.assertTrue(
                mapper.map(
                                new ThingRouteMapper(schema)
                                        .map(
                                                String.format(
                                                        "project/%s/task",
                                                        projectInstance.getPrimaryKeyValue())),
                                new QueryFilterParams())
                        .isError());
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

    private ApiBodyFields bodyFieldsFor(final Map<String, String> body) {
        return ApiBodyFields.fromMap(new HashMap<>(body));
    }
}
