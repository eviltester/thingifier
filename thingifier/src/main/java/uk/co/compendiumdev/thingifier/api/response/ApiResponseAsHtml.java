package uk.co.compendiumdev.thingifier.api.response;

import java.util.List;

public final class ApiResponseAsHtml {

    private final ApiResponse apiResponse;

    public ApiResponseAsHtml(final ApiResponse apiResponse) {
        this.apiResponse = apiResponse;
    }

    public String getHtml() {
        if (!apiResponse.hasABody()) {
            return "";
        }

        if (apiResponse.isErrorResponse()) {
            return errorMessages();
        }

        ApiResponseBodyRows responseRows = new ApiResponseBodyRows(apiResponse);
        List<String> fieldNames = responseRows.fieldNames();
        StringBuilder html = new StringBuilder();
        html.append("<table><thead><tr>");
        for (String fieldName : fieldNames) {
            html.append("<th>").append(escape(fieldName)).append("</th>");
        }
        html.append("</tr></thead><tbody>");
        for (List<String> row : responseRows.rows()) {
            html.append("<tr>");
            for (String value : row) {
                html.append("<td>").append(escape(value)).append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</tbody></table>");
        return html.toString();
    }

    private String errorMessages() {
        StringBuilder html = new StringBuilder();
        html.append("<ul>");
        for (String message : apiResponse.getErrorMessages()) {
            html.append("<li>").append(escape(message)).append("</li>");
        }
        html.append("</ul>");
        return html.toString();
    }

    private String escape(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
