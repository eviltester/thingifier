package uk.co.compendiumdev.thingifier.htmlgui.htmlgen;

public class HtmlUtils {
    public static String sanitise(String value) {
        if(value==null) return "null";

        // todo - add a appconfig to allow XSS vulnerabilities in the GUI

        return value.replace("&","&amp;").
                replace("<", "&lt;").
                replace(">", "&gt;").
                replace(" ", "&nbsp;");
    }
}
