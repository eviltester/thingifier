package uk.co.compendiumdev.thingifier.crudui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonSupport {

    private static final Gson GSON =
            new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private JsonSupport() {}

    public static String toJson(final Object value) {
        return GSON.toJson(value);
    }

    public static Map<?, ?> fromJsonMap(final String jsonText) {
        try {
            Object parsed = GSON.fromJson(jsonText, Object.class);
            if (parsed instanceof Map) {
                return (Map<?, ?>) parsed;
            }
            throw new CrudUiException(400, "Import file must contain a JSON object");
        } catch (JsonParseException e) {
            throw new CrudUiException(400, "Could not parse import JSON");
        }
    }

    public static Object fromJsonElement(final JsonElement element) {
        return GSON.fromJson(element, Object.class);
    }

    public static UiHttpResponse error(final int statusCode, final String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorMessages", List.of(message));
        return UiHttpResponse.json(statusCode, toJson(body));
    }
}
