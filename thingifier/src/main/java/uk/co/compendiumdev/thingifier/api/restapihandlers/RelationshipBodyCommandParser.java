package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.application.command.RelationshipReference;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public final class RelationshipBodyCommandParser {

    private final SchemaCatalog schema;

    public RelationshipBodyCommandParser(final Thingifier thingifier) {
        this(new ThingifierSchemaCatalog(thingifier));
    }

    RelationshipBodyCommandParser(final SchemaCatalog schema) {
        this.schema = schema;
    }

    public RelationshipBodyCommands parse(
            final BodyParser bodyargs, final EntityDefinition entity) {
        return parse(bodyargs.getFlattenedStringMap(), entity);
    }

    public RelationshipBodyCommands parse(
            final List<Map.Entry<String, String>> flattenedArgs, final EntityDefinition entity) {
        List<Map.Entry<String, String>> relationshipEntries = new ArrayList<>();
        List<RelationshipReference> references = new ArrayList<>();
        ValidationReport report = new ValidationReport();

        for (Map.Entry<String, String> keyValue : flattenedArgs) {
            String key = keyValue.getKey();
            if (key.startsWith("relationships.")) {
                relationshipEntries.add(keyValue);
                parseComplexRelationship(entity, keyValue, report, references);
            } else if (key.contains(".")) {
                String[] parts = key.split("\\.");
                if (parts.length > 0 && entity.related().hasRelationship(parts[0])) {
                    relationshipEntries.add(keyValue);
                    parseCompressedRelationship(entity, keyValue, report, references);
                }
            }
        }

        report.setValid(report.getErrorMessages().isEmpty());
        return new RelationshipBodyCommands(relationshipEntries, references, report);
    }

    private void parseCompressedRelationship(
            final EntityDefinition entity,
            final Map.Entry<String, String> keyValue,
            final ValidationReport report,
            final List<RelationshipReference> references) {
        String[] parts = keyValue.getKey().split("\\.");
        if (parts.length != 2) {
            reportIsNotValidRelationship(keyValue.getKey(), report);
            return;
        }

        String relationshipName = parts[0];
        String fieldName = parts[1];
        if (!isValidRelationship(entity, relationshipName, report)) {
            return;
        }

        if (!supportsReferenceField(entity, relationshipName, fieldName)) {
            report.addErrorMessage(
                    String.format("Do not support relationship references using %s", fieldName));
            return;
        }

        references.add(
                RelationshipReference.compressed(relationshipName, fieldName, keyValue.getValue()));
    }

    private void parseComplexRelationship(
            final EntityDefinition entity,
            final Map.Entry<String, String> keyValue,
            final ValidationReport report,
            final List<RelationshipReference> references) {
        String[] parts = keyValue.getKey().split("\\.");
        if (parts.length != 4) {
            reportIsNotValidRelationship(keyValue.getKey(), report);
            return;
        }

        String relationshipName = parts[1];
        String relationshipToTerm = parts[2];
        String relationshipField = parts[3];

        if (!isValidRelationship(entity, relationshipName, report)) {
            return;
        }

        EntityDefinition relationshipTo =
                schema.definitionWithSingularOrPluralNamed(relationshipToTerm);
        if (relationshipTo == null
                || !validRelationshipBetweenThings(
                        entity, relationshipName, relationshipTo, report)) {
            return;
        }

        references.add(
                RelationshipReference.explicit(
                        relationshipName,
                        relationshipTo,
                        relationshipToTerm,
                        relationshipField,
                        keyValue.getValue()));
    }

    private boolean supportsReferenceField(
            final EntityDefinition entity, final String relationshipName, final String fieldName) {
        for (RelationshipVectorDefinition vector :
                entity.related().getRelationships(relationshipName)) {
            List<String> linkingFields =
                    vector.getTo()
                            .getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
            if (linkingFields.contains(fieldName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidRelationship(
            final EntityDefinition entity,
            final String relationshipName,
            final ValidationReport report) {
        if (!entity.related().hasRelationship(relationshipName)) {
            report.addErrorMessage(
                    String.format(
                            "%s is not a valid relationship for %s",
                            relationshipName, entity.getName()));
            return false;
        }
        return true;
    }

    private void reportIsNotValidRelationship(
            final String relationshipToMention, final ValidationReport report) {
        report.addErrorMessage(
                String.format("%s is not a valid relationship", relationshipToMention));
    }

    private boolean validRelationshipBetweenThings(
            final EntityDefinition entity,
            final String relationshipName,
            final EntityDefinition relatedEntity,
            final ValidationReport report) {
        for (RelationshipVectorDefinition relationship :
                entity.related().getRelationships(relationshipName)) {
            if (relationship.getTo() == relatedEntity) {
                return true;
            }
        }

        report.addErrorMessage(
                String.format(
                        "%s to %s is not a valid relationship for %s",
                        relationshipName, relatedEntity.getPlural(), entity.getName()));
        return false;
    }
}
