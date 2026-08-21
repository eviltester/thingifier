package uk.co.compendiumdev.thingifier.yaml;

import java.io.InputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldRelationshipReference;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

class ThingifierYamlExporterTest {

    @Test
    void exporterEmitsCanonicalYamlForDefinition() {
        ThingifierModelDefinition definition =
                new ThingifierYamlLoader().loadDefinition(resource("minimal-todo.yaml"));

        String yaml = new ThingifierYamlExporter().export(definition);

        Assertions.assertTrue(yaml.contains("formatVersion: 1"));
        Assertions.assertTrue(yaml.contains("entities:"));
        Assertions.assertTrue(yaml.contains("todo:"));
        Assertions.assertTrue(
                new ThingifierYamlLoader().loadDefinition(yaml).relationships().isEmpty());
    }

    @Test
    void exportedYamlCanBeLoadedAgain() {
        Thingifier first = new ThingifierYamlLoader().loadThingifier(resource("validations.yaml"));

        String yaml = new ThingifierYamlExporter().export(first);
        Thingifier second = new ThingifierYamlLoader().loadThingifier(yaml);

        Assertions.assertEquals(first.getThingNames().size(), second.getThingNames().size());
        Assertions.assertEquals(
                first.getDefinitionNamed("item").getFieldNames(),
                second.getDefinitionNamed("item").getFieldNames());
        Assertions.assertTrue(second.getDefinitionNamed("item").getField("title").isMandatory());
        Assertions.assertTrue(second.getDefinitionNamed("item").getField("title").mustBeUnique());
    }

    @Test
    void loadExportLoadRoundTripPreservesRelationshipSemantics() {
        Thingifier first =
                new ThingifierYamlLoader().loadThingifier(resource("relationships-two-way.yaml"));

        String yaml = new ThingifierYamlExporter().export(first);
        Thingifier second = new ThingifierYamlLoader().loadThingifier(yaml);

        Assertions.assertTrue(second.hasRelationshipNamed("tasks"));
        Assertions.assertTrue(second.hasRelationshipNamed("taskof"));
        Assertions.assertEquals(
                first.getRelationshipDefinitions().size(),
                second.getRelationshipDefinitions().size());
    }

    @Test
    void loadExportLoadRoundTripPreservesRelationshipOwnershipPolicies() {
        Thingifier first = new ThingifierYamlLoader().loadThingifier(relationshipMetadataYaml());

        String yaml = new ThingifierYamlExporter().export(first);
        Thingifier second = new ThingifierYamlLoader().loadThingifier(yaml);

        RelationshipVectorDefinition relationship =
                second.getDefinitionNamed("item")
                        .getNamedRelationshipTo("product", second.getDefinitionNamed("product"));
        Assertions.assertTrue(relationship.shouldDeleteTargetWhenDisconnected());
        Assertions.assertTrue(relationship.shouldDeleteTargetsWhenSourceDeleted());
    }

    @Test
    void loadExportLoadRoundTripPreservesFieldRelationshipReferences() {
        Thingifier first = new ThingifierYamlLoader().loadThingifier(relationshipMetadataYaml());

        String yaml = new ThingifierYamlExporter().export(first);
        Thingifier second = new ThingifierYamlLoader().loadThingifier(yaml);

        FieldRelationshipReference reference =
                second.getDefinitionNamed("item").getField("productId").relationshipReference();
        Assertions.assertEquals("product", reference.targetEntity().getName());
        Assertions.assertEquals("id", reference.targetFieldName());
        Assertions.assertEquals("product", reference.relationshipName());
    }

    @Test
    void exporterCanEmitYamlFromRuntimeThingifier() {
        Thingifier thingifier =
                new ThingifierYamlLoader().loadThingifier(resource("field-types.yaml"));

        String yaml = new ThingifierYamlExporter().export(thingifier);

        Assertions.assertTrue(yaml.contains("auto-guid"));
        Assertions.assertTrue(yaml.contains("auto-increment"));
        Assertions.assertTrue(yaml.contains("metadata:"));
        Assertions.assertTrue(yaml.contains("source:"));
    }

    @Test
    void exporterOmitsCodeOnlyCustomValidators() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition item = thingifier.defineThing("item", "items");
        item.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        item.addField(
                Field.is("title", FieldType.STRING)
                        .withCustomValidation(value -> invalidReport("not exportable")));
        item.withInstanceValidation(context -> invalidReport("not exportable"));
        item.withDomainValidation(context -> invalidReport("not exportable"));
        thingifier.withGlobalValidation(context -> invalidReport("not exportable"));

        String yaml = new ThingifierYamlExporter().export(thingifier);
        Thingifier loaded = new ThingifierYamlLoader().loadThingifier(yaml);

        Assertions.assertFalse(yaml.contains("not exportable"));
        Assertions.assertFalse(yaml.contains("custom"));
        Assertions.assertTrue(
                loaded.getDefinitionNamed("item").getField("title").customValidators().isEmpty());
        Assertions.assertTrue(loaded.getDefinitionNamed("item").instanceValidators().isEmpty());
        Assertions.assertTrue(loaded.getDefinitionNamed("item").domainValidators().isEmpty());
        Assertions.assertTrue(loaded.getERmodel().getSchema().globalValidators().isEmpty());
    }

    private InputStream resource(final String resourceName) {
        InputStream stream = getClass().getResourceAsStream("/models/" + resourceName);
        Assertions.assertNotNull(stream, resourceName);
        return stream;
    }

    private String relationshipMetadataYaml() {
        return "formatVersion: 1\n"
                + "model:\n"
                + "  title: Relationships\n"
                + "  description: Relationship metadata.\n"
                + "entities:\n"
                + "  product:\n"
                + "    plural: products\n"
                + "    primaryKey: id\n"
                + "    fields:\n"
                + "      id:\n"
                + "        type: auto-increment\n"
                + "  item:\n"
                + "    plural: items\n"
                + "    primaryKey: id\n"
                + "    fields:\n"
                + "      id:\n"
                + "        type: auto-increment\n"
                + "      productId:\n"
                + "        type: integer\n"
                + "        reference:\n"
                + "          entity: product\n"
                + "          field: id\n"
                + "          relationship: product\n"
                + "relationships:\n"
                + "  - from: item\n"
                + "    name: product\n"
                + "    to: product\n"
                + "    cardinality: one-to-one\n"
                + "    optionality: optional\n"
                + "    deleteTargetWhenDisconnected: true\n"
                + "    deleteTargetsWhenSourceDeleted: true\n";
    }

    private ValidationReport invalidReport(final String message) {
        return new ValidationReport().setValid(false).addErrorMessage(message);
    }
}
