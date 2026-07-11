package uk.co.compendiumdev.thingifier.core;

import java.util.*;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.reporting.ERModelReport;
import uk.co.compendiumdev.thingifier.core.reporting.RepositoryJsonExporter;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreProvider;
import uk.co.compendiumdev.thingifier.core.repository.inmemory.InMemoryThingStoreProvider;

/*
   The ERM has the 'model' (ERSchema) and the 'instances' (things).
   Schema and instances are separate to allow us to have multiple
   'databases' in memory at the same time built from the same schema.
*/
public class EntityRelModel implements AutoCloseable {

    public static final String DEFAULT_DATABASE_NAME = "__default";

    // a provider so that key, database can be backed by memory, files, SQLite, etc.
    private final ERSchema schema; // all the definitions
    private final ThingStoreProvider stores;
    private RepositoryDataPopulator dataPopulator;

    public EntityRelModel() {
        schema = new ERSchema();
        stores = new InMemoryThingStoreProvider();
        dataPopulator = null;
    }

    public EntityRelModel(final ThingStoreProvider stores) {
        this.schema = new ERSchema();
        this.stores = stores;
        this.stores.getDefaultStore().administration().initializeFrom(schema);
        dataPopulator = null;
    }

    public EntityRelModel(final ERSchema schema, final ThingStoreProvider stores) {
        this.schema = schema;
        this.stores = stores;
        this.stores.getDefaultStore().administration().initializeFrom(schema);
        dataPopulator = null;
    }

    public EntityDefinition createEntityDefinition(
            final String entityName, final String pluralName) {
        return createEntityDefinition(entityName, pluralName, -1);
    }

    public EntityDefinition createEntityDefinition(
            final String entityName, final String pluralName, int maximumNumberOfInstances) {
        EntityDefinition defn =
                schema.defineEntity(entityName, pluralName, maximumNumberOfInstances);
        refreshRepositorySchemas();
        return defn;
    }

    public ERSchema getSchema() {
        return schema;
    }

    public String exportInstanceDataAsJson(final String databaseKey) {
        ThingStore store = stores.getStore(databaseKey);
        if (store == null) {
            return "{}";
        }
        return new RepositoryJsonExporter(schema, store.entityQueries()).asJson();
    }

    public String reportAsMarkdown(final String databaseKey) {
        ThingStore store = stores.getStore(databaseKey);
        if (store == null) {
            return "";
        }
        return new ERModelReport(schema, store.entityQueries()).asMarkdown();
    }

    public Set<String> getDatabaseNames() {
        return stores.getStoreNames();
    }

    public ThingStore getStore(String databaseKey) {
        return stores.getStore(databaseKey);
    }

    public ThingStoreProvider getStoreProvider() {
        return stores;
    }

    @Override
    public void close() {
        stores.close();
    }

    // Schema methods
    // TODO: consider inlining all of these
    public boolean hasEntityNamed(final String aName) {
        return schema.hasEntityNamed(aName);
    }

    public List<String> getEntityNames() {
        return schema.getEntityNames();
    }

    public Collection<RelationshipDefinition> getRelationshipDefinitions() {
        return schema.getRelationships();
    }

    public Collection<EntityDefinition> getEntityDefinitions() {
        return schema.getEntityDefinitions();
    }

    public RelationshipDefinition createRelationshipDefinition(
            EntityDefinition from, EntityDefinition to, final String named, final Cardinality of) {
        RelationshipDefinition relationship = schema.defineRelationship(from, to, named, of);
        refreshRepositorySchemas();
        return relationship;
    }

    public boolean hasRelationshipNamed(final String relationshipName) {
        return schema.hasRelationshipNamed(relationshipName);
    }

    public boolean hasEntityWithPluralNamed(final String term) {
        return schema.hasEntityWithPluralNamed(term);
    }

    public EntityDefinition getEntityDefinitionWithPluralNamed(final String pluralName) {
        return schema.getEntityDefinitionWithPluralNamed(pluralName);
    }

    public EntityDefinition getEntityDefinitionNamed(final String term) {
        return schema.getEntityDefinitionNamed(term);
    }

    // Multiple Databases
    public void createInstanceDatabase(String databaseKey) {

        if (stores.getStore(databaseKey) != null) {
            throw new IllegalStateException("ERM Database Already Exists with name " + databaseKey);
        }

        createInstanceDatabaseIfNotExisting(databaseKey);
    }

    public void deleteInstanceDatabase(String databaseKey) {
        if (databaseKey.equals(DEFAULT_DATABASE_NAME)) {
            throw new IllegalStateException("Cannot delete default database");
        }
        stores.deleteStore(databaseKey);
    }

    public boolean createInstanceDatabaseIfNotExisting(String databaseKey) {
        return stores.createStoreIfNotExisting(databaseKey, this.schema);
    }

    public boolean populateDatabase(String databaseKey) {
        ThingStore store = stores.getStore(databaseKey);
        if (store == null) {
            return false;
        }

        if (dataPopulator == null) {
            return false;
        }

        store.administration().refreshSchema(getSchema());
        populateStore(store);

        return true;
    }

    public void setDataGenerator(RepositoryDataPopulator dataPopulator) {
        this.dataPopulator = dataPopulator;
    }

    private void refreshRepositorySchemas() {
        for (String databaseKey : stores.getStoreNames()) {
            stores.getStore(databaseKey).administration().refreshSchema(schema);
        }
    }

    private void populateStore(final ThingStore store) {
        dataPopulator.populate(getSchema(), store);
    }
}
