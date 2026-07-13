package uk.co.compendiumdev.thingifier.api.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;

public class ThingifierRequestContextTest {

    @Test
    public void defaultsToDefaultDatabaseWhenSessionHeaderIsAbsent() {
        Thingifier thingifier = new Thingifier();

        ThingifierRequestContext context =
                ThingifierRequestContext.from(thingifier, new HttpHeadersBlock());

        Assertions.assertEquals(EntityRelModel.DEFAULT_DATABASE_NAME, context.databaseName());
        Assertions.assertSame(
                thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME), context.store());
    }

    @Test
    public void resolvesSessionDatabaseAndPreservesHeaders() {
        Thingifier thingifier = new Thingifier();
        HttpHeadersBlock headers = new HttpHeadersBlock();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "session-one");

        ThingifierRequestContext context = ThingifierRequestContext.from(thingifier, headers);

        Assertions.assertEquals("session-one", context.databaseName());
        Assertions.assertSame(thingifier.getStore("session-one"), context.store());
        Assertions.assertSame(headers, context.headers());
    }
}
