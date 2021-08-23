package uk.co.compendiumdev.thingifier.core.domain.definitions.relationship;

import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.RelationshipVectorInstance;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality.*;

/**
 * A relationshipVector is the definition of the variant of relationship from one thing to another
 * e.g. a specific a -> b relationship might have a different name from the main relationships
 *
 * task <- estimates /estimate of-> estimate
 *
 * task to estimate would be called 'estimates' and would be 1(o):M i.e. 1 task can have 0 to many estimates
 * estimate to task would be called 'estimate-of' and would be 1:1 an estimate must have 1 task
 *
 *
 */
public class RelationshipVectorDefinition {

    private final String name;
    private final Cardinality cardinality;
    private Optionality optionality;
    private EntityDefinition from;
    private EntityDefinition to;
    private RelationshipDefinition parentRelationship;

    public RelationshipVectorDefinition(EntityDefinition from, String relationShipName, EntityDefinition to, Cardinality cardinality) {
        this.from = from;
        this.name = relationShipName;
        this.to = to;
        this.cardinality = cardinality;
        this.optionality = OPTIONAL_RELATIONSHIP;

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


}
