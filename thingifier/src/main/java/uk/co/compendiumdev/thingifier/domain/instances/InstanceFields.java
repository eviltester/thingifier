package uk.co.compendiumdev.thingifier.domain.instances;

import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.domain.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.DefinedFields;
import uk.co.compendiumdev.thingifier.domain.definitions.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.FieldValue;

import java.util.*;

public class InstanceFields {

    private final DefinedFields objectDefinition;
    private Map<String, FieldValue> values = new HashMap<String, FieldValue>();

    private Map<String, InstanceFields> objectFields;

    public InstanceFields(final DefinedFields objectDefinition) {
        this.objectDefinition = objectDefinition;
    }

    public void addValue(String fieldName, String value) {
        values.put(fieldName.toLowerCase(), FieldValue.is(fieldName.toLowerCase(), value));
    }

    public String getAssignedValue(String fieldName) {
        FieldValue value = values.get(fieldName.toLowerCase());
        if(value==null){
            return null;
        }
        return value.getValue();
    }

    public String getValue(String fieldName) {

        if(!objectDefinition.hasFieldNameDefined(fieldName)){
            reportCannotFindFieldError(fieldName);
        }

        Field field = objectDefinition.getField(fieldName);
        if(field.getType()==FieldType.OBJECT){
            return ""; // should use getObjectField to find values of object
        }

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

    // todo: rename to getFieldNames
    public List<String> getFields() {
        List<String> fields = new ArrayList<String>(values.keySet());
        return fields;
    }

    public String toString() {

        StringBuilder output = new StringBuilder();

        for (Map.Entry<String, FieldValue> entry : values.entrySet()) {
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

    /* todo, this should really be a 'clone' */
    /* todo, this does not handle objects */
    public Map<String, String> asMap() {
        HashMap<String, String> aMap = new HashMap<String, String>();
        for(FieldValue value : values.values()){
            aMap.put(value.getName(), value.getValue());
        }
        return aMap;
    }

    public boolean hasFieldNamed(String fieldName) {
        // todo: handle objects with . referencing e.g. person.name
        return values.keySet().contains(fieldName);
    }


    private void addObjectInstance(final String objectFieldName, final InstanceFields instance) {
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

    private InstanceFields setFieldValue(final Field field, final String fieldName, final String value) {

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


    private InstanceFields getObjectField(final String objectFieldName) {
        if(objectFields==null){
            objectFields = new HashMap<>();
        }
        return objectFields.get(objectFieldName);
    }

    // todo perhaps we should return a FieldValue and then allow the 'caller'
    //  to getObject(), getString(), getInteger() etc. on that value?
    // that would make it easier to handle objects, nested objects and arrays
    public InstanceFields getObjectInstance(final String objectFieldName) {

        // todo handle nested objects e.g. person.phonenumbers
        Field objectField = objectDefinition.getField(objectFieldName);
        if(objectField.getType()==FieldType.OBJECT){

            InstanceFields instance = getObjectField(objectFieldName);
            if(instance==null){
                instance = new InstanceFields(objectField.getObjectDefinition());
                addObjectInstance(objectFieldName, instance);
            }
            return instance;
        }
        return null;
    }



    private InstanceFields processFieldNameAsObject(final String fieldName, final String value) {
        // processing a complex set of fields
        final String[] fields = fieldName.split("\\.");
        String toplevelFieldName = fields[0];
        if (objectDefinition.hasFieldNameDefined(toplevelFieldName)){

            // process a setField on an Object field
            Field field = objectDefinition.getField(toplevelFieldName);
            if(field.getType()!= FieldType.OBJECT){
                throw new RuntimeException(
                        "Cannot reference fields on non object fields: " + fieldName);
            }
            final List<String> fieldNames = new ArrayList();
            fieldNames.addAll(Arrays.asList(fields));
            fieldNames.remove(0);
            // need to track the instance with it
            final InstanceFields fieldInstance = getObjectInstance(toplevelFieldName);

            return setFieldValue(fieldInstance, fieldNames, value);
        }
        return this;
    }

    /*
        recursive setFieldValue to handle 'objects'
     */
    private InstanceFields setFieldValue(final InstanceFields fieldInstance, final List<String> fieldNames, final String value) {

        String fieldName = fieldNames.get(0);
        final DefinedFields defn = fieldInstance.getDefinition();
        if(defn==null || !defn.hasFieldNameDefined(fieldName)){
            reportCannotFindFieldError(fieldName);
        }

        final Field nextField = defn.getField(fieldName);

        if(fieldNames.size()==1){
            fieldInstance.setFieldValue(nextField, fieldName, value);
            return this;
        }else{

            if(nextField.getType()!= FieldType.OBJECT){
                throw new RuntimeException(
                        "Cannot reference fields on non object fields: " + fieldName);
            }
            fieldNames.remove(0);

            final InstanceFields nextFieldInstance = fieldInstance.getObjectField(fieldName);
            return setFieldValue(nextFieldInstance, fieldNames, value);
        }

    }

    private void reportCannotFindFieldError(final String fieldName) {
        throw new RuntimeException("Could not find field: " + fieldName);
    }

    public InstanceFields setValue(final String fieldName, final String value) {

        if (objectDefinition.hasFieldNameDefined(fieldName)) {

            return setFieldValue(objectDefinition.getField(fieldName), fieldName, value);

        } else {

            if(fieldName.contains(".")){
                return processFieldNameAsObject(fieldName, value);

            }else {
                reportCannotFindFieldError(fieldName);
            }
        }

        return this;
    }

    public ValidationReport validateFields(final List<String> excluding, final boolean amAllowedToSetIds) {
        ValidationReport report = new ValidationReport();

        // Field validation

        for (String fieldName : objectDefinition.getFieldNames()) {
            if(!excluding.contains(fieldName)) {
                Field field = objectDefinition.getField(fieldName);
                ValidationReport validity = field.validate(
                                                    getAssignedValue(fieldName),
                                                amAllowedToSetIds);
                report.combine(validity);
            }
        }

        return report;
    }

    // TODO: this probably should not take a body parser as arg - seems too high level
    public List<String> findAnyGuidOrIdDifferences(final BodyParser args) {

        List<String> errorMessages = new ArrayList<>();

        // protected fields
        List<Field> idOrGuidFields = objectDefinition.getFieldsOfType(FieldType.GUID);
        idOrGuidFields.addAll(objectDefinition.getFieldsOfType(FieldType.ID));

        for (Map.Entry<String, String> entry : args.getFlattenedStringMap()) {

            // Handle attempt to amend a protected field
            Field field = objectDefinition.getField(entry.getKey());
            if (idOrGuidFields.contains(field)) {
                // if editing it then throw error, ignore if same value
                String existingValue = getValue(entry.getKey());
                if (existingValue != null && existingValue.trim().length() > 0) {
                    // if value is different then it is an attempt to amend it
                    if (!existingValue.equalsIgnoreCase(entry.getValue())) {
                        errorMessages.add(
                                String.format("Can not amend %s from %s to %s",
                                        entry.getKey(),
                                        existingValue,
                                        entry.getValue()));
                    }
                }
            }
        }
        return errorMessages;
    }
}
