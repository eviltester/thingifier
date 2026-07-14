package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.application.command.RelationshipReference;
import uk.co.compendiumdev.thingifier.application.schema.EntityTypeRef;
import uk.co.compendiumdev.thingifier.application.schema.FieldSpec;
import uk.co.compendiumdev.thingifier.application.schema.RelationshipSpec;
import uk.co.compendiumdev.thingifier.application.schema.SchemaViewCatalog;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public final class RelationshipBodyCommandParser {

    private final SchemaViewCatalog schema;

    public RelationshipBodyCommandParser(final SchemaViewCatalog schema) {
        this.schema = schema;
    }

    public RelationshipBodyCommands parse(
            final List<Map.Entry<String, String>> flattenedArgs, final EntityTypeRef entity) {
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
                if (parts.length > 0 && entity.hasRelationship(parts[0])) {
                    relationshipEntries.add(keyValue);
                    parseCompressedRelationship(entity, keyValue, report, references);
                }
            }
        }

        report.setValid(report.getErrorMessages().isEmpty());
        return new RelationshipBodyCommands(relationshipEntries, references, report);
    }

    private void parseCompressedRelationship(
            final EntityTypeRef entity,
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
            final EntityTypeRef entity,
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

        EntityTypeRef relationshipTo = schema.entityWithSingularOrPluralName(relationshipToTerm);
        if (relationshipTo == null
                || !validRelationshipBetweenThings(
                        entity, relationshipName, relationshipTo, report)) {
            return;
        }

        references.add(
                RelationshipReference.explicit(
                        relationshipName,
                        relationshipTo.name(),
                        relationshipToTerm,
                        relationshipField,
                        keyValue.getValue()));
    }

    private boolean supportsReferenceField(
            final EntityTypeRef entity, final String relationshipName, final String fieldName) {
        for (RelationshipSpec relationship : entity.relationships()) {
            if (!relationship.name().equals(relationshipName)) {
                continue;
            }
            EntityTypeRef target =
                    schema.entityWithSingularOrPluralName(relationship.toEntityName());
            FieldSpec field = target == null ? null : target.fieldNamed(fieldName);
            if (field != null && field.isProtectedField()) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidRelationship(
            final EntityTypeRef entity,
            final String relationshipName,
            final ValidationReport report) {
        if (!entity.hasRelationship(relationshipName)) {
            report.addErrorMessage(
                    String.format(
                            "%s is not a valid relationship for %s",
                            relationshipName, entity.name()));
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
            final EntityTypeRef entity,
            final String relationshipName,
            final EntityTypeRef relatedEntity,
            final ValidationReport report) {
        for (RelationshipSpec relationship : entity.relationships()) {
            if (relationship.name().equals(relationshipName)
                    && relationship.toEntityName().equals(relatedEntity.name())) {
                return true;
            }
        }

        report.addErrorMessage(
                String.format(
                        "%s to %s is not a valid relationship for %s",
                        relationshipName, relatedEntity.pluralName(), entity.name()));
        return false;
    }
}
