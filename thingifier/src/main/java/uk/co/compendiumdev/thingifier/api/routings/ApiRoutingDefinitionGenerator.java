package uk.co.compendiumdev.thingifier.api.routings;

import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.api.response.ResponseHeader;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVector;

import java.util.List;

public class ApiRoutingDefinitionGenerator {


    private final Thingifier thingifier;
    private final ThingifierApiConfig config;

    private final String uniqueGUID = ":guid";
    private final String uniqueID = ":id";

    public ApiRoutingDefinitionGenerator(final Thingifier thingifier) {
        this.thingifier = thingifier;
        this.config = thingifier.apiConfig();
    }

    // TODO: generate /_plural_ instead of /_entityName_ for top level routing (make this toggelable from command line (to inject buggyness) but make plural the default
    // - plural should always return a collection object regardless if it is a single instance or not
    // - single should return a single if instance and collection if multiple
    // TODO: have a toggle to allow for /_entityName  - this should only be valid if  /_entityName/_guid_ and will return a single object
    // TODO: create an /instance/_entityName_/_guid_ that provides a report with all relationships listed as objects e.g.
    /*
      /instance/todo/_guid_
            todo
                guid
                todo-field-1
                todi-field-1
                relationship1
                    thing
                        guid
                        thing-field1
                        thing-field2
                    thing
                        guid
                        thing-field1
                        thing-field2
                    etc. (no relationships for 'thing' are included)
    */



    // TODO: have the ability to override these from config and define from config rather than code
    public ApiRoutingDefinition generate() {
        ApiRoutingDefinition defn = new ApiRoutingDefinition();




        for (Thing thing : thingifier.getThings()) {

            String uniqueIdentifier;
            String uniqueIdFieldName;
            // todo: a thing can have many id fields, so should choose one to be in the url
            // e.g. set a field as 'usedForIndividualRouting'
            final List<Field> idFields = thing.definition().getFieldsOfType(FieldType.ID);
            if(config.willUrlsShowIdsIfAvailable() && !idFields.isEmpty()){
                uniqueIdentifier=uniqueID;
                uniqueIdFieldName = idFields.get(0).getName();
            }else{
                uniqueIdentifier=uniqueGUID;
                uniqueIdFieldName="guid";
            }

            String pluralUrl;
            if(config.willUrlShowInstancesAsPlural()) {
                pluralUrl = thing.definition().getPlural().toLowerCase();
            }else {
                pluralUrl = thing.definition().getName().toLowerCase();
            }

            // we should be able to get things e.g. GET project
            defn.addRouting(
                    String.format("return all the instances of %s", thing.definition().getName()),
                    RoutingVerb.GET, pluralUrl, RoutingStatus.returnedFromCall()).
                    addPossibleStatus(RoutingStatus.returnValue(
                                200, String.format("All the %s", thing.definition().getPlural()))).
                    setAsFilterableFrom(thing.definition());

            defn.addRouting(String.format("headers for all the instances of %s", thing.definition().getName()),
                    RoutingVerb.HEAD, pluralUrl, RoutingStatus.returnedFromCall()).
                    addPossibleStatus(RoutingStatus.returnValue(200));

            // TODO: more granularity if singleUrl!=pluralUrl then we need two paths here
            // e.g. GET projects, POST project - which also changes the Options
            // we should be able to create things without a GUID e.g. POST project
            defn.addRouting(
                    String.format("we should be able to create %s without a %s using the field values in the body of the message",
                                        thing.definition().getName(), uniqueIdFieldName.toUpperCase()),
                    RoutingVerb.POST, pluralUrl, RoutingStatus.returnedFromCall()).
                    addPossibleStatus(RoutingStatus.returnValue(
                            201, String.format("Created a %s", thing.definition().getName()))).
                    addPossibleStatus(RoutingStatus.returnValue(
                            400, String.format("Error when creating a %s", thing.definition().getName())));

            defn.addRouting(
                    String.format("show all Options for endpoint of %s", pluralUrl),
                    RoutingVerb.OPTIONS, pluralUrl, RoutingStatus.returnValue(200, "the endpoint verb options"),
                    new ResponseHeader("Allow", "OPTIONS, GET, HEAD, POST"));

            // the following are not handled so return 405
            defn.addRouting("method not allowed",
                    RoutingVerb.DELETE, pluralUrl, RoutingStatus.returnValue(405));
            defn.addRouting("method not allowed",
                    RoutingVerb.PATCH, pluralUrl, RoutingStatus.returnValue(405));
            defn.addRouting("method not allowed",
                    RoutingVerb.PUT, pluralUrl, RoutingStatus.returnValue(405));

            String aUrlWGuid = pluralUrl + "/" + uniqueIdentifier;
            // we should be able to get specific things based on the GUID e.g. GET project/GUID
            defn.addRouting(
                    String.format("return a specific instances of %s using a %s",
                            thing.definition().getName(),uniqueIdFieldName),
                    RoutingVerb.GET, aUrlWGuid, RoutingStatus.returnedFromCall()).
                    addPossibleStatus(RoutingStatus.returnValue(
                            200, String.format("A specific %s", thing.definition().getName()))).
                    addPossibleStatus(RoutingStatus.returnValue(
                            404, String.format("Could not find a specific %s", thing.definition().getName())));

            defn.addRouting(String.format("headers for a specific instances of %s using a %s",
                            thing.definition().getName(),uniqueIdFieldName),
                    RoutingVerb.HEAD, aUrlWGuid, RoutingStatus.returnedFromCall()).
                    addPossibleStatus(RoutingStatus.returnValue(
                            200, String.format("Headers for a specific %s", thing.definition().getName()))).
                    addPossibleStatus(RoutingStatus.returnValue(
                            404, String.format("Could not find a specific %s", thing.definition().getName())));;

            // we should be able to amend things with a GUID e.g. POST project/GUID with body
            defn.addRouting(
                    String.format("amend a specific instances of %s using a %s with a body containing the fields to amend",
                            thing.definition().getName(), uniqueIdFieldName),
                    RoutingVerb.POST, aUrlWGuid, RoutingStatus.returnedFromCall()).
                    addPossibleStatus(RoutingStatus.returnValue(
                            200, String.format("Amended the specific %s", thing.definition().getName()))).
                    addPossibleStatus(RoutingStatus.returnValue(
                            404, String.format("Could not find a specific %s", thing.definition().getName())));;

            // we should be able to amend things with PUT and a GUID e.g. PUT project/GUID
            String ifGUIDamendment="";
            if(uniqueIdentifier.contentEquals(uniqueGUID)){
                ifGUIDamendment = String.format(", if the %2$s does not exist then a %1$s will be created with that %2$s",
                        thing.definition().getName(),uniqueIdFieldName);
            }
            defn.addRouting(
                    String.format("amend a specific instances of %1$s using a %2$s with a body containing the fields to amend",
                            thing.definition().getName(),uniqueIdFieldName )+ifGUIDamendment,
                    RoutingVerb.PUT, aUrlWGuid, RoutingStatus.returnedFromCall()).
                    addPossibleStatus(RoutingStatus.returnValue(
                            200, String.format("Replaced the specific %s details", thing.definition().getName()))).
                    addPossibleStatus(RoutingStatus.returnValue(
                            404, String.format("Could not find a specific %s", thing.definition().getName())));;

            // we should be able to delete specific things e.g. DELETE project/GUID
            defn.addRouting(
                    String.format("delete a specific instances of %s using a %s",
                                thing.definition().getName(), uniqueIdFieldName),
                    RoutingVerb.DELETE, aUrlWGuid, RoutingStatus.returnedFromCall()).
                    addPossibleStatus(RoutingStatus.returnValue(
                            200, String.format("Deleted a specific %s", thing.definition().getName()))).
                    addPossibleStatus(RoutingStatus.returnValue(
                            404, String.format("Could not find a specific %s", thing.definition().getName())));;

            defn.addRouting(
                    String.format("show all Options for endpoint of %s", aUrlWGuid),
                    RoutingVerb.OPTIONS, aUrlWGuid, RoutingStatus.returnValue(200),
                    new ResponseHeader("Allow", "OPTIONS, GET, HEAD, POST, PUT, DELETE"));

            defn.addRouting("method not allowed", RoutingVerb.PATCH, aUrlWGuid, RoutingStatus.returnValue(405));


            for (RelationshipVector rel : thing.definition().related().getRelationships()) {
                addRoutingsForRelationship(defn, rel);
            }
        }

        return defn;
    }

    private void addRoutingsForRelationship(final ApiRoutingDefinition defn, final RelationshipVector relationship) {

        String fromName = relationship.getFrom().definition().getName();
        String toName = relationship.getTo().definition().getName();
        String relationshipName = relationship.getName();


        final Thing thing = relationship.getFrom();
        String uniqueIdentifier;
        String uniqueIdFieldName;
        // todo: should mark a field as being used as identifiers for a thing
        final List<Field> idFields = thing.definition().getFieldsOfType(FieldType.ID);
        if(config.willUrlsShowIdsIfAvailable() && !idFields.isEmpty()){
            uniqueIdentifier=uniqueID;
            uniqueIdFieldName = idFields.get(0).getName();
        }else{
            uniqueIdentifier=uniqueGUID;
            uniqueIdFieldName="guid";
        }

        String fromNameForUrl;
        if(config.willUrlShowInstancesAsPlural()) {
            fromNameForUrl = thing.definition().getPlural().toLowerCase();
        }else {
            fromNameForUrl = thing.definition().getName().toLowerCase();
        }


        String aUrl = fromNameForUrl + "/" + uniqueIdentifier + "/" + relationshipName;
        defn.addRouting(
                String.format("return all the %s items related to %s, with given %s, by the relationship named %s",
                        toName, fromName, uniqueIdFieldName, relationshipName),
                RoutingVerb.GET, aUrl, RoutingStatus.returnedFromCall()).
                addPossibleStatus(RoutingStatus.returnValue(
                200, String.format("all the related the %s items", toName)));

        defn.addRouting(String.format("headers for the %s items related to %s, with given %s, by the relationship named %s",
                toName, fromName, uniqueIdFieldName, relationshipName),
                RoutingVerb.HEAD, aUrl, RoutingStatus.returnedFromCall()).
                addPossibleStatus(RoutingStatus.returnValue(
                200, String.format("headers for all the related the %s items", toName)));


        defn.addRouting(
                String.format("show all Options for endpoint of %s", aUrl),
                RoutingVerb.OPTIONS, aUrl, RoutingStatus.returnValue(200),
                new ResponseHeader("Allow", "OPTIONS, GET, HEAD, POST"));

        // we can post if there is no guid as it will create the 'thing' and the relationship connection
        defn.addRouting(
                String.format("create an instance of a relationship named %s between %s instance %s and the %s instance represented by the %s in the body of the message",
                        relationshipName, fromName, uniqueIdentifier, toName, uniqueIdFieldName),
                RoutingVerb.POST, aUrl, RoutingStatus.returnedFromCall()).
                addPossibleStatus(RoutingStatus.returnValue(
                        201, String.format("created the relationship"))).
                addPossibleStatus(RoutingStatus.returnValue(
                        400, String.format("error when creating the relationship")));

        defn.addRouting("method not allowed", RoutingVerb.DELETE, aUrl, RoutingStatus.returnValue(405));
        defn.addRouting("method not allowed", RoutingVerb.PATCH, aUrl, RoutingStatus.returnValue(405));
        defn.addRouting("method not allowed", RoutingVerb.PUT, aUrl, RoutingStatus.returnValue(405));


        // we should be able to delete a relationship
        final String aUrlDelete = fromNameForUrl + "/" + uniqueIdentifier +"/" + relationshipName + "/" + uniqueIdentifier;
        defn.addRouting(
                String.format("delete the instance of the relationship named %s between %s and %s using the %s", relationshipName,  fromName, toName, uniqueIdentifier),
                RoutingVerb.DELETE, aUrlDelete, RoutingStatus.returnedFromCall()).
                addPossibleStatus(RoutingStatus.returnValue(
                        200, String.format("deleted the relationship"))).
                addPossibleStatus(RoutingStatus.returnValue(
                        400, String.format("error when deleting the relationship"))).
                addPossibleStatus(RoutingStatus.returnValue(
                        404, String.format("relationship not found")));

        defn.addRouting(
                String.format("show all Options for endpoint of %s", aUrlDelete),
                RoutingVerb.OPTIONS, aUrlDelete, RoutingStatus.returnValue(200),
                new ResponseHeader("Allow", "OPTIONS, DELETE"));


        defn.addRouting("method not allowed", RoutingVerb.HEAD, aUrlDelete, RoutingStatus.returnValue(405));
        defn.addRouting("method not allowed", RoutingVerb.GET, aUrlDelete, RoutingStatus.returnValue(405));
        defn.addRouting("method not allowed", RoutingVerb.PATCH, aUrlDelete, RoutingStatus.returnValue(405));
        defn.addRouting("method not allowed", RoutingVerb.PUT, aUrlDelete, RoutingStatus.returnValue(405));
        defn.addRouting("method not allowed", RoutingVerb.POST, aUrlDelete, RoutingStatus.returnValue(405));
    }
}
