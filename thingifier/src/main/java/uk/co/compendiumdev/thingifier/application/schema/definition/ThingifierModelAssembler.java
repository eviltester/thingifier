package uk.co.compendiumdev.thingifier.application.schema.definition;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.VRule;

public final class ThingifierModelAssembler {

    public SchemaDefinitionValidationReport validate(final ThingifierModelDefinition definition) {
        final SchemaDefinitionValidationReport report = new SchemaDefinitionValidationReport();
        if (definition == null) {
            report.addError("model", "Model definition is required");
            return report;
        }

        if (definition.formatVersion() != 1) {
            report.addError("formatVersion", "Only formatVersion 1 is supported");
        }
        validateEntities(definition, report);
        validateRelationships(definition, report);
        return report;
    }

    public Thingifier assemble(final ThingifierModelDefinition definition) {
        final SchemaDefinitionValidationReport report = validate(definition);
        if (!report.isValid()) {
            throw new IllegalArgumentException(report.combinedMessages());
        }

        final Thingifier thingifier = new Thingifier();
        thingifier.setDocumentation(
                emptyIfNull(definition.title()), emptyIfNull(definition.description()));

        for (EntityDefinitionSpec entitySpec : definition.entities()) {
            thingifier.defineThing(
                    entitySpec.name(), entitySpec.pluralName(), entitySpec.maxInstances());
        }

        for (EntityDefinitionSpec entitySpec : definition.entities()) {
            final EntityDefinition entity = thingifier.getDefinitionNamed(entitySpec.name());
            for (FieldDefinitionSpec fieldSpec : entitySpec.fields()) {
                final Field field = createField(fieldSpec);
                if (fieldSpec.name().equals(entitySpec.primaryKeyFieldName())) {
                    entity.addAsPrimaryKeyField(field);
                } else {
                    entity.addField(field);
                }
            }
        }

        for (RelationshipDefinitionSpec relationshipSpec : definition.relationships()) {
            final EntityDefinition from =
                    thingifier.getDefinitionNamed(relationshipSpec.fromEntityName());
            final EntityDefinition to =
                    thingifier.getDefinitionNamed(relationshipSpec.toEntityName());
            final RelationshipDefinition relationship =
                    thingifier.defineRelationship(
                            from,
                            to,
                            relationshipSpec.name(),
                            cardinalityFor(relationshipSpec.cardinality()));
            applyOptionality(relationship.getFromRelationship(), relationshipSpec.optionality());
            if (relationshipSpec.hasReverse()) {
                relationship.whenReversed(
                        cardinalityFor(relationshipSpec.reverse().cardinality()),
                        relationshipSpec.reverse().name());
                applyOptionality(
                        relationship.getReversedRelationship(),
                        relationshipSpec.reverse().optionality());
            }
        }

        return thingifier;
    }

    private void validateEntities(
            final ThingifierModelDefinition definition,
            final SchemaDefinitionValidationReport report) {
        if (definition.entities().isEmpty()) {
            report.addError("entities", "At least one entity is required");
        }

        final Set<String> entityNames = new HashSet<>();
        final Set<String> pluralNames = new HashSet<>();
        for (EntityDefinitionSpec entity : definition.entities()) {
            final String entityPath = "entities." + emptyIfNull(entity.name());
            validateEntity(entity, entityPath, entityNames, pluralNames, report);
        }
    }

    private void validateEntity(
            final EntityDefinitionSpec entity,
            final String entityPath,
            final Set<String> entityNames,
            final Set<String> pluralNames,
            final SchemaDefinitionValidationReport report) {
        if (isBlank(entity.name())) {
            report.addError(entityPath + ".name", "Entity name is required");
        } else if (!entityNames.add(entity.name().toLowerCase(Locale.ROOT))) {
            report.addError(entityPath, "Duplicate entity name " + entity.name());
        }

        if (isBlank(entity.pluralName())) {
            report.addError(entityPath + ".plural", "Entity plural name is required");
        } else if (!pluralNames.add(entity.pluralName().toLowerCase(Locale.ROOT))) {
            report.addError(entityPath, "Duplicate entity plural " + entity.pluralName());
        }

        if (entity.maxInstances() < -1) {
            report.addError(entityPath + ".maxInstances", "maxInstances must be -1 or greater");
        }

        validateFields(entity, entityPath, report);
    }

    private void validateFields(
            final EntityDefinitionSpec entity,
            final String entityPath,
            final SchemaDefinitionValidationReport report) {
        final Set<String> fieldNames = new HashSet<>();
        for (FieldDefinitionSpec field : entity.fields()) {
            validateField(
                    field, entityPath + ".fields." + emptyIfNull(field.name()), fieldNames, report);
        }

        if (entity.hasPrimaryKeyField() && !fieldNames.contains(entity.primaryKeyFieldName())) {
            report.addError(
                    entityPath + ".primaryKey",
                    "Primary key field " + entity.primaryKeyFieldName() + " is not defined");
        }
    }

    private void validateField(
            final FieldDefinitionSpec field,
            final String fieldPath,
            final Set<String> siblingFieldNames,
            final SchemaDefinitionValidationReport report) {
        if (isBlank(field.name())) {
            report.addError(fieldPath + ".name", "Field name is required");
        } else if (!siblingFieldNames.add(field.name())) {
            report.addError(fieldPath, "Duplicate field name " + field.name());
        }

        final FieldType fieldType = fieldTypeFor(field.type());
        if (fieldType == null) {
            report.addError(fieldPath + ".type", "Unsupported field type " + field.type());
            return;
        }

        validateFieldConfiguration(field, fieldPath, fieldType, report);
    }

    private void validateFieldConfiguration(
            final FieldDefinitionSpec field,
            final String fieldPath,
            final FieldType fieldType,
            final SchemaDefinitionValidationReport report) {
        if (fieldType == FieldType.ENUM && field.examples().isEmpty()) {
            report.addError(fieldPath + ".examples", "Enum fields require examples");
        }

        if (fieldType == FieldType.OBJECT) {
            final Set<String> childNames = new HashSet<>();
            for (FieldDefinitionSpec child : field.objectFields()) {
                validateField(
                        child,
                        fieldPath + ".fields." + emptyIfNull(child.name()),
                        childNames,
                        report);
            }
        } else if (!field.objectFields().isEmpty()) {
            report.addError(fieldPath + ".fields", "Only object fields can define child fields");
        }

        validateRange(field, fieldPath, fieldType, report);
        validateRules(field, fieldPath, report);
    }

    private void validateRange(
            final FieldDefinitionSpec field,
            final String fieldPath,
            final FieldType fieldType,
            final SchemaDefinitionValidationReport report) {
        if (!field.hasRange()) {
            return;
        }

        if (field.minValue() == null || field.maxValue() == null) {
            report.addError(fieldPath, "Both min and max are required when defining a range");
            return;
        }

        if (fieldType == FieldType.INTEGER) {
            validateIntegerRange(field, fieldPath, report);
            return;
        }

        if (fieldType == FieldType.FLOAT) {
            validateFloatRange(field, fieldPath, report);
            return;
        }

        report.addError(fieldPath, "Only integer and float fields support min and max");
    }

    private void validateIntegerRange(
            final FieldDefinitionSpec field,
            final String fieldPath,
            final SchemaDefinitionValidationReport report) {
        try {
            final int minimum = Integer.parseInt(field.minValue());
            final int maximum = Integer.parseInt(field.maxValue());
            if (minimum > maximum) {
                report.addError(fieldPath, "min must be less than or equal to max");
            }
        } catch (NumberFormatException e) {
            report.addError(fieldPath, "Integer min and max must be whole numbers");
        }
    }

    private void validateFloatRange(
            final FieldDefinitionSpec field,
            final String fieldPath,
            final SchemaDefinitionValidationReport report) {
        try {
            final float minimum = Float.parseFloat(field.minValue());
            final float maximum = Float.parseFloat(field.maxValue());
            if (minimum > maximum) {
                report.addError(fieldPath, "min must be less than or equal to max");
            }
        } catch (NumberFormatException e) {
            report.addError(fieldPath, "Float min and max must be numeric");
        }
    }

    private void validateRules(
            final FieldDefinitionSpec field,
            final String fieldPath,
            final SchemaDefinitionValidationReport report) {
        for (ValidationRuleSpec rule : field.validationRules()) {
            if (ValidationRuleSpec.NOT_EMPTY.equals(rule.name())) {
                continue;
            }
            if (ValidationRuleSpec.MAXIMUM_LENGTH.equals(rule.name())) {
                validateMaximumLength(rule, fieldPath, report);
                continue;
            }
            if (ValidationRuleSpec.MATCHES_REGEX.equals(rule.name())
                    || ValidationRuleSpec.SATISFIES_REGEX.equals(rule.name())) {
                validateRegex(rule, fieldPath, report);
                continue;
            }
            report.addError(
                    fieldPath + ".validations", "Unsupported validation rule " + rule.name());
        }
    }

    private void validateMaximumLength(
            final ValidationRuleSpec rule,
            final String fieldPath,
            final SchemaDefinitionValidationReport report) {
        try {
            if (Integer.parseInt(rule.value()) < 0) {
                report.addError(
                        fieldPath + ".validations", "maximumLength must be zero or greater");
            }
        } catch (NumberFormatException e) {
            report.addError(fieldPath + ".validations", "maximumLength must be a whole number");
        }
    }

    private void validateRegex(
            final ValidationRuleSpec rule,
            final String fieldPath,
            final SchemaDefinitionValidationReport report) {
        try {
            Pattern.compile(rule.value());
        } catch (PatternSyntaxException | NullPointerException e) {
            report.addError(fieldPath + ".validations", rule.name() + " must define a valid regex");
        }
    }

    private void validateRelationships(
            final ThingifierModelDefinition definition,
            final SchemaDefinitionValidationReport report) {
        for (int index = 0; index < definition.relationships().size(); index++) {
            final RelationshipDefinitionSpec relationship = definition.relationships().get(index);
            final String path = "relationships[" + index + "]";
            if (definition.entityNamed(relationship.fromEntityName()) == null) {
                report.addError(
                        path + ".from",
                        "Unknown relationship source " + relationship.fromEntityName());
            }
            if (definition.entityNamed(relationship.toEntityName()) == null) {
                report.addError(
                        path + ".to", "Unknown relationship target " + relationship.toEntityName());
            }
            if (isBlank(relationship.name())) {
                report.addError(path + ".name", "Relationship name is required");
            }
            validateCardinality(relationship.cardinality(), path + ".cardinality", report);
            validateOptionality(relationship.optionality(), path + ".optionality", report);
            if (relationship.hasReverse()) {
                validateReverse(relationship.reverse(), path + ".reverse", report);
            }
        }
    }

    private void validateReverse(
            final RelationshipVectorSpec reverse,
            final String path,
            final SchemaDefinitionValidationReport report) {
        if (isBlank(reverse.name())) {
            report.addError(path + ".name", "Reverse relationship name is required");
        }
        validateCardinality(reverse.cardinality(), path + ".cardinality", report);
        validateOptionality(reverse.optionality(), path + ".optionality", report);
    }

    private void validateCardinality(
            final CardinalitySpec cardinality,
            final String path,
            final SchemaDefinitionValidationReport report) {
        if (cardinality == null) {
            return;
        }
        try {
            CardinalitySpec.fromText(cardinality.canonicalName());
        } catch (IllegalArgumentException e) {
            report.addError(path, e.getMessage());
        }
    }

    private void validateOptionality(
            final String optionality,
            final String path,
            final SchemaDefinitionValidationReport report) {
        if (optionality == null || optionality.trim().isEmpty()) {
            return;
        }
        final String normalized = optionality.trim().toLowerCase(Locale.ROOT);
        if (!"optional".equals(normalized) && !"mandatory".equals(normalized)) {
            report.addError(path, "Optionality must be optional or mandatory");
        }
    }

    private Field createField(final FieldDefinitionSpec fieldSpec) {
        final Field field = Field.is(fieldSpec.name(), fieldTypeFor(fieldSpec.type()));
        if (fieldSpec.required()) {
            field.makeMandatory();
        }
        if (fieldSpec.unique()) {
            field.setMustBeUnique(true);
        }
        if (fieldSpec.defaultValue() != null) {
            field.withDefaultValue(fieldSpec.defaultValue());
        }
        if (fieldSpec.description() != null) {
            field.withDescription(fieldSpec.description());
        }
        for (String example : fieldSpec.examples()) {
            field.withExample(example);
        }
        if (fieldSpec.truncateTo() != null) {
            field.truncateStringTo(fieldSpec.truncateTo());
        }
        applyRange(field, fieldSpec);
        applyValidationRules(field, fieldSpec);
        for (FieldDefinitionSpec child : fieldSpec.objectFields()) {
            field.withField(createField(child));
        }
        return field;
    }

    private void applyRange(final Field field, final FieldDefinitionSpec fieldSpec) {
        if (!fieldSpec.hasRange()) {
            return;
        }
        if (field.getType() == FieldType.INTEGER) {
            field.withMinMaxValues(
                    Integer.parseInt(fieldSpec.minValue()), Integer.parseInt(fieldSpec.maxValue()));
        }
        if (field.getType() == FieldType.FLOAT) {
            field.withMinMaxValues(
                    Float.parseFloat(fieldSpec.minValue()), Float.parseFloat(fieldSpec.maxValue()));
        }
    }

    private void applyValidationRules(final Field field, final FieldDefinitionSpec fieldSpec) {
        for (ValidationRuleSpec rule : fieldSpec.validationRules()) {
            if (ValidationRuleSpec.NOT_EMPTY.equals(rule.name())) {
                field.withValidation(VRule.notEmpty());
            }
            if (ValidationRuleSpec.MAXIMUM_LENGTH.equals(rule.name())) {
                field.withValidation(VRule.maximumLength(Integer.parseInt(rule.value())));
            }
            if (ValidationRuleSpec.MATCHES_REGEX.equals(rule.name())) {
                field.withValidation(VRule.matchesRegex(rule.value()));
            }
            if (ValidationRuleSpec.SATISFIES_REGEX.equals(rule.name())) {
                field.withValidation(VRule.satisfiesRegex(rule.value()));
            }
        }
    }

    private Cardinality cardinalityFor(final CardinalitySpec spec) {
        final CardinalitySpec cardinality = spec == null ? CardinalitySpec.oneToMany() : spec;
        return new Cardinality(cardinality.left(), cardinality.right());
    }

    private void applyOptionality(
            final RelationshipVectorDefinition relationship, final String optionality) {
        relationship.setOptionality(optionalityFor(optionality));
    }

    private Optionality optionalityFor(final String optionality) {
        if (optionality == null || !"mandatory".equalsIgnoreCase(optionality.trim())) {
            return Optionality.OPTIONAL_RELATIONSHIP;
        }
        return Optionality.MANDATORY_RELATIONSHIP;
    }

    private FieldType fieldTypeFor(final String type) {
        if (type == null) {
            return null;
        }
        final String normalized = type.trim().toLowerCase(Locale.ROOT).replace("-", "_");
        try {
            return FieldType.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isBlank(final String text) {
        return text == null || text.trim().isEmpty();
    }

    private String emptyIfNull(final String text) {
        return text == null ? "" : text;
    }
}
