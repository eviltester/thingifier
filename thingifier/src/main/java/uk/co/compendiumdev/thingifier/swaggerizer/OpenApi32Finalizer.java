package uk.co.compendiumdev.thingifier.swaggerizer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;

public final class OpenApi32Finalizer {

    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public String finalizeJson(final String openApi31Json) {
        final JsonObject document = JsonParser.parseString(openApi31Json).getAsJsonObject();
        document.addProperty("openapi", OpenApiSpecificationVersion.OPENAPI_3_2.documentVersion());
        promoteQueryOperations(document);
        return gson.toJson(document);
    }

    private void promoteQueryOperations(final JsonObject document) {
        final JsonObject paths = objectAt(document, "paths");
        if (paths == null) {
            return;
        }

        for (String pathName : paths.keySet()) {
            final JsonObject path = objectAt(paths, pathName);
            if (path == null || !path.has("x-query-operation")) {
                continue;
            }

            final JsonElement queryOperation = path.remove("x-query-operation");
            if (queryOperation == null || !queryOperation.isJsonObject()) {
                continue;
            }

            final JsonObject query = queryOperation.getAsJsonObject();
            query.remove("x-http-method");
            query.remove("x-query-content-types");
            ensureFormEncodedRequestBody(query);
            path.add("query", query);
        }
    }

    private void ensureFormEncodedRequestBody(final JsonObject operation) {
        JsonObject requestBody = objectAt(operation, "requestBody");
        if (requestBody == null) {
            requestBody = new JsonObject();
            operation.add("requestBody", requestBody);
        }

        if (!requestBody.has("required")) {
            requestBody.addProperty("required", false);
        }

        JsonObject content = objectAt(requestBody, "content");
        if (content == null) {
            content = new JsonObject();
            requestBody.add("content", content);
        }

        JsonObject formMediaType = objectAt(content, ThingifierHttpApi.QUERY_CONTENT_TYPE);
        if (formMediaType == null) {
            formMediaType = new JsonObject();
            content.add(ThingifierHttpApi.QUERY_CONTENT_TYPE, formMediaType);
        }

        if (!formMediaType.has("schema") || !formMediaType.get("schema").isJsonObject()) {
            formMediaType.add("schema", permissiveFormSchema());
        }

        JsonObject jsonPathMediaType =
                objectAt(content, ThingifierHttpApi.JSONPATH_QUERY_CONTENT_TYPE);
        if (jsonPathMediaType == null) {
            jsonPathMediaType = new JsonObject();
            content.add(ThingifierHttpApi.JSONPATH_QUERY_CONTENT_TYPE, jsonPathMediaType);
        }

        if (!jsonPathMediaType.has("schema") || !jsonPathMediaType.get("schema").isJsonObject()) {
            jsonPathMediaType.add("schema", stringSchema());
        }
    }

    private JsonObject permissiveFormSchema() {
        final JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        final JsonObject additionalProperties = new JsonObject();
        additionalProperties.addProperty("type", "string");
        schema.add("additionalProperties", additionalProperties);
        return schema;
    }

    private JsonObject stringSchema() {
        final JsonObject schema = new JsonObject();
        schema.addProperty("type", "string");
        return schema;
    }

    private JsonObject objectAt(final JsonObject parent, final String name) {
        if (parent == null || !parent.has(name) || !parent.get(name).isJsonObject()) {
            return null;
        }
        return parent.getAsJsonObject(name);
    }
}
