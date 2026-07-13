package uk.co.compendiumdev.thingifier.core.domain.definitions.relationship;

/** Relationship Optionalities */
public enum Optionality {
    MANDATORY_RELATIONSHIP,
    OPTIONAL_RELATIONSHIP;

    public static Optionality from(final String textRepresentation) {
        if (textRepresentation == null) {
            return OPTIONAL_RELATIONSHIP;
        }

        return textRepresentation.equalsIgnoreCase("M")
                ? MANDATORY_RELATIONSHIP
                : OPTIONAL_RELATIONSHIP;
    }
}
