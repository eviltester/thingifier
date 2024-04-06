package uk.co.compendiumdev.challenge.gui;

import spark.Response;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public class PageNotFoundResponse {
    private final DefaultGUIHTML guiManagement;

    public PageNotFoundResponse(DefaultGUIHTML guiManagement) {
        this.guiManagement = guiManagement;
    }

    public void amendResponse(Response response, String bodyStringAppend) {
        response.status(404);
        response.type("text/html");
        StringBuilder html = new StringBuilder();
        html.append(guiManagement.getPageStart("404 Not Found","", ""));
        html.append(guiManagement.getMenuAsHTML());
        html.append(guiManagement.getStartOfMainContentMarker());
        html.append("<h1>Page Not Found</h1>");
        html.append(bodyStringAppend);
        html.append(guiManagement.getEndOfMainContentMarker());
        html.append(guiManagement.getPageFooter());
        html.append(guiManagement.getPageEnd());
        response.type("text/html");
        response.body(html.toString());
    }
}
