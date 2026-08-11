package uk.co.compendiumdev.thingifier.swaggerizer;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public final class ScalarUiPage {

    private static final String SCALAR_API_REFERENCE =
            "https://cdn.jsdelivr.net/npm/@scalar/api-reference";

    private final ThingifierApiDocumentationDefn apiDefn;
    private final DefaultGUIHTML guiManagement;
    private final String openApiUrl;
    private final String openApi30Url;
    private final String openApi31Url;
    private final String openApi32Url;
    private final String docsUrl;
    private final String canonicalUrl;

    public ScalarUiPage(
            final ThingifierApiDocumentationDefn apiDefn,
            final DefaultGUIHTML guiManagement,
            final String openApiUrl,
            final String openApi30Url,
            final String openApi31Url,
            final String openApi32Url,
            final String docsUrl,
            final String canonicalUrl) {
        this.apiDefn = apiDefn;
        this.guiManagement = guiManagement;
        this.openApiUrl = openApiUrl;
        this.openApi30Url = openApi30Url;
        this.openApi31Url = openApi31Url;
        this.openApi32Url = openApi32Url;
        this.docsUrl = docsUrl;
        this.canonicalUrl = canonicalUrl;
    }

    public String html() {
        final String title = resolveTitle();
        final StringBuilder html = new StringBuilder();

        html.append(guiManagement.getPageStart(title, headInject(), canonicalUrl));
        html.append(guiManagement.getMenuAsHTML());
        html.append(guiManagement.getStartOfMainContentMarker());
        html.append("<div class='scalar-ui-page'>");
        html.append("<h1>").append(escapeHtml(title)).append("</h1>");
        html.append("<p>");
        html.append("<a href='").append(escapeHtmlAttribute(docsUrl)).append("'>API Docs</a>");
        html.append(" | ");
        html.append("<a href='")
                .append(escapeHtmlAttribute(openApi31Url))
                .append("'>OpenAPI 3.1 JSON</a>");
        html.append(" | ");
        html.append("<a href='")
                .append(escapeHtmlAttribute(openApi32Url))
                .append("'>OpenAPI 3.2 JSON</a>");
        html.append(" | ");
        html.append("<a href='")
                .append(escapeHtmlAttribute(openApi30Url))
                .append("'>OpenAPI 3.0 JSON</a>");
        html.append("</p>");
        html.append("<div id='scalar-api-reference'></div>");
        html.append("<script src='")
                .append(SCALAR_API_REFERENCE)
                .append("' charset='UTF-8'></script>");
        html.append("<script>");
        html.append("window.onload = function () {");
        html.append("const scalarThingifierHost = window.location.hostname;");
        html.append("const scalarThingifierIsLocalHost = ");
        html.append("[\"localhost\", \"127.0.0.1\", \"0.0.0.0\", \"::1\"].includes(");
        html.append("scalarThingifierHost);");
        html.append("const scalarThingifierHideClientButton = ");
        html.append(apiDefn.scalarUi().willHideClientButton());
        html.append(";");
        html.append("Scalar.createApiReference(\"#scalar-api-reference\", {");
        html.append("sources: [");
        appendSource(html, openApiUrl, "OpenAPI 3.1 default", "openapi-3-1-default", true);
        html.append(",");
        appendSource(html, openApi31Url, "OpenAPI 3.1", "openapi-3-1", false);
        html.append(",");
        appendSource(html, openApi32Url, "OpenAPI 3.2", "openapi-3-2", false);
        html.append(",");
        appendSource(html, openApi30Url, "OpenAPI 3.0", "openapi-3-0", false);
        html.append("],");
        html.append("agent: {disabled: true},");
        html.append("hideClientButton: ");
        html.append("scalarThingifierHideClientButton || scalarThingifierIsLocalHost,");
        html.append("showDeveloperTools: ");
        html.append(apiDefn.scalarUi().willShowDeveloperTools() ? "\"always\"" : "\"never\"");
        html.append(",");
        html.append("theme: \"default\"");
        html.append("});");
        html.append("};");
        html.append("</script>");
        html.append("</div>");
        html.append(guiManagement.getEndOfMainContentMarker());
        html.append(guiManagement.getPageFooter());
        html.append(guiManagement.getPageEnd());
        return html.toString();
    }

    private void appendSource(
            final StringBuilder html,
            final String url,
            final String title,
            final String slug,
            final boolean defaultSource) {
        html.append("{");
        html.append("url: \"").append(escapeJavaScriptString(url)).append("\",");
        html.append("title: \"").append(escapeJavaScriptString(title)).append("\",");
        html.append("slug: \"").append(escapeJavaScriptString(slug)).append("\"");
        if (defaultSource) {
            html.append(",default: true");
        }
        html.append("}");
    }

    private String headInject() {
        return "<style>"
                + "html{background:#fff;}"
                + ".scalar-ui-page{background:#fff;color:#222;color-scheme:light;}"
                + ".scalar-ui-page h1{margin-bottom:.25em;}"
                + ".scalar-ui-page #scalar-api-reference{margin-top:1em;}"
                + "</style>";
    }

    private String resolveTitle() {
        final String configuredTitle = firstNonBlank(apiDefn.getSeoTitle(), apiDefn.getTitle());
        if (!configuredTitle.isEmpty()) {
            return configuredTitle + " Scalar UI";
        }
        final Thingifier thingifier = apiDefn.getThingifier();
        if (thingifier != null
                && thingifier.getTitle() != null
                && !thingifier.getTitle().trim().isEmpty()) {
            return thingifier.getTitle().trim() + " Scalar UI";
        }
        return "API Scalar UI";
    }

    private String firstNonBlank(final String preferred, final String fallback) {
        if (preferred != null && !preferred.trim().isEmpty()) {
            return preferred.trim();
        }
        if (fallback != null && !fallback.trim().isEmpty()) {
            return fallback.trim();
        }
        return "";
    }

    private String escapeHtml(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String escapeHtmlAttribute(final String value) {
        return escapeHtml(value).replace("'", "&#39;");
    }

    private String escapeJavaScriptString(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
