package uk.co.compendiumdev.thingifier.api.spec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

class ThingifierApiSpecTest {

    @Test
    void emptyApiSpecLeavesGeneratedRoutesUnchanged() {
        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(model()).generate("/api");

        final RoutingDefinition route = route(definition, RoutingVerb.POST, "api/todos");
        Assertions.assertFalse(route.isDisabled());
        Assertions.assertFalse(route.isHiddenFromDocumentation());
        Assertions.assertFalse(route.isSecuredByBearerAuth());
        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, POST, QUERY",
                route(definition, RoutingVerb.OPTIONS, "api/todos").headerValue());
    }

    @Test
    void apiSpecCanSecureAndOverrideGeneratedRouteDocumentation() {
        final Thingifier thingifier = model();
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/api/projects/{projectId}/tasks")
                .secureWithBearerAuth()
                .addDocumentation("create a task through the project API")
                .requestPayload("create_todo");

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api");
        final RoutingDefinition route =
                route(definition, RoutingVerb.POST, "api/projects/:id/tasks");

        Assertions.assertTrue(route.isSecuredByBearerAuth());
        Assertions.assertEquals("create a task through the project API", route.getDocumentation());
        Assertions.assertEquals("create_todo", route.getRequestPayload());
    }

    @Test
    void disabledGeneratedRouteIsHiddenAndRemovedFromOptions() {
        final Thingifier thingifier = model();
        thingifier.apiSpec().route(RoutingVerb.POST, "/api/todos").disable();

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api");

        final RoutingDefinition post = route(definition, RoutingVerb.POST, "api/todos");
        Assertions.assertTrue(post.isDisabled());
        Assertions.assertTrue(post.isHiddenFromDocumentation());
        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, QUERY",
                route(definition, RoutingVerb.OPTIONS, "api/todos").headerValue());
    }

    @Test
    void disabledGeneratedRouteReturns404FromInternalApi() {
        final Thingifier thingifier = model();
        thingifier.apiConfig().setFrom(new ThingifierApiConfig("/api"));
        thingifier.apiSpec().route(RoutingVerb.POST, "/api/todos").disable();

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).post(new HttpApiRequest("/api/todos"));

        Assertions.assertEquals(404, response.getStatusCode());
    }

    @Test
    void apiSpecCanDisableAllRoutesForAnEntity() {
        final Thingifier thingifier = model();
        thingifier.apiSpec().disableEntityRoutes("/api/todos");

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api");

        Assertions.assertTrue(route(definition, RoutingVerb.GET, "api/todos").isDisabled());
        Assertions.assertTrue(route(definition, RoutingVerb.POST, "api/todos").isDisabled());
        Assertions.assertTrue(route(definition, RoutingVerb.GET, "api/todos/:id").isDisabled());
        Assertions.assertTrue(route(definition, RoutingVerb.PUT, "api/todos/:id").isDisabled());
    }

    @Test
    void apiSpecCanDisableAllRoutesForARelationship() {
        final Thingifier thingifier = model();
        thingifier.apiSpec().disableRelationshipRoutes("/api/projects", "tasks");

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api");

        Assertions.assertTrue(
                route(definition, RoutingVerb.GET, "api/projects/:id/tasks").isDisabled());
        Assertions.assertTrue(
                route(definition, RoutingVerb.POST, "api/projects/:id/tasks").isDisabled());
        Assertions.assertTrue(
                route(definition, RoutingVerb.DELETE, "api/projects/:id/tasks/:relatedId")
                        .isDisabled());
        Assertions.assertTrue(
                route(definition, RoutingVerb.OPTIONS, "api/projects/:id/tasks/:relatedId")
                        .isDisabled());
    }

    @Test
    void apiSpecCanHideAllRoutesForARelationshipWithoutDisablingThem() {
        final Thingifier thingifier = model();
        thingifier.apiSpec().hideRelationshipRoutes("/api/projects", "tasks");

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api");

        final RoutingDefinition get = route(definition, RoutingVerb.GET, "api/projects/:id/tasks");
        Assertions.assertTrue(get.isHiddenFromDocumentation());
        Assertions.assertFalse(get.isDisabled());

        final RoutingDefinition delete =
                route(definition, RoutingVerb.DELETE, "api/projects/:id/tasks/:relatedId");
        Assertions.assertTrue(delete.isHiddenFromDocumentation());
        Assertions.assertFalse(delete.isDisabled());
    }

    @Test
    void apiSpecCanBindEntityViewsToGeneratedRoutes() {
        final Thingifier thingifier = viewModel();
        thingifier.apiSpec().route(RoutingVerb.POST, "/api/items").entityView("PublicItem");

        final ApiRoutingDefinition definition =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api");
        final RoutingDefinition route = route(definition, RoutingVerb.POST, "api/items");

        Assertions.assertEquals("create_PublicItem", route.getRequestPayload());
        Assertions.assertEquals("PublicItem", route.getReturnPayloadFor(201));
        Assertions.assertTrue(route.hasRequestEntityView());
        Assertions.assertEquals("PublicItem", route.getResponseEntityViewFor(201));
    }

    @Test
    void entityViewsFilterResponsesAndRejectDisallowedInputFieldsAtRuntime() {
        final Thingifier thingifier = viewModel();
        thingifier.apiConfig().setFrom(new ThingifierApiConfig("/api"));
        thingifier.apiSpec().route(RoutingVerb.POST, "/api/items").entityView("PublicItem");
        final ThingifierHttpApi api = new ThingifierHttpApi(thingifier);

        final HttpApiResponse createResponse =
                api.post(
                        jsonRequest(
                                "/api/items",
                                "{\"name\":\"visible\",\"secret\":\"stored but hidden\"}"));

        Assertions.assertEquals(201, createResponse.getStatusCode());
        Assertions.assertTrue(createResponse.getBody().contains("visible"));
        Assertions.assertFalse(createResponse.getBody().contains("secret"));
        Assertions.assertFalse(createResponse.getBody().contains("stored but hidden"));

        final HttpApiResponse getResponse = api.get(new HttpApiRequest("/api/items/1"));
        Assertions.assertEquals(200, getResponse.getStatusCode());
        Assertions.assertTrue(getResponse.getBody().contains("stored but hidden"));

        final HttpApiResponse disallowedResponse =
                api.post(
                        jsonRequest("/api/items", "{\"name\":\"visible\",\"forbidden\":\"nope\"}"));

        Assertions.assertEquals(422, disallowedResponse.getStatusCode());
        Assertions.assertTrue(disallowedResponse.getBody().contains("forbidden"));
    }

    private Thingifier model() {
        Thingifier thingifier = new Thingifier();

        EntityDefinition project = thingifier.defineThing("project", "projects", 5);
        project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        project.addField(Field.is("title", FieldType.STRING));

        EntityDefinition todo = thingifier.defineThing("todo", "todos", 5);
        todo.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        todo.addField(Field.is("title", FieldType.STRING).makeMandatory());

        thingifier
                .defineRelationship(project, todo, "tasks", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_ONE(), "project");

        return thingifier;
    }

    private Thingifier viewModel() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition item = thingifier.defineThing("item", "items", 5);
        item.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        item.addField(Field.is("name", FieldType.STRING).makeMandatory());
        item.addField(Field.is("secret", FieldType.STRING));
        item.addField(Field.is("forbidden", FieldType.STRING));
        item.defineView("PublicItem")
                .hideRequestFields("secret")
                .hideResponseFields("secret")
                .disallowInputFields("forbidden");
        return thingifier;
    }

    private HttpApiRequest jsonRequest(final String path, final String body) {
        return new HttpApiRequest(path)
                .setVerb("POST")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .setBody(body);
    }

    private RoutingDefinition route(
            final ApiRoutingDefinition definition, final RoutingVerb verb, final String url) {
        return definition.definitions().stream()
                .filter(route -> route.verb() == verb)
                .filter(route -> route.url().equals(url))
                .findFirst()
                .orElseThrow();
    }
}
