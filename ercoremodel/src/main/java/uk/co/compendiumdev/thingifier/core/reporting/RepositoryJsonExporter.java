package uk.co.compendiumdev.thingifier.core.reporting;

import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.EntityInstanceQuery;

public class RepositoryJsonExporter {

    private final ERSchema schema;
    private final EntityInstanceQuery query;

    public RepositoryJsonExporter(final ERSchema schema, final EntityInstanceQuery query) {
        this.schema = schema;
        this.query = query;
    }

    public String asJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");

        String entitySeparator = "";
        for (EntityDefinition entity : schema.getEntityDefinitions()) {
            json.append(entitySeparator).append(quoted(entity.getPlural())).append(" : [");

            String instanceSeparator = "";
            for (EntityInstance instance : query.list(entity)) {
                json.append(instanceSeparator);
                appendInstanceJson(json, entity, instance);
                instanceSeparator = ", ";
            }

            json.append("]");
            entitySeparator = ", ";
        }

        json.append("}");
        return json.toString();
    }

    public static void appendInstanceJson(
            final StringBuilder json,
            final EntityDefinition entity,
            final EntityInstance instance) {
        json.append("{");

        String fieldSeparator = "";
        for (String fieldName : entity.getFieldNames()) {
            Field field = entity.getField(fieldName);
            String fieldJsonValue = fieldJsonValue(field, instance);
            if (fieldJsonValue == null) {
                continue;
            }

            json.append(fieldSeparator)
                    .append(quoted(field.getName()))
                    .append(": ")
                    .append(fieldJsonValue);
            fieldSeparator = ", ";
        }

        json.append("}");
    }

    public static String fieldJsonValue(final Field field, final EntityInstance instance) {
        if (instance.hasInstantiatedFieldNamed(field.getName())) {
            return instance.getFieldValue(field.getName()).asJsonValue();
        }

        if (field.isMandatory()) {
            return field.getDefaultValue().asJsonValue();
        }

        return null;
    }

    public static String fieldJsonValue(final Field field, final String value) {
        if (value == null) {
            if (field.isMandatory()) {
                return field.getDefaultValue().asJsonValue();
            }
            return null;
        }
        return field.valueFor(value).asJsonValue();
    }

    public static String quoted(final String value) {
        return "\"" + value.replaceAll("\"", "\\\\\"") + "\"";
    }
}
