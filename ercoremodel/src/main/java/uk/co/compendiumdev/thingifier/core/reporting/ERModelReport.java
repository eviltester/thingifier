package uk.co.compendiumdev.thingifier.core.reporting;

import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;

public class ERModelReport {
    private final EntityRelModel erModel;

    public ERModelReport(final EntityRelModel erModel) {
        this.erModel = erModel;
    }

    public String asMarkdown() {
        StringBuilder output = new StringBuilder();

        output.append(schemaAsMarkdown(erModel.getSchema()));

        output.append("\n# Instances\n");

        for (EntityInstanceCollection instances : erModel.getInstanceData().getAllInstanceCollections()) {

            output.append("## Of " + instances.definition().getName() + "\n");

            for (EntityInstance anInstance : instances.getInstances()) {
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
