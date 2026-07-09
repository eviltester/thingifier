package uk.co.compendiumdev.thingifier.core.reporting;

import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public class ERModelReport {
    private final ERSchema schema;
    private final ThingRepository repository;

    public ERModelReport(final EntityRelModel erModel) {
        this(erModel.getSchema(), erModel.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME));
    }

    public ERModelReport(final ERSchema schema, final ThingRepository repository) {
        this.schema = schema;
        this.repository = repository;
    }

    public String asMarkdown() {
        StringBuilder output = new StringBuilder();

        output.append(schemaAsMarkdown(schema));

        output.append("\n# Instances\n");

        for (EntityDefinition entity : schema.getEntityDefinitions()) {

            output.append("## Of " + entity.getName() + "\n");

            for (EntityInstance anInstance : repository.listInstances(entity)) {
                output.append(anInstance);
            }
        }

        return output.toString();
    }

    private String schemaAsMarkdown(final ERSchema schema) {

        StringBuilder output = new StringBuilder();

        output.append("\n# Entity Definitions:\n");

        for (EntityDefinition entityDefn : schema.getEntityDefinitions()) {
            output.append(entityDefn);
        }

        output.append("\n# Relationship Definitions\n");

        for (RelationshipDefinition aRelationship : schema.getRelationships()) {
            output.append(aRelationship);
        }

        return output.toString();
    }
}
