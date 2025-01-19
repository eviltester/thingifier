package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

import java.util.List;

public class TypeValidationFailedMessageGenerator {

    public static String thisValueDoesNotMatchType(final FieldValue value, FieldType type) {
        return TypeValidationFailedMessageGenerator.thisValueDoesNotMatchType(value, type, List.of());
    }

    public static String thisValueDoesNotMatchType( FieldValue value, FieldType type,
                                                 final List<String> validValues) {

        String reportValids = "";

        if(validValues!=null && !validValues.isEmpty()){
            reportValids = " - valid values are [%s]".formatted(String.join(",",validValues));
        }

        return String.format( "%s : %s does not match type %s%s",
                        value.getName(),  value.asString(), type, reportValids);
    }
}
