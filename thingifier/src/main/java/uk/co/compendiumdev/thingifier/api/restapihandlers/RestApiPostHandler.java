package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.restapihandlers.commonerrorresponse.NoSuchEntity;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.SimpleQuery;

import java.util.List;

public class RestApiPostHandler {
    private final Thingifier thingifier;

    public RestApiPostHandler(final Thingifier aThingifier) {
        thingifier = aThingifier;
    }

    public ApiResponse handle(final String url, final BodyParser args, final HttpHeadersBlock requestHeaders) {
        // we want to

        String instanceDatabaseName = SessionHeaderParser.getDatabaseNameFromHeaderValue(requestHeaders);

        /*
            No GUID and match a Thing
         */
        // if queryis empty then need a way to check if the query matched
        // create a thing
        EntityInstanceCollection instancesCollection = thingifier.getInstancesForSingularOrPluralNamedEntity(url, instanceDatabaseName);
        if (instancesCollection != null) {
            // create a new thing does not enforce relationships
            // TODO: validate before creation so as to only delete in an 'emergency' not as default
            final ApiResponse response = new ThingCreation(thingifier).with(args, instancesCollection, instanceDatabaseName);
            if (response.isErrorResponse()) {
                return response;
            }

            EntityInstance returnedInstance = response.getReturnedInstance();
            final List<String> protectedFieldNames = returnedInstance.getEntity().getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
            ValidationReport validity = returnedInstance.validateFieldValues(protectedFieldNames, false);
            validity.combine(returnedInstance.validateRelationships());

            if (validity.isValid()) {
                return response;
            } else {
                instancesCollection.deleteInstance(response.getReturnedInstance());
                return ApiResponse.error(400, validity.getErrorMessages()).addToErrorMessages("No new item created");
            }
        }


        /*
            Match a specific thing
         */
        // amend  a thing
        // thing/guid
        String[] urlParts = url.split("/");
        if (urlParts.length == 2) {

            String thingName = urlParts[0];
            instancesCollection = thingifier.getInstancesForSingularOrPluralNamedEntity(thingName, instanceDatabaseName);

            if (instancesCollection == null) {
                // this is not a URL for thing/guid
                // unknown thing
                return NoSuchEntity.response(thingName);

            }

            String primaryKey = urlParts[1];

            if(instancesCollection.definition().hasPrimaryKeyField()){
                EntityInstance instance = instancesCollection.findInstanceByPrimaryKey(primaryKey);

                if (instance == null) {
                    // cannot amend something that does not exist
                    return ApiResponse.error404(String.format("No such %s entity instance with %s == %s found",
                            instancesCollection.definition().getName(),
                            instancesCollection.definition().getPrimaryKeyField().getName(),
                            primaryKey)
                    );
                }

                return amendAThingWithPost(args, instance, instanceDatabaseName);
            }else{
                return ApiResponse.error404(String.format("Entity %s does not have a primary key defined", instancesCollection.definition().getName()));
            }

        }


        /*
            Match a Relationship
         */
        // get the things to post to
        SimpleQuery query = new SimpleQuery(
                                    thingifier.getERmodel().getSchema(),
                                    thingifier.getERmodel().getInstanceData(instanceDatabaseName), url).performQuery();
        if (query.lastMatchWasRelationship()) {
            return new RelationshipCreation(thingifier).create(url, args, query, instanceDatabaseName);
        }

        // WHAT was that query?
        return ApiResponse.error(400, "Your request was not understood");

    }


    private ApiResponse amendAThingWithPost(BodyParser args, EntityInstance instance, final String database) {
        // with a post we do not want to clear fields before setting - we only amend what we pass in
        return new ThingAmendment(thingifier).amendInstance(args, instance, false, database);
    }


}
