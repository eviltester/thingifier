package uk.co.compendiumdev.thingifier.api;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipDefinition;

import static spark.Spark.*;
import static spark.Spark.post;

public class ApiRoutingDefinitionGenerator {


    private final Thingifier thingifier;

    public ApiRoutingDefinitionGenerator(Thingifier thingifier){
        this.thingifier = thingifier;
    }

    // TODO: use this to create basic documentation
    public ApiRoutingDefinition generate() {
        ApiRoutingDefinition defn = new ApiRoutingDefinition();

        for(Thing thing : thingifier.getThings()){

            String url = thing.definition().getName().toLowerCase();
            // we should be able to get things e.g. GET project
            defn.addRouting(RoutingVerb.GET, url, RoutingStatus.returnedFromCall());

            // we should be able to create things without a GUID e.g. POST project
            defn.addRouting(RoutingVerb.POST, url, RoutingStatus.returnedFromCall());

            defn.addRouting(RoutingVerb.OPTIONS, url, RoutingStatus.returnValue(200),
                    new ResponseHeader("Allow", "OPTIONS, GET, POST"));

            // the following are not valid so return 405
            defn.addRouting(RoutingVerb.HEAD, url, RoutingStatus.returnValue(405));
            defn.addRouting(RoutingVerb.DELETE, url, RoutingStatus.returnValue(405));
            defn.addRouting(RoutingVerb.PATCH, url, RoutingStatus.returnValue(405));
            defn.addRouting(RoutingVerb.PUT, url, RoutingStatus.returnValue(405));

            String aUrlWGuid = thing.definition().getName().toLowerCase()  + "/:guid";
            // we should be able to get specific things based on the GUID e.g. GET project/GUID
            defn.addRouting(RoutingVerb.GET, aUrlWGuid, RoutingStatus.returnedFromCall());

            // we should be able to amend things with a GUID e.g. POST project/GUID with body
            defn.addRouting(RoutingVerb.POST, aUrlWGuid, RoutingStatus.returnedFromCall());

            // we should be able to amend things with PUT and a GUID e.g. PUT project/GUID
            defn.addRouting(RoutingVerb.PUT, aUrlWGuid, RoutingStatus.returnedFromCall());

            // we should be able to delete specific things e.g. DELETE project/GUID
            defn.addRouting(RoutingVerb.DELETE, aUrlWGuid, RoutingStatus.returnedFromCall());

            defn.addRouting(RoutingVerb.OPTIONS, aUrlWGuid, RoutingStatus.returnValue(200),
                    new ResponseHeader("Allow", "OPTIONS, GET, POST, PUT, DELETE"));


            defn.addRouting(RoutingVerb.HEAD, aUrlWGuid, RoutingStatus.returnValue(405));
            defn.addRouting(RoutingVerb.PATCH, aUrlWGuid, RoutingStatus.returnValue(405));

        }

        for(RelationshipDefinition relationship : thingifier.getRelationshipDefinitions()){
            // get all things for a relationship

            String aUrl = relationship.from().definition().getName() + "/:guid/" + relationship.getName();
            defn.addRouting(RoutingVerb.GET, aUrl, RoutingStatus.returnedFromCall());

            defn.addRouting(RoutingVerb.OPTIONS, aUrl, RoutingStatus.returnValue(200),
                    new ResponseHeader("Allow", "OPTIONS, GET"));

            // we can post if there is no guid as it will create the 'thing' and the relationship connection
            defn.addRouting(RoutingVerb.POST, aUrl, RoutingStatus.returnedFromCall());

            defn.addRouting(RoutingVerb.HEAD, aUrl, RoutingStatus.returnValue(405));
            defn.addRouting(RoutingVerb.DELETE, aUrl, RoutingStatus.returnValue(405));
            defn.addRouting(RoutingVerb.PATCH, aUrl, RoutingStatus.returnValue(405));
            defn.addRouting(RoutingVerb.PUT, aUrl, RoutingStatus.returnValue(405));


            // we should be able to delete a relationship
            final String aUrlDelete = relationship.from().definition().getName() + "/:guid/" + relationship.getName() + "/:relatedguid";
            defn.addRouting(RoutingVerb.DELETE, aUrlDelete, RoutingStatus.returnedFromCall());

            defn.addRouting(RoutingVerb.OPTIONS, aUrlDelete, RoutingStatus.returnValue(200),
                    new ResponseHeader("Allow", "OPTIONS, DELETE"));


            defn.addRouting(RoutingVerb.HEAD, aUrlDelete, RoutingStatus.returnValue(405));
            defn.addRouting(RoutingVerb.GET, aUrlDelete, RoutingStatus.returnValue(405));
            defn.addRouting(RoutingVerb.PATCH, aUrlDelete, RoutingStatus.returnValue(405));
            defn.addRouting(RoutingVerb.PUT, aUrlDelete, RoutingStatus.returnValue(405));
            defn.addRouting(RoutingVerb.POST, aUrlDelete, RoutingStatus.returnValue(405));

        }

        return defn;
    }
}
