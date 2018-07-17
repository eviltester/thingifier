package uk.co.compendiumdev.thingifier.api;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipDefinition;

public class ApiRoutingDefinitionGenerator {


    private final Thingifier thingifier;

    public ApiRoutingDefinitionGenerator(Thingifier thingifier){
        this.thingifier = thingifier;
    }

    // TODO: have the ability to override these from config and define from config rather than code
    public ApiRoutingDefinition generate() {
        ApiRoutingDefinition defn = new ApiRoutingDefinition();

        for(Thing thing : thingifier.getThings()){

            String url = thing.definition().getName().toLowerCase();
            // we should be able to get things e.g. GET project
            defn.addRouting(
                    String.format("return all the instances of %s", thing.definition().getName())
                            , RoutingVerb.GET, url, RoutingStatus.returnedFromCall());

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

            String aUrlWGuid = thing.definition().getName().toLowerCase()  + "/:guid";
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


            // TODO: should really output thing relationship here to make documentation clearer

        }

        for(RelationshipDefinition relationship : thingifier.getRelationshipDefinitions()){
            // get all things for a relationship

            addRoutingsForRelationship(defn, relationship);

            // TODO: the fact that I am creating a 'reversed' relationship to make this easier suggests to me that this might be a concept I need in the main app
            if(relationship.isTwoWay()) {
                addRoutingsForRelationship(defn, relationship.getReversedRelationship());
            }

        }

        return defn;
    }

    private void addRoutingsForRelationship(ApiRoutingDefinition defn, RelationshipDefinition relationship) {
        String aUrl = relationship.from().definition().getName() + "/:guid/" + relationship.getName();
        defn.addRouting(
                String.format("return all the %s items related to %s :guid by the relationship named %s", relationship.to().getName(), relationship.from().definition().getName(), relationship.getName()),
                RoutingVerb.GET, aUrl, RoutingStatus.returnedFromCall());

        defn.addRouting(
                String.format("show all Options for endpoint of %s", aUrl),
                RoutingVerb.OPTIONS, aUrl, RoutingStatus.returnValue(200),
                new ResponseHeader("Allow", "OPTIONS, GET"));

        // we can post if there is no guid as it will create the 'thing' and the relationship connection
        defn.addRouting(String.format("create an instance of a relationship named %s between %s instance :guid and the %s instance represented by the guid in the body of the message"
                                        , relationship.getName(), relationship.from().definition().getName(), relationship.to().getName()),
                RoutingVerb.POST, aUrl, RoutingStatus.returnedFromCall());

        defn.addRouting("method not allowed", RoutingVerb.HEAD, aUrl, RoutingStatus.returnValue(405));
        defn.addRouting("method not allowed", RoutingVerb.DELETE, aUrl, RoutingStatus.returnValue(405));
        defn.addRouting("method not allowed", RoutingVerb.PATCH, aUrl, RoutingStatus.returnValue(405));
        defn.addRouting("method not allowed", RoutingVerb.PUT, aUrl, RoutingStatus.returnValue(405));


        // we should be able to delete a relationship
        final String aUrlDelete = relationship.from().definition().getName() + "/:guid/" + relationship.getName() + "/:relatedguid";
        defn.addRouting(
                String.format("delete the instance of the relationship between %s :guid and %s :relatedguid named %s ", relationship.from().definition().getName(), relationship.to().getName(), relationship.getName()),
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
