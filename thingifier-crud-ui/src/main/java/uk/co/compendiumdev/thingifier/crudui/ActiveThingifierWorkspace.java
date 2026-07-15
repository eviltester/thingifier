package uk.co.compendiumdev.thingifier.crudui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfile;
import uk.co.compendiumdev.thingifier.application.examples.TodoManagerThingifier;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelDefinition;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelExporter;
import uk.co.compendiumdev.thingifier.yaml.ThingifierYamlExporter;
import uk.co.compendiumdev.thingifier.yaml.ThingifierYamlLoader;

public final class ActiveThingifierWorkspace implements AutoCloseable {

    private final ThingifierModelExporter modelExporter;
    private final ThingifierYamlExporter yamlExporter;
    private final ThingifierYamlLoader yamlLoader;
    private Thingifier thingifier;
    private ThingifierModelDefinition definition;
    private String schemaYaml;
    private long version;

    private ActiveThingifierWorkspace(final Thingifier thingifier) {
        modelExporter = new ThingifierModelExporter();
        yamlExporter = new ThingifierYamlExporter();
        yamlLoader = new ThingifierYamlLoader();
        replaceWith(thingifier);
        version = 1L;
    }

    public static ActiveThingifierWorkspace defaultTodoManagerWorkspace() {
        Thingifier todoManager = new TodoManagerThingifier().get();
        ThingifierApiConfigProfile defaultProfile = todoManager.apiConfigProfiles().getDefault();
        if (defaultProfile != null) {
            todoManager.configureWithProfile(defaultProfile);
        }
        return new ActiveThingifierWorkspace(todoManager);
    }

    public synchronized WorkspaceSnapshot snapshot() {
        return new WorkspaceSnapshot(version, thingifier, definition, schemaYaml);
    }

    public synchronized WorkspaceSnapshot replaceWithYaml(final String yamlText) {
        Thingifier newThingifier = yamlLoader.loadThingifier(yamlText);
        Thingifier oldThingifier = thingifier;
        replaceWith(newThingifier);
        version++;
        if (oldThingifier != null) {
            oldThingifier.close();
        }
        return snapshot();
    }

    public synchronized WorkspaceSnapshot replaceWithYaml(final Path path) throws IOException {
        return replaceWithYaml(Files.readString(path));
    }

    private void replaceWith(final Thingifier newThingifier) {
        thingifier = newThingifier;
        definition = modelExporter.export(newThingifier);
        schemaYaml = yamlExporter.export(definition);
    }

    @Override
    public synchronized void close() {
        if (thingifier != null) {
            thingifier.close();
        }
    }
}
