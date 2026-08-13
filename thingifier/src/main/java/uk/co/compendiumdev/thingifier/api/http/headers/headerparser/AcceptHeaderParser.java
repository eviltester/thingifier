package uk.co.compendiumdev.thingifier.api.http.headers.headerparser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AcceptHeaderParser {
    private final String acceptHeader;
    private final List<AcceptedMediaRange> acceptMediaTypeDefinitionsList;

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
        if (acceptMediaTypeDefinitionsList.size() == 0) {
            // we are allowed blank or missing accept - that counts as default
            return true;
        }

        for (AcceptedMediaRange askedFor : acceptMediaTypeDefinitionsList) {
            if (askedFor.matchesAnyOf(ACCEPT_TYPE.responseMediaTypes())) {
                return true;
            }
        }
        return false;
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
        ANYTHING("*/*"),
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

        acceptMediaTypeDefinitionsList = new ArrayList<>();
        int order = 0;
        for (String type : splitOutsideQuotes(this.acceptHeader, ',')) {
            if (type != null && type.trim().length() > 0) {
                acceptMediaTypeDefinitionsList.add(AcceptedMediaRange.from(type.trim(), order));
                order++;
            }
        }
    }

    public String getPreferredType() {
        if (acceptMediaTypeDefinitionsList.size() == 0) {
            return "";
        }
        return acceptMediaTypeDefinitionsList.get(0).mediaRange();
    }

    public boolean hasAPreferenceFor(final ACCEPT_TYPE type) {

        final List<ACCEPT_TYPE> supportedTypes = getSupportedTypesInPreferenceOrder();
        if (supportedTypes.isEmpty()) {
            return false;
        }

        return supportedTypes.get(0) == type;
    }

    public List<ACCEPT_TYPE> getSupportedTypesInPreferenceOrder() {
        return getSupportedTypesInPreferenceOrder(ACCEPT_TYPE.responseMediaTypes());
    }

    public ACCEPT_TYPE preferredSupportedType(
            final List<ACCEPT_TYPE> supportedTypes, final ACCEPT_TYPE defaultType) {
        final List<ACCEPT_TYPE> concreteSupportedTypes = concreteSupportedTypes(supportedTypes);

        if (concreteSupportedTypes.isEmpty()) {
            return ACCEPT_TYPE.NO_MATCHING_TYPE;
        }

        if (acceptMediaTypeDefinitionsList.isEmpty()) {
            return defaultOrFirstSupported(concreteSupportedTypes, defaultType);
        }

        final List<ACCEPT_TYPE> orderedTypes =
                getSupportedTypesInPreferenceOrder(concreteSupportedTypes);
        if (orderedTypes.isEmpty()) {
            return ACCEPT_TYPE.NO_MATCHING_TYPE;
        }

        return orderedTypes.get(0);
    }

    private List<ACCEPT_TYPE> getSupportedTypesInPreferenceOrder(
            final List<ACCEPT_TYPE> candidateTypes) {
        final List<NegotiatedAcceptType> supportedTypes = new ArrayList<>();

        for (ACCEPT_TYPE type : candidateTypes) {
            bestMatchFor(type).ifPresent(match -> supportedTypes.add(match.negotiatedType()));
        }

        supportedTypes.sort(NegotiatedAcceptType.preferenceComparator());

        final List<ACCEPT_TYPE> responseTypes = new ArrayList<>();
        for (NegotiatedAcceptType supportedType : supportedTypes) {
            responseTypes.add(supportedType.type());
        }
        return responseTypes;
    }

    private List<ACCEPT_TYPE> concreteSupportedTypes(final List<ACCEPT_TYPE> supportedTypes) {
        final List<ACCEPT_TYPE> concreteSupportedTypes = new ArrayList<>();
        if (supportedTypes == null) {
            return concreteSupportedTypes;
        }
        for (ACCEPT_TYPE type : supportedTypes) {
            if (type != null && type.hasConcreteResponseMediaType()) {
                concreteSupportedTypes.add(type);
            }
        }
        return concreteSupportedTypes;
    }

    private ACCEPT_TYPE defaultOrFirstSupported(
            final List<ACCEPT_TYPE> supportedTypes, final ACCEPT_TYPE defaultType) {
        if (defaultType != null && supportedTypes.contains(defaultType)) {
            return defaultType;
        }
        return supportedTypes.get(0);
    }

    private Optional<AcceptedMediaRangeMatch> bestMatchFor(final ACCEPT_TYPE type) {
        if (type == null || !type.hasConcreteResponseMediaType()) {
            return Optional.empty();
        }

        AcceptedMediaRangeMatch bestMatch = null;
        for (AcceptedMediaRange acceptedType : acceptMediaTypeDefinitionsList) {
            final Optional<AcceptedMediaRangeMatch> match = acceptedType.match(type);
            if (match.isEmpty()) {
                continue;
            }

            if (bestMatch == null || match.get().isBetterEffectiveMatchThan(bestMatch)) {
                bestMatch = match.get();
            }
        }

        if (bestMatch == null || !bestMatch.isAcceptable()) {
            return Optional.empty();
        }

        return Optional.of(bestMatch);
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

        if (type == ACCEPT_TYPE.ANYTHING) {
            return hasAskedFor(ACCEPT_TYPE.ANYTHING);
        }

        return bestMatchFor(type).isPresent();
    }

    public boolean hasAskedFor(final ACCEPT_TYPE type) {
        // look for specific type
        for (AcceptedMediaRange acceptedType : acceptMediaTypeDefinitionsList) {
            for (String typeValue : type.mediaTypes()) {
                if (acceptedType.isAcceptable() && acceptedType.mediaRange().equals(typeValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<String> splitOutsideQuotes(final String value, final char separator) {
        final List<String> parts = new ArrayList<>();
        final StringBuilder currentPart = new StringBuilder();
        boolean insideQuotes = false;
        boolean escaped = false;

        for (int i = 0; i < value.length(); i++) {
            final char current = value.charAt(i);
            if (current == '\\' && insideQuotes && !escaped) {
                escaped = true;
                currentPart.append(current);
                continue;
            }
            if (current == '"' && !escaped) {
                insideQuotes = !insideQuotes;
            }
            if (current == separator && !insideQuotes) {
                parts.add(currentPart.toString());
                currentPart.setLength(0);
            } else {
                currentPart.append(current);
            }
            escaped = false;
        }

        parts.add(currentPart.toString());
        return parts;
    }

    private static final class AcceptedMediaRange {
        private static final double DEFAULT_Q_VALUE = 1.0D;

        private final String mediaRange;
        private final double qValue;
        private final boolean qValueIsValid;
        private final int order;

        private AcceptedMediaRange(
                final String mediaRange,
                final double qValue,
                final boolean qValueIsValid,
                final int order) {
            this.mediaRange = mediaRange;
            this.qValue = qValue;
            this.qValueIsValid = qValueIsValid;
            this.order = order;
        }

        static AcceptedMediaRange from(final String acceptDefinition, final int order) {
            final List<String> acceptParts = splitOutsideQuotes(acceptDefinition, ';');
            final String mediaRange = acceptParts.get(0).trim();
            double qValue = DEFAULT_Q_VALUE;
            boolean qValueIsValid = true;

            for (int i = 1; i < acceptParts.size(); i++) {
                final String parameter = acceptParts.get(i).trim();
                final int separator = parameter.indexOf('=');
                if (separator == -1) {
                    continue;
                }

                final String name = parameter.substring(0, separator).trim();
                final String value = unquote(parameter.substring(separator + 1).trim());
                if ("q".equals(name)) {
                    final Optional<Double> parsedQValue = parseQValue(value);
                    if (parsedQValue.isEmpty()) {
                        qValueIsValid = false;
                    } else {
                        qValue = parsedQValue.get();
                    }
                }
            }

            return new AcceptedMediaRange(mediaRange, qValue, qValueIsValid, order);
        }

        String mediaRange() {
            return mediaRange;
        }

        boolean isAcceptable() {
            return qValueIsValid && qValue > 0.0D;
        }

        boolean matchesAnyOf(final List<ACCEPT_TYPE> types) {
            for (ACCEPT_TYPE type : types) {
                if (match(type).isPresent()) {
                    return true;
                }
            }
            return false;
        }

        Optional<AcceptedMediaRangeMatch> match(final ACCEPT_TYPE type) {
            final int specificity = specificityFor(type);
            if (specificity == AcceptedMediaRangeMatch.NO_MATCH) {
                return Optional.empty();
            }
            return Optional.of(
                    new AcceptedMediaRangeMatch(type, qValue, qValueIsValid, order, specificity));
        }

        private int specificityFor(final ACCEPT_TYPE type) {
            for (String concreteMediaType : type.mediaTypes()) {
                if (mediaRange.equals(concreteMediaType)) {
                    return AcceptedMediaRangeMatch.EXACT_MATCH;
                }
                if (isStructuredSuffixWildcardMatch(concreteMediaType)) {
                    return AcceptedMediaRangeMatch.STRUCTURED_SUFFIX_MATCH;
                }
                if (isTypeWildcardMatch(concreteMediaType)) {
                    return AcceptedMediaRangeMatch.TYPE_WILDCARD_MATCH;
                }
                if (isAnythingWildcard()) {
                    return AcceptedMediaRangeMatch.ANYTHING_MATCH;
                }
            }
            return AcceptedMediaRangeMatch.NO_MATCH;
        }

        private boolean isStructuredSuffixWildcardMatch(final String concreteMediaType) {
            final String[] rangeParts = mediaRangeParts(mediaRange);
            final String[] concreteParts = mediaRangeParts(concreteMediaType);

            if (rangeParts.length != 2 || concreteParts.length != 2) {
                return false;
            }

            final String rangeType = rangeParts[0];
            final String rangeSubtype = rangeParts[1];
            if (!"*".equals(rangeType) && !rangeType.equals(concreteParts[0])) {
                return false;
            }

            if (!rangeSubtype.startsWith("*+")) {
                return false;
            }

            return concreteParts[1].endsWith(rangeSubtype.substring(1));
        }

        private boolean isTypeWildcardMatch(final String concreteMediaType) {
            final String[] rangeParts = mediaRangeParts(mediaRange);
            final String[] concreteParts = mediaRangeParts(concreteMediaType);

            return rangeParts.length == 2
                    && concreteParts.length == 2
                    && rangeParts[0].equals(concreteParts[0])
                    && "*".equals(rangeParts[1]);
        }

        private boolean isAnythingWildcard() {
            return "*/*".equals(mediaRange);
        }

        private static String[] mediaRangeParts(final String mediaType) {
            return mediaType.split("/", -1);
        }

        private static Optional<Double> parseQValue(final String value) {
            try {
                final double qValue = Double.parseDouble(value);
                if (qValue < 0.0D || qValue > 1.0D) {
                    return Optional.empty();
                }
                return Optional.of(qValue);
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }

        private static String unquote(final String value) {
            if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                return value.substring(1, value.length() - 1);
            }
            return value;
        }
    }

    private static final class AcceptedMediaRangeMatch {
        static final int NO_MATCH = -1;
        static final int ANYTHING_MATCH = 0;
        static final int TYPE_WILDCARD_MATCH = 1;
        static final int STRUCTURED_SUFFIX_MATCH = 2;
        static final int EXACT_MATCH = 3;

        private final ACCEPT_TYPE type;
        private final double qValue;
        private final boolean qValueIsValid;
        private final int order;
        private final int specificity;

        private AcceptedMediaRangeMatch(
                final ACCEPT_TYPE type,
                final double qValue,
                final boolean qValueIsValid,
                final int order,
                final int specificity) {
            this.type = type;
            this.qValue = qValue;
            this.qValueIsValid = qValueIsValid;
            this.order = order;
            this.specificity = specificity;
        }

        boolean isAcceptable() {
            return qValueIsValid && qValue > 0.0D;
        }

        boolean isBetterEffectiveMatchThan(final AcceptedMediaRangeMatch other) {
            if (specificity != other.specificity) {
                return specificity > other.specificity;
            }
            if (Double.compare(qValue, other.qValue) != 0) {
                return qValue > other.qValue;
            }
            return order < other.order;
        }

        NegotiatedAcceptType negotiatedType() {
            return new NegotiatedAcceptType(type, qValue, order, specificity);
        }
    }

    private static final class NegotiatedAcceptType {
        private final ACCEPT_TYPE type;
        private final double qValue;
        private final int order;
        private final int specificity;

        private NegotiatedAcceptType(
                final ACCEPT_TYPE type,
                final double qValue,
                final int order,
                final int specificity) {
            this.type = type;
            this.qValue = qValue;
            this.order = order;
            this.specificity = specificity;
        }

        ACCEPT_TYPE type() {
            return type;
        }

        static Comparator<NegotiatedAcceptType> preferenceComparator() {
            return Comparator.comparingDouble(NegotiatedAcceptType::qValue)
                    .reversed()
                    .thenComparing(
                            Comparator.comparingInt(NegotiatedAcceptType::specificity).reversed())
                    .thenComparingInt(NegotiatedAcceptType::order)
                    .thenComparingInt(NegotiatedAcceptType::serverPreference);
        }

        private double qValue() {
            return qValue;
        }

        private int specificity() {
            return specificity;
        }

        private int order() {
            return order;
        }

        private int serverPreference() {
            return ACCEPT_TYPE.responseMediaTypes().indexOf(type);
        }
    }
}
