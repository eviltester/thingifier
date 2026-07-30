package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.commonerrorresponse.NoSuchEntity;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.CollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipInstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.UnmatchedRoute;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.application.command.DeleteThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DisconnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.RelateThingCommand;
import uk.co.compendiumdev.thingifier.application.schema.EntityTypeRef;
import uk.co.compendiumdev.thingifier.application.schema.RelationshipSpec;
import uk.co.compendiumdev.thingifier.application.schema.SchemaViewCatalog;

public final class ThingWriteRequestMapper {

    private final SchemaViewCatalog schema;
    private final ThingBodyCommandMapper bodyCommandMapper;

    public ThingWriteRequestMapper(final SchemaViewCatalog schema) {
        this.schema = schema;
        this.bodyCommandMapper = new ThingBodyCommandMapper(schema);
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

    public ThingWriteRequestMapping mapPatch(
            final ThingRoute route, final ApiBodyFields bodyFields) {
        if (route instanceof CollectionRoute) {
            return ThingWriteRequestMapping.error(
                    ApiMappingError.withMessage(405, "Cannot patch root level entity"));
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

        return ThingWriteRequestMapping.error(
                ApiMappingError.withMessage(400, "Your request was not understood"));
    }

    public ThingWriteRequestMapping mapPatchReplacingFields(
            final ThingRoute route, final ApiBodyFields bodyFields) {
        if (route instanceof InstanceRoute) {
            InstanceRoute instance = (InstanceRoute) route;
            return mapPatchToInstanceReplacingFields(instance, bodyFields);
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
                    new DeleteThingCommand(instance.entity().name(), instance.identifier()),
                    ApiRouteDisplay.originalPath(route.originalPath()));
        }

        if (route instanceof RelationshipInstanceRoute) {
            RelationshipInstanceRoute relationship = (RelationshipInstanceRoute) route;
            return ThingWriteRequestMapping.command(
                    new DisconnectRelationshipCommand(
                            relationship.parentEntity().name(),
                            relationship.parentIdentifier(),
                            relationship.relationshipName(),
                            relationship.childIdentifier()),
                    ApiRouteDisplay.originalPath(route.originalPath()));
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
            ThingWriteRequestMapping mapping =
                    bodyCommandMapper.mapAmend(
                            bodyFields, route.entity(), route.identifier(), false);
            return mapping.withRouteDisplay(
                    ApiRouteDisplay.missingInstanceMessage(
                            String.format(
                                    "No such %s entity instance with %s == %s found",
                                    route.entity().name(),
                                    route.entity().primaryKeyFieldName(),
                                    route.identifier())));
        }

        return ThingWriteRequestMapping.error(
                ApiMappingError.withMessage(
                        404,
                        String.format(
                                "Entity %s does not have a primary key defined",
                                route.entity().name())));
    }

    private ThingWriteRequestMapping mapPatchToInstanceReplacingFields(
            final InstanceRoute route, final ApiBodyFields bodyFields) {
        if (route.entity().hasPrimaryKeyField()) {
            ThingWriteRequestMapping mapping =
                    bodyCommandMapper.mapAmend(
                            bodyFields, route.entity(), route.identifier(), true, false);
            return mapping.withRouteDisplay(
                    ApiRouteDisplay.missingInstanceMessage(
                            String.format(
                                    "No such %s entity instance with %s == %s found",
                                    route.entity().name(),
                                    route.entity().primaryKeyFieldName(),
                                    route.identifier())));
        }

        return ThingWriteRequestMapping.error(
                ApiMappingError.withMessage(
                        404,
                        String.format(
                                "Entity %s does not have a primary key defined",
                                route.entity().name())));
    }

    private ThingWriteRequestMapping mapPostToRelationship(
            final RelationshipCollectionRoute route, final ApiBodyFields bodyFields) {
        EntityTypeRef childEntity = firstRelationshipTarget(route);
        if (childEntity == null) {
            return ThingWriteRequestMapping.error(
                    ApiMappingError.withMessage(400, "Your request was not understood"));
        }

        RelationshipBodyCommands relationships =
                bodyCommandMapper.parseRelationships(bodyFields, childEntity);
        if (!relationships.validationReport().isValid()) {
            return ThingWriteRequestMapping.error(
                    ApiMappingError.withMessage(
                            400,
                            String.format(
                                    "Invalid relationships: %s",
                                    relationships.validationReport().getCombinedErrorMessages())));
        }

        return ThingWriteRequestMapping.command(
                new RelateThingCommand(
                        route.parentEntity().name(),
                        route.parentIdentifier(),
                        route.relationshipName(),
                        bodyCommandMapper.fieldValuesExcludingRelationships(
                                bodyFields, relationships),
                        bodyCommandMapper.bodyFieldValues(bodyFields),
                        relationships.references()),
                ApiRouteDisplay.originalPath(route.originalPath()));
    }

    private EntityTypeRef firstRelationshipTarget(final RelationshipCollectionRoute route) {
        for (RelationshipSpec relationship : route.parentEntity().relationships()) {
            if (relationship.name().equals(route.relationshipName())) {
                return schema.entityWithSingularOrPluralName(relationship.toEntityName());
            }
        }
        return null;
    }
}
