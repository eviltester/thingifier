package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.restapihandlers.commonerrorresponse.NoSuchEntity;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.command.ConnectExistingRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.DeleteThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DisconnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public final class ThingWriteRequestMapper {

    private final SchemaCatalog schema;
    private final ThingStore store;
    private final ThingBodyCommandMapper bodyCommandMapper;

    public ThingWriteRequestMapper(final Thingifier thingifier, final String database) {
        this(thingifier, thingifier.getStore(database));
    }

    public ThingWriteRequestMapper(final Thingifier thingifier, final ThingStore store) {
        this(new ThingifierSchemaCatalog(thingifier), thingifier.apiConfig(), store);
    }

    ThingWriteRequestMapper(
            final SchemaCatalog schema,
            final ThingifierApiConfig apiConfig,
            final ThingStore store) {
        this.schema = schema;
        this.store = store;
        this.bodyCommandMapper = new ThingBodyCommandMapper(schema, apiConfig, store);
    }

    public ThingWriteRequestMapping mapPost(final String url, final BodyParser args) {
        EntityDefinition entityDefinition = EntityUrlMatcher.entityFromCollectionUrl(schema, url);
        if (entityDefinition != null) {
            return bodyCommandMapper.mapCreate(args, entityDefinition, true);
        }

        entityDefinition = EntityUrlMatcher.entityFromInstanceUrl(schema, url);
        if (entityDefinition != null) {
            return mapPostToInstance(url, args, entityDefinition);
        }

        String thingName = EntityUrlMatcher.entityTermFromUrl(url);
        if (!thingName.isEmpty() && EntityUrlMatcher.hasPartCount(url, 2)) {
            return ThingWriteRequestMapping.error(NoSuchEntity.error(thingName));
        }

        RepositoryBackedRelationshipUrlResolver.RelationshipUrlResolution relationship =
                new RepositoryBackedRelationshipUrlResolver(schema, store).resolveCollection(url);
        if (relationship.matchedRelationshipPath()) {
            return mapPostToRelationship(url, args, relationship);
        }

        return ThingWriteRequestMapping.error(
                ApiMappingError.withMessage(400, "Your request was not understood"));
    }

    public ThingWriteRequestMapping mapPut(final String url, final BodyParser args) {
        EntityDefinition thing = EntityUrlMatcher.entityFromCollectionUrl(schema, url);
        if (thing != null) {
            return ThingWriteRequestMapping.error(
                    ApiMappingError.withMessage(405, "Cannot create root level entity with a PUT"));
        }

        thing = EntityUrlMatcher.entityFromInstanceUrl(schema, url);
        if (thing == null) {
            if (EntityUrlMatcher.hasPartCount(url, 2)) {
                String thingName = EntityUrlMatcher.entityTermFromUrl(url);
                if (!thingName.isEmpty()) {
                    return ThingWriteRequestMapping.error(NoSuchEntity.error(thingName));
                }
            }
            return ThingWriteRequestMapping.error(
                    ApiMappingError.withMessage(400, "Your request was not understood"));
        }

        String instanceGuid = EntityUrlMatcher.identifierFromInstanceUrl(url);
        EntityInstance instance = store.entityQueries().findByQueryIdentifier(thing, instanceGuid);

        if (instance == null) {
            return mapPutCreate(args, thing, instanceGuid);
        }

        return bodyCommandMapper.mapAmend(args, instance, true);
    }

    public ThingWriteRequestMapping mapDelete(final String url) {
        EntityDefinition thing = EntityUrlMatcher.entityFromCollectionUrl(schema, url);
        if (thing != null) {
            return ThingWriteRequestMapping.error(
                    ApiMappingError.withMessage(405, "Cannot delete root level entity"));
        }

        EntityDefinition entity = EntityUrlMatcher.entityFromInstanceUrl(schema, url);
        if (entity != null) {
            EntityInstance instance = EntityUrlMatcher.findInstanceFromUrl(schema, store, url);
            if (instance == null) {
                return ThingWriteRequestMapping.error(
                        ApiMappingError.withMessage(
                                404, String.format("Could not find any instances with %s", url)));
            }
            return ThingWriteRequestMapping.command(new DeleteThingCommand(instance));
        }

        RepositoryBackedRelationshipUrlResolver.RelationshipUrlResolution relationship =
                new RepositoryBackedRelationshipUrlResolver(schema, store)
                        .resolveRelationshipInstance(url);
        if (relationship.matchedRelationshipPath()) {
            if (!relationship.relationshipInstancePath()
                    || relationship.parentInstance() == null
                    || relationship.childInstance() == null) {
                return ThingWriteRequestMapping.error(
                        ApiMappingError.withMessage(
                                404, String.format("Could not find any instances with %s", url)));
            }
            return ThingWriteRequestMapping.command(
                    new DisconnectRelationshipCommand(
                            relationship.parentInstance(),
                            relationship.childInstance(),
                            relationship.relationshipName()));
        }

        return ThingWriteRequestMapping.error(
                ApiMappingError.withMessage(
                        404, String.format("Could not find any instances with %s", url)));
    }

    private ThingWriteRequestMapping mapPostToInstance(
            final String url, final BodyParser args, final EntityDefinition entityDefinition) {
        String primaryKey = EntityUrlMatcher.identifierFromInstanceUrl(url);

        if (entityDefinition.hasPrimaryKeyField()) {
            EntityInstance instance =
                    store.entityQueries().findByQueryIdentifier(entityDefinition, primaryKey);

            if (instance == null) {
                return ThingWriteRequestMapping.error(
                        ApiMappingError.withMessage(
                                404,
                                String.format(
                                        "No such %s entity instance with %s == %s found",
                                        entityDefinition.getName(),
                                        entityDefinition.getPrimaryKeyField().getName(),
                                        primaryKey)));
            }

            return bodyCommandMapper.mapAmend(args, instance, false);
        }

        return ThingWriteRequestMapping.error(
                ApiMappingError.withMessage(
                        404,
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
                    ApiMappingError.withMessage(
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
                        ApiMappingError.withMessage(
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
                    ApiMappingError.withMessage(
                            404,
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
                        store.entityQueries().findByField(thingTo, fieldName, args.get(fieldName));
                if (relatedItem != null) {
                    break;
                }
            }
        }
        if (amExpectingARelatedItem && relatedItem == null) {
            matchingFieldNames = matchingFieldNames.trim().replace(" ", ", ");
            return ThingWriteRequestMapping.error(
                    ApiMappingError.withMessage(
                            404,
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
        ApiMappingError relationshipError =
                relationshipErrorIfInvalid(
                        connectThis, relatedItem, relationshipToUse, relationshipName);
        if (relationshipError != null) {
            return ThingWriteRequestMapping.error(relationshipError);
        }

        return ThingWriteRequestMapping.command(
                new ConnectExistingRelationshipCommand(
                        connectThis, relationshipToUse.getName(), relatedItem));
    }

    private ApiMappingError relationshipErrorIfInvalid(
            final EntityInstance connectThis,
            final EntityInstance relatedItem,
            final RelationshipVectorDefinition relationshipToUse,
            final String relationshipName) {
        if (relationshipToUse == null) {
            return ApiMappingError.withMessage(
                    400,
                    String.format(
                            "Could not find a relationship named %s between %s and a %s",
                            relationshipName,
                            connectThis.getEntity().getName(),
                            relatedItem.getEntity().getName()));
        }

        if (relationshipToUse.getTo() != relatedItem.getEntity()) {
            return ApiMappingError.withMessage(
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
