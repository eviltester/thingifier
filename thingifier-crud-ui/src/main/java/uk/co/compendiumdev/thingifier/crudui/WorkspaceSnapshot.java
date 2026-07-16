package uk.co.compendiumdev.thingifier.crudui;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelDefinition;

public final class WorkspaceSnapshot {

    private final long version;
    private final Thingifier thingifier;
    private final ThingifierModelDefinition definition;
    private final String schemaYaml;
    private final WorkspaceStorage storage;
    private final String projectPath;
    private final String projectTitle;
    private final String projectDescription;

    public WorkspaceSnapshot(
            final long version,
            final Thingifier thingifier,
            final ThingifierModelDefinition definition,
            final String schemaYaml) {
        this(version, thingifier, definition, schemaYaml, WorkspaceStorage.memory(), "", "", "");
    }

    public WorkspaceSnapshot(
            final long version,
            final Thingifier thingifier,
            final ThingifierModelDefinition definition,
            final String schemaYaml,
            final WorkspaceStorage storage,
            final String projectPath,
            final String projectTitle,
            final String projectDescription) {
        this.version = version;
        this.thingifier = thingifier;
        this.definition = definition;
        this.schemaYaml = schemaYaml;
        this.storage = storage == null ? WorkspaceStorage.memory() : storage;
        this.projectPath = nullToEmpty(projectPath);
        this.projectTitle = nullToEmpty(projectTitle);
        this.projectDescription = nullToEmpty(projectDescription);
    }

    public long version() {
        return version;
    }

    public Thingifier thingifier() {
        return thingifier;
    }

    public ThingifierModelDefinition definition() {
        return definition;
    }

    public String schemaYaml() {
        return schemaYaml;
    }

    public WorkspaceStorage storage() {
        return storage;
    }

    public String projectPath() {
        return projectPath;
    }

    public String projectTitle() {
        return projectTitle;
    }

    public String projectDescription() {
        return projectDescription;
    }

    public boolean hasProjectPath() {
        return !projectPath.isEmpty();
    }

    private String nullToEmpty(final String value) {
        return value == null ? "" : value;
    }
}
