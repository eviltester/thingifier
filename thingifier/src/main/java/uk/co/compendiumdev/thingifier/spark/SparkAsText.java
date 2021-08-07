package uk.co.compendiumdev.thingifier.spark;

import spark.Request;

public class SparkAsText {

    public String getRequestDetails(final Request request) {

        StringBuilder output = new StringBuilder();

        output.append(String.format("%s %s",request.requestMethod(), request.url()));
        output.append("\n");

        output.append("\n");
        output.append("Query Params");
        output.append("\n");
        output.append("============");
        output.append("\n");
        for(String queryParam : request.queryParams()){
            output.append(String.format("%s: %s",queryParam, request.queryParams(queryParam)));
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
        for(String header : request.headers()){
            output.append(String.format("%s: %s",header, request.headers(header)));
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
