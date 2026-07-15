package uk.co.compendiumdev.thingifier.application.schema.definition;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierSchemaCatalog;
import uk.co.compendiumdev.thingifier.application.schema.EntityTypeRef;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

class ThingifierModelAssemblerTest {

    @Test
    void validSpecsAssembleIntoThingifierSchema() {
        Thingifier thingifier = new ThingifierModelAssembler().assemble(taskProjectDefinition());

        Assertions.assertEquals("Task Project", thingifier.getTitle());
        Assertions.assertEquals("Task project schema", thingifier.getInitialParagraph());
        Assertions.assertNotNull(thingifier.getDefinitionNamed("task"));
        Assertions.assertNotNull(thingifier.getDefinitionNamed("project"));

        EntityDefinition task = thingifier.getDefinitionNamed("task");
        Assertions.assertEquals("id", task.getPrimaryKeyField().getName());
        Assertions.assertTrue(task.getField("title").isMandatory());
        Assertions.assertEquals(FieldType.STRING, task.getField("title").getType());
        Assertions.assertTrue(task.getField("priority").mustBeUnique());

        EntityTypeRef project =
                new ThingifierSchemaCatalog(thingifier).entityWithSingularOrPluralName("projects");
        Assertions.assertTrue(project.hasRelationship("tasks"));
    }

    @Test
    void assemblerOutputWorksWithSchemaViewCatalog() {
        Thingifier thingifier = new ThingifierModelAssembler().assemble(taskProjectDefinition());
        ThingifierSchemaCatalog catalog = new ThingifierSchemaCatalog(thingifier);

        EntityTypeRef task = catalog.entityWithSingularOrPluralName("todos");

        Assertions.assertEquals("task", task.name());
        Assertions.assertEquals("todos", task.pluralName());
        Assertions.assertEquals("id", task.primaryKeyFieldName());
        Assertions.assertEquals("title", task.fieldNamed("title").name());
    }

    @Test
    void invalidSpecsReturnPathSpecificValidationErrors() {
        ThingifierModelDefinition definition =
                ThingifierModelDefinition.builder()
                        .entity(
                                EntityDefinitionSpec.named("task")
                                        .plural("tasks")
                                        .primaryKey("missing")
                                        .field(FieldDefinitionSpec.named("title", "string").build())
                                        .build())
                        .entity(EntityDefinitionSpec.named("task").plural("tasks").build())
                        .relationship(
                                new RelationshipDefinitionSpec(
                                        "task",
                                        "widgets",
                                        "widget",
                                        CardinalitySpec.oneToMany(),
                                        "optional",
                                        null))
                        .build();

        SchemaDefinitionValidationReport report =
                new ThingifierModelAssembler().validate(definition);

        Assertions.assertFalse(report.isValid());
        Assertions.assertTrue(
                messages(report).contains("entities.task: Duplicate entity name task"));
        Assertions.assertTrue(
                messages(report)
                        .contains(
                                "entities.task.primaryKey: Primary key field missing is not defined"));
        Assertions.assertTrue(
                messages(report)
                        .contains("relationships[0].to: Unknown relationship target widget"));
    }

    @Test
    void invalidFieldConfigurationIsReportedBeforeAssembly() {
        ThingifierModelDefinition definition =
                ThingifierModelDefinition.builder()
                        .entity(
                                EntityDefinitionSpec.named("item")
                                        .plural("items")
                                        .field(FieldDefinitionSpec.named("state", "enum").build())
                                        .field(
                                                FieldDefinitionSpec.named("count", "integer")
                                                        .range("10", "2")
                                                        .build())
                                        .field(
                                                FieldDefinitionSpec.named("broken", "not-a-type")
                                                        .build())
                                        .build())
                        .build();

        SchemaDefinitionValidationReport report =
                new ThingifierModelAssembler().validate(definition);

        Assertions.assertFalse(report.isValid());
        Assertions.assertTrue(
                messages(report)
                        .contains(
                                "entities.item.fields.state.examples: Enum fields require examples"));
        Assertions.assertTrue(
                messages(report)
                        .contains(
                                "entities.item.fields.count: min must be less than or equal to max"));
        Assertions.assertTrue(
                messages(report)
                        .contains(
                                "entities.item.fields.broken.type: Unsupported field type not-a-type"));
    }

    @Test
    void invalidDefinitionsAreNotAssembled() {
        ThingifierModelDefinition definition =
                ThingifierModelDefinition.builder()
                        .entity(EntityDefinitionSpec.named("item").plural("items").build())
                        .relationship(
                                new RelationshipDefinitionSpec(
                                        "item",
                                        "bad",
                                        "missing",
                                        CardinalitySpec.oneToMany(),
                                        "optional",
                                        null))
                        .build();

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ThingifierModelAssembler().assemble(definition));
    }

    private ThingifierModelDefinition taskProjectDefinition() {
        return ThingifierModelDefinition.builder()
                .title("Task Project")
                .description("Task project schema")
                .entity(
                        EntityDefinitionSpec.named("task")
                                .plural("todos")
                                .primaryKey("id")
                                .field(FieldDefinitionSpec.named("id", "auto-increment").build())
                                .field(
                                        FieldDefinitionSpec.named("title", "string")
                                                .required(true)
                                                .validationRule(ValidationRuleSpec.notEmpty())
                                                .build())
                                .field(
                                        FieldDefinitionSpec.named("priority", "integer")
                                                .unique(true)
                                                .range("1", "5")
                                                .build())
                                .build())
                .entity(
                        EntityDefinitionSpec.named("project")
                                .plural("projects")
                                .field(FieldDefinitionSpec.named("title", "string").build())
                                .build())
                .relationship(
                        new RelationshipDefinitionSpec(
                                "project",
                                "tasks",
                                "task",
                                CardinalitySpec.oneToMany(),
                                "optional",
                                new RelationshipVectorSpec(
                                        "taskof", CardinalitySpec.oneToOne(), "mandatory")))
                .build();
    }

    private List<String> messages(final SchemaDefinitionValidationReport report) {
        return report.messages();
    }
}
