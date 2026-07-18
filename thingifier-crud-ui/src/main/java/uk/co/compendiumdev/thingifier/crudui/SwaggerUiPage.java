package uk.co.compendiumdev.thingifier.crudui;

public final class SwaggerUiPage {

    private final WorkspaceSnapshot snapshot;

    public SwaggerUiPage(final WorkspaceSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public String html() {
        String title = title();
        return "<!doctype html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "  <meta charset=\"utf-8\">\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
                + "  <title>"
                + escape(title)
                + " Swagger UI</title>\n"
                + "  <link rel=\"icon\" href=\"/favicon.svg\" type=\"image/svg+xml\">\n"
                + "  <link rel=\"stylesheet\" href=\"https://unpkg.com/swagger-ui-dist/swagger-ui.css\">\n"
                + "  <link rel=\"stylesheet\" href=\"/css/swagger-copy-for-ai.css\">\n"
                + "  <style>body{margin:0;background:#fafafa}.swagger-topbar{height:44px;display:flex;align-items:center;gap:12px;padding:0 14px;background:#1f2733;color:#fff;font-family:Arial,Helvetica,sans-serif}.swagger-topbar a{color:#fff}.swagger-title{font-weight:700}</style>\n"
                + "</head>\n"
                + "<body>\n"
                + "<div class=\"swagger-topbar\"><span class=\"swagger-title\">"
                + escape(title)
                + "</span><a href=\"/\">Workspace</a><a href=\"/docs\">API Docs</a><a href=\"/docs/swagger\">Download OpenAPI</a></div>\n"
                + "<div id=\"swagger-ui\"></div>\n"
                + "<script src=\"https://unpkg.com/swagger-ui-dist/swagger-ui-bundle.js\" charset=\"UTF-8\"></script>\n"
                + "<script src=\"https://unpkg.com/swagger-ui-dist/swagger-ui-standalone-preset.js\" charset=\"UTF-8\"></script>\n"
                + "<script>\n"
                + "window.onload = function () {\n"
                + "  window.ui = SwaggerUIBundle({\n"
                + "    url: \"/openapi.json\",\n"
                + "    dom_id: \"#swagger-ui\",\n"
                + "    deepLinking: true,\n"
                + "    presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],\n"
                + "    plugins: [SwaggerUIBundle.plugins.DownloadUrl],\n"
                + "    layout: \"StandaloneLayout\"\n"
                + "  });\n"
                + "};\n"
                + "</script>\n"
                + "<script>window.thingifierSwaggerCopyForAi = { openApiUrl: \"/openapi.json\" };</script>\n"
                + "<script src=\"/js/swagger-copy-for-ai.js\" charset=\"UTF-8\"></script>\n"
                + "</body>\n"
                + "</html>\n";
    }

    private String title() {
        String title = snapshot.definition().title();
        return title == null || title.trim().isEmpty() ? "Thingifier" : title.trim();
    }

    private String escape(final String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
