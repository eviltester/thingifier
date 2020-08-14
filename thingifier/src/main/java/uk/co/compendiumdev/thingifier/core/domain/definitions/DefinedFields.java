package uk.co.compendiumdev.thingifier.core.domain.definitions;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefinedFields {

    private Map<String, Field> fields = new ConcurrentHashMap<>();
    // to control field order
    private List<String> orderedFieldNames = new ArrayList<>();

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();

        output.append("\t\tFields:\n");

        for (Field aField : fields.values()) {

            output.append("\t\t\t" + aField.getName() + "\n");
        }
        return output.toString();
    }

    public void addField(final Field aField) {
        fields.put(aField.getName().toLowerCase(), aField);
        orderedFieldNames.add(aField.getName());
    }

    public List<String> getFieldNames() {
        ArrayList<String> fieldNames = new ArrayList<>();
        fieldNames.addAll(orderedFieldNames);
        return fieldNames;
    }

    public boolean hasFieldNameDefined(final String fieldName) {
        return fields.keySet().contains(fieldName.toLowerCase());
    }

    public DefinedFields addFields(final Field... theseFields) {
        for (Field aField : theseFields) {
            addField(aField);
        }
        return this;
    }

    public Field getField(final String fieldName) {
        if (hasFieldNameDefined(fieldName)) {
            return fields.get(fieldName.toLowerCase());
        }
        return null;
    }

    public List<Field> getFieldsOfType(final FieldType... types) {
        final List<FieldType> typeCheck = Arrays.asList(types);
        List<Field> returnFields = new ArrayList<>();

        for(Map.Entry<String,Field> field : fields.entrySet()){
            Field aField = field.getValue();
            if(typeCheck.contains(aField.getType()))
                returnFields.add(aField);
        }

        return returnFields;
    }

    public List<String> getFieldNamesOfType(final FieldType... types) {

        List<String> fieldNames = new ArrayList<>();
        List<Field> typedFields = getFieldsOfType(types);

        for(Field field : typedFields){
            fieldNames.add(field.getName());
        }

        return fieldNames;
    }
}
