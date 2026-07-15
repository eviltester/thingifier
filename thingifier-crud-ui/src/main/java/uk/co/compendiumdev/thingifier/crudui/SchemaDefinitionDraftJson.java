package uk.co.compendiumdev.thingifier.crudui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.application.schema.definition.CardinalitySpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.EntityDefinitionSpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.FieldDefinitionSpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.RelationshipDefinitionSpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.RelationshipVectorSpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelDefinition;
import uk.co.compendiumdev.thingifier.application.schema.definition.ValidationRuleSpec;

public final class SchemaDefinitionDraftJson {

    public String toJson(final ThingifierModelDefinition definition) {
        return JsonSupport.toJson(toMap(definition));
    }

    public Map<String, Object> toMap(final ThingifierModelDefinition definition) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("formatVersion", definition.formatVersion());
        root.put("model", modelMap(definition));
        root.put("entities", entityMaps(definition.entities()));
        root.put("relationships", relationshipMaps(definition.relationships()));
        return root;
    }

    public ThingifierModelDefinition fromJson(final String jsonText) {
        return fromMap(
                JsonSupport.fromJsonMap(
                        jsonText,
                        "Schema draft must contain a JSON object",
                        "Could not parse schema draft JSON"));
    }

    public ThingifierModelDefinition fromMap(final Map<?, ?> root) {
        Map<?, ?> model = mapValue(root.get("model"));
        ThingifierModelDefinition.Builder builder =
                ThingifierModelDefinition.builder()
                        .formatVersion(intValue(root.get("formatVersion"), 1))
                        .title(stringValue(model.get("title")))
                        .description(stringValue(model.get("description")));
        for (Object entityValue : listValue(root.get("entities"))) {
            builder.entity(entityFrom(mapValue(entityValue)));
        }
        for (Object relationshipValue : listValue(root.get("relationships"))) {
            builder.relationship(relationshipFrom(mapValue(relationshipValue)));
        }
        return builder.build();
    }

    private Map<String, Object> modelMap(final ThingifierModelDefinition definition) {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("title", nullToEmpty(definition.title()));
        model.put("description", nullToEmpty(definition.description()));
        return model;
    }

    private List<Map<String, Object>> entityMaps(final List<EntityDefinitionSpec> entities) {
        List<Map<String, Object>> values = new ArrayList<>();
        for (EntityDefinitionSpec entity : entities) {
            Map<String, Object> entityMap = new LinkedHashMap<>();
            entityMap.put("name", entity.name());
            entityMap.put("plural", entity.pluralName());
            entityMap.put("maxInstances", entity.maxInstances());
            entityMap.put("primaryKey", nullToEmpty(entity.primaryKeyFieldName()));
            entityMap.put("fields", fieldMaps(entity.fields()));
            values.add(entityMap);
        }
        return values;
    }

    private List<Map<String, Object>> fieldMaps(final List<FieldDefinitionSpec> fields) {
        List<Map<String, Object>> values = new ArrayList<>();
        for (FieldDefinitionSpec field : fields) {
            values.add(fieldMap(field));
        }
        return values;
    }

    private Map<String, Object> fieldMap(final FieldDefinitionSpec field) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("name", field.name());
        value.put("type", field.type());
        value.put("required", field.required());
        value.put("unique", field.unique());
        value.put("defaultValue", field.defaultValue());
        value.put("description", field.description());
        value.put("examples", field.examples());
        value.put("truncateTo", field.truncateTo());
        value.put("min", field.minValue());
        value.put("max", field.maxValue());
        value.put("validations", validationMaps(field.validationRules()));
        value.put("objectFields", fieldMaps(field.objectFields()));
        return value;
    }

    private List<Map<String, Object>> validationMaps(final List<ValidationRuleSpec> rules) {
        List<Map<String, Object>> values = new ArrayList<>();
        for (ValidationRuleSpec rule : rules) {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("type", rule.name());
            value.put("value", rule.value());
            values.add(value);
        }
        return values;
    }

    private List<Map<String, Object>> relationshipMaps(
            final List<RelationshipDefinitionSpec> relationships) {
        List<Map<String, Object>> values = new ArrayList<>();
        for (RelationshipDefinitionSpec relationship : relationships) {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("from", relationship.fromEntityName());
            value.put("name", relationship.name());
            value.put("to", relationship.toEntityName());
            value.put("cardinality", relationship.cardinality().canonicalName());
            value.put("optionality", nullToEmpty(relationship.optionality()));
            value.put("reverse", reverseMap(relationship.reverse()));
            values.add(value);
        }
        return values;
    }

    private Map<String, Object> reverseMap(final RelationshipVectorSpec reverse) {
        if (reverse == null) {
            return null;
        }
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("name", reverse.name());
        value.put("cardinality", reverse.cardinality().canonicalName());
        value.put("optionality", nullToEmpty(reverse.optionality()));
        return value;
    }

    private EntityDefinitionSpec entityFrom(final Map<?, ?> map) {
        EntityDefinitionSpec.Builder builder =
                EntityDefinitionSpec.named(stringValue(map.get("name")))
                        .plural(stringValue(map.get("plural")))
                        .maxInstances(intValue(map.get("maxInstances"), -1))
                        .primaryKey(stringValue(map.get("primaryKey")));
        for (Object fieldValue : listValue(map.get("fields"))) {
            builder.field(fieldFrom(mapValue(fieldValue)));
        }
        return builder.build();
    }

    private FieldDefinitionSpec fieldFrom(final Map<?, ?> map) {
        FieldDefinitionSpec.Builder builder =
                FieldDefinitionSpec.named(
                                stringValue(map.get("name")), stringValue(map.get("type")))
                        .required(booleanValue(map.get("required")))
                        .unique(booleanValue(map.get("unique")))
                        .defaultValue(nullableString(map.get("defaultValue")))
                        .description(nullableString(map.get("description")))
                        .examples(stringList(map.get("examples")))
                        .truncateTo(nullableInt(map.get("truncateTo")))
                        .range(nullableString(map.get("min")), nullableString(map.get("max")))
                        .validationRules(validationRulesFrom(map.get("validations")));
        for (Object fieldValue : listValue(map.get("objectFields"))) {
            builder.objectField(fieldFrom(mapValue(fieldValue)));
        }
        return builder.build();
    }

    private List<ValidationRuleSpec> validationRulesFrom(final Object value) {
        List<ValidationRuleSpec> rules = new ArrayList<>();
        for (Object ruleValue : listValue(value)) {
            Map<?, ?> rule = mapValue(ruleValue);
            rules.add(
                    new ValidationRuleSpec(
                            stringValue(rule.get("type")), nullableString(rule.get("value"))));
        }
        return rules;
    }

    private RelationshipDefinitionSpec relationshipFrom(final Map<?, ?> map) {
        return new RelationshipDefinitionSpec(
                stringValue(map.get("from")),
                stringValue(map.get("name")),
                stringValue(map.get("to")),
                cardinalityFor(stringValue(map.get("cardinality"))),
                nullableString(map.get("optionality")),
                reverseFrom(map.get("reverse")));
    }

    private RelationshipVectorSpec reverseFrom(final Object value) {
        if (value == null) {
            return null;
        }
        Map<?, ?> map = mapValue(value);
        if (stringValue(map.get("name")).trim().isEmpty()) {
            return null;
        }
        return new RelationshipVectorSpec(
                stringValue(map.get("name")),
                cardinalityFor(stringValue(map.get("cardinality"))),
                nullableString(map.get("optionality")));
    }

    private CardinalitySpec cardinalityFor(final String text) {
        try {
            return CardinalitySpec.fromText(text);
        } catch (IllegalArgumentException e) {
            return new CardinalitySpec(text, "");
        }
    }

    private List<String> stringList(final Object value) {
        List<String> values = new ArrayList<>();
        for (Object item : listValue(value)) {
            values.add(stringValue(item));
        }
        return values;
    }

    private Map<?, ?> mapValue(final Object value) {
        if (value instanceof Map) {
            return (Map<?, ?>) value;
        }
        return Map.of();
    }

    private List<?> listValue(final Object value) {
        if (value instanceof List) {
            return (List<?>) value;
        }
        return List.of();
    }

    private boolean booleanValue(final Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(stringValue(value));
    }

    private Integer nullableInt(final Object value) {
        if (value == null || stringValue(value).trim().isEmpty()) {
            return null;
        }
        return intValue(value, 0);
    }

    private int intValue(final Object value, final int defaultValue) {
        if (value == null || stringValue(value).trim().isEmpty()) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(stringValue(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String nullableString(final Object value) {
        if (value == null) {
            return null;
        }
        String text = stringValue(value);
        return text.isEmpty() ? null : text;
    }

    private String stringValue(final Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String nullToEmpty(final String value) {
        return value == null ? "" : value;
    }
}
