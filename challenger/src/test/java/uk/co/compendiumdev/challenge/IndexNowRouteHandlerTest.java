package uk.co.compendiumdev.challenge;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteHandler;

public class IndexNowRouteHandlerTest {

    @Test
    public void routeCanBeConfiguredWhenValuesAreValid() {

        AtomicReference<String> capturedPath = new AtomicReference<>(null);
        AtomicReference<HttpRouteHandler> capturedRoute = new AtomicReference<>(null);

        BiConsumer<String, HttpRouteHandler> registrar =
                (path, HttpRouteHandler) -> {
                    capturedPath.set(path);
                    capturedRoute.set(HttpRouteHandler);
                };

        IndexNowRouteHandler handler =
                new IndexNowRouteHandler("/myIndexNowKey63638.txt", "abc123", registrar);

        Assertions.assertTrue(handler.configureRoutes());
        Assertions.assertEquals("/myIndexNowKey63638.txt", capturedPath.get());
        Assertions.assertNotNull(capturedRoute.get());
    }

    @Test
    public void routeReturnsConfiguredKey() throws Exception {

        AtomicReference<HttpRouteHandler> capturedRoute = new AtomicReference<>(null);

        IndexNowRouteHandler handler =
                new IndexNowRouteHandler(
                        "/myIndexNowKey63638.txt",
                        "abc123",
                        (path, HttpRouteHandler) -> capturedRoute.set(HttpRouteHandler));

        handler.configureRoutes();

        Assertions.assertNotNull(capturedRoute.get());
        Assertions.assertEquals("abc123", capturedRoute.get().handle(null, null));
    }

    @Test
    public void doesNotConfigureRouteWhenKeyMissing() {

        AtomicReference<HttpRouteHandler> capturedRoute = new AtomicReference<>(null);

        IndexNowRouteHandler handler =
                new IndexNowRouteHandler(
                        "/myIndexNowKey63638.txt",
                        " ",
                        (path, HttpRouteHandler) -> capturedRoute.set(HttpRouteHandler));

        Assertions.assertFalse(handler.configureRoutes());
        Assertions.assertNull(capturedRoute.get());
    }

    @Test
    public void doesNotConfigureRouteWhenLocationMissing() {

        AtomicReference<HttpRouteHandler> capturedRoute = new AtomicReference<>(null);

        IndexNowRouteHandler handler =
                new IndexNowRouteHandler(
                        " ",
                        "abc123",
                        (path, HttpRouteHandler) -> capturedRoute.set(HttpRouteHandler));

        Assertions.assertFalse(handler.configureRoutes());
        Assertions.assertNull(capturedRoute.get());
    }

    @Test
    public void locationMustStartWithSlashAndEndWithTxt() {
        Assertions.assertTrue(IndexNowRouteHandler.isValidLocation("/myIndexNowKey63638.txt"));
        Assertions.assertFalse(IndexNowRouteHandler.isValidLocation("myIndexNowKey63638.txt"));
        Assertions.assertFalse(IndexNowRouteHandler.isValidLocation("/myIndexNowKey63638"));
    }

    @Test
    public void locationMustNotContainUnsafeCharacters() {
        Assertions.assertFalse(IndexNowRouteHandler.isValidLocation("/my*IndexNowKey63638.txt"));
        Assertions.assertFalse(IndexNowRouteHandler.isValidLocation("/myIndexNowKey63638.txt?x=1"));
        Assertions.assertFalse(
                IndexNowRouteHandler.isValidLocation("/myIndexNowKey63638.txt#hash"));
        Assertions.assertFalse(IndexNowRouteHandler.isValidLocation("/my IndexNowKey63638.txt"));
        Assertions.assertFalse(IndexNowRouteHandler.isValidLocation("/my\\IndexNowKey63638.txt"));
    }
}
