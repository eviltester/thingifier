package uk.co.compendiumdev.thingifier.swaggerizer;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public final class SwaggerUiPage {

    private static final String SWAGGER_UI_CSS = "https://unpkg.com/swagger-ui-dist/swagger-ui.css";
    private static final String SWAGGER_UI_BUNDLE =
            "https://unpkg.com/swagger-ui-dist/swagger-ui-bundle.js";
    private static final String SWAGGER_UI_PRESET =
            "https://unpkg.com/swagger-ui-dist/swagger-ui-standalone-preset.js";
    private static final String COPY_FOR_AI_CSS = "/css/swagger-copy-for-ai.css";
    private static final String COPY_FOR_AI_SCRIPT = "/js/swagger-copy-for-ai.js";

    private final ThingifierApiDocumentationDefn apiDefn;
    private final DefaultGUIHTML guiManagement;
    private final String openApiUrl;
    private final String openApi30Url;
    private final String openApi31Url;
    private final String openApi32Url;
    private final String docsUrl;
    private final String canonicalUrl;

    public SwaggerUiPage(
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
        html.append("<div class='swagger-ui-page'>");
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
        html.append("<div id='swagger-ui'></div>");
        html.append("<script src='")
                .append(SWAGGER_UI_BUNDLE)
                .append("' charset='UTF-8'></script>");
        html.append("<script src='")
                .append(SWAGGER_UI_PRESET)
                .append("' charset='UTF-8'></script>");
        html.append("<script>");
        html.append("window.onload = function () {");
        html.append("document.documentElement.classList.remove(\"dark-mode\");");
        html.append("window.ui = SwaggerUIBundle({");
        html.append("urls: [");
        html.append("{url: \"")
                .append(escapeJavaScriptString(openApiUrl))
                .append("\", name: \"OpenAPI 3.1 default\"},");
        html.append("{url: \"")
                .append(escapeJavaScriptString(openApi31Url))
                .append("\", name: \"OpenAPI 3.1\"},");
        html.append("{url: \"")
                .append(escapeJavaScriptString(openApi32Url))
                .append("\", name: \"OpenAPI 3.2\"},");
        html.append("{url: \"")
                .append(escapeJavaScriptString(openApi30Url))
                .append("\", name: \"OpenAPI 3.0\"}");
        html.append("],");
        html.append("\"urls.primaryName\": \"OpenAPI 3.1 default\",");
        html.append("dom_id: \"#swagger-ui\",");
        html.append("deepLinking: true,");
        html.append("syntaxHighlight: {activated: false},");
        html.append("presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],");
        html.append("plugins: [SwaggerUIBundle.plugins.DownloadUrl],");
        html.append("layout: \"StandaloneLayout\"");
        html.append("});");
        html.append("setTimeout(function () {");
        html.append("document.documentElement.classList.remove(\"dark-mode\");");
        html.append("}, 0);");
        html.append("};");
        html.append("</script>");
        html.append("<script>");
        html.append("window.thingifierSwaggerCopyForAi = {");
        html.append("openApiUrl: \"").append(escapeJavaScriptString(openApiUrl)).append("\"");
        html.append("};");
        html.append("</script>");
        html.append("<script src='")
                .append(COPY_FOR_AI_SCRIPT)
                .append("' charset='UTF-8'></script>");
        html.append("</div>");
        html.append(guiManagement.getEndOfMainContentMarker());
        html.append(guiManagement.getPageFooter());
        html.append(guiManagement.getPageEnd());
        return html.toString();
    }

    private String headInject() {
        return "<link rel='stylesheet' href='"
                + SWAGGER_UI_CSS
                + "'>"
                + "<link rel='stylesheet' href='"
                + COPY_FOR_AI_CSS
                + "'>"
                + "<style>"
                + lightThemeCss()
                + "</style>";
    }

    private String lightThemeCss() {
        return String.join(
                "",
                "html{background:#fff;}",
                ".swagger-ui-page{background:#fff;color:#222;color-scheme:light;}",
                ".swagger-ui-page h1{margin-bottom:.25em;}",
                ".swagger-ui-page #swagger-ui{margin-top:1em;}",
                ".swagger-ui{background:#fff;color:#222;color-scheme:light;}",
                ".swagger-ui .topbar{background:#f7f7f7!important;border:1px solid #d7d7d7;"
                        + "border-radius:4px;margin:1em 0;padding:8px 0;}",
                ".swagger-ui .topbar-wrapper{align-items:center;}",
                ".swagger-ui .topbar a span{color:#222!important;}",
                ".swagger-ui .wrapper,.swagger-ui .scheme-container,"
                        + ".swagger-ui section.models,.swagger-ui .model-box,"
                        + ".swagger-ui .opblock,.swagger-ui .opblock .opblock-summary,"
                        + ".swagger-ui .opblock .opblock-section-header,"
                        + ".swagger-ui .responses-inner,.swagger-ui table,"
                        + ".swagger-ui .dialog-ux .modal-ux{background:#fff!important;"
                        + "color:#222!important;}",
                ".swagger-ui .scheme-container{box-shadow:none!important;"
                        + "border:1px solid #d7d7d7;}",
                ".swagger-ui .opblock .opblock-section-header{box-shadow:none!important;"
                        + "border-bottom:1px solid #d7d7d7;}",
                ".swagger-ui .info .title,.swagger-ui .opblock-tag,"
                        + ".swagger-ui .opblock-summary-description,"
                        + ".swagger-ui .markdown p,.swagger-ui .markdown li,"
                        + ".swagger-ui .renderedMarkdown p,.swagger-ui .renderedMarkdown li,"
                        + ".swagger-ui table thead tr td,.swagger-ui table thead tr th,"
                        + ".swagger-ui table tbody tr td,.swagger-ui label,"
                        + ".swagger-ui .parameter__name,.swagger-ui .parameter__type,"
                        + ".swagger-ui .response-col_status,.swagger-ui .response-col_description,"
                        + ".swagger-ui .model-title,.swagger-ui .model{color:#222!important;}",
                ".swagger-ui a,.swagger-ui .link,.swagger-ui .opblock-tag:hover{"
                        + "color:#1f5f8b!important;}",
                ".swagger-ui input,.swagger-ui textarea,.swagger-ui select{background:#fff!important;"
                        + "color:#222!important;border-color:#8b8b8b!important;}",
                ".swagger-ui .btn,.swagger-ui button{background:#fff!important;"
                        + "color:#222!important;border-color:#777!important;}",
                ".swagger-ui .authorization__btn{display:inline-flex!important;"
                        + "align-items:center!important;justify-content:center!important;"
                        + "min-width:32px!important;height:28px!important;margin:0 8px!important;"
                        + "border:1px solid #1f5f8b!important;border-radius:4px!important;"
                        + "background:#eef7fc!important;color:#1f5f8b!important;}",
                ".swagger-ui .authorization__btn svg{display:block!important;"
                        + "width:18px!important;height:18px!important;fill:#1f5f8b!important;}",
                ".swagger-ui .authorization__btn svg path{fill:#1f5f8b!important;}",
                ".swagger-ui .highlight-code,.swagger-ui .microlight{background:#f7f7f7!important;"
                        + "color:#222!important;}");
    }

    private String resolveTitle() {
        final String swaggerUiTitle = firstNonBlank(apiDefn.getSwaggerUiTitle(), "");
        if (!swaggerUiTitle.isEmpty()) {
            return swaggerUiTitle;
        }

        final String configuredTitle = firstNonBlank(apiDefn.getSeoTitle(), apiDefn.getTitle());
        if (!configuredTitle.isEmpty()) {
            return configuredTitle + " Swagger UI";
        }
        final Thingifier thingifier = apiDefn.getThingifier();
        if (thingifier != null
                && thingifier.getTitle() != null
                && !thingifier.getTitle().trim().isEmpty()) {
            return thingifier.getTitle().trim() + " Swagger UI";
        }
        return "API Swagger UI";
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
