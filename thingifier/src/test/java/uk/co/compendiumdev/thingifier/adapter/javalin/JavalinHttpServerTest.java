package uk.co.compendiumdev.thingifier.adapter.javalin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JavalinHttpServerTest {

    @Test
    void duplicateLegacyPathParamNamesAreMadeUniqueForJavalin() {
        Assertions.assertEquals(
                "todos/{id}/tasksof/{id__2}",
                JavalinHttpServer.javalinPath("todos/:id/tasksof/:id"));
    }

    @Test
    void keepsSingleLegacyPathParamNamesReadable() {
        Assertions.assertEquals(
                "/challenger/{id}", JavalinHttpServer.javalinPath("/challenger/:id"));
    }
}
