package uk.co.compendiumdev.thingifier.api.http;

import static uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi.HTTP_SESSION_HEADER_NAME;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public final class ThingifierRequestContext {

    private final String databaseName;
    private final ThingStore store;
    private final HttpHeadersBlock headers;

    private ThingifierRequestContext(
            final String databaseName, final ThingStore store, final HttpHeadersBlock headers) {
        this.databaseName = databaseName;
        this.store = store;
        this.headers = headers;
    }

    public static ThingifierRequestContext from(
            final Thingifier thingifier, final HttpHeadersBlock requestHeaders) {
        HttpHeadersBlock safeHeaders =
                requestHeaders == null ? new HttpHeadersBlock() : requestHeaders;
        String databaseName = databaseNameFrom(safeHeaders);
        thingifier.ensureCreatedAndPopulatedInstanceDatabaseNamed(databaseName);
        return new ThingifierRequestContext(
                databaseName, thingifier.getStore(databaseName), safeHeaders);
    }

    private static String databaseNameFrom(final HttpHeadersBlock requestHeaders) {
        String sessionHeaderValue = requestHeaders.get(HTTP_SESSION_HEADER_NAME);
        if (sessionHeaderValue.isEmpty()) {
            return EntityRelModel.DEFAULT_DATABASE_NAME;
        }
        return sessionHeaderValue;
    }

    public String databaseName() {
        return databaseName;
    }

    public ThingStore store() {
        return store;
    }

    public HttpHeadersBlock headers() {
        return headers;
    }
}
