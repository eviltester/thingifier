package uk.co.compendiumdev.thingifier.generic.definitions;

import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.validation.MatchesTypeValidationRule;
import uk.co.compendiumdev.thingifier.generic.definitions.validation.ValidationRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Field {

    private final String name;
    private final FieldType type;
    private boolean fieldIsOptional;

    // default value for the field
    private String defaultValue;
    private List<ValidationRule> validationRules;
    private boolean truncateStringIfTooLong;
    private int truncateStringLengthTo;

    private Field(final String name, final FieldType type) {
        this.name = name;
        this.type = type;
        validationRules = new ArrayList<>();
        fieldIsOptional = true;
        truncateStringIfTooLong=false;

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


    public Field withDefaultValue(String aDefaultValue) {
        this.defaultValue = aDefaultValue;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean hasDefaultValue() {
        return defaultValue != null;
    }

    public FieldType getType() {
        return type;
    }

    public boolean isValidValue(String value) {

        if (value == null) {
            return false;
        }

        if (type == FieldType.BOOLEAN) {
            if (value.toLowerCase().contentEquals("true")
                    || value.toLowerCase().contentEquals("false")) {
                return true;
            }

            return false;
        }

        if (type == FieldType.INTEGER) {
            try {
                Integer.valueOf(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        // TODO : add validation for DATE

        return true;
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

        if (!fieldIsOptional && value == null) {
            report.setValid(false);
            report.addErrorMessage(String.format("%s : field is mandatory", this.getName()));
            return report;
        }

        for (ValidationRule rule : validationRules) {

            if (rule instanceof MatchesTypeValidationRule) {
                if (!isValidValue(value)) {
                    report.setValid(false);
                    report.addErrorMessage(String.format("%s : %s does not match type %s", this.getName(), value, type));
                }
            } else {

                if (!rule.validates(value)) {
                    report.setValid(false);
                    report.addErrorMessage(rule.getErrorMessage(this.getName()));
                }
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
}
