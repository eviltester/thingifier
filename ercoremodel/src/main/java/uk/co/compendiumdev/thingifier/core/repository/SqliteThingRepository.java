package uk.co.compendiumdev.thingifier.core.repository;

import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.AutoIncrement;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.core.domain.instances.RelationshipVectorInstance;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SqliteThingRepository extends InMemoryThingRepository {

    private static final String INTERNAL_ID_COLUMN = "__internal_id";
    private static final String COUNTERS_TABLE = "__thingifier_counters";

    private final String jdbcUrl;
    private ERSchema schema;
    private Connection connection;
    private boolean loadedExistingData;

    public SqliteThingRepository(final String databaseKey, final String jdbcUrl) {
        super(databaseKey, new ERInstanceData());
        this.jdbcUrl = jdbcUrl;
    }

    public static SqliteThingRepository inMemory(final String databaseKey) {
        return new SqliteThingRepository(databaseKey, "jdbc:sqlite::memory:");
    }

    public static SqliteThingRepository fileBacked(final String databaseKey, final Path databasePath) {
        String path = databasePath.toAbsolutePath().toString().replace("\\", "/");
        return new SqliteThingRepository(databaseKey, "jdbc:sqlite:" + path);
    }

    @Override
    public void initializeFrom(final ERSchema schema) {
        this.schema = schema;
        openConnection();
        createMetadataTables();
        refreshSchema(schema);
        if (!loadedExistingData) {
            loadExistingData();
            loadedExistingData = true;
        }
    }

    @Override
    public void refreshSchema(final ERSchema schema) {
        this.schema = schema;
        openConnection();
        super.refreshSchema(schema);
        for (EntityDefinition entity : schema.getEntityDefinitions()) {
            createEntityTable(entity);
        }
        for (RelationshipVectorDefinition vector : relationshipVectors()) {
            createRelationshipTable(vector);
        }
    }

    @Override
    public EntityInstanceCollection createInstanceCollectionFor(final EntityDefinition definition) {
        EntityInstanceCollection collection = super.createInstanceCollectionFor(definition);
        if (connection != null) {
            createEntityTable(definition);
        }
        return collection;
    }

    @Override
    public EntityInstance addInstance(final EntityInstance instance) {
        ensureSchemaReady();
        super.addInstance(instance);
        persistInstance(instance);
        persistCountersFor(instance.getEntity());
        return instance;
    }

    @Override
    public EntityInstance updateInstance(final EntityInstance instance) {
        ensureSchemaReady();
        persistInstance(instance);
        persistCountersFor(instance.getEntity());
        return instance;
    }

    @Override
    public void deleteEntityInstance(final EntityInstance instance) {
        Set<String> idsBeforeDelete = currentInternalIds();
        super.deleteEntityInstance(instance);
        Set<String> idsAfterDelete = currentInternalIds();

        idsBeforeDelete.removeAll(idsAfterDelete);
        for (String internalId : idsBeforeDelete) {
            deletePersistedInstance(internalId);
        }
        replaceAllRelationshipRowsFromCache();
    }

    @Override
    public void clearAllData() {
        super.clearAllData();
        for (EntityDefinition entity : schema.getEntityDefinitions()) {
            executeSql("DELETE FROM " + table(entity));
            persistCountersFor(entity);
        }
        clearRelationshipRows();
    }

    @Override
    public void clearInstanceDataFor(final String entityName) {
        EntityInstanceCollection collection = getInstanceCollectionForEntityNamed(entityName);
        if (collection == null) {
            return;
        }
        super.clearInstanceDataFor(entityName);
        executeSql("DELETE FROM " + table(collection.definition()));
        persistCountersFor(collection.definition());
        replaceAllRelationshipRowsFromCache();
    }

    @Override
    public void connectRelationship(
            final EntityInstance from, final String relationshipName, final EntityInstance to) {
        super.connectRelationship(from, relationshipName, to);
        replaceAllRelationshipRowsFromCache();
    }

    @Override
    public List<EntityInstance> removeRelationshipsInvolving(
            final EntityInstance parent,
            final EntityInstance child,
            final String relationshipName) {
        List<EntityInstance> removed = super.removeRelationshipsInvolving(parent, child, relationshipName);
        replaceAllRelationshipRowsFromCache();
        return removed;
    }

    @Override
    public List<EntityInstance> removeAllRelationships(final EntityInstance instance) {
        List<EntityInstance> removed = super.removeAllRelationships(instance);
        replaceAllRelationshipRowsFromCache();
        return removed;
    }

    @Override
    public void flush() {
        refreshSchema(schema);
        for (EntityDefinition entity : schema.getEntityDefinitions()) {
            executeSql("DELETE FROM " + table(entity));
        }
        clearRelationshipRows();

        for (EntityInstanceCollection collection : getAllInstanceCollections()) {
            for (EntityInstance instance : collection.getInstances()) {
                persistInstance(instance);
            }
            persistCountersFor(collection.definition());
        }

        replaceAllRelationshipRowsFromCache();
    }

    @Override
    public void close() {
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

    private void openConnection() {
        if (connection != null) {
            return;
        }
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(jdbcUrl);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "SQLite repository requires org.xerial:sqlite-jdbc on the runtime classpath", e);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not open SQLite repository " + jdbcUrl, e);
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
    }

    private void loadExistingData() {
        for (EntityDefinition entity : schema.getEntityDefinitions()) {
            loadEntityRows(entity);
            restoreCountersFor(entity);
        }
        loadRelationshipRows();
    }

    private void loadEntityRows(final EntityDefinition entity) {
        EntityInstanceCollection collection = getInstanceCollectionForEntityNamed(entity.getName());
        String sql = "SELECT * FROM " + table(entity);
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String internalId = resultSet.getString(INTERNAL_ID_COLUMN);
                if (collection.findInstanceByInternalID(internalId) != null) {
                    continue;
                }

                EntityInstance instance = new EntityInstance(entity, UUID.fromString(internalId));
                for (String fieldName : entity.getFieldNames()) {
                    String value = resultSet.getString(fieldName);
                    if (value != null) {
                        instance.overrideValue(fieldName, value);
                    }
                }
                collection.addInstance(instance);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load SQLite entity rows for " + entity.getName(), e);
        }
    }

    private void loadRelationshipRows() {
        Map<String, EntityInstance> instances = instancesByInternalId();
        for (RelationshipVectorDefinition vector : relationshipVectors()) {
            String sql = "SELECT from_internal_id, to_internal_id FROM " + relationshipTable(vector);
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    EntityInstance from = instances.get(resultSet.getString("from_internal_id"));
                    EntityInstance to = instances.get(resultSet.getString("to_internal_id"));
                    if (from != null && to != null) {
                        from.getRelationships().connect(vector.getName(), to);
                    }
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Could not load SQLite relationship rows", e);
            }
        }
    }

    private void restoreCountersFor(final EntityDefinition entity) {
        EntityInstanceCollection collection = getInstanceCollectionForEntityNamed(entity.getName());
        String sql = "SELECT field_name, next_value FROM " + identifier(COUNTERS_TABLE) +
                " WHERE entity_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getName());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    AutoIncrement counter = collection.getCounters().get(resultSet.getString("field_name"));
                    if (counter != null) {
                        int nextValue = resultSet.getInt("next_value");
                        if (counter.getCurrentValue() < nextValue) {
                            counter.incrementToNextAbove(nextValue - 1);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not restore SQLite counters for " + entity.getName(), e);
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

    private void persistCountersFor(final EntityDefinition entity) {
        executeSql("DELETE FROM " + identifier(COUNTERS_TABLE) +
                " WHERE entity_name = " + quotedValue(entity.getName()));

        String sql = "INSERT INTO " + identifier(COUNTERS_TABLE) +
                " (entity_name, field_name, next_value) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (AutoIncrement counter : countersFor(entity).values()) {
                statement.setString(1, entity.getName());
                statement.setString(2, counter.getName());
                statement.setInt(3, counter.getCurrentValue());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not persist SQLite counters for " + entity.getName(), e);
        }
    }

    private void deletePersistedInstance(final String internalId) {
        for (EntityDefinition entity : schema.getEntityDefinitions()) {
            executeSql("DELETE FROM " + table(entity) +
                    " WHERE " + identifier(INTERNAL_ID_COLUMN) + " = " + quotedValue(internalId));
        }
        for (RelationshipVectorDefinition vector : relationshipVectors()) {
            executeSql("DELETE FROM " + relationshipTable(vector) +
                    " WHERE from_internal_id = " + quotedValue(internalId) +
                    " OR to_internal_id = " + quotedValue(internalId));
        }
    }

    private void replaceAllRelationshipRowsFromCache() {
        clearRelationshipRows();
        Set<String> persisted = new HashSet<>();

        String sql = "INSERT OR IGNORE INTO %s (from_internal_id, to_internal_id) VALUES (?, ?)";
        for (EntityInstance instance : instancesByInternalId().values()) {
            for (RelationshipVectorInstance relationship : instance.getRelationships().getRelationshipInstances()) {
                RelationshipVectorDefinition vector = relationship.getDefinition();
                String key = vector.getName() + "|" +
                        relationship.getFrom().getInternalId() + "|" +
                        relationship.getTo().getInternalId();
                if (!persisted.add(key)) {
                    continue;
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        String.format(sql, relationshipTable(vector)))) {
                    statement.setString(1, relationship.getFrom().getInternalId());
                    statement.setString(2, relationship.getTo().getInternalId());
                    statement.executeUpdate();
                } catch (SQLException e) {
                    throw new IllegalStateException("Could not persist SQLite relationship", e);
                }
            }
        }
    }

    private void clearRelationshipRows() {
        for (RelationshipVectorDefinition vector : relationshipVectors()) {
            executeSql("DELETE FROM " + relationshipTable(vector));
        }
    }

    private Set<String> currentInternalIds() {
        return instancesByInternalId().keySet();
    }

    private Map<String, EntityInstance> instancesByInternalId() {
        Map<String, EntityInstance> instances = new HashMap<>();
        for (EntityInstanceCollection collection : getAllInstanceCollections()) {
            for (EntityInstance instance : collection.getInstances()) {
                instances.put(instance.getInternalId(), instance);
            }
        }
        return instances;
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
        return identifier(
                "thing_rel_" +
                        vector.getFrom().getName() + "_" +
                        vector.getName() + "_" +
                        vector.getTo().getName());
    }

    private String identifier(final String rawIdentifier) {
        return "\"" + rawIdentifier.replace("\"", "\"\"") + "\"";
    }

    private String quotedValue(final String value) {
        return "'" + value.replace("'", "''") + "'";
    }
}
