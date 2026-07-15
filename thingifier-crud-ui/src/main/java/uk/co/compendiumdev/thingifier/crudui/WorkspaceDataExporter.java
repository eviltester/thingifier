package uk.co.compendiumdev.thingifier.crudui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.co.compendiumdev.thingifier.application.schema.definition.EntityDefinitionSpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.RelationshipDefinitionSpec;

public final class WorkspaceDataExporter {

    private final ActiveThingifierWorkspace workspace;
    private final DynamicThingifierApiProxy apiProxy;

    public WorkspaceDataExporter(
            final ActiveThingifierWorkspace workspace, final DynamicThingifierApiProxy apiProxy) {
        this.workspace = workspace;
        this.apiProxy = apiProxy;
    }

    public UiHttpResponse exportData() {
        WorkspaceSnapshot snapshot = workspace.snapshot();
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("formatVersion", 1);
        document.put("schemaYaml", snapshot.schemaYaml());
        document.put("entities", exportEntities(snapshot));
        document.put("relationships", exportRelationships(snapshot));
        return UiHttpResponse.json(200, JsonSupport.toJson(document));
    }

    private Map<String, Object> exportEntities(final WorkspaceSnapshot snapshot) {
        Map<String, Object> entities = new LinkedHashMap<>();
        for (EntityDefinitionSpec entity : snapshot.definition().entities()) {
            JsonArray instances = collectionFor(entity.pluralName());
            List<Map<String, Object>> exportedInstances = new ArrayList<>();
            for (JsonElement instance : instances) {
                exportedInstances.add(fieldOnlyMap(entity, instance.getAsJsonObject(), snapshot));
            }
            entities.put(entity.pluralName(), exportedInstances);
        }
        return entities;
    }

    private List<Map<String, Object>> exportRelationships(final WorkspaceSnapshot snapshot) {
        List<Map<String, Object>> relationships = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (RelationshipDefinitionSpec relationship : snapshot.definition().relationships()) {
            EntityDefinitionSpec source =
                    snapshot.definition().entityNamed(relationship.fromEntityName());
            EntityDefinitionSpec target =
                    snapshot.definition().entityNamed(relationship.toEntityName());
            if (source == null
                    || target == null
                    || !source.hasPrimaryKeyField()
                    || !target.hasPrimaryKeyField()) {
                continue;
            }
            for (JsonElement sourceElement : collectionFor(source.pluralName())) {
                JsonObject sourceInstance = sourceElement.getAsJsonObject();
                String sourceId = stringValue(sourceInstance, source.primaryKeyFieldName());
                if (sourceId.isEmpty()) {
                    continue;
                }
                JsonArray related =
                        collectionFor(
                                source.pluralName()
                                        + "/"
                                        + encodeSegment(sourceId)
                                        + "/"
                                        + relationship.name(),
                                target.pluralName());
                addEdges(relationships, seen, relationship, source, sourceId, target, related);
            }
        }
        return relationships;
    }

    private void addEdges(
            final List<Map<String, Object>> relationships,
            final Set<String> seen,
            final RelationshipDefinitionSpec relationship,
            final EntityDefinitionSpec source,
            final String sourceId,
            final EntityDefinitionSpec target,
            final JsonArray related) {
        for (JsonElement targetElement : related) {
            JsonObject targetInstance = targetElement.getAsJsonObject();
            String targetId = stringValue(targetInstance, target.primaryKeyFieldName());
            String key =
                    source.name() + "|" + sourceId + "|" + relationship.name() + "|" + targetId;
            if (targetId.isEmpty() || seen.contains(key)) {
                continue;
            }
            Map<String, Object> edge = new LinkedHashMap<>();
            edge.put("fromEntity", source.name());
            edge.put("fromPlural", source.pluralName());
            edge.put("fromId", sourceId);
            edge.put("relationship", relationship.name());
            edge.put("toEntity", target.name());
            edge.put("toPlural", target.pluralName());
            edge.put("toId", targetId);
            relationships.add(edge);
            seen.add(key);
        }
    }

    private Map<String, Object> fieldOnlyMap(
            final EntityDefinitionSpec entity,
            final JsonObject instance,
            final WorkspaceSnapshot snapshot) {
        Set<String> relationshipNames = relationshipNamesFor(entity.name(), snapshot);
        Map<String, Object> fields = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : instance.entrySet()) {
            if ("relationships".equals(entry.getKey())
                    || relationshipNames.contains(entry.getKey())) {
                continue;
            }
            fields.put(entry.getKey(), JsonSupport.fromJsonElement(entry.getValue()));
        }
        return fields;
    }

    private Set<String> relationshipNamesFor(
            final String entityName, final WorkspaceSnapshot snapshot) {
        Set<String> names = new LinkedHashSet<>();
        for (RelationshipDefinitionSpec relationship : snapshot.definition().relationships()) {
            if (entityName.equals(relationship.fromEntityName())) {
                names.add(relationship.name());
            }
            if (relationship.hasReverse() && entityName.equals(relationship.toEntityName())) {
                names.add(relationship.reverse().name());
            }
        }
        return names;
    }

    private JsonArray collectionFor(final String path) {
        return collectionFor(path, path.contains("/") ? "" : path);
    }

    private JsonArray collectionFor(final String path, final String rootName) {
        UiHttpResponse response = apiProxy.getJson(path);
        if (response.statusCode() >= 400) {
            throw new CrudUiException(response.statusCode(), "Could not export " + path);
        }
        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
        String arrayRoot = rootName.isEmpty() ? firstArrayRoot(root) : rootName;
        JsonElement collection = root.get(arrayRoot);
        if (collection == null || !collection.isJsonArray()) {
            return new JsonArray();
        }
        return collection.getAsJsonArray();
    }

    private String firstArrayRoot(final JsonObject root) {
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            if (entry.getValue().isJsonArray()) {
                return entry.getKey();
            }
        }
        return "";
    }

    private String stringValue(final JsonObject instance, final String fieldName) {
        JsonElement value = instance.get(fieldName);
        if (value == null || value.isJsonNull()) {
            return "";
        }
        return value.getAsString();
    }

    private String encodeSegment(final String value) {
        try {
            return URLEncoder.encode(value, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
