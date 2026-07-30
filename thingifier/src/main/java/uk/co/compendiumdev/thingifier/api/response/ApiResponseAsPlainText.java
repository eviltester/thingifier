package uk.co.compendiumdev.thingifier.api.response;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public final class ApiResponseAsPlainText {

    private final ApiResponse apiResponse;

    public ApiResponseAsPlainText(final ApiResponse apiResponse) {
        this.apiResponse = apiResponse;
    }

    public String getText() {
        if (!apiResponse.hasABody()) {
            return "";
        }

        if (apiResponse.isErrorResponse()) {
            return String.join("\n", escapedMessages());
        }

        ApiResponseBodyRows responseRows = new ApiResponseBodyRows(apiResponse);
        List<String> fieldNames = responseRows.fieldNames();
        List<String> lines = new ArrayList<>();
        for (List<String> row : responseRows.rows()) {
            StringJoiner joiner = new StringJoiner(", ");
            for (int index = 0; index < fieldNames.size(); index++) {
                joiner.add(fieldNames.get(index) + "=" + escape(row.get(index)));
            }
            lines.add(joiner.toString());
        }
        return String.join("\n", lines);
    }

    private List<String> escapedMessages() {
        List<String> messages = new ArrayList<>();
        for (String message : apiResponse.getErrorMessages()) {
            messages.add(escape(message));
        }
        return messages;
    }

    private String escape(final String value) {
        return value == null
                ? ""
                : value.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n");
    }
}
