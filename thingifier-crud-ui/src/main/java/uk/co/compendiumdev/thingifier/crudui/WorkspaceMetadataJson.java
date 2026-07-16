package uk.co.compendiumdev.thingifier.crudui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.application.schema.definition.EntityDefinitionSpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.FieldDefinitionSpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.RelationshipDefinitionSpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.RelationshipVectorSpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.ValidationRuleSpec;

public final class WorkspaceMetadataJson {

    public String toJson(final WorkspaceSnapshot snapshot) {
        return JsonSupport.toJson(toMap(snapshot));
    }

    public Map<String, Object> toMap(final WorkspaceSnapshot snapshot) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("formatVersion", 1);
        body.put("workspaceVersion", snapshot.version());
        body.put("model", modelMap(snapshot));
        body.put("entities", entityMaps(snapshot));
        body.put("relationships", relationshipMaps(snapshot));
        body.put("schemaYaml", snapshot.schemaYaml());
        body.put("project", projectMap(snapshot));
        return body;
    }

    public String toJson(final WorkspaceSnapshot snapshot, final String projectStatus) {
        Map<String, Object> body = toMap(snapshot);
        body.put("projectStatus", projectStatus);
        return JsonSupport.toJson(body);
    }

    private Map<String, Object> modelMap(final WorkspaceSnapshot snapshot) {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("title", nullToEmpty(snapshot.definition().title()));
        model.put("description", nullToEmpty(snapshot.definition().description()));
        return model;
    }

    private Map<String, Object> projectMap(final WorkspaceSnapshot snapshot) {
        Map<String, Object> project = new LinkedHashMap<>();
        project.put("path", snapshot.projectPath());
        project.put("title", snapshot.projectTitle());
        project.put("description", snapshot.projectDescription());
        project.put("active", snapshot.hasProjectPath());
        return project;
    }

    private List<Map<String, Object>> entityMaps(final WorkspaceSnapshot snapshot) {
        List<Map<String, Object>> entities = new ArrayList<>();
        for (EntityDefinitionSpec entity : snapshot.definition().entities()) {
            Map<String, Object> entityMap = new LinkedHashMap<>();
            entityMap.put("name", entity.name());
            entityMap.put("plural", entity.pluralName());
            entityMap.put("primaryKey", nullToEmpty(entity.primaryKeyFieldName()));
            entityMap.put("maxInstances", entity.maxInstances());
            entityMap.put("fields", fieldMaps(entity));
            entityMap.put("relationships", relationshipMapsFor(entity.name(), snapshot));
            entities.add(entityMap);
        }
        return entities;
    }

    private List<Map<String, Object>> fieldMaps(final EntityDefinitionSpec entity) {
        List<Map<String, Object>> fields = new ArrayList<>();
        for (FieldDefinitionSpec field : entity.fields()) {
            fields.add(fieldMap(field, field.name().equals(entity.primaryKeyFieldName())));
        }
        return fields;
    }

    private Map<String, Object> fieldMap(final FieldDefinitionSpec field, final boolean primary) {
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("name", field.name());
        fieldMap.put("type", field.type());
        fieldMap.put("required", field.required());
        fieldMap.put("unique", field.unique());
        fieldMap.put("primary", primary);
        fieldMap.put("auto", isAutoField(field));
        fieldMap.put("defaultValue", field.defaultValue());
        fieldMap.put("description", field.description());
        fieldMap.put("examples", field.examples());
        fieldMap.put("truncateTo", field.truncateTo());
        fieldMap.put("min", field.minValue());
        fieldMap.put("max", field.maxValue());
        fieldMap.put("validations", validationMaps(field.validationRules()));
        fieldMap.put("objectFields", objectFieldMaps(field.objectFields()));
        return fieldMap;
    }

    private List<Map<String, Object>> objectFieldMaps(final List<FieldDefinitionSpec> fields) {
        List<Map<String, Object>> objectFields = new ArrayList<>();
        for (FieldDefinitionSpec field : fields) {
            objectFields.add(fieldMap(field, false));
        }
        return objectFields;
    }

    private List<Map<String, Object>> validationMaps(final List<ValidationRuleSpec> rules) {
        List<Map<String, Object>> validations = new ArrayList<>();
        for (ValidationRuleSpec rule : rules) {
            Map<String, Object> validation = new LinkedHashMap<>();
            validation.put("type", rule.name());
            validation.put("value", rule.value());
            validations.add(validation);
        }
        return validations;
    }

    private List<Map<String, Object>> relationshipMaps(final WorkspaceSnapshot snapshot) {
        List<Map<String, Object>> relationships = new ArrayList<>();
        for (RelationshipDefinitionSpec relationship : snapshot.definition().relationships()) {
            relationships.add(forwardRelationshipMap(relationship, snapshot));
            if (relationship.hasReverse()) {
                relationships.add(reverseRelationshipMap(relationship, snapshot));
            }
        }
        return relationships;
    }

    private List<Map<String, Object>> relationshipMapsFor(
            final String entityName, final WorkspaceSnapshot snapshot) {
        List<Map<String, Object>> relationships = new ArrayList<>();
        for (RelationshipDefinitionSpec relationship : snapshot.definition().relationships()) {
            if (entityName.equals(relationship.fromEntityName())) {
                relationships.add(forwardRelationshipMap(relationship, snapshot));
            }
            if (relationship.hasReverse() && entityName.equals(relationship.toEntityName())) {
                relationships.add(reverseRelationshipMap(relationship, snapshot));
            }
        }
        return relationships;
    }

    private Map<String, Object> forwardRelationshipMap(
            final RelationshipDefinitionSpec relationship, final WorkspaceSnapshot snapshot) {
        return relationshipMap(
                relationship.name(),
                relationship.fromEntityName(),
                relationship.toEntityName(),
                relationship.cardinality().canonicalName(),
                relationship.optionality(),
                snapshot);
    }

    private Map<String, Object> reverseRelationshipMap(
            final RelationshipDefinitionSpec relationship, final WorkspaceSnapshot snapshot) {
        RelationshipVectorSpec reverse = relationship.reverse();
        return relationshipMap(
                reverse.name(),
                relationship.toEntityName(),
                relationship.fromEntityName(),
                reverse.cardinality().canonicalName(),
                reverse.optionality(),
                snapshot);
    }

    private Map<String, Object> relationshipMap(
            final String name,
            final String fromEntity,
            final String toEntity,
            final String cardinality,
            final String optionality,
            final WorkspaceSnapshot snapshot) {
        Map<String, Object> relationship = new LinkedHashMap<>();
        relationship.put("name", name);
        relationship.put("fromEntity", fromEntity);
        relationship.put("fromPlural", pluralFor(fromEntity, snapshot));
        relationship.put("toEntity", toEntity);
        relationship.put("toPlural", pluralFor(toEntity, snapshot));
        relationship.put("cardinality", cardinality);
        relationship.put("optionality", optionality);
        return relationship;
    }

    private String pluralFor(final String entityName, final WorkspaceSnapshot snapshot) {
        EntityDefinitionSpec entity = snapshot.definition().entityNamed(entityName);
        return entity == null ? entityName : entity.pluralName();
    }

    private boolean isAutoField(final FieldDefinitionSpec field) {
        return "auto-increment".equals(field.type()) || "auto-guid".equals(field.type());
    }

    private String nullToEmpty(final String value) {
        return value == null ? "" : value;
    }
}
