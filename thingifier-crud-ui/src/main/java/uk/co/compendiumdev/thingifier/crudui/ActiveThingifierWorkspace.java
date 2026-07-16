package uk.co.compendiumdev.thingifier.crudui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfile;
import uk.co.compendiumdev.thingifier.application.examples.TodoManagerThingifier;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelAssembler;
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
    private WorkspaceStorage storage;
    private String projectPath;
    private String projectTitle;
    private String projectDescription;
    private long version;

    private ActiveThingifierWorkspace(final Thingifier thingifier) {
        this(thingifier, WorkspaceStorage.memory());
    }

    private ActiveThingifierWorkspace(final Thingifier thingifier, final WorkspaceStorage storage) {
        modelExporter = new ThingifierModelExporter();
        yamlExporter = new ThingifierYamlExporter();
        yamlLoader = new ThingifierYamlLoader();
        this.storage = storage;
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

    public static ActiveThingifierWorkspace defaultTodoManagerWorkspace(
            final WorkspaceStorage storage) {
        ActiveThingifierWorkspace workspace = defaultTodoManagerWorkspace();
        if (!WorkspaceStorage.MODE_MEMORY.equals(storage.mode())) {
            workspace.switchStorage(storage);
        }
        return workspace;
    }

    static ActiveThingifierWorkspace forThingifier(final Thingifier thingifier) {
        return new ActiveThingifierWorkspace(thingifier);
    }

    static ActiveThingifierWorkspace forThingifier(
            final Thingifier thingifier, final WorkspaceStorage storage) {
        return new ActiveThingifierWorkspace(thingifier, storage);
    }

    public synchronized WorkspaceSnapshot snapshot() {
        return new WorkspaceSnapshot(
                version,
                thingifier,
                definition,
                schemaYaml,
                storage,
                projectPath,
                projectTitle,
                projectDescription);
    }

    public synchronized WorkspaceSnapshot replaceWithYaml(final String yamlText) {
        Thingifier newThingifier = yamlLoader.loadThingifier(yamlText, storage.provider());
        newThingifier.clearAllData();
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
        return replaceWithMigratedThingifier(newThingifier, storage, expectedVersion);
    }

    public synchronized WorkspaceSnapshot replaceWithMigratedThingifier(
            final Thingifier newThingifier,
            final WorkspaceStorage newStorage,
            final long expectedVersion) {
        if (version != expectedVersion) {
            throw new IllegalStateException(
                    "Workspace changed; refresh schema preview before applying");
        }
        Thingifier oldThingifier = thingifier;
        storage = newStorage;
        replaceWith(newThingifier);
        version++;
        if (oldThingifier != null) {
            oldThingifier.close();
        }
        return snapshot();
    }

    synchronized WorkspaceSnapshot replaceWithProjectThingifier(
            final Thingifier newThingifier,
            final WorkspaceStorage newStorage,
            final Path projectFolder,
            final String title,
            final String description) {
        return replaceWithProjectThingifier(
                newThingifier,
                newStorage,
                projectFolder.toAbsolutePath().normalize().toString(),
                title,
                description);
    }

    synchronized WorkspaceSnapshot replaceWithProjectThingifier(
            final Thingifier newThingifier,
            final WorkspaceStorage newStorage,
            final String projectPathDisplay,
            final String title,
            final String description) {
        Thingifier oldThingifier = thingifier;
        storage = newStorage;
        replaceWith(newThingifier);
        projectPath = nullToEmpty(projectPathDisplay);
        projectTitle = nullToEmpty(title);
        projectDescription = nullToEmpty(description);
        version++;
        if (oldThingifier != null) {
            oldThingifier.close();
        }
        return snapshot();
    }

    public synchronized WorkspaceSnapshot switchStorage(final WorkspaceStorage newStorage) {
        if (sameStorage(newStorage)) {
            return snapshot();
        }

        final String dataJson =
                new WorkspaceDataExporter(this, new DynamicThingifierApiProxy(this))
                        .exportProjectDataJson();
        ActiveThingifierWorkspace staging = null;
        try {
            Thingifier stagedThingifier =
                    new ThingifierModelAssembler().assemble(definition, newStorage.provider());
            stagedThingifier.clearAllData();
            staging = ActiveThingifierWorkspace.forThingifier(stagedThingifier, newStorage);
            new WorkspaceDataImporter(
                            staging,
                            new DynamicThingifierApiProxy(staging),
                            new WorkspaceMetadataJson())
                    .importDataIntoCurrentWorkspace(dataJson);
            Thingifier released = staging.releaseThingifier();
            Thingifier oldThingifier = thingifier;
            storage = newStorage;
            replaceWith(released);
            version++;
            if (oldThingifier != null) {
                oldThingifier.close();
            }
            return snapshot();
        } finally {
            if (staging != null) {
                staging.close();
            }
        }
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

    private boolean sameStorage(final WorkspaceStorage other) {
        return storage.mode().equals(other.mode())
                && storage.sqliteFilePath().equals(other.sqliteFilePath());
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
