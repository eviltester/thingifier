package uk.co.compendiumdev.thingifier.domain.instances;

import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.domain.definitions.DefinedFields;
import uk.co.compendiumdev.thingifier.domain.definitions.Field;

import java.util.*;

public class InstanceFields {

    private final DefinedFields objectDefinition;
    private Map<String, String> values = new HashMap<String, String>();

    private Map<String, InstanceFields> objectFields;

    public InstanceFields(final DefinedFields objectDefinition) {
        this.objectDefinition = objectDefinition;
    }

    public void addValue(String fieldName, String value) {
        values.put(fieldName.toLowerCase(), value);
    }

    public String getAssignedValue(String fieldName) {
        return values.get(fieldName.toLowerCase());
    }

    public String getValue(String fieldName) {
        String assignedValue = getAssignedValue(fieldName);
        if (assignedValue == null) {
            // does definition have a default value?
            if (objectDefinition.getField(fieldName).hasDefaultValue()) {
                return objectDefinition.getField(fieldName).getDefaultValue();
            } else {
                // return the field type default value
                String defaultVal = objectDefinition.getField(fieldName).getType().getDefault();
                if (defaultVal != null) {
                    return defaultVal;
                }
            }
        }

        return assignedValue;
    }

    public List<String> getFields() {
        List<String> fields = new ArrayList<String>(values.keySet());
        return fields;
    }

    public String toString() {

        StringBuilder output = new StringBuilder();

        for (Map.Entry<String, String> entry : values.entrySet()) {
            output.append("\n\t\t\t\t" + entry.getKey() + " : " + entry.getValue() + "\n");
        }

        return output.toString();
    }


    public void deleteAllFieldsExcept(List fieldNamesToIgnore) {

        Set<String> ignorekeys = new HashSet<>(fieldNamesToIgnore);
        Set<String> keys = new HashSet(values.keySet());

        for (String key : keys) {
            if (!ignorekeys.contains(key)) {
                values.remove(key);
            }
        }
    }

    public Map<String, String> asMap() {
        HashMap<String, String> aMap = new HashMap<String, String>();
        aMap.putAll(values);
        return aMap;
    }

    public boolean hasFieldNamed(String fieldName) {
        return values.keySet().contains(fieldName);
    }

    public InstanceFields getObjectField(final String objectFieldName) {
        if(objectFields==null){
            objectFields = new HashMap<>();
        }
        return objectFields.get(objectFieldName);
    }

    public void addObjectInstance(final String objectFieldName, final InstanceFields instance) {
        if(objectFields==null){
            objectFields = new HashMap<>();
        }
        objectFields.put(objectFieldName, instance);
    }

    public DefinedFields getDefinition() {
        return objectDefinition;
    }

    public InstanceFields putFieldValue(final String fieldName, final String value) {
        if(objectDefinition.hasFieldNameDefined(fieldName)){
            addValue(fieldName, value);
        }
        return this;
    }
    public InstanceFields setFieldValue(final Field field, final String fieldName, final String value) {
        final ValidationReport validationReport = field.validate(value);
        if (validationReport.isValid()) {
            addValue(
                    fieldName,
                    field.getValueToAdd(value));

        } else {
            throw new IllegalArgumentException(
                    validationReport.getCombinedErrorMessages());
        }

        return this;
    }
}
