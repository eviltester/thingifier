package uk.co.compendiumdev.thingifier;

import uk.co.compendiumdev.thingifier.api.ThingifierRestAPIHandler;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfile;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfiles;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.DataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.*;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.reporting.ThingReporter;

import java.util.*;

/* Thingifier
    is the main class that allows access to:
    - the ERM Schema
    - the ERM data
    - the API Definition and config
    - TODO: why is the API documentation not in here?
 */
final public class Thingifier {

    private final EntityRelModel erm;
    private String title;
    private String initialParagraph;
    private final ThingifierApiConfig apiConfig;
    private final ThingifierApiConfigProfiles apiConfigProfiles;

    public Thingifier(){
        erm = new EntityRelModel();
        title = "";
        initialParagraph = "";
        apiConfig = new ThingifierApiConfig();
        apiConfigProfiles = new ThingifierApiConfigProfiles();
    }

    public Thingifier(final EntityRelModel erm,
                      final ThingifierApiConfig apiConfig,
                      final ThingifierApiConfigProfiles apiConfigProfiles,
                      final String title,
                      final String initialParagraph) {

        this.erm = erm;
        this.title = title;
        this.initialParagraph = initialParagraph;
        this.apiConfig = apiConfig;
        this.apiConfigProfiles = apiConfigProfiles;
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
        aliases to entites and relationships to override definitions in the entity etc.
        create 'queries' to show subsets of data, etc.
        Do not put this into the entities and relationships make this a separate model
     */


    // THINGS

    public EntityDefinition defineThing(final String thingName, final String pluralName) {
        return erm.createEntityDefinition(thingName, pluralName);
    }

    public List<EntityInstanceCollection> getThings() {
        return erm.getAllEntityInstanceCollections();
    }


    public EntityInstance findThingInstanceByGuid(final String thingGUID) {
        return erm.findEntityInstanceByGuid(thingGUID);
    }

    public boolean hasThingNamed(final String aName) {
        return erm.hasEntityNamed(aName);
    }

    public EntityInstanceCollection getThingInstancesNamed(final String aName) {
        return erm.getInstanceCollectionForEntityNamed(aName);
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

    public EntityInstanceCollection getInstancesForSingularOrPluralNamedEntity(final String term) {
        final EntityDefinition defn = erm.getSchema().getDefinitionWithSingularOrPluralNamed(term);
        if(defn!=null){
            return erm.getInstanceCollectionForEntityNamed(defn.getName());
        }

        return null;
    }

    public void clearAllData() {
        erm.clearAllData();
    }

    public void deleteThing(final EntityInstance aThingInstance) {
        erm.deleteEntityInstance(aThingInstance);
    }

    public List<String> getThingNames() {
        return erm.getEntityNames();
    }

    // data generation

    public void generateData() {
        erm.generateData();
    }

    public void setDataGenerator(DataPopulator dataPopulator) {
        erm.setDataGenerator(dataPopulator);
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

    public Thingifier cloneWithDifferentData(final List<EntityInstance> instances) {
        return new Thingifier(  this.getERmodel().cloneWithDifferentData(instances),
                                                    this.apiConfig(),
                                                    this.apiConfigProfiles(),
                                                    this.title,
                                                    this.initialParagraph);
    }
}
