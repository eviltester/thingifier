package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public class RelationshipCreator {
    private final Thingifier thingifier;

    public RelationshipCreator(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    public ApiResponse createRelationships(
            final BodyParser bodyargs, final EntityInstance instance, final String database) {
        try {
            List<RelationshipDetails> relationships =
                    getRelationshipsFromArgs(bodyargs, instance, database);
            for (RelationshipDetails relationship : relationships) {
                EntityDefinition relatedEntity =
                        thingifier
                                .getERmodel()
                                .getSchema()
                                .getDefinitionWithSingularOrPluralNamed(relationship.toType);
                thingifier
                        .getRepository(database)
                        .connectRelationship(
                                instance,
                                relationship.relationshipName,
                                thingifier
                                        .getRepository(database)
                                        .findInstanceByFieldNameAndValue(
                                                relatedEntity,
                                                relationship.guidName,
                                                relationship.guidValue));
            }

            return ApiResponse.created(instance, thingifier.apiConfig());

        } catch (Exception e) {
            return ApiResponse.error(400, "Error creating relationships " + e.getMessage());
        }
    }

    private List<RelationshipDetails> getRelationshipsFromArgs(
            final BodyParser bodyargs, final EntityInstance instance, final String database) {

        List<RelationshipDetails> relationships = new ArrayList<>();
        RelationshipCollector collector = new RelationshipCollector();

        new BodyArgsProcessor(thingifier, bodyargs)
                .identifyRelationships(
                        bodyargs.getFlattenedStringMap(), instance, collector, database);

        relationships.addAll(collector.getRelationshipDetails());

        return relationships;
    }
}
