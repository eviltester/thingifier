package uk.co.compendiumdev.thingifier.api.http.bodyparser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonBodyValueConverter {

    private static final ObjectMapper JSON =
            new ObjectMapper(
                    JsonFactory.builder().enable(JsonReadFeature.ALLOW_SINGLE_QUOTES).build());
    private static final ObjectMapper STRICT_JSON = new ObjectMapper();

    private JsonBodyValueConverter() {}

    public static JsonNode readTree(final String body) throws JsonProcessingException {
        return JSON.readTree(body);
    }

    public static JsonNode readStrictTree(final String body) throws JsonProcessingException {
        return STRICT_JSON.readTree(body);
    }

    public static Map<String, Object> jsonObjectAsMap(final String body)
            throws JsonProcessingException {
        return objectNodeAsMap(readTree(body));
    }

    public static Map<String, Object> objectNodeAsMap(final JsonNode node) {
        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException("JSON document must be an object");
        }

        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> field : node.properties()) {
            values.put(field.getKey(), valueFrom(field.getValue()));
        }
        return values;
    }

    private static Object valueFrom(final JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isBoolean()) {
            return Boolean.valueOf(node.asBoolean());
        }
        if (node.isIntegralNumber()) {
            return node.bigIntegerValue();
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }
        if (node.isObject()) {
            return objectNodeAsMap(node);
        }
        if (node.isArray()) {
            List<Object> values = new ArrayList<>();
            for (JsonNode item : node) {
                values.add(valueFrom(item));
            }
            return values;
        }
        return node.asText();
    }
}
