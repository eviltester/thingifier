package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.domain.definitions.FieldValue;
import uk.co.compendiumdev.thingifier.domain.definitions.RelationshipVector;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BodyArgsProcessor {

    private final Thingifier thingifier;
    private final BodyParser bodyargs;

    public BodyArgsProcessor(Thingifier thingifier, BodyParser bodyargs){
        this.thingifier = thingifier;
        this.bodyargs = bodyargs;
    }

    public List<Map.Entry<String,String>> removeRelationshipsFrom(final ThingInstance instance){

        List<Map.Entry<String,String>> fullargs = bodyargs.getFlattenedStringMap();
        List<Map.Entry<String,String>> relationships = new ArrayList<>();

        // assume any relationships errors already reported

        for(Map.Entry<String, String> complexKeyValue : fullargs) {
            //is it a relationship?
            String complexKey = complexKeyValue.getKey();
            if (complexKey.startsWith("relationships.")) {
                String[] parts = complexKey.split("\\.");
                if (parts.length == 4) {
                    relationships.add(complexKeyValue);
                }
            }else{
                // support compressed relationships
                if(complexKey.contains(".")) {
                    String[] parts = complexKey.split("\\.");
                    // assume it is a relationship - because of earlier validation
                    if(parts.length == 2){
                        String relationshipName = parts[0];
                        String relationshipFieldName = parts[1];
                        // assume it is a guid
                        ThingInstance instanceToRelateTo = thingifier.findThingInstanceByGuid(complexKeyValue.getValue());
                        if(instanceToRelateTo ==null){
                            // but it might not be
                            // TODO: find other usages of this pattern and refactor to
                            if(instance.getEntity().hasRelationship(relationshipName)){
                                final List<RelationshipVector> relationshipsAre =
                                        instance.getEntity().getRelationships(relationshipName);
                                for(RelationshipVector relate : relationshipsAre){
                                    instanceToRelateTo = relate.getTo().
                                            findInstanceByField(
                                                    FieldValue.is(relationshipFieldName, complexKeyValue.getValue()));
                                    if(instanceToRelateTo!=null){
                                        break;
                                    }
                                }
                            }
                        }
                        if(instanceToRelateTo!=null){
                            relationships.add(complexKeyValue);
                        }
                    }
                }
            }
        }

        for(Map.Entry<String, String> removeMe : relationships) {
            fullargs.remove(removeMe);
        }

        return fullargs;
    }
}
