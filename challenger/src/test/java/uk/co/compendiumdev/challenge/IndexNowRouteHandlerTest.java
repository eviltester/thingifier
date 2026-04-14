package uk.co.compendiumdev.challenge;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import spark.Route;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class IndexNowRouteHandlerTest {

    @Test
    public void routeCanBeConfiguredWhenValuesAreValid() {

        AtomicReference<String> capturedPath = new AtomicReference<>(null);
        AtomicReference<Route> capturedRoute = new AtomicReference<>(null);

        BiConsumer<String, Route> registrar = (path, route) -> {
            capturedPath.set(path);
            capturedRoute.set(route);
        };

        IndexNowRouteHandler handler =
                new IndexNowRouteHandler("/myIndexNowKey63638.txt", "abc123", registrar);

        Assertions.assertTrue(handler.configureRoutes());
        Assertions.assertEquals("/myIndexNowKey63638.txt", capturedPath.get());
        Assertions.assertNotNull(capturedRoute.get());
    }

    @Test
    public void routeReturnsConfiguredKey() throws Exception {

        AtomicReference<Route> capturedRoute = new AtomicReference<>(null);

        IndexNowRouteHandler handler =
                new IndexNowRouteHandler("/myIndexNowKey63638.txt", "abc123",
                        (path, route) -> capturedRoute.set(route));

        handler.configureRoutes();

        Assertions.assertNotNull(capturedRoute.get());
        Assertions.assertEquals("abc123", capturedRoute.get().handle(null, null));
    }

    @Test
    public void doesNotConfigureRouteWhenKeyMissing() {

        AtomicReference<Route> capturedRoute = new AtomicReference<>(null);

        IndexNowRouteHandler handler =
                new IndexNowRouteHandler("/myIndexNowKey63638.txt", " ",
                        (path, route) -> capturedRoute.set(route));

        Assertions.assertFalse(handler.configureRoutes());
        Assertions.assertNull(capturedRoute.get());
    }

    @Test
    public void doesNotConfigureRouteWhenLocationMissing() {

        AtomicReference<Route> capturedRoute = new AtomicReference<>(null);

        IndexNowRouteHandler handler =
                new IndexNowRouteHandler(" ", "abc123",
                        (path, route) -> capturedRoute.set(route));

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
        Assertions.assertFalse(IndexNowRouteHandler.isValidLocation("/myIndexNowKey63638.txt#hash"));
        Assertions.assertFalse(IndexNowRouteHandler.isValidLocation("/my IndexNowKey63638.txt"));
        Assertions.assertFalse(IndexNowRouteHandler.isValidLocation("/my\\IndexNowKey63638.txt"));
    }
}
