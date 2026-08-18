package uk.co.compendiumdev.thingifier.application.schema.definition;

public final class RelationshipVectorSpec {

    private final String name;
    private final CardinalitySpec cardinality;
    private final String optionality;
    private final boolean deleteTargetWhenDisconnected;
    private final boolean deleteTargetsWhenSourceDeleted;

    public RelationshipVectorSpec(
            final String name, final CardinalitySpec cardinality, final String optionality) {
        this(name, cardinality, optionality, false, false);
    }

    public RelationshipVectorSpec(
            final String name,
            final CardinalitySpec cardinality,
            final String optionality,
            final boolean deleteTargetWhenDisconnected,
            final boolean deleteTargetsWhenSourceDeleted) {
        this.name = name;
        this.cardinality = cardinality;
        this.optionality = optionality;
        this.deleteTargetWhenDisconnected = deleteTargetWhenDisconnected;
        this.deleteTargetsWhenSourceDeleted = deleteTargetsWhenSourceDeleted;
    }

    public String name() {
        return name;
    }

    public CardinalitySpec cardinality() {
        return cardinality;
    }

    public String optionality() {
        return optionality;
    }

    /**
     * Reports whether disconnecting this vector deletes the target entity.
     *
     * @return true when this vector owns the target on disconnect
     */
    public boolean deleteTargetWhenDisconnected() {
        return deleteTargetWhenDisconnected;
    }

    /**
     * Reports whether deleting this vector's source deletes current target entities.
     *
     * @return true when this vector cascades source deletes to targets
     */
    public boolean deleteTargetsWhenSourceDeleted() {
        return deleteTargetsWhenSourceDeleted;
    }
}
