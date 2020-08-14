package uk.co.compendiumdev.thingifier.core;

import uk.co.compendiumdev.thingifier.core.domain.datapopulator.DataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVector;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class EntityRelModel {

    private DataPopulator initialDataGenerator;
    private final ConcurrentHashMap<String, Thing> things;
    private final ConcurrentHashMap<String, RelationshipDefinition> relationships;

    public EntityRelModel(){
        things = new ConcurrentHashMap<String, Thing>();
        relationships = new ConcurrentHashMap<String, RelationshipDefinition>();
        initialDataGenerator=null; // todo consider having a default random data generator
    }

    public Thing createThing(final String thingName, final String pluralName) {
        Thing aThing = Thing.create(thingName, pluralName);
        things.put(thingName, aThing);
        return aThing;
    }

    public List<Thing> getThings() {
        return new ArrayList<Thing>(things.values());
    }

    public boolean hasThingNamed(final String aName) {
        return things.containsKey(aName);
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

    public void deleteThing(final ThingInstance aThingInstance) {
        // delete a thing and all related things with mandatory relationships
        final Thing aThing = getThingNamed(aThingInstance.getEntity().getName());

        if(aThing==null){
            // if it was a hanging thing, not managed by EntityRelModel
            return;
        }

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

    public void clearAllData() {
        // clear all instance data
        for (Thing aThing : things.values()) {
            for(ThingInstance instance : aThing.getInstances()) {
                deleteThing(instance);
            }
        }
    }

    // data generation
    public void generateData() {
        if(initialDataGenerator!=null) {
            initialDataGenerator.populate(this);
        }
    }

    public void setDataGenerator(DataPopulator dataPopulator) {
        initialDataGenerator = dataPopulator;
    }

    // Relationships

    public Collection<RelationshipDefinition> getRelationshipDefinitions() {
        return relationships.values();
    }

    public RelationshipDefinition defineRelationship(Thing from, Thing to, final String named, final Cardinality of) {
        RelationshipDefinition relationship =
                RelationshipDefinition.create(
                        new RelationshipVector(
                                from,
                                named,
                                to,
                                of));
        relationships.put(named, relationship);
        return relationship;
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
}
