package uk.co.compendiumdev.thingifier.application.schema.definition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.DefinedFields;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.EnumValidationRule;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.FindsRegexValidationRule;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.FloatValidationRule;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.IntegerValidationRule;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.MatchesRegexValidationRule;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.MaximumLengthValidationRule;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.NotEmptyValidationRule;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.ValidationRule;

public final class ThingifierModelExporter {

    public ThingifierModelDefinition export(final Thingifier thingifier) {
        final ThingifierModelDefinition.Builder builder =
                ThingifierModelDefinition.builder()
                        .formatVersion(1)
                        .title(thingifier.getTitle())
                        .description(thingifier.getInitialParagraph());

        for (EntityDefinition entity : sortedEntities(thingifier)) {
            builder.entity(entitySpecFor(entity));
        }
        for (RelationshipDefinition relationship : sortedRelationships(thingifier)) {
            builder.relationship(relationshipSpecFor(relationship));
        }
        return builder.build();
    }

    private List<EntityDefinition> sortedEntities(final Thingifier thingifier) {
        final List<EntityDefinition> entities =
                new ArrayList<>(thingifier.getERmodel().getSchema().getEntityDefinitions());
        entities.sort(Comparator.comparing(EntityDefinition::getName));
        return entities;
    }

    private EntityDefinitionSpec entitySpecFor(final EntityDefinition entity) {
        final EntityDefinitionSpec.Builder builder =
                EntityDefinitionSpec.named(entity.getName())
                        .plural(entity.getPlural())
                        .maxInstances(entity.getMaxInstanceLimit());
        if (entity.hasPrimaryKeyField()) {
            builder.primaryKey(entity.getPrimaryKeyField().getName());
        }
        for (String fieldName : entity.getFieldNames()) {
            builder.field(fieldSpecFor(entity.getField(fieldName)));
        }
        return builder.build();
    }

    private FieldDefinitionSpec fieldSpecFor(final Field field) {
        final FieldDefinitionSpec.Builder builder =
                FieldDefinitionSpec.named(field.getName(), typeNameFor(field.getType()))
                        .unique(field.mustBeUnique());
        if (field.isMandatory() && !isAutoField(field)) {
            builder.required(true);
        }
        if (field.getConfiguredDefaultValue() != null) {
            builder.defaultValue(field.getConfiguredDefaultValue());
        }
        if (field.hasDescription()) {
            builder.description(field.getDescription());
        }
        builder.examples(configuredExamplesFor(field));
        if (field.shouldTruncate()) {
            builder.truncateTo(field.getTruncatedStringLength());
        }
        applyRange(builder, field);
        for (ValidationRule rule : field.validationRules()) {
            addRule(builder, rule);
        }
        if (field.getObjectDefinition() != null) {
            addObjectFields(builder, field.getObjectDefinition());
        }
        return builder.build();
    }

    private List<String> configuredExamplesFor(final Field field) {
        final List<String> examples = new ArrayList<>(field.getConfiguredExamples());
        if (field.getType() == FieldType.ENUM && examples.isEmpty()) {
            final ValidationRule rule = field.getTypeValidationRule();
            if (rule instanceof EnumValidationRule) {
                examples.addAll(((EnumValidationRule) rule).getValidValues());
            }
        }
        examples.sort(String::compareTo);
        return examples;
    }

    private void addObjectFields(
            final FieldDefinitionSpec.Builder builder, final DefinedFields objectDefinition) {
        for (String childName : objectDefinition.getFieldNames()) {
            builder.objectField(fieldSpecFor(objectDefinition.getField(childName)));
        }
    }

    private void applyRange(final FieldDefinitionSpec.Builder builder, final Field field) {
        final ValidationRule typeRule = field.getTypeValidationRule();
        if (typeRule instanceof IntegerValidationRule) {
            final IntegerValidationRule integerRule = (IntegerValidationRule) typeRule;
            if (integerRule.getMinimumIntegerValue() != null) {
                builder.range(
                        String.valueOf(integerRule.getMinimumIntegerValue()),
                        String.valueOf(integerRule.getMaximumIntegerValue()));
            }
        }
        if (typeRule instanceof FloatValidationRule) {
            final FloatValidationRule floatRule = (FloatValidationRule) typeRule;
            if (floatRule.getMinimumFloatValue() != null) {
                builder.range(
                        String.valueOf(floatRule.getMinimumFloatValue()),
                        String.valueOf(floatRule.getMaximumFloatValue()));
            }
        }
    }

    private void addRule(final FieldDefinitionSpec.Builder builder, final ValidationRule rule) {
        if (rule instanceof NotEmptyValidationRule) {
            builder.validationRule(ValidationRuleSpec.notEmpty());
        }
        if (rule instanceof MaximumLengthValidationRule) {
            builder.validationRule(
                    ValidationRuleSpec.maximumLength(
                            ((MaximumLengthValidationRule) rule).getMaximumLength()));
        }
        if (rule instanceof MatchesRegexValidationRule) {
            builder.validationRule(
                    ValidationRuleSpec.matchesRegex(
                            ((MatchesRegexValidationRule) rule).getRegexToMatch()));
        }
        if (rule instanceof FindsRegexValidationRule) {
            builder.validationRule(
                    ValidationRuleSpec.satisfiesRegex(
                            ((FindsRegexValidationRule) rule).getRegexToFind()));
        }
    }

    private List<RelationshipDefinition> sortedRelationships(final Thingifier thingifier) {
        final List<RelationshipDefinition> relationships =
                new ArrayList<>(thingifier.getRelationshipDefinitions());
        relationships.sort(
                Comparator.comparing(
                        relationship ->
                                relationship.getFromRelationship().getFrom().getName()
                                        + "/"
                                        + relationship.getFromRelationship().getName()
                                        + "/"
                                        + relationship.getFromRelationship().getTo().getName()));
        return relationships;
    }

    private RelationshipDefinitionSpec relationshipSpecFor(
            final RelationshipDefinition relationship) {
        final RelationshipVectorDefinition forward = relationship.getFromRelationship();
        final RelationshipVectorSpec reverse =
                relationship.isTwoWay()
                        ? vectorSpecFor(relationship.getReversedRelationship())
                        : null;
        return new RelationshipDefinitionSpec(
                forward.getFrom().getName(),
                forward.getName(),
                forward.getTo().getName(),
                cardinalitySpecFor(forward.getCardinality()),
                optionalityNameFor(forward.getOptionality()),
                reverse);
    }

    private RelationshipVectorSpec vectorSpecFor(final RelationshipVectorDefinition vector) {
        return new RelationshipVectorSpec(
                vector.getName(),
                cardinalitySpecFor(vector.getCardinality()),
                optionalityNameFor(vector.getOptionality()));
    }

    private CardinalitySpec cardinalitySpecFor(final Cardinality cardinality) {
        return new CardinalitySpec(cardinality.left(), cardinality.right());
    }

    private String optionalityNameFor(final Optionality optionality) {
        return optionality == Optionality.MANDATORY_RELATIONSHIP ? "mandatory" : "optional";
    }

    private boolean isAutoField(final Field field) {
        return field.getType() == FieldType.AUTO_INCREMENT
                || field.getType() == FieldType.AUTO_GUID;
    }

    private String typeNameFor(final FieldType type) {
        return type.name().toLowerCase(Locale.ROOT).replace("_", "-");
    }
}
