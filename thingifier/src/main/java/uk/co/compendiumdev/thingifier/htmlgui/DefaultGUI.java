package uk.co.compendiumdev.thingifier.htmlgui;

import com.google.gson.GsonBuilder;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVector;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;
import uk.co.compendiumdev.thingifier.reporting.XmlThing;

import java.util.Collection;

import static spark.Spark.get;

public class DefaultGUI {

    // TODO: templates or tidier way to create the default GUI pages with styling
    // todo: support filters in the GUI Urls

    private final Thingifier thingifier;
    private final JsonThing jsonThing;
    private final DefaultGUIHTML templates;
    private final XmlThing xmlThing;
    private final ThingifierApiConfig apiConfig;

    public DefaultGUI(final Thingifier thingifier, DefaultGUIHTML defaultGui) {
        this.thingifier=thingifier;
        this.apiConfig = thingifier.apiConfig();
        this.jsonThing = new JsonThing(apiConfig.jsonOutput());
        this.xmlThing = new XmlThing(jsonThing);
        this.templates = defaultGui;
    }

    public DefaultGUI configureRoutes(){

        get("/gui", (request, response) -> {
            response.type("text/html");
            response.status(200);
            StringBuilder html = new StringBuilder();
            html.append(templates.getPageStart("GUI"));
            html.append(templates.getMenuAsHTML());

            // allow an app level configuration html here
            html.append(templates.getHomePageContent());

            html.append(templates.getPageFooter());
            html.append(templates.getPageEnd());
            return html.toString();
        });


        templates.appendMenuItem("Home", "/gui");

        // This is not an API call - don't show it in API documentation
//        publicRoutes.add(new RoutingDefinition(
//                                RoutingVerb.GET,
//                                "/gui",
//                                RoutingStatus.returnedFromCall(),
//                                null
//                            ).addDocumentation("Show the Default GUI"));

        templates.appendMenuItem("Entities Explorer", "/gui/entities");

        get("/gui/entities", (request, response) -> {
            response.type("text/html");
            response.status(200);
            StringBuilder html = new StringBuilder();
            html.append(templates.getPageStart("Entities Menu"));

            html.append(templates.getMenuAsHTML());
            html.append(getInstancesRootMenuHtml());
            //html.append(heading(2, "Choose from the Entities Menu Above"));
            html.append(templates.getPageFooter());
            html.append(templates.getPageEnd());
            return html.toString();
        });

        get("/gui/instances", (request, response) -> {
            response.type("text/html");
            response.status(200);
            StringBuilder html = new StringBuilder();

            String entityName = request.queryParams("entity");

            html.append(templates.getPageStart(entityName + " Instances"));

            html.append(templates.getMenuAsHTML());
            html.append(getInstancesRootMenuHtml());

            html.append(heading(1, entityName + " Instances"));

            final Thing thing = thingifier.getThingNamed(entityName);

            if(thing==null){
                response.redirect("/gui/entities");
                return "";
            }

            final ThingDefinition definition = thing.definition();

            html.append("<h2>" + definition.getPlural() + "</h2>");

            html.append(startHtmlTableFor(definition));

            for(ThingInstance instance : thing.getInstances()){
                html.append(htmlTableRowFor(instance));
            }
            html.append("</tbody>");
            html.append("</table>");

            html.append(templates.getPageFooter());
            html.append(templates.getPageEnd());
            return html.toString();
        });

        get("/gui/instance", (request, response) -> {
                    response.type("text/html");
                    response.status(200);
                    StringBuilder html = new StringBuilder();

                    String entityName = "";
                    Thing thing = null;
                    for (String queryParam : request.queryParams()) {
                        if (queryParam.contentEquals("entity")) {
                            entityName = request.queryParams("entity");
                            thing = thingifier.getThingNamed(entityName);
                        }
                    }

                    if (thing == null) {
                        response.redirect("/gui/entities");
                        return "";
                    }

                    String keyName = "";
                    String keyValue = "";
                    for (String queryParam : request.queryParams()) {
                        Field field = thing.definition().getField(queryParam);
                        if (field != null) {
                            if (field.getType() == FieldType.GUID || field.getType() == FieldType.ID) {
                                keyName = field.getName();
                                keyValue = request.queryParams(queryParam);
                                break;
                            }
                        }
                    }

                    final ThingDefinition definition = thing.definition();
                    ThingInstance instance = thing.findInstanceByField(FieldValue.is(keyName, keyValue));

                    if (instance == null) {
                        response.redirect("/gui/instances?entity=" + entityName);
                        return "";
                    }

                    html.append(templates.getPageStart(entityName + " Instance"));
                    html.append(templates.getMenuAsHTML());
                    html.append(getInstancesRootMenuHtml());

                    html.append(heading(1, entityName + " Instance"));

                    html.append("<h2>" + definition.getName() + "</h2>");

                    html.append(getInstanceAsTable(instance));

                    html.append("<details style='padding:1em'><summary>As List</summary>");
                    html.append(getInstanceAsUl(instance));
                    html.append("</details>");


            if (instance.getRelationships().hasAnyRelationshipInstances()) {

                        html.append("<h2>Relationships</h2>");

                        for (RelationshipVector relationship : definition.related().getRelationships()) {
                            final Collection<ThingInstance> relatedItems = instance.getRelationships().getConnectedItems(relationship.getName());
                            html.append("<h3>" + relationship.getName() + "</h3>");
                            if (relatedItems.size() > 0) {
                                boolean header = true;

                                for (ThingInstance relatedInstance : relatedItems) {
                                    if (header) {
                                        html.append(startHtmlTableFor(relatedInstance.getEntity()));
                                        header = false;
                                    }
                                    html.append(htmlTableRowFor(relatedInstance));

                                }
                                html.append("</tbody>");
                                html.append("</table>");

                            } else {
                                html.append("<ul><li>none</li></ul>");
                            }
                        }
                    }

                    if (thingifier.apiConfig().willApiAllowJsonForResponses()){
                            html.append("<h2>JSON Example</h2>");
                        html.append("<pre class='json'>");
                        html.append("<code class='json'>");
                        // pretty print the json
                        html.append(new GsonBuilder().setPrettyPrinting()
                                .create().toJson(jsonThing.asJsonObject(instance)));
                        html.append("</code>");
                        html.append("</pre>");
                    }

            if (thingifier.apiConfig().willApiAllowXmlForResponses()) {
                html.append("<h2>XML Example</h2>");
                html.append("<pre class='xml'>");
                html.append("<code class='xml'>");
                // pretty print the json
                html.append(xmlThing.prettyPrintHtml(xmlThing.getSingleObjectXml(instance)));
                html.append("</code>");
                html.append("</pre>");
            }

            html.append(templates.getPageFooter());
            html.append(templates.getPageEnd());
            return html.toString();
        });

        return this;
    }

    private String getInstanceAsUl(final ThingInstance instance) {
        final ThingDefinition definition = instance.getEntity();
        StringBuilder html = new StringBuilder();
        html.append("<ul>");
        if(apiConfig.willResponsesShowGuids()) {
            html.append(String.format("<li>%s<ul><li>%s</li></ul></li>", "guid", instance.getGUID()));
        }
        for(String field : definition.getFieldNames()) {
            if (!field.equals("guid")) {
                html.append(String.format("<li>%s<ul><li>%s</li></ul></li>",
                        field, htmlsanitise(instance.getFieldValue(field).asString())));
            }
        }
        html.append("</ul>");
        return html.toString();
    }

    private String htmlsanitise(final String value) {

        // todo - add a appconfig to allow XSS vulnerabilities in the GUI

        return value.replace("&","&amp;").
                     replace("<", "&lt;").
                     replace(">", "&gt;").
                     replace(" ", "&nbsp;");
    }

    private String getInstanceAsTable(ThingInstance instance) {

        final ThingDefinition definition = instance.getEntity();

        StringBuilder html = new StringBuilder();
        html.append("<table>");
        html.append("<thead><tr>");
        for(String fieldName : definition.getFieldNames()) {
            Field field = definition.getField(fieldName);
            if(field.getType()==FieldType.GUID){
                if(apiConfig.willResponsesShowGuids()) {
                    html.append(String.format("<th>%s</th>",field.getName()));
                }
            }else{
                html.append(String.format("<th>%s</th>",field.getName()));
            }
        }
        html.append("</tr></thead>");
        html.append("<tbody><tr>");
        for(String fieldName : definition.getFieldNames()) {
            Field field = definition.getField(fieldName);
            if(field.getType()==FieldType.GUID){
                if(apiConfig.willResponsesShowGuids()) {
                    html.append(String.format("<td>%s</td>", instance.getFieldValue(fieldName).asString()));
                }
            }else{
                html.append(String.format("<td>%s</td>",htmlsanitise(instance.getFieldValue(fieldName).asString())));
            }
        }

        html.append("</tr></tbody>");
        html.append("</table>");
        return html.toString();
    }

    private String getInstancesRootMenuHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<div class='entity-instances-menu menu'>");
        html.append("<ul>");
        for(String thing : thingifier.getThingNames()){
            html.append(String.format("<li><a href='/gui/instances?entity=%1$s'>%1$s</a></li>",thing));
        }
        html.append("</ul>");
        html.append("</div>");
        return html.toString();
    }


    private String htmlTableRowFor(final ThingInstance instance) {
        StringBuilder html = new StringBuilder();
        final ThingDefinition definition = instance.getEntity();

        html.append("<tr>");
        // show keys first
        if(apiConfig.willResponsesShowGuids()) {
            html.append(String.format("<td><a href='/gui/instance?entity=%1$s&guid=%2$s'>%2$s</a></td>",
                    definition.getName(), instance.getGUID()));
        }

        // show any clickable id fields
        for(String field : definition.getFieldNames()) {
            if (!field.equals("guid")) {
                Field theField = definition.getField(field);
                if(theField.getType()== FieldType.ID){
                    // make ids clickable
                    String renderAs = String.format("<a href='/gui/instance?entity=%1$s&%2$s=%3$s'>%3$s</a>",
                            definition.getName(),theField.getName(), instance.getFieldValue(field).asString());
                    html.append(String.format("<td>%s</td>", renderAs));
                }

            }
        }

        // show any normal fields
        for(String field : definition.getFieldNames()) {
            Field theField = definition.getField(field);
            if(theField.getType()!= FieldType.ID && theField.getType()!=FieldType.GUID){
                html.append(String.format("<td>%s</td>", htmlsanitise(instance.getFieldValue(field).asString())));
            }
        }
        html.append("</tr>");

        return html.toString();
    }

    private String startHtmlTableFor(final ThingDefinition definition) {
        StringBuilder html = new StringBuilder();

        html.append("<table>");
        html.append("<thead>");
        html.append("<tr>");
        // guid first
        if(apiConfig.willResponsesShowGuids()) {
            html.append("<th>guid</th>");
        }
        // then any ids
        for(String field : definition.getFieldNames()) {
            Field theField = definition.getField(field);
            if (theField.getType()==FieldType.ID) {
                html.append(String.format("<th>%s</th>", field));
            }
        }
        // then the normal fields
        for(String field : definition.getFieldNames()) {
            Field theField = definition.getField(field);
            if (theField.getType()!=FieldType.ID && theField.getType()!=FieldType.GUID) {
                html.append(String.format("<th>%s</th>", field));
            }
        }
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");

        return html.toString();
    }

    private String heading(final int level, final String text) {
        return String.format("<h%1$d>%2$s</h%1$d>%n", level, text);
    }
}


