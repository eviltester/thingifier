package uk.co.compendiumdev.thingifier.api.docgen;

import java.util.HashMap;
import java.util.Map;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ResponseHeader;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;

public class ApiRoutingDefinitionDocGenerator {

    private final Thingifier thingifier;
    private final ThingifierApiConfig config;

    private final String uniqueGUID = ":guid";
    private final Map<FieldType, String> uniqueReferenceText;

    public ApiRoutingDefinitionDocGenerator(final Thingifier thingifier) {
        this.thingifier = thingifier;
        this.config = thingifier.apiConfig();

        uniqueReferenceText = new HashMap<>();
        uniqueReferenceText.put(FieldType.AUTO_INCREMENT, ":id");
        uniqueReferenceText.put(FieldType.AUTO_GUID, ":guid");
    }

    // TODO: generate /_plural_ instead of /_entityName_ for top level routing (make this toggelable
    // from command line (to inject buggyness) but make plural the default
    // - plural should always return a collection object regardless if it is a single instance or
    // not
    // - single should return a single if instance and collection if multiple
    // TODO: have a toggle to allow for /_entityName  - this should only be valid if
    // /_entityName/_guid_ and will return a single object
    // TODO: create an /instance/_entityName_/_guid_ that provides a report with all relationships
    // listed as objects e.g.
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
    public ApiRoutingDefinition generate(String apiPathPrefix) {
        ApiRoutingDefinition defn = new ApiRoutingDefinition();

        String endPointPrefix = "";
        if (apiPathPrefix != null && !apiPathPrefix.isEmpty()) {
            endPointPrefix = apiPathPrefix + "/";
        }

        for (EntityDefinition entityDefn : thingifier.getERmodel().getEntityDefinitions()) {

            // add for the single entity
            defn.addObjectSchema(entityDefn);

            String uniqueIdentifier = "?";
            String uniqueIdFieldName = "fieldName";

            // a thing can have many id fields, so should choose one to be in the url
            // e.g. set a field as 'usedForIndividualRouting'
            Field uniqueIdField = getUniqueIdField(entityDefn);
            if (uniqueIdField != null) {
                uniqueIdentifier = uniqueReferenceText.get(uniqueIdField.getType());
                uniqueIdFieldName = uniqueIdField.getName();
            }

            //            final List<Field> idFields = entityDefn.getFieldsOfType(FieldType.ID);
            //            if(config.willUrlsShowIdsIfAvailable() && !idFields.isEmpty()){
            //                uniqueIdentifier=uniqueID;
            //                uniqueIdFieldName = idFields.get(0).getName();
            //            }else{
            //                final List<Field> guidFields =
            // entityDefn.getFieldsOfType(FieldType.GUID);
            //                uniqueIdentifier=uniqueGUID;
            //                uniqueIdFieldName=guidFields.get(0).getName();
            //            }

            String pluralUrl;
            if (config.willUrlShowInstancesAsPlural()) {
                pluralUrl = endPointPrefix + entityDefn.getPlural().toLowerCase();
            } else {
                pluralUrl = endPointPrefix + entityDefn.getName().toLowerCase();
            }

            // we should be able to get things e.g. GET project
            defn.addRouting(
                            String.format("return all the instances of %s", entityDefn.getName()),
                            RoutingVerb.GET,
                            pluralUrl,
                            RoutingStatus.returnedFromCall())
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    200, String.format("All the %s", entityDefn.getPlural())))
                    .setAsFilterableFrom(entityDefn)
                    .returnPayload(200, entityDefn.getPlural());

            defn.addRouting(
                            String.format(
                                    "query all the instances of %s using application/x-www-form-urlencoded query content",
                                    entityDefn.getName()),
                            RoutingVerb.QUERY,
                            pluralUrl,
                            RoutingStatus.returnedFromCall())
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    200,
                                    String.format("All the matching %s", entityDefn.getPlural())))
                    .addPossibleStatus(RoutingStatus.returnValue(400))
                    .addPossibleStatus(RoutingStatus.returnValue(413))
                    .addPossibleStatus(RoutingStatus.returnValue(415))
                    .setAsFilterableFrom(entityDefn)
                    .returnPayload(200, entityDefn.getPlural());

            defn.addRouting(
                            String.format(
                                    "headers for all the instances of %s", entityDefn.getName()),
                            RoutingVerb.HEAD,
                            pluralUrl,
                            RoutingStatus.returnedFromCall())
                    .addPossibleStatus(RoutingStatus.returnValue(200));

            // TODO: more granularity if singleUrl!=pluralUrl then we need two paths here
            // e.g. GET projects, POST project - which also changes the Options
            // we should be able to create things without a GUID e.g. POST project
            String maxLimitInformation = "";
            if (entityDefn.hasMaxInstanceLimit()) {
                maxLimitInformation =
                        String.format(
                                "A maximum of %d %s is allowed.",
                                entityDefn.getMaxInstanceLimit(), entityDefn.getPlural());
            }
            defn.addRouting(
                            String.format(
                                    "we should be able to create %s without a %s using the field values in the body of the message. "
                                            + maxLimitInformation,
                                    entityDefn.getName(),
                                    uniqueIdFieldName.toUpperCase()),
                            RoutingVerb.POST,
                            pluralUrl,
                            RoutingStatus.returnedFromCall())
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    201, String.format("Created a %s", entityDefn.getName())))
                    .returnPayload(201, entityDefn.getName())
                    .requestPayload("create_" + entityDefn.getName())
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    400,
                                    String.format(
                                            "Error when creating a %s", entityDefn.getName())))
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    422,
                                    String.format(
                                            "Validation error when creating a %s",
                                            entityDefn.getName())))
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    409,
                                    String.format(
                                            "Conflict when creating a %s", entityDefn.getName())));

            // TODO: allow configurable 200 for options
            defn.addRouting(
                    String.format("show all Options for endpoint of %s", pluralUrl),
                    RoutingVerb.OPTIONS,
                    pluralUrl,
                    RoutingStatus.returnValue(204, "the endpoint verb options"),
                    new ResponseHeader("Allow", "OPTIONS, GET, HEAD, POST, QUERY"));

            // the following are not handled so return 405
            defn.addRouting(
                    "method not allowed",
                    RoutingVerb.DELETE,
                    pluralUrl,
                    RoutingStatus.returnValue(405));
            defn.addRouting(
                    "method not allowed",
                    RoutingVerb.PATCH,
                    pluralUrl,
                    RoutingStatus.returnValue(405));
            defn.addRouting(
                    "method not allowed",
                    RoutingVerb.PUT,
                    pluralUrl,
                    RoutingStatus.returnValue(405));
            defn.addRouting(
                    "method not allowed",
                    RoutingVerb.TRACE,
                    pluralUrl,
                    RoutingStatus.returnValue(405));

            String aUrlWGuid = pluralUrl + "/" + uniqueIdentifier;
            // we should be able to get specific things based on the GUID e.g. GET project/GUID
            defn.addRouting(
                            String.format(
                                    "return a specific instances of %s using a %s",
                                    entityDefn.getName(), uniqueIdFieldName),
                            RoutingVerb.GET,
                            aUrlWGuid,
                            RoutingStatus.returnedFromCall())
                    .addRequestUrlParam(entityDefn.getField(uniqueIdFieldName))
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    200, String.format("A specific %s", entityDefn.getName())))
                    .returnPayload(200, entityDefn.getName())
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    404,
                                    String.format(
                                            "Could not find a specific %s", entityDefn.getName())));

            defn.addRouting(
                            String.format(
                                    "headers for a specific instances of %s using a %s",
                                    entityDefn.getName(), uniqueIdFieldName),
                            RoutingVerb.HEAD,
                            aUrlWGuid,
                            RoutingStatus.returnedFromCall())
                    .addRequestUrlParam(entityDefn.getField(uniqueIdFieldName))
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    200,
                                    String.format(
                                            "Headers for a specific %s", entityDefn.getName())))
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    404,
                                    String.format(
                                            "Could not find a specific %s", entityDefn.getName())));
            ;

            defn.addRouting(
                            "method not allowed",
                            RoutingVerb.QUERY,
                            aUrlWGuid,
                            RoutingStatus.returnValue(405),
                            new ResponseHeader("Allow", "OPTIONS, GET, HEAD, POST, PUT, DELETE"))
                    .addRequestUrlParam(entityDefn.getField(uniqueIdFieldName));

            // we should be able to amend things with a GUID e.g. POST project/GUID with body
            defn.addRouting(
                            String.format(
                                    "amend a specific instances of %s using a %s with a body containing the fields to amend",
                                    entityDefn.getName(), uniqueIdFieldName),
                            RoutingVerb.POST,
                            aUrlWGuid,
                            RoutingStatus.returnedFromCall())
                    .addRequestUrlParam(entityDefn.getField(uniqueIdFieldName))
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    200,
                                    String.format("Amended the specific %s", entityDefn.getName())))
                    .returnPayload(200, entityDefn.getName())
                    .requestPayload(entityDefn.getName())
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    404,
                                    String.format(
                                            "Could not find a specific %s", entityDefn.getName())))
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    422,
                                    String.format(
                                            "Validation error when amending a %s",
                                            entityDefn.getName())))
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    409,
                                    String.format(
                                            "Conflict when amending a %s", entityDefn.getName())));
            ;

            // we should be able to amend things with PUT and a GUID e.g. PUT project/GUID
            String ifGUIDamendment = "";
            if (uniqueIdentifier.contentEquals(uniqueGUID)) {
                ifGUIDamendment =
                        String.format(
                                ", if the %2$s does not exist then a %1$s will be created with that %2$s",
                                entityDefn.getName(), uniqueIdFieldName);
            }
            defn.addRouting(
                            String.format(
                                            "amend a specific instances of %1$s using a %2$s with a body containing the fields to amend",
                                            entityDefn.getName(), uniqueIdFieldName)
                                    + ifGUIDamendment,
                            RoutingVerb.PUT,
                            aUrlWGuid,
                            RoutingStatus.returnedFromCall())
                    .addRequestUrlParam(entityDefn.getField(uniqueIdFieldName))
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    200,
                                    String.format(
                                            "Replaced the specific %s details",
                                            entityDefn.getName())))
                    .returnPayload(200, entityDefn.getName())
                    .requestPayload(entityDefn.getName())
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    404,
                                    String.format(
                                            "Could not find a specific %s", entityDefn.getName())))
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    422,
                                    String.format(
                                            "Validation error when replacing a %s",
                                            entityDefn.getName())))
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    409,
                                    String.format(
                                            "Conflict when replacing a %s", entityDefn.getName())));
            ;

            // we should be able to delete specific things e.g. DELETE project/GUID
            defn.addRouting(
                            String.format(
                                    "delete a specific instances of %s using a %s",
                                    entityDefn.getName(), uniqueIdFieldName),
                            RoutingVerb.DELETE,
                            aUrlWGuid,
                            RoutingStatus.returnedFromCall())
                    .addRequestUrlParam(entityDefn.getField(uniqueIdFieldName))
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    204,
                                    String.format("Deleted a specific %s", entityDefn.getName())))
                    .addPossibleStatus(
                            RoutingStatus.returnValue(
                                    404,
                                    String.format(
                                            "Could not find a specific %s", entityDefn.getName())));

            defn.addRouting(
                            String.format("show all Options for endpoint of %s", aUrlWGuid),
                            RoutingVerb.OPTIONS,
                            aUrlWGuid,
                            RoutingStatus.returnValue(204),
                            new ResponseHeader("Allow", "OPTIONS, GET, HEAD, POST, PUT, DELETE"))
                    .addRequestUrlParam(entityDefn.getField(uniqueIdFieldName));

            defn.addRouting(
                            "method not allowed",
                            RoutingVerb.PATCH,
                            aUrlWGuid,
                            RoutingStatus.returnValue(405))
                    .addRequestUrlParam(entityDefn.getField(uniqueIdFieldName));
            defn.addRouting(
                            "method not allowed",
                            RoutingVerb.TRACE,
                            aUrlWGuid,
                            RoutingStatus.returnValue(405))
                    .addRequestUrlParam(entityDefn.getField(uniqueIdFieldName));

            for (RelationshipVectorDefinition rel : entityDefn.related().getRelationships()) {
                addRoutingsForRelationship(defn, rel, endPointPrefix);
            }
        }

        new WriteMethodRoutePolicy(thingifier).applyTo(defn, apiPathPrefix);
        thingifier.apiSpec().applyTo(defn, apiPathPrefix);
        return defn;
    }

    private Field getUniqueIdField(final EntityDefinition thingDefn) {
        return thingDefn.getPrimaryKeyField();

        //        final List<Field> idFields = thingDefn.getFieldsOfType(FieldType.AUTO_INCREMENT);
        //        if(config.willUrlsShowIdsIfAvailable() && !idFields.isEmpty()){
        //            return idFields.get(0);
        //        }else{
        //            final List<Field> guidFields = thingDefn.getFieldsOfType(FieldType.AUTO_GUID);
        //            if(!guidFields.isEmpty()) {
        //                return guidFields.get(0);
        //            }else{
        //                return null;
        //            }
        //        }
    }

    private String relatedParameterNameFor(final Field field) {
        if (field == null) {
            return null;
        }
        String fieldName = field.getName();
        return "related" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    private void addRoutingsForRelationship(
            final ApiRoutingDefinition defn,
            final RelationshipVectorDefinition relationship,
            final String endPointPrefix) {

        String fromName = relationship.getFrom().getName();
        String toName = relationship.getTo().getName();
        String relationshipName = relationship.getName();
        boolean getReturnsSingle = relationshipReturnsSingleInstance(relationship);
        String relationshipGetTargetDescription = getReturnsSingle ? "item" : "items";
        String relationshipGetDocumentationVerb =
                getReturnsSingle ? "return the" : "return all the";

        final EntityDefinition thingDefn = relationship.getFrom();

        String uniqueIdentifier = "?";
        String uniqueIdFieldName = "fieldName";

        Field uniqueIdField = getUniqueIdField(thingDefn);
        if (uniqueIdField != null) {
            uniqueIdentifier = uniqueReferenceText.get(uniqueIdField.getType());
            uniqueIdFieldName = uniqueIdField.getName();
        }

        //        final List<Field> idFields = thingDefn.getFieldsOfType(FieldType.ID);
        //        if(config.willUrlsShowIdsIfAvailable() && !idFields.isEmpty()){
        //            uniqueIdentifier=uniqueID;
        //            uniqueIdFieldName = idFields.get(0).getName();
        //        }else{
        //            uniqueIdentifier=uniqueGUID;
        //            uniqueIdFieldName="guid";
        //        }

        String fromNameForUrl;
        if (config.willUrlShowInstancesAsPlural()) {
            fromNameForUrl = thingDefn.getPlural().toLowerCase();
        } else {
            fromNameForUrl = thingDefn.getName().toLowerCase();
        }

        String aUrl =
                endPointPrefix + fromNameForUrl + "/" + uniqueIdentifier + "/" + relationshipName;
        defn.addRouting(
                        String.format(
                                "%s %s %s related to %s, with given %s, by the relationship named %s",
                                relationshipGetDocumentationVerb,
                                toName,
                                relationshipGetTargetDescription,
                                fromName,
                                uniqueIdFieldName,
                                relationshipName),
                        RoutingVerb.GET,
                        aUrl,
                        RoutingStatus.returnedFromCall())
                .addRequestUrlParam(uniqueIdField)
                .addPossibleStatus(
                        RoutingStatus.returnValue(
                                200,
                                String.format(
                                        "%s related %s %s",
                                        getReturnsSingle ? "the" : "all the",
                                        toName,
                                        relationshipGetTargetDescription)))
                .returnPayload(200, relationshipGetReturnPayload(relationship));

        defn.addRouting(
                        String.format(
                                "query all the %s items related to %s, with given %s, by the relationship named %s using application/x-www-form-urlencoded query content",
                                toName, fromName, uniqueIdFieldName, relationshipName),
                        RoutingVerb.QUERY,
                        aUrl,
                        RoutingStatus.returnedFromCall())
                .addRequestUrlParam(uniqueIdField)
                .addPossibleStatus(
                        RoutingStatus.returnValue(
                                200, String.format("all the matching related %s items", toName)))
                .addPossibleStatus(RoutingStatus.returnValue(400))
                .addPossibleStatus(RoutingStatus.returnValue(413))
                .addPossibleStatus(RoutingStatus.returnValue(415))
                .setAsFilterableFrom(relationship.getTo())
                .returnPayload(200, relationship.getTo().getPlural());

        defn.addRouting(
                        String.format(
                                "headers for the %s %s related to %s, with given %s, by the relationship named %s",
                                toName,
                                relationshipGetTargetDescription,
                                fromName,
                                uniqueIdFieldName,
                                relationshipName),
                        RoutingVerb.HEAD,
                        aUrl,
                        RoutingStatus.returnedFromCall())
                .addRequestUrlParam(uniqueIdField)
                .addPossibleStatus(
                        RoutingStatus.returnValue(
                                200,
                                String.format(
                                        "headers for %s related %s %s",
                                        getReturnsSingle ? "the" : "all the",
                                        toName,
                                        relationshipGetTargetDescription)));

        defn.addRouting(
                String.format("show all Options for endpoint of %s", aUrl),
                RoutingVerb.OPTIONS,
                aUrl,
                RoutingStatus.returnValue(200),
                new ResponseHeader("Allow", "OPTIONS, GET, HEAD, POST, QUERY"));

        // we can post if there is no guid as it will create the 'thing' and the relationship
        // connection
        defn.addRouting(
                        String.format(
                                "create an instance of a relationship named %s between %s instance %s and the %s instance represented by the %s in the body of the message",
                                relationshipName,
                                fromName,
                                uniqueIdentifier,
                                toName,
                                uniqueIdFieldName),
                        RoutingVerb.POST,
                        aUrl,
                        RoutingStatus.returnedFromCall())
                .addRequestUrlParam(uniqueIdField)
                .requestPayload(relationship.getTo().getName())
                .addPossibleStatus(
                        RoutingStatus.returnValue(201, String.format("created the relationship")))
                .addPossibleStatus(
                        RoutingStatus.returnValue(
                                400, String.format("error when creating the relationship")))
                .addPossibleStatus(
                        RoutingStatus.returnValue(
                                404, String.format("relationship source or target not found")))
                .addPossibleStatus(
                        RoutingStatus.returnValue(
                                422,
                                String.format("validation error when creating the relationship")))
                .addPossibleStatus(
                        RoutingStatus.returnValue(
                                409, String.format("conflict when creating the relationship")));

        defn.addRouting(
                "method not allowed", RoutingVerb.DELETE, aUrl, RoutingStatus.returnValue(405));
        defn.addRouting(
                "method not allowed", RoutingVerb.PATCH, aUrl, RoutingStatus.returnValue(405));
        defn.addRouting(
                "method not allowed", RoutingVerb.PUT, aUrl, RoutingStatus.returnValue(405));

        // we should be able to delete a relationship
        final Field relatedUniqueIdField = getUniqueIdField(relationship.getTo());
        final String relatedUniqueIdentifier =
                relatedUniqueIdField == null
                        ? uniqueIdentifier
                        : ":" + relatedParameterNameFor(relatedUniqueIdField);

        final String aUrlDelete =
                endPointPrefix
                        + fromNameForUrl
                        + "/"
                        + uniqueIdentifier
                        + "/"
                        + relationshipName
                        + "/"
                        + relatedUniqueIdentifier;
        defn.addRouting(
                        String.format(
                                "delete the instance of the relationship named %s between %s and %s using the %s",
                                relationshipName, fromName, toName, uniqueIdentifier),
                        RoutingVerb.DELETE,
                        aUrlDelete,
                        RoutingStatus.returnedFromCall())
                .addRequestUrlParam(uniqueIdField)
                .addRequestUrlParam(
                        relatedParameterNameFor(relatedUniqueIdField), relatedUniqueIdField)
                .addPossibleStatus(
                        RoutingStatus.returnValue(204, String.format("deleted the relationship")))
                .addPossibleStatus(
                        RoutingStatus.returnValue(
                                400, String.format("error when deleting the relationship")))
                .addPossibleStatus(
                        RoutingStatus.returnValue(404, String.format("relationship not found")))
                .addPossibleStatus(
                        RoutingStatus.returnValue(
                                422,
                                String.format("validation error when deleting the relationship")))
                .addPossibleStatus(
                        RoutingStatus.returnValue(
                                409, String.format("conflict when deleting the relationship")));

        defn.addRouting(
                String.format("show all Options for endpoint of %s", aUrlDelete),
                RoutingVerb.OPTIONS,
                aUrlDelete,
                RoutingStatus.returnValue(200),
                new ResponseHeader("Allow", "OPTIONS, DELETE"));

        defn.addRouting(
                "method not allowed", RoutingVerb.HEAD, aUrlDelete, RoutingStatus.returnValue(405));
        defn.addRouting(
                "method not allowed", RoutingVerb.GET, aUrlDelete, RoutingStatus.returnValue(405));
        defn.addRouting(
                "method not allowed",
                RoutingVerb.QUERY,
                aUrlDelete,
                RoutingStatus.returnValue(405),
                new ResponseHeader("Allow", "OPTIONS, DELETE"));
        defn.addRouting(
                "method not allowed",
                RoutingVerb.PATCH,
                aUrlDelete,
                RoutingStatus.returnValue(405));
        defn.addRouting(
                "method not allowed", RoutingVerb.PUT, aUrlDelete, RoutingStatus.returnValue(405));
        defn.addRouting(
                "method not allowed", RoutingVerb.POST, aUrlDelete, RoutingStatus.returnValue(405));
    }

    private boolean relationshipReturnsSingleInstance(
            final RelationshipVectorDefinition relationship) {
        return relationship.getCardinality().hasMaximumLimit()
                && relationship.getCardinality().maximumLimit() == 1;
    }

    private String relationshipGetReturnPayload(final RelationshipVectorDefinition relationship) {
        if (relationshipReturnsSingleInstance(relationship)) {
            return relationship.getTo().getName();
        }
        return relationship.getTo().getPlural();
    }
}
