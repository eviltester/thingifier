package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;

public class FieldValues {
    public static List<NamedValue> fromListMapEntryStringString(
            final List<Map.Entry<String, String>> args) {
        List<NamedValue> fieldValues = new ArrayList<>();

        for (Map.Entry<String, String> potentialField : args) {
            fieldValues.add(new NamedValue(potentialField.getKey(), potentialField.getValue()));
        }

        return fieldValues;
    }
}
