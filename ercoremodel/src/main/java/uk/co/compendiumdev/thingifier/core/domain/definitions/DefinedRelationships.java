package uk.co.compendiumdev.thingifier.core.domain.definitions;

import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefinedRelationships {

    private Map<String, List<RelationshipVector>> relationships;

    public DefinedRelationships(){
        this.relationships = new ConcurrentHashMap<>();
    }

    public boolean hasRelationship(final String relationshipName) {
        return relationships.containsKey(relationshipName.toLowerCase());
    }

    public void addRelationship(final RelationshipVector relationship) {
        List<RelationshipVector> relationshipsWithThisName = relationships.get(relationship.getName());
        if (relationshipsWithThisName == null) {
            // there is no relationship with this name
            relationshipsWithThisName = new ArrayList<>();
            relationships.put(relationship.getName(), relationshipsWithThisName);
        }

        relationshipsWithThisName.add(relationship);
    }

    public List<RelationshipVector> getRelationships(final String relationshipName) {

        String seekName = relationshipName.toLowerCase();

        if(!relationships.containsKey(seekName)){
            return new ArrayList<>();
        }

        List<RelationshipVector> myrelationships = relationships.get(seekName);
        return new ArrayList<>(myrelationships);
    }

    public Set<RelationshipVector> getRelationships() {

        Set<RelationshipVector> myRelationships = new HashSet<>();
        for (List<RelationshipVector> list : relationships.values()) {
            for (RelationshipVector rel : list) {
                myRelationships.add(rel);
            }

        }
        return myRelationships;
    }
}
