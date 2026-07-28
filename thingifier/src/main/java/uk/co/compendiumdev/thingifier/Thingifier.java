package uk.co.compendiumdev.thingifier;

import java.util.*;
import uk.co.compendiumdev.thingifier.api.ThingifierRestAPIHandler;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonPopulator;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiSpec;
import uk.co.compendiumdev.thingifier.apiconfig.ApiDocsConfig;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfile;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfiles;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.*;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.reporting.ThingReporter;

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
    private RepositoryDataPopulator dataPopulator;
    private String title;
    private String initialParagraph;
    private final ThingifierApiConfig apiConfig;
    private final ThingifierApiConfigProfiles apiConfigProfiles;
    private final ThingifierApiSpec apiSpec;

    public Thingifier() {
        this(new EntityRelModel());
    }

    public Thingifier(final EntityRelModel erm) {
        this.erm = erm;
        title = "";
        initialParagraph = "";
        apiConfig = new ThingifierApiConfig("");
        apiConfigProfiles = new ThingifierApiConfigProfiles();
        apiSpec = new ThingifierApiSpec();
        apiDocsConfig = new ApiDocsConfig();
    }

    public Thingifier(
            final EntityRelModel erm,
            final ThingifierApiConfig apiConfig,
            final ThingifierApiConfigProfiles apiConfigProfiles,
            final String title,
            final String initialParagraph,
            final ApiDocsConfig apiDocsConfig) {

        this.erm = erm;
        this.title = title;
        this.initialParagraph = initialParagraph;
        this.apiConfig = apiConfig;
        this.apiConfigProfiles = apiConfigProfiles;
        this.apiSpec = new ThingifierApiSpec();
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

    public EntityDefinition defineThing(
            final String thingName, final String pluralName, final int maximumNumberOfInstances) {
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

    public RelationshipDefinition defineRelationship(
            EntityDefinition from, EntityDefinition to, final String named, final Cardinality of) {
        return erm.createRelationshipDefinition(from, to, named, of);
    }

    public boolean hasRelationshipNamed(final String relationshipName) {
        return erm.hasRelationshipNamed(relationshipName);
    }

    // Instances

    public EntityInstance findThingInstanceByGuid(final String thingGUID, final String database) {
        return erm.getStore(database).entityQueries().findByGuid(thingGUID);
    }

    public List<EntityInstance> listThingInstancesNamed(final String aName, final String database) {
        EntityDefinition definition = erm.getSchema().getDefinitionWithSingularOrPluralNamed(aName);
        ThingStore store = erm.getStore(database);
        if (definition == null || store == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(store.entityQueries().list(definition));
    }

    public EntityInstance findThingInstanceByFieldNameAndValue(
            final String entityName,
            final String fieldName,
            final String fieldValue,
            final String database) {
        EntityDefinition definition =
                erm.getSchema().getDefinitionWithSingularOrPluralNamed(entityName);
        ThingStore store = erm.getStore(database);
        if (definition == null || store == null) {
            return null;
        }
        return store.entityQueries().findByField(definition, fieldName, fieldValue);
    }

    public void clearAllData() {
        // clear data in default database but keep database
        clearAllData(EntityRelModel.DEFAULT_DATABASE_NAME);
        // delete all the other databases
        for (String databaseName : erm.getDatabaseNames()) {
            if (!databaseName.equals(EntityRelModel.DEFAULT_DATABASE_NAME)) {
                erm.deleteInstanceDatabase(databaseName);
            }
        }
    }

    public void clearAllData(final String database) {
        erm.getStore(database).administration().clearAllData();
    }

    public void deleteThing(final EntityInstance aThingInstance, final String database) {
        erm.getStore(database).entities().delete(aThingInstance);
    }

    // data generation
    public void generateData(final String database) {
        if (dataPopulator != null) {
            ThingStore store = erm.getStore(database);
            if (store == null) {
                return;
            }
            store.administration().refreshSchema(erm.getSchema());
            populateStore(store);
        }
    }

    public void setDataGenerator(RepositoryDataPopulator dataPopulator) {
        this.dataPopulator = dataPopulator;
        erm.setDataGenerator(dataPopulator);
    }

    // Generic

    public String toString() {

        return new ThingReporter(this).basicReport();
    }

    // API

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

    public ThingifierApiSpec apiSpec() {
        return apiSpec;
    }

    public void configureWithProfile(final ThingifierApiConfigProfile profileToUse) {
        if (profileToUse == null) {
            System.out.println("API System Defaults Used");
        } else {
            apiConfig.setFrom(profileToUse.apiConfig());
        }
    }

    public EntityRelModel getERmodel() {
        return erm;
    }

    public ThingStore getStore(final String database) {
        return erm.getStore(database);
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

    public RepositoryDataPopulator getDefaultDataPopulator() {
        return dataPopulator;
    }

    // TODO: this is used in too many places, suggesting something went wrong with coding
    // decision: when we create a challenger we always create and populate a database, no need to do
    // it any other time - check that this is enforced and cut down on this usage
    public void ensureCreatedAndPopulatedInstanceDatabaseNamed(String databaseName) {
        if (getERmodel().createInstanceDatabaseIfNotExisting(databaseName)) {
            // if we created it then populate it
            if (getDefaultDataPopulator() != null) {
                // Use any default data populator to populate the new database
                ThingStore store = getERmodel().getStore(databaseName);
                store.administration().refreshSchema(getERmodel().getSchema());
                populateStore(store);
            }
        }
    }

    public void ensureCreatedAndPopulatedInstanceDatabaseFromJson(
            String databaseName, String jsonDatabaseContents) {
        getERmodel().createInstanceDatabaseIfNotExisting(databaseName);

        new JsonPopulator(jsonDatabaseContents)
                .populate(getERmodel().getSchema(), getERmodel().getStore(databaseName));
    }

    public ApiDocsConfig apidocsconfig() {
        return apiDocsConfig;
    }

    private void populateStore(final ThingStore store) {
        dataPopulator.populate(erm.getSchema(), store);
    }
}
