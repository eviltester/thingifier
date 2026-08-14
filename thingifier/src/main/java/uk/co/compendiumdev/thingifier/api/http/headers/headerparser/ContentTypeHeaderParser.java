package uk.co.compendiumdev.thingifier.api.http.headers.headerparser;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ContentTypeHeaderParser {
    public static final String APPLICATION_XML = "application/xml";
    public static final String TEXT_XML = "text/xml";
    private static final List<String> RESERVED_STRUCTURED_XML_SUBTYPES =
            List.of("problem", "soap", "xhtml", "atom", "rss", "svg");

    private final String header;

    public ContentTypeHeaderParser(final String header) {
        if (header == null) {
            this.header = "";
        } else {
            this.header = header.trim().toLowerCase();
        }
    }

    public boolean isXML() {
        // text/xml in standard https://datatracker.ietf.org/doc/html/rfc3023
        return supportedXmlMediaTypes().contains(mediaType());
    }

    public boolean isXML(final Collection<String> entityNames) {
        return isXML() || isStructuredXmlForEntity(entityNames);
    }

    public static List<String> supportedXmlMediaTypes() {
        return List.of(APPLICATION_XML, TEXT_XML);
    }

    public boolean isStructuredXmlForEntity(final Collection<String> entityNames) {
        return structuredXmlEntityName(entityNames).isPresent();
    }

    public Optional<String> structuredXmlEntityName(final Collection<String> entityNames) {
        final String[] parts = mediaRangeParts(mediaType());
        if (parts.length != 2 || !"application".equals(parts[0])) {
            return Optional.empty();
        }

        final String subtype = parts[1];
        if (subtype.contains("*") || !subtype.endsWith("+xml")) {
            return Optional.empty();
        }

        final String baseSubtype = subtype.substring(0, subtype.length() - "+xml".length());
        final String subtypeEntityToken = entityTokenFrom(baseSubtype);
        if (RESERVED_STRUCTURED_XML_SUBTYPES.contains(subtypeEntityToken)) {
            return Optional.empty();
        }

        if (entityNames == null) {
            return Optional.empty();
        }
        for (String entityName : entityNames) {
            final String normalizedEntity = normalizeEntityName(entityName);
            if (!normalizedEntity.isEmpty() && subtypeEntityToken.equals(normalizedEntity)) {
                return Optional.of(normalizedEntity);
            }
        }
        return Optional.empty();
    }

    public static Optional<String> defaultStructuredXmlMediaTypeFor(
            final Collection<String> entityNames) {
        if (entityNames == null) {
            return Optional.empty();
        }
        for (String entityName : entityNames) {
            final String normalizedEntity = normalizeEntityName(entityName);
            if (!normalizedEntity.isEmpty()
                    && !RESERVED_STRUCTURED_XML_SUBTYPES.contains(normalizedEntity)) {
                return Optional.of("application/" + normalizedEntity + "+xml");
            }
        }
        return Optional.empty();
    }

    private static String entityTokenFrom(final String subtypeWithoutSuffix) {
        int lastSeparator =
                Math.max(
                        subtypeWithoutSuffix.lastIndexOf('.'),
                        subtypeWithoutSuffix.lastIndexOf('-'));
        if (lastSeparator == -1) {
            return subtypeWithoutSuffix;
        }
        return subtypeWithoutSuffix.substring(lastSeparator + 1);
    }

    private static String normalizeEntityName(final String entityName) {
        if (entityName == null) {
            return "";
        }
        return entityName.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "-");
    }

    private static String[] mediaRangeParts(final String mediaType) {
        return mediaType.split("/", -1);
    }

    public boolean isJSON() {
        return isMediaType("application/json");
    }

    public boolean isJsonMergePatch() {
        return isMediaType("application/merge-patch+json");
    }

    public boolean isJsonPatch() {
        return isMediaType("application/json-patch+json");
    }

    public boolean isFormUrlEncoded() {
        return isMediaType("application/x-www-form-urlencoded");
    }

    public boolean isJsonPath() {
        return isMediaType("application/jsonpath");
    }

    public boolean isStructuredQueryJson() {
        return isMediaType("application/vnd.thingifier.query+json");
    }

    public boolean isMissing() {
        return (header.isEmpty());
    }

    public boolean isText() {
        return mediaType().startsWith("text/");
    }

    public boolean isMediaType(final String mediaType) {
        return mediaType().equalsIgnoreCase(mediaType);
    }

    public String mediaType() {
        int separatorIndex = header.indexOf(";");
        if (separatorIndex == -1) {
            return header;
        }
        return header.substring(0, separatorIndex).trim();
    }
}
