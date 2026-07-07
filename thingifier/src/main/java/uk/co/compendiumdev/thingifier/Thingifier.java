package uk.co.compendiumdev.thingifier;

import uk.co.compendiumdev.thingifier.api.ThingifierRestAPIHandler;
import uk.co.compendiumdev.thingifier.apiconfig.ApiDocsConfig;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfile;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfiles;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonPopulator;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.LegacyDataPopulatorAdapter;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.DataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.*;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;
import uk.co.compendiumdev.thingifier.reporting.ThingReporter;

import java.util.*;

/* Thingifier
    is the main class that allows access to:
    - the ERM Schema
    - the ERM data
    - the API Definition and config
    - TODO: why is the API documentation not in here?
 */
public final class Thingifier implements AutoCloseable {

    private final EntityRelModel erm;
    private ApiDocsConfig apiDocsConfig;
    private DataPopulator dataPopulator;
    private String title;
    private String initialParagraph;
    private final ThingifierApiConfig apiConfig;
    private final ThingifierApiConfigProfiles apiConfigProfiles;

    public Thingifier(){
        this(new EntityRelModel());
    }

    public Thingifier(final EntityRelModel erm) {
        this.erm = erm;
        title = "";
        initialParagraph = "";
        apiConfig = new ThingifierApiConfig("");
        apiConfigProfiles = new ThingifierApiConfigProfiles();
        apiDocsConfig = new ApiDocsConfig();
    }

    public Thingifier(final EntityRelModel erm,
                      final ThingifierApiConfig apiConfig,
                      final ThingifierApiConfigProfiles apiConfigProfiles,
                      final String title,
                      final String initialParagraph,
                      final ApiDocsConfig apiDocsConfig
                      ) {

        this.erm = erm;
        this.title = title;
        this.initialParagraph = initialParagraph;
        this.apiConfig = apiConfig;
        this.apiConfigProfiles = apiConfigProfiles;
        this.apiDocsConfig = apiDocsConfig;
    }

    /*
        TODO: configure the REST API from the entities and relationship definitions
        at the moment a default REST API is created, consider an API model as separate
        e.g
         - apiConfig.usePluralNouns(), useSingleNouns()
         - apiConfig.allowQueryParamFilters()
         - apiConfig.disallowQueryParamFilters("/todos")
         - apiConfig.routing("/todos").disallow("PATCH,POST.UPDATE")
         - apiConfig.hideGUIDsWhenIDAvailable()
         - etc.
        aliases to entities and relationships to override definitions in the entity etc.
        create 'queries' to show subsets of data, etc.
        Do not put this into the entities and relationships make this a separate model
     */


    // Entity Definitions

    public EntityDefinition defineThing(final String thingName, final String pluralName) {
        return defineThing(thingName, pluralName, -1);
    }

    public EntityDefinition defineThing(final String thingName, final String pluralName, final int maximumNumberOfInstances) {
        return erm.createEntityDefinition(thingName, pluralName, maximumNumberOfInstances);
    }

    public boolean hasThingNamed(final String aName) {
        return erm.hasEntityNamed(aName);
    }

    public boolean hasThingWithPluralNamed(final String term) {
        return erm.hasEntityWithPluralNamed(term);
    }

    public EntityDefinition getDefinitionNamed(final String term) {
        return erm.getSchema().getEntityDefinitionNamed(term);
    }
    public EntityDefinition getDefinitionWithPluralNamed(final String term) {
        return erm.getSchema().getEntityDefinitionWithPluralNamed(term);
    }

    public List<String> getThingNames() {
        return erm.getEntityNames();
    }

    // RELATIONSHIPS
    public Collection<RelationshipDefinition> getRelationshipDefinitions() {
        return erm.getRelationshipDefinitions();
    }

    public RelationshipDefinition defineRelationship(EntityDefinition from, EntityDefinition to,
                                                     final String named, final Cardinality of) {
        return erm.createRelationshipDefinition(from,to,named, of);
    }

    public boolean hasRelationshipNamed(final String relationshipName) {
        return erm.hasRelationshipNamed(relationshipName);
    }

    // Instances

    /**
     * @deprecated Compatibility collection access. Use {@link #getRepository(String)}
     * with schema entity definitions, or {@link #listThingInstancesNamed(String, String)}.
     */
    @Deprecated(forRemoval = true, since = "1.5.6")
    public List<EntityInstanceCollection> getThings(final String database) {
        return erm.getRepository(database).getAllInstanceCollections();
    }


    public EntityInstance findThingInstanceByGuid(final String thingGUID, final String database) {
        return erm.getRepository(database).findEntityInstanceByGUID(thingGUID);
    }

    /**
     * @deprecated Compatibility collection access. Use {@link #getRepository(String)}
     * or {@link #listThingInstancesNamed(String, String)}.
     */
    @Deprecated(forRemoval = true, since = "1.5.6")
    public EntityInstanceCollection getThingInstancesNamed(final String aName, final String database) {
        return erm.getRepository(database).getInstanceCollectionForEntityNamed(aName);
    }

    public List<EntityInstance> listThingInstancesNamed(final String aName, final String database) {
        EntityDefinition definition = erm.getSchema().getDefinitionWithSingularOrPluralNamed(aName);
        ThingRepository repository = erm.getRepository(database);
        if (definition == null || repository == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(repository.listInstances(definition));
    }

    public EntityInstance findThingInstanceByFieldNameAndValue(
            final String entityName,
            final String fieldName,
            final String fieldValue,
            final String database) {
        EntityDefinition definition = erm.getSchema().getDefinitionWithSingularOrPluralNamed(entityName);
        ThingRepository repository = erm.getRepository(database);
        if (definition == null || repository == null) {
            return null;
        }
        return repository.findInstanceByFieldNameAndValue(definition, fieldName, fieldValue);
    }

    /**
     * @deprecated Compatibility collection access. Use {@link #getRepository(String)}
     * with {@link EntityDefinition} resolved from {@link #getERmodel()}.
     */
    @Deprecated(forRemoval = true, since = "1.5.6")
    public EntityInstanceCollection getInstancesForSingularOrPluralNamedEntity(final String term, final String database) {
        final EntityDefinition defn = erm.getSchema().getDefinitionWithSingularOrPluralNamed(term);
        if(defn!=null){
            final String entityName = defn.getName();
            return erm.getRepository(database).getInstanceCollectionForEntityNamed(entityName);
        }

        return null;
    }

    public void clearAllData() {
        // clear data in default database but keep database
        clearAllData(EntityRelModel.DEFAULT_DATABASE_NAME);
        // delete all the other databases
        for(String databaseName : erm.getDatabaseNames()){
            if(!databaseName.equals(EntityRelModel.DEFAULT_DATABASE_NAME)){
                erm.deleteInstanceDatabase(databaseName);
            }
        }
    }

    public void clearAllData(final String database) {
        erm.getRepository(database).clearAllData();
    }

    public void deleteThing(final EntityInstance aThingInstance, final String database) {
        erm.getRepository(database).deleteEntityInstance(aThingInstance);
    }


    // data generation
    public void generateData(final String database) {
        if(dataPopulator!=null){
            ThingRepository repository = erm.getRepository(database);
            if (repository == null) {
                return;
            }
            repository.refreshSchema(erm.getSchema());
            populateRepository(repository);
        }
    }

    public void setDataGenerator(DataPopulator dataPopulator) {
        this.dataPopulator = dataPopulator;
        erm.setDataGenerator(dataPopulator);
    }





    // Generic

    public String toString() {

        return new ThingReporter(this).basicReport();
    }

    //API

    public ThingifierRestAPIHandler api() {
        // TODO: why is this created each time?
        return new ThingifierRestAPIHandler(this);
    }


    public ThingifierApiConfig apiConfig() {
        return apiConfig;
    }

    public ThingifierApiConfigProfiles apiConfigProfiles() {
        return apiConfigProfiles;
    }

    public void configureWithProfile(final ThingifierApiConfigProfile profileToUse) {
        if(profileToUse==null){
            System.out.println("API System Defaults Used");
        }else {
            apiConfig.setFrom(profileToUse.apiConfig());
        }
    }


    public EntityRelModel getERmodel() {
        return erm;
    }

    public ThingRepository getRepository(final String database) {
        return erm.getRepository(database);
    }

    public String exportDataAsJson(final String database) {
        return erm.exportInstanceDataAsJson(database);
    }

    public String reportAsMarkdown(final String database) {
        return erm.reportAsMarkdown(database);
    }

    @Override
    public void close() {
        erm.close();
    }


    /*
        TODO: these are documentation methods, why are they not in the
        documentation classes e.g. ThingifierAPIDefn ?
     */
    public void setDocumentation(final String modelTitle, final String anInitialParagraph) {
        this.title = modelTitle;
        this.initialParagraph = anInitialParagraph;
    }

    public String getTitle() {
        return this.title;
    }

    public String getInitialParagraph() {
        return this.initialParagraph;
    }

    /**
     * @deprecated Compatibility helper backed by legacy instance data. Prefer a
     * repository/provider configured with the desired data source.
     */
    @Deprecated(forRemoval = true, since = "1.5.6")
    public Thingifier cloneWithDifferentData(final List<EntityInstance> instances) {
        return new Thingifier(  this.getERmodel().cloneWithDifferentData(instances),
                                                    this.apiConfig(),
                                                    this.apiConfigProfiles(),
                                                    this.title,
                                                    this.initialParagraph,
                                                    this.apiDocsConfig
                );
    }

    public DataPopulator getDefaultDataPopulator() {
        return dataPopulator;
    }

    // TODO: this is used in too many places, suggesting something went wrong with coding
    // decision: when we create a challenger we always create and populate a database, no need to do it any other time - check that this is enforced and cut down on this usage
    public void ensureCreatedAndPopulatedInstanceDatabaseNamed(String databaseName) {
        if(getERmodel().createInstanceDatabaseIfNotExisting(databaseName)){
            // if we created it then populate it
            if(getDefaultDataPopulator()!=null){
                // Use any default data populator to populate the new database
                ThingRepository repository = getERmodel().getRepository(databaseName);
                repository.refreshSchema(getERmodel().getSchema());
                populateRepository(repository);
            }
        }
    }

    public void ensureCreatedAndPopulatedInstanceDatabaseFromJson(String databaseName, String jsonDatabaseContents) {
        getERmodel().createInstanceDatabaseIfNotExisting(databaseName);

        new JsonPopulator(jsonDatabaseContents).populate(
                getERmodel().getSchema(),
                getERmodel().getRepository(databaseName)
        );

    }

    public ApiDocsConfig apidocsconfig() {
        return apiDocsConfig;
    }

    private void populateRepository(final ThingRepository repository) {
        if (dataPopulator instanceof RepositoryDataPopulator) {
            ((RepositoryDataPopulator) dataPopulator).populate(erm.getSchema(), repository);
            return;
        }

        LegacyDataPopulatorAdapter.populate(dataPopulator, erm.getSchema(), repository);
    }
}
