package uk.co.compendiumdev.thingifier.crudui;

import java.util.Locale;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.swaggerizer.Swaggerizer;

public final class OpenApiDocumentation {

    private static final String API_SERVER_URL = "/api";

    private final WorkspaceSnapshot snapshot;

    public OpenApiDocumentation(final WorkspaceSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public String openApiJson() {
        return new Swaggerizer(apiDefinition()).asJson();
    }

    public String permissiveOpenApiJson() {
        return new Swaggerizer(apiDefinition()).asJson(true);
    }

    public String downloadFilename(final boolean permissive) {
        String suffix = permissive ? "-permissive-openapi.json" : "-openapi.json";
        return filenameBase() + suffix;
    }

    private ThingifierApiDocumentationDefn apiDefinition() {
        ThingifierApiDocumentationDefn apiDefinition = new ThingifierApiDocumentationDefn();
        apiDefinition.setThingifier(snapshot.thingifier());
        apiDefinition.setTitle(title());
        apiDefinition.setDescription(description());
        apiDefinition.setVersion("1.0.0");
        apiDefinition.setPathPrefix("");
        apiDefinition.addServer(API_SERVER_URL, "Current CRUD UI workspace API");
        return apiDefinition;
    }

    private String title() {
        String title = snapshot.definition().title();
        return title == null || title.trim().isEmpty() ? "Thingifier API" : title.trim();
    }

    private String description() {
        String description = snapshot.definition().description();
        return description == null ? "" : description;
    }

    private String filenameBase() {
        String candidate = title().replaceAll("[^A-Za-z0-9]+", "-");
        candidate = candidate.replaceAll("(^-+|-+$)", "");
        if (candidate.isEmpty()) {
            return "thingifier";
        }
        return candidate.toLowerCase(Locale.ROOT);
    }
}
