package uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import uk.co.compendiumdev.thingifier.core.domain.definitions.DefinedFields;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.*;
import uk.co.compendiumdev.thingifier.core.domain.randomdata.RandomString;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public final class Field {

    private final String name;
    private final FieldType type;
    private final Set<String> fieldExamples;
    private final Set<String> configuredExamples;
    private boolean fieldIsOptional;

    // default value for the field
    private String defaultValue;
    private final List<ValidationRule> validationRules;
    private final List<FieldValidator> customValidators;
    private boolean truncateStringIfTooLong;

    private final boolean allowedNullable;

    private ValidationRule typeValidationRule;

    private DefinedFields objectDefinition;

    private boolean mustBeUnique;

    private int truncatedStringLength;
    private Function<String, String> transformToMakeUnique;
    private String description;
    private FieldRelationshipReference relationshipReference;

    // todo: rather than all these fields, consider moving to more validation rules
    // to help keep the class to a more manageable size or create a FieldValidator class

    public Field(final String name, final FieldType type) {
        this.name = name;
        this.type = type;
        validationRules = new ArrayList<>();
        customValidators = new ArrayList<>();

        fieldIsOptional = true;
        if (type == FieldType.AUTO_INCREMENT || type == FieldType.AUTO_GUID) {
            fieldIsOptional = false;
            typeValidationRule = new FieldAutoIncrementValidationRule();
        }

        truncateStringIfTooLong = false;
        truncatedStringLength = -1;
        fieldExamples = new HashSet<>();
        configuredExamples = new HashSet<>();

        if (type == FieldType.INTEGER) {
            typeValidationRule = new IntegerValidationRule();
        }

        if (type == FieldType.FLOAT) {
            typeValidationRule = new FloatValidationRule();
        }

        if (type == FieldType.BOOLEAN) {
            typeValidationRule = new BooleanValidationRule();
        }

        if (type == FieldType.ENUM) {
            typeValidationRule = new EnumValidationRule(getExamples());
        }

        mustBeUnique = false;
        allowedNullable = false;

        description = "";
        transformToMakeUnique = (s) -> s;
        relationshipReference = null;
    }

    public static Field is(String name, FieldType type) {
        return new Field(name, type);
    }

    public String getName() {
        return name;
    }

    public Field withDefaultValue(String aDefaultValue) {
        this.defaultValue = aDefaultValue;
        fieldExamples.add(aDefaultValue);
        return this;
    }

    public FieldValue getDefaultValue() {
        // todo: allow configuration of allowedNullable
        // todo: handle defaults of object and array
        if (defaultValue == null && !allowedNullable) {
            // get the definition default
            return FieldValue.is(this, type.getDefault());
        }
        return FieldValue.is(this, defaultValue);
    }

    public boolean hasDefaultValue() {
        return defaultValue != null;
    }

    public Field setMustBeUnique(boolean uniqueness) {
        this.mustBeUnique = uniqueness;
        return this;
    }

    public boolean mustBeUnique() {
        return this.mustBeUnique;
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

    /**
     * Adds a code-only custom validator for this field.
     *
     * <p>Custom validators run after built-in field checks and standard {@link ValidationRule}
     * checks have passed. This keeps custom code focused on business rules and prevents it from
     * seeing values that Thingifier already knows are structurally invalid.
     *
     * @param validationRule validator to add to this field
     * @return this field for fluent model setup
     */
    public Field withCustomValidation(FieldValidator validationRule) {
        customValidators.add(validationRule);
        return this;
    }

    /**
     * Adds code-only custom validators for this field.
     *
     * @param validationRule validators to add to this field
     * @return this field for fluent model setup
     */
    public Field withCustomValidation(FieldValidator... validationRule) {
        customValidators.addAll(Arrays.asList(validationRule));
        return this;
    }

    /**
     * Validates a field value for user-supplied writes.
     *
     * <p>This is the default field validation entry point. It rejects caller-supplied protected ID
     * values and runs the complete field pipeline: built-in structural/type/object checks, standard
     * {@link ValidationRule} checks, and then custom field validators.
     *
     * @param value value to validate, or null when the field is absent from the candidate
     * @return validation report for the field value
     */
    public ValidationReport validate(FieldValue value) {
        return validateWithIdPolicy(value, IdWritePolicy.REJECT_SUPPLIED_IDS);
    }

    /**
     * Validates a field value for trusted repository/system writes.
     *
     * <p>Protected writes allow generated or restored ID values while still running the complete
     * field pipeline. This is intended for internal materialization, imports, and repository paths
     * that legitimately need to carry protected fields.
     *
     * @param value value to validate, or null when the field is absent from the candidate
     * @return validation report for the field value
     */
    public ValidationReport validateForProtectedWrite(FieldValue value) {
        return validateWithIdPolicy(value, IdWritePolicy.ALLOW_SUPPLIED_IDS);
    }

    private ValidationReport validateWithIdPolicy(FieldValue value, IdWritePolicy idWritePolicy) {
        ValidationReport report = validateBuiltInWithIdPolicy(value, idWritePolicy);
        if (!report.isValid()) {
            return report;
        }
        report.combine(validateCustom(value));
        return report;
    }

    /**
     * Runs only built-in and standard field validation for a user-supplied write.
     *
     * <p>This is used by the repository write pipeline so uniqueness can be checked before custom
     * field validators run.
     *
     * @param value value to validate, or null when the field is absent from the candidate
     * @return validation report for built-in field checks
     */
    public ValidationReport validateBuiltInForNormalWrite(FieldValue value) {
        return validateBuiltInWithIdPolicy(value, IdWritePolicy.REJECT_SUPPLIED_IDS);
    }

    /**
     * Runs only built-in and standard field validation for a trusted repository/system write.
     *
     * <p>This supports validation of already generated or restored protected IDs without running
     * custom validators before the ordered repository pipeline reaches that stage.
     *
     * @param value value to validate, or null when the field is absent from the candidate
     * @return validation report for built-in field checks
     */
    public ValidationReport validateBuiltInForProtectedWrite(FieldValue value) {
        return validateBuiltInWithIdPolicy(value, IdWritePolicy.ALLOW_SUPPLIED_IDS);
    }

    private ValidationReport validateBuiltInWithIdPolicy(
            FieldValue value, IdWritePolicy idWritePolicy) {

        ValidationReport report = new ValidationReport();

        // missing fields will come through as null,
        // if they are optional then that is fine
        if (fieldIsOptional && value == null) {
            report.setValid(true);
            return report;
        }

        if (idWritePolicy == IdWritePolicy.REJECT_SUPPLIED_IDS) {
            if (type == FieldType.AUTO_INCREMENT) {
                report.setValid(false);
                report.addErrorMessage(
                        String.format("%s : field is an ID, you can't set it", this.getName()));
                return report;
            }
        }

        if (!fieldIsOptional && value == null) {
            report.setValid(false);
            report.addErrorMessage(String.format("%s : field is mandatory", this.getName()));
            return report;
        }

        // always validate against type
        // if(shouldValidateValuesAgainstType) {

        switch (type) {
            case BOOLEAN:
            case INTEGER:
            case FLOAT:
            case ENUM:
                if (typeValidationRule != null) {
                    validateAgainstRule(value, typeValidationRule, report);
                }
                break;
            default:
                break;
        }

        if (type == FieldType.STRING) {
            // length is validated by a rule
        }

        // TODO : add validation for DATE

        if (type == FieldType.OBJECT) {
            validateObjectValue(value, report);
        }

        // }

        for (ValidationRule rule : validationRules) {
            validateAgainstRule(value, rule, report);
        }

        return report;
    }

    /**
     * Runs only code-defined custom validators for this field.
     *
     * <p>Callers should use this after built-in field validation has passed. The repository write
     * pipeline keeps this separate so it can enforce stage ordering and short-circuit behavior.
     *
     * @param value value to validate, or null when the field is absent from the candidate
     * @return validation report for custom field validators
     */
    public ValidationReport validateCustom(final FieldValue value) {
        ValidationReport report = new ValidationReport();
        if (value == null) {
            return report;
        }
        if (type == FieldType.OBJECT && value.asObject() != null) {
            report.combine(value.asObject().validateCustomFields(new ArrayList<>()));
        }
        for (FieldValidator validator : customValidators) {
            ValidationReport validity = validator.validate(value);
            if (validity != null) {
                report.combine(validity);
            }
        }
        return report;
    }

    public List<ValidationRule> getAllValidationRules() {
        List<ValidationRule> rules = new ArrayList<>();

        if (typeValidationRule != null) {
            rules.add(typeValidationRule);
        }

        if (type != FieldType.AUTO_INCREMENT && !fieldIsOptional) {
            rules.add(new MandatoryValidationRule());
        }

        // TODO : add validation for DATE

        if (type == FieldType.OBJECT) {
            // TODO: validation rule for object
            // validateObjectValue(value, report);
        }

        rules.addAll(validationRules);
        return rules;
    }

    private void validateAgainstRule(
            FieldValue value, ValidationRule rule, ValidationReport report) {
        if (!rule.validates(value)) {
            report.setValid(false);
            report.addErrorMessage(rule.getErrorMessage(value));
        }
    }

    private void validateObjectValue(final FieldValue value, final ValidationReport report) {
        if (value != null && value.asObject() != null) {
            final ValidationReport objectValidity =
                    value.asObject().validateBuiltInFieldsForProtectedWrite(new ArrayList<>());
            report.combine(objectValidity);
        }
    }

    public List<ValidationRule> validationRules() {
        return validationRules;
    }

    /**
     * Returns custom validators attached to this field.
     *
     * <p>These validators are intentionally excluded from model export/import because functions
     * cannot safely round-trip through YAML or schema metadata.
     *
     * @return immutable list of custom field validators
     */
    public List<FieldValidator> customValidators() {
        return Collections.unmodifiableList(customValidators);
    }

    public boolean isMandatory() {
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
        truncatedStringLength = maximumTruncatedLengthOfString;
        return this;
    }

    public boolean shouldTruncate() {
        return truncateStringIfTooLong;
    }

    public String getAsTruncatedString(FieldValue value) {
        String truncated = value.asString();
        if (truncateStringIfTooLong && truncatedStringLength > -1) {
            if (truncated.length() > truncatedStringLength) {
                truncated = truncated.substring(0, truncatedStringLength);
            }
        }
        return truncated;
    }

    public Field withExample(final String anExample) {
        configuredExamples.add(anExample);
        fieldExamples.add(anExample);
        if (type == FieldType.ENUM) {
            typeValidationRule = new EnumValidationRule(getExamples());
        }
        return this;
    }

    public List<String> getExamples() {

        // field might have examples in definition, if it does, use them
        if (!fieldExamples.isEmpty()) {
            return new ArrayList<>(fieldExamples);
        }

        Set<String> buildExamples = new HashSet<>();

        if (type == FieldType.BOOLEAN) {
            String[] samples = {"true", "false"};
            buildExamples.addAll(Arrays.asList(samples));
        }

        if (type == FieldType.INTEGER) {
            IntegerValidationRule rule = (IntegerValidationRule) typeValidationRule;
            int min = Integer.MIN_VALUE;
            int max = Integer.MAX_VALUE;

            if (rule.getMinimumIntegerValue() != null) {
                min = rule.getMinimumIntegerValue();
                max = rule.getMaximumIntegerValue();
            }
            int rndInt = ThreadLocalRandom.current().nextInt(min, max + 1);
            buildExamples.add(String.valueOf(rndInt));
        }

        if (type == FieldType.AUTO_INCREMENT) {
            int rndInt = ThreadLocalRandom.current().nextInt(1, 100);
            buildExamples.add(String.valueOf(rndInt));
        }

        if (type == FieldType.AUTO_GUID) {
            buildExamples.add(UUID.randomUUID().toString());
        }

        if (type == FieldType.FLOAT) {
            FloatValidationRule rule = (FloatValidationRule) typeValidationRule;
            Float min = 0.0F;
            Float max = 100.0F;
            if (rule.getMinimumFloatValue() != null) {
                min = rule.getMinimumFloatValue();
                max = rule.getMaximumFloatValue();
            }
            final float rndFloat = min + ThreadLocalRandom.current().nextFloat() * (max - min);
            buildExamples.add(String.valueOf(rndFloat));
        }

        // TODO: try to use regex in matching rules to generate
        if (type == FieldType.STRING) {
            if (fieldExamples.isEmpty()) {
                buildExamples.add(getAsTruncatedString(valueFor(new RandomString().get(20))));
            }
        }

        // return as a list
        return new ArrayList<>(buildExamples);
    }

    public List<String> getConfiguredExamples() {
        return new ArrayList<>(configuredExamples);
    }

    public String getConfiguredDefaultValue() {
        return defaultValue;
    }

    public int getTruncatedStringLength() {
        return truncatedStringLength;
    }

    public ValidationRule getTypeValidationRule() {
        return typeValidationRule;
    }

    public String getRandomExampleValue() {
        final List<String> examples = getExamples();

        if (examples.isEmpty()) {
            return "";
        }

        return examples.get(new Random().nextInt(examples.size()));
    }

    public Field withMinMaxValues(final float minFloat, final float maxFloat) {
        if (type == FieldType.FLOAT) {
            if (maxFloat >= minFloat) {
                typeValidationRule = new FloatValidationRule(minFloat, maxFloat);
            } else {
                throw new IllegalArgumentException(
                        "Attempt to create Float field with minimum %f > %f maximum"
                                .formatted(minFloat, maxFloat));
            }
        }
        return this;
    }

    public Field withField(final Field childField) {
        if (objectDefinition == null) {
            objectDefinition = new DefinedFields();
        }
        objectDefinition.addField(childField);
        return this;
    }

    public DefinedFields getObjectDefinition() {
        return objectDefinition;
    }

    public String getActualValueToAdd(final FieldValue value) {

        switch (type) {
            case BOOLEAN:
                return Boolean.valueOf(value.asString()).toString();
            case FLOAT:
                return Float.valueOf(value.asString()).toString();
            case STRING:
                if (shouldTruncate()) {
                    return getAsTruncatedString(value);
                } else {
                    return value.asString();
                }
            case INTEGER:
            case AUTO_INCREMENT:
                // TODO: integer field uses BigDecimal to do this - check for inconsistency in
                // result
                Double dVal = Double.parseDouble(value.asString());
                return String.valueOf(dVal.intValue());
            case AUTO_GUID:
            case OBJECT:
            case ENUM:
            case DATE:
                return value.asString();
            default:
                System.out.println("Unhandled value to add on set");
                return value.asString();
        }
    }

    public FieldValue valueFor(String value) {
        return FieldValue.is(this, value);
    }

    public Field setUniqueAfterTransform(Function<String, String> transform) {
        setMustBeUnique(true);
        transformToMakeUnique = transform;
        return this;
    }

    public String uniqueAfterTransform(String string) {
        try {
            return transformToMakeUnique.apply(string);
        } catch (Exception e) {
            return "ERROR: " + string + " " + e.getMessage();
        }
    }

    public Field withDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean hasDescription() {
        return this.description != null && !description.isEmpty();
    }

    public String getDescription() {
        return description;
    }

    /**
     * Declares that this field references another entity through a named relationship.
     *
     * <p>The field remains part of the source entity's data. During write processing, Thingifier
     * resolves the supplied field value against {@code targetFieldName} on {@code targetEntity} and
     * connects the source instance through {@code relationshipName}.
     *
     * @param targetEntity entity containing the referenced value
     * @param targetFieldName field on the target entity to match
     * @param relationshipName relationship from this field's owning entity to the target entity
     * @return this field so definition code can be chained
     */
    public Field references(
            final EntityDefinition targetEntity,
            final String targetFieldName,
            final String relationshipName) {
        relationshipReference =
                FieldRelationshipReference.to(targetEntity, targetFieldName, relationshipName);
        return this;
    }

    /**
     * Reports whether this field declares a relationship reference.
     *
     * @return true when writes to the field should also maintain a relationship
     */
    public boolean hasRelationshipReference() {
        return relationshipReference != null;
    }

    /**
     * Returns this field's relationship reference metadata.
     *
     * @return relationship reference metadata, or null when none is configured
     */
    public FieldRelationshipReference relationshipReference() {
        return relationshipReference;
    }

    public Field withMinMaxValues(int minInt, int maxInt) {
        if (type == FieldType.INTEGER) {
            if (maxInt >= minInt) {
                typeValidationRule = new IntegerValidationRule(minInt, maxInt);
            } else {
                throw new IllegalArgumentException(
                        "Attempt to create Integer field with minimum %d > %d maximum"
                                .formatted(minInt, maxInt));
            }
        }
        return this;
    }

    private enum IdWritePolicy {
        REJECT_SUPPLIED_IDS,
        ALLOW_SUPPLIED_IDS
    }
}
