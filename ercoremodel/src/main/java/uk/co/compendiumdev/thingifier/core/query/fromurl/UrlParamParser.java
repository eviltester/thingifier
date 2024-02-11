package uk.co.compendiumdev.thingifier.core.query.fromurl;

import uk.co.compendiumdev.thingifier.core.query.FilterBy;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

import java.net.URLDecoder;

public class UrlParamParser {
    public String urlDecode(String possiblyUrlEncodedString) {
        String decoded = possiblyUrlEncodedString;

        try {
            decoded = URLDecoder.decode(possiblyUrlEncodedString, "UTF-8");
        }catch(Exception e){
            System.out.println("error decoding " + possiblyUrlEncodedString);
            System.out.println(e.getMessage());
        }

        return decoded;
    }

    public QueryFilterParams parse(String rawUrlParams) {

        QueryFilterParams filters = new QueryFilterParams();

        if(rawUrlParams==null){
            return filters;
        }

        String parseThis = rawUrlParams.trim();

        if(parseThis.isEmpty()){
            return filters;
        }

        String rawDecoded = urlDecode(parseThis);
        String[] rawParams = rawDecoded.split("&");


        for (String rawParam : rawParams){
            try {
                FilterBy aFilterBy = parseToFilterBy(rawParam);
                filters.add(aFilterBy);
            }catch (Exception e){
                // TODO: should really have added a logger by now and avoid System.out
                System.out.println(e.getMessage());
            }
        }
        return filters;
    }

    private FilterBy parseToFilterBy(String rawParam) {
        String param = rawParam.trim();
        String fieldName = getFieldNameFrom(param);

        String opAndValue = param.substring(fieldName.length());

        return new FilterBy(fieldName, opAndValue);
    }

    private String getFieldNameFrom(String param) {

        // for each FilterBy operator, try to find it in the string
        // if present, split the string there and the fieldname is to the
        // left of the operator
        for(String anOperator : FilterBy.operators){
            if(param.contains(anOperator)){
                return param.substring(0, param.indexOf(anOperator));
            }
        }

        return param;
    }
}
