package uk.co.compendiumdev.thingifier.api.http;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import uk.co.compendiumdev.thingifier.core.query.FilterBy;
import uk.co.compendiumdev.thingifier.core.query.FilterOperation;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public final class UrlQueryParamParser {
    public String urlDecode(final String possiblyUrlEncodedString) {
        String decoded = possiblyUrlEncodedString;

        try {
            decoded = URLDecoder.decode(possiblyUrlEncodedString, "UTF-8");
        } catch (Exception e) {
            System.out.println("error decoding " + possiblyUrlEncodedString);
            System.out.println(e.getMessage());
        }

        return decoded;
    }

    public QueryFilterParams parse(final String rawUrlParams) {
        try {
            return parseStrict(rawUrlParams);
        } catch (IllegalArgumentException e) {
            // TODO: should really have added a logger by now and avoid System.out
            System.out.println(e.getMessage());
            return new QueryFilterParams();
        }
    }

    public QueryFilterParams parseStrict(final String rawUrlParams) {

        QueryFilterParams filters = new QueryFilterParams();

        if (rawUrlParams == null) {
            return filters;
        }

        String parseThis = rawUrlParams.trim();

        if (parseThis.isEmpty()) {
            return filters;
        }

        String rawDecoded = urlDecodeStrict(parseThis);
        String[] rawParams = rawDecoded.split("&");

        for (String rawParam : rawParams) {
            String param = rawParam.trim();
            if (param.isEmpty()) {
                continue;
            }

            FilterBy aFilterBy = parseToFilterBy(param);
            if (aFilterBy.fieldName == null || aFilterBy.fieldName.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Malformed query content: field name is missing");
            }
            filters.add(aFilterBy);
        }
        return filters;
    }

    private String urlDecodeStrict(final String possiblyUrlEncodedString) {
        return URLDecoder.decode(possiblyUrlEncodedString, StandardCharsets.UTF_8);
    }

    private FilterBy parseToFilterBy(final String rawParam) {
        String param = rawParam.trim();
        return FilterOperation.firstIn(param)
                .map(operation -> filterByForOperation(param, operation))
                .orElseGet(() -> new FilterBy(param, FilterOperation.EQUALS, ""));
    }

    private FilterBy filterByForOperation(final String param, final FilterOperation operation) {
        int operationIndex = param.indexOf(operation.token());
        String fieldName = param.substring(0, operationIndex);

        if (operation != FilterOperation.EQUALS) {
            return new FilterBy(
                    fieldName,
                    operation,
                    param.substring(operationIndex + operation.token().length()));
        }

        String value = param.substring(operationIndex + operation.token().length());
        FilterOperation.ParsedValue parsedValue = FilterOperation.parseLeadingToken(value);
        return new FilterBy(fieldName, parsedValue.operation(), parsedValue.value());
    }
}
