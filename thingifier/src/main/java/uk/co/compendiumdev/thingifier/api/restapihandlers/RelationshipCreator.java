package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RelationshipCreator {
    private final Thingifier thingifier;

    public RelationshipCreator(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    public ApiResponse createRelationships(final BodyParser bodyargs, final ThingInstance instance) {
        try {
            List<RelationshipDetails> relationships = getRelationshipsFromArgs(bodyargs);
            for (RelationshipDetails relationship : relationships) {
                instance.connects(relationship.relationshipName,
                        thingifier.getThingNamedSingularOrPlural(relationship.toType).
                                findInstanceByGUID(relationship.guidValue));
            }

            return ApiResponse.created(instance);

        }catch(Exception e){
            return ApiResponse.error(400, "Error creating relationships " + e.getMessage());
        }
    }

    private List<RelationshipDetails> getRelationshipsFromArgs(final BodyParser bodyargs) {

        List<Map.Entry<String,String>> fullargs = bodyargs.getFlattenedStringMap();
        List<RelationshipDetails>relationships = new ArrayList<>();

        // assume any relationships errors already reported

        for(Map.Entry<String, String> complexKeyValue : fullargs) {
            //is it a relationship?
            String complexKey = complexKeyValue.getKey();
            if (complexKey.startsWith("relationships.")) {
                String[] parts = complexKey.split("\\.");
                if (parts.length == 4) {
                    relationships.add(
                            new RelationshipDetails(
                                    parts[1], parts[2], parts[3],
                                    complexKeyValue.getValue()));
                }
            }else{
                // support compressed relationships
                if(complexKey.contains(".")) {
                    String[] parts = complexKey.split("\\.");
                    // assume it is a relationship - because of earlier validation
                    if(parts.length == 2){
                        final ThingInstance instanceToRelateTo = thingifier.findThingInstanceByGuid(complexKeyValue.getValue());
                        if(instanceToRelateTo!=null){
                            relationships.add(
                                new RelationshipDetails(
                                        parts[0], instanceToRelateTo.getEntity().getPlural(), parts[1],
                                        complexKeyValue.getValue()));
                        }
                    }
                }
            }
        }
        return relationships;
    }

    private class RelationshipDetails {
        public final String relationshipName;
        public final String toType;
        public final String guidName;
        public final String guidValue;

        public RelationshipDetails(final String relationshipName, final String toType, final String keyName, final String keyValue) {
            this.relationshipName = relationshipName;
            this.toType = toType;
            this.guidName = keyName;
            this.guidValue = keyValue;
        }
    }
}
