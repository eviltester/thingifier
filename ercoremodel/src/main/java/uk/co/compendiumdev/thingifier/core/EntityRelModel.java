package uk.co.compendiumdev.thingifier.core;

import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.repository.InMemoryThingRepositoryProvider;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepositoryProvider;

import java.util.*;

/*
    The ERM has the 'model' (ERSchema) and the 'instances' (things).
    Schema and instances are separate to allow us to have multiple
    'databases' in memory at the same time built from the same schema.
 */
public class EntityRelModel implements AutoCloseable {

    public static final String DEFAULT_DATABASE_NAME = "__default";

    // a provider so that key, database can be backed by memory, files, SQLite, etc.
    private final ERSchema schema; // all the definitions
    private final ThingRepositoryProvider repositories;
    private RepositoryDataPopulator dataPopulator;

    public EntityRelModel(){
        schema = new ERSchema();
        repositories = new InMemoryThingRepositoryProvider();
        dataPopulator = null;
    }

    public EntityRelModel(final ThingRepositoryProvider repositories) {
        this.schema = new ERSchema();
        this.repositories = repositories;
        this.repositories.getDefaultRepository().initializeFrom(schema);
        dataPopulator = null;
    }

    public EntityRelModel(final ERSchema schema, final ThingRepositoryProvider repositories) {
        this.schema = schema;
        this.repositories = repositories;
        this.repositories.getDefaultRepository().initializeFrom(schema);
        dataPopulator = null;
    }

    public EntityDefinition createEntityDefinition(final String entityName, final String pluralName) {
        return createEntityDefinition(entityName, pluralName, -1);
    }

    public EntityDefinition createEntityDefinition(final String entityName, final String pluralName, int maximumNumberOfInstances) {
        EntityDefinition defn = schema.defineEntity(entityName, pluralName, maximumNumberOfInstances);
        refreshRepositorySchemas();
        return defn;
    }

    public ERSchema getSchema(){
        return schema;
    }

    public String exportInstanceDataAsJson(final String databaseKey) {
        ThingRepository repository = repositories.getRepository(databaseKey);
        if (repository == null) {
            return "{}";
        }
        return repository.exportDataAsJson(schema);
    }

    public String reportAsMarkdown(final String databaseKey) {
        ThingRepository repository = repositories.getRepository(databaseKey);
        if (repository == null) {
            return "";
        }
        return repository.reportAsMarkdown(schema);
    }

    public Set<String> getDatabaseNames(){
        return repositories.getRepositoryNames();
    }

    public ThingRepository getRepository(String databaseKey) {
        return repositories.getRepository(databaseKey);
    }

    public ThingRepositoryProvider getRepositoryProvider() {
        return repositories;
    }

    @Override
    public void close() {
        repositories.close();
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

    public EntityDefinition getEntityDefinitionWithPluralNamed(final String pluralName){
        return schema.getEntityDefinitionWithPluralNamed(pluralName);
    }

    public EntityDefinition getEntityDefinitionNamed(final String term){
        return schema.getEntityDefinitionNamed(term);
    }


    // Multiple Databases
    public void createInstanceDatabase(String databaseKey) {

        if(repositories.getRepository(databaseKey)!=null){
            throw new IllegalStateException("ERM Database Already Exists with name " + databaseKey);
        }

        createInstanceDatabaseIfNotExisting(databaseKey);
    }


    public void deleteInstanceDatabase(String databaseKey) {
        if(databaseKey.equals(DEFAULT_DATABASE_NAME)){
            throw new IllegalStateException("Cannot delete default database");
        }
        repositories.deleteRepository(databaseKey);
    }

    public boolean createInstanceDatabaseIfNotExisting(String databaseKey) {
        return repositories.createRepositoryIfNotExisting(databaseKey, this.schema);
    }

    public boolean populateDatabase(String databaseKey){
        ThingRepository repository = repositories.getRepository(databaseKey);
        if(repository==null){
            return false;
        }

        if(dataPopulator==null){
            return false;
        }

        repository.refreshSchema(getSchema());
        populateRepository(repository);

        return true;
    }

    public void setDataGenerator(RepositoryDataPopulator dataPopulator) {
        this.dataPopulator = dataPopulator;
    }

    private void refreshRepositorySchemas() {
        for(String databaseKey : repositories.getRepositoryNames()){
            repositories.getRepository(databaseKey).refreshSchema(schema);
        }
    }

    private void populateRepository(final ThingRepository repository) {
        dataPopulator.populate(getSchema(), repository);
    }
}
