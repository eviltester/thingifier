package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;
import uk.co.compendiumdev.thingifier.application.ThingCommandService;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public class RestApiDeleteHandler {
    private final Thingifier thingifier;

    public RestApiDeleteHandler(final Thingifier aThingifier) {
        this.thingifier = aThingifier;
    }

    public ApiResponse handle(final String url, HttpHeadersBlock requestHeaders) {

        String instanceDatabaseName =
                SessionHeaderParser.getDatabaseNameFromHeaderValue(requestHeaders);

        // this should probably not delete root items
        EntityDefinition thing = EntityUrlMatcher.entityFromCollectionUrl(thingifier, url);
        if (thing != null) {
            // can't delete root level with a DELETE
            return ApiResponse.error(405, "Cannot delete root level entity");
        }

        EntityDefinition entity = EntityUrlMatcher.entityFromInstanceUrl(thingifier, url);
        if (entity != null) {
            EntityInstance instance =
                    EntityUrlMatcher.findInstanceFromUrl(thingifier, url, instanceDatabaseName);
            if (instance == null) {
                return ApiResponse.error404(
                        String.format("Could not find any instances with %s", url));
            }
            return responseFrom(
                    new ThingCommandService(thingifier.getStore(instanceDatabaseName))
                            .delete(instance));
        }

        RepositoryBackedRelationshipUrlResolver.RelationshipUrlResolution relationship =
                new RepositoryBackedRelationshipUrlResolver(thingifier, instanceDatabaseName)
                        .resolveRelationshipInstance(url);
        if (relationship.matchedRelationshipPath()) {
            if (!relationship.relationshipInstancePath()
                    || relationship.parentInstance() == null
                    || relationship.childInstance() == null) {
                return ApiResponse.error404(
                        String.format("Could not find any instances with %s", url));
            }
            return responseFrom(
                    new ThingCommandService(thingifier.getStore(instanceDatabaseName))
                            .disconnectRelationship(
                                    relationship.parentInstance(),
                                    relationship.childInstance(),
                                    relationship.relationshipName()));
        }

        return ApiResponse.error404(String.format("Could not find any instances with %s", url));
    }

    private ApiResponse responseFrom(final ThingCommandResult result) {
        if (result.isError()) {
            return ApiResponse.error(400, result.getErrorMessages());
        }
        return ApiResponse.success();
    }
}
