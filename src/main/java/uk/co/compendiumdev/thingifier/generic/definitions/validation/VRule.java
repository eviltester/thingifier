package uk.co.compendiumdev.thingifier.generic.definitions.validation;

import uk.co.compendiumdev.thingifier.generic.definitions.validation.ValidationRule;

public class VRule{

    public static ValidationRule notEmpty() {
        return new NotEmptyValidationRule();
    }

    public static ValidationRule matchesType() {
        return new MatchesTypeValidationRule();
    }


}
