package uk.co.compendiumdev.thingifier.domain.definitions;

// todo: cardinality is not used or enforced yet
//  https://en.wikipedia.org/wiki/Cardinality_(data_modeling)
public class Cardinality {
    private final String left;
    private final String right;

    public Cardinality(final String fromAmount, final String toAmount) {
        this.left = fromAmount;
        this.right = toAmount;
    }

    public String right() {
        return this.right;
    }
}
