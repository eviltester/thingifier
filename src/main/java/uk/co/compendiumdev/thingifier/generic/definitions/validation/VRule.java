package uk.co.compendiumdev.thingifier.generic.definitions.validation;

import uk.co.compendiumdev.thingifier.generic.definitions.validation.ValidationRule;

public class VRule{
    public static ValidationRule NotEmpty() {
        return new NotEmptyValidationRule();
    }

    public static ValidationRule MatchesType() {
        return new MatchesTypeValidationRule();
    }


}
