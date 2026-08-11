package uk.co.compendiumdev.thingifier.adapter.httpserver;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;
import uk.co.compendiumdev.thingifier.htmlgui.routing.DefaultGuiRoutings;

class ThingifierAutoDocGenRoutingTest {

    @AfterEach
    void clearRouteRegistry() {
        HttpRouteRegistry.clearCurrent();
    }

    @Test
    void registersApiUiRoutesByDefault() {
        final HttpRouteRegistry registry = new HttpRouteRegistry();
        HttpRouteRegistry.use(registry);

        new ThingifierAutoDocGenRouting(
                new Thingifier(), new ThingifierApiDocumentationDefn(), new DefaultGUIHTML());

        Assertions.assertTrue(hasGetRoute(registry, "/docs/swagger-ui"));
        Assertions.assertTrue(hasGetRoute(registry, "/docs/scalar-ui"));
    }

    @Test
    void canSkipSwaggerUiRoute() {
        final HttpRouteRegistry registry = new HttpRouteRegistry();
        HttpRouteRegistry.use(registry);
        final ThingifierApiDocumentationDefn apiDefn =
                new ThingifierApiDocumentationDefn().setCreateSwaggerUi(false);

        new ThingifierAutoDocGenRouting(new Thingifier(), apiDefn, new DefaultGUIHTML());

        Assertions.assertFalse(hasGetRoute(registry, "/docs/swagger-ui"));
        Assertions.assertTrue(hasGetRoute(registry, "/docs"));
        Assertions.assertTrue(hasGetRoute(registry, "/docs/openapi.json"));
    }

    @Test
    void canSkipScalarUiRoute() {
        final HttpRouteRegistry registry = new HttpRouteRegistry();
        HttpRouteRegistry.use(registry);
        final ThingifierApiDocumentationDefn apiDefn =
                new ThingifierApiDocumentationDefn().setCreateScalarUi(false);

        new ThingifierAutoDocGenRouting(new Thingifier(), apiDefn, new DefaultGUIHTML());

        Assertions.assertFalse(hasGetRoute(registry, "/docs/scalar-ui"));
        Assertions.assertTrue(hasGetRoute(registry, "/docs/swagger-ui"));
        Assertions.assertTrue(hasGetRoute(registry, "/docs"));
        Assertions.assertTrue(hasGetRoute(registry, "/docs/openapi.json"));
    }

    @Test
    void addsDefaultNavigationToEnabledUiRoutes() {
        final HttpRouteRegistry registry = new HttpRouteRegistry();
        HttpRouteRegistry.use(registry);
        final Thingifier thingifier = new Thingifier();
        final DefaultGUIHTML gui = new DefaultGUIHTML();

        new DefaultGuiRoutings(thingifier, gui).configureRoutes("/gui");
        new ThingifierAutoDocGenRouting(thingifier, new ThingifierApiDocumentationDefn(), gui);

        Assertions.assertEquals(
                "<div class='rootmenu menu'><ul>"
                        + "<li><a href='/docs'>Docs</a></li>"
                        + "<li><a href='/gui/entities'>UI</a></li>"
                        + "<li><a href='/docs/swagger-ui'>Swagger UI</a></li>"
                        + "<li><a href='/docs/scalar-ui'>Scalar UI</a></li>"
                        + "</ul></div>",
                gui.getActualMenuHtml());
    }

    @Test
    void omitsDisabledUiRoutesFromDefaultNavigation() {
        final HttpRouteRegistry registry = new HttpRouteRegistry();
        HttpRouteRegistry.use(registry);
        final Thingifier thingifier = new Thingifier();
        final DefaultGUIHTML gui = new DefaultGUIHTML();
        final ThingifierApiDocumentationDefn apiDefn =
                new ThingifierApiDocumentationDefn()
                        .setCreateSwaggerUi(false)
                        .setCreateScalarUi(false);

        new DefaultGuiRoutings(thingifier, gui).configureRoutes("/gui");
        new ThingifierAutoDocGenRouting(thingifier, apiDefn, gui);

        Assertions.assertEquals(
                "<div class='rootmenu menu'><ul>"
                        + "<li><a href='/docs'>Docs</a></li>"
                        + "<li><a href='/gui/entities'>UI</a></li>"
                        + "</ul></div>",
                gui.getActualMenuHtml());
    }

    private boolean hasGetRoute(final HttpRouteRegistry registry, final String path) {
        return registry.routes().stream()
                .anyMatch(route -> route.verb() == HttpRouteVerb.GET && route.path().equals(path));
    }
}
