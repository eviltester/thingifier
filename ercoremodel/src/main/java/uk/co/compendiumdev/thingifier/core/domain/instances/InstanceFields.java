package uk.co.compendiumdev.thingifier.core.domain.instances;

import java.util.*;
import uk.co.compendiumdev.thingifier.core.domain.definitions.DefinedFields;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

/*
   InstanceFields is a sparse list, i.e. it might not have a value
   for a specific field, in which case the default from the DefinedFields
   will be used instead.

*/
public class InstanceFields {

    private final DefinedFields objectDefinition;
    private final Map<String, FieldValue> values = new HashMap<>();
    private final AutoIncrement defaultAuto;

    public InstanceFields(final DefinedFields objectDefinition) {
        this.objectDefinition = objectDefinition;
        // todo: there should be no auto increment here
        defaultAuto = new AutoIncrement("default", 1);
    }

    public void addValue(final FieldValue value) {
        values.put(value.getName().toLowerCase(), value);
    }

    public FieldValue getAssignedValue(String fieldName) {
        return values.get(fieldName.toLowerCase());
    }

    public FieldValue getFieldValue(String fieldName) {

        // todo : support complex fieldNames e.g. person.firstname

        if (!objectDefinition.hasFieldNameDefined(fieldName)) {
            reportCannotFindFieldError(fieldName);
        }

        // bypass default processing for OBJECT, ARRAY - at the moment
        // todo: allow defaults for OBJECT, ARRAY, etc.
        Field field = objectDefinition.getField(fieldName);
        if (field.getType() == FieldType.OBJECT) {
            getAssignedValue(fieldName);
        }

        // pass back any defaults setup
        FieldValue assignedValue = getAssignedValue(fieldName);
        if (assignedValue == null) {
            // does definition have a default value?
            if (objectDefinition.getField(fieldName).hasDefaultValue()) {
                assignedValue = objectDefinition.getField(fieldName).getDefaultValue();
            } else {
                // return the field type default value
                String defaultVal = objectDefinition.getField(fieldName).getType().getDefault();
                if (defaultVal != null) {
                    assignedValue = FieldValue.is(field, defaultVal);
                }
            }
        }

        return assignedValue;
    }

    public String toString() {

        StringBuilder output = new StringBuilder();

        for (Map.Entry<String, FieldValue> entry : values.entrySet()) {
            output.append("\n\t\t\t\t" + entry.getKey() + " : " + entry.getValue() + "\n");
        }

        return output.toString();
    }

    public void deleteAllFieldValuesExcept(List<String> fieldNamesToIgnore) {

        Set<String> ignorekeys = new HashSet<>(fieldNamesToIgnore);
        Set<String> keys = new HashSet<>(values.keySet());

        for (String key : keys) {
            if (!ignorekeys.contains(key)) {
                values.remove(key);
            }
        }
    }

    public InstanceFields cloned() {
        final InstanceFields clone = new InstanceFields(objectDefinition);
        for (FieldValue value : values.values()) {
            clone.addValue(value.cloned());
        }
        return clone;
    }

    public List<FieldValue> assignedValues() {
        List<FieldValue> assignedValues = new ArrayList<>();
        for (FieldValue value : values.values()) {
            assignedValues.add(value.cloned());
        }
        return Collections.unmodifiableList(assignedValues);
    }

    public DefinedFields getDefinition() {
        return objectDefinition;
    }

    // fieldname can be a path e.g. object.fieldOnObject
    public InstanceFields putValue(final String fieldName, final String value) {
        setFieldNameAsPath(fieldName, value, false);
        return this;
    }

    // fieldname can be a path e.g. object.fieldOnObject
    public InstanceFields setValue(final String fieldName, final String value) {
        setFieldNameAsPath(fieldName, value, true);
        return this;
    }

    private InstanceFields setFieldValue(final Field field, final FieldValue value) {

        final ValidationReport validationReport = field.validate(value);
        if (validationReport.isValid()) {
            addValue(FieldValue.is(field, field.getActualValueToAdd(value)));

        } else {
            throw new IllegalArgumentException(validationReport.getCombinedErrorMessages());
        }

        return this;
    }

    /*
        set a value in the object hierarchy and create objects as we go
        note that this can create partial objects which may not actually
        match validation rules
    */
    private void setFieldNameAsPath(
            final String fieldName, final String value, boolean shouldValidateValue) {
        // processing a complex set of fields

        final String[] fields = fieldName.split("\\.");
        final List<String> fieldNames = new ArrayList<>(Arrays.asList(fields));

        // start recursive call to work through list
        setFieldValue(fieldNames, value, shouldValidateValue);
    }

    /*
       recursive setFieldValue to handle 'objects'
    */
    private void setFieldValue(
            final List<String> fieldNames, final String value, boolean shouldValidateValue) {

        String fieldName = fieldNames.get(0);
        if (!objectDefinition.hasFieldNameDefined(fieldName)) {
            reportCannotFindFieldError(fieldName);
        }

        final Field field = objectDefinition.getField(fieldName);

        if (fieldNames.size() == 1) {
            // set the primitive value
            if (shouldValidateValue) {
                setFieldValue(field, FieldValue.is(field, value));
            } else {
                addValue(FieldValue.is(field, value));
            }
        } else {

            if (field.getType() != FieldType.OBJECT) {
                throw new RuntimeException(
                        "Cannot reference fields on non object fields: " + fieldName);
            }

            // to traverse to next object, may need to create it
            FieldValue objectValue = getAssignedValue(fieldName);
            if (objectValue == null) {
                objectValue = createObjectField(fieldName);
            }
            final InstanceFields fieldInstance = objectValue.asObject();

            fieldNames.remove(0); // processed this field

            fieldInstance.setFieldValue(fieldNames, value, shouldValidateValue);
        }
    }

    private FieldValue createObjectField(final String fieldName) {
        if (objectDefinition.hasFieldNameDefined(fieldName)) {
            Field field = objectDefinition.getField(fieldName);
            if (field.getType() == FieldType.OBJECT) {
                final FieldValue objectValue =
                        FieldValue.is(
                                field,
                                new InstanceFields(field.getObjectDefinition())
                                        .withAutoIncrementIds(defaultAuto));
                addValue(objectValue);
                return objectValue;
            }
        }
        return null;
    }

    private InstanceFields withAutoIncrementIds(final AutoIncrement auto) {
        List<Field> idfields = objectDefinition.getFieldsOfType(FieldType.AUTO_INCREMENT);
        for (Field field : idfields) {
            if (!values.containsKey(field.getName().toLowerCase())) {
                addValue(FieldValue.is(field, String.valueOf(auto.getNextValueAndUpdate())));
            }
        }
        return this;
    }

    private void reportCannotFindFieldError(final String fieldName) {
        throw new RuntimeException("Could not find field: " + fieldName);
    }

    // todo: not sure about the amAllowedToSetIds would prefer a different
    // way to configure the validation rules or exceptions to the rules
    public ValidationReport validateFields(
            final List<String> excluding, final boolean amAllowedToSetIds) {
        ValidationReport report = new ValidationReport();

        // Field validation

        for (String fieldName : objectDefinition.getFieldNames()) {
            if (!excluding.contains(fieldName)) {
                Field field = objectDefinition.getField(fieldName);
                ValidationReport validity =
                        field.validate(getAssignedValue(fieldName), amAllowedToSetIds);
                report.combine(validity);
            }
        }

        return report;
    }

    public boolean hasAssignedValue(String fieldName) {
        return values.containsKey(fieldName.toLowerCase());
    }
}
