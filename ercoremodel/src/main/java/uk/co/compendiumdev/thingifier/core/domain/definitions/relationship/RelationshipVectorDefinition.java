package uk.co.compendiumdev.thingifier.core.domain.definitions.relationship;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality.*;

import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

/**
 * A relationshipVector is the definition of the variant of relationship from one thing to another
 * e.g. a specific a -> b relationship might have a different name from the main relationships
 *
 * <p>task <- estimates /estimate of-> estimate
 *
 * <p>task to estimate would be called 'estimates' and would be 1(o):M i.e. 1 task can have 0 to
 * many estimates estimate to task would be called 'estimate-of' and would be 1:1 an estimate must
 * have 1 task
 */
public class RelationshipVectorDefinition {

    private final String name;
    private final Cardinality cardinality;
    private Optionality optionality;
    private EntityDefinition from;
    private EntityDefinition to;
    private RelationshipDefinition parentRelationship;
    private boolean deleteTargetWhenDisconnected;
    private boolean deleteTargetsWhenSourceDeleted;

    public RelationshipVectorDefinition(
            EntityDefinition from,
            String relationShipName,
            EntityDefinition to,
            Cardinality cardinality) {
        this.from = from;
        this.name = relationShipName;
        this.to = to;
        this.cardinality = cardinality;
        this.optionality = OPTIONAL_RELATIONSHIP;
        this.deleteTargetWhenDisconnected = false;
        this.deleteTargetsWhenSourceDeleted = false;

        // assign to the from thingDefinition
        from.related().addRelationship(this);
    }

    public String getName() {
        return name;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public EntityDefinition getTo() {
        return to;
    }

    public EntityDefinition getFrom() {
        return from;
    }

    public RelationshipDefinition getRelationshipDefinition() {
        return parentRelationship;
    }

    // todo: not sure if this should exist, or if we should have 0:M mean 1(optional):M
    public void setOptionality(final Optionality aGivenOptionality) {
        this.optionality = aGivenOptionality;
    }

    public Optionality getOptionality() {
        return optionality;
    }

    public void forRelationship(final RelationshipDefinition relationshipDefinition) {
        this.parentRelationship = relationshipDefinition;
    }

    /**
     * Configures relationship instance deletion to delete the related target entity.
     *
     * <p>This models owned children such as cart items, where removing the relationship from the
     * source should remove the target record rather than merely disconnecting it.
     *
     * @return this vector so relationship configuration can be chained
     */
    public RelationshipVectorDefinition deleteTargetWhenDisconnected() {
        deleteTargetWhenDisconnected = true;
        return this;
    }

    /**
     * Configures source entity deletion to delete all currently related target entities.
     *
     * <p>This is an explicit cascade policy for generated and direct Thingifier deletes. It is
     * independent of mandatory-relationship cleanup, which remains the store's responsibility.
     *
     * @return this vector so relationship configuration can be chained
     */
    public RelationshipVectorDefinition deleteTargetsWhenSourceDeleted() {
        deleteTargetsWhenSourceDeleted = true;
        return this;
    }

    /**
     * Reports whether disconnecting this vector should delete the target entity.
     *
     * @return true when relationship-instance DELETE should also delete the target
     */
    public boolean shouldDeleteTargetWhenDisconnected() {
        return deleteTargetWhenDisconnected;
    }

    /**
     * Reports whether deleting the source should delete related targets for this vector.
     *
     * @return true when source entity DELETE should cascade to current targets
     */
    public boolean shouldDeleteTargetsWhenSourceDeleted() {
        return deleteTargetsWhenSourceDeleted;
    }
}
