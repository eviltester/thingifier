package uk.co.compendiumdev.thingifier.generic.definitions.validation;

public interface ValidationRule {

    boolean validates(String value);

    String getErrorMessage(String fieldName);
}
