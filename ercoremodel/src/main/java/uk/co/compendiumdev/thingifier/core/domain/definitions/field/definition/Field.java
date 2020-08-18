package uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition;

import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.domain.definitions.DefinedFields;
import uk.co.compendiumdev.thingifier.core.domain.randomdata.RandomString;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.ValidationRule;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

// todo: beginning to think that we should have an XField for each field type
// e.g. IdField, StringField, etc. - possibly with an interface or abstract
//      AField class - e.g. for 'mandatory'
//      then we would not have 'maximumStringLength' 'maximumIntegerValue' etc.
//      would have 'maximum' 'minimum' methods - some fields would have unique methods
public final class Field {

    private final String name;
    private final FieldType type;
    private final Set<String> fieldExamples;

    private boolean fieldIsOptional;

    // default value for the field
    private String defaultValue;
    private List<ValidationRule> validationRules;
    private boolean truncateStringIfTooLong;

    private int maximumIntegerValue;
    private int minimumIntegerValue;
    private boolean allowedNullable;
    // todo: use BigDecimal for the internal float representations
    private float maximumFloatValue;
    private float minimumFloatValue;
    private DefinedFields objectDefinition;


    private int nextId; // only used for id fields
    private int truncatedStringLength;

    // todo: rather than all these fields, consider moving to more validation rules
    // to help keep the class to a more manageable size

    private Field(final String name, final FieldType type) {
        this.name = name;
        this.type = type;
        validationRules = new ArrayList<>();
        fieldIsOptional = true;
        truncateStringIfTooLong=false;
        truncatedStringLength=-1;
        fieldExamples = new HashSet<>();
        maximumIntegerValue = Integer.MAX_VALUE;
        minimumIntegerValue = Integer.MIN_VALUE;
        maximumFloatValue = Float.MAX_VALUE;
        minimumFloatValue = Float.MIN_VALUE;
        allowedNullable=false;
        nextId=1;
    }

    public static Field is(String name) {
        return Field.is(name, FieldType.STRING);
    }

    public static Field is(String name, FieldType type) {
        return new Field(name, type);
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

    public ValidationReport validate(FieldValue value) {
        boolean NOT_ALLOWED_TO_SET_IDs = false;
        return validate(value, NOT_ALLOWED_TO_SET_IDs);
    }

    // allowedToSetIds is a bit of hack - refactor other code so not required
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

        // always validate against type
        //if(shouldValidateValuesAgainstType) {

            if (type == FieldType.BOOLEAN) {
                validateBooleanValue(value, report);
            }

            if (type == FieldType.INTEGER) {
                validateIntegerValue(value, report);
            }

            if(type == FieldType.STRING){
                // length is validated by a rule
            }

            if (type == FieldType.FLOAT) {
                validateFloatValue(value, report);
            }

            if (type == FieldType.ENUM) {
                validateEnumValue(value, report);
            }

            // TODO : add validation for DATE

            if(type == FieldType.OBJECT){
                validateObjectValue(value, report);
            }


        //}

        for (ValidationRule rule : validationRules) {
            if (!rule.validates(value)) {
                report.setValid(false);
                report.addErrorMessage(rule.getErrorMessage(value));
            }
        }

        return report;
    }

    private void validateObjectValue(final FieldValue value, final ValidationReport report) {
        FieldValue object = value;
        if(object!= null && object.asObject()!=null){
            final ValidationReport objectValidity =
                    object.asObject().
                            validateFields(new ArrayList<>(), true);
            report.combine(objectValidity);
        }
    }

    private void validateEnumValue(final FieldValue value, final ValidationReport report) {
        if (!getExamples().contains(value.asString())) {
            reportThisValueDoesNotMatchType(report, value.asString());
        }
    }

    private void validateFloatValue(final FieldValue value, final ValidationReport report) {
        try {
            float floatValue = value.asFloat();
            if (!withinAllowedFloatRange(floatValue)) {
                report.setValid(false);
                report.addErrorMessage(
                        String.format(
                                "%s : %s is not within range for type %s (%f to %f)",
                                this.getName(), value.asString(),
                                type, minimumFloatValue, maximumFloatValue));
            }

        } catch (NumberFormatException e) {
            reportThisValueDoesNotMatchType(report, value.asString());
        }
    }

    private void validateIntegerValue(final FieldValue value,
                                      final ValidationReport report) {
        try {

            int intVal = value.asInteger();

            if (!withinAllowedIntegerRange(intVal)) {
                report.setValid(false);
                report.addErrorMessage(
                        String.format(
                                "%s : %s is not within range for type %s (%d to %d)",
                                this.getName(), value.asString(),
                                type, minimumIntegerValue, maximumIntegerValue));
            }
        } catch (NumberFormatException e) {
            reportThisValueDoesNotMatchType(report, value.asString());

        }
    }

    private void reportThisValueDoesNotMatchType(final ValidationReport report,
                                                 final String valueString) {
        report.setValid(false);
        report.addErrorMessage(
                String.format(
                        "%s : %s does not match type %s",
                        name,  valueString, type));
    }

    private void validateBooleanValue(final FieldValue value,
                                      final ValidationReport report) {
        try{
            boolean bool = value.asBoolean();
        }catch(IllegalArgumentException e){
            report.setValid(false);
            report.addErrorMessage(
                    String.format(
                            "%s : %s does not match type %s (true, false)",
                            this.getName(),
                            value.asString(), type));
        }
    }


    public List<ValidationRule> validationRules() {
        return validationRules;
    }

    public boolean isMandatory(){
        return !fieldIsOptional;
    }

    public Field makeMandatory() {
        fieldIsOptional = false;
        return this;
    }

    /*
       todo: consider adding Formatting Rules
        instead of truncateString To -
        could be useful for integers, float rounding, dates, etc.
     */
    public Field truncateStringTo(final int maximumTruncatedLengthOfString) {
        truncateStringIfTooLong = true;
        truncatedStringLength =maximumTruncatedLengthOfString;
        return this;
    }

    public boolean shouldTruncate() {
        return truncateStringIfTooLong;
    }

    public String getAsTruncatedString(FieldValue value){
        String truncated = value.asString();
        if(truncateStringIfTooLong && truncatedStringLength>-1){
            if(truncated.length()>truncatedStringLength) {
                truncated = truncated.substring(0, truncatedStringLength);
            }
        }
        return truncated;
    }

    public Field withExample(final String anExample) {
        fieldExamples.add(anExample);
        return this;
    }

    public List<String> getExamples() {

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

        // field might have examples in definition
        if(!fieldExamples.isEmpty()){
            buildExamples.addAll(fieldExamples);
        }

        // TODO: try to use regex in matching rules to generate
        if(type==FieldType.STRING){
            if(fieldExamples.isEmpty()){
                buildExamples.add(
                    getAsTruncatedString(
                        FieldValue.is(getName(), new RandomString().get(20))
                    )
                );
            }
        }

        // return as a list
        return new ArrayList<>(buildExamples);
    }

    public String getRandomExampleValue() {
        final List<String> examples = getExamples();

        if(examples.isEmpty()){
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
                floatValue<=maximumFloatValue);
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

    public String getActualValueToAdd(final FieldValue value) {

        switch (type){
            case BOOLEAN:
                return Boolean.valueOf(value.asString()).toString();
            case FLOAT:
                return Float.valueOf(value.asString()).toString();
            case STRING:
                if(shouldTruncate()){
                    return getAsTruncatedString(value);
                }else{
                    return value.asString();
                }
            case INTEGER:
            case ID:
                Double dVal = Double.parseDouble(value.asString());
                return String.valueOf(dVal.intValue());
            case GUID:
            case OBJECT:
            case ENUM:
            case DATE:
                return value.asString();
            default:
                System.out.println("Unhandled value to add on set");
                return value.asString();
        }
    }

}
