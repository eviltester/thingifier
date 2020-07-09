package uk.co.compendiumdev.thingifier.htmlgui;

import uk.co.compendiumdev.thingifier.application.ThingifierVersionDetails;

public class DefaultGUIHTML {

    public String getPageStart(final String title){
        StringBuilder html = new StringBuilder();
        html.append("<html><head>");
        html.append("<title>" + title + "</title>");
        html.append(" <link rel='stylesheet' href='/css/default.css'>");
        html.append("</head><body>");
        html.append("<div class='content'>");
        return html.toString();
    }

    public String getMenuAsHTML() {
        StringBuilder html = new StringBuilder();
        html.append("<div class='rootmenu menu'>");
        html.append("<ul>");
        html.append("<li><a href='/docs'>API documentation</a></li>");
        html.append("<li><a href='/gui/entities'>Entities Explorer</a></li>");
        html.append("</ul>");
        html.append("</div>");
        return html.toString();
    }

    public String getPageFooter(){
        StringBuilder html = new StringBuilder();
        html.append("<br/><hr/>");
        html.append("<div class='footer'>");
        html.append(paragraph(
                String.format(
                        "Thingifier version %s, Copyright Alan Richardson, Compendium Developments Ltd %s ",
                        ThingifierVersionDetails.VERSION_NUMBER,
                        ThingifierVersionDetails.COPYRIGHT_YEAR)));
        html.append("<ul class='footerlinks'>");
        html.append(li(href("Thingifier", "https://github.com/eviltester/thingifier")));
        html.append(li(href("EvilTester.com", "http://eviltester.com")));
        html.append(li(href("Compendium Developments", "https://compendiumdev.co.uk")));
        html.append("</ul>");
        html.append("</div>");
        return html.toString();
    }

    public String getPageEnd() {
        return "</div></body></html>";
    }

    private String href(final String text, final String url) {
        return String.format("<a href='%s'>%s</a>", url, text);
    }

    private String paragraph(final String initialParagraph) {
        return String.format("<p>%s</p>%n", initialParagraph);
    }

    private String li(final String initialParagraph) {
        return String.format("<li>%s</li>%n", initialParagraph);
    }

    // Template functions
    private String heading(final int level, final String text) {
        return String.format("<h%1$d>%2$s</h%1$d>%n", level, text);
    }
}
