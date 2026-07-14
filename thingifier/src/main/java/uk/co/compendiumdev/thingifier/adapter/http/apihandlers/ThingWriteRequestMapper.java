package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.List;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.commonerrorresponse.NoSuchEntity;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.CollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipInstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.UnmatchedRoute;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.command.ConnectExistingRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.DeleteThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DisconnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public final class ThingWriteRequestMapper {

    private final ThingBodyCommandMapper bodyCommandMapper;

    public ThingWriteRequestMapper(
            final SchemaCatalog schema,
            final ThingifierApiConfig apiConfig,
            final ThingStore store) {
        this.bodyCommandMapper = new ThingBodyCommandMapper(schema, apiConfig, store);
    }

    public ThingWriteRequestMapping mapPost(
            final ThingRoute route, final ApiBodyFields bodyFields) {
        if (route instanceof CollectionRoute) {
            CollectionRoute collection = (CollectionRoute) route;
            return bodyCommandMapper.mapCreate(bodyFields, collection.entity(), true);
        }

        if (route instanceof InstanceRoute) {
            InstanceRoute instance = (InstanceRoute) route;
            return mapPostToInstance(instance, bodyFields);
        }

        if (route instanceof UnmatchedRoute) {
            UnmatchedRoute unmatched = (UnmatchedRoute) route;
            if (!unmatched.firstPart().isEmpty() && unmatched.partCount() == 2) {
                return ThingWriteRequestMapping.error(NoSuchEntity.error(unmatched.firstPart()));
            }
        }

        if (route instanceof RelationshipCollectionRoute) {
            RelationshipCollectionRoute relationship = (RelationshipCollectionRoute) route;
            return mapPostToRelationship(relationship, bodyFields);
        }

        return ThingWriteRequestMapping.error(
                ApiMappingError.withMessage(400, "Your request was not understood"));
    }

    public ThingWriteRequestMapping mapPut(final ThingRoute route, final ApiBodyFields bodyFields) {
        if (route instanceof CollectionRoute) {
            return ThingWriteRequestMapping.error(
                    ApiMappingError.withMessage(405, "Cannot create root level entity with a PUT"));
        }

        if (route instanceof InstanceRoute) {
            InstanceRoute instance = (InstanceRoute) route;
            return bodyCommandMapper.mapPut(bodyFields, instance.entity(), instance.identifier());
        }

        if (route instanceof UnmatchedRoute) {
            UnmatchedRoute unmatched = (UnmatchedRoute) route;
            if (!unmatched.firstPart().isEmpty() && unmatched.partCount() == 2) {
                return ThingWriteRequestMapping.error(NoSuchEntity.error(unmatched.firstPart()));
            }
        }

        return ThingWriteRequestMapping.error(
                ApiMappingError.withMessage(400, "Your request was not understood"));
    }

    public ThingWriteRequestMapping mapDelete(final ThingRoute route) {
        if (route instanceof CollectionRoute) {
            return ThingWriteRequestMapping.error(
                    ApiMappingError.withMessage(405, "Cannot delete root level entity"));
        }

        if (route instanceof InstanceRoute) {
            InstanceRoute instance = (InstanceRoute) route;
            return ThingWriteRequestMapping.command(
                    new DeleteThingCommand(
                            instance.entity(), instance.identifier(), route.originalPath()));
        }

        if (route instanceof RelationshipInstanceRoute) {
            RelationshipInstanceRoute relationship = (RelationshipInstanceRoute) route;
            return ThingWriteRequestMapping.command(
                    new DisconnectRelationshipCommand(
                            relationship.parentEntity(),
                            relationship.parentIdentifier(),
                            relationship.relationshipName(),
                            relationship.childIdentifier(),
                            route.originalPath()));
        }

        return ThingWriteRequestMapping.error(
                ApiMappingError.withMessage(
                        404,
                        String.format(
                                "Could not find any instances with %s", route.originalPath())));
    }

    private ThingWriteRequestMapping mapPostToInstance(
            final InstanceRoute route, final ApiBodyFields bodyFields) {
        if (route.entity().hasPrimaryKeyField()) {
            return bodyCommandMapper.mapAmend(
                    bodyFields,
                    route.entity(),
                    route.identifier(),
                    false,
                    String.format(
                            "No such %s entity instance with %s == %s found",
                            route.entity().getName(),
                            route.entity().getPrimaryKeyField().getName(),
                            route.identifier()));
        }

        return ThingWriteRequestMapping.error(
                ApiMappingError.withMessage(
                        404,
                        String.format(
                                "Entity %s does not have a primary key defined",
                                route.entity().getName())));
    }

    private ThingWriteRequestMapping mapPostToRelationship(
            final RelationshipCollectionRoute route, final ApiBodyFields bodyFields) {
        List<RelationshipVectorDefinition> possibleRelationships =
                route.parentEntity().related().getRelationships(route.relationshipName());
        RelationshipVectorDefinition relationshipToUse = possibleRelationships.get(0);
        EntityDefinition thingTo = relationshipToUse.getTo();

        List<NamedValue> childReferenceFields =
                FieldValues.fromListMapEntryStringString(bodyFields.asFlattenedStringMap());
        if (bodyReferencesExistingRelatedItem(thingTo, childReferenceFields)) {
            return ThingWriteRequestMapping.command(
                    new ConnectExistingRelationshipCommand(
                            route.parentEntity(),
                            route.parentIdentifier(),
                            route.relationshipName(),
                            childReferenceFields,
                            route.originalPath()));
        }

        return bodyCommandMapper.mapCreateAndConnect(
                bodyFields,
                route.parentEntity(),
                route.parentIdentifier(),
                route.relationshipName(),
                relationshipToUse.getTo(),
                route.originalPath());
    }

    private boolean bodyReferencesExistingRelatedItem(
            final EntityDefinition targetEntity, final List<NamedValue> bodyFields) {
        for (NamedValue fieldValue : bodyFields) {
            Field field = targetEntity.getField(fieldValue.getName());
            if (field == null) {
                continue;
            }
            if (field.getType() == FieldType.AUTO_GUID
                    || field.getType() == FieldType.AUTO_INCREMENT) {
                return true;
            }
        }
        return false;
    }
}
