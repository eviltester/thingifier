package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.SimpleQuery;

import java.util.List;

public class RestApiDeleteHandler {
    private final Thingifier thingifier;

    public RestApiDeleteHandler(final Thingifier aThingifier) {
        this.thingifier = aThingifier;
    }

    public ApiResponse handle(final String url) {
        // this should probably not delete root items
        EntityInstanceCollection thing = thingifier.getInstancesForSingularOrPluralNamedEntity(url);
        if (thing != null) {
            // can't delete root level with a DELETE
            return ApiResponse.error(405, "Cannot delete root level entity");
        }

        SimpleQuery queryresult = new SimpleQuery(thingifier.getERmodel(), url).performQuery();

        if (queryresult.wasItemFoundUnderARelationship()) {
            // delete the relationships not the items
            EntityInstance parent = queryresult.getParentInstance();
            EntityInstance child = queryresult.getLastInstance();
            // todo: this returns a list of 'items' to be removed based on
            // removal of the relationships, these should really be deleted from thingifier now
            parent.getRelationships().removeRelationshipsInvolving(child, queryresult.getLastRelationshipName());
        } else {
            List<EntityInstance> items = queryresult.getListEntityInstances();
            if (items.size() == 0) {
                // 404 not found - nothing to delete
                return ApiResponse.error404(String.format("Could not find any instances with %s", url));
            }
            for (EntityInstance instance : items) {
                thingifier.deleteThing(instance);
            }

        }
        return ApiResponse.success();
    }
}
