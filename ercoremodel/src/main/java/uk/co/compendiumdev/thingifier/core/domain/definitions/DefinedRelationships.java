package uk.co.compendiumdev.thingifier.core.domain.definitions;

import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefinedRelationships {

    private Map<String, List<RelationshipVectorDefinition>> relationships;

    public DefinedRelationships(){
        this.relationships = new ConcurrentHashMap<>();
    }

    public boolean hasRelationship(final String relationshipName) {
        return relationships.containsKey(relationshipName.toLowerCase());
    }

    public void addRelationship(final RelationshipVectorDefinition relationship) {
        List<RelationshipVectorDefinition> relationshipsWithThisName = relationships.get(relationship.getName());
        if (relationshipsWithThisName == null) {
            // there is no relationship with this name
            relationshipsWithThisName = new ArrayList<>();
            relationships.put(relationship.getName(), relationshipsWithThisName);
        }

        relationshipsWithThisName.add(relationship);
    }

    public List<RelationshipVectorDefinition> getRelationships(final String relationshipName) {

        String seekName = relationshipName.toLowerCase();

        if(!relationships.containsKey(seekName)){
            return new ArrayList<>();
        }

        List<RelationshipVectorDefinition> myrelationships = relationships.get(seekName);
        return new ArrayList<>(myrelationships);
    }

    public Set<RelationshipVectorDefinition> getRelationships() {

        Set<RelationshipVectorDefinition> myRelationships = new HashSet<>();
        for (List<RelationshipVectorDefinition> list : relationships.values()) {
            for (RelationshipVectorDefinition rel : list) {
                myRelationships.add(rel);
            }

        }
        return myRelationships;
    }
}
