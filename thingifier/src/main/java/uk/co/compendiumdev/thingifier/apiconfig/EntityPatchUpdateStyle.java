package uk.co.compendiumdev.thingifier.apiconfig;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.ContentTypeHeaderParser;

public enum EntityPatchUpdateStyle {
    PARTIAL_JSON_UPDATE("application/json"),
    JSON_MERGE_PATCH_RFC7396("application/merge-patch+json"),
    JSON_PATCH_RFC6902("application/json-patch+json");

    private final String mediaType;

    EntityPatchUpdateStyle(final String mediaType) {
        this.mediaType = mediaType;
    }

    public String mediaType() {
        return mediaType;
    }

    public boolean matches(final String contentTypeHeader) {
        return new ContentTypeHeaderParser(contentTypeHeader).isMediaType(mediaType);
    }

    public static Optional<EntityPatchUpdateStyle> fromContentType(final String contentTypeHeader) {
        for (EntityPatchUpdateStyle style : values()) {
            if (style.matches(contentTypeHeader)) {
                return Optional.of(style);
            }
        }
        return Optional.empty();
    }

    public static String acceptPatchHeaderValue(final Collection<EntityPatchUpdateStyle> styles) {
        return styles.stream()
                .sorted()
                .map(EntityPatchUpdateStyle::mediaType)
                .collect(Collectors.joining(", "));
    }
}
