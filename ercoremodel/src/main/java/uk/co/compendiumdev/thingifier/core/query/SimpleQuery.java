package uk.co.compendiumdev.thingifier.core.query;

import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.ArrayList;
import java.util.List;

import static uk.co.compendiumdev.thingifier.core.query.SimpleQuery.LastMatchValue.*;

/*
Note this is not the same as a GET e.g.
- /item will always mark the query as not a collection
- /items will always mark the query as a collection

This is a simple query to then build more complex or specific query
processing on top.

Use the isResultACollection, wasIntentToMatchACollection, lastMatchWasInstance
in the calling method.
 */
final public class SimpleQuery {

    private final ERInstanceData database;
    private final ERSchema schema; // all the definitions

    private final String query;

    private boolean isCollection = false;
    private boolean pluralMatch = false;
    private boolean wasIntentToMatchInstance = false;


    enum LastMatchValue {NOTHING, CURRENT_THING, CURRENT_INSTANCE, CURRENT_ITEMS, CURRENT_RELATIONSHIP}

    LastMatchValue lastMatch = NOTHING;

    // populated during search
    EntityInstanceCollection currentCollection = null;
    EntityInstance currentInstance = null;
    List<EntityInstance> foundItems = new ArrayList<>();
    RelationshipVectorDefinition lastRelationshipFound = null;
    List<RelationshipVectorDefinition> lastRelationshipsFound = null;
    EntityInstanceCollection parentCollection = null;
    private EntityInstance parentInstance = null;

    // TODO: this should be an object with FoundItem objects which have getAsRelationshipDefinition etc.
    List<Object> foundItemsHistoryList = new ArrayList<>();
    private EntityDefinition resultContainsDefinition;

    public SimpleQuery(ERSchema aSchema, ERInstanceData aDatabase, String query) {

        this.schema = aSchema;
        this.database = aDatabase;

        if(query.startsWith("/")){
            this.query = query.substring(1);
        }else{
            this.query = query;
        }
    }


    public SimpleQuery performQuery() {
        // a simple query is a URL based REST query
        // e.g. THING/_GUID_/RELATIONSHIP/THING
        // e.g. THING/_ID_/RELATIONSHIP/THING
        // THING/RELATIONSHIP

        String[] terms = query.split("/");


        lastMatch = NOTHING;

        for (String term : terms) {

            // if we have a parent thing then we want to check for relationships before we check for things
            // if it matches a relationship then get the instances identified by the relationship
            //if(currentThing != null && currentThing.definition().hasRelationship(term)){
            if (parentCollection !=null && schema.hasRelationshipNamed(term)) {

                // what I want to store is the relationship between the parent Thing and the relationship name
                EntityInstanceCollection thingToCheckForRelationship = currentCollection == null ? parentCollection : currentCollection;
                lastRelationshipsFound = thingToCheckForRelationship.definition().related().getRelationships(term);
                lastRelationshipFound = lastRelationshipsFound.get(0);

                foundItemsHistoryList.add(lastRelationshipFound);


                if (foundItems != null && !foundItems.isEmpty()) {
                    resultContainsDefinition = foundItems.get(0).getRelationships().getTypeOfConnectableItems(term);
                }

                List<EntityInstance> newitems = new ArrayList<>();
                if (foundItems != null) {
                    for (EntityInstance instance : foundItems) {
                        newitems.addAll(instance.getRelationships().getConnectedItems(term));
                    }
                }

                // relationships is always a collection
                isCollection = true;

                foundItems = newitems;
                parentInstance = currentInstance;
                parentCollection = currentCollection;
                currentCollection = null;
                currentInstance = null;
                lastMatch = CURRENT_RELATIONSHIP;
                continue;
            }

            // if matches an entity type
            if (schema.hasEntityNamed(term) || schema.hasEntityWithPluralNamed(term)) {
                if (currentCollection == null && foundItems.isEmpty()) {
                    // first thing - find it
                    currentCollection = database.getInstanceCollectionForEntityNamed(term);
                    pluralMatch = false;

                    if (currentCollection == null) {
                        // was it the plural?
                        final EntityDefinition defn = schema.getEntityDefinitionWithPluralNamed(term);
                        currentCollection = database.getInstanceCollectionForEntityNamed(defn.getName());
                        pluralMatch = true;
                    }

                    // entity type is always a collection
                    isCollection = true;

                    resultContainsDefinition = currentCollection.definition();
                    foundItemsHistoryList.add(currentCollection);
                    parentCollection = currentCollection;
                    currentInstance = null;
                    lastMatch = CURRENT_THING;
                    foundItems = new ArrayList<>(currentCollection.getInstances());

                } else {
                    // related to another type of thing
                    foundItemsHistoryList.add(database.getInstanceCollectionForEntityNamed(term));

                    if (foundItems != null && !foundItems.isEmpty()) {
                        resultContainsDefinition = foundItems.get(0).getRelationships().getTypeOfConnectableItems(term);
                    }

                    List<EntityInstance> newitems = new ArrayList<>();
                    if (foundItems != null) {
                        for (EntityInstance instance : foundItems) {
                            List<EntityInstance> matchedInstances = instance.getRelationships().getConnectedItemsOfType(term);
                            newitems.addAll(matchedInstances);
                        }
                    }

                    // relationship is a collection
                    foundItems = newitems;
                    lastMatch = CURRENT_ITEMS;
                    parentCollection = currentCollection;
                    currentCollection = null;
                    currentInstance = null;
                }
                continue;
            }



            // is it a GUID or ID?
            // this should be based on the EntityDefinition Primary Identifier Field
            // TODO: create a PrimaryIdentifierField to allow finding via simple query
            boolean found = false;
            for (EntityInstance instance : foundItems) {

                boolean matchBasedOnIdOrGUID = false;

                // found based on ID ?
                final List<Field> idFields = instance.getEntity().
                        getFieldsOfType(FieldType.AUTO_INCREMENT);
                if(!idFields.isEmpty()){
                    final String idValue = instance.getFieldValue(
                            idFields.get(0).getName()).asString();
                    if(idValue.contentEquals(term)){
                        matchBasedOnIdOrGUID=true;
                    }
                }

                if (instance.getPrimaryKeyValue().contentEquals(term)) {
                    matchBasedOnIdOrGUID = true;
                }

                if(matchBasedOnIdOrGUID){

                    foundItemsHistoryList.add(instance);

                    if (currentCollection != null) {
                        parentCollection = currentCollection;
                    }

                    // because we matched based on primary key
                    wasIntentToMatchInstance = true;
                    isCollection = pluralMatch;

                    currentCollection = null;

                    currentInstance = instance;
                    foundItems = new ArrayList<>();
                    foundItems.add(instance);
                    lastMatch = CURRENT_INSTANCE;
                    found = true;
                }
                if (found) {
                    break;
                }
            }
            if (found) {
                // it was a GUID or id
                continue;
            }

            // is it a field?
            // is it a filter query?  e.g. ?title="name"
            lastMatch = NOTHING;
        }

        return this;
    }

    // i.e. did the query end with an identifier which was a primary key
    public boolean wasQueryIntendedToMatchAnInstance(){
        return wasIntentToMatchInstance;
    }

    public boolean isResultACollection() {
        return isCollection;
    }

    // Should not use map from query processing, instead parse to filters using our parser UrlParamParser

    public SimpleQuery performQuery(final QueryFilterParams queryParams) {

        performQuery();
        //filter the results based on the query
        // todo: should we filter single instances?
        if(!isCollection){
            return this;
        }

        final EntityInstanceListFilter filterer = new EntityInstanceListFilter(queryParams);

        foundItems = filterer.filter(foundItems);

        // support sorting after filtering
        final EntityInstanceListSorter sorter = new EntityInstanceListSorter(queryParams);
        foundItems = sorter.sort(foundItems);

        return this;
    }

    public List<EntityInstance> getListEntityInstances() {
        List<EntityInstance> returnThis = new ArrayList<>();

        if (lastMatch == CURRENT_THING) {
            // if not allow filtering then...
            //returnThis.addAll(currentThing.getInstances());
            // if allow filtering then...
            returnThis.addAll(foundItems);
        }

        if (lastMatch == CURRENT_INSTANCE) {
            returnThis.add(currentInstance);
        }

        if (lastMatch == CURRENT_ITEMS || lastMatch == CURRENT_RELATIONSHIP) {
            returnThis.addAll(foundItems);
        }

        //if(lastMatch==NOTHING){ // then the array is already empty}

        return returnThis;
    }

    public boolean lastMatchWasRelationship() {
        return lastMatch == CURRENT_RELATIONSHIP;
    }

    public String getLastRelationshipName() {
        return lastRelationshipFound.getName();
    }

    public EntityInstance getParentInstance() {
        return parentInstance;
    }

    public boolean wasItemFoundUnderARelationship() {

        // not enough in the history list to do the check, so no it wasn't
        if (foundItemsHistoryList.size() - 2 < 0) {
            return false;
        }
        return foundItemsHistoryList.get(foundItemsHistoryList.size() - 2) instanceof RelationshipVectorDefinition;
    }

    public EntityInstance getLastInstance() {
        return currentInstance;
    }

    public boolean lastMatchWasInstance() {
        return lastMatch == CURRENT_INSTANCE;
    }

    public boolean lastMatchWasNothing() {
        return lastMatch == NOTHING;
    }

    public EntityDefinition resultContainsDefn() {
        return resultContainsDefinition;
    }
}
