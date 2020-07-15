package uk.co.compendiumdev.thingifier.api.http;

import java.util.*;

public class AcceptHeaderParser {
    private final String acceptHeader;
    private final List<String> acceptMediaTypeDefinitionsList;
    private final String[] acceptedXmlStrings = {
                                            "application/xml",
                        };
    private final String[] acceptedJsonStrings = {
            "application/json"
    };

    private final String[] acceptedAnythingStrings = {
            "application/*", "*/*"
    };

    private final Map<ACCEPT_TYPE, List<String>> acceptedTypes;

    public boolean willAcceptAnything() {
        return willAccept(ACCEPT_TYPE.ANYTHING);
    }

    public boolean willAcceptXml() {
        return willAccept(ACCEPT_TYPE.XML);
    }

    public boolean willAcceptJson() {
        return willAccept(ACCEPT_TYPE.JSON);
    }


    public enum ACCEPT_TYPE{ XML, JSON, ANYTHING, NO_MATCHING_TYPE};

    public AcceptHeaderParser(final String acceptHeader) {

        if(acceptHeader== null){
            this.acceptHeader="";
        }else{
            this.acceptHeader = acceptHeader;
        }

        acceptedTypes = new HashMap<ACCEPT_TYPE, List<String>>();
        acceptedTypes.put(ACCEPT_TYPE.XML, Arrays.asList(acceptedXmlStrings));
        acceptedTypes.put(ACCEPT_TYPE.JSON, Arrays.asList(acceptedJsonStrings));
        acceptedTypes.put(ACCEPT_TYPE.ANYTHING, Arrays.asList(acceptedAnythingStrings));
        acceptedTypes.put(ACCEPT_TYPE.NO_MATCHING_TYPE, new ArrayList<>());

        // TODO: use ;q=0.9 to sort items in the array
        String[] acceptMediaTypeDefinitions = this.acceptHeader.split(",");
        acceptMediaTypeDefinitionsList = new ArrayList<>();
        for(String type : acceptMediaTypeDefinitions){
            if(type!=null & type.trim().length()>0){
                acceptMediaTypeDefinitionsList.add(type.trim());
            }
        }
    }

    public boolean hasAPreferenceFor(final ACCEPT_TYPE type) {

        List<String> preferredHeaderValues = acceptedTypes.get(type);

        // if type is found in the array before any other type
        // then assume this is a preference
        // TODO: use ;q=0.9 to allow preferences to have a priority but listed in different order
        for(String acceptedType : acceptMediaTypeDefinitionsList){
            ACCEPT_TYPE matchingType = getMatchingType(acceptedType);
            if(matchingType!= ACCEPT_TYPE.NO_MATCHING_TYPE &&
                    matchingType!= ACCEPT_TYPE.ANYTHING){
                return matchingType==type;
            }

        }
        return false;
    }

    private ACCEPT_TYPE getMatchingType(final String matchMe) {
        for(Map.Entry<ACCEPT_TYPE, List<String>> type : acceptedTypes.entrySet()){
            List<String> validMatches = type.getValue();
            for(String possibleMatch : validMatches){
                if(matchMe.contains(possibleMatch)){
                    return type.getKey();
                }
            }
        }
        return ACCEPT_TYPE.NO_MATCHING_TYPE;
    }

    public boolean hasAPreferenceForXml() {
        return hasAPreferenceFor(ACCEPT_TYPE.XML);
    }

    public boolean hasAPreferenceForJson() {
        return hasAPreferenceFor(ACCEPT_TYPE.JSON);
    }

    public boolean willAccept(final ACCEPT_TYPE type) {

        if(acceptMediaTypeDefinitionsList.size()==0 &&
                type == ACCEPT_TYPE.ANYTHING){
            return true;
        }

        List<String> typeValues = acceptedTypes.get(type);

        for(String acceptedType : acceptMediaTypeDefinitionsList){
            for(String typeValue : typeValues) {
                if (acceptedType.contains(typeValue)) {
                    return true;
                }
            }
        }
        return false;
    }
}
