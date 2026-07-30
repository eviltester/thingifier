package uk.co.compendiumdev.thingifier.htmlgui.htmlgen;

import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.PARTIAL_JSON_UPDATE;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

class RestApiDocumentationGeneratorTest {

    @Test
    void apiDocumentationShowsSwaggerUiLinkByDefault() {
        final Thingifier thingifier = new Thingifier();
        final ThingifierApiDocumentationDefn apiDocDefn = new ThingifierApiDocumentationDefn();

        final String docs =
                new RestApiDocumentationGenerator(thingifier, new DefaultGUIHTML())
                        .getApiDocumentation(
                                new ApiRoutingDefinition(),
                                List.of(),
                                apiDocDefn,
                                "/mirror",
                                "https://example.com/mirror/docs");

        Assertions.assertTrue(docs.contains("href='/mirror/docs/swagger-ui'"));
        Assertions.assertTrue(docs.contains("Open Swagger UI"));
        Assertions.assertTrue(docs.contains("<li>OpenAPI v 3.0 JSON"));
        Assertions.assertTrue(docs.contains("<li>OpenAPI v 3.1 JSON"));
        Assertions.assertTrue(docs.contains("<li>OpenAPI v 3.2 JSON"));
        Assertions.assertTrue(
                docs.contains("href='/mirror/docs/openapi-3.2.json'>[standard validation]</a>"));
        Assertions.assertTrue(docs.contains("href='/mirror/docs/openapi-3.2.json?download'"));
        Assertions.assertTrue(docs.contains("href='/mirror/docs/openapi-3.2.json?permissive'"));
        Assertions.assertTrue(
                docs.contains("href='/mirror/docs/openapi-3.2.json?permissive&amp;download'"));
        Assertions.assertFalse(docs.contains("download normal swagger file"));
        Assertions.assertFalse(docs.contains("download swagger file with less validation"));
        Assertions.assertFalse(docs.contains("Add <code>?download</code>"));
        Assertions.assertFalse(docs.contains("class='mermaid'"));
        Assertions.assertFalse(docs.contains("mermaid.esm.min.mjs"));
    }

    @Test
    void apiDocumentationCanHideSwaggerUiLink() {
        final Thingifier thingifier = new Thingifier();
        final ThingifierApiDocumentationDefn apiDocDefn =
                new ThingifierApiDocumentationDefn().setShowSwaggerUiLink(false);

        final String docs =
                new RestApiDocumentationGenerator(thingifier, new DefaultGUIHTML())
                        .getApiDocumentation(
                                new ApiRoutingDefinition(),
                                List.of(),
                                apiDocDefn,
                                "/mirror",
                                "https://example.com/mirror/docs");

        Assertions.assertFalse(docs.contains("href='/mirror/docs/swagger-ui'"));
        Assertions.assertFalse(docs.contains("Open Swagger UI"));
        Assertions.assertFalse(docs.contains("href='/mirror/docs/swagger'"));
        Assertions.assertTrue(docs.contains("<li>OpenAPI v 3.0 JSON"));
    }

    @Test
    void apiDocumentationShowsEntityDescriptions() {
        final Thingifier thingifier = new Thingifier();
        thingifier.setDocumentation("Task API", "Task API docs.");
        final EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.withDescription("A task & project <summary>.");
        task.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        task.addField(
                Field.is("done", FieldType.BOOLEAN)
                        .withDescription("Whether the task has been completed."));

        final String docs =
                new RestApiDocumentationGenerator(thingifier, new DefaultGUIHTML())
                        .getApiDocumentation(
                                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api"),
                                List.of(),
                                new ThingifierApiDocumentationDefn(),
                                "/api",
                                "https://example.com/api/docs");

        Assertions.assertTrue(docs.contains("<h4>task</h4>"));
        Assertions.assertTrue(docs.contains("<p>A task &amp; project &lt;summary&gt;.</p>"));
        Assertions.assertTrue(docs.contains("<td>Description</td>"));
        Assertions.assertFalse(docs.contains("<td>Validation</td>"));
        Assertions.assertTrue(
                docs.contains(
                        "<li>Whether the task has been completed.</li>\n"
                                + "<li>Value must be a Boolean (true, false) value</li>"));
    }

    @Test
    void apiDocumentationShowsConfiguredPatchInstanceRouteAsSupported() {
        final Thingifier thingifier = new Thingifier();
        thingifier.setDocumentation("Task API", "Task API docs.");
        final EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        task.addField(Field.is("title", FieldType.STRING));
        thingifier.apiConfig().writeMethods().entities().patchCan(PARTIAL_JSON_UPDATE);

        final String docs =
                new RestApiDocumentationGenerator(thingifier, new DefaultGUIHTML())
                        .getApiDocumentation(
                                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api"),
                                List.of(),
                                new ThingifierApiDocumentationDefn(),
                                "/api",
                                "https://example.com/api/docs");

        Assertions.assertTrue(docs.contains("<strong>PATCH /api/tasks/:id</strong>"));
        Assertions.assertTrue(
                docs.contains(
                        "patch a specific instance of task with a body containing the patch"
                                + " details"));
        Assertions.assertFalse(
                docs.contains(
                        "<strong>PATCH /api/tasks/:id</strong><ul><li class='normal'>method not"
                                + " allowed</li></ul>"));
    }

    @Test
    void apiDocumentationShowsTwoWayRelationshipsAsSeparateDirections() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition project = thingifier.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        project.addField(Field.is("title", FieldType.STRING));
        final EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        task.addField(Field.is("title", FieldType.STRING));
        thingifier
                .defineRelationship(project, task, "tasks", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_ONE(), "project");

        final String docs =
                new RestApiDocumentationGenerator(thingifier, new DefaultGUIHTML())
                        .getApiDocumentation(
                                new ApiRoutingDefinitionDocGenerator(thingifier).generate(""),
                                List.of(),
                                new ThingifierApiDocumentationDefn(),
                                "",
                                "https://example.com/docs");

        Assertions.assertTrue(docs.contains("<li>tasks : project =(tasks, max *)=> task</li>"));
        Assertions.assertTrue(docs.contains("<li>project : task =(project, max 1)=> project</li>"));
        Assertions.assertFalse(docs.contains("tasks/project :"));
        Assertions.assertTrue(docs.contains("<pre class='mermaid'>\nerDiagram"));
        Assertions.assertTrue(docs.contains("    PROJECT ||--o{ TASK : tasks\n"));
        Assertions.assertTrue(docs.contains("<script type='module'>"));
        Assertions.assertTrue(
                docs.contains("https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.esm.min.mjs"));
        Assertions.assertTrue(docs.contains("mermaid.initialize({ startOnLoad: true });"));
    }

    @Test
    void apiDocumentationUsesMirroredMermaidCardinalityMarkers() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition tag = thingifier.defineThing("tag", "tags");
        tag.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        tag.addField(Field.is("name", FieldType.STRING));
        final EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        task.addField(Field.is("title", FieldType.STRING));
        thingifier.defineRelationship(tag, task, "tasks", Cardinality.ZERO_TO_MANY());

        final String docs =
                new RestApiDocumentationGenerator(thingifier, new DefaultGUIHTML())
                        .getApiDocumentation(
                                new ApiRoutingDefinitionDocGenerator(thingifier).generate(""),
                                List.of(),
                                new ThingifierApiDocumentationDefn(),
                                "",
                                "https://example.com/docs");

        Assertions.assertTrue(docs.contains("    TAG |o--o{ TASK : tasks\n"));
    }

    @Test
    void apiDocumentationOmitsHiddenAndDisabledGeneratedRoutes() {
        final Thingifier thingifier = new Thingifier();
        thingifier.setDocumentation("Task API", "Task API docs.");
        final EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        task.addField(Field.is("title", FieldType.STRING));
        thingifier.apiSpec().route(RoutingVerb.POST, "/api/tasks").disable();
        thingifier.apiSpec().route(RoutingVerb.PUT, "/api/tasks/{id}").hide();

        final String docs =
                new RestApiDocumentationGenerator(thingifier, new DefaultGUIHTML())
                        .getApiDocumentation(
                                new ApiRoutingDefinitionDocGenerator(thingifier).generate("/api"),
                                List.of(),
                                new ThingifierApiDocumentationDefn(),
                                "/api",
                                "https://example.com/api/docs");

        Assertions.assertTrue(docs.contains("<strong>GET /api/tasks</strong>"));
        Assertions.assertFalse(docs.contains("<strong>POST /api/tasks</strong>"));
        Assertions.assertFalse(docs.contains("<strong>PUT /api/tasks/:id</strong>"));
    }
}
