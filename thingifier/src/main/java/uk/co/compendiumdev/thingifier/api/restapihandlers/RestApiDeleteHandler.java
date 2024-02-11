package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.SimpleQuery;

import java.util.List;
import java.util.Map;

public class RestApiDeleteHandler {
    private final Thingifier thingifier;

    public RestApiDeleteHandler(final Thingifier aThingifier) {
        this.thingifier = aThingifier;
    }

    public ApiResponse handle(final String url, HttpHeadersBlock requestHeaders) {

        String instanceDatabaseName = SessionHeaderParser.getDatabaseNameFromHeaderValue(requestHeaders);

        // this should probably not delete root items
        EntityInstanceCollection thing = thingifier.getInstancesForSingularOrPluralNamedEntity(url, instanceDatabaseName);
        if (thing != null) {
            // can't delete root level with a DELETE
            return ApiResponse.error(405, "Cannot delete root level entity");
        }

        SimpleQuery queryResult = new SimpleQuery(thingifier.getERmodel().getSchema(), thingifier.getERmodel().getInstanceData(instanceDatabaseName), url).performQuery();

        if (queryResult.wasItemFoundUnderARelationship()) {
            // delete the relationships not the items
            EntityInstance parent = queryResult.getParentInstance();
            EntityInstance child = queryResult.getLastInstance();
            // todo: this returns a list of 'items' to be removed based on
            // removal of the relationships, these should really be deleted from thingifier now
            parent.getRelationships().removeRelationshipsInvolving(child, queryResult.getLastRelationshipName());
        } else {
            List<EntityInstance> items = queryResult.getListEntityInstances();
            if (items.isEmpty()) {
                // 404 not found - nothing to delete
                return ApiResponse.error404(String.format("Could not find any instances with %s", url));
            }
            for (EntityInstance instance : items) {
                thingifier.deleteThing(instance, instanceDatabaseName);
            }

        }
        return ApiResponse.success();
    }
}
