package uk.co.compendiumdev.thingifier.api.http.headers.headerparser;

import java.util.*;

public class AcceptHeaderParser {
    private final String acceptHeader;
    private final List<String> acceptMediaTypeDefinitionsList;

    public boolean willAcceptAnything() {
        return willAccept(ACCEPT_TYPE.ANYTHING);
    }

    public boolean willAcceptXml() {
        return willAccept(ACCEPT_TYPE.XML);
    }

    public boolean willAcceptJson() {
        return willAccept(ACCEPT_TYPE.JSON);
    }

    public boolean willAcceptText() {
        return willAccept(ACCEPT_TYPE.TEXT);
    }

    public boolean willAcceptCsv() {
        return willAccept(ACCEPT_TYPE.CSV);
    }

    public boolean willAcceptHtml() {
        return willAccept(ACCEPT_TYPE.HTML);
    }

    public boolean willAcceptNdJson() {
        return willAccept(ACCEPT_TYPE.NDJSON);
    }

    public boolean willAcceptJsonLines() {
        return willAccept(ACCEPT_TYPE.JSONL);
    }

    public boolean willAcceptJsonSequence() {
        return willAccept(ACCEPT_TYPE.JSON_SEQ);
    }

    public boolean willAcceptTsv() {
        return willAccept(ACCEPT_TYPE.TSV);
    }

    public boolean hasAskedForXML() {
        return hasAskedFor(AcceptHeaderParser.ACCEPT_TYPE.XML);
    }

    public boolean hasAskedForJSON() {
        return hasAskedFor(ACCEPT_TYPE.JSON);
    }

    public boolean hasAskedForANY() {
        return hasAskedFor(ACCEPT_TYPE.ANYTHING);
    }

    public boolean hasAskedForTEXT() {
        return hasAskedFor(ACCEPT_TYPE.TEXT);
    }

    public boolean hasAskedForCSV() {
        return hasAskedFor(ACCEPT_TYPE.CSV);
    }

    public boolean hasAskedForHTML() {
        return hasAskedFor(ACCEPT_TYPE.HTML);
    }

    public boolean hasAskedForNDJSON() {
        return hasAskedFor(ACCEPT_TYPE.NDJSON);
    }

    public boolean hasAskedForJSONL() {
        return hasAskedFor(ACCEPT_TYPE.JSONL);
    }

    public boolean hasAskedForJSONSEQ() {
        return hasAskedFor(ACCEPT_TYPE.JSON_SEQ);
    }

    public boolean hasAskedForTSV() {
        return hasAskedFor(ACCEPT_TYPE.TSV);
    }

    public boolean missingAcceptHeader() {
        return this.acceptHeader.length() == 0;
    }

    public boolean isSupportedHeader() {
        boolean supported = false;

        if (acceptMediaTypeDefinitionsList.size() == 0) {
            // we are allowed blank or missing accept - that counts as default
            supported = true;
        }

        for (String askedFor : acceptMediaTypeDefinitionsList) {
            if (getMatchingType(askedFor) != ACCEPT_TYPE.NO_MATCHING_TYPE) {
                supported = true;
            }
        }
        return supported;
    }

    public enum ACCEPT_TYPE {
        XML("application/xml"),
        JSON("application/json"),
        CSV("text/csv"),
        TEXT("text/plain"),
        HTML("text/html"),
        NDJSON("application/x-ndjson"),
        JSONL("application/jsonl"),
        JSON_SEQ("application/json-seq"),
        TSV("text/tab-separated-values"),
        ANYTHING("application/*", "*/*"),
        NO_MATCHING_TYPE();

        private final List<String> mediaTypes;

        ACCEPT_TYPE(final String... mediaTypes) {
            this.mediaTypes = List.of(mediaTypes);
        }

        public String mediaType() {
            if (mediaTypes.isEmpty()) {
                return "";
            }
            return mediaTypes.get(0);
        }

        public List<String> mediaTypes() {
            return mediaTypes;
        }

        public boolean hasConcreteResponseMediaType() {
            return this != ANYTHING && this != NO_MATCHING_TYPE;
        }

        public boolean usesComponentSchemaInDocumentation() {
            return this == JSON || this == XML;
        }

        public static List<ACCEPT_TYPE> responseMediaTypes() {
            return List.of(JSON, XML, CSV, TEXT, HTML, NDJSON, JSONL, JSON_SEQ, TSV);
        }
    };

    // TODO: configure to all new accept headers and remove accept headers
    //       should probably do this with an AllowedAcceptableHeaders class
    public AcceptHeaderParser(final String acceptHeader) {

        if (acceptHeader == null) {
            this.acceptHeader = "";
        } else {
            this.acceptHeader = acceptHeader.trim().toLowerCase();
        }

        // TODO: use ;q=0.9 to sort items in the array
        String[] acceptMediaTypeDefinitions = this.acceptHeader.split(",");
        acceptMediaTypeDefinitionsList = new ArrayList<>();
        for (String type : acceptMediaTypeDefinitions) {
            if (type != null && type.trim().length() > 0) {
                acceptMediaTypeDefinitionsList.add(type.trim());
            }
        }
    }

    public String getPreferredType() {
        if (acceptMediaTypeDefinitionsList.size() == 0) {
            return "";
        }
        return acceptMediaTypeDefinitionsList.get(0);
    }

    public boolean hasAPreferenceFor(final ACCEPT_TYPE type) {

        // if type is found in the array before any other type
        // then assume this is a preference
        // TODO: use ;q=0.9 to allow preferences to have a priority but listed in different order
        for (String acceptedType : acceptMediaTypeDefinitionsList) {
            ACCEPT_TYPE matchingType = getMatchingType(acceptedType);
            if (matchingType != ACCEPT_TYPE.NO_MATCHING_TYPE
                    && matchingType != ACCEPT_TYPE.ANYTHING) {
                return matchingType == type;
            }
        }
        return false;
    }

    public List<ACCEPT_TYPE> getSupportedTypesInPreferenceOrder() {
        List<ACCEPT_TYPE> supportedTypes = new ArrayList<>();
        for (String acceptedType : acceptMediaTypeDefinitionsList) {
            ACCEPT_TYPE matchingType = getMatchingType(acceptedType);
            if (matchingType != ACCEPT_TYPE.NO_MATCHING_TYPE) {
                supportedTypes.add(matchingType);
            }
        }
        return supportedTypes;
    }

    private ACCEPT_TYPE getMatchingType(final String matchMe) {
        final String mediaType = mediaTypeFrom(matchMe);
        for (ACCEPT_TYPE type : ACCEPT_TYPE.values()) {
            for (String possibleMatch : type.mediaTypes()) {
                if (mediaType.equals(possibleMatch)) {
                    return type;
                }
            }
        }
        return ACCEPT_TYPE.NO_MATCHING_TYPE;
    }

    private String mediaTypeFrom(final String acceptMediaTypeDefinition) {
        if (acceptMediaTypeDefinition == null) {
            return "";
        }
        return acceptMediaTypeDefinition.split(";", 2)[0].trim();
    }

    public boolean hasAPreferenceForXml() {
        return hasAPreferenceFor(ACCEPT_TYPE.XML);
    }

    public boolean hasAPreferenceForJson() {
        return hasAPreferenceFor(ACCEPT_TYPE.JSON);
    }

    public boolean hasAPreferenceForCsv() {
        return hasAPreferenceFor(ACCEPT_TYPE.CSV);
    }

    public boolean hasAPreferenceForText() {
        return hasAPreferenceFor(ACCEPT_TYPE.TEXT);
    }

    public boolean hasAPreferenceForHtml() {
        return hasAPreferenceFor(ACCEPT_TYPE.HTML);
    }

    public boolean hasAPreferenceForNdJson() {
        return hasAPreferenceFor(ACCEPT_TYPE.NDJSON);
    }

    public boolean hasAPreferenceForJsonLines() {
        return hasAPreferenceFor(ACCEPT_TYPE.JSONL);
    }

    public boolean hasAPreferenceForJsonSequence() {
        return hasAPreferenceFor(ACCEPT_TYPE.JSON_SEQ);
    }

    public boolean hasAPreferenceForTsv() {
        return hasAPreferenceFor(ACCEPT_TYPE.TSV);
    }

    public boolean willAccept(final ACCEPT_TYPE type) {

        // if no types provided then we will accept anything
        if (acceptMediaTypeDefinitionsList.size() == 0) {
            return true;
        }

        boolean askedFor = hasAskedFor(type);
        if (askedFor) {
            return true;
        }

        // before we say no, check if it has asked for anything
        return hasAskedFor(ACCEPT_TYPE.ANYTHING);
    }

    public boolean hasAskedFor(final ACCEPT_TYPE type) {
        // look for specific type
        for (String acceptedType : acceptMediaTypeDefinitionsList) {
            String mediaType = mediaTypeFrom(acceptedType);
            for (String typeValue : type.mediaTypes()) {
                if (mediaType.equals(typeValue)) {
                    return true;
                }
            }
        }
        return false;
    }
}
