package uk.co.compendiumdev.thingifier.application;

import com.google.gson.GsonBuilder;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipVector;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;

import java.util.Collection;

import static spark.Spark.get;

public class DefaultGUI {

    // TODO: templates or tidier way to create the default GUI pages with styling

    private final Thingifier thingifier;
    private final JsonThing jsonThing;

    public DefaultGUI(final Thingifier thingifier) {
        this.thingifier=thingifier;
        this.jsonThing = new JsonThing(thingifier.apiConfig().jsonOutput());
    }

    public void setupDefaultGUI(){

        get("/gui", (request, response) -> {
            response.type("text/html");
            response.status(200);
            StringBuilder html = new StringBuilder();
            html.append("<html><head><title>GUI</title></head></body>");
            html.append("<p><a href='/'>API documentation</a></p>");
            html.append("<p><a href='/gui/entities'>Entities Explorer</a></p>");

            html.append("</body></html>");
            return html.toString();
        });

        get("/gui/entities", (request, response) -> {
            response.type("text/html");
            response.status(200);
            StringBuilder html = new StringBuilder();
            html.append("<html><head><title>GUI</title></head></body>");
            html.append(getInstancesRootMenuHtml());
            html.append("</body></html>");
            return html.toString();
        });

        get("/gui/instances", (request, response) -> {
            response.type("text/html");
            response.status(200);
            StringBuilder html = new StringBuilder();

            String entityName = request.queryParams("entity");

            html.append("<html><head><title>GUI</title></head><body>");
            html.append(getInstancesRootMenuHtml());
            html.append(getHTMLTableStylingCSS());


            final Thing thing = thingifier.getThingNamed(entityName);
            final ThingDefinition definition = thing.definition();

            html.append("<h2>" + definition.getPlural() + "</h2>");

            html.append(startHtmlTableFor(definition));

            for(ThingInstance instance : thing.getInstances()){
                html.append(htmlTableRowFor(instance));
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

            final Thing thing = thingifier.getThingNamed(entityName);
            final ThingDefinition definition = thing.definition();
            ThingInstance instance = thing.findInstanceByGUID(guid);

            html.append("<html><head><title>GUI</title></head></body>");
            html.append(getInstancesRootMenuHtml());

            html.append("<h2>" + definition.getName() + "</h2>");

            html.append("<ul>");
            html.append(String.format("<li>%s<ul><li>%s</li></ul></li>", "guid", instance.getGUID()));
            for(String field : definition.getFieldNames()) {
                if (!field.equals("guid")) {
                    html.append(String.format("<li>%s<ul><li>%s</li></ul></li>",
                            field, instance.getValue(field)));
                }
            }
            html.append("</ul>");

            if(instance.hasAnyRelationshipInstances()) {

                html.append("<h2>Relationships</h2>");
                html.append(getHTMLTableStylingCSS());

                for (RelationshipVector relationship : definition.getRelationships()) {
                    final Collection<ThingInstance> relatedItems = instance.connectedItems(relationship.getName());
                    html.append("<h2>" + relationship.getName() + "</h2>");
                    if(relatedItems.size() > 0) {
                        boolean header=true;

                        for (ThingInstance relatedInstance : relatedItems) {
                            if(header){
                                html.append(startHtmlTableFor(relatedInstance.getEntity()));
                                header=false;
                            }
                            html.append(htmlTableRowFor(relatedInstance));
                        }
                        html.append("</table>");

                    }else{
                        html.append("<ul><li>none</li></ul>");
                    }
                }
            }

            html.append("<h2>JSON Example</h2>");
            html.append("<pre>");
            // pretty print the json
            html.append(new GsonBuilder().setPrettyPrinting()
                    .create().toJson(jsonThing.asJsonObject(instance)));
            html.append("</pre>");
            html.append("</body></html>");
            return html.toString();
        });
    }

    private String getInstancesRootMenuHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<div class='entity-instances-menu'>");
        html.append("<p><a href='/'>API documentation</a></p>");
        html.append("<p><a href='/gui'>Menu</a>:</p>");
        html.append("<ul>");
        for(String thing : thingifier.getThingNames()){
            html.append(String.format("<li><a href='/gui/instances?entity=%1$s'>%1$s</a></li>",thing));
        }
        html.append("</ul>");
        html.append("</div>");
        return html.toString();
    }

    private String getHTMLTableStylingCSS() {
        StringBuilder html = new StringBuilder();
        html.append("<style>\n");
        html.append("table {border-collapse: collapse;}\n" +
                "table, th, td {border: 1px solid black;}\n");
        html.append("</style>");
        return html.toString();
    }

    private String htmlTableRowFor(final ThingInstance instance) {
        StringBuilder html = new StringBuilder();
        final ThingDefinition definition = instance.getEntity();

        html.append("<tr>");
        html.append(String.format("<td><a href='/gui/instance?entity=%1$s&guid=%2$s'>%2$s</a></td>",
                        definition.getName(),instance.getGUID()));

        for(String field : definition.getFieldNames()) {
            if (!field.equals("guid")) {
                html.append(String.format("<td>%s</td>", instance.getValue(field)));
            }
        }
        html.append("</tr>");

        return html.toString();
    }

    private String startHtmlTableFor(final ThingDefinition definition) {
        StringBuilder html = new StringBuilder();

        html.append("<table>");
        html.append("<tr>");
        html.append("<th>guid</th>");
        for(String field : definition.getFieldNames()) {
            if (!field.equals("guid")) {
                html.append(String.format("<th>%s</th>", field));
            }
        }
        html.append("</tr>");

        return html.toString();
    }
}


