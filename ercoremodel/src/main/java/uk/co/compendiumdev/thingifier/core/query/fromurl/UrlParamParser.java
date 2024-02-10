package uk.co.compendiumdev.thingifier.core.query.fromurl;

import uk.co.compendiumdev.thingifier.core.query.FilterBy;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

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
        // fieldnames are all chars
        StringBuilder fieldName = new StringBuilder();

        // yeah it's horrible but start with this and refactor
        for (char ch: param.toCharArray()) {
            // this needs to match the valid values for creating field names and should probably use the same validation
            // it could even match the field values on entities and match against those direct?
            // This will probably break for complicated settings at the moment
            if("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".contains(String.valueOf(ch))){
                fieldName.append(ch);
            }else{
                break;
            }
        }

        return fieldName.toString();
    }
}
