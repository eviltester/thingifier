package uk.co.compendiumdev.thingifier.api.routings;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ResponseHeader;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipVector;

public class ApiRoutingDefinitionGenerator {


    private final Thingifier thingifier;

    public ApiRoutingDefinitionGenerator(final Thingifier thingifier) {
        this.thingifier = thingifier;
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

            // TODO: make this configurable between plural and single
            String thingName;
            thingName = thing.definition().getPlural().toLowerCase();
            //thingName = thing.definition().getName().toLowerCase();

            String url = thingName;

            // we should be able to get things e.g. GET project
            defn.addRouting(
                    String.format("return all the instances of %s", thing.definition().getName()),
                    RoutingVerb.GET, url, RoutingStatus.returnedFromCall());

            // we should be able to create things without a GUID e.g. POST project
            defn.addRouting(
                    String.format("we should be able to create %s without a GUID using the field values in the body of the message", thing.definition().getName()),
                    RoutingVerb.POST, url, RoutingStatus.returnedFromCall());

            defn.addRouting(
                    String.format("show all Options for endpoint of %s", url),
                    RoutingVerb.OPTIONS, url, RoutingStatus.returnValue(200),
                    new ResponseHeader("Allow", "OPTIONS, GET, POST"));

            // the following are not valid so return 405
            defn.addRouting("method not allowed",
                    RoutingVerb.HEAD, url, RoutingStatus.returnValue(405));
            defn.addRouting("method not allowed",
                    RoutingVerb.DELETE, url, RoutingStatus.returnValue(405));
            defn.addRouting("method not allowed",
                    RoutingVerb.PATCH, url, RoutingStatus.returnValue(405));
            defn.addRouting("method not allowed",
                    RoutingVerb.PUT, url, RoutingStatus.returnValue(405));

            String aUrlWGuid = url + "/:guid";
            // we should be able to get specific things based on the GUID e.g. GET project/GUID
            defn.addRouting(
                    String.format("return a specific instances of %s using a guid", thing.definition().getName()),
                    RoutingVerb.GET, aUrlWGuid, RoutingStatus.returnedFromCall());

            // we should be able to amend things with a GUID e.g. POST project/GUID with body
            defn.addRouting(
                    String.format("amend a specific instances of %s using a guid with a body containing the fields to amend", thing.definition().getName()),
                    RoutingVerb.POST, aUrlWGuid, RoutingStatus.returnedFromCall());

            // we should be able to amend things with PUT and a GUID e.g. PUT project/GUID
            defn.addRouting(
                    String.format("amend a specific instances of %1$s using a guid with a body containing the fields to amend, if the guid does not exist then a %1$s will be created with that GUID", thing.definition().getName()),
                    RoutingVerb.PUT, aUrlWGuid, RoutingStatus.returnedFromCall());

            // we should be able to delete specific things e.g. DELETE project/GUID
            defn.addRouting(
                    String.format("delete a specific instances of %s using a guid", thing.definition().getName()),
                    RoutingVerb.DELETE, aUrlWGuid, RoutingStatus.returnedFromCall());

            defn.addRouting(
                    String.format("show all Options for endpoint of %s", aUrlWGuid),
                    RoutingVerb.OPTIONS, aUrlWGuid, RoutingStatus.returnValue(200),
                    new ResponseHeader("Allow", "OPTIONS, GET, POST, PUT, DELETE"));


            defn.addRouting("method not allowed", RoutingVerb.HEAD, aUrlWGuid, RoutingStatus.returnValue(405));
            defn.addRouting("method not allowed", RoutingVerb.PATCH, aUrlWGuid, RoutingStatus.returnValue(405));


            for (RelationshipVector rel : thing.definition().getRelationships()) {
                addRoutingsForRelationship(defn, rel);
            }
        }

        return defn;
    }

    private void addRoutingsForRelationship(final ApiRoutingDefinition defn, final RelationshipVector relationship) {

        String fromName = relationship.getFrom().definition().getName();
        String toName = relationship.getTo().definition().getName();
        String relationshipName = relationship.getName();

        // TODO: make this configurable between plural and single
        String fromNameForUrl;
        //fromNameForUrl = relationship.getFrom().definition().getName();
        fromNameForUrl = relationship.getFrom().definition().getPlural();

        String aUrl = fromNameForUrl + "/:guid/" + relationshipName;
        defn.addRouting(
                String.format("return all the %s items related to %s :guid by the relationship named %s", toName, fromName, relationshipName),
                RoutingVerb.GET, aUrl, RoutingStatus.returnedFromCall());

        defn.addRouting(
                String.format("show all Options for endpoint of %s", aUrl),
                RoutingVerb.OPTIONS, aUrl, RoutingStatus.returnValue(200),
                new ResponseHeader("Allow", "OPTIONS, GET"));

        // we can post if there is no guid as it will create the 'thing' and the relationship connection
        defn.addRouting(
                String.format("create an instance of a relationship named %s between %s instance :guid and the %s instance represented by the guid in the body of the message",
                        relationshipName, fromName, toName),
                RoutingVerb.POST, aUrl, RoutingStatus.returnedFromCall());

        defn.addRouting("method not allowed", RoutingVerb.HEAD, aUrl, RoutingStatus.returnValue(405));
        defn.addRouting("method not allowed", RoutingVerb.DELETE, aUrl, RoutingStatus.returnValue(405));
        defn.addRouting("method not allowed", RoutingVerb.PATCH, aUrl, RoutingStatus.returnValue(405));
        defn.addRouting("method not allowed", RoutingVerb.PUT, aUrl, RoutingStatus.returnValue(405));


        // we should be able to delete a relationship
        final String aUrlDelete = fromNameForUrl + "/:guid/" + relationshipName + "/:relatedguid";
        defn.addRouting(
                String.format("delete the instance of the relationship between %s :guid and %s :relatedguid named %s ", fromName, toName, relationshipName),
                RoutingVerb.DELETE, aUrlDelete, RoutingStatus.returnedFromCall());

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
