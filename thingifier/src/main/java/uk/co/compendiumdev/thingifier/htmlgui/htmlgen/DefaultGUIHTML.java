package uk.co.compendiumdev.thingifier.htmlgui.htmlgen;

import uk.co.compendiumdev.thingifier.application.ThingifierVersionDetails;

import java.util.ArrayList;
import java.util.List;

public class DefaultGUIHTML {

    private String customActualMenu;
    private String homePageContent;
    private String customFooter;
    private String customHeadContent;

    List<GuiMenuItem> menuItems;
    private String canonicalHostHttpUrl;

    public DefaultGUIHTML(){
        menuItems = new ArrayList<>();
        this.homePageContent = "";
        this.customFooter = "";
        this.customHeadContent = "";
        this.canonicalHostHttpUrl = "";
        this.customActualMenu = "";
    }

    public void appendMenuItem(final String title, final String url) {
        for(GuiMenuItem item : menuItems){
            if(item.menuTitle.equals(title) || item.url.equals(url)){
                // avoid adding duplicates
                return;
            }
        }
        menuItems.add(new GuiMenuItem(title, url));
    }

    public void appendToCustomHeadContent(final String someContent){
        customHeadContent = customHeadContent + "\n" + someContent;
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

    public void setCanonicalHost(String url) {
        canonicalHostHttpUrl = url;
    }

    private class GuiMenuItem {
        public String menuTitle;
        public String url;

        public GuiMenuItem(String title, String url){
            this.menuTitle = title;
            this.url = url;
        }
    }

    public String getPageStart(final String title, final String headInject, final String canonical){
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='en'><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>" + title + "</title>");
        html.append(" <link rel='stylesheet' href='/css/default.css'>");
        String injectFromEnv = System.getenv("HTML_HEAD_INJECT");
        if(injectFromEnv!=null){
            html.append(injectFromEnv);
        }
        if(headInject!=null) {
            html.append(headInject);
        }
        if(canonical!=null && !canonical.equals("")){
            String useCanoncial = canonical;

            if(canonicalHostHttpUrl!=null && !canonicalHostHttpUrl.equals("")){
                if(canonical.startsWith("https:") || canonical.startsWith("http:")){
                    // use the passed in canonical
                }else{
                    useCanoncial = canonicalHostHttpUrl + canonical;
                }
            }

            if(useCanoncial!= null && !useCanoncial.equals("")){
                html.append(" <link rel='canonical' href='%s'>".formatted(useCanoncial));
            }
        }
        html.append(customHeadContent);
        html.append("</head><body>");
        html.append("<div class='content'>");
        return html.toString();
    }

    public String getMenuAsHTML() {
        StringBuilder html = new StringBuilder();
        html.append("<div class='accessibilityskiplink'><a href='#maincontentstartshere'>Skip to main content</a></div>");
        html.append(getActualMenuHtml());
        return html.toString();
    }

    public DefaultGUIHTML setActualMenuHtml(String actual){
        customActualMenu = actual;
        return this;
    }

    public String getActualMenuHtml(){
        if(!customActualMenu.isEmpty()){
            return customActualMenu;
        }
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

    public String getStartOfMainContentMarker(){
        return "<main id='maincontentstartshere'>";
    }

    public String getEndOfMainContentMarker(){
        return "</main>";
        // end the main tag that we are assuming was already added
        // - potential for user created invalid HTML
    }

    public String getPageFooter(){

        if(!customFooter.isEmpty()){
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
