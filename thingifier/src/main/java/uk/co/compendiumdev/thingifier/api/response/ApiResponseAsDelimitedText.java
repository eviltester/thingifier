package uk.co.compendiumdev.thingifier.api.response;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public final class ApiResponseAsDelimitedText {

    private final ApiResponse apiResponse;
    private final char delimiter;

    public ApiResponseAsDelimitedText(final ApiResponse apiResponse, final char delimiter) {
        this.apiResponse = apiResponse;
        this.delimiter = delimiter;
    }

    public String getText() {
        if (!apiResponse.hasABody()) {
            return "";
        }

        if (apiResponse.isErrorResponse()) {
            return errorMessages();
        }

        ApiResponseBodyRows responseRows = new ApiResponseBodyRows(apiResponse);
        List<String> lines = new ArrayList<>();
        lines.add(delimitedLine(responseRows.fieldNames()));
        for (List<String> row : responseRows.rows()) {
            lines.add(delimitedLine(row));
        }
        return String.join("\n", lines);
    }

    private String errorMessages() {
        List<String> lines = new ArrayList<>();
        lines.add(escape("errorMessage"));
        for (String message : apiResponse.getErrorMessages()) {
            lines.add(escape(message));
        }
        return String.join("\n", lines);
    }

    private String delimitedLine(final List<String> values) {
        StringJoiner joiner = new StringJoiner(String.valueOf(delimiter));
        for (String value : values) {
            joiner.add(escape(value));
        }
        return joiner.toString();
    }

    private String escape(final String value) {
        String safeValue = value == null ? "" : value;
        if (delimiter == '\t') {
            return safeValue
                    .replace("\\", "\\\\")
                    .replace("\t", "\\t")
                    .replace("\r", "\\r")
                    .replace("\n", "\\n");
        }

        if (safeValue.contains("\"")
                || safeValue.contains(",")
                || safeValue.contains("\r")
                || safeValue.contains("\n")) {
            return "\"" + safeValue.replace("\"", "\"\"") + "\"";
        }
        return safeValue;
    }
}
