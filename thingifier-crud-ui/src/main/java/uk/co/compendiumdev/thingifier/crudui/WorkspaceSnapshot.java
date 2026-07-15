package uk.co.compendiumdev.thingifier.crudui;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelDefinition;

public final class WorkspaceSnapshot {

    private final long version;
    private final Thingifier thingifier;
    private final ThingifierModelDefinition definition;
    private final String schemaYaml;

    public WorkspaceSnapshot(
            final long version,
            final Thingifier thingifier,
            final ThingifierModelDefinition definition,
            final String schemaYaml) {
        this.version = version;
        this.thingifier = thingifier;
        this.definition = definition;
        this.schemaYaml = schemaYaml;
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
}
