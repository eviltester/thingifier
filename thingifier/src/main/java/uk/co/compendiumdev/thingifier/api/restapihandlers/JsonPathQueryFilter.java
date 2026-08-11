package uk.co.compendiumdev.thingifier.api.restapihandlers;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
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
            final String expression) {
        final EntityDefinition entity = queryResults.resultContainsDefn();
        if (entity == null || entity.getPrimaryKeyField() == null) {
            return unsafeSelection();
        }

        final String json =
                jsonThing
                        .asJsonObjectTypedArrayWithContentsUntyped(
                                queryResults.getListEntityInstances(),
                                entity.getPlural(),
                                context.store().relationships())
                        .toString();

        final Configuration configuration =
                Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST);
        final Object document = configuration.jsonProvider().parse(json);

        try {
            final Map<String, EntityInstance> instancesByPrimaryKey =
                    instancesByPrimaryKey(queryResults.getListEntityInstances());
            final Map<String, Map<?, ?>> jsonResourcesByPrimaryKey =
                    jsonResourcesByPrimaryKey(configuration, document, entity);

            final Object rawSelected =
                    JsonPath.using(configuration).parse(document).read(expression.trim());
            final List<?> selectedResources = normalizeSelectedResources(rawSelected, entity);
            final List<EntityInstance> matchingInstances = new ArrayList<>();

            for (Object selectedResource : selectedResources) {
                if (!(selectedResource instanceof Map<?, ?>)) {
                    return unsafeSelection();
                }

                final Map<?, ?> selectedResourceMap = (Map<?, ?>) selectedResource;
                final String primaryKey =
                        primaryKeyValueFrom(
                                selectedResourceMap, entity.getPrimaryKeyField().getName());
                if (primaryKey == null) {
                    return unsafeSelection();
                }

                final Map<?, ?> fullResourceMap = jsonResourcesByPrimaryKey.get(primaryKey);
                final EntityInstance matchingInstance = instancesByPrimaryKey.get(primaryKey);
                if (fullResourceMap == null
                        || matchingInstance == null
                        || !fullResourceMap.equals(selectedResourceMap)) {
                    return unsafeSelection();
                }

                matchingInstances.add(matchingInstance);
            }

            return ApiResponse.success()
                    .returnInstanceCollection(matchingInstances)
                    .resultContainsType(entity);
        } catch (PathNotFoundException e) {
            return unsafeSelection();
        } catch (InvalidPathException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (JsonPathException | IllegalArgumentException e) {
            return unsafeSelection();
        }
    }

    private Map<String, EntityInstance> instancesByPrimaryKey(
            final List<EntityInstance> instances) {
        final Map<String, EntityInstance> instancesByPrimaryKey = new LinkedHashMap<>();
        for (EntityInstance instance : instances) {
            instancesByPrimaryKey.put(instance.getPrimaryKeyValue(), instance);
        }
        return instancesByPrimaryKey;
    }

    private Map<String, Map<?, ?>> jsonResourcesByPrimaryKey(
            final Configuration configuration,
            final Object document,
            final EntityDefinition entity) {
        final List<?> jsonResources =
                JsonPath.using(configuration)
                        .parse(document)
                        .read("$['" + escapedJsonPathName(entity.getPlural()) + "'][*]");
        final Map<String, Map<?, ?>> jsonResourcesByPrimaryKey = new LinkedHashMap<>();

        for (Object jsonResource : jsonResources) {
            if (jsonResource instanceof Map<?, ?>) {
                final Map<?, ?> jsonResourceMap = (Map<?, ?>) jsonResource;
                final String primaryKey =
                        primaryKeyValueFrom(jsonResourceMap, entity.getPrimaryKeyField().getName());
                if (primaryKey != null) {
                    jsonResourcesByPrimaryKey.put(primaryKey, jsonResourceMap);
                }
            }
        }

        return jsonResourcesByPrimaryKey;
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

    private String primaryKeyValueFrom(final Map<?, ?> resource, final String primaryKeyName) {
        if (!resource.containsKey(primaryKeyName)) {
            return null;
        }

        final Object value = resource.get(primaryKeyName);
        if (value instanceof Number) {
            return new BigDecimal(value.toString()).stripTrailingZeros().toPlainString();
        }
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private String escapedJsonPathName(final String name) {
        return name.replace("\\", "\\\\").replace("'", "\\'");
    }

    private ApiResponse unsafeSelection() {
        return ApiResponse.error(
                422, "JSONPath query must select complete resource objects from the collection");
    }
}
