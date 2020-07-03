package uk.co.compendiumdev.thingifier.generic.definitions.validation;

public class VRule {

    public static ValidationRule notEmpty() {
        return new NotEmptyValidationRule();
    }

    public static ValidationRule matchesType() {
        return new MatchesTypeValidationRule();
    }


}
