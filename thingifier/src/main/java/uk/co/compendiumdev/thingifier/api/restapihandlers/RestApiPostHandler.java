package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.restapihandlers.commonerrorresponse.NoSuchEntity;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.validation.EntityInstanceStateValidator;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public class RestApiPostHandler {
    private final Thingifier thingifier;
    private final EntityInstanceStateValidator stateValidator;

    public RestApiPostHandler(final Thingifier aThingifier) {
        thingifier = aThingifier;
        stateValidator = new EntityInstanceStateValidator();
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
            // Creation stores field state first; relationship invariants are repository-validated
            // before the response is accepted.
            final ApiResponse response =
                    new ThingCreation(thingifier)
                            .with(args, entityDefinition, instanceDatabaseName);
            if (response.isErrorResponse()) {
                return response;
            }

            EntityInstance returnedInstance = response.getReturnedInstance();
            final List<String> protectedFieldNames =
                    returnedInstance
                            .getEntity()
                            .getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
            ValidationReport validity =
                    stateValidator.validateFields(returnedInstance, protectedFieldNames, false);
            validity.combine(
                    thingifier
                            .getRepository(instanceDatabaseName)
                            .validateRelationships(returnedInstance));

            if (validity.isValid()) {
                return response;
            } else {
                thingifier.deleteThing(response.getReturnedInstance(), instanceDatabaseName);
                return ApiResponse.error(400, validity.getErrorMessages())
                        .addToErrorMessages("No new item created");
            }
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
                                .getRepository(instanceDatabaseName)
                                .findInstanceByQueryIdentifier(entityDefinition, primaryKey);

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
