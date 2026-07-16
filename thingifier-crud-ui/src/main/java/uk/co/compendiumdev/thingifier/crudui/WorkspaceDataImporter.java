package uk.co.compendiumdev.thingifier.crudui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.application.schema.definition.EntityDefinitionSpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.FieldDefinitionSpec;

public final class WorkspaceDataImporter {

    private final ActiveThingifierWorkspace workspace;
    private final DynamicThingifierApiProxy apiProxy;
    private final WorkspaceMetadataJson metadataJson;

    public WorkspaceDataImporter(
            final ActiveThingifierWorkspace workspace,
            final DynamicThingifierApiProxy apiProxy,
            final WorkspaceMetadataJson metadataJson) {
        this.workspace = workspace;
        this.apiProxy = apiProxy;
        this.metadataJson = metadataJson;
    }

    public UiHttpResponse importData(final String jsonText) {
        Map<?, ?> document = JsonSupport.fromJsonMap(jsonText);
        String schemaYaml = stringValue(document.get("schemaYaml"));
        if (schemaYaml.isEmpty()) {
            throw new CrudUiException(400, "Import file must contain schemaYaml");
        }

        WorkspaceSnapshot snapshot = workspace.replaceWithYaml(schemaYaml);
        Map<String, String> importedIdentifiers = importEntities(document, snapshot);
        importRelationships(document, workspace.snapshot(), importedIdentifiers);
        return UiHttpResponse.json(200, metadataJson.toJson(workspace.snapshot()));
    }

    void importDataIntoCurrentWorkspace(final String jsonText) {
        Map<?, ?> document =
                JsonSupport.fromJsonMap(
                        jsonText,
                        "Project data file must contain a JSON object",
                        "Could not parse project data JSON");
        WorkspaceSnapshot snapshot = workspace.snapshot();
        Map<String, String> importedIdentifiers = importEntities(document, snapshot);
        importRelationships(document, workspace.snapshot(), importedIdentifiers);
    }

    private Map<String, String> importEntities(
            final Map<?, ?> document, final WorkspaceSnapshot snapshot) {
        Map<String, String> importedIdentifiers = new LinkedHashMap<>();
        Map<?, ?> entities =
                mapValue(document.get("entities"), "Import file entities must be an object");
        for (EntityDefinitionSpec entity : snapshot.definition().entities()) {
            Object instancesValue = entities.get(entity.pluralName());
            if (instancesValue == null) {
                instancesValue = entities.get(entity.name());
            }
            for (Object instanceValue : listValue(instancesValue)) {
                Map<?, ?> instance = mapValue(instanceValue, "Imported instance must be an object");
                importInstance(entity, instance, importedIdentifiers);
            }
        }
        return importedIdentifiers;
    }

    private void importInstance(
            final EntityDefinitionSpec entity,
            final Map<?, ?> instance,
            final Map<String, String> importedIdentifiers) {
        Map<String, Object> body = new LinkedHashMap<>();
        for (Map.Entry<?, ?> field : instance.entrySet()) {
            body.put(String.valueOf(field.getKey()), field.getValue());
        }
        String oldId = stringValue(body.get(entity.primaryKeyFieldName()));
        if (hasAutoPrimaryKey(entity)) {
            body.remove(entity.primaryKeyFieldName());
        }

        UiHttpResponse response = apiProxy.postJson(entity.pluralName(), JsonSupport.toJson(body));
        if (response.statusCode() >= 400) {
            throw new CrudUiException(
                    400, "Could not import " + entity.pluralName() + ": " + response.body());
        }

        JsonObject created = JsonParser.parseString(response.body()).getAsJsonObject();
        String newId = stringValue(created.get(entity.primaryKeyFieldName()));
        if (!oldId.isEmpty() && !newId.isEmpty()) {
            importedIdentifiers.put(entity.name() + "|" + oldId, newId);
        }
    }

    private void importRelationships(
            final Map<?, ?> document,
            final WorkspaceSnapshot snapshot,
            final Map<String, String> importedIdentifiers) {
        Object relationshipsValue = document.get("relationships");
        if (relationshipsValue == null) {
            return;
        }
        for (Object edgeValue : listValue(relationshipsValue)) {
            Map<?, ?> edge = mapValue(edgeValue, "Imported relationship edge must be an object");
            importRelationship(edge, snapshot, importedIdentifiers);
        }
    }

    private void importRelationship(
            final Map<?, ?> edge,
            final WorkspaceSnapshot snapshot,
            final Map<String, String> importedIdentifiers) {
        EntityDefinitionSpec source =
                snapshot.definition().entityNamed(stringValue(edge.get("fromEntity")));
        EntityDefinitionSpec target =
                snapshot.definition().entityNamed(stringValue(edge.get("toEntity")));
        String relationshipName = stringValue(edge.get("relationship"));
        if (source == null || target == null || relationshipName.isEmpty()) {
            throw new CrudUiException(
                    400, "Imported relationship edge references an unknown schema item");
        }

        String sourceId = mappedIdentifier(source, edge.get("fromId"), importedIdentifiers);
        String targetId = mappedIdentifier(target, edge.get("toId"), importedIdentifiers);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(target.primaryKeyFieldName(), targetId);

        UiHttpResponse response =
                apiProxy.postJson(
                        source.pluralName()
                                + "/"
                                + encodeSegment(sourceId)
                                + "/"
                                + relationshipName,
                        JsonSupport.toJson(body));
        if (response.statusCode() >= 400) {
            throw new CrudUiException(400, "Could not import relationship: " + response.body());
        }
    }

    private String mappedIdentifier(
            final EntityDefinitionSpec entity,
            final Object oldId,
            final Map<String, String> importedIdentifiers) {
        String oldIdText = stringValue(oldId);
        return importedIdentifiers.getOrDefault(entity.name() + "|" + oldIdText, oldIdText);
    }

    private boolean hasAutoPrimaryKey(final EntityDefinitionSpec entity) {
        FieldDefinitionSpec primaryKey = entity.fieldNamed(entity.primaryKeyFieldName());
        return primaryKey != null
                && ("auto-increment".equals(primaryKey.type())
                        || "auto-guid".equals(primaryKey.type()));
    }

    private Map<?, ?> mapValue(final Object value, final String errorMessage) {
        if (value instanceof Map) {
            return (Map<?, ?>) value;
        }
        throw new CrudUiException(400, errorMessage);
    }

    private List<?> listValue(final Object value) {
        if (value instanceof List) {
            return (List<?>) value;
        }
        return List.of();
    }

    private String stringValue(final Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof JsonElement) {
            JsonElement element = (JsonElement) value;
            if (element.isJsonNull()) {
                return "";
            }
            return element.getAsString();
        }
        return String.valueOf(value);
    }

    private String encodeSegment(final String value) {
        try {
            return URLEncoder.encode(value, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
