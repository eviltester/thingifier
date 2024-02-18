package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.ArrayList;
import java.util.List;

public class RelationshipCreator {
    private final Thingifier thingifier;

    public RelationshipCreator(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    public ApiResponse createRelationships(final BodyParser bodyargs, final EntityInstance instance, final String database) {
        try {
            List<RelationshipDetails> relationships = getRelationshipsFromArgs(bodyargs, instance, database);
            for (RelationshipDetails relationship : relationships) {
                instance.getRelationships().connect(
                        relationship.relationshipName,
                            thingifier.getInstancesForSingularOrPluralNamedEntity(relationship.toType, database).
                                    findInstanceByFieldNameAndValue(relationship.guidName, relationship.guidValue));
            }

            return ApiResponse.created(instance, thingifier.apiConfig());

        }catch(Exception e){
            return ApiResponse.error(400, "Error creating relationships " + e.getMessage());
        }
    }

    private List<RelationshipDetails> getRelationshipsFromArgs(final BodyParser bodyargs, final EntityInstance instance, final String database) {

        List<RelationshipDetails>relationships = new ArrayList<>();
        RelationshipCollector collector = new RelationshipCollector();

        new BodyArgsProcessor(thingifier, bodyargs).identifyRelationships(
                bodyargs.getFlattenedStringMap(), instance, collector, database
        );

        relationships.addAll(collector.getRelationshipDetails());

        return relationships;

    }

}
