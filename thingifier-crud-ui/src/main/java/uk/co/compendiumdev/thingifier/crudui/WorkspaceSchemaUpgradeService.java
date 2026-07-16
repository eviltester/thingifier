package uk.co.compendiumdev.thingifier.crudui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.schema.definition.EntityDefinitionSpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.FieldDefinitionSpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.SchemaDefinitionValidationReport;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelAssembler;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelDefinition;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.domain.instances.InstanceFields;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.yaml.ThingifierYamlExporter;

public final class WorkspaceSchemaUpgradeService {

    private static final String STALE_WORKSPACE_MESSAGE =
            "Workspace changed; refresh schema preview before applying";

    private final ActiveThingifierWorkspace workspace;
    private final WorkspaceMetadataJson metadataJson;
    private final SchemaDefinitionDraftJson draftJson;
    private final ThingifierModelAssembler assembler;
    private final ThingifierYamlExporter yamlExporter;

    public WorkspaceSchemaUpgradeService(
            final ActiveThingifierWorkspace workspace, final WorkspaceMetadataJson metadataJson) {
        this.workspace = workspace;
        this.metadataJson = metadataJson;
        this.draftJson = new SchemaDefinitionDraftJson();
        this.assembler = new ThingifierModelAssembler();
        this.yamlExporter = new ThingifierYamlExporter();
    }

    public UiHttpResponse preview(final String jsonText) {
        UpgradeRequest request = requestFrom(jsonText, false);
        MigrationResult result = migrationResultFor(request);
        result.closeTargetThingifier();
        return UiHttpResponse.json(200, JsonSupport.toJson(result.body()));
    }

    public UiHttpResponse apply(final String jsonText) {
        UpgradeRequest request = requestFrom(jsonText, true);
        MigrationResult result = migrationResultFor(request);
        if (!result.canApply()) {
            result.closeTargetThingifier();
            return UiHttpResponse.json(result.statusCode(), JsonSupport.toJson(result.body()));
        }

        try {
            WorkspaceSnapshot upgraded =
                    workspace.replaceWithMigratedThingifier(
                            result.targetThingifier(),
                            result.targetStorage(),
                            request.expectedWorkspaceVersion());
            result.releaseTargetThingifier();
            result.markApplied(upgraded, metadataJson.toMap(upgraded));
            return UiHttpResponse.json(200, JsonSupport.toJson(result.body()));
        } catch (IllegalStateException e) {
            result.closeTargetThingifier();
            result.addBlockingError("workspaceVersion", e.getMessage());
            result.finish();
            return UiHttpResponse.json(409, JsonSupport.toJson(result.body()));
        }
    }

    private MigrationResult migrationResultFor(final UpgradeRequest request) {
        WorkspaceSnapshot snapshot = workspace.snapshot();
        MigrationResult result = new MigrationResult(snapshot.version());
        result.putRequestMappings(request.mappings().asMap());

        if (request.hasExpectedWorkspaceVersion()
                && request.expectedWorkspaceVersion() != snapshot.version()) {
            result.addBlockingError("workspaceVersion", STALE_WORKSPACE_MESSAGE);
            result.finish();
            return result;
        }

        SchemaDefinitionValidationReport validation = assembler.validate(request.definition());
        result.putSchemaValidation(validation);
        if (!validation.isValid()) {
            result.finish();
            return result;
        }

        result.putYaml(yamlExporter.export(request.definition()));
        SourceWorkspaceData sourceData = sourceDataFrom(snapshot);
        EffectiveMappings mappings =
                EffectiveMappings.from(
                        snapshot.definition(), request.definition(), request.mappings());
        result.putEffectiveMappings(mappings.asMap());
        result.addWarnings(mappings.warnings());
        result.putSummary(mappings.summaryMap(sourceData));

        WorkspaceStorage targetStorage = targetStorageFor(snapshot.storage());
        Thingifier targetThingifier =
                assembler.assemble(request.definition(), targetStorage.provider());
        targetThingifier.clearAllData();
        try {
            migrate(sourceData, request.definition(), targetThingifier, mappings, result);
            validateFinalData(request.definition(), targetThingifier, result);
            result.targetThingifier(targetThingifier, targetStorage);
        } catch (IllegalArgumentException | IllegalStateException | IndexOutOfBoundsException e) {
            targetThingifier.close();
            result.addBlockingError("migration", e.getMessage());
        }
        result.finish();
        return result;
    }

    private WorkspaceStorage targetStorageFor(final WorkspaceStorage currentStorage) {
        if (WorkspaceStorage.MODE_SQLITE_MEMORY.equals(currentStorage.mode())) {
            return WorkspaceStorage.sqliteMemory();
        }
        return WorkspaceStorage.memory();
    }

    private void migrate(
            final SourceWorkspaceData sourceData,
            final ThingifierModelDefinition targetDefinition,
            final Thingifier targetThingifier,
            final EffectiveMappings mappings,
            final MigrationResult result) {
        ThingStore targetStore = targetThingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
        Map<String, EntityInstance> migratedBySourceInternalId = new LinkedHashMap<>();

        for (EntityDefinitionSpec targetEntity : targetDefinition.entities()) {
            String sourceEntityName = mappings.sourceEntityFor(targetEntity.name());
            if (sourceEntityName == null) {
                continue;
            }
            EntityDefinition targetRuntimeEntity =
                    targetThingifier.getDefinitionNamed(targetEntity.name());
            for (SourceRow sourceRow : sourceData.rowsFor(sourceEntityName)) {
                EntityInstance migrated =
                        migrateRow(
                                sourceRow,
                                sourceData.sourceDefinition().entityNamed(sourceEntityName),
                                targetEntity,
                                targetRuntimeEntity,
                                targetStore,
                                mappings,
                                result);
                migratedBySourceInternalId.put(sourceRow.internalId(), migrated);
                result.incrementSummary("migratedInstances");
            }
        }

        for (RelationshipMapping relationship : mappings.relationshipMappings()) {
            migrateRelationship(
                    sourceData, targetStore, relationship, migratedBySourceInternalId, result);
        }
    }

    private EntityInstance migrateRow(
            final SourceRow sourceRow,
            final EntityDefinitionSpec sourceEntity,
            final EntityDefinitionSpec targetEntity,
            final EntityDefinition targetRuntimeEntity,
            final ThingStore targetStore,
            final EffectiveMappings mappings,
            final MigrationResult result) {
        EntityInstanceDraft draft = EntityInstanceDraft.forEntity(targetRuntimeEntity);
        for (FieldDefinitionSpec targetField : targetEntity.fields()) {
            migrateField(
                    sourceRow, sourceEntity, targetEntity, targetField, mappings, draft, result);
        }
        targetStore
                .administration()
                .accommodateProtectedIds(targetRuntimeEntity, protectedValues(draft));
        try {
            EntityInstance migrated = targetStore.entities().create(draft);
            result.incrementEntityCount(targetEntity.name());
            return migrated;
        } catch (IllegalArgumentException | IllegalStateException | IndexOutOfBoundsException e) {
            result.addBlockingError(
                    "entities." + targetEntity.name(),
                    "Could not migrate "
                            + targetEntity.name()
                            + " "
                            + sourceRow.displayIdentifier()
                            + ": "
                            + e.getMessage());
            throw e;
        }
    }

    private List<NamedValue> protectedValues(final EntityInstanceDraft draft) {
        return new ArrayList<>(draft.getProtectedFieldValues());
    }

    private void migrateField(
            final SourceRow sourceRow,
            final EntityDefinitionSpec sourceEntity,
            final EntityDefinitionSpec targetEntity,
            final FieldDefinitionSpec targetField,
            final EffectiveMappings mappings,
            final EntityInstanceDraft draft,
            final MigrationResult result) {
        String sourceFieldName = mappings.sourceFieldFor(targetEntity.name(), targetField.name());
        FieldDefinitionSpec sourceField =
                sourceEntity == null || sourceFieldName == null
                        ? null
                        : sourceEntity.fieldNamed(sourceFieldName);
        if ("object".equals(normalizedType(targetField))) {
            migrateObjectField(sourceRow, sourceField, sourceFieldName, targetField, draft, result);
            return;
        }

        String value =
                valueForField(
                        sourceRow,
                        sourceField,
                        sourceFieldName,
                        targetField,
                        targetField.name(),
                        result);
        if (value == null) {
            return;
        }
        addValueToDraft(draft, targetField, targetField.name(), value);
    }

    private void migrateObjectField(
            final SourceRow sourceRow,
            final FieldDefinitionSpec sourceField,
            final String sourceFieldName,
            final FieldDefinitionSpec targetField,
            final EntityInstanceDraft draft,
            final MigrationResult result) {
        if (sourceField != null && !"object".equals(normalizedType(sourceField))) {
            result.addWarning(
                    "Field "
                            + sourceField.name()
                            + " could not be mapped into object field "
                            + targetField.name());
        }
        for (FieldDefinitionSpec targetChild : targetField.objectFields()) {
            String targetPath = targetField.name() + "." + targetChild.name();
            FieldDefinitionSpec sourceChild = null;
            String sourcePath = null;
            if (sourceField != null && "object".equals(normalizedType(sourceField))) {
                sourceChild = childFieldNamed(sourceField, targetChild.name());
                if (sourceChild != null) {
                    sourcePath = sourceFieldName + "." + sourceChild.name();
                }
            }
            String value =
                    valueForField(
                            sourceRow, sourceChild, sourcePath, targetChild, targetPath, result);
            if (value == null) {
                continue;
            }
            addValueToDraft(draft, targetChild, targetPath, value);
        }
    }

    private String valueForField(
            final SourceRow sourceRow,
            final FieldDefinitionSpec sourceField,
            final String sourceFieldName,
            final FieldDefinitionSpec targetField,
            final String targetPath,
            final MigrationResult result) {
        String sourceValue = sourceFieldName == null ? null : sourceRow.valueFor(sourceFieldName);
        if (sourceField == null || sourceValue == null) {
            return defaultOrFallback(targetField, targetPath, sourceRow, result);
        }

        String coerced = coerce(sourceValue, targetField, targetPath, sourceRow, result);
        if (!normalizedType(sourceField).equals(normalizedType(targetField))
                || !sourceValue.equals(coerced)) {
            result.addCoercion(
                    sourceRow.entityName(),
                    sourceRow.displayIdentifier(),
                    targetPath,
                    sourceValue,
                    coerced,
                    "Converted " + sourceField.type() + " to " + targetField.type());
        }
        return coerced;
    }

    private String defaultOrFallback(
            final FieldDefinitionSpec targetField,
            final String targetPath,
            final SourceRow sourceRow,
            final MigrationResult result) {
        if (isAutoField(targetField)) {
            return null;
        }
        if (targetField.defaultValue() != null) {
            result.addValueAssignment(
                    sourceRow.entityName(),
                    sourceRow.displayIdentifier(),
                    targetPath,
                    "",
                    targetField.defaultValue(),
                    "Used target default");
            return targetField.defaultValue();
        }
        String fallback = fallbackFor(targetField);
        if (fallback == null) {
            return null;
        }
        result.addValueAssignment(
                sourceRow.entityName(),
                sourceRow.displayIdentifier(),
                targetPath,
                "",
                fallback,
                "Used target type fallback");
        return fallback;
    }

    private String coerce(
            final String sourceValue,
            final FieldDefinitionSpec targetField,
            final String targetPath,
            final SourceRow sourceRow,
            final MigrationResult result) {
        String targetType = normalizedType(targetField);
        try {
            if ("boolean".equals(targetType)) {
                if ("true".equalsIgnoreCase(sourceValue) || "false".equalsIgnoreCase(sourceValue)) {
                    return Boolean.valueOf(sourceValue).toString();
                }
                return fallbackAfterFailedCoercion(
                        sourceValue, targetField, targetPath, sourceRow, result);
            }
            if ("integer".equals(targetType) || "auto-increment".equals(targetType)) {
                BigDecimal numeric = new BigDecimal(sourceValue);
                BigDecimal fractional =
                        numeric.abs().subtract(new BigDecimal(numeric.abs().toBigInteger()));
                if (fractional.compareTo(BigDecimal.ZERO) == 0) {
                    return String.valueOf(numeric.intValue());
                }
                return fallbackAfterFailedCoercion(
                        sourceValue, targetField, targetPath, sourceRow, result);
            }
            if ("float".equals(targetType)) {
                return Float.valueOf(sourceValue).toString();
            }
            if ("enum".equals(targetType)
                    && !targetField.examples().isEmpty()
                    && !targetField.examples().contains(sourceValue)) {
                return fallbackAfterFailedCoercion(
                        sourceValue, targetField, targetPath, sourceRow, result);
            }
            return sourceValue;
        } catch (NumberFormatException | ArithmeticException e) {
            return fallbackAfterFailedCoercion(
                    sourceValue, targetField, targetPath, sourceRow, result);
        }
    }

    private String fallbackAfterFailedCoercion(
            final String sourceValue,
            final FieldDefinitionSpec targetField,
            final String targetPath,
            final SourceRow sourceRow,
            final MigrationResult result) {
        String fallback = defaultOrFallback(targetField, targetPath, sourceRow, result);
        result.addWarning(
                "Could not coerce "
                        + sourceRow.entityName()
                        + " "
                        + sourceRow.displayIdentifier()
                        + " field "
                        + targetPath
                        + " value "
                        + sourceValue
                        + "; used "
                        + fallback);
        return fallback;
    }

    private void addValueToDraft(
            final EntityInstanceDraft draft,
            final FieldDefinitionSpec field,
            final String fieldPath,
            final String value) {
        if (isAutoField(field)) {
            draft.withProtectedField(fieldPath, value);
        } else {
            draft.withField(fieldPath, value);
        }
    }

    private void migrateRelationship(
            final SourceWorkspaceData sourceData,
            final ThingStore targetStore,
            final RelationshipMapping relationship,
            final Map<String, EntityInstance> migratedBySourceInternalId,
            final MigrationResult result) {
        for (SourceEdge edge :
                sourceData.edgesFor(relationship.sourceFromEntity(), relationship.sourceName())) {
            EntityInstance from = migratedBySourceInternalId.get(edge.fromInternalId());
            EntityInstance to = migratedBySourceInternalId.get(edge.toInternalId());
            if (from == null || to == null) {
                result.incrementSummary("droppedEdges");
                continue;
            }
            try {
                targetStore.relationships().connect(from, relationship.targetName(), to);
                result.incrementSummary("preservedEdges");
            } catch (IllegalArgumentException | IllegalStateException e) {
                result.addBlockingError(
                        "relationships."
                                + relationship.targetFromEntity()
                                + "."
                                + relationship.targetName(),
                        "Could not migrate relationship "
                                + relationship.targetName()
                                + ": "
                                + e.getMessage());
                throw e;
            }
        }
    }

    private void validateFinalData(
            final ThingifierModelDefinition targetDefinition,
            final Thingifier targetThingifier,
            final MigrationResult result) {
        ThingStore store = targetThingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
        for (EntityDefinitionSpec entitySpec : targetDefinition.entities()) {
            EntityDefinition entity = targetThingifier.getDefinitionNamed(entitySpec.name());
            for (EntityInstance instance : store.entityQueries().list(entity)) {
                ValidationReport fieldValidation = instance.validateFieldValues(List.of(), true);
                if (!fieldValidation.isValid()) {
                    result.addBlockingError(
                            "entities." + entitySpec.name(),
                            fieldValidation.getCombinedErrorMessages());
                }
                ValidationReport relationshipValidation = store.relationships().validate(instance);
                if (!relationshipValidation.isValid()) {
                    result.addBlockingError(
                            "relationships." + entitySpec.name(),
                            relationshipValidation.getCombinedErrorMessages());
                }
            }
        }
    }

    private SourceWorkspaceData sourceDataFrom(final WorkspaceSnapshot snapshot) {
        SourceWorkspaceData data = new SourceWorkspaceData(snapshot.definition());
        ThingStore store = snapshot.thingifier().getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
        for (EntityDefinitionSpec entitySpec : snapshot.definition().entities()) {
            EntityDefinition entity = snapshot.thingifier().getDefinitionNamed(entitySpec.name());
            if (entity == null) {
                continue;
            }
            for (EntityInstance instance : store.entityQueries().list(entity)) {
                data.addRow(sourceRowFor(entitySpec, instance));
            }
        }
        for (uk.co.compendiumdev.thingifier.application.schema.definition.RelationshipDefinitionSpec
                relationship : snapshot.definition().relationships()) {
            EntityDefinition sourceEntity =
                    snapshot.thingifier().getDefinitionNamed(relationship.fromEntityName());
            if (sourceEntity == null) {
                continue;
            }
            for (EntityInstance instance : store.entityQueries().list(sourceEntity)) {
                for (EntityInstance related :
                        store.relationships().listRelated(instance, relationship.name())) {
                    data.addEdge(
                            new SourceEdge(
                                    relationship.fromEntityName(),
                                    relationship.name(),
                                    instance.getInternalId(),
                                    related.getInternalId()));
                }
            }
        }
        return data;
    }

    private SourceRow sourceRowFor(
            final EntityDefinitionSpec entitySpec, final EntityInstance instance) {
        Map<String, String> values = new LinkedHashMap<>();
        for (FieldDefinitionSpec field : entitySpec.fields()) {
            collectFieldValues(values, field, field.name(), instance.getFieldValue(field.name()));
        }
        String identifier = "";
        if (entitySpec.hasPrimaryKeyField()) {
            identifier = values.getOrDefault(entitySpec.primaryKeyFieldName(), "");
        }
        return new SourceRow(entitySpec.name(), instance.getInternalId(), identifier, values);
    }

    private void collectFieldValues(
            final Map<String, String> values,
            final FieldDefinitionSpec field,
            final String path,
            final FieldValue value) {
        if (value == null) {
            return;
        }
        if ("object".equals(normalizedType(field))) {
            InstanceFields object = value.asObject();
            if (object == null) {
                return;
            }
            for (FieldDefinitionSpec child : field.objectFields()) {
                collectObjectFieldValues(values, child, path + "." + child.name(), object);
            }
            return;
        }
        values.put(path, value.asString());
    }

    private void collectObjectFieldValues(
            final Map<String, String> values,
            final FieldDefinitionSpec field,
            final String path,
            final InstanceFields object) {
        FieldValue value = object.getFieldValue(field.name());
        collectFieldValues(values, field, path, value);
    }

    private UpgradeRequest requestFrom(final String jsonText, final boolean apply) {
        Map<?, ?> root =
                JsonSupport.fromJsonMap(
                        jsonText,
                        "Schema upgrade request must contain a JSON object",
                        "Could not parse schema upgrade JSON");
        Object draftValue = root.get("draft");
        if (!(draftValue instanceof Map)) {
            throw new CrudUiException(400, "Schema upgrade request must contain a draft object");
        }
        Long expected = nullableLong(root.get("expectedWorkspaceVersion"));
        if (apply && expected == null) {
            throw new CrudUiException(400, "expectedWorkspaceVersion is required");
        }
        return new UpgradeRequest(
                draftJson.fromMap((Map<?, ?>) draftValue),
                expected == null ? -1L : expected,
                expected != null,
                ManualMappings.from(mapValue(root.get("mappings"))));
    }

    private String fallbackFor(final FieldDefinitionSpec field) {
        String type = normalizedType(field);
        if ("boolean".equals(type)) {
            return "false";
        }
        if ("integer".equals(type) || "auto-increment".equals(type)) {
            return "0";
        }
        if ("float".equals(type)) {
            return "0.0";
        }
        if ("enum".equals(type)) {
            return field.examples().isEmpty() ? "" : field.examples().get(0);
        }
        if ("object".equals(type) || "auto-guid".equals(type)) {
            return null;
        }
        return "";
    }

    private FieldDefinitionSpec childFieldNamed(
            final FieldDefinitionSpec field, final String childName) {
        for (FieldDefinitionSpec child : field.objectFields()) {
            if (child.name().equals(childName)) {
                return child;
            }
        }
        return null;
    }

    private boolean isAutoField(final FieldDefinitionSpec field) {
        return "auto-increment".equals(normalizedType(field))
                || "auto-guid".equals(normalizedType(field));
    }

    private static String normalizedType(final FieldDefinitionSpec field) {
        return field.type() == null ? "" : field.type().trim().toLowerCase(Locale.ROOT);
    }

    private Long nullableLong(final Object value) {
        if (value == null || stringValue(value).trim().isEmpty()) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(stringValue(value));
    }

    private static Map<?, ?> mapValue(final Object value) {
        if (value instanceof Map) {
            return (Map<?, ?>) value;
        }
        return Map.of();
    }

    private static List<?> listValue(final Object value) {
        if (value instanceof List) {
            return (List<?>) value;
        }
        return List.of();
    }

    private static String stringValue(final Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static final class UpgradeRequest {
        private final ThingifierModelDefinition definition;
        private final long expectedWorkspaceVersion;
        private final boolean hasExpectedWorkspaceVersion;
        private final ManualMappings mappings;

        private UpgradeRequest(
                final ThingifierModelDefinition definition,
                final long expectedWorkspaceVersion,
                final boolean hasExpectedWorkspaceVersion,
                final ManualMappings mappings) {
            this.definition = definition;
            this.expectedWorkspaceVersion = expectedWorkspaceVersion;
            this.hasExpectedWorkspaceVersion = hasExpectedWorkspaceVersion;
            this.mappings = mappings;
        }

        private ThingifierModelDefinition definition() {
            return definition;
        }

        private long expectedWorkspaceVersion() {
            return expectedWorkspaceVersion;
        }

        private boolean hasExpectedWorkspaceVersion() {
            return hasExpectedWorkspaceVersion;
        }

        private ManualMappings mappings() {
            return mappings;
        }
    }

    private static final class ManualMappings {
        private final Map<String, String> entityMappings;
        private final Map<String, Map<String, String>> fieldMappings;
        private final List<RelationshipMapping> relationshipMappings;

        private ManualMappings(
                final Map<String, String> entityMappings,
                final Map<String, Map<String, String>> fieldMappings,
                final List<RelationshipMapping> relationshipMappings) {
            this.entityMappings = entityMappings;
            this.fieldMappings = fieldMappings;
            this.relationshipMappings = relationshipMappings;
        }

        private static ManualMappings from(final Map<?, ?> root) {
            Map<String, String> entityMappings = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : mapValue(root.get("entityMappings")).entrySet()) {
                String target = stringValue(entry.getKey());
                String source = stringValue(entry.getValue());
                if (!target.isBlank() && !source.isBlank()) {
                    entityMappings.put(target, source);
                }
            }

            Map<String, Map<String, String>> fieldMappings = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entityEntry : mapValue(root.get("fieldMappings")).entrySet()) {
                Map<String, String> fields = new LinkedHashMap<>();
                for (Map.Entry<?, ?> fieldEntry : mapValue(entityEntry.getValue()).entrySet()) {
                    String targetField = stringValue(fieldEntry.getKey());
                    String sourceField = stringValue(fieldEntry.getValue());
                    if (!targetField.isBlank() && !sourceField.isBlank()) {
                        fields.put(targetField, sourceField);
                    }
                }
                if (!fields.isEmpty()) {
                    fieldMappings.put(stringValue(entityEntry.getKey()), fields);
                }
            }

            List<RelationshipMapping> relationshipMappings = new ArrayList<>();
            for (Object value : listValue(root.get("relationshipMappings"))) {
                Map<?, ?> map = mapValue(value);
                RelationshipMapping relationship =
                        new RelationshipMapping(
                                stringValue(map.get("targetFromEntity")),
                                stringValue(map.get("targetName")),
                                stringValue(map.get("sourceFromEntity")),
                                stringValue(map.get("sourceName")));
                if (relationship.isComplete()) {
                    relationshipMappings.add(relationship);
                }
            }
            return new ManualMappings(entityMappings, fieldMappings, relationshipMappings);
        }

        private Map<String, Object> asMap() {
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("entityMappings", entityMappings);
            values.put("fieldMappings", fieldMappings);
            values.put("relationshipMappings", relationshipMappingMaps(relationshipMappings));
            return values;
        }
    }

    private static final class EffectiveMappings {
        private final Map<String, String> entityMappings;
        private final Map<String, Map<String, String>> fieldMappings;
        private final Map<String, RelationshipMapping> relationshipMappings;
        private final List<String> warnings;

        private EffectiveMappings(
                final Map<String, String> entityMappings,
                final Map<String, Map<String, String>> fieldMappings,
                final Map<String, RelationshipMapping> relationshipMappings,
                final List<String> warnings) {
            this.entityMappings = entityMappings;
            this.fieldMappings = fieldMappings;
            this.relationshipMappings = relationshipMappings;
            this.warnings = warnings;
        }

        private static EffectiveMappings from(
                final ThingifierModelDefinition source,
                final ThingifierModelDefinition target,
                final ManualMappings manual) {
            Map<String, String> entityMappings = entityMappings(source, target, manual);
            Map<String, Map<String, String>> fieldMappings =
                    fieldMappings(source, target, manual, entityMappings);
            Map<String, RelationshipMapping> relationshipMappings =
                    relationshipMappings(source, target, manual, entityMappings);
            List<String> warnings =
                    warnings(source, target, entityMappings, fieldMappings, relationshipMappings);
            return new EffectiveMappings(
                    entityMappings, fieldMappings, relationshipMappings, warnings);
        }

        private static Map<String, String> entityMappings(
                final ThingifierModelDefinition source,
                final ThingifierModelDefinition target,
                final ManualMappings manual) {
            Map<String, String> mappings = new LinkedHashMap<>();
            for (EntityDefinitionSpec targetEntity : target.entities()) {
                String sourceName = manual.entityMappings.get(targetEntity.name());
                if (sourceName == null && source.entityNamed(targetEntity.name()) != null) {
                    sourceName = targetEntity.name();
                }
                if (sourceName != null && source.entityNamed(sourceName) != null) {
                    mappings.put(targetEntity.name(), sourceName);
                }
            }
            return mappings;
        }

        private static Map<String, Map<String, String>> fieldMappings(
                final ThingifierModelDefinition source,
                final ThingifierModelDefinition target,
                final ManualMappings manual,
                final Map<String, String> entityMappings) {
            Map<String, Map<String, String>> mappings = new LinkedHashMap<>();
            for (EntityDefinitionSpec targetEntity : target.entities()) {
                EntityDefinitionSpec sourceEntity =
                        source.entityNamed(entityMappings.get(targetEntity.name()));
                if (sourceEntity == null) {
                    continue;
                }
                Map<String, String> entityFieldMappings = new LinkedHashMap<>();
                Map<String, String> manualFields =
                        manual.fieldMappings.getOrDefault(targetEntity.name(), Map.of());
                for (FieldDefinitionSpec targetField : targetEntity.fields()) {
                    String sourceFieldName = manualFields.get(targetField.name());
                    if (sourceFieldName == null
                            && sourceEntity.fieldNamed(targetField.name()) != null) {
                        sourceFieldName = targetField.name();
                    }
                    if (sourceFieldName != null
                            && sourceEntity.fieldNamed(sourceFieldName) != null) {
                        entityFieldMappings.put(targetField.name(), sourceFieldName);
                    }
                }
                mappings.put(targetEntity.name(), entityFieldMappings);
            }
            return mappings;
        }

        private static Map<String, RelationshipMapping> relationshipMappings(
                final ThingifierModelDefinition source,
                final ThingifierModelDefinition target,
                final ManualMappings manual,
                final Map<String, String> entityMappings) {
            Map<String, RelationshipMapping> mappings = new LinkedHashMap<>();
            for (var relationship : target.relationships()) {
                String targetKey =
                        relationshipKey(relationship.fromEntityName(), relationship.name());
                RelationshipMapping manualMapping = manualRelationshipFor(manual, targetKey);
                if (manualMapping != null && sourceRelationshipExists(source, manualMapping)) {
                    mappings.put(targetKey, manualMapping);
                    continue;
                }
                String sourceFrom = entityMappings.get(relationship.fromEntityName());
                RelationshipMapping exact =
                        new RelationshipMapping(
                                relationship.fromEntityName(),
                                relationship.name(),
                                sourceFrom,
                                relationship.name());
                if (sourceFrom != null && sourceRelationshipExists(source, exact)) {
                    mappings.put(targetKey, exact);
                }
            }
            return mappings;
        }

        private static RelationshipMapping manualRelationshipFor(
                final ManualMappings manual, final String targetKey) {
            for (RelationshipMapping relationship : manual.relationshipMappings) {
                if (targetKey.equals(
                        relationshipKey(
                                relationship.targetFromEntity(), relationship.targetName()))) {
                    return relationship;
                }
            }
            return null;
        }

        private static boolean sourceRelationshipExists(
                final ThingifierModelDefinition source, final RelationshipMapping mapping) {
            for (var relationship : source.relationships()) {
                if (relationship.fromEntityName().equals(mapping.sourceFromEntity())
                        && relationship.name().equals(mapping.sourceName())) {
                    return true;
                }
            }
            return false;
        }

        private static List<String> warnings(
                final ThingifierModelDefinition source,
                final ThingifierModelDefinition target,
                final Map<String, String> entityMappings,
                final Map<String, Map<String, String>> fieldMappings,
                final Map<String, RelationshipMapping> relationshipMappings) {
            List<String> warnings = new ArrayList<>();
            Set<String> mappedSourceEntities = new LinkedHashSet<>(entityMappings.values());
            for (EntityDefinitionSpec sourceEntity : source.entities()) {
                if (!mappedSourceEntities.contains(sourceEntity.name())) {
                    warnings.add("Source entity " + sourceEntity.name() + " will be dropped");
                }
            }
            for (EntityDefinitionSpec targetEntity : target.entities()) {
                String sourceEntityName = entityMappings.get(targetEntity.name());
                if (sourceEntityName == null) {
                    warnings.add("Target entity " + targetEntity.name() + " will be added empty");
                    continue;
                }
                EntityDefinitionSpec sourceEntity = source.entityNamed(sourceEntityName);
                warnAboutFields(targetEntity, sourceEntity, fieldMappings, warnings);
            }
            Set<String> mappedSourceRelationships = new LinkedHashSet<>();
            for (RelationshipMapping relationship : relationshipMappings.values()) {
                mappedSourceRelationships.add(
                        relationshipKey(
                                relationship.sourceFromEntity(), relationship.sourceName()));
            }
            for (var relationship : source.relationships()) {
                if (!mappedSourceRelationships.contains(
                        relationshipKey(relationship.fromEntityName(), relationship.name()))) {
                    warnings.add(
                            "Source relationship "
                                    + relationship.fromEntityName()
                                    + "."
                                    + relationship.name()
                                    + " will be dropped");
                }
            }
            return warnings;
        }

        private static void warnAboutFields(
                final EntityDefinitionSpec targetEntity,
                final EntityDefinitionSpec sourceEntity,
                final Map<String, Map<String, String>> fieldMappings,
                final List<String> warnings) {
            Set<String> mappedSourceFields =
                    new LinkedHashSet<>(
                            fieldMappings.getOrDefault(targetEntity.name(), Map.of()).values());
            for (FieldDefinitionSpec sourceField : sourceEntity.fields()) {
                if (!mappedSourceFields.contains(sourceField.name())) {
                    warnings.add(
                            "Source field "
                                    + sourceEntity.name()
                                    + "."
                                    + sourceField.name()
                                    + " will be dropped");
                }
            }
            for (FieldDefinitionSpec targetField : targetEntity.fields()) {
                if (!fieldMappings
                        .getOrDefault(targetEntity.name(), Map.of())
                        .containsKey(targetField.name())) {
                    warnings.add(
                            "Target field "
                                    + targetEntity.name()
                                    + "."
                                    + targetField.name()
                                    + " will be added");
                }
            }
        }

        private String sourceEntityFor(final String targetEntityName) {
            return entityMappings.get(targetEntityName);
        }

        private String sourceFieldFor(final String targetEntityName, final String targetFieldName) {
            return fieldMappings.getOrDefault(targetEntityName, Map.of()).get(targetFieldName);
        }

        private Collection<RelationshipMapping> relationshipMappings() {
            return relationshipMappings.values();
        }

        private List<String> warnings() {
            return warnings;
        }

        private Map<String, Object> asMap() {
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("entityMappings", entityMappings);
            values.put("fieldMappings", fieldMappings);
            values.put(
                    "relationshipMappings", relationshipMappingMaps(relationshipMappings.values()));
            return values;
        }

        private Map<String, Object> summaryMap(final SourceWorkspaceData sourceData) {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("sourceEntities", sourceData.sourceDefinition().entities().size());
            summary.put("targetEntities", entityMappings.size());
            summary.put("migratedInstances", 0);
            summary.put("preservedEdges", 0);
            summary.put("droppedEdges", 0);
            return summary;
        }
    }

    private static List<Map<String, Object>> relationshipMappingMaps(
            final Collection<RelationshipMapping> relationshipMappings) {
        List<Map<String, Object>> values = new ArrayList<>();
        for (RelationshipMapping relationship : relationshipMappings) {
            values.add(relationship.asMap());
        }
        return values;
    }

    private static String relationshipKey(final String fromEntity, final String name) {
        return fromEntity + "|" + name;
    }

    private static final class RelationshipMapping {
        private final String targetFromEntity;
        private final String targetName;
        private final String sourceFromEntity;
        private final String sourceName;

        private RelationshipMapping(
                final String targetFromEntity,
                final String targetName,
                final String sourceFromEntity,
                final String sourceName) {
            this.targetFromEntity = targetFromEntity;
            this.targetName = targetName;
            this.sourceFromEntity = sourceFromEntity;
            this.sourceName = sourceName;
        }

        private boolean isComplete() {
            return !targetFromEntity.isBlank()
                    && !targetName.isBlank()
                    && !sourceFromEntity.isBlank()
                    && !sourceName.isBlank();
        }

        private String targetFromEntity() {
            return targetFromEntity;
        }

        private String targetName() {
            return targetName;
        }

        private String sourceFromEntity() {
            return sourceFromEntity;
        }

        private String sourceName() {
            return sourceName;
        }

        private Map<String, Object> asMap() {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("targetFromEntity", targetFromEntity);
            value.put("targetName", targetName);
            value.put("sourceFromEntity", sourceFromEntity);
            value.put("sourceName", sourceName);
            return value;
        }
    }

    private static final class SourceWorkspaceData {
        private final ThingifierModelDefinition sourceDefinition;
        private final Map<String, List<SourceRow>> rowsByEntity;
        private final List<SourceEdge> edges;

        private SourceWorkspaceData(final ThingifierModelDefinition sourceDefinition) {
            this.sourceDefinition = sourceDefinition;
            this.rowsByEntity = new LinkedHashMap<>();
            this.edges = new ArrayList<>();
        }

        private ThingifierModelDefinition sourceDefinition() {
            return sourceDefinition;
        }

        private void addRow(final SourceRow row) {
            rowsByEntity.computeIfAbsent(row.entityName(), ignored -> new ArrayList<>()).add(row);
        }

        private void addEdge(final SourceEdge edge) {
            edges.add(edge);
        }

        private List<SourceRow> rowsFor(final String entityName) {
            return rowsByEntity.getOrDefault(entityName, List.of());
        }

        private List<SourceEdge> edgesFor(final String fromEntity, final String relationshipName) {
            List<SourceEdge> matches = new ArrayList<>();
            for (SourceEdge edge : edges) {
                if (edge.fromEntity().equals(fromEntity)
                        && edge.relationshipName().equals(relationshipName)) {
                    matches.add(edge);
                }
            }
            return matches;
        }
    }

    private static final class SourceRow {
        private final String entityName;
        private final String internalId;
        private final String displayIdentifier;
        private final Map<String, String> values;

        private SourceRow(
                final String entityName,
                final String internalId,
                final String displayIdentifier,
                final Map<String, String> values) {
            this.entityName = entityName;
            this.internalId = internalId;
            this.displayIdentifier =
                    displayIdentifier == null || displayIdentifier.isBlank()
                            ? internalId
                            : displayIdentifier;
            this.values = values;
        }

        private String entityName() {
            return entityName;
        }

        private String internalId() {
            return internalId;
        }

        private String displayIdentifier() {
            return displayIdentifier;
        }

        private String valueFor(final String fieldName) {
            return values.get(fieldName);
        }
    }

    private static final class SourceEdge {
        private final String fromEntity;
        private final String relationshipName;
        private final String fromInternalId;
        private final String toInternalId;

        private SourceEdge(
                final String fromEntity,
                final String relationshipName,
                final String fromInternalId,
                final String toInternalId) {
            this.fromEntity = fromEntity;
            this.relationshipName = relationshipName;
            this.fromInternalId = fromInternalId;
            this.toInternalId = toInternalId;
        }

        private String fromEntity() {
            return fromEntity;
        }

        private String relationshipName() {
            return relationshipName;
        }

        private String fromInternalId() {
            return fromInternalId;
        }

        private String toInternalId() {
            return toInternalId;
        }
    }

    private static final class MigrationResult {
        private final Map<String, Object> body;
        private final List<Map<String, Object>> errors;
        private final List<String> warnings;
        private final List<Map<String, Object>> coercions;
        private final List<Map<String, Object>> valueAssignments;
        private Thingifier targetThingifier;
        private WorkspaceStorage targetStorage;
        private int statusCode;

        private MigrationResult(final long workspaceVersion) {
            body = new LinkedHashMap<>();
            errors = new ArrayList<>();
            warnings = new ArrayList<>();
            coercions = new ArrayList<>();
            valueAssignments = new ArrayList<>();
            statusCode = 400;
            body.put("valid", false);
            body.put("canApply", false);
            body.put("workspaceVersion", workspaceVersion);
            body.put("summary", new LinkedHashMap<String, Object>());
            body.put("mappings", new LinkedHashMap<String, Object>());
            body.put("coercions", coercions);
            body.put("valueAssignments", valueAssignments);
            body.put("warnings", warnings);
            body.put("errors", errors);
            body.put("yaml", "");
            body.put("canonicalTargetYaml", "");
        }

        private void putRequestMappings(final Map<String, Object> mappings) {
            body.put("requestMappings", mappings);
        }

        private void putSchemaValidation(final SchemaDefinitionValidationReport report) {
            body.put("valid", report.isValid());
            for (SchemaDefinitionValidationReport.SchemaDefinitionValidationError error :
                    report.errors()) {
                addBlockingError(error.path(), error.message());
            }
        }

        private void putYaml(final String yaml) {
            body.put("yaml", yaml);
            body.put("canonicalTargetYaml", yaml);
        }

        private void putEffectiveMappings(final Map<String, Object> mappings) {
            body.put("mappings", mappings);
        }

        private void putSummary(final Map<String, Object> summary) {
            body.put("summary", summary);
        }

        private void incrementEntityCount(final String entityName) {
            Map<String, Object> summary = summary();
            Map<String, Integer> entityCounts =
                    (Map<String, Integer>)
                            summary.computeIfAbsent(
                                    "migratedEntityRows", ignored -> new LinkedHashMap<>());
            entityCounts.put(entityName, entityCounts.getOrDefault(entityName, 0) + 1);
        }

        private void incrementSummary(final String key) {
            Map<String, Object> summary = summary();
            Object existing = summary.get(key);
            int value = existing instanceof Number ? ((Number) existing).intValue() : 0;
            summary.put(key, value + 1);
        }

        private Map<String, Object> summary() {
            return (Map<String, Object>) body.get("summary");
        }

        private void addWarnings(final List<String> values) {
            warnings.addAll(values);
        }

        private void addWarning(final String warning) {
            warnings.add(warning);
        }

        private void addCoercion(
                final String entity,
                final String identifier,
                final String field,
                final String from,
                final String to,
                final String reason) {
            Map<String, Object> coercion = new LinkedHashMap<>();
            coercion.put("entity", entity);
            coercion.put("identifier", identifier);
            coercion.put("field", field);
            coercion.put("from", from);
            coercion.put("to", to);
            coercion.put("reason", reason);
            coercions.add(coercion);
        }

        private void addValueAssignment(
                final String entity,
                final String identifier,
                final String field,
                final String from,
                final String to,
                final String reason) {
            Map<String, Object> assignment = new LinkedHashMap<>();
            assignment.put("entity", entity);
            assignment.put("identifier", identifier);
            assignment.put("field", field);
            assignment.put("from", from);
            assignment.put("to", to);
            assignment.put("reason", reason);
            valueAssignments.add(assignment);
        }

        private void addBlockingError(final String path, final String message) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("path", path);
            error.put("message", message);
            errors.add(error);
        }

        private void targetThingifier(
                final Thingifier targetThingifier, final WorkspaceStorage targetStorage) {
            this.targetThingifier = targetThingifier;
            this.targetStorage = targetStorage;
        }

        private Thingifier targetThingifier() {
            return targetThingifier;
        }

        private WorkspaceStorage targetStorage() {
            return targetStorage == null ? WorkspaceStorage.memory() : targetStorage;
        }

        private boolean canApply() {
            return Boolean.TRUE.equals(body.get("canApply"));
        }

        private int statusCode() {
            return statusCode;
        }

        private void finish() {
            boolean valid = Boolean.TRUE.equals(body.get("valid"));
            boolean canApply = valid && errors.isEmpty() && targetThingifier != null;
            body.put("canApply", canApply);
            statusCode = staleWorkspace() ? 409 : 400;
        }

        private boolean staleWorkspace() {
            for (Map<String, Object> error : errors) {
                if ("workspaceVersion".equals(error.get("path"))
                        && STALE_WORKSPACE_MESSAGE.equals(error.get("message"))) {
                    return true;
                }
            }
            return false;
        }

        private void markApplied(
                final WorkspaceSnapshot upgraded, final Map<String, Object> workspaceMetadata) {
            body.put("workspaceVersion", upgraded.version());
            body.put("workspace", workspaceMetadata);
            body.put("applied", true);
            body.put("canApply", true);
            statusCode = 200;
        }

        private Map<String, Object> body() {
            return body;
        }

        private void releaseTargetThingifier() {
            targetThingifier = null;
            targetStorage = null;
        }

        private void closeTargetThingifier() {
            if (targetThingifier != null) {
                targetThingifier.close();
                targetThingifier = null;
                targetStorage = null;
            }
        }
    }
}
