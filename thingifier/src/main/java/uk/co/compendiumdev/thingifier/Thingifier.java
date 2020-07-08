package uk.co.compendiumdev.thingifier;

import uk.co.compendiumdev.thingifier.api.ThingifierRestAPIHandler;
import uk.co.compendiumdev.thingifier.generic.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.generic.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipVector;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.AndCall;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.Between;
import uk.co.compendiumdev.thingifier.reporting.ThingReporter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

final public class Thingifier {

    private Map<String, Thing> things = new ConcurrentHashMap<String, Thing>();
    private Map<String, RelationshipDefinition> relationships = new ConcurrentHashMap<String, RelationshipDefinition>();
    private String title = "";
    private String initialParagraph = "";

    /*
        TODO: configure the REST API from the entities and relationship definitions
        at the moment a default REST API is created, consider an API model as separate
        e.g
         - API.usePluralNouns(), useSingleNouns()
         - API.allowQueryParamFilters()
         - API.disallowQueryParamFilters("/todos")
         - API.routing("/todos").disallow("PATCH,POST.UPDATE")
         - API.hideGUIDsWhenIDAvailable()
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
}
