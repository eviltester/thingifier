package uk.co.compendiumdev.thingifier.yaml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ThingifierProjectManifestYamlTest {

    @Test
    void validManifestRoundTripsThroughYaml() {
        ThingifierProjectManifest manifest =
                new ThingifierProjectManifest(
                        1, "Project Tasks", "A saved CRUD UI project", "schema.yaml", "data.json");
        ThingifierProjectManifestYaml yaml = new ThingifierProjectManifestYaml();

        ThingifierProjectManifest loaded = yaml.load(yaml.export(manifest));

        Assertions.assertEquals(1, loaded.formatVersion());
        Assertions.assertEquals("Project Tasks", loaded.title());
        Assertions.assertEquals("A saved CRUD UI project", loaded.description());
        Assertions.assertEquals("schema.yaml", loaded.schemaFile());
        Assertions.assertEquals("data.json", loaded.dataFile());
    }

    @Test
    void missingSchemaOrDataFileIsRejected() {
        ThingifierProjectManifestYaml yaml = new ThingifierProjectManifestYaml();

        Assertions.assertThrows(
                ThingifierYamlException.class,
                () -> yaml.load("formatVersion: 1\nschemaFile: schema.yaml\n"));
        Assertions.assertThrows(
                ThingifierYamlException.class,
                () -> yaml.load("formatVersion: 1\ndataFile: data.json\n"));
    }

    @Test
    void absoluteAndEscapingPathsAreRejected() {
        ThingifierProjectManifestYaml yaml = new ThingifierProjectManifestYaml();

        Assertions.assertThrows(
                ThingifierYamlException.class,
                () ->
                        yaml.load(
                                "formatVersion: 1\n"
                                        + "schemaFile: /tmp/schema.yaml\n"
                                        + "dataFile: data.json\n"));
        Assertions.assertThrows(
                ThingifierYamlException.class,
                () ->
                        yaml.load(
                                "formatVersion: 1\n"
                                        + "schemaFile: ..\\schema.yaml\n"
                                        + "dataFile: data.json\n"));
        Assertions.assertThrows(
                ThingifierYamlException.class,
                () ->
                        yaml.load(
                                "formatVersion: 1\n"
                                        + "schemaFile: schema.yaml\n"
                                        + "dataFile: ../data.json\n"));
    }

    @Test
    void unknownFutureKeysAreTolerated() {
        ThingifierProjectManifest loaded =
                new ThingifierProjectManifestYaml()
                        .load(
                                "formatVersion: 1\n"
                                        + "project:\n"
                                        + "  title: Future Project\n"
                                        + "schemaFile: schema.yaml\n"
                                        + "dataFile: data.json\n"
                                        + "validators:\n"
                                        + "  - validators.jar\n");

        Assertions.assertEquals("Future Project", loaded.title());
        Assertions.assertEquals("schema.yaml", loaded.schemaFile());
        Assertions.assertEquals("data.json", loaded.dataFile());
    }
}
