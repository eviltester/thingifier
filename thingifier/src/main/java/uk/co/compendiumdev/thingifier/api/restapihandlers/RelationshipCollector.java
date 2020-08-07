package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RelationshipCollector {

    private List<Map.Entry<String,String>> identifiedRelationships;
    private List<RelationshipDetails> theRelationshipDetails;

    public RelationshipCollector(){
        identifiedRelationships = new ArrayList<>();
        theRelationshipDetails = new ArrayList<>();
    }

    public void thisIsARelationship(final Map.Entry<String, String> complexKeyValue,
                                        final RelationshipDetails aRelationshipDetails) {
        identifiedRelationships.add(complexKeyValue);
        theRelationshipDetails.add(aRelationshipDetails);
    }

    public List<Map.Entry<String, String>> getRelationshipsKeys() {
        return identifiedRelationships;
    }

    public List<RelationshipDetails> getRelationshipDetails() {
        return theRelationshipDetails;
    }
}
