package uk.co.compendiumdev.thingifier.core;

import uk.co.compendiumdev.thingifier.core.domain.datapopulator.DataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVector;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/*
    The ERM has the 'model' (ERSchema) and the 'instances' (things).
    Schema and instances are separate to allow us to have multiple
    'databases' in memory at the same time built from the same schema.
 */
public class EntityRelModel {

    private DataPopulator initialDataGenerator;
    private final ConcurrentHashMap<String, Thing> things;
    private final ERSchema schema; // all the definitions

    public EntityRelModel(){
        things = new ConcurrentHashMap<String, Thing>();
        schema = new ERSchema();
        initialDataGenerator=null; // todo consider having a default random data generator
    }

    public Thing createThing(final String thingName, final String pluralName) {
        Thing aThing = Thing.create(thingName, pluralName);
        things.put(thingName, aThing);
        schema.addThingDefinition(aThing.definition());
        return aThing;
    }

    // Schema methods
    public boolean hasThingNamed(final String aName) {
        return schema.hasThingNamed(aName);
    }

    public List<String> getThingNames() {
        return schema.getThingNames();
    }

    public Collection<RelationshipDefinition> getRelationshipDefinitions() {
        return schema.getRelationships();
    }

    public RelationshipDefinition defineRelationship(Thing from, Thing to, final String named, final Cardinality of) {
        return schema.defineRelationship(from.definition(), to.definition(), named, of);
    }

    public boolean hasRelationshipNamed(final String relationshipName) {
        return schema.hasRelationshipNamed(relationshipName);
    }

    // Instance Methods

    public List<Thing> getThings() {
        return new ArrayList<Thing>(things.values());
    }

    public ThingInstance findThingInstanceByGuid(final String thingGUID) {
        for (Thing aThing : things.values()) {
            final List<String> guidFields = aThing.definition().getFieldNamesOfType(FieldType.GUID);
            for(String fieldName : guidFields){
                ThingInstance instance = aThing.
                        findInstanceByField(FieldValue.is(fieldName, thingGUID));
                if (instance != null) {
                    return instance;
                }
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


}
