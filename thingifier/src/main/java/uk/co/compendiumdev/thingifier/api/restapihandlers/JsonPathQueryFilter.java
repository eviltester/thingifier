package uk.co.compendiumdev.thingifier.api.restapihandlers;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityViewDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQueryResult;

final class JsonPathQueryFilter {

    private final JsonThing jsonThing;

    JsonPathQueryFilter(final JsonThing jsonThing) {
        this.jsonThing = jsonThing;
    }

    ApiResponse filter(
            final RepositoryQueryResult queryResults,
            final ThingifierRequestContext context,
            final String expression,
            final EntityViewDefinition responseView) {
        final EntityDefinition entity = queryResults.resultContainsDefn();
        if (entity == null) {
            return unsafeSelection();
        }

        final String json =
                jsonThing
                        .asJsonObjectTypedArrayWithContentsUntyped(
                                queryResults.getListEntityInstances(),
                                entity.getPlural(),
                                context.store().relationships(),
                                responseView)
                        .toString();

        final Configuration configuration =
                Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST);
        final Object document = configuration.jsonProvider().parse(json);

        try {
            final ProjectedResourceLookup projectedResourceLookup =
                    projectedResourceLookup(
                            configuration, document, entity, queryResults.getListEntityInstances());

            final Object rawSelected =
                    JsonPath.using(configuration).parse(document).read(expression.trim());
            final List<?> selectedResources = normalizeSelectedResources(rawSelected, entity);
            final List<EntityInstance> matchingInstances = new ArrayList<>();

            for (Object selectedResource : selectedResources) {
                if (!(selectedResource instanceof Map<?, ?>)) {
                    return unsafeSelection();
                }

                final Map<?, ?> selectedResourceMap = (Map<?, ?>) selectedResource;
                final EntityInstance matchingInstance =
                        projectedResourceLookup.matchingInstanceFor(selectedResourceMap);
                if (matchingInstance == null) {
                    return unsafeSelection();
                }

                matchingInstances.add(matchingInstance);
            }

            final ApiResponse response =
                    ApiResponse.success()
                            .returnInstanceCollection(matchingInstances)
                            .resultContainsType(entity);
            if (responseView != null) {
                response.usingEntityView(responseView);
            }
            return response;
        } catch (PathNotFoundException e) {
            return unsafeSelection();
        } catch (InvalidPathException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (JsonPathException | IllegalArgumentException e) {
            return unsafeSelection();
        }
    }

    private ProjectedResourceLookup projectedResourceLookup(
            final Configuration configuration,
            final Object document,
            final EntityDefinition entity,
            final List<EntityInstance> instances) {
        final List<?> jsonResources =
                JsonPath.using(configuration)
                        .parse(document)
                        .read("$['" + escapedJsonPathName(entity.getPlural()) + "'][*]");
        if (jsonResources.size() != instances.size()) {
            throw new IllegalArgumentException("Projected resource count did not match instances");
        }

        final ProjectedResourceLookup projectedResourceLookup = new ProjectedResourceLookup();

        for (int index = 0; index < jsonResources.size(); index++) {
            final Object jsonResource = jsonResources.get(index);
            if (!(jsonResource instanceof Map<?, ?>)) {
                throw new IllegalArgumentException("Projected resource was not an object");
            }
            projectedResourceLookup.add((Map<?, ?>) jsonResource, instances.get(index));
        }

        return projectedResourceLookup;
    }

    private List<?> normalizeSelectedResources(
            final Object rawSelected, final EntityDefinition entity) {
        final List<?> selectedList = asList(rawSelected);
        final List<Object> resources = new ArrayList<>();

        for (Object selectedItem : selectedList) {
            if (selectedItem instanceof List<?>) {
                resources.addAll((List<?>) selectedItem);
                continue;
            }

            if (selectedItem instanceof Map<?, ?>) {
                final Map<?, ?> selectedMap = (Map<?, ?>) selectedItem;
                Object wrappedCollection = selectedMap.get(entity.getPlural());
                if (wrappedCollection instanceof List<?>) {
                    resources.addAll((List<?>) wrappedCollection);
                    continue;
                }
            }

            resources.add(selectedItem);
        }

        return resources;
    }

    private List<?> asList(final Object rawSelected) {
        if (rawSelected instanceof List<?>) {
            return (List<?>) rawSelected;
        }
        return List.of(rawSelected);
    }

    private String escapedJsonPathName(final String name) {
        return name.replace("\\", "\\\\").replace("'", "\\'");
    }

    private ApiResponse unsafeSelection() {
        return ApiResponse.error(
                422, "JSONPath query must select complete resource objects from the collection");
    }

    private static final class ProjectedResourceLookup {

        private final IdentityHashMap<Map<?, ?>, EntityInstance> identityMatches =
                new IdentityHashMap<>();

        private void add(final Map<?, ?> resource, final EntityInstance instance) {
            identityMatches.put(resource, instance);
        }

        private EntityInstance matchingInstanceFor(final Map<?, ?> selectedResource) {
            return identityMatches.get(selectedResource);
        }
    }
}
