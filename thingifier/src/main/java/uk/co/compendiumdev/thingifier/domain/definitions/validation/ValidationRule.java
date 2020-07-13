package uk.co.compendiumdev.thingifier.domain.definitions.validation;

public interface ValidationRule {

    boolean validates(String value);

    String getErrorMessage(String fieldName);
}
