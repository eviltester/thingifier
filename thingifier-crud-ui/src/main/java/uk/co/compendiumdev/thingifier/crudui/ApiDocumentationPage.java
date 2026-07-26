package uk.co.compendiumdev.thingifier.crudui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.application.schema.definition.EntityDefinitionSpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.FieldDefinitionSpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.RelationshipDefinitionSpec;
import uk.co.compendiumdev.thingifier.application.schema.definition.ValidationRuleSpec;

public final class ApiDocumentationPage {

    private static final String API_PREFIX = "/api";

    private final WorkspaceSnapshot snapshot;

    public ApiDocumentationPage(final WorkspaceSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public String html() {
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("  <meta charset=\"utf-8\">\n");
        html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
        html.append("  <title>").append(escape(title())).append(" API Documentation</title>\n");
        html.append("  <link rel=\"icon\" href=\"/favicon.svg\" type=\"image/svg+xml\">\n");
        html.append("  <link rel=\"stylesheet\" href=\"/assets/styles.css\">\n");
        html.append("</head>\n");
        html.append("<body class=\"documentation-page\">\n");
        html.append("<main class=\"documentation-shell\">\n");
        appendHeader(html);
        appendEntities(html);
        appendRelationships(html);
        appendEndpoints(html);
        html.append("</main>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        return html.toString();
    }

    private void appendHeader(final StringBuilder html) {
        html.append("<header class=\"documentation-header\">\n");
        html.append("  <nav class=\"documentation-actions\">\n");
        html.append("    <a href=\"/\">Workspace</a>\n");
        html.append("    <a href=\"/swagger\">Swagger UI</a>\n");
        html.append("    <a href=\"/docs/swagger\">Download OpenAPI</a>\n");
        html.append("    <a href=\"/openapi.json\">Raw OpenAPI JSON</a>\n");
        html.append("    <a href=\"/openapi-3.1.json\">OpenAPI 3.1 JSON</a>\n");
        html.append("    <a href=\"/openapi-3.2.json\">OpenAPI 3.2 JSON</a>\n");
        html.append("    <a href=\"/openapi-3.0.json\">OpenAPI 3.0 JSON</a>\n");
        html.append("  </nav>\n");
        html.append("  <h1>").append(escape(title())).append(" API Documentation</h1>\n");
        if (!description().isEmpty()) {
            html.append("  <p>").append(escape(description())).append("</p>\n");
        }
        html.append("</header>\n");
    }

    private void appendEntities(final StringBuilder html) {
        html.append("<section class=\"documentation-section\">\n");
        html.append("  <h2>Entities</h2>\n");
        for (EntityDefinitionSpec entity : snapshot.definition().entities()) {
            html.append("  <article class=\"documentation-block\">\n");
            html.append("    <h3>")
                    .append(escape(entity.name()))
                    .append(" <span>")
                    .append(escape(entity.pluralName()))
                    .append("</span></h3>\n");
            html.append("    <table class=\"documentation-table\">\n");
            html.append(
                    "      <thead><tr><th>Field</th><th>Type</th><th>Rules</th></tr></thead>\n");
            html.append("      <tbody>\n");
            for (FieldDefinitionSpec field : entity.fields()) {
                html.append("        <tr><td>")
                        .append(escape(field.name()))
                        .append(
                                field.name().equals(entity.primaryKeyFieldName())
                                        ? " <b>primary</b>"
                                        : "")
                        .append("</td><td>")
                        .append(escape(field.type()))
                        .append("</td><td>")
                        .append(escape(rulesFor(field)))
                        .append("</td></tr>\n");
            }
            html.append("      </tbody>\n");
            html.append("    </table>\n");
            html.append("  </article>\n");
        }
        html.append("</section>\n");
    }

    private void appendRelationships(final StringBuilder html) {
        html.append("<section class=\"documentation-section\">\n");
        html.append("  <h2>Relationships</h2>\n");
        if (snapshot.definition().relationships().isEmpty()) {
            html.append("  <p>No relationships are defined.</p>\n");
        }
        for (RelationshipDefinitionSpec relationship : snapshot.definition().relationships()) {
            html.append("  <article class=\"documentation-block compact\">\n");
            html.append("    <h3>")
                    .append(escape(relationship.fromEntityName()))
                    .append(".")
                    .append(escape(relationship.name()))
                    .append(" -> ")
                    .append(escape(relationship.toEntityName()))
                    .append("</h3>\n");
            html.append("    <p>")
                    .append(escape(relationship.cardinality().canonicalName()))
                    .append(", ")
                    .append(escape(relationship.optionality()));
            if (relationship.hasReverse()) {
                html.append("; reverse ")
                        .append(escape(relationship.reverse().name()))
                        .append(" (")
                        .append(escape(relationship.reverse().cardinality().canonicalName()))
                        .append(", ")
                        .append(escape(relationship.reverse().optionality()))
                        .append(")");
            }
            html.append("</p>\n");
            html.append("  </article>\n");
        }
        html.append("</section>\n");
    }

    private void appendEndpoints(final StringBuilder html) {
        html.append("<section class=\"documentation-section\">\n");
        html.append("  <h2>Generated Endpoints</h2>\n");
        html.append("  <table class=\"documentation-table endpoints-table\">\n");
        html.append(
                "    <thead><tr><th>Method</th><th>Path</th><th>Result</th><th>Description</th></tr></thead>\n");
        html.append("    <tbody>\n");
        for (RoutingDefinition HttpRouteHandler : generatedRoutes()) {
            html.append("      <tr><td><code>")
                    .append(HttpRouteHandler.verb())
                    .append("</code></td><td><code>")
                    .append(escape(endpointPath(HttpRouteHandler)))
                    .append("</code></td><td>")
                    .append(escape(statusesFor(HttpRouteHandler)))
                    .append("</td><td>")
                    .append(escape(HttpRouteHandler.getDocumentation()))
                    .append("</td></tr>\n");
        }
        html.append("    </tbody>\n");
        html.append("  </table>\n");
        html.append("</section>\n");
    }

    private List<RoutingDefinition> generatedRoutes() {
        ApiRoutingDefinition routes =
                new ApiRoutingDefinitionDocGenerator(snapshot.thingifier()).generate("");
        List<RoutingDefinition> definitions = new ArrayList<>(routes.definitions());
        definitions.sort(
                Comparator.comparing(this::endpointPath)
                        .thenComparing(HttpRouteHandler -> HttpRouteHandler.verb().name()));
        return definitions;
    }

    private String endpointPath(final RoutingDefinition HttpRouteHandler) {
        return API_PREFIX + "/" + HttpRouteHandler.urlWithParamFormatter("{", "}");
    }

    private String statusesFor(final RoutingDefinition HttpRouteHandler) {
        if (!HttpRouteHandler.status().isReturnedFromCall()) {
            return HttpRouteHandler.status().value()
                    + " "
                    + HttpRouteHandler.status().description();
        }
        List<String> statuses = new ArrayList<>();
        for (RoutingStatus status : HttpRouteHandler.getPossibleStatusReponses()) {
            statuses.add(status.value() + " " + status.description());
        }
        return String.join(", ", statuses);
    }

    private String rulesFor(final FieldDefinitionSpec field) {
        List<String> rules = new ArrayList<>();
        if (field.required()) {
            rules.add("required");
        }
        if (field.unique()) {
            rules.add("unique");
        }
        if (field.defaultValue() != null) {
            rules.add("default " + field.defaultValue());
        }
        if (field.truncateTo() != null) {
            rules.add("truncate to " + field.truncateTo());
        }
        if (field.hasRange()) {
            rules.add(
                    "range "
                            + nullToWildcard(field.minValue())
                            + " to "
                            + nullToWildcard(field.maxValue()));
        }
        for (ValidationRuleSpec validation : field.validationRules()) {
            if (validation.value() == null) {
                rules.add(validation.name());
            } else {
                rules.add(validation.name() + " " + validation.value());
            }
        }
        return String.join(", ", rules);
    }

    private String nullToWildcard(final String value) {
        return value == null ? "*" : value;
    }

    private String title() {
        String title = snapshot.definition().title();
        return title == null || title.trim().isEmpty() ? "Thingifier" : title.trim();
    }

    private String description() {
        String description = snapshot.definition().description();
        return description == null ? "" : description.trim();
    }

    private String escape(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
