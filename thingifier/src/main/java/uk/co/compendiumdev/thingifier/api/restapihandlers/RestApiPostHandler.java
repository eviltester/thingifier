package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.restapihandlers.commonerrorresponse.NoSuchEntity;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public class RestApiPostHandler {
    private final Thingifier thingifier;

    public RestApiPostHandler(final Thingifier aThingifier) {
        thingifier = aThingifier;
    }

    public ApiResponse handle(
            final String url, final BodyParser args, final HttpHeadersBlock requestHeaders) {
        // we want to

        String instanceDatabaseName =
                SessionHeaderParser.getDatabaseNameFromHeaderValue(requestHeaders);

        /*
           No GUID and match a Thing
        */
        // if queryis empty then need a way to check if the query matched
        // create a thing
        EntityDefinition entityDefinition =
                EntityUrlMatcher.entityFromCollectionUrl(thingifier, url);
        if (entityDefinition != null) {
            return new ThingCreation(thingifier).with(args, entityDefinition, instanceDatabaseName);
        }

        /*
           Match a specific thing
        */
        // amend  a thing
        // thing/guid
        entityDefinition = EntityUrlMatcher.entityFromInstanceUrl(thingifier, url);
        if (entityDefinition != null) {

            String primaryKey = EntityUrlMatcher.identifierFromInstanceUrl(url);

            if (entityDefinition.hasPrimaryKeyField()) {
                EntityInstance instance =
                        thingifier
                                .getStore(instanceDatabaseName)
                                .entityQueries()
                                .findByQueryIdentifier(entityDefinition, primaryKey);

                if (instance == null) {
                    // cannot amend something that does not exist
                    return ApiResponse.error404(
                            String.format(
                                    "No such %s entity instance with %s == %s found",
                                    entityDefinition.getName(),
                                    entityDefinition.getPrimaryKeyField().getName(),
                                    primaryKey));
                }

                return amendAThingWithPost(args, instance, instanceDatabaseName);
            } else {
                return ApiResponse.error404(
                        String.format(
                                "Entity %s does not have a primary key defined",
                                entityDefinition.getName()));
            }
        }

        String thingName = EntityUrlMatcher.entityTermFromUrl(url);
        if (!thingName.isEmpty() && EntityUrlMatcher.hasPartCount(url, 2)) {
            return NoSuchEntity.response(thingName);
        }

        /*
           Match a Relationship
        */
        RepositoryBackedRelationshipUrlResolver.RelationshipUrlResolution relationship =
                new RepositoryBackedRelationshipUrlResolver(thingifier, instanceDatabaseName)
                        .resolveCollection(url);
        if (relationship.matchedRelationshipPath()) {
            return new RelationshipCreation(thingifier)
                    .create(url, args, relationship, instanceDatabaseName);
        }

        // WHAT was that query?
        return ApiResponse.error(400, "Your request was not understood");
    }

    private ApiResponse amendAThingWithPost(
            BodyParser args, EntityInstance instance, final String database) {
        // with a post we do not want to clear fields before setting - we only amend what we pass in
        return new ThingAmendment(thingifier).amendInstance(args, instance, false, database);
    }
}
