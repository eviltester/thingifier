package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FieldValues {
    public static List<FieldValue> fromListMapEntryStringString(
            final List<Map.Entry<String, String>> args) {
        List<FieldValue> fieldValues = new ArrayList<>();

        for(Map.Entry<String, String> potentialField : args){
            fieldValues.add(FieldValue.is(potentialField.getKey(), potentialField.getValue()));
        }

        return fieldValues;
    }
}
