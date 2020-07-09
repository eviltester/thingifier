package uk.co.compendiumdev.thingifier.htmlgui;

public class DefaultGUIHTMLRootMenu {
    public String getMenuAsHTML() {
        StringBuilder html = new StringBuilder();
        html.append("<div class='rootmenu'>");
        html.append("<ul>");
        html.append("<li><a href='/'>API documentation</a></li>");
        html.append("<li><a href='/gui/entities'>Entities Explorer</a></li>");
        html.append("</ul>");
        html.append("</div>");
        return html.toString();
    }
}
