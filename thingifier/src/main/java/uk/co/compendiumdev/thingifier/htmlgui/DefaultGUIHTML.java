package uk.co.compendiumdev.thingifier.htmlgui;

import uk.co.compendiumdev.thingifier.application.ThingifierVersionDetails;

import java.util.ArrayList;
import java.util.List;

public class DefaultGUIHTML {

    private String homePageContent;
    private String customFooter;
    List<GuiMenuItem> menuItems;

    public DefaultGUIHTML(){
        menuItems = new ArrayList<>();
        this.homePageContent = "";
        this.customFooter = "";
    }

    public void appendMenuItem(final String title, final String url) {
        menuItems.add(new GuiMenuItem(title, url));
    }

    public void prefixMenuItem(final String title, final String url) {
        menuItems.add(0, new GuiMenuItem(title, url));
    }

    public void removeMenuItem(final String title) {
        GuiMenuItem removeme=null;
        for(GuiMenuItem item : menuItems){
            if(item.menuTitle.contentEquals(title)){
                removeme = item;
                break;
            }
        }
        if(removeme!=null) {
            menuItems.remove(removeme);
        }
    }

    public void setHomePageContent(final String content) {
        homePageContent = content;
    }

    public String getHomePageContent() {
        return homePageContent;
    }

    public void setFooter(final String footer) {
        if(footer!=null) {
            this.customFooter = footer;
        }
    }

    private class GuiMenuItem {
        public String menuTitle;
        public String url;

        public GuiMenuItem(String title, String url){
            this.menuTitle = title;
            this.url = url;
        }
    }

    public String getPageStart(final String title){
        StringBuilder html = new StringBuilder();
        html.append("<html><head>");
        html.append("<meta http-equiv='content-language' content='en-us'>");
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
        for(GuiMenuItem menu : menuItems){
            html.append(String.format("<li><a href='%s'>%s</a></li>",
                    menu.url, menu.menuTitle));
        }

        html.append("</ul>");
        html.append("</div>");
        return html.toString();
    }

    public String getPageFooter(){

        if(customFooter.length()>0){
            return customFooter;
        }

        StringBuilder html = new StringBuilder();
        html.append("<p>&nbsp;</p><hr/>");
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
