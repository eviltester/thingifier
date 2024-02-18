package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.List;
import java.util.Map;

// todo: potentially move this into the BodyParser
public class BodyArgsProcessor {

    private final Thingifier thingifier;
    private final BodyParser bodyargs;

    public BodyArgsProcessor(Thingifier thingifier, BodyParser bodyargs){
        this.thingifier = thingifier;
        this.bodyargs = bodyargs;
    }


    public List<Map.Entry<String,String>> removeRelationshipsFrom(final EntityInstance instance, final String database) {

        List<Map.Entry<String,String>> fullargs = bodyargs.getFlattenedStringMap();
        RelationshipCollector collectedRelationships = new RelationshipCollector();

        identifyRelationships(fullargs, instance, collectedRelationships, database);

        for(Map.Entry<String, String> removeMe : collectedRelationships.getRelationshipsKeys()) {
            fullargs.remove(removeMe);
        }

        return fullargs;

    }

    public void identifyRelationships(List<Map.Entry<String,String>> fullargs,
                                     final EntityInstance instance,
                                     RelationshipCollector collector,
                                      final String database){

        // assume any relationships errors already reported

        for(Map.Entry<String, String> complexKeyValue : fullargs) {
            //is it a relationship?
            String complexKey = complexKeyValue.getKey();
            if (complexKey.startsWith("relationships.")) {
                String[] parts = complexKey.split("\\.");
                if (parts.length == 4) {
                    collector.thisIsARelationship(
                                    complexKeyValue,
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
                        String relationshipName = parts[0];
                        String relationshipFieldName = parts[1];
                        // assume it is a guid
                        EntityInstance instanceToRelateTo = thingifier.findThingInstanceByGuid(complexKeyValue.getValue(), database);
                        if(instanceToRelateTo ==null){
                            // but it might not be
                            // TODO: find other usages of this pattern and refactor to
                            if(instance.getEntity().related().hasRelationship(relationshipName)){
                                final List<RelationshipVectorDefinition> relationshipsAre =
                                        instance.getEntity().related().getRelationships(relationshipName);
                                for(RelationshipVectorDefinition relate : relationshipsAre){
                                    final EntityDefinition typeOfThing = relate.getTo();
                                    instanceToRelateTo =  thingifier.getThingInstancesNamed(typeOfThing.getName(), database).
                                            findInstanceByFieldNameAndValue(relationshipFieldName,
                                                                complexKeyValue.getValue());
                                    if(instanceToRelateTo!=null){
                                        break;
                                    }
                                }
                            }
                        }
                        if(instanceToRelateTo!=null){
                            collector.thisIsARelationship(
                                    complexKeyValue,
                                    new RelationshipDetails(
                                            relationshipName,
                                            instanceToRelateTo.getEntity().getPlural(),
                                            relationshipFieldName,
                                            complexKeyValue.getValue()));
                        }
                    }
                }
            }
        }


    }
}
