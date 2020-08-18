package uk.co.compendiumdev.thingifier;

import uk.co.compendiumdev.thingifier.api.ThingifierRestAPIHandler;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfile;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfiles;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.DataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.*;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.ThingReporter;

import java.util.*;


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

    public Thing createThing(final String thingName, final String pluralName) {
        return erm.createThing(thingName, pluralName);
    }

    public List<Thing> getThings() {
        return erm.getThings();
    }


    public ThingInstance findThingInstanceByGuid(final String thingGUID) {
        return erm.findThingInstanceByGuid(thingGUID);
    }

    public boolean hasThingNamed(final String aName) {
        return erm.hasThingNamed(aName);
    }

    public Thing getThingNamed(final String aName) {
        return erm.getThingNamed(aName);
    }

    public boolean hasThingWithPluralNamed(final String term) {
        return erm.hasThingWithPluralNamed(term);
    }

    public Thing getThingWithPluralNamed(final String term) {
        return erm.getThingWithPluralNamed(term);
    }

    public Thing getThingNamedSingularOrPlural(final String term) {
        return erm.getThingNamedSingularOrPlural(term);
    }

    public void clearAllData() {
        erm.clearAllData();
    }

    public void deleteThing(final ThingInstance aThingInstance) {
        erm.deleteThing(aThingInstance);
    }

    public List<String> getThingNames() {
        return erm.getThingNames();
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

    public RelationshipDefinition defineRelationship(Thing from, Thing to,
                                                     final String named, final Cardinality of) {
        return erm.defineRelationship(from,to,named, of);
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
        return new ThingifierRestAPIHandler(this);
    }

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
}
