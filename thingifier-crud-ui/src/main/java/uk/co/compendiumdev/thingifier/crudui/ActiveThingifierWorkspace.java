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
    private String projectPath;
    private String projectTitle;
    private String projectDescription;
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

    static ActiveThingifierWorkspace forThingifier(final Thingifier thingifier) {
        return new ActiveThingifierWorkspace(thingifier);
    }

    public synchronized WorkspaceSnapshot snapshot() {
        return new WorkspaceSnapshot(
                version,
                thingifier,
                definition,
                schemaYaml,
                projectPath,
                projectTitle,
                projectDescription);
    }

    public synchronized WorkspaceSnapshot replaceWithYaml(final String yamlText) {
        Thingifier newThingifier = yamlLoader.loadThingifier(yamlText);
        Thingifier oldThingifier = thingifier;
        replaceWith(newThingifier);
        clearProject();
        version++;
        if (oldThingifier != null) {
            oldThingifier.close();
        }
        return snapshot();
    }

    public synchronized WorkspaceSnapshot replaceWithYaml(final Path path) throws IOException {
        return replaceWithYaml(Files.readString(path));
    }

    public synchronized WorkspaceSnapshot replaceWithMigratedThingifier(
            final Thingifier newThingifier, final long expectedVersion) {
        if (version != expectedVersion) {
            throw new IllegalStateException(
                    "Workspace changed; refresh schema preview before applying");
        }
        Thingifier oldThingifier = thingifier;
        replaceWith(newThingifier);
        version++;
        if (oldThingifier != null) {
            oldThingifier.close();
        }
        return snapshot();
    }

    synchronized WorkspaceSnapshot replaceWithProjectThingifier(
            final Thingifier newThingifier,
            final Path projectFolder,
            final String title,
            final String description) {
        Thingifier oldThingifier = thingifier;
        replaceWith(newThingifier);
        projectPath = projectFolder.toAbsolutePath().normalize().toString();
        projectTitle = nullToEmpty(title);
        projectDescription = nullToEmpty(description);
        version++;
        if (oldThingifier != null) {
            oldThingifier.close();
        }
        return snapshot();
    }

    synchronized WorkspaceSnapshot markProjectSaved(
            final Path projectFolder, final String title, final String description) {
        projectPath = projectFolder.toAbsolutePath().normalize().toString();
        projectTitle = nullToEmpty(title);
        projectDescription = nullToEmpty(description);
        return snapshot();
    }

    synchronized Thingifier releaseThingifier() {
        Thingifier released = thingifier;
        thingifier = null;
        return released;
    }

    private void replaceWith(final Thingifier newThingifier) {
        thingifier = newThingifier;
        definition = modelExporter.export(newThingifier);
        schemaYaml = yamlExporter.export(definition);
    }

    private void clearProject() {
        projectPath = "";
        projectTitle = "";
        projectDescription = "";
    }

    private String nullToEmpty(final String value) {
        return value == null ? "" : value;
    }

    @Override
    public synchronized void close() {
        if (thingifier != null) {
            thingifier.close();
        }
    }
}
