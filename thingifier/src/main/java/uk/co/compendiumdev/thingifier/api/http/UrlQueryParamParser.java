package uk.co.compendiumdev.thingifier.api.http;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import uk.co.compendiumdev.thingifier.core.query.FilterBy;
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
        String fieldName = getFieldNameFrom(param);

        String opAndValue = param.substring(fieldName.length());

        return new FilterBy(fieldName, opAndValue);
    }

    private String getFieldNameFrom(final String param) {

        // for each FilterBy operator, try to find it in the string
        // if present, split the string there and the fieldname is to the
        // left of the operator
        for (String anOperator : FilterBy.operators) {
            if (param.contains(anOperator)) {
                return param.substring(0, param.indexOf(anOperator));
            }
        }

        return param;
    }
}
