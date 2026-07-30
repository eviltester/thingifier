package uk.co.compendiumdev.thingifier.swaggerizer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;

public class SwaggerizerTest {

    @Test
    public void canCreateSwagger() {
        Thingifier t = new TodoListThingifierTestModel().get();

        ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.setThingifier(t);

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.POST, "/plan", RoutingStatus.returnedFromCall(), null)
                        .addDocumentation("Create a plan")
                        .addPossibleStatuses(200, 400));

        apiDefn.addServer("https://apichallenges.herokuapp.com", "heroku hosted version");
        apiDefn.addServer("http://localhost:4567", "local execution");
        apiDefn.setVersion("1.0.1");

        String swagger = new Swaggerizer(apiDefn).asJson();

        Assertions.assertTrue(openApiVersion(swagger).startsWith("3.1."));
        Assertions.assertTrue(swagger.contains("\"x-query-operation\""));
        Assertions.assertTrue(swagger.contains("application/x-www-form-urlencoded"));
    }

    @Test
    public void canCreateOpenApi30Swagger() {
        Thingifier t = new TodoListThingifierTestModel().get();

        ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.setThingifier(t);
        apiDefn.setVersion("1.0.1");

        String swagger = new Swaggerizer(apiDefn).asJson(OpenApiSpecificationVersion.OPENAPI_3_0);

        Assertions.assertTrue(openApiVersion(swagger).startsWith("3.0."));
        Assertions.assertTrue(swagger.contains("\"x-query-operation\""));
        Assertions.assertTrue(swagger.contains("application/x-www-form-urlencoded"));
    }

    @Test
    public void canCreateOpenApi32Swagger() {
        Thingifier t = new TodoListThingifierTestModel().get();

        ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.setThingifier(t);
        apiDefn.setVersion("1.0.1");

        String swagger = new Swaggerizer(apiDefn).asJson(OpenApiSpecificationVersion.OPENAPI_3_2);
        JsonObject document = JsonParser.parseString(swagger).getAsJsonObject();
        JsonObject todos = document.getAsJsonObject("paths").getAsJsonObject("/todos");
        JsonObject query = todos.getAsJsonObject("query");

        Assertions.assertEquals("3.2.0", openApiVersion(swagger));
        Assertions.assertTrue(todos.has("query"));
        Assertions.assertTrue(hasParameterNamed(todos.getAsJsonObject("get"), "_sortBy"));
        Assertions.assertTrue(hasParameterNamed(todos.getAsJsonObject("get"), "_limit"));
        Assertions.assertTrue(hasParameterNamed(todos.getAsJsonObject("get"), "_offset"));
        Assertions.assertTrue(hasParameterNamed(query, "_sortBy"));
        Assertions.assertTrue(hasParameterNamed(query, "_limit"));
        Assertions.assertTrue(hasParameterNamed(query, "_offset"));
        Assertions.assertFalse(swagger.contains("\"x-query-operation\""));
        Assertions.assertFalse(swagger.contains("\"x-http-method\""));
        Assertions.assertFalse(swagger.contains("\"x-query-content-types\""));
        Assertions.assertTrue(
                query.getAsJsonObject("requestBody")
                        .getAsJsonObject("content")
                        .has("application/x-www-form-urlencoded"));
    }

    private boolean hasParameterNamed(final JsonObject operation, final String name) {
        if (!operation.has("parameters")) {
            return false;
        }
        for (JsonElement parameterElement : operation.getAsJsonArray("parameters")) {
            JsonObject parameter = parameterElement.getAsJsonObject();
            if (name.equals(parameter.get("name").getAsString())) {
                return true;
            }
        }
        return false;
    }

    private String openApiVersion(final String swagger) {
        return JsonParser.parseString(swagger).getAsJsonObject().get("openapi").getAsString();
    }
}
