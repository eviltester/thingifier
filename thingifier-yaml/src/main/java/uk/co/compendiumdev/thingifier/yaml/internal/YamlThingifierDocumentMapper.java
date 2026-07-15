package uk.co.compendiumdev.thingifier.yaml.internal;

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

public final class YamlThingifierDocumentMapper {

    public ThingifierModelDefinition toDefinition(final YamlThingifierDocument document) {
        final ThingifierModelDefinition.Builder builder =
                ThingifierModelDefinition.builder()
                        .formatVersion(document.formatVersion())
                        .title(document.title())
                        .description(document.description());
        for (YamlEntityDocument entity : document.entities().values()) {
            builder.entity(entitySpecFor(entity));
        }
        for (YamlRelationshipDocument relationship : document.relationships()) {
            builder.relationship(relationshipSpecFor(relationship));
        }
        return builder.build();
    }

    public Map<String, Object> toYamlMap(final ThingifierModelDefinition definition) {
        final Map<String, Object> root = new LinkedHashMap<>();
        root.put("formatVersion", definition.formatVersion());
        root.put("model", modelMapFor(definition));
        root.put("entities", entitiesMapFor(definition));
        root.put("relationships", relationshipsListFor(definition));
        return root;
    }

    private EntityDefinitionSpec entitySpecFor(final YamlEntityDocument entity) {
        final EntityDefinitionSpec.Builder builder =
                EntityDefinitionSpec.named(entity.name())
                        .plural(entity.pluralName())
                        .maxInstances(entity.maxInstances() == null ? -1 : entity.maxInstances())
                        .primaryKey(entity.primaryKey());
        for (YamlFieldDocument field : entity.fields().values()) {
            builder.field(fieldSpecFor(field));
        }
        return builder.build();
    }

    private FieldDefinitionSpec fieldSpecFor(final YamlFieldDocument field) {
        final FieldDefinitionSpec.Builder builder =
                FieldDefinitionSpec.named(field.name(), field.type())
                        .required(field.required())
                        .unique(field.unique())
                        .defaultValue(field.defaultValue())
                        .description(field.description())
                        .examples(field.examples())
                        .truncateTo(field.truncateTo())
                        .range(field.minValue(), field.maxValue())
                        .validationRules(validationRulesFor(field.validations()));
        for (YamlFieldDocument child : field.fields().values()) {
            builder.objectField(fieldSpecFor(child));
        }
        return builder.build();
    }

    private List<ValidationRuleSpec> validationRulesFor(final List<Object> validations) {
        final List<ValidationRuleSpec> rules = new ArrayList<>();
        for (Object validation : validations) {
            if (validation instanceof String) {
                rules.add(new ValidationRuleSpec(String.valueOf(validation), null));
            } else {
                rules.add(ruleFromMap(validation));
            }
        }
        return rules;
    }

    private ValidationRuleSpec ruleFromMap(final Object validation) {
        final Map<String, Object> map = YamlMapSupport.asMap(validation);
        if (map.isEmpty()) {
            return new ValidationRuleSpec("", null);
        }
        final Map.Entry<String, Object> entry = map.entrySet().iterator().next();
        return new ValidationRuleSpec(entry.getKey(), YamlMapSupport.stringValue(entry.getValue()));
    }

    private RelationshipDefinitionSpec relationshipSpecFor(
            final YamlRelationshipDocument relationship) {
        return new RelationshipDefinitionSpec(
                relationship.fromEntityName(),
                relationship.name(),
                relationship.toEntityName(),
                cardinalityFor(relationship.cardinality()),
                relationship.optionality(),
                reverseSpecFor(relationship.reverse()));
    }

    private RelationshipVectorSpec reverseSpecFor(final YamlRelationshipReverseDocument reverse) {
        if (reverse == null) {
            return null;
        }
        return new RelationshipVectorSpec(
                reverse.name(), cardinalityFor(reverse.cardinality()), reverse.optionality());
    }

    private CardinalitySpec cardinalityFor(final String text) {
        if (text == null || text.trim().isEmpty()) {
            return CardinalitySpec.oneToMany();
        }
        try {
            return CardinalitySpec.fromText(text);
        } catch (IllegalArgumentException e) {
            return new CardinalitySpec(text, "");
        }
    }

    private Map<String, Object> modelMapFor(final ThingifierModelDefinition definition) {
        final Map<String, Object> model = new LinkedHashMap<>();
        model.put("title", definition.title());
        model.put("description", definition.description());
        return model;
    }

    private Map<String, Object> entitiesMapFor(final ThingifierModelDefinition definition) {
        final Map<String, Object> entities = new LinkedHashMap<>();
        for (EntityDefinitionSpec entity : definition.entities()) {
            entities.put(entity.name(), entityMapFor(entity));
        }
        return entities;
    }

    private Map<String, Object> entityMapFor(final EntityDefinitionSpec entity) {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("plural", entity.pluralName());
        map.put("maxInstances", entity.maxInstances());
        if (entity.hasPrimaryKeyField()) {
            map.put("primaryKey", entity.primaryKeyFieldName());
        }
        map.put("fields", fieldsMapFor(entity.fields()));
        return map;
    }

    private Map<String, Object> fieldsMapFor(final List<FieldDefinitionSpec> fields) {
        final Map<String, Object> map = new LinkedHashMap<>();
        for (FieldDefinitionSpec field : fields) {
            map.put(field.name(), fieldMapFor(field));
        }
        return map;
    }

    private Map<String, Object> fieldMapFor(final FieldDefinitionSpec field) {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", field.type());
        putIfTrue(map, "required", field.required());
        putIfTrue(map, "unique", field.unique());
        putIfPresent(map, "default", field.defaultValue());
        putIfPresent(map, "description", field.description());
        if (!field.examples().isEmpty()) {
            map.put("examples", field.examples());
        }
        putIfPresent(map, "truncateTo", field.truncateTo());
        putIfPresent(map, "min", field.minValue());
        putIfPresent(map, "max", field.maxValue());
        if (!field.validationRules().isEmpty()) {
            map.put("validations", validationRulesListFor(field.validationRules()));
        }
        if (!field.objectFields().isEmpty()) {
            map.put("fields", fieldsMapFor(field.objectFields()));
        }
        return map;
    }

    private List<Object> validationRulesListFor(final List<ValidationRuleSpec> rules) {
        final List<Object> values = new ArrayList<>();
        for (ValidationRuleSpec rule : rules) {
            if (rule.value() == null) {
                values.add(rule.name());
            } else {
                final Map<String, Object> map = new LinkedHashMap<>();
                map.put(rule.name(), rule.value());
                values.add(map);
            }
        }
        return values;
    }

    private List<Object> relationshipsListFor(final ThingifierModelDefinition definition) {
        final List<Object> relationships = new ArrayList<>();
        for (RelationshipDefinitionSpec relationship : definition.relationships()) {
            relationships.add(relationshipMapFor(relationship));
        }
        return relationships;
    }

    private Map<String, Object> relationshipMapFor(final RelationshipDefinitionSpec relationship) {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("from", relationship.fromEntityName());
        map.put("name", relationship.name());
        map.put("to", relationship.toEntityName());
        map.put("cardinality", relationship.cardinality().canonicalName());
        putIfPresent(map, "optionality", relationship.optionality());
        if (relationship.hasReverse()) {
            map.put("reverse", reverseMapFor(relationship.reverse()));
        }
        return map;
    }

    private Map<String, Object> reverseMapFor(final RelationshipVectorSpec reverse) {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", reverse.name());
        map.put("cardinality", reverse.cardinality().canonicalName());
        putIfPresent(map, "optionality", reverse.optionality());
        return map;
    }

    private void putIfPresent(final Map<String, Object> map, final String key, final Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private void putIfTrue(final Map<String, Object> map, final String key, final boolean value) {
        if (value) {
            map.put(key, Boolean.TRUE);
        }
    }
}
