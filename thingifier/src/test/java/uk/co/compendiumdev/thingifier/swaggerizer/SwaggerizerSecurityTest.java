package uk.co.compendiumdev.thingifier.swaggerizer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

class SwaggerizerSecurityTest {

    @Test
    void bearerSecuredRoutesAddHttpBearerSecuritySchemeAndOperationRequirement() {
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.POST,
                                "/shop/checkout/:cartId",
                                RoutingStatus.returnedFromCall(),
                                null)
                        .addDocumentation("checkout a cart")
                        .addPossibleStatuses(200, 401, 403)
                        .secureWithBearerAuth());

        final OpenAPI openApi = new Swaggerizer(apiDefn).swagger();

        final SecurityScheme bearerAuth =
                openApi.getComponents().getSecuritySchemes().get("bearerAuth");
        Assertions.assertNotNull(bearerAuth);
        Assertions.assertEquals(SecurityScheme.Type.HTTP, bearerAuth.getType());
        Assertions.assertEquals("bearer", bearerAuth.getScheme());
        Assertions.assertNull(bearerAuth.getBearerFormat());
        Assertions.assertEquals(
                "bearerAuth",
                openApi.getPaths()
                        .get("/shop/checkout/{cartId}")
                        .getPost()
                        .getSecurity()
                        .get(0)
                        .keySet()
                        .iterator()
                        .next());
    }

    @Test
    void basicSecuredRoutesAddHttpBasicSecuritySchemeAndOperationRequirement() {
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.POST,
                                "/shop/login",
                                RoutingStatus.returnedFromCall(),
                                null)
                        .addDocumentation("login")
                        .addPossibleStatuses(200, 401, 403)
                        .secureWithBasicAuth());

        final OpenAPI openApi = new Swaggerizer(apiDefn).swagger();

        final SecurityScheme basicAuth =
                openApi.getComponents().getSecuritySchemes().get("basicAuth");
        Assertions.assertNotNull(basicAuth);
        Assertions.assertEquals(SecurityScheme.Type.HTTP, basicAuth.getType());
        Assertions.assertEquals("basic", basicAuth.getScheme());
        Assertions.assertNull(basicAuth.getBearerFormat());
        Assertions.assertEquals(
                "basicAuth",
                openApi.getPaths()
                        .get("/shop/login")
                        .getPost()
                        .getSecurity()
                        .get(0)
                        .keySet()
                        .iterator()
                        .next());
    }

    @Test
    void apiKeySecuredRoutesAddHeaderApiKeySecuritySchemeAndOperationRequirement() {
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.GET,
                                "/secret/token",
                                RoutingStatus.returnedFromCall(),
                                null)
                        .addDocumentation("read a token")
                        .addPossibleStatuses(200, 401, 403)
                        .secureWithApiKey("authToken", "X-AUTH-TOKEN"));

        final OpenAPI openApi = new Swaggerizer(apiDefn).swagger();

        final SecurityScheme apiKeyAuth =
                openApi.getComponents().getSecuritySchemes().get("authToken");
        Assertions.assertNotNull(apiKeyAuth);
        Assertions.assertEquals(SecurityScheme.Type.APIKEY, apiKeyAuth.getType());
        Assertions.assertEquals(SecurityScheme.In.HEADER, apiKeyAuth.getIn());
        Assertions.assertEquals("X-AUTH-TOKEN", apiKeyAuth.getName());
        Assertions.assertEquals(
                "authToken",
                openApi.getPaths()
                        .get("/secret/token")
                        .getGet()
                        .getSecurity()
                        .get(0)
                        .keySet()
                        .iterator()
                        .next());
    }

    @Test
    void alternativeAuthRendersOpenApiSecurityRequirementsInDeclarationOrder() {
        final Thingifier thingifier = relationshipModel();
        thingifier.apiSpec().security().bearer("secretBearer");
        thingifier.apiSpec().security().apiKey("secretApiKey", "X-AUTH-TOKEN");
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/api/todos")
                .secureWithAnyOf("secretBearer", "secretApiKey");
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.setThingifier(thingifier);
        apiDefn.setPathPrefix("/api");

        final OpenAPI openApi = new Swaggerizer(apiDefn).swagger();

        final SecurityScheme bearer =
                openApi.getComponents().getSecuritySchemes().get("secretBearer");
        final SecurityScheme apiKey =
                openApi.getComponents().getSecuritySchemes().get("secretApiKey");
        Assertions.assertEquals(SecurityScheme.Type.HTTP, bearer.getType());
        Assertions.assertEquals("bearer", bearer.getScheme());
        Assertions.assertEquals(SecurityScheme.Type.APIKEY, apiKey.getType());
        Assertions.assertEquals("X-AUTH-TOKEN", apiKey.getName());
        Assertions.assertEquals(
                "secretBearer",
                openApi.getPaths()
                        .get("/api/todos")
                        .getPost()
                        .getSecurity()
                        .get(0)
                        .keySet()
                        .iterator()
                        .next());
        Assertions.assertEquals(
                "secretApiKey",
                openApi.getPaths()
                        .get("/api/todos")
                        .getPost()
                        .getSecurity()
                        .get(1)
                        .keySet()
                        .iterator()
                        .next());
    }

    @Test
    void bearerSecuredGeneratedRoutesAddOperationRequirement() {
        final Thingifier thingifier = relationshipModel();
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/api/projects/{projectId}/tasks")
                .secureWithBearerAuth();
        thingifier.apiSpec().route(RoutingVerb.POST, "/api/todos").disable();

        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.setThingifier(thingifier);
        apiDefn.setPathPrefix("/api");

        final OpenAPI openApi = new Swaggerizer(apiDefn).swagger();

        Assertions.assertEquals(
                "bearerAuth",
                openApi.getPaths()
                        .get("/api/projects/{id}/tasks")
                        .getPost()
                        .getSecurity()
                        .get(0)
                        .keySet()
                        .iterator()
                        .next());
        Assertions.assertNull(openApi.getPaths().get("/api/todos").getPost());
    }

    private Thingifier relationshipModel() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition project = thingifier.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        project.addField(Field.is("title", FieldType.STRING));
        final EntityDefinition todo = thingifier.defineThing("todo", "todos");
        todo.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        todo.addField(Field.is("title", FieldType.STRING));
        thingifier
                .defineRelationship(project, todo, "tasks", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_ONE(), "project");
        return thingifier;
    }
}
