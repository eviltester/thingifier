package uk.co.compendiumdev.thingifier.crudui;

import java.util.Locale;
import uk.co.compendiumdev.thingifier.application.schema.definition.EntityDefinitionSpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.FieldDefinitionSpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.RelationshipDefinitionSpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelDefinition;

public final class SchemaDiagramExporter {

    public String mermaid(final ThingifierModelDefinition definition) {
        StringBuilder builder = new StringBuilder();
        builder.append("erDiagram\n");
        for (EntityDefinitionSpec entity : definition.entities()) {
            builder.append("    ").append(identifier(entity.name())).append(" {\n");
            for (FieldDefinitionSpec field : entity.fields()) {
                builder.append("        ")
                        .append(typeName(field.type()))
                        .append(" ")
                        .append(identifier(field.name()));
                if (field.name().equals(entity.primaryKeyFieldName())) {
                    builder.append(" PK");
                }
                builder.append("\n");
            }
            builder.append("    }\n");
        }
        for (RelationshipDefinitionSpec relationship : definition.relationships()) {
            builder.append("    ")
                    .append(identifier(relationship.fromEntityName()))
                    .append(" ")
                    .append(leftMarker(relationship.cardinality().left()))
                    .append("--")
                    .append(
                            rightMarker(
                                    relationship.cardinality().right(), relationship.optionality()))
                    .append(" ")
                    .append(identifier(relationship.toEntityName()))
                    .append(" : ")
                    .append(label(relationship.name()))
                    .append("\n");
        }
        return builder.toString();
    }

    public String graphviz(final ThingifierModelDefinition definition) {
        StringBuilder builder = new StringBuilder();
        builder.append("digraph schema {\n");
        builder.append("  rankdir=LR;\n");
        builder.append("  node [shape=record];\n");
        for (EntityDefinitionSpec entity : definition.entities()) {
            builder.append("  ")
                    .append(dotId(entity.name()))
                    .append(" [label=\"{")
                    .append(escape(entity.name()))
                    .append("|");
            for (int index = 0; index < entity.fields().size(); index++) {
                FieldDefinitionSpec field = entity.fields().get(index);
                if (index > 0) {
                    builder.append("\\l");
                }
                builder.append(escape(field.name())).append(": ").append(escape(field.type()));
                if (field.name().equals(entity.primaryKeyFieldName())) {
                    builder.append(" PK");
                }
            }
            builder.append("\\l}\"];\n");
        }
        for (RelationshipDefinitionSpec relationship : definition.relationships()) {
            builder.append("  ")
                    .append(dotId(relationship.fromEntityName()))
                    .append(" -> ")
                    .append(dotId(relationship.toEntityName()))
                    .append(" [label=\"")
                    .append(escape(relationship.name()))
                    .append("\\n")
                    .append(escape(relationship.cardinality().canonicalName()));
            if (relationship.optionality() != null
                    && !relationship.optionality().trim().isEmpty()) {
                builder.append(" ").append(escape(relationship.optionality()));
            }
            builder.append("\"];\n");
        }
        builder.append("}\n");
        return builder.toString();
    }

    private String leftMarker(final String amount) {
        if ("*".equals(amount)) {
            return "}|";
        }
        if ("0".equals(amount)) {
            return "o|";
        }
        return "||";
    }

    private String rightMarker(final String amount, final String optionality) {
        final boolean optional = optionality == null || !"mandatory".equalsIgnoreCase(optionality);
        if ("*".equals(amount)) {
            return optional ? "o{" : "|{";
        }
        if ("0".equals(amount) || optional) {
            return "o|";
        }
        return "||";
    }

    private String identifier(final String value) {
        String cleaned = value == null ? "" : value.trim().replaceAll("[^A-Za-z0-9_]", "_");
        if (cleaned.isEmpty()) {
            return "unnamed";
        }
        if (Character.isDigit(cleaned.charAt(0))) {
            return "_" + cleaned;
        }
        return cleaned;
    }

    private String typeName(final String type) {
        String value = type == null || type.trim().isEmpty() ? "string" : type;
        return value.toLowerCase(Locale.ROOT).replace('-', '_');
    }

    private String label(final String text) {
        return text == null ? "" : text.replace("\"", "");
    }

    private String dotId(final String value) {
        return "\"" + escape(value) + "\"";
    }

    private String escape(final String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
