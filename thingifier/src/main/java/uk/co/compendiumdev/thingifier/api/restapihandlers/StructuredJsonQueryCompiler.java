package uk.co.compendiumdev.thingifier.api.restapihandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ApiMappingError;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.JsonBodyValueConverter;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.query.FilterOperation;
import uk.co.compendiumdev.thingifier.core.query.PaginationParams;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.query.SortByFieldName;

final class StructuredJsonQueryCompiler {

    private static final Set<String> TOP_LEVEL_KEYS = Set.of("filter", "sort", "limit", "offset");
    private static final Set<String> SORT_KEYS = Set.of("field", "direction");

    private final EntityDefinition entity;
    private final QueryFilterParams queryParams;

    private StructuredJsonQueryCompiler(final EntityDefinition entity) {
        this.entity = entity;
        this.queryParams = new QueryFilterParams();
    }

    static Result compile(final String body, final EntityDefinition entity) {
        JsonNode document;
        try {
            document = JsonBodyValueConverter.readStrictTree(body);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            return Result.error(400, "Malformed JSON query body");
        }

        return new StructuredJsonQueryCompiler(entity).compile(document);
    }

    private Result compile(final JsonNode document) {
        if (entity == null) {
            return Result.error(422, "Structured JSON query target entity could not be resolved");
        }

        if (document == null || !document.isObject()) {
            return Result.error(422, "Structured JSON query body must be an object");
        }

        for (Map.Entry<String, JsonNode> property : document.properties()) {
            if (!TOP_LEVEL_KEYS.contains(property.getKey())) {
                return Result.error(
                        422, String.format("Unknown structured query field %s", property.getKey()));
            }
        }

        Result filterResult = compileFilter(document.get("filter"));
        if (filterResult.isError()) {
            return filterResult;
        }

        Result sortResult = compileSort(document.get("sort"));
        if (sortResult.isError()) {
            return sortResult;
        }

        Result limitResult =
                compilePagination(
                        document.get("limit"), PaginationParams.LIMIT_PARAMETER_NAME, "limit");
        if (limitResult.isError()) {
            return limitResult;
        }

        Result offsetResult =
                compilePagination(
                        document.get("offset"), PaginationParams.OFFSET_PARAMETER_NAME, "offset");
        if (offsetResult.isError()) {
            return offsetResult;
        }

        return Result.ok(queryParams);
    }

    private Result compileFilter(final JsonNode filter) {
        if (filter == null) {
            return Result.ok(queryParams);
        }

        if (!filter.isObject()) {
            return Result.error(422, "filter must be an object");
        }

        for (Map.Entry<String, JsonNode> fieldFilter : filter.properties()) {
            Result result = compileFieldFilter(fieldFilter.getKey(), fieldFilter.getValue());
            if (result.isError()) {
                return result;
            }
        }

        return Result.ok(queryParams);
    }

    private Result compileFieldFilter(final String requestedFieldName, final JsonNode value) {
        Field field = fieldNamed(requestedFieldName);
        if (field == null) {
            return Result.error(
                    422, String.format("Unknown query filter field %s", requestedFieldName));
        }

        if (value != null && value.isObject()) {
            return compileFieldOperators(field, value);
        }

        return compileExactFilter(field, value);
    }

    private Result compileExactFilter(final Field field, final JsonNode value) {
        if (!isExactMatchValueCompatible(field, value)) {
            return Result.error(
                    422, String.format("Invalid exact match value for field %s", field.getName()));
        }

        queryParams.put(field.getName(), FilterOperation.EXACT_MATCH, queryValue(value));
        return Result.ok(queryParams);
    }

    private Result compileFieldOperators(final Field field, final JsonNode operators) {
        if (operators.isEmpty()) {
            return Result.error(
                    422,
                    String.format("Filter operators for field %s are required", field.getName()));
        }

        for (Map.Entry<String, JsonNode> operator : operators.properties()) {
            Result result = compileFieldOperator(field, operator.getKey(), operator.getValue());
            if (result.isError()) {
                return result;
            }
        }

        return Result.ok(queryParams);
    }

    private Result compileFieldOperator(
            final Field field, final String operator, final JsonNode value) {
        switch (operator) {
            case "contains":
                return compileContainsFilter(field, value);
            case "greaterThan":
                return compileComparisonFilter(field, FilterOperation.GREATER_THAN, value);
            case "lessThan":
                return compileComparisonFilter(field, FilterOperation.LESS_THAN, value);
            default:
                return Result.error(
                        422,
                        String.format(
                                "Unsupported query operator %s for field %s",
                                operator, field.getName()));
        }
    }

    private Result compileContainsFilter(final Field field, final JsonNode value) {
        if (field.getType() != FieldType.STRING || value == null || !value.isTextual()) {
            return Result.error(
                    422,
                    String.format("contains is only supported for text field %s", field.getName()));
        }

        queryParams.put(field.getName(), FilterOperation.LITERAL_CONTAINS, value.asText());
        return Result.ok(queryParams);
    }

    private Result compileComparisonFilter(
            final Field field, final FilterOperation operation, final JsonNode value) {
        if (!isNumericValueCompatible(field, value)) {
            return Result.error(
                    422,
                    String.format(
                            "%s is only supported for numeric field %s",
                            operationName(operation), field.getName()));
        }

        queryParams.put(field.getName(), operation, queryValue(value));
        return Result.ok(queryParams);
    }

    private Result compileSort(final JsonNode sort) {
        if (sort == null) {
            return Result.ok(queryParams);
        }

        if (!sort.isArray()) {
            return Result.error(422, "sort must be an array");
        }

        List<String> sortFields = new ArrayList<>();
        for (JsonNode sortEntry : sort) {
            Result result = compileSortEntry(sortEntry, sortFields);
            if (result.isError()) {
                return result;
            }
        }

        if (!sortFields.isEmpty()) {
            queryParams.put(SortByFieldName.PARAMETER_NAME, String.join(",", sortFields));
        }

        return Result.ok(queryParams);
    }

    private Result compileSortEntry(final JsonNode sortEntry, final List<String> sortFields) {
        if (sortEntry == null || !sortEntry.isObject()) {
            return Result.error(422, "sort entries must be objects");
        }

        for (Map.Entry<String, JsonNode> property : sortEntry.properties()) {
            if (!SORT_KEYS.contains(property.getKey())) {
                return Result.error(422, String.format("Unknown sort field %s", property.getKey()));
            }
        }

        JsonNode fieldNode = sortEntry.get("field");
        JsonNode directionNode = sortEntry.get("direction");
        if (fieldNode == null || !fieldNode.isTextual()) {
            return Result.error(422, "sort field must be a string");
        }
        if (directionNode == null || !directionNode.isTextual()) {
            return Result.error(422, "sort direction must be asc or desc");
        }

        Field field = fieldNamed(fieldNode.asText());
        if (field == null) {
            return Result.error(422, String.format("Unknown sort field %s", fieldNode.asText()));
        }
        if (!isSortable(field)) {
            return Result.error(422, String.format("Field %s can not be sorted", field.getName()));
        }

        switch (directionNode.asText()) {
            case "asc":
                sortFields.add("+" + field.getName());
                return Result.ok(queryParams);
            case "desc":
                sortFields.add("-" + field.getName());
                return Result.ok(queryParams);
            default:
                return Result.error(422, "sort direction must be asc or desc");
        }
    }

    private Result compilePagination(
            final JsonNode value, final String parameterName, final String jsonName) {
        if (value == null) {
            return Result.ok(queryParams);
        }

        if (!value.isIntegralNumber() || !value.canConvertToInt() || value.asInt() < 0) {
            return Result.error(422, String.format("%s must be a non-negative integer", jsonName));
        }

        queryParams.put(parameterName, Integer.toString(value.asInt()));
        return Result.ok(queryParams);
    }

    private Field fieldNamed(final String fieldName) {
        if (fieldName == null || !entity.hasFieldNameDefined(fieldName)) {
            return null;
        }
        return entity.getField(fieldName);
    }

    private boolean isExactMatchValueCompatible(final Field field, final JsonNode value) {
        if (value == null || value.isNull() || value.isArray() || value.isObject()) {
            return false;
        }

        switch (field.getType()) {
            case AUTO_INCREMENT:
            case INTEGER:
                return value.isIntegralNumber();
            case FLOAT:
                return value.isNumber();
            case BOOLEAN:
                return value.isBoolean();
            case AUTO_GUID:
            case DATE:
            case ENUM:
            case STRING:
                return value.isTextual();
            default:
                return false;
        }
    }

    private boolean isNumericValueCompatible(final Field field, final JsonNode value) {
        if (value == null || !value.isNumber()) {
            return false;
        }

        switch (field.getType()) {
            case AUTO_INCREMENT:
            case INTEGER:
                return value.isIntegralNumber();
            case FLOAT:
                return true;
            default:
                return false;
        }
    }

    private boolean isSortable(final Field field) {
        switch (field.getType()) {
            case AUTO_INCREMENT:
            case INTEGER:
            case FLOAT:
            case BOOLEAN:
            case ENUM:
            case STRING:
                return true;
            default:
                return false;
        }
    }

    private String queryValue(final JsonNode value) {
        return value.asText();
    }

    private String operationName(final FilterOperation operation) {
        switch (operation) {
            case GREATER_THAN:
                return "greaterThan";
            case LESS_THAN:
                return "lessThan";
            default:
                return operation.token();
        }
    }

    static final class Result {
        private final QueryFilterParams queryParams;
        private final ApiMappingError error;

        private Result(final QueryFilterParams queryParams, final ApiMappingError error) {
            this.queryParams = queryParams;
            this.error = error;
        }

        static Result ok(final QueryFilterParams queryParams) {
            return new Result(queryParams, null);
        }

        static Result error(final int statusCode, final String message) {
            return new Result(null, ApiMappingError.withMessage(statusCode, message));
        }

        boolean isError() {
            return error != null;
        }

        QueryFilterParams queryParams() {
            return queryParams;
        }

        ApiMappingError error() {
            return error;
        }
    }
}
