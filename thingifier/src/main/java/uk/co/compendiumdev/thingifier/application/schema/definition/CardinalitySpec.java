package uk.co.compendiumdev.thingifier.application.schema.definition;

public final class CardinalitySpec {

    private final String left;
    private final String right;

    public CardinalitySpec(final String left, final String right) {
        this.left = left;
        this.right = right;
    }

    public static CardinalitySpec oneToMany() {
        return new CardinalitySpec("1", "*");
    }

    public static CardinalitySpec oneToOne() {
        return new CardinalitySpec("1", "1");
    }

    public static CardinalitySpec zeroToOne() {
        return new CardinalitySpec("0", "1");
    }

    public static CardinalitySpec zeroToMany() {
        return new CardinalitySpec("0", "*");
    }

    public static CardinalitySpec fromText(final String text) {
        if (text == null || text.trim().isEmpty()) {
            return oneToMany();
        }

        final String normalized = text.trim().toLowerCase();
        switch (normalized) {
            case "one-to-many":
                return oneToMany();
            case "one-to-one":
                return oneToOne();
            case "zero-to-one":
                return zeroToOne();
            case "zero-to-many":
                return zeroToMany();
            default:
                if (normalized.contains(":")) {
                    final String[] parts = normalized.split(":", -1);
                    if (parts.length == 2 && isValidAmount(parts[0]) && isValidAmount(parts[1])) {
                        return new CardinalitySpec(parts[0], parts[1]);
                    }
                }
                throw new IllegalArgumentException("Unsupported cardinality " + text);
        }
    }

    public String left() {
        return left;
    }

    public String right() {
        return right;
    }

    public String canonicalName() {
        if ("1".equals(left) && "*".equals(right)) {
            return "one-to-many";
        }
        if ("1".equals(left) && "1".equals(right)) {
            return "one-to-one";
        }
        if ("0".equals(left) && "1".equals(right)) {
            return "zero-to-one";
        }
        if ("0".equals(left) && "*".equals(right)) {
            return "zero-to-many";
        }
        return left + ":" + right;
    }

    private static boolean isValidAmount(final String amount) {
        if ("*".equals(amount)) {
            return true;
        }
        try {
            Integer.parseInt(amount);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
