package uk.co.compendiumdev.thingifier.domain.instances;

import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.domain.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.DefinedFields;
import uk.co.compendiumdev.thingifier.domain.definitions.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.FieldValue;

import java.util.*;

public class InstanceFields {

    private final DefinedFields objectDefinition;
    private Map<String, FieldValue> values = new HashMap<String, FieldValue>();

    public InstanceFields(final DefinedFields objectDefinition) {
        this.objectDefinition = objectDefinition;
    }

    public void addValue(String fieldName, String value) {
        values.put(fieldName.toLowerCase(), FieldValue.is(fieldName.toLowerCase(), value));
    }

    private void addValue(final String name, final FieldValue value) {
        values.put(name.toLowerCase(), value);
    }

    public FieldValue getAssignedValue(String fieldName) {
        return values.get(fieldName.toLowerCase());
    }

    public FieldValue getValue(String fieldName) {

        if(!objectDefinition.hasFieldNameDefined(fieldName)){
            reportCannotFindFieldError(fieldName);
        }

        // bypass default processing for OBJECT, ARRAY - at the moment
        // todo: allow defaults for OBJECT, ARRAY, etc.
        Field field = objectDefinition.getField(fieldName);
        if(field.getType()==FieldType.OBJECT){
            getAssignedValue(fieldName);
        }

        // pass back any defaults setup
        FieldValue assignedValue = getAssignedValue(fieldName);
        if (assignedValue == null) {
            // does definition have a default value?
            if (objectDefinition.getField(fieldName).hasDefaultValue()) {
                return objectDefinition.getField(fieldName).getDefaultValue();
            } else {
                // return the field type default value
                String defaultVal = objectDefinition.getField(fieldName).getType().getDefault();
                if (defaultVal != null) {
                    return FieldValue.is(fieldName, defaultVal);
                }
            }
        }

        return assignedValue;
    }

    // todo perhaps we should return a FieldValue and then allow the 'caller'
    //  to getObject(), getString(), getInteger() etc. on that value?
    // that would make it easier to handle objects, nested objects and arrays
    public InstanceFields getObjectInstance(final String objectFieldName) {

        // todo handle nested objects e.g. person.phonenumbers
        Field objectField = objectDefinition.getField(objectFieldName);
        if(objectField.getType()==FieldType.OBJECT){

            InstanceFields instance = getValue(objectFieldName).asObject();
            if(instance==null){
                instance = new InstanceFields(objectField.getObjectDefinition());
                addValue(objectFieldName, FieldValue.is(objectFieldName, instance));
            }
            return instance;
        }
        return null;
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

    public InstanceFields cloned(){
        final InstanceFields clone = new InstanceFields(objectDefinition);
        for(FieldValue value : values.values()){
            clone.addValue(value.getName(), value.cloned());
        }
        return clone;
    }

    public boolean hasFieldNamed(String fieldName) {
        // todo: handle objects with . referencing e.g. person.name
        return values.keySet().contains(fieldName);
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






    /*
        set a value in the object hierarchy and create objects as we go
        note that this can create partial objects which may not actually
        match validation rules
    */
    private void setFieldNameAsObject(final String fieldName, final String value) {
        // processing a complex set of fields

        final String[] fields = fieldName.split("\\.");
        final List<String> fieldNames = new ArrayList();
        fieldNames.addAll(Arrays.asList(fields));

        // start recursive call to work through list
        setFieldValue(fieldNames, value);
    }

    /*
        recursive setFieldValue to handle 'objects'
     */
    protected void setFieldValue(final List<String> fieldNames, final String value) {

        String fieldName = fieldNames.get(0);
        if(!objectDefinition.hasFieldNameDefined(fieldName)){
            reportCannotFindFieldError(fieldName);
        }

        final Field field = objectDefinition.getField(fieldName);

        if(fieldNames.size()==1){
            // set the primitive value
            setFieldValue(field, fieldName, value);
            return;
        }else{

            if(field.getType()!= FieldType.OBJECT){
                throw new RuntimeException(
                        "Cannot reference fields on non object fields: " + fieldName);
            }

            // to traverse to next object, may need to create it
            FieldValue objectValue = getAssignedValue(fieldName);
            if(objectValue==null){
                objectValue = createObjectField(fieldName);
            }
            final InstanceFields fieldInstance = objectValue.asObject();

            fieldNames.remove(0); // processed this field

            fieldInstance.setFieldValue(fieldNames, value);
        }

    }

    private FieldValue createObjectField(final String fieldName) {
        if(objectDefinition.hasFieldNameDefined(fieldName)){
            Field field = objectDefinition.getField(fieldName);
            if(field.getType()==FieldType.OBJECT){
                final FieldValue objectValue = FieldValue.is(fieldName,
                        new InstanceFields(field.getObjectDefinition()));
                addValue(fieldName, objectValue);
                return objectValue;
            }
        }
        return null;
    }

    private void reportCannotFindFieldError(final String fieldName) {
        throw new RuntimeException("Could not find field: " + fieldName);
    }

    public InstanceFields setValue(final String fieldName, final String value) {

        if (objectDefinition.hasFieldNameDefined(fieldName)) {
            setFieldValue(objectDefinition.getField(fieldName), fieldName, value);
        } else {
            if(fieldName.contains(".")){
                // this will throw an error if called with a 'relationship'
                // up to the caller to make sure that doesnt' happen
                setFieldNameAsObject(fieldName, value);
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
    public List<String> findAnyGuidOrIdDifferences(final List<Map.Entry<String, String>> args) {

        List<String> errorMessages = new ArrayList<>();

        // protected fields
        List<Field> idOrGuidFields = objectDefinition.getFieldsOfType(FieldType.GUID);
        idOrGuidFields.addAll(objectDefinition.getFieldsOfType(FieldType.ID));

        for (Map.Entry<String, String> entry : args) {

            // Handle attempt to amend a protected field
            Field field = objectDefinition.getField(entry.getKey());
            if (idOrGuidFields.contains(field)) {
                // if editing it then throw error, ignore if same value
                String existingValue = getValue(entry.getKey()).asString();
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

    public void setValuesFromClone(final InstanceFields args) {
        for(String fieldName : objectDefinition.getFieldNames()){
            FieldValue value = args.getAssignedValue(fieldName);
            if(value!=null){
                addValue(fieldName, value);
            }
        }
    }
}
