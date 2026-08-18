package uk.co.compendiumdev.thingifier.application.schema.definition;

public final class RelationshipDefinitionSpec {

    private final String fromEntityName;
    private final String name;
    private final String toEntityName;
    private final CardinalitySpec cardinality;
    private final String optionality;
    private final boolean deleteTargetWhenDisconnected;
    private final boolean deleteTargetsWhenSourceDeleted;
    private final RelationshipVectorSpec reverse;

    public RelationshipDefinitionSpec(
            final String fromEntityName,
            final String name,
            final String toEntityName,
            final CardinalitySpec cardinality,
            final String optionality,
            final RelationshipVectorSpec reverse) {
        this(fromEntityName, name, toEntityName, cardinality, optionality, false, false, reverse);
    }

    public RelationshipDefinitionSpec(
            final String fromEntityName,
            final String name,
            final String toEntityName,
            final CardinalitySpec cardinality,
            final String optionality,
            final boolean deleteTargetWhenDisconnected,
            final boolean deleteTargetsWhenSourceDeleted,
            final RelationshipVectorSpec reverse) {
        this.fromEntityName = fromEntityName;
        this.name = name;
        this.toEntityName = toEntityName;
        this.cardinality = cardinality;
        this.optionality = optionality;
        this.deleteTargetWhenDisconnected = deleteTargetWhenDisconnected;
        this.deleteTargetsWhenSourceDeleted = deleteTargetsWhenSourceDeleted;
        this.reverse = reverse;
    }

    public String fromEntityName() {
        return fromEntityName;
    }

    public String name() {
        return name;
    }

    public String toEntityName() {
        return toEntityName;
    }

    public CardinalitySpec cardinality() {
        return cardinality;
    }

    public String optionality() {
        return optionality;
    }

    /**
     * Reports whether disconnecting the forward vector deletes the target entity.
     *
     * @return true when the forward vector owns the target on disconnect
     */
    public boolean deleteTargetWhenDisconnected() {
        return deleteTargetWhenDisconnected;
    }

    /**
     * Reports whether deleting the forward source deletes current target entities.
     *
     * @return true when the forward vector cascades source deletes to targets
     */
    public boolean deleteTargetsWhenSourceDeleted() {
        return deleteTargetsWhenSourceDeleted;
    }

    public RelationshipVectorSpec reverse() {
        return reverse;
    }

    public boolean hasReverse() {
        return reverse != null;
    }
}
