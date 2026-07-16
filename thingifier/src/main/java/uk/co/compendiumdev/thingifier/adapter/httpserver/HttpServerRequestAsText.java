package uk.co.compendiumdev.thingifier.adapter.httpserver;

public class HttpServerRequestAsText {

    public String getRequestDetails(final HttpServerRequest request) {

        StringBuilder output = new StringBuilder();

        output.append(String.format("%s %s", request.method(), request.url()));
        output.append("\n");

        output.append("\n");
        output.append("Query Params");
        output.append("\n");
        output.append("============");
        output.append("\n");
        for (String queryParam : request.queryParamNames()) {
            output.append(String.format("%s: %s", queryParam, request.queryParams(queryParam)));
            output.append("\n");
        }

        output.append("\n");
        output.append("IP");
        output.append("\n");
        output.append("=======");
        output.append("\n");
        output.append(request.ip());
        output.append("\n");

        output.append("\n");
        output.append("Headers");
        output.append("\n");
        output.append("=======");
        output.append("\n");
        for (String header : request.headerNames()) {
            output.append(String.format("%s: %s", header, request.header(header)));
            output.append("\n");
        }
        output.append("\n");
        output.append("Body");
        output.append("\n");
        output.append("====");
        output.append("\n");
        output.append(request.body());
        output.append("\n");
        return output.toString();
    }
}
