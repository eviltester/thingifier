package uk.co.compendiumdev.thingifier.application.command;

public final class DeleteThingCommand implements ThingWriteCommand {

    private final String entityName;
    private final String identifier;

    public DeleteThingCommand(final String entityName, final String identifier) {
        this.entityName = entityName;
        this.identifier = identifier;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getIdentifier() {
        return identifier;
    }
}
