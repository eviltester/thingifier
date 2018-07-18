package uk.co.compendiumdev.casestudy.todomanager.api_non_http;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SimpleJsonValueParser {

    public static String get(String body, String... terms) {

        JsonElement obj = new JsonParser().parse(body);

        String value = "";

        for (int termId = 0; termId < terms.length; termId++) {
            if (termId < terms.length - 1) {
                if (obj.isJsonObject()) {
                    obj = obj.getAsJsonObject();
                    obj = ((JsonObject) obj).get(terms[termId]);
                } else {
                    if (obj.isJsonArray()) {
                        obj = ((JsonArray) obj).get(Integer.valueOf(terms[termId]));
                    }
                }

            } else {
                // TODO: if the final thing is an array of primitives then this won't work
                value = ((JsonObject) obj).get(terms[termId]).getAsString();
            }
        }

        return value;
    }
}
