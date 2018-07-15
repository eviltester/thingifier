package uk.co.compendiumdev.thingifier.generic.definitions;

import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.validation.ValidationRule;

import java.util.ArrayList;
import java.util.List;

public class Field {

    private final String name;
    private final FieldType type;

    // default value for the field
    private String defaultValue;
    private List<ValidationRule> validationRules;

    private Field(String name, FieldType type) {
        this.name = name;
        this.type = type;
        validationRules=new ArrayList<>();

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
        return defaultValue!=null;
    }

    public FieldType getType() {
        return type;
    }

    public boolean isValidValue(String value) {
        if(type == FieldType.BOOLEAN){
            if(value.toLowerCase().contentEquals("true") ||
                    value.toLowerCase().contentEquals("false")     ){
                return true;
            }

            return false;
        }

        // TODO : add validation for Integer

        return true;
    }

    public Field withValidation(ValidationRule validationRule) {
        validationRules.add(validationRule);
        return this;
    }

    public ValidationReport validate(String value) {

        ValidationReport report = new ValidationReport();

        for(ValidationRule rule : validationRules){
            boolean valid = rule.validates(value);
            if(!valid) {
                report.setValid(false);
                report.addErrorMessage(rule.getErrorMessage(this.getName()));
            }
        }

        return report;
    }

    public List<ValidationRule> validationRules() {
        return validationRules;
    }


    /* Too complicated add to DSL later */
    /*
    public static Field fromData(String fieldData) {
        // if starts with !DEFAULT! then it is a default field
        // if it contains : then it is a property pair
        // if it contains \: then it is not a property pair

        boolean isDefault = false;
        String fieldString = fieldData;


        // find property split point
        int propertySplitPoint = 0;

        Field retField=null;

        while(propertySplitPoint<fieldData.length() && retField==null) {
            int colonPos = fieldString.indexOf(':', propertySplitPoint);
            if (colonPos == -1) {
                // not found it is not a property pair it is just a name
                retField = Field.is(fieldString);

            } else {
                if (colonPos == 0) {
                    // invalid position for colon
                    throw new RuntimeException("Cannot create field from data that starts with a :");

                } else {
                    if (fieldString.charAt(colonPos - 1) == '\\') {
                        // then it is not a property pair at this position
                        propertySplitPoint = colonPos+1;
                    } else {
                        // it is a property pair, split it out
                        String fieldName = fieldString.substring(0, colonPos);
                        String fieldType = fieldString.substring(colonPos + 1);
                        String[] fieldTypeData = fieldType.split(":");
                        retField =  new Field(fieldName, FieldType.valueOf(fieldTypeData[0]));
                        if(fieldTypeData.length>1){
                            // 0 is the type
                            // 1 is the default value
                            retField.withDefaultValue(fieldTypeData[1]);
                        }
                    }

                }
            }
        }

        // over ran so it must be just a name
        if(retField == null){
            retField = Field.is(fieldString);
        }

        return retField;
    }*/
}
