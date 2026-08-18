package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.Set;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.commonerrorresponse.NoSuchEntity;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.CollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipInstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.UnmatchedRoute;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.apiconfig.EntityWriteMethodConfig;
import uk.co.compendiumdev.thingifier.apiconfig.PutIdentifierPolicy;
import uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation;
import uk.co.compendiumdev.thingifier.application.command.DeleteThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DisconnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.RelateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.UpdateConnectedRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.schema.EntityTypeRef;
import uk.co.compendiumdev.thingifier.application.schema.RelationshipSpec;
import uk.co.compendiumdev.thingifier.application.schema.SchemaViewCatalog;

public final class ThingWriteRequestMapper {

    private final SchemaViewCatalog schema;
    private final ThingBodyCommandMapper bodyCommandMapper;
    private final EntityWriteMethodConfig entityWriteMethods;
    private final Set<RelationshipWriteOperation> relationshipPostOperations;
    private final ThingifierRequestContext context;
    private final RelationshipWriteIntentResolver relationshipIntents;

    public ThingWriteRequestMapper(final SchemaViewCatalog schema) {
        this(schema, new EntityWriteMethodConfig());
    }

    public ThingWriteRequestMapper(
            final SchemaViewCatalog schema, final EntityWriteMethodConfig entityWriteMethods) {
        this.schema = schema;
        this.bodyCommandMapper = new ThingBodyCommandMapper(schema);
        this.entityWriteMethods =
                entityWriteMethods == null ? new EntityWriteMethodConfig() : entityWriteMethods;
        this.relationshipPostOperations = Set.of();
        this.context = null;
        this.relationshipIntents = null;
    }

    public ThingWriteRequestMapper(
            final SchemaCatalog schema,
            final EntityWriteMethodConfig entityWriteMethods,
            final Set<RelationshipWriteOperation> relationshipPostOperations,
            final ThingifierRequestContext context) {
        this.schema = schema;
        this.bodyCommandMapper = new ThingBodyCommandMapper(schema);
        this.entityWriteMethods =
                entityWriteMethods == null ? new EntityWriteMethodConfig() : entityWriteMethods;
        this.relationshipPostOperations =
                relationshipPostOperations == null ? Set.of() : relationshipPostOperations;
        this.context = context;
        this.relationshipIntents = new RelationshipWriteIntentResolver(schema);
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
            CollectionRoute collection = (CollectionRoute) route;
            return mapPutToEntity(bodyFields, collection.entity(), null);
        }

        if (route instanceof InstanceRoute) {
            InstanceRoute instance = (InstanceRoute) route;
            return mapPutToEntity(bodyFields, instance.entity(), instance.identifier());
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

    private ThingWriteRequestMapping mapPutToEntity(
            final ApiBodyFields bodyFields,
            final EntityTypeRef entity,
            final String uriIdentifier) {
        ApiMappingError identityError = putIdentifierPolicyError(bodyFields, entity, uriIdentifier);
        if (identityError != null) {
            return ThingWriteRequestMapping.error(identityError);
        }

        String identifier =
                hasIdentifier(uriIdentifier)
                        ? uriIdentifier
                        : payloadIdentifier(bodyFields, entity);
        if (!hasIdentifier(identifier)) {
            if (!entity.hasPrimaryKeyField()) {
                return ThingWriteRequestMapping.error(missingPrimaryKeyDefinitionError(entity));
            }
            return ThingWriteRequestMapping.error(
                    ApiMappingError.withMessage(
                            422, "PUT requires an identifier in the URI or payload"));
        }

        return bodyCommandMapper.mapPut(bodyFields, entity, identifier);
    }

    private ApiMappingError putIdentifierPolicyError(
            final ApiBodyFields bodyFields,
            final EntityTypeRef entity,
            final String uriIdentifier) {
        boolean hasUriIdentifier = hasIdentifier(uriIdentifier);
        if (!hasUriIdentifier
                && entityWriteMethods.putIdentifierInUri() == PutIdentifierPolicy.MANDATORY) {
            return ApiMappingError.withMessage(405, "Cannot create root level entity with a PUT");
        }
        if (hasUriIdentifier
                && entityWriteMethods.putIdentifierInUri() == PutIdentifierPolicy.DISALLOWED) {
            return ApiMappingError.withMessage(405, "Cannot identify entity with URI for PUT");
        }

        boolean hasPayloadIdentifier = hasPayloadIdentifier(bodyFields, entity);
        if (entityWriteMethods.putIdentifierInPayload() == PutIdentifierPolicy.MANDATORY
                && !hasPayloadIdentifier) {
            if (!entity.hasPrimaryKeyField()) {
                return missingPrimaryKeyDefinitionError(entity);
            }
            return ApiMappingError.withMessage(
                    422,
                    String.format(
                            "PUT payload must include identifier field %s",
                            entity.primaryKeyFieldName()));
        }
        if (entityWriteMethods.putIdentifierInPayload() == PutIdentifierPolicy.DISALLOWED
                && hasPayloadIdentifier) {
            return ApiMappingError.withMessage(
                    422,
                    String.format(
                            "PUT payload must not include identifier field %s",
                            entity.primaryKeyFieldName()));
        }
        return null;
    }

    private ApiMappingError missingPrimaryKeyDefinitionError(final EntityTypeRef entity) {
        return ApiMappingError.withMessage(
                404, String.format("Entity %s does not have a primary key defined", entity.name()));
    }

    private boolean hasPayloadIdentifier(
            final ApiBodyFields bodyFields, final EntityTypeRef entity) {
        return entity.hasPrimaryKeyField()
                && bodyFields.asStringMap().containsKey(entity.primaryKeyFieldName());
    }

    private String payloadIdentifier(final ApiBodyFields bodyFields, final EntityTypeRef entity) {
        if (!entity.hasPrimaryKeyField()) {
            return null;
        }
        return bodyFields.asStringMap().get(entity.primaryKeyFieldName());
    }

    private boolean hasIdentifier(final String identifier) {
        return identifier != null && !identifier.trim().isEmpty();
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

        RelationshipWriteIntent intent = relationshipIntentFor(route, bodyFields);
        if (intent.operation() == RelationshipWriteOperation.UPDATE_CONNECTED) {
            return ThingWriteRequestMapping.command(
                    new UpdateConnectedRelationshipCommand(
                            route.parentEntity().name(),
                            route.parentIdentifier(),
                            route.relationshipName(),
                            bodyCommandMapper.fieldValuesExcludingRelationships(
                                    bodyFields, relationships),
                            bodyCommandMapper.bodyFieldValues(bodyFields),
                            relationships.references()),
                    ApiRouteDisplay.originalPath(route.originalPath()));
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

    private RelationshipWriteIntent relationshipIntentFor(
            final RelationshipCollectionRoute route, final ApiBodyFields bodyFields) {
        if (relationshipIntents == null) {
            return RelationshipWriteIntent.none();
        }
        return relationshipIntents.intentFor(
                uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb.POST,
                route,
                bodyFields,
                context,
                relationshipPostOperations);
    }
}
