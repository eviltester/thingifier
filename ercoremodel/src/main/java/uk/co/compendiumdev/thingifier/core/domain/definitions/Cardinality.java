package uk.co.compendiumdev.thingifier.core.domain.definitions;

// todo: cardinality is not used or enforced yet
//  https://en.wikipedia.org/wiki/Cardinality_(data_modeling)
public class Cardinality {

    public static Cardinality ONE_TO_MANY(){
        return new Cardinality("1","*");
    }
    public static Cardinality ONE_TO_ONE() {
        return new Cardinality("1", "1");
    }
    public static Cardinality ZERO_TO_ONE() {
        return new Cardinality("0", "1");
    }
    public static Cardinality ZERO_TO_MANY() {
        return new Cardinality("0", "*");
    }

    private final String left;
    private final String right;

    public Cardinality(final String fromAmount, final String toAmount) {
        this.left = fromAmount;
        this.right = toAmount;
    }

    public Cardinality(final int fromAmount, final int toAmount) {
        this.left = String.valueOf(fromAmount);
        this.right = String.valueOf(toAmount);
    }

    public String right() {
        return right;
    }
    public String left() {
        return left;
    }

    public boolean hasMaximumLimit(){
        return !right.equals("*");
    }

    public int maximumLimit() {
        return Integer.valueOf(right);
    }
}
