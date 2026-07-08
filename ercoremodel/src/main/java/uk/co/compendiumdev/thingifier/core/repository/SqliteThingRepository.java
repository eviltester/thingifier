package uk.co.compendiumdev.thingifier.core.repository;

import org.sqlite.Function;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.AutoIncrement;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.EntityInstanceListSorter;
import uk.co.compendiumdev.thingifier.core.query.EntityListSortParamParser;
import uk.co.compendiumdev.thingifier.core.query.FilterBy;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.query.SortByFieldName;
import uk.co.compendiumdev.thingifier.core.reporting.RepositoryJsonExporter;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SqliteThingRepository implements ThingRepository {

    private static final Logger LOGGER =
            Logger.getLogger(SqliteThingRepository.class.getName());
    private static final String INTERNAL_ID_COLUMN = "__internal_id";
    private static final String COUNTERS_TABLE = "__thingifier_counters";

    private final String databaseKey;
    private final String jdbcUrl;
    private final Map<String, EntityInstance> materializedInstances;
    private final Map<String, Map<String, AutoIncrement>> countersByEntity;
    private ERSchema schema;
    private Connection connection;
    private boolean closed;

    public SqliteThingRepository(final String databaseKey, final String jdbcUrl) {
        this.databaseKey = databaseKey;
        this.jdbcUrl = jdbcUrl;
        this.materializedInstances = new HashMap<>();
        this.countersByEntity = new HashMap<>();
    }

    public static SqliteThingRepository inMemory(final String databaseKey) {
        return new SqliteThingRepository(databaseKey, "jdbc:sqlite::memory:");
    }

    public static SqliteThingRepository fileBacked(final String databaseKey, final Path databasePath) {
        String path = databasePath.toAbsolutePath().toString().replace("\\", "/");
        return new SqliteThingRepository(databaseKey, "jdbc:sqlite:" + path);
    }

    @Override
    public String databaseKey() {
        return databaseKey;
    }

    @Override
    public void initializeFrom(final ERSchema schema) {
        this.schema = schema;
        openConnection();
        createMetadataTables();
        refreshSchema(schema);
    }

    @Override
    public String exportDataAsJson(final ERSchema schema) {
        ensureSchemaReady();

        StringBuilder json = new StringBuilder();
        json.append("{");

        String entitySeparator = "";
        for (EntityDefinition entity : schema.getEntityDefinitions()) {
            json.append(entitySeparator).
                    append(RepositoryJsonExporter.quoted(entity.getPlural())).
                    append(" : [");

            appendEntityRowsAsJson(json, entity);

            json.append("]");
            entitySeparator = ", ";
        }

        json.append("}");
        return json.toString();
    }

    @Override
    public void refreshSchema(final ERSchema schema) {
        this.schema = schema;
        openConnection();
        for (EntityDefinition entity : schema.getEntityDefinitions()) {
            initializeCountersFor(entity);
            createEntityTable(entity);
            restoreCountersFor(entity);
        }
        for (RelationshipVectorDefinition vector : relationshipVectors()) {
            createRelationshipTable(vector);
        }
    }

    @Override
    public EntityInstance findEntityInstanceByGUID(final String thingGUID) {
        ensureSchemaReady();
        for (EntityDefinition entity : schema.getEntityDefinitions()) {
            for (Field guidField : entity.getFieldsOfType(FieldType.AUTO_GUID)) {
                EntityInstance instance = findInstanceByFieldNameAndValue(
                        entity, guidField.getName(), thingGUID);
                if (instance != null) {
                    return instance;
                }
            }
        }
        return null;
    }

    @Override
    public EntityInstance findInstanceByPrimaryKey(
            final EntityDefinition entity, final String primaryKeyValue) {
        if (!entity.hasPrimaryKeyField()) {
            return null;
        }
        return findInstanceByFieldNameAndValue(
                entity, entity.getPrimaryKeyField().getName(), primaryKeyValue);
    }

    @Override
    public EntityInstance findInstanceByFieldNameAndValue(
            final EntityDefinition entity, final String fieldName, final String fieldValue) {
        ensureSchemaReady();
        if (!entity.hasFieldNameDefined(fieldName)) {
            return null;
        }

        String sql = "SELECT * FROM " + table(entity) +
                " WHERE " + identifier(fieldName) + " = ? LIMIT 1";
        List<EntityInstance> instances = queryEntityRows(entity, sql, List.of(fieldValue));
        if (instances.isEmpty()) {
            return null;
        }
        return instances.get(0);
    }

    @Override
    public Collection<EntityInstance> listInstances(final EntityDefinition entity) {
        return listInstances(entity, new QueryFilterParams());
    }

    @Override
    public List<EntityInstance> listInstances(
            final EntityDefinition entity, final QueryFilterParams queryParams) {
        ensureSchemaReady();
        QueryFilterParams params = queryParams == null ? new QueryFilterParams() : queryParams;

        SqlQuery sqlQuery = selectInstancesSql(entity, params);
        return queryEntityRows(entity, sqlQuery.sql, sqlQuery.parameters);
    }

    @Override
    public int countInstances(final EntityDefinition entity) {
        ensureSchemaReady();
        String sql = "SELECT COUNT(*) FROM " + table(entity);
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not count SQLite entity rows for " + entity.getName(), e);
        }
    }

    @Override
    public EntityInstance findInstanceByQueryIdentifier(
            final EntityDefinition entity, final String identifierValue) {
        ensureSchemaReady();
        List<String> clauses = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        for (Field autoIncrementField : entity.getFieldsOfType(FieldType.AUTO_INCREMENT)) {
            clauses.add(identifier(autoIncrementField.getName()) + " = ?");
            parameters.add(identifierValue);
            break;
        }

        if (entity.hasPrimaryKeyField()) {
            clauses.add(identifier(entity.getPrimaryKeyField().getName()) + " = ?");
            parameters.add(identifierValue);
        }

        if (clauses.isEmpty()) {
            return null;
        }

        String sql = "SELECT * FROM " + table(entity) +
                " WHERE " + String.join(" OR ", clauses) +
                " LIMIT 1";
        List<EntityInstance> instances = queryEntityRows(entity, sql, parameters);
        if (instances.isEmpty()) {
            return null;
        }
        return instances.get(0);
    }

    @Override
    public EntityInstance addInstance(final EntityInstance instance) {
        ensureSchemaReady();
        runInTransaction(() -> {
            prepareInstanceForInsert(instance);
            persistInstance(instance);
            materializedInstances.put(instance.getInternalId(), instance);
            persistCountersFor(instance.getEntity());
        });
        return instance;
    }

    @Override
    public EntityInstance updateInstance(final EntityInstance instance) {
        ensureSchemaReady();
        runInTransaction(() -> {
            persistInstance(instance);
            materializedInstances.put(instance.getInternalId(), instance);
            persistCountersFor(instance.getEntity());
        });
        return instance;
    }

    @Override
    public void deleteEntityInstance(final EntityInstance instance) {
        ensureSchemaReady();
        runInTransaction(() -> deleteEntityInstanceAndMandatoryRelated(instance, new HashSet<>()));
    }

    @Override
    public void clearAllData() {
        ensureSchemaReady();
        runInTransaction(() -> {
            for (EntityDefinition entity : schema.getEntityDefinitions()) {
                executeSql("DELETE FROM " + table(entity));
                resetCountersFor(entity);
                persistCountersFor(entity);
            }
            clearRelationshipRows();
            materializedInstances.clear();
        });
    }

    @Override
    public void clearInstanceDataFor(final String entityName) {
        ensureSchemaReady();
        EntityDefinition entity = schema.getEntityDefinitionNamed(entityName);
        if (entity == null) {
            return;
        }
        runInTransaction(() -> {
            Set<String> internalIds = internalIdsFor(entity);
            executeSql("DELETE FROM " + table(entity));
            deleteRelationshipRowsInvolving(internalIds);
            resetCountersFor(entity);
            persistCountersFor(entity);
            materializedInstances.entrySet().
                    removeIf(entry -> entry.getValue().getEntity() == entity);
        });
    }

    @Override
    public void connectRelationship(
            final EntityInstance from, final String relationshipName, final EntityInstance to) {
        ensureSchemaReady();
        from.getRelationships().connect(relationshipName, to);
        runInTransaction(() -> persistRelationship(from, relationshipName, to));
    }

    @Override
    public List<EntityInstance> removeRelationshipsInvolving(
            final EntityInstance parent,
            final EntityInstance child,
            final String relationshipName) {
        ensureSchemaReady();
        materializeRelationshipsInvolving(parent, child, relationshipName);
        List<EntityInstance> removed = parent.getRelationships().
                removeRelationshipsInvolving(child, relationshipName);
        runInTransaction(() -> deleteRelationshipRowsInvolving(parent, child, relationshipName));
        return removed;
    }

    @Override
    public List<EntityInstance> removeAllRelationships(final EntityInstance instance) {
        ensureSchemaReady();
        materializeRelationshipsFor(instance);
        List<EntityInstance> removed = instance.getRelationships().removeAllRelationships();
        runInTransaction(() -> deleteRelationshipRowsInvolving(instance));
        return removed;
    }

    @Override
    public Collection<EntityInstance> getConnectedItems(
            final EntityInstance instance, final String relationshipName) {
        return listRelatedInstances(instance, relationshipName, new QueryFilterParams());
    }

    @Override
    public List<EntityInstance> listRelatedInstances(
            final EntityInstance instance,
            final String relationshipName,
            final QueryFilterParams queryParams) {
        ensureSchemaReady();
        QueryFilterParams params = queryParams == null ? new QueryFilterParams() : queryParams;

        Map<String, EntityInstance> related = new LinkedHashMap<>();
        for (RelationshipVectorDefinition vector :
                relationshipVectorsFor(instance, relationshipName)) {
            SqlQuery query = selectRelatedInstancesSql(instance, vector, params);
            if (query == null) {
                continue;
            }
            for (EntityInstance item : queryEntityRows(query.entity, query.sql, query.parameters)) {
                related.put(item.getInternalId(), item);
            }
        }

        return new EntityInstanceListSorter(params).
                sort(new ArrayList<>(related.values()));
    }

    @Override
    public ValidationReport checkFieldsForUniqueNess(
            final EntityInstance instance, final boolean isAmendment) {
        ensureSchemaReady();

        ValidationReport report = new ValidationReport();
        for (String fieldName : instance.getEntity().getFieldNames()) {
            Field field = instance.getEntity().getField(fieldName);
            if (!field.mustBeUnique()) {
                continue;
            }

            FieldValue value = instance.getFieldValue(fieldName);
            if (value == null) {
                continue;
            }

            String sql = "SELECT * FROM " + table(instance.getEntity()) +
                    " WHERE " + identifier(fieldName) + " = ?";
            List<EntityInstance> matching = queryEntityRows(
                    instance.getEntity(),
                    sql,
                    List.of(field.getActualValueToAdd(value)));
            String uniqueValue = value.asUniqueComparisonString();
            for (EntityInstance existing : matching) {
                if (isAmendment &&
                        existing.getPrimaryKeyValue().equals(instance.getPrimaryKeyValue())) {
                    continue;
                }

                FieldValue existingValue = existing.getFieldValue(fieldName);
                if (existingValue != null &&
                        uniqueValue.equals(existingValue.asUniqueComparisonString())) {
                    report.setValid(false);
                    report.addErrorMessage("Field %s Value is not unique".formatted(fieldName));
                    return report;
                }
            }
        }
        return report;
    }

    @Override
    public Map<String, AutoIncrement> countersFor(final EntityDefinition entity) {
        initializeCountersFor(entity);
        return countersByEntity.get(entity.getName());
    }

    @Override
    public void setNextIdCountersToAccomodate(
            final EntityDefinition entity, final List<NamedValue> fieldValues) {
        initializeCountersFor(entity);
        for (NamedValue fieldNameValue : fieldValues) {
            Field field = entity.getField(fieldNameValue.getName());
            if (field != null && field.getType() == FieldType.AUTO_INCREMENT) {
                AutoIncrement counter = counterFor(entity, field);
                counter.incrementToNextAbove(Integer.parseInt(fieldNameValue.value));
            }
        }
        ensureSchemaReady();
        persistCountersFor(entity);
    }

    @Override
    public void resetAutoIncrementCounter(final EntityDefinition entity, final String fieldName) {
        ensureSchemaReady();
        runInTransaction(() -> {
            resetAutoIncrementCounterInCache(entity, fieldName);
            persistCountersFor(entity);
        });
    }

    @Override
    public void close() {
        closed = true;
        if (connection == null) {
            return;
        }
        try {
            connection.close();
            connection = null;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not close SQLite repository", e);
        }
    }

    private void prepareInstanceForInsert(final EntityInstance instance) {
        EntityDefinition entity = instance.getEntity();
        initializeCountersFor(entity);

        if (entity.hasMaxInstanceLimit() && countInstances(entity) >= entity.getMaxInstanceLimit()) {
            throw new RuntimeException(
                    String.format(
                            "ERROR: Cannot add instance, maximum limit of %d reached",
                            entity.getMaxInstanceLimit()));
        }

        List<String> explicitAutoIncrementFields = new ArrayList<>();
        for (Field field : entity.getFieldsOfType(FieldType.AUTO_GUID, FieldType.AUTO_INCREMENT)) {
            if (!instance.hasInstantiatedFieldNamed(field.getName())) {
                if (field.getType() == FieldType.AUTO_GUID) {
                    instance.setValue(field.getName(), UUID.randomUUID().toString());
                }
                if (field.getType() == FieldType.AUTO_INCREMENT) {
                    instance.overrideValue(
                            field.getName(),
                            String.valueOf(counterFor(entity, field).getNextValueAndUpdate()));
                }
            } else if (field.getType() == FieldType.AUTO_INCREMENT) {
                explicitAutoIncrementFields.add(field.getName());
            }
        }

        if (entity.hasPrimaryKeyField()) {
            Field primaryField = entity.getPrimaryKeyField();
            if (!instance.hasInstantiatedFieldNamed(primaryField.getName())) {
                throw new RuntimeException(
                        String.format(
                                "ERROR: Cannot add instance, primary key field %s not set",
                                primaryField.getName()));
            }

            EntityInstance existing = findInstanceByPrimaryKey(entity, instance.getPrimaryKeyValue());
            if (existing != null && !existing.getInternalId().equals(instance.getInternalId())) {
                throw new RuntimeException(
                        "ERROR: Cannot add instance, another instance with primary key value exists: " +
                                existing.getPrimaryKeyValue());
            }
        }

        for (String fieldName : explicitAutoIncrementFields) {
            AutoIncrement counter = countersFor(entity).get(fieldName);
            int value = instance.getFieldValue(fieldName).asInteger();
            if (counter.peekNextValue() < value) {
                counter.incrementToNextAbove(value);
            }
        }
    }

    private void initializeCountersFor(final EntityDefinition entity) {
        Map<String, AutoIncrement> counters =
                countersByEntity.computeIfAbsent(entity.getName(), ignored -> new HashMap<>());
        for (Field field : entity.getFieldsOfType(FieldType.AUTO_INCREMENT)) {
            counters.computeIfAbsent(
                    field.getName(),
                    ignored -> new AutoIncrement(field.getName(), field.getDefaultValue().asInteger()));
        }
    }

    private AutoIncrement counterFor(final EntityDefinition entity, final Field field) {
        initializeCountersFor(entity);
        return countersByEntity.get(entity.getName()).
                computeIfAbsent(
                        field.getName(),
                        ignored -> new AutoIncrement(field.getName(), field.getDefaultValue().asInteger()));
    }

    private void resetCountersFor(final EntityDefinition entity) {
        countersByEntity.remove(entity.getName());
        initializeCountersFor(entity);
    }

    private void resetAutoIncrementCounterInCache(
            final EntityDefinition entity, final String fieldName) {
        Field field = entity.getField(fieldName);
        if (field == null || field.getType() != FieldType.AUTO_INCREMENT) {
            throw new IllegalArgumentException(
                    String.format("%s is not an auto-increment field on %s", fieldName, entity.getName()));
        }

        AutoIncrement counter = counterFor(entity, field);
        counter.incrementToNextAbove(field.getDefaultValue().asInteger() - 1);
    }

    private void deleteEntityInstanceAndMandatoryRelated(
            final EntityInstance instance, final Set<String> alreadyDeleting) {
        if (!alreadyDeleting.add(instance.getInternalId())) {
            return;
        }

        materializeRelationshipsFor(instance);
        List<EntityInstance> alsoDelete =
                instance.getRelationships().removeAllRelationships();
        deletePersistedInstance(instance.getInternalId());
        materializedInstances.remove(instance.getInternalId());

        for (EntityInstance deleteMe : alsoDelete) {
            if (!deleteMe.getInternalId().equals(instance.getInternalId())) {
                deleteEntityInstanceAndMandatoryRelated(deleteMe, alreadyDeleting);
            }
        }
    }

    private void materializeRelationshipsInvolving(
            final EntityInstance parent,
            final EntityInstance child,
            final String relationshipName) {
        for (RelationshipVectorDefinition vector : relationshipVectors()) {
            if (!vector.getRelationshipDefinition().isKnownAs(relationshipName)) {
                continue;
            }
            if (!relationshipRowExists(parent, child, vector)) {
                continue;
            }
            EntityInstance from = parent.getEntity() == vector.getFrom() ? parent : child;
            EntityInstance to = from == parent ? child : parent;
            if (!from.getRelationships().getConnectedItems(vector.getName()).contains(to)) {
                from.getRelationships().connect(vector.getName(), to);
            }
        }
    }

    private void materializeRelationshipsFor(final EntityInstance instance) {
        for (RelationshipVectorDefinition vector : relationshipVectors()) {
            String sql = "SELECT from_internal_id, to_internal_id FROM " +
                    relationshipTable(vector) +
                    " WHERE from_internal_id = ? OR to_internal_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, instance.getInternalId());
                statement.setString(2, instance.getInternalId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        EntityInstance from = findInstanceByInternalId(
                                vector.getFrom(),
                                resultSet.getString("from_internal_id"));
                        EntityInstance to = findInstanceByInternalId(
                                vector.getTo(),
                                resultSet.getString("to_internal_id"));
                        if (from != null && to != null &&
                                !from.getRelationships().getConnectedItems(vector.getName()).contains(to)) {
                            from.getRelationships().connect(vector.getName(), to);
                        }
                    }
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Could not materialize SQLite relationship rows", e);
            }
        }
    }

    private boolean relationshipRowExists(
            final EntityInstance parent,
            final EntityInstance child,
            final RelationshipVectorDefinition vector) {
        String sql = "SELECT 1 FROM " + relationshipTable(vector) +
                " WHERE (from_internal_id = ? AND to_internal_id = ?)" +
                " OR (from_internal_id = ? AND to_internal_id = ?)" +
                " LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, parent.getInternalId());
            statement.setString(2, child.getInternalId());
            statement.setString(3, child.getInternalId());
            statement.setString(4, parent.getInternalId());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not query SQLite relationship row", e);
        }
    }

    private EntityInstance findInstanceByInternalId(
            final EntityDefinition entity, final String internalId) {
        if (internalId == null) {
            return null;
        }
        EntityInstance existing = materializedInstances.get(internalId);
        if (existing != null) {
            return existing;
        }

        String sql = "SELECT * FROM " + table(entity) +
                " WHERE " + identifier(INTERNAL_ID_COLUMN) + " = ? LIMIT 1";
        List<EntityInstance> instances = queryEntityRows(entity, sql, List.of(internalId));
        return instances.isEmpty() ? null : instances.get(0);
    }

    private Set<String> internalIdsFor(final EntityDefinition entity) {
        Set<String> ids = new HashSet<>();
        String sql = "SELECT " + identifier(INTERNAL_ID_COLUMN) + " FROM " + table(entity);
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                ids.add(resultSet.getString(INTERNAL_ID_COLUMN));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not query SQLite ids for " + entity.getName(), e);
        }
        return ids;
    }

    private void openConnection() {
        if (closed) {
            throw new IllegalStateException("SQLite repository has been closed");
        }
        if (connection != null) {
            return;
        }
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(jdbcUrl);
            configureConnection();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "SQLite repository requires org.xerial:sqlite-jdbc on the runtime classpath", e);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not open SQLite repository " + jdbcUrl, e);
        }
    }

    private void configureConnection() {
        registerRegexpFunction();
        executeSql("PRAGMA case_sensitive_like = ON");
    }

    private void registerRegexpFunction() {
        try {
            Function.create(
                    connection,
                    "regexp",
                    new JavaRegexpFunction(),
                    2,
                    Function.FLAG_DETERMINISTIC);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not register SQLite REGEXP function", e);
        }
    }

    private void ensureSchemaReady() {
        if (schema != null) {
            refreshSchema(schema);
        }
    }

    private void createMetadataTables() {
        executeSql(
                "CREATE TABLE IF NOT EXISTS " + identifier(COUNTERS_TABLE) + " (" +
                        "entity_name TEXT NOT NULL, " +
                        "field_name TEXT NOT NULL, " +
                        "next_value INTEGER NOT NULL, " +
                        "PRIMARY KEY(entity_name, field_name))");
    }

    private void createEntityTable(final EntityDefinition entity) {
        executeSql(
                "CREATE TABLE IF NOT EXISTS " + table(entity) + " (" +
                        identifier(INTERNAL_ID_COLUMN) + " TEXT PRIMARY KEY)");

        Set<String> existingColumns = columnNames(entityTableName(entity));
        for (String fieldName : entity.getFieldNames()) {
            if (!existingColumns.contains(fieldName)) {
                Field field = entity.getField(fieldName);
                executeSql(
                        "ALTER TABLE " + table(entity) + " ADD COLUMN " +
                                identifier(fieldName) + " " + sqlType(field.getType()));
            }
        }

        if (entity.hasPrimaryKeyField()) {
            createUniqueIndex(entity, entity.getPrimaryKeyField());
        }
        for (String fieldName : entity.getFieldNames()) {
            Field field = entity.getField(fieldName);
            if (field.mustBeUnique()) {
                createUniqueIndex(entity, field);
            }
        }
    }

    private void createUniqueIndex(final EntityDefinition entity, final Field field) {
        String indexName = "__thingifier_idx_" +
                Integer.toHexString((entity.getName() + "_" + field.getName()).hashCode());
        executeSql(
                "CREATE UNIQUE INDEX IF NOT EXISTS " + identifier(indexName) +
                        " ON " + table(entity) + " (" + identifier(field.getName()) + ")");
    }

    private void createRelationshipTable(final RelationshipVectorDefinition vector) {
        executeSql(
                "CREATE TABLE IF NOT EXISTS " + relationshipTable(vector) + " (" +
                        "from_internal_id TEXT NOT NULL, " +
                        "to_internal_id TEXT NOT NULL, " +
                        "PRIMARY KEY(from_internal_id, to_internal_id))");
        createRelationshipIndex(vector, "from_internal_id");
        createRelationshipIndex(vector, "to_internal_id");
    }

    private void createRelationshipIndex(
            final RelationshipVectorDefinition vector, final String columnName) {
        String indexName = "__thingifier_rel_idx_" +
                Integer.toHexString((relationshipTableName(vector) + "_" + columnName).hashCode());
        executeSql(
                "CREATE INDEX IF NOT EXISTS " + identifier(indexName) +
                        " ON " + relationshipTable(vector) +
                        " (" + identifier(columnName) + ")");
    }

    private void restoreCountersFor(final EntityDefinition entity) {
        String sql = "SELECT field_name, next_value FROM " + identifier(COUNTERS_TABLE) +
                " WHERE entity_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getName());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    AutoIncrement counter = countersFor(entity).get(resultSet.getString("field_name"));
                    if (counter != null) {
                        int nextValue = resultSet.getInt("next_value");
                        if (counter.peekNextValue() < nextValue) {
                            counter.incrementToNextAbove(nextValue - 1);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not restore SQLite counters for " + entity.getName(), e);
        }
    }

    private SqlQuery selectInstancesSql(
            final EntityDefinition entity, final QueryFilterParams queryParams) {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + table(entity));
        List<String> whereClauses = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        for (FilterBy filterBy : queryParams.toList()) {
            if (EntityListSortParamParser.isSortByParam(filterBy.fieldName)) {
                continue;
            }
            if (!entity.hasFieldNameDefined(filterBy.fieldName)) {
                continue;
            }

            String column = identifier(filterBy.fieldName);
            switch (filterBy.filterOperation) {
                case "=":
                    whereClauses.add(column + " = ?");
                    parameters.add(filterBy.fieldValue);
                    break;
                case "!":
                case "!=":
                    whereClauses.add(column + " <> ?");
                    parameters.add(filterBy.fieldValue);
                    break;
                case "<":
                case ">":
                case "<=":
                case ">=":
                    whereClauses.add(column + " " + filterBy.filterOperation + " ?");
                    parameters.add(filterBy.fieldValue);
                    break;
                case "*=":
                    whereClauses.add(column + " LIKE ? ESCAPE '\\'");
                    parameters.add(sqlLikeWildcard(filterBy.fieldValue));
                    break;
                case "~=":
                    whereClauses.add(regexSqlCondition(column, filterBy.fieldValue, parameters));
                    break;
                default:
                    break;
            }
        }

        if (!whereClauses.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", whereClauses));
        }

        appendOrderBy(sql, entity, queryParams, "");

        return new SqlQuery(entity, sql.toString(), parameters);
    }

    private SqlQuery selectRelatedInstancesSql(
            final EntityInstance instance,
            final RelationshipVectorDefinition vector,
            final QueryFilterParams queryParams) {
        EntityDefinition connectedEntity;
        String targetJoinColumn;
        String sourceWhereColumn;

        if (vector.getFrom() == instance.getEntity()) {
            connectedEntity = vector.getTo();
            targetJoinColumn = "to_internal_id";
            sourceWhereColumn = "from_internal_id";
        } else if (vector.getTo() == instance.getEntity()) {
            connectedEntity = vector.getFrom();
            targetJoinColumn = "from_internal_id";
            sourceWhereColumn = "to_internal_id";
        } else {
            return null;
        }

        StringBuilder sql = new StringBuilder(
                "SELECT target.* FROM " + relationshipTable(vector) + " rel " +
                        "JOIN " + table(connectedEntity) + " target " +
                        "ON target." + identifier(INTERNAL_ID_COLUMN) +
                        " = rel." + targetJoinColumn +
                        " WHERE rel." + sourceWhereColumn + " = ?");
        List<Object> parameters = new ArrayList<>();
        parameters.add(instance.getInternalId());

        for (FilterBy filterBy : queryParams.toList()) {
            if (EntityListSortParamParser.isSortByParam(filterBy.fieldName)) {
                continue;
            }
            if (!connectedEntity.hasFieldNameDefined(filterBy.fieldName)) {
                continue;
            }

            String column = "target." + identifier(filterBy.fieldName);
            switch (filterBy.filterOperation) {
                case "=":
                    sql.append(" AND ").append(column).append(" = ?");
                    parameters.add(filterBy.fieldValue);
                    break;
                case "!":
                case "!=":
                    sql.append(" AND ").append(column).append(" <> ?");
                    parameters.add(filterBy.fieldValue);
                    break;
                case "<":
                case ">":
                case "<=":
                case ">=":
                    sql.append(" AND ").append(column).append(" ")
                            .append(filterBy.filterOperation).append(" ?");
                    parameters.add(filterBy.fieldValue);
                    break;
                case "*=":
                    sql.append(" AND ").append(column).append(" LIKE ? ESCAPE '\\'");
                    parameters.add(sqlLikeWildcard(filterBy.fieldValue));
                    break;
                case "~=":
                    sql.append(" AND ").
                            append(regexSqlCondition(column, filterBy.fieldValue, parameters));
                    break;
                default:
                    break;
            }
        }

        appendOrderBy(sql, connectedEntity, queryParams, "target.");

        return new SqlQuery(connectedEntity, sql.toString(), parameters);
    }

    private void appendOrderBy(
            final StringBuilder sql,
            final EntityDefinition entity,
            final QueryFilterParams queryParams,
            final String columnPrefix) {
        List<String> orderClauses = new ArrayList<>();
        for (SortByFieldName sortBy : new EntityListSortParamParser(queryParams).sortBys()) {
            if (!entity.hasFieldNameDefined(sortBy.getFieldName())) {
                continue;
            }
            orderClauses.add(columnPrefix + identifier(sortBy.getFieldName()) +
                    (sortBy.getOrder() < 0 ? " ASC" : " DESC"));
        }
        if (!orderClauses.isEmpty()) {
            sql.append(" ORDER BY ").append(String.join(", ", orderClauses));
        }
    }

    private String regexSqlCondition(
            final String column,
            final String regex,
            final List<Object> parameters) {
        try {
            SqliteRegexToLikeConverter.Conversion conversion =
                    SqliteRegexToLikeConverter.convert(regex);
            parameters.add(conversion.sqlValue());
            if (conversion.isEquality()) {
                return column + " = ?";
            }
            return column + " LIKE ? ESCAPE '\\'";
        } catch (SqliteRegexToLikeConverter.RegexToLikeConversionException e) {
            SqliteRegexFilterPolicy.compileSupported(regex);
            LOGGER.warning("SQLite regex filter could not be converted to LIKE; using REGEXP: " +
                    e.getMessage());
            parameters.add(regex);
            return column + " REGEXP ?";
        }
    }

    private List<EntityInstance> queryEntityRows(
            final EntityDefinition entity, final String sql, final List<Object> parameters) {
        List<EntityInstance> instances = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < parameters.size(); index++) {
                statement.setObject(index + 1, parameters.get(index));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    instances.add(instanceFromRow(entity, resultSet));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not query SQLite entity rows for " + entity.getName(), e);
        }
        return instances;
    }

    private void appendEntityRowsAsJson(
            final StringBuilder json,
            final EntityDefinition entity) {
        String sql = "SELECT * FROM " + table(entity);
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            String instanceSeparator = "";
            while (resultSet.next()) {
                json.append(instanceSeparator);
                appendEntityRowAsJson(json, entity, resultSet);
                instanceSeparator = ", ";
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not export SQLite entity rows for " + entity.getName(), e);
        }
    }

    private void appendEntityRowAsJson(
            final StringBuilder json,
            final EntityDefinition entity,
            final ResultSet resultSet) throws SQLException {
        json.append("{");

        String fieldSeparator = "";
        for (String fieldName : entity.getFieldNames()) {
            Field field = entity.getField(fieldName);
            String fieldJsonValue =
                    RepositoryJsonExporter.fieldJsonValue(
                            field, resultSet.getString(fieldName));
            if (fieldJsonValue == null) {
                continue;
            }

            json.append(fieldSeparator).
                    append(RepositoryJsonExporter.quoted(field.getName())).
                    append(": ").
                    append(fieldJsonValue);
            fieldSeparator = ", ";
        }

        json.append("}");
    }

    private EntityInstance instanceFromRow(
            final EntityDefinition entity, final ResultSet resultSet) throws SQLException {
        String internalId = resultSet.getString(INTERNAL_ID_COLUMN);
        EntityInstance existing = materializedInstances.get(internalId);
        if (existing != null) {
            hydrateInstanceFromRow(existing, resultSet);
            return existing;
        }

        EntityInstance instance = new EntityInstance(
                entity, UUID.fromString(internalId));
        hydrateInstanceFromRow(instance, resultSet);
        materializedInstances.put(internalId, instance);
        return instance;
    }

    private void hydrateInstanceFromRow(
            final EntityInstance instance,
            final ResultSet resultSet) throws SQLException {
        EntityDefinition entity = instance.getEntity();
        for (String fieldName : entity.getFieldNames()) {
            String value = resultSet.getString(fieldName);
            if (value != null) {
                instance.overrideValue(fieldName, value);
            }
        }
    }

    private String sqlLikeWildcard(final String wildcard) {
        return wildcard.
                replace("\\", "\\\\").
                replace("%", "\\%").
                replace("_", "\\_").
                replace("*", "%").
                replace("?", "_");
    }

    private List<RelationshipVectorDefinition> relationshipVectorsFor(
            final EntityInstance instance, final String relationshipName) {
        List<RelationshipVectorDefinition> vectors = new ArrayList<>();
        Set<String> seenTables = new HashSet<>();

        for (RelationshipVectorDefinition vector :
                instance.getEntity().related().getRelationships(relationshipName)) {
            addRelationshipVectorIfNew(vectors, seenTables, vector);
            if (vector.getRelationshipDefinition().isTwoWay()) {
                RelationshipVectorDefinition otherVector =
                        vector.getRelationshipDefinition().otherVectorOf(vector);
                if (otherVector != null) {
                    addRelationshipVectorIfNew(vectors, seenTables, otherVector);
                }
            }
        }

        return vectors;
    }

    private void addRelationshipVectorIfNew(
            final List<RelationshipVectorDefinition> vectors,
            final Set<String> seenTables,
            final RelationshipVectorDefinition vector) {
        String tableName = relationshipTableName(vector);
        if (seenTables.add(tableName)) {
            vectors.add(vector);
        }
    }

    private void persistInstance(final EntityInstance instance) {
        EntityDefinition entity = instance.getEntity();
        List<String> columns = new ArrayList<>();
        columns.add(INTERNAL_ID_COLUMN);
        columns.addAll(entity.getFieldNames());

        StringBuilder names = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        String separator = "";
        for (String column : columns) {
            names.append(separator).append(identifier(column));
            placeholders.append(separator).append("?");
            separator = ", ";
        }

        String sql = "INSERT OR REPLACE INTO " + table(entity) +
                " (" + names + ") VALUES (" + placeholders + ")";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, instance.getInternalId());
            int parameterIndex = 2;
            for (String fieldName : entity.getFieldNames()) {
                Field field = entity.getField(fieldName);
                FieldValue value = instance.getFieldValue(fieldName);
                if (value == null) {
                    statement.setString(parameterIndex, null);
                } else {
                    statement.setString(parameterIndex, field.getActualValueToAdd(value));
                }
                parameterIndex++;
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not persist SQLite instance", e);
        }
    }

    private void persistRelationship(
            final EntityInstance from, final String relationshipName, final EntityInstance to) {
        RelationshipVectorDefinition vector =
                from.getEntity().getNamedRelationshipTo(relationshipName, to.getEntity());
        if (vector == null) {
            throw new IllegalArgumentException(
                    "Unknown relationship " + relationshipName + " between " +
                            from.getEntity().getName() + " and " + to.getEntity().getName());
        }

        String sql = "INSERT OR IGNORE INTO " + relationshipTable(vector) +
                " (from_internal_id, to_internal_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, from.getInternalId());
            statement.setString(2, to.getInternalId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not persist SQLite relationship", e);
        }
    }

    private void persistCountersFor(final EntityDefinition entity) {
        executePreparedSql(
                "DELETE FROM " + identifier(COUNTERS_TABLE) +
                        " WHERE entity_name = ?",
                List.of(entity.getName()));

        String sql = "INSERT INTO " + identifier(COUNTERS_TABLE) +
                " (entity_name, field_name, next_value) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (AutoIncrement counter : countersFor(entity).values()) {
                statement.setString(1, entity.getName());
                statement.setString(2, counter.getName());
                statement.setInt(3, counter.peekNextValue());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not persist SQLite counters for " + entity.getName(), e);
        }
    }

    private void deletePersistedInstance(final String internalId) {
        for (EntityDefinition entity : schema.getEntityDefinitions()) {
            executePreparedSql(
                    "DELETE FROM " + table(entity) +
                            " WHERE " + identifier(INTERNAL_ID_COLUMN) + " = ?",
                    List.of(internalId));
        }
        for (RelationshipVectorDefinition vector : relationshipVectors()) {
            executePreparedSql(
                    "DELETE FROM " + relationshipTable(vector) +
                            " WHERE from_internal_id = ? OR to_internal_id = ?",
                    List.of(internalId, internalId));
        }
    }

    private void deleteRelationshipRowsInvolving(
            final EntityInstance parent,
            final EntityInstance child,
            final String relationshipName) {
        for (RelationshipVectorDefinition vector : relationshipVectors()) {
            if (!vector.getRelationshipDefinition().isKnownAs(relationshipName)) {
                continue;
            }
            executePreparedSql(
                    "DELETE FROM " + relationshipTable(vector) +
                            " WHERE (from_internal_id = ? AND to_internal_id = ?)" +
                            " OR (from_internal_id = ? AND to_internal_id = ?)",
                    List.of(
                            parent.getInternalId(),
                            child.getInternalId(),
                            child.getInternalId(),
                            parent.getInternalId()));
        }
    }

    private void deleteRelationshipRowsInvolving(final EntityInstance instance) {
        for (RelationshipVectorDefinition vector : relationshipVectors()) {
            executePreparedSql(
                    "DELETE FROM " + relationshipTable(vector) +
                            " WHERE from_internal_id = ? OR to_internal_id = ?",
                    List.of(instance.getInternalId(), instance.getInternalId()));
        }
    }

    private void deleteRelationshipRowsInvolving(final Set<String> internalIds) {
        for (String internalId : internalIds) {
            for (RelationshipVectorDefinition vector : relationshipVectors()) {
                executePreparedSql(
                        "DELETE FROM " + relationshipTable(vector) +
                                " WHERE from_internal_id = ? OR to_internal_id = ?",
                        List.of(internalId, internalId));
            }
        }
    }

    private void clearRelationshipRows() {
        for (RelationshipVectorDefinition vector : relationshipVectors()) {
            executeSql("DELETE FROM " + relationshipTable(vector));
        }
    }

    private List<RelationshipVectorDefinition> relationshipVectors() {
        List<RelationshipVectorDefinition> vectors = new ArrayList<>();
        if (schema == null) {
            return vectors;
        }
        for (RelationshipDefinition relationship : schema.getRelationships()) {
            vectors.add(relationship.getFromRelationship());
            if (relationship.isTwoWay()) {
                vectors.add(relationship.getReversedRelationship());
            }
        }
        return vectors;
    }

    private Set<String> columnNames(final String tableName) {
        Set<String> names = new HashSet<>();
        String sql = "PRAGMA table_info(" + identifier(tableName) + ")";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                names.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read SQLite table info for " + tableName, e);
        }
        return names;
    }

    private void executeSql(final String sql) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not execute SQLite SQL: " + sql, e);
        }
    }

    private void executePreparedSql(final String sql, final List<Object> parameters) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < parameters.size(); index++) {
                statement.setObject(index + 1, parameters.get(index));
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not execute SQLite SQL: " + sql, e);
        }
    }

    private void runInTransaction(final Runnable operation) {
        try {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                operation.run();
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new IllegalStateException("Could not run SQLite transaction", e);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not run SQLite transaction", e);
        }
    }

    private String sqlType(final FieldType type) {
        switch (type) {
            case AUTO_INCREMENT:
            case INTEGER:
                return "INTEGER";
            case FLOAT:
                return "REAL";
            case BOOLEAN:
            case AUTO_GUID:
            case DATE:
            case ENUM:
            case OBJECT:
            case STRING:
            default:
                return "TEXT";
        }
    }

    private String table(final EntityDefinition entity) {
        return identifier(entityTableName(entity));
    }

    private String entityTableName(final EntityDefinition entity) {
        return "thing_" + entity.getName();
    }

    private String relationshipTable(final RelationshipVectorDefinition vector) {
        return identifier(relationshipTableName(vector));
    }

    private String relationshipTableName(final RelationshipVectorDefinition vector) {
        return "thing_rel_" +
                vector.getFrom().getName() + "_" +
                vector.getName() + "_" +
                vector.getTo().getName();
    }

    private String identifier(final String rawIdentifier) {
        return "\"" + rawIdentifier.replace("\"", "\"\"") + "\"";
    }

    private static class JavaRegexpFunction extends Function {
        private String cachedRegex;
        private Pattern cachedPattern;

        @Override
        protected void xFunc() throws SQLException {
            if (args() != 2) {
                result(0);
                return;
            }

            String regex = value_text(0);
            String value = value_text(1);
            if (regex == null || value == null) {
                result(0);
                return;
            }

            Pattern pattern = patternFor(regex);
            result(pattern.matcher(value).matches() ? 1 : 0);
        }

        private Pattern patternFor(final String regex) throws SQLException {
            if (!regex.equals(cachedRegex)) {
                try {
                    cachedPattern = SqliteRegexFilterPolicy.compileSupported(regex);
                    cachedRegex = regex;
                } catch (SqliteRegexFilterPolicy.UnsupportedRegexFilterException e) {
                    throw new SQLException("Unsupported SQLite regex filter: " + e.getMessage(), e);
                }
            }
            return cachedPattern;
        }
    }

    private static class SqlQuery {
        private final EntityDefinition entity;
        private final String sql;
        private final List<Object> parameters;

        private SqlQuery(
                final EntityDefinition entity,
                final String sql,
                final List<Object> parameters) {
            this.entity = entity;
            this.sql = sql;
            this.parameters = parameters;
        }
    }
}
