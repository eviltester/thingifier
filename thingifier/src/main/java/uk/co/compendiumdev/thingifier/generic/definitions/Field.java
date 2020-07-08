package uk.co.compendiumdev.thingifier.generic.definitions;

import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.validation.ValidationRule;

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
    private int truncateStringLengthTo;
    private int maximumIntegerValue;
    private int minimumIntegerValue;
    private boolean allowedNullable;
    // todo: use BigDecimal for the internal float representations
    private float maximumFloatValue;
    private float minimumFloatValue;

    // allow this being switched off
    private boolean shouldValidateValuesAgainstType;

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

    public Field ignoreTypeValidation(){
        shouldValidateValuesAgainstType=true;
        return this;
    }

    public Field withDefaultValue(String aDefaultValue) {
        this.defaultValue = aDefaultValue;
        fieldExamples.add(aDefaultValue);
        return this;
    }

    public String getDefaultValue() {
        // todo: allow configuration of allowedNullable
        if(defaultValue==null && !allowedNullable){
            // get the definition default
            return type.getDefault();
        }
        return defaultValue;
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


        ValidationReport report = new ValidationReport();

        // missing fields will come through as null,
        // if they are optional then that is fine
        if (fieldIsOptional && value == null) {
            report.setValid(true);
            return report;
        }

        if(type == FieldType.ID){
            report.setValid(false);
            report.addErrorMessage(String.format("%s : field is an ID, you can't set it", this.getName()));
            return report;
        }
        // TODO: if we do allow setting ids then we have to check if it is higher or equal to the nextId
        // and adjust nextId to be this id +1


        if (!fieldIsOptional && value == null) {
            report.setValid(false);
            report.addErrorMessage(String.format("%s : field is mandatory", this.getName()));
            return report;
        }


        if(shouldValidateValuesAgainstType) {
            if (type == FieldType.BOOLEAN) {
                if (!(value.toLowerCase().contentEquals("true")
                        || value.toLowerCase().contentEquals("false"))) {
                    report.setValid(false);
                    report.addErrorMessage(
                            String.format(
                                    "%s : %s does not match type %s (true, false)", this.getName(), value, type));
                }
            }

            if (type == FieldType.INTEGER) {
                try {
                    int intVal = Integer.valueOf(value);
                    if (!withinAllowedIntegerRange(intVal)) {
                        report.setValid(false);
                        report.addErrorMessage(
                                String.format(
                                        "%s : %s is not within range for type %s (%d to %d)",
                                        this.getName(), value, type, minimumIntegerValue, maximumIntegerValue));
                    }
                } catch (NumberFormatException e) {
                    report.setValid(false);
                    report.addErrorMessage(
                            String.format(
                                    "%s : %s does not match type %s", this.getName(), value, type));
                }
            }


            if (type == FieldType.FLOAT) {
                try {
                    float floatValue = Float.valueOf(value);
                    if (!withinAllowedFloatRange(floatValue)) {
                        report.setValid(false);
                        report.addErrorMessage(
                                String.format(
                                        "%s : %s is not within range for type %s (%f to %f)",
                                        this.getName(), value, type, minimumFloatValue, maximumFloatValue));
                    }

                } catch (NumberFormatException e) {
                    report.setValid(false);
                    report.addErrorMessage(
                            String.format(
                                    "%s : %s does not match type %s", this.getName(), value, type));
                }
            }

            // TODO : add validation for DATE
            // TODO : add validation for ENUM
            if (type == FieldType.ENUM) {
                if (!getExamples().contains(value)) {
                    report.setValid(false);
                    report.addErrorMessage(
                            String.format(
                                    "%s : %s does not match type %s", this.getName(), value, type));
                }
            }
        }

        for (ValidationRule rule : validationRules) {
            if (!rule.validates(value)) {
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
        truncateStringLengthTo=maximumTruncatedLengthOfString;
        return this;
    }

    public boolean shouldTruncate() {
        return truncateStringIfTooLong;
    }

    public int getMaximumAllowedLength() {
        if(truncateStringIfTooLong){
            return truncateStringLengthTo;
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

        if(type==FieldType.BOOLEAN){
            String[] samples = {"true", "false"};
            return new ArrayList<String>(Arrays.asList(samples));
        }

        if(type==FieldType.INTEGER){
            ArrayList<String>returnThis = new ArrayList<>();
            int rndInt = ThreadLocalRandom.current().
                            nextInt(minimumIntegerValue, maximumIntegerValue + 1);
            returnThis.add(String.valueOf(rndInt));
            return returnThis;
        }

        if(type==FieldType.FLOAT){
            ArrayList<String>returnThis = new ArrayList<>();
            final float rndFloat = minimumFloatValue + ThreadLocalRandom.current().nextFloat() * (maximumFloatValue - minimumFloatValue);
            returnThis.add(String.valueOf(rndFloat));
            return returnThis;
        }

        if(type==FieldType.STRING){
            if(fieldExamples.size()==0){
                ArrayList<String>returnThis = new ArrayList<>();
                //TODO: randomly generate a string
                returnThis.add(truncatedString("bob"));
                return returnThis;
            }
        }
        return new ArrayList<String>(fieldExamples);
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
}
