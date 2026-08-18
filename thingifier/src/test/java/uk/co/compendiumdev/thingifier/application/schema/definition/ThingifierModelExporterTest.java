package uk.co.compendiumdev.thingifier.application.schema.definition;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.schema.FieldReferenceSpec;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.VRule;

class ThingifierModelExporterTest {

    @Test
    void exporterCapturesJavaBuiltEntitiesAndFieldMetadata() {
        ThingifierModelDefinition definition = new ThingifierModelExporter().export(model());

        EntityDefinitionSpec task = definition.entityNamed("task");
        FieldDefinitionSpec title = task.fieldNamed("title");
        FieldDefinitionSpec status = task.fieldNamed("status");
        FieldDefinitionSpec priority = task.fieldNamed("priority");
        FieldDefinitionSpec metadata = task.fieldNamed("metadata");

        Assertions.assertEquals("Todo Model", definition.title());
        Assertions.assertEquals("Model description", definition.description());
        Assertions.assertEquals("A task item to complete.", task.description());
        Assertions.assertEquals("id", task.primaryKeyFieldName());
        Assertions.assertEquals("auto-increment", task.fieldNamed("id").type());
        Assertions.assertTrue(title.required());
        Assertions.assertEquals("Task title", title.description());
        Assertions.assertEquals(List.of("example title"), title.examples());
        Assertions.assertEquals(Integer.valueOf(30), title.truncateTo());
        Assertions.assertEquals("false", status.defaultValue());
        Assertions.assertEquals("1", priority.minValue());
        Assertions.assertEquals("5", priority.maxValue());
        Assertions.assertEquals("object", metadata.type());
        Assertions.assertEquals("source", metadata.objectFields().get(0).name());
    }

    @Test
    void exporterCapturesValidationRules() {
        FieldDefinitionSpec title =
                new ThingifierModelExporter()
                        .export(model())
                        .entityNamed("task")
                        .fieldNamed("title");

        Assertions.assertEquals(3, title.validationRules().size());
        Assertions.assertEquals(
                ValidationRuleSpec.NOT_EMPTY, title.validationRules().get(0).name());
        Assertions.assertEquals(
                ValidationRuleSpec.MAXIMUM_LENGTH, title.validationRules().get(1).name());
        Assertions.assertEquals("50", title.validationRules().get(1).value());
        Assertions.assertEquals(
                ValidationRuleSpec.MATCHES_REGEX, title.validationRules().get(2).name());
        Assertions.assertEquals(".+", title.validationRules().get(2).value());
    }

    @Test
    void exporterCapturesRelationshipsAndReverseVectors() {
        ThingifierModelDefinition definition = new ThingifierModelExporter().export(model());

        RelationshipDefinitionSpec relationship = definition.relationships().get(0);

        Assertions.assertEquals("project", relationship.fromEntityName());
        Assertions.assertEquals("tasks", relationship.name());
        Assertions.assertEquals("task", relationship.toEntityName());
        Assertions.assertEquals("one-to-many", relationship.cardinality().canonicalName());
        Assertions.assertTrue(relationship.hasReverse());
        Assertions.assertEquals("taskof", relationship.reverse().name());
        Assertions.assertEquals("one-to-one", relationship.reverse().cardinality().canonicalName());
    }

    @Test
    void exporterCapturesRelationshipOwnershipPolicies() {
        ThingifierModelDefinition definition =
                new ThingifierModelExporter().export(relationshipMetadataModel());

        RelationshipDefinitionSpec relationship = definition.relationships().get(0);

        Assertions.assertTrue(relationship.deleteTargetWhenDisconnected());
        Assertions.assertTrue(relationship.deleteTargetsWhenSourceDeleted());
    }

    @Test
    void exporterCapturesFieldRelationshipReferences() {
        ThingifierModelDefinition definition =
                new ThingifierModelExporter().export(relationshipMetadataModel());

        FieldReferenceSpec reference =
                definition.entityNamed("item").fieldNamed("productId").relationshipReference();

        Assertions.assertEquals("product", reference.targetEntityName());
        Assertions.assertEquals("id", reference.targetFieldName());
        Assertions.assertEquals("product", reference.relationshipName());
    }

    @Test
    void exportedDefinitionCanBeAssembledAgain() {
        ThingifierModelDefinition definition = new ThingifierModelExporter().export(model());

        Thingifier assembled = new ThingifierModelAssembler().assemble(definition);

        Assertions.assertNotNull(assembled.getDefinitionNamed("task"));
        Assertions.assertEquals(
                "A task item to complete.", assembled.getDefinitionNamed("task").getDescription());
        Assertions.assertEquals(
                "id", assembled.getDefinitionNamed("task").getPrimaryKeyField().getName());
        Assertions.assertTrue(assembled.hasRelationshipNamed("tasks"));
        Assertions.assertTrue(assembled.hasRelationshipNamed("taskof"));
    }

    private Thingifier model() {
        Thingifier thingifier = new Thingifier();
        thingifier.setDocumentation("Todo Model", "Model description");

        EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.withDescription("A task item to complete.");
        task.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        task.addFields(
                Field.is("title", FieldType.STRING)
                        .makeMandatory()
                        .withDescription("Task title")
                        .withExample("example title")
                        .truncateStringTo(30)
                        .withValidation(
                                VRule.notEmpty(),
                                VRule.maximumLength(50),
                                VRule.matchesRegex(".+")),
                Field.is("status", FieldType.BOOLEAN).withDefaultValue("false"),
                Field.is("priority", FieldType.INTEGER)
                        .setMustBeUnique(true)
                        .withMinMaxValues(1, 5),
                Field.is("metadata", FieldType.OBJECT)
                        .withField(Field.is("source", FieldType.STRING)));

        EntityDefinition project = thingifier.defineThing("project", "projects");
        project.addField(Field.is("title", FieldType.STRING));

        thingifier
                .defineRelationship(project, task, "tasks", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_ONE(), "taskof");

        return thingifier;
    }

    private Thingifier relationshipMetadataModel() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition product = thingifier.defineThing("product", "products");
        product.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));

        EntityDefinition item = thingifier.defineThing("item", "items");
        item.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        item.addField(
                Field.is("productId", FieldType.INTEGER).references(product, "id", "product"));

        thingifier
                .defineRelationship(item, product, "product", Cardinality.ONE_TO_ONE())
                .deleteTargetWhenDisconnected()
                .deleteTargetsWhenSourceDeleted();
        return thingifier;
    }
}
