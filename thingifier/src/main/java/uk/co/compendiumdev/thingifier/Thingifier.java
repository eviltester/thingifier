package uk.co.compendiumdev.thingifier;

import uk.co.compendiumdev.thingifier.api.ThingifierRestAPIHandler;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfile;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfiles;
import uk.co.compendiumdev.thingifier.domain.FieldType;
import uk.co.compendiumdev.thingifier.domain.data.ThingifierDataPopulator;
import uk.co.compendiumdev.thingifier.domain.definitions.*;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.domain.dsl.relationship.AndCall;
import uk.co.compendiumdev.thingifier.domain.dsl.relationship.Between;
import uk.co.compendiumdev.thingifier.reporting.ThingReporter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

final public class Thingifier {

    private ThingifierDataPopulator initialDataGenerator;
    private Map<String, Thing> things;
    private Map<String, RelationshipDefinition> relationships;
    private String title;
    private String initialParagraph;
    private final ThingifierApiConfig apiConfig;
    private final ThingifierApiConfigProfiles apiConfigProfiles;

    public Thingifier(){
        things = new ConcurrentHashMap<String, Thing>();
        relationships = new ConcurrentHashMap<String, RelationshipDefinition>();
        title = "";
        initialParagraph = "";
        apiConfig = new ThingifierApiConfig();
        apiConfigProfiles = new ThingifierApiConfigProfiles();
        initialDataGenerator=null; // todo consider having a default random data generator
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



    public Thing createThing(final String thingName, final String pluralName) {
        Thing aThing = Thing.create(thingName, pluralName);
        things.put(thingName, aThing);
        return aThing;
    }

    public List<Thing> getThings() {
        return new ArrayList<Thing>(things.values());
    }


    public RelationshipDefinition defineRelationship(final Between giventhings, final AndCall it, final Cardinality of) {
        RelationshipDefinition relationship = RelationshipDefinition.create(giventhings.from(), giventhings.to(), new RelationshipVector(it.isCalled(), of));
        relationships.put(it.isCalled(), relationship);
        return relationship;
    }


    public String toString() {

        return new ThingReporter(this).basicReport();
    }


    public ThingifierRestAPIHandler api() {
        return new ThingifierRestAPIHandler(this);
    }

    public boolean hasRelationshipNamed(final String relationshipName) {
        if (relationships.containsKey(relationshipName.toLowerCase())) {
            return true;
        }

        // perhaps it is a reverse relationship?
        for (RelationshipDefinition defn : relationships.values()) {
            if (defn.isTwoWay()) {
                if (defn.getReversedRelationship().getName().equalsIgnoreCase(relationshipName)) {
                    return true;
                }
            }

        }

        return false;
    }


    public ThingInstance findThingInstanceByGuid(final String thingGUID) {
        for (Thing aThing : things.values()) {
            ThingInstance instance = aThing.findInstanceByField(FieldValue.is("guid", thingGUID));
            if (instance != null) {
                return instance;
            }
        }
        return null;
    }

    public Collection<RelationshipDefinition> getRelationshipDefinitions() {
        return relationships.values();
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


    public boolean hasThingNamed(final String aName) {
        return things.containsKey(aName);
    }

    public Thing getThingNamed(final String aName) {
        return things.get(aName);
    }

    public boolean hasThingWithPluralNamed(final String term) {
        Thing aThing = getThingWithPluralNamed(term);
        return aThing != null;
    }

    public Thing getThingWithPluralNamed(final String term) {
        for (Thing aThing : things.values()) {
            if (aThing.definition().getPlural().equalsIgnoreCase(term)) {
                return aThing;
            }
        }
        return null;
    }

    public Thing getThingNamedSingularOrPlural(final String term) {
        Thing thing = getThingNamed(term);
        if (thing == null) {
            if (hasThingWithPluralNamed(term)) {
                thing = getThingWithPluralNamed(term);
            }
        }

        return thing;
    }

    public void clearAllData() {
        // clear all instance data
        for (Thing aThing : things.values()) {
            for(ThingInstance instance : aThing.getInstances()) {
                deleteThing(instance);
            }
        }
    }

    public void deleteThing(final ThingInstance aThingInstance) {
        // delete a thing and all related things with mandatory relationships
        final Thing aThing = getThingNamed(aThingInstance.getEntity().getName());

        // we may also have to delete things which are mandatorily related i.e. can't exist on their own
        final List<ThingInstance> otherThingsToDelete = aThing.deleteInstance(aThingInstance.getGUID());

        // TODO: Warning recursion with no 'cut off' if any cyclical relationships then this might fail
        for(ThingInstance deleteMe : otherThingsToDelete){
            deleteThing(deleteMe);
        }
    }

    public List<String> getThingNames() {
        List<String> names = new ArrayList();
        names.addAll(things.keySet());
        return names;
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

    public void generateData() {
        if(initialDataGenerator!=null) {
            initialDataGenerator.populate(this);
        }
    }

    public void setDataGenerator(ThingifierDataPopulator dataPopulator) {
        initialDataGenerator = dataPopulator;
    }

    public void setNextIdsToAccomodate(final BodyParser bodyargs, final Thing thing) {
        final List<Map.Entry<String, String>> fieldNamesAndValues = bodyargs.getFlattenedStringMap();
        final ThingDefinition defn = thing.definition();
        // todo: process nested objects - currently assume these are not ids, but they might be
        for(Map.Entry<String, String> fieldNameValue : fieldNamesAndValues){
            final Field field = defn.getField(fieldNameValue.getKey());
            if(field!=null && field.getType()== FieldType.ID) {
                defn.ensureNextIdAbove(fieldNameValue.getValue());
            }
        }
    }
}
