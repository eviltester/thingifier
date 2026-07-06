package uk.co.compendiumdev.thingifier.core;

import uk.co.compendiumdev.thingifier.core.domain.datapopulator.DataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.InMemoryThingRepositoryProvider;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepositoryProvider;

import java.util.*;

/*
    The ERM has the 'model' (ERSchema) and the 'instances' (things).
    Schema and instances are separate to allow us to have multiple
    'databases' in memory at the same time built from the same schema.
 */
public class EntityRelModel {

    public static final String DEFAULT_DATABASE_NAME = "__default";

    // a provider so that key, database can be backed by memory, files, SQLite, etc.
    private final ERSchema schema; // all the definitions
    private final ThingRepositoryProvider repositories;
    private DataPopulator dataPopulator;

    public EntityRelModel(){
        schema = new ERSchema();
        repositories = new InMemoryThingRepositoryProvider();
        dataPopulator = null;
    }

    public EntityRelModel(final ERSchema schema, final ERInstanceData erInstanceData) {
        this.schema = schema;
        this.repositories = new InMemoryThingRepositoryProvider(erInstanceData);
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
        for(String databaseKey : repositories.getRepositoryNames()){
            repositories.getRepository(databaseKey).createInstanceCollectionFor(defn);
        }
        return defn;
    }

    public ERSchema getSchema(){
        return schema;
    }

    // TODO: use of this is basically deprecated since is refers to the default database
    @Deprecated() // we should use the parameterised version
    public ERInstanceData getInstanceData(){
        return getInstanceData(DEFAULT_DATABASE_NAME);
    }

    public ERInstanceData getInstanceDataAsJson(){
        return getInstanceData(DEFAULT_DATABASE_NAME);
    }

    public ERInstanceData getInstanceData(String databaseKey) {
        ThingRepository repository = repositories.getRepository(databaseKey);
        if(repository==null){
            return null;
        }
        return repository.getInstanceData();
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

    // ERM Object Level
    public EntityRelModel cloneWithDifferentData(final List<EntityInstance> instances) {
        return new EntityRelModel(schema, new ERInstanceData(instances));
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
        if(repositories.getRepository(databaseKey)==null){
            return false;
        }

        if(dataPopulator==null){
            return false;
        }

        repositories.getRepository(databaseKey).refreshSchema(getSchema());

        dataPopulator.populate(
                getSchema(),
                repositories.getRepository(databaseKey).getInstanceData()
        );
        repositories.getRepository(databaseKey).flush();

        return true;
    }

    public void setDataGenerator(DataPopulator dataPopulator) {
        this.dataPopulator = dataPopulator;
    }

    private void refreshRepositorySchemas() {
        for(String databaseKey : repositories.getRepositoryNames()){
            repositories.getRepository(databaseKey).refreshSchema(schema);
        }
    }
}
