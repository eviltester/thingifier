package uk.co.compendiumdev.thingifier.core.domain.definitions;

// todo: cardinality is not used or enforced yet
//  https://en.wikipedia.org/wiki/Cardinality_(data_modeling)
public enum Cardinality {

    ONE_TO_MANY("1", "*"),
    ONE_TO_ONE("1", "1" );

    private final String left;
    private final String right;

    Cardinality(final String fromAmount, final String toAmount) {
        this.left = fromAmount;
        this.right = toAmount;
    }

    public String right() {
        return this.right;
    }
    public String left() {
        return this.left;
    }
}
