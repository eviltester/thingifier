package uk.co.compendiumdev.thingifier;

import uk.co.compendiumdev.thingifier.api.ThingifierRestAPIHandler;
import uk.co.compendiumdev.thingifier.generic.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.generic.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipVector;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.WithCardinality;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.AndCall;
import uk.co.compendiumdev.thingifier.generic.dsl.relationship.Between;
import uk.co.compendiumdev.thingifier.query.SimpleQuery;
import uk.co.compendiumdev.thingifier.reporting.ThingReporter;

import java.util.*;

public class Thingifier {

    Map<String, Thing> things = new HashMap<String, Thing>();
    private Map<String, RelationshipDefinition> relationships = new HashMap<String, RelationshipDefinition>();
    private String title="";
    private String initialParagraph="";


    public Thing createThing(String thingName, String pluralName) {
        Thing aThing = Thing.create(thingName, pluralName);
        things.put(thingName, aThing);
        return aThing;
    }

    public List<Thing> getThings(){
        return new ArrayList<Thing>(things.values());
    }

    public boolean hasThingNamed(String aName){
        return things.containsKey(aName);
    }

    public Thing getThingNamed(String aName){
        return things.get(aName);
    }

    public RelationshipDefinition defineRelationship(Between things, AndCall it, Cardinality of) {
        RelationshipDefinition relationship = RelationshipDefinition.create( things.from(), things.to(), new RelationshipVector(it.isCalled(), of));
        relationships.put(it.isCalled(), relationship);
        return relationship;
    }

    public RelationshipDefinition defineRelationshipBetween(String nameOfFromThing, String nameOfToThing, AndCall it) {
        return defineRelationship(Between.things(getThingNamed(nameOfFromThing),
                getThingNamed(nameOfToThing)), it, WithCardinality.of("1", "*"));
    }

    public String toString(){

        return new ThingReporter(things, relationships).basicReport();
    }


    // todo: allow duplicate named relationships but between different types of things
    private RelationshipDefinition getRelationship(String relationshipName) {
        return relationships.get(relationshipName);
    }

    public List<ThingInstance> simplequery(String query) {

        return new SimpleQuery(this, query).performQuery().getListThingInstance();

    }

    public ThingifierRestAPIHandler api() {
        return new ThingifierRestAPIHandler(this);
    }

    public boolean hasRelationshipNamed(String relationshipName) {
        if(relationships.containsKey(relationshipName.toLowerCase())){
            return true;
        }

        // perhaps it is a reverse relationship?
        for(RelationshipDefinition defn : relationships.values()){
            if(defn.isTwoWay()){
                if(defn.getReversedRelationship().getName().equalsIgnoreCase(relationshipName)){
                    return true;
                }
            }

        }

        return false;
    }


    public ThingInstance findThingInstanceByGuid(String thingGUID) {
        for(Thing aThing : things.values()){
            ThingInstance instance = aThing.findInstanceByField(FieldValue.is("guid", thingGUID));
            if(instance!=null){
                return instance;
            }
        }
        return null;
    }

    public Collection<RelationshipDefinition> getRelationshipDefinitions() {
        return relationships.values();
    }

    public void setDocumentation(String title, String initialParagraph) {
        this.title = title;
        this.initialParagraph = initialParagraph;


    }

    public String getTitle() {
        return this.title;
    }

    public String getInitialParagraph() {
        return this.initialParagraph;
    }
}
