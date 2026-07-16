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
    void sqliteFileBackedManifestRoundTripsThroughYamlWithDataFile() {
        ThingifierProjectManifest manifest =
                ThingifierProjectManifest.sqliteFileBackedFor(
                        "SQLite Project", "Project data lives in a DB file");
        ThingifierProjectManifestYaml yaml = new ThingifierProjectManifestYaml();

        String text = yaml.export(manifest);
        ThingifierProjectManifest loaded = yaml.load(text);

        Assertions.assertTrue(text.contains("dataFile: data.sqlite"));
        Assertions.assertFalse(text.contains("storage:"));
        Assertions.assertEquals("sqlite-file", loaded.storageMode());
        Assertions.assertEquals("data.sqlite", loaded.sqliteFile());
        Assertions.assertEquals("data.sqlite", loaded.dataFile());
    }

    @Test
    void sqliteDataFileWithoutStorageBlockIsDetectedAsSqliteFileStorage() {
        ThingifierProjectManifest loaded =
                new ThingifierProjectManifestYaml()
                        .load(
                                "formatVersion: 1\n"
                                        + "schemaFile: schema.yaml\n"
                                        + "dataFile: todomanager.sqlite\n");

        Assertions.assertEquals("sqlite-file", loaded.storageMode());
        Assertions.assertEquals("todomanager.sqlite", loaded.sqliteFile());
        Assertions.assertEquals("todomanager.sqlite", loaded.dataFile());
    }

    @Test
    void sqliteFileBackedManifestRejectsEscapingSqliteFile() {
        ThingifierProjectManifestYaml yaml = new ThingifierProjectManifestYaml();

        Assertions.assertThrows(
                ThingifierYamlException.class,
                () ->
                        yaml.load(
                                "formatVersion: 1\n"
                                        + "schemaFile: schema.yaml\n"
                                        + "storage:\n"
                                        + "  mode: sqlite-file\n"
                                        + "  sqliteFile: ../data.sqlite\n"));
        Assertions.assertThrows(
                ThingifierYamlException.class,
                () ->
                        yaml.load(
                                "formatVersion: 1\n"
                                        + "schemaFile: schema.yaml\n"
                                        + "storage:\n"
                                        + "  mode: sqlite-file\n"
                                        + "  sqliteFile: C:\\\\data.sqlite\n"));
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
