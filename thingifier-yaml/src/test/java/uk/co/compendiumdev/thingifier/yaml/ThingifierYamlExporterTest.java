package uk.co.compendiumdev.thingifier.yaml;

import java.io.InputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelDefinition;

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
    void exporterCanEmitYamlFromRuntimeThingifier() {
        Thingifier thingifier =
                new ThingifierYamlLoader().loadThingifier(resource("field-types.yaml"));

        String yaml = new ThingifierYamlExporter().export(thingifier);

        Assertions.assertTrue(yaml.contains("auto-guid"));
        Assertions.assertTrue(yaml.contains("auto-increment"));
        Assertions.assertTrue(yaml.contains("metadata:"));
        Assertions.assertTrue(yaml.contains("source:"));
    }

    private InputStream resource(final String resourceName) {
        InputStream stream = getClass().getResourceAsStream("/models/" + resourceName);
        Assertions.assertNotNull(stream, resourceName);
        return stream;
    }
}
