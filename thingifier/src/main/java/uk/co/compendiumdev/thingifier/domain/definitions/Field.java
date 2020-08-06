package uk.co.compendiumdev.thingifier.domain.definitions;

import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.domain.FieldType;
import uk.co.compendiumdev.thingifier.domain.data.RandomString;
import uk.co.compendiumdev.thingifier.domain.definitions.validation.ValidationRule;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class Field {

    private final String name;
    private final FieldType type;
    private final Set<String> fieldExamples;

    private boolean fieldIsOptional;

    // default value for the field
    private String defaultValue;
    private List<ValidationRule> validationRules;
    private boolean truncateStringIfTooLong;
    private int maximumStringLength;
    private int maximumIntegerValue;
    private int minimumIntegerValue;
    private boolean allowedNullable;
    // todo: use BigDecimal for the internal float representations
    private float maximumFloatValue;
    private float minimumFloatValue;
    private DefinedFields objectDefinition;

    // allow this being switched off
    private boolean shouldValidateValuesAgainstType;
    private boolean validateIfStringIsTooLong;

    private int nextId; // only used for id fields

    private Field(final String name, final FieldType type) {
        this.name = name;
        this.type = type;
        validationRules = new ArrayList<>();
        fieldIsOptional = true;
        truncateStringIfTooLong=false;
        fieldExamples = new HashSet<>();
        maximumIntegerValue = Integer.MAX_VALUE;
        minimumIntegerValue = Integer.MIN_VALUE;
        maximumFloatValue = Float.MAX_VALUE;
        minimumFloatValue = Float.MIN_VALUE;
        allowedNullable=false;
        shouldValidateValuesAgainstType=true;
        validateIfStringIsTooLong = false;
        nextId=1;
    }

    public static Field is(String name) {
        return Field.is(name, FieldType.STRING);
    }

    public static Field is(String name, FieldType type) {
        Field aField = new Field(name, type);
        return aField;
    }

    public String getName() {
        return name;
    }

    public String getNextIdValue() {
        int id = nextId;
        nextId++;
        return String.valueOf(id);
    }

    // an external way to set the next id
    public void ensureNextIdAbove(final String value) {
        try{
            final int desiredId = Integer.parseInt(value);
            if(nextId<=desiredId){
                nextId=desiredId+1;
            }
        }catch(Exception e){
            // ignore conversion errors
        }
    }

    public Field ignoreTypeValidation(){
        shouldValidateValuesAgainstType=true;
        return this;
    }

    public Field withDefaultValue(String aDefaultValue) {
        this.defaultValue = aDefaultValue;
        fieldExamples.add(aDefaultValue);
        return this;
    }

    public FieldValue getDefaultValue() {
        // todo: allow configuration of allowedNullable
        // todo: handle defaults of object and array
        if(defaultValue==null && !allowedNullable){
            // get the definition default
            return FieldValue.is(name, type.getDefault());
        }
        return FieldValue.is(name, defaultValue);
    }

    public boolean hasDefaultValue() {
        return defaultValue != null;
    }

    public FieldType getType() {
        return type;
    }

    public Field withValidation(ValidationRule validationRule) {
        validationRules.add(validationRule);
        return this;
    }

    public Field withValidation(ValidationRule... validationRule) {
        validationRules.addAll(Arrays.asList(validationRule));
        return this;
    }

    public ValidationReport validate(String value) {
        boolean NOT_ALLOWED_TO_SET_IDs = false;
        return validate(FieldValue.is(name, value), NOT_ALLOWED_TO_SET_IDs);
    }

    public ValidationReport validate(FieldValue value) {
        boolean NOT_ALLOWED_TO_SET_IDs = false;
        return validate(value, NOT_ALLOWED_TO_SET_IDs);
    }

    public ValidationReport validate(FieldValue value, boolean allowedToSetIds) {


        ValidationReport report = new ValidationReport();

        // missing fields will come through as null,
        // if they are optional then that is fine
        if (fieldIsOptional && value == null) {
            report.setValid(true);
            return report;
        }

        if(!allowedToSetIds) {
            if (type == FieldType.ID) {
                report.setValid(false);
                report.addErrorMessage(String.format("%s : field is an ID, you can't set it", this.getName()));
                return report;
            }
        }

        if (!fieldIsOptional && value == null) {
            report.setValid(false);
            report.addErrorMessage(String.format("%s : field is mandatory", this.getName()));
            return report;
        }


        if(shouldValidateValuesAgainstType) {

            String stringValue = value.getValueAsString();

            if (type == FieldType.BOOLEAN) {
                if (!(stringValue.toLowerCase().contentEquals("true")
                        || stringValue.toLowerCase().contentEquals("false"))) {
                    report.setValid(false);
                    report.addErrorMessage(
                            String.format(
                                    "%s : %s does not match type %s (true, false)", this.getName(), stringValue, type));
                }
            }

            if (type == FieldType.INTEGER) {
                try {
                    int intVal = Integer.valueOf(stringValue);
                    if (!withinAllowedIntegerRange(intVal)) {
                        report.setValid(false);
                        report.addErrorMessage(
                                String.format(
                                        "%s : %s is not within range for type %s (%d to %d)",
                                        this.getName(), stringValue, type, minimumIntegerValue, maximumIntegerValue));
                    }
                } catch (NumberFormatException e) {
                    report.setValid(false);
                    report.addErrorMessage(
                            String.format(
                                    "%s : %s does not match type %s", this.getName(), stringValue, type));
                }
            }


            if(type == FieldType.STRING){
                if(validateIfStringIsTooLong){
                    if(stringValue.length()>maximumStringLength){
                        report.setValid(false);
                        report.addErrorMessage(
                                String.format(
                                        "%s : is too long (max %d)",
                                        this.getName(), maximumStringLength));
                    }
                }
            }

            if (type == FieldType.FLOAT) {
                try {
                    float floatValue = Float.valueOf(stringValue);
                    if (!withinAllowedFloatRange(floatValue)) {
                        report.setValid(false);
                        report.addErrorMessage(
                                String.format(
                                        "%s : %s is not within range for type %s (%f to %f)",
                                        this.getName(), stringValue, type, minimumFloatValue, maximumFloatValue));
                    }

                } catch (NumberFormatException e) {
                    report.setValid(false);
                    report.addErrorMessage(
                            String.format(
                                    "%s : %s does not match type %s", this.getName(), stringValue, type));
                }
            }

            // TODO : add validation for DATE
            // TODO : add validation for ENUM
            // TODO : add validation for OBJECT
            if (type == FieldType.ENUM) {
                if (!getExamples().contains(stringValue)) {
                    report.setValid(false);
                    report.addErrorMessage(
                            String.format(
                                    "%s : %s does not match type %s", this.getName(), stringValue, type));
                }
            }
        }

        // TODO : ValidationRule should use FieldValue not String
        for (ValidationRule rule : validationRules) {
            String stringValue = value.getValueAsString();
            if (!rule.validates(stringValue)) {
                report.setValid(false);
                report.addErrorMessage(rule.getErrorMessage(this.getName()));
            }
        }

        return report;
    }



    public List<ValidationRule> validationRules() {
        return validationRules;
    }

    public boolean isMandatory(){
        return !fieldIsOptional;
    }

    public Field mandatory() {
        fieldIsOptional = false;
        return this;
    }

    public Field truncateStringTo(final int maximumTruncatedLengthOfString) {
        truncateStringIfTooLong = true;
        maximumStringLength =maximumTruncatedLengthOfString;
        return this;
    }

    public Field maximumStringLength(final int maximumLengthOfString) {
        validateIfStringIsTooLong = true;
        maximumStringLength =maximumLengthOfString;
        return this;
    }

    public boolean shouldTruncate() {
        return truncateStringIfTooLong;
    }

    public int getMaximumAllowedLength() {
        if(truncateStringIfTooLong){
            return maximumStringLength;
        }
        return -1; // no limit
    }

    public String truncatedString(String toMakeWithinLimits){
        String truncated = toMakeWithinLimits;
        if(truncateStringIfTooLong){
            truncated = toMakeWithinLimits.substring(0,getMaximumAllowedLength());
        }
        return truncated;
    }

    public Field withExample(final String anExample) {
        fieldExamples.add(anExample);
        return this;
    }

    public ArrayList<String> getExamples() {

        // todo add the default, any examples to retExamples and any extra stuff below
        // combine, don't replace
        Set<String> buildExamples = new HashSet<>();

        if (type == FieldType.BOOLEAN) {
            String[] samples = {"true", "false"};
            buildExamples.addAll(Arrays.asList(samples));
        }

        if(type==FieldType.INTEGER){
            int rndInt = ThreadLocalRandom.current().
                            nextInt(minimumIntegerValue, maximumIntegerValue + 1);
            buildExamples.add(String.valueOf(rndInt));
        }

        if(type==FieldType.ID){
            int rndInt = ThreadLocalRandom.current().
                    nextInt(1, 100);
            buildExamples.add(String.valueOf(rndInt));
        }

        if(type==FieldType.GUID){
            buildExamples.add(UUID.randomUUID().toString());
        }

        if(type==FieldType.FLOAT){
            final float rndFloat = minimumFloatValue + ThreadLocalRandom.current().nextFloat() * (maximumFloatValue - minimumFloatValue);
            buildExamples.add(String.valueOf(rndFloat));
        }

        // string might have examples
        if(fieldExamples.size()>0){
            buildExamples.addAll(fieldExamples);
        }

        // TODO: try to use regex in matching rules to generate
        if(type==FieldType.STRING){
            if(fieldExamples.size()==0){
                buildExamples.add(truncatedString(new RandomString().get(20)));
            }
        }

        // return as a list
        return new ArrayList<String>(buildExamples);
    }

    public String getRandomExampleValue() {
        final ArrayList<String> examples = getExamples();

        if(examples.size()==0){
            return "";
        }

        return examples.get(new Random().nextInt(examples.size()));
    }

    public Field withMaximumValue(final int maximumInteger) {
        this.maximumIntegerValue = maximumInteger;
        return this;
    }

    public Field withMinimumValue(final int minimumInteger) {
        this.minimumIntegerValue = minimumInteger;
        return this;
    }

    public boolean withinAllowedIntegerRange(final int intVal) {
        return (intVal>=minimumIntegerValue &&
                intVal<=maximumIntegerValue);
    }

    public Field withMaximumValue(final float maxFloat) {
        this.maximumFloatValue = maxFloat;
        return this;
    }

    public Field withMinimumValue(final float minFloat) {
        this.minimumFloatValue = minFloat;
        return this;
    }

    public boolean withinAllowedFloatRange(final float floatValue) {
        return (floatValue>=minimumFloatValue &&
                floatValue<=maximumIntegerValue);
    }

    public int truncateLength() {
        return maximumStringLength;
    }

    public int getMaximumIntegerValue() {
        return maximumIntegerValue;
    }

    public int getMinimumIntegerValue() {
        return minimumIntegerValue;
    }

    public Float getMinimumFloatValue() {
        return minimumFloatValue;
    }

    public Float getMaximumFloatValue() {
        return maximumFloatValue;
    }

    public boolean willValidate() {
        return shouldValidateValuesAgainstType;
    }

    public boolean willEnforceLength() {
        return validateIfStringIsTooLong;
    }

    public Field withField(final Field childField) {
        if(objectDefinition==null){
            objectDefinition = new DefinedFields();
        }
        objectDefinition.addField(childField);
        return this;
    }

    public DefinedFields getObjectDefinition() {
        return objectDefinition;
    }

    public String getValueToAdd(final String value) {

        String valueToAdd = value;

        switch (type){
            case BOOLEAN:
                return Boolean.valueOf(valueToAdd).toString();
            case FLOAT:
                return Float.valueOf(valueToAdd).toString();
            case STRING:
                if(shouldTruncate()){
                    return valueToAdd.substring(0,getMaximumAllowedLength());
                }else{
                    return valueToAdd;
                }
            case INTEGER:
            case ID:
                try {
                    Double dVal = Double.parseDouble(value);
                    return String.valueOf(dVal.intValue());
                }catch(Exception e){
                    return Integer.valueOf(valueToAdd).toString();
                }
            case GUID:
            case OBJECT:
            case ENUM:
            case DATE:
                return valueToAdd;
            default:
                System.out.println("Unhandled value to add on set");
                return valueToAdd;
        }

    }

}
