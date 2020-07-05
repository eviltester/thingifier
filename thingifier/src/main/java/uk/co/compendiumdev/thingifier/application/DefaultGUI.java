package uk.co.compendiumdev.thingifier.application;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import static spark.Spark.get;

public class DefaultGUI {

    private final Thingifier thingifier;

    public DefaultGUI(final Thingifier thingifier) {
        this.thingifier=thingifier;
    }

    public void setupDefaultGUI(){

        get("/gui", (request, response) -> {
            response.type("text/html");
            response.status(200);
            return "<html><head><title>GUI</title></head></body><a href='/gui/entities'>entities</a></body></html>";
        });

        get("/gui/entities", (request, response) -> {
            response.type("text/html");
            response.status(200);
            StringBuilder html = new StringBuilder();
            html.append("<html><head><title>GUI</title></head></body>");
            html.append("<ul>");
            for(String thing : thingifier.getThingNames()){
                html.append(String.format("<li><a href='/gui/instances?entity=%1$s'>%1$s</a></li>",thing));
            }
            html.append("</ul>");
            html.append("</body></html>");
            return html.toString();
        });

        get("/gui/instances", (request, response) -> {
            response.type("text/html");
            response.status(200);
            StringBuilder html = new StringBuilder();

            String entityName = request.queryParams("entity");

            html.append("<html><head><title>GUI</title></head></body>");
            html.append("<table>");
            html.append("<tr>");
            html.append("<th>guid</th>");
            for(String field : thingifier.getThingNamed(entityName).definition().getFieldNames()) {
                if (!field.equals("guid")) {
                    html.append(String.format("<th>%s</th>", field));
                }
            }
            html.append("</tr>");
            for(ThingInstance instance : thingifier.getThingNamed(entityName).getInstances()){
                html.append("<tr>");
                html.append(String.format("<td><a href='/gui/instance?entity=%1$s&guid=%2$s'>%2$s</a></td>",entityName,instance.getGUID()));
                for(String field : thingifier.getThingNamed(entityName).definition().getFieldNames()) {
                    if (!field.equals("guid")) {
                        html.append(String.format("<td>%s</td>", instance.getValue(field)));
                    }
                }
                html.append("</tr>");
            }
            html.append("</table>");
            html.append("</body></html>");
            return html.toString();
        });

        get("/gui/instance", (request, response) -> {
            response.type("text/html");
            response.status(200);
            StringBuilder html = new StringBuilder();

            String entityName = request.queryParams("entity");
            String guid = request.queryParams("guid");

            html.append("<html><head><title>GUI</title></head></body>");
            html.append("<ul>");
            ThingInstance thing = thingifier.getThingNamed(entityName).findInstanceByGUID(guid);
            html.append(String.format("<li>%s<ul><li>%s</li></ul></li>", "guid", thing.getGUID()));
            for(String field : thingifier.getThingNamed(entityName).definition().getFieldNames()) {
                if (!field.equals("guid")) {
                    html.append(String.format("<li>%s<ul><li>%s</li></ul></li>", field, thing.getValue(field)));
                }
            }
            html.append("</ul>");
            html.append("</body></html>");
            return html.toString();
        });
    }
}


