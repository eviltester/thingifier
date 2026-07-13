package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.restapihandlers.commonerrorresponse.NoSuchEntity;
import uk.co.compendiumdev.thingifier.application.command.ConnectExistingRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.DeleteThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DisconnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public final class ThingWriteRequestMapper {

    private final Thingifier thingifier;
    private final String database;
    private final ThingBodyCommandMapper bodyCommandMapper;

    public ThingWriteRequestMapper(final Thingifier thingifier, final String database) {
        this.thingifier = thingifier;
        this.database = database;
        this.bodyCommandMapper = new ThingBodyCommandMapper(thingifier, database);
    }

    public ThingWriteRequestMapping mapPost(final String url, final BodyParser args) {
        EntityDefinition entityDefinition =
                EntityUrlMatcher.entityFromCollectionUrl(thingifier, url);
        if (entityDefinition != null) {
            return bodyCommandMapper.mapCreate(args, entityDefinition, true);
        }

        entityDefinition = EntityUrlMatcher.entityFromInstanceUrl(thingifier, url);
        if (entityDefinition != null) {
            return mapPostToInstance(url, args, entityDefinition);
        }

        String thingName = EntityUrlMatcher.entityTermFromUrl(url);
        if (!thingName.isEmpty() && EntityUrlMatcher.hasPartCount(url, 2)) {
            return ThingWriteRequestMapping.error(NoSuchEntity.response(thingName));
        }

        RepositoryBackedRelationshipUrlResolver.RelationshipUrlResolution relationship =
                new RepositoryBackedRelationshipUrlResolver(thingifier, database)
                        .resolveCollection(url);
        if (relationship.matchedRelationshipPath()) {
            return mapPostToRelationship(url, args, relationship);
        }

        return ThingWriteRequestMapping.error(
                ApiResponse.error(400, "Your request was not understood"));
    }

    public ThingWriteRequestMapping mapPut(final String url, final BodyParser args) {
        EntityDefinition thing = EntityUrlMatcher.entityFromCollectionUrl(thingifier, url);
        if (thing != null) {
            return ThingWriteRequestMapping.error(
                    ApiResponse.error(405, "Cannot create root level entity with a PUT"));
        }

        thing = EntityUrlMatcher.entityFromInstanceUrl(thingifier, url);
        if (thing == null) {
            if (EntityUrlMatcher.hasPartCount(url, 2)) {
                String thingName = EntityUrlMatcher.entityTermFromUrl(url);
                if (!thingName.isEmpty()) {
                    return ThingWriteRequestMapping.error(NoSuchEntity.response(thingName));
                }
            }
            return ThingWriteRequestMapping.error(
                    ApiResponse.error(400, "Your request was not understood"));
        }

        String instanceGuid = EntityUrlMatcher.identifierFromInstanceUrl(url);
        EntityInstance instance =
                thingifier
                        .getStore(database)
                        .entityQueries()
                        .findByQueryIdentifier(thing, instanceGuid);

        if (instance == null) {
            return mapPutCreate(args, thing, instanceGuid);
        }

        return bodyCommandMapper.mapAmend(args, instance, true);
    }

    public ThingWriteRequestMapping mapDelete(final String url) {
        EntityDefinition thing = EntityUrlMatcher.entityFromCollectionUrl(thingifier, url);
        if (thing != null) {
            return ThingWriteRequestMapping.error(
                    ApiResponse.error(405, "Cannot delete root level entity"));
        }

        EntityDefinition entity = EntityUrlMatcher.entityFromInstanceUrl(thingifier, url);
        if (entity != null) {
            EntityInstance instance =
                    EntityUrlMatcher.findInstanceFromUrl(thingifier, url, database);
            if (instance == null) {
                return ThingWriteRequestMapping.error(
                        ApiResponse.error404(
                                String.format("Could not find any instances with %s", url)));
            }
            return ThingWriteRequestMapping.command(new DeleteThingCommand(instance));
        }

        RepositoryBackedRelationshipUrlResolver.RelationshipUrlResolution relationship =
                new RepositoryBackedRelationshipUrlResolver(thingifier, database)
                        .resolveRelationshipInstance(url);
        if (relationship.matchedRelationshipPath()) {
            if (!relationship.relationshipInstancePath()
                    || relationship.parentInstance() == null
                    || relationship.childInstance() == null) {
                return ThingWriteRequestMapping.error(
                        ApiResponse.error404(
                                String.format("Could not find any instances with %s", url)));
            }
            return ThingWriteRequestMapping.command(
                    new DisconnectRelationshipCommand(
                            relationship.parentInstance(),
                            relationship.childInstance(),
                            relationship.relationshipName()));
        }

        return ThingWriteRequestMapping.error(
                ApiResponse.error404(String.format("Could not find any instances with %s", url)));
    }

    private ThingWriteRequestMapping mapPostToInstance(
            final String url, final BodyParser args, final EntityDefinition entityDefinition) {
        String primaryKey = EntityUrlMatcher.identifierFromInstanceUrl(url);

        if (entityDefinition.hasPrimaryKeyField()) {
            EntityInstance instance =
                    thingifier
                            .getStore(database)
                            .entityQueries()
                            .findByQueryIdentifier(entityDefinition, primaryKey);

            if (instance == null) {
                return ThingWriteRequestMapping.error(
                        ApiResponse.error404(
                                String.format(
                                        "No such %s entity instance with %s == %s found",
                                        entityDefinition.getName(),
                                        entityDefinition.getPrimaryKeyField().getName(),
                                        primaryKey)));
            }

            return bodyCommandMapper.mapAmend(args, instance, false);
        }

        return ThingWriteRequestMapping.error(
                ApiResponse.error404(
                        String.format(
                                "Entity %s does not have a primary key defined",
                                entityDefinition.getName())));
    }

    private ThingWriteRequestMapping mapPutCreate(
            final BodyParser args, final EntityDefinition thing, final String instanceGuid) {
        List<Field> forbiddenPutCreationFields =
                thing.getFieldsOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
        if (!forbiddenPutCreationFields.isEmpty()) {
            return ThingWriteRequestMapping.error(
                    ApiResponse.error(
                            400,
                            String.format(
                                    "Cannot create %s with PUT due to Auto fields %s",
                                    thing.getName(), fieldNames(forbiddenPutCreationFields))));
        }

        List<NamedValue> fieldValues =
                FieldValues.fromListMapEntryStringString(args.getFlattenedStringMap());
        for (NamedValue namedValue : fieldValues) {
            if (namedValue.name.equals(thing.getPrimaryKeyField().getName())
                    && !namedValue.value.equals(instanceGuid)) {
                return ThingWriteRequestMapping.error(
                        ApiResponse.error(
                                400,
                                String.format(
                                        "Cannot create %s with PUT as key does not match body value %s != %s",
                                        thing.getName(), instanceGuid, namedValue.value)));
            }
        }

        return bodyCommandMapper.mapCreateWithPrimaryKey(instanceGuid, args, thing);
    }

    private ThingWriteRequestMapping mapPostToRelationship(
            final String url,
            final BodyParser bodyargs,
            final RepositoryBackedRelationshipUrlResolver.RelationshipUrlResolution relationship) {
        final Map<String, String> args = bodyargs.getStringMap();
        String relationshipName = relationship.relationshipName();

        EntityInstance connectThis = relationship.parentInstance();
        if (connectThis == null) {
            return ThingWriteRequestMapping.error(
                    ApiResponse.error404(
                            String.format("Could not find parent thing for relationship %s", url)));
        }

        List<RelationshipVectorDefinition> possibleRelationships =
                connectThis.getEntity().related().getRelationships(relationshipName);
        RelationshipVectorDefinition relationshipToUse = possibleRelationships.get(0);
        EntityDefinition thingTo = relationshipToUse.getTo();

        EntityInstance relatedItem = null;
        boolean amExpectingARelatedItem = false;
        String matchingFieldNames = "";
        for (String fieldName : args.keySet()) {
            final Field field = thingTo.getField(fieldName);
            if (field == null) {
                continue;
            }
            if (field.getType() == FieldType.AUTO_GUID
                    || field.getType() == FieldType.AUTO_INCREMENT) {
                amExpectingARelatedItem = true;
                if (!matchingFieldNames.contains(fieldName + " ")) {
                    matchingFieldNames = matchingFieldNames + fieldName + " ";
                }
                relatedItem =
                        thingifier
                                .getStore(database)
                                .entityQueries()
                                .findByField(thingTo, fieldName, args.get(fieldName));
                if (relatedItem != null) {
                    break;
                }
            }
        }
        if (amExpectingARelatedItem && relatedItem == null) {
            matchingFieldNames = matchingFieldNames.trim().replace(" ", ", ");
            return ThingWriteRequestMapping.error(
                    ApiResponse.error404(
                            String.format(
                                    "Could not find thing matching value for %s",
                                    matchingFieldNames)));
        }

        if (relatedItem == null) {
            return bodyCommandMapper.mapCreateAndConnect(
                    bodyargs, connectThis, relationshipToUse.getName(), relationshipToUse.getTo());
        }

        relationshipToUse =
                connectThis
                        .getEntity()
                        .getNamedRelationshipTo(relationshipName, relatedItem.getEntity());
        ApiResponse relationshipError =
                relationshipErrorIfInvalid(
                        connectThis, relatedItem, relationshipToUse, relationshipName);
        if (relationshipError != null) {
            return ThingWriteRequestMapping.error(relationshipError);
        }

        return ThingWriteRequestMapping.command(
                new ConnectExistingRelationshipCommand(
                        connectThis, relationshipToUse.getName(), relatedItem));
    }

    private ApiResponse relationshipErrorIfInvalid(
            final EntityInstance connectThis,
            final EntityInstance relatedItem,
            final RelationshipVectorDefinition relationshipToUse,
            final String relationshipName) {
        if (relationshipToUse == null) {
            return ApiResponse.error(
                    400,
                    String.format(
                            "Could not find a relationship named %s between %s and a %s",
                            relationshipName,
                            connectThis.getEntity().getName(),
                            relatedItem.getEntity().getName()));
        }

        if (relationshipToUse.getTo() != relatedItem.getEntity()) {
            return ApiResponse.error(
                    400,
                    String.format(
                            "Could not connect %s (%s) to %s (%s) via relationship %s because it is a %s instead of a %s",
                            connectThis.getPrimaryKeyValue(),
                            connectThis.getEntity().getName(),
                            relatedItem.getPrimaryKeyValue(),
                            relatedItem.getEntity().getName(),
                            relationshipToUse.getName(),
                            relatedItem.getEntity().getName(),
                            relationshipToUse.getTo().getName()));
        }
        return null;
    }

    private String fieldNames(final List<Field> fields) {
        String names = "";
        for (Field field : fields) {
            if (!names.isEmpty()) {
                names = names + ", ";
            }
            names = names + field.getName();
        }
        return names;
    }
}
