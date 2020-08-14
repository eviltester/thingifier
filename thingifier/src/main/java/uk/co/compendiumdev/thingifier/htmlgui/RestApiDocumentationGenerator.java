package uk.co.compendiumdev.thingifier.htmlgui;

import com.google.gson.GsonBuilder;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.api.routings.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVector;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.ValidationRule;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;
import uk.co.compendiumdev.thingifier.reporting.XmlThing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class RestApiDocumentationGenerator {
    private final Thingifier thingifier;
    private final List<Thing> things;
    private final Collection<RelationshipDefinition> relationships;
    private final JsonThing jsonThing;
    private final XmlThing xmlThing;
    private final DefaultGUIHTML defaultGui;
    private final ThingifierApiConfig apiConfig;
    private String prependPath;

    // TODO: DefaultGUIHTML is hard coded in here, need more flexibility
    // around GUIs to allow hooks and other main classes to expand it
    // possibly a GuiHtml with ability to set meta tags, title, register menu items, change footers etc.
    // start with a menu and register menu items and return menu html

    public RestApiDocumentationGenerator(final Thingifier aThingifier, DefaultGUIHTML defaultGui) {
        this.thingifier = aThingifier;
        this.things = thingifier.getThings();
        this.relationships = thingifier.getRelationshipDefinitions();
        apiConfig = thingifier.apiConfig();
        jsonThing = new JsonThing(apiConfig.jsonOutput());
        xmlThing = new XmlThing(jsonThing);
        this.defaultGui = defaultGui;
        prependPath = "";
    }

    public String getApiDocumentation(final ApiRoutingDefinition routingDefinitions,
                                      final List<RoutingDefinition> additionalRoutes,
                                      final String urlPath) {

        StringBuilder output = new StringBuilder();

        output.append(defaultGui.getPageStart("API Documentation"));
        output.append(defaultGui.getMenuAsHTML());


        if(urlPath!=null){
            prependPath = urlPath;
        }

        if (thingifier != null) {
            // create generic API documentation
            output.append(heading(1, thingifier.getTitle()));
            output.append(String.format("%n"));

            output.append("<div class='headertextblock'>");
            output.append(paragraph(thingifier.getInitialParagraph()));
            output.append(String.format("%n"));

            // TODO: the following should be configurable by api config
            // e.g. if XML is not supported then do not show info about XML
            output.append(paragraph("Will accept json by default."));
            output.append(paragraph("<i>Content-Type: application/json</i>"));
            output.append(paragraph("Set Content-Type header to application/xml if you want to send in XML."));
            output.append(paragraph("<i>Content-Type: application/xml</i>"));

            if(thingifier.apiConfig().willApiAllowXmlForResponses() &&
                    thingifier.apiConfig().willApiAllowJsonForResponses() ) {
                output.append(paragraph("You can control the returned data format by setting the Accept header"));
            }

            if(thingifier.apiConfig().willApiAllowXmlForResponses() &&
                    !thingifier.apiConfig().willApiAllowJsonForResponses() ) {
                output.append(paragraph("Returned data format will be XML by default."));
            }

            if(thingifier.apiConfig().willApiAllowXmlForResponses()) {
                output.append(paragraph("You can request XML response by setting the Accept header."));
                output.append(paragraph("i.e. for XML use"));
                output.append(paragraph("<i>Accept: application/xml</i><br/><br/>\n"));
            }

            if(!thingifier.apiConfig().willApiAllowXmlForResponses() &&
                    thingifier.apiConfig().willApiAllowJsonForResponses() ) {
                output.append(paragraph("You receive JSON by default as the response"));
            }

            if(thingifier.apiConfig().willApiAllowJsonForResponses()) {
                output.append(paragraph("You can request JSON response by setting the Accept header."));
                output.append(paragraph("i.e. for JSON use"));
                output.append(paragraph("<i>Accept: application/json</i><br/><br/>\n"));
            }

            if(thingifier.apiConfig().forParams().willAllowFilteringThroughUrlParams()){
                output.append(paragraph("Some requests can be filtered by adding query params of fieldname=value. Where only matching items will be returned."));
                output.append(paragraph("e.g. <i>/items?size=2&status=true</i><br/><br/>\n"));
            }

            output.append(paragraph("All data lives in memory and is not persisted so the application is cleared everytime you start it. It does have some test data in here when you start"));
            output.append("</div>");
        }

        output.append(heading(2, "Model"));

        if (things != null) {
            output.append(heading(3, "Things"));
            for (Thing aThing : things) {
                output.append(heading(4, aThing.definition().getName()));

                output.append("Fields:\n");
                output.append("<ul>\n");

                // todo: generate an example Thing
                final DocumentationThingInstance exampleThing = new DocumentationThingInstance(aThing.definition());

                output.append("<table>\n");
                output.append("<thead>\n");
                output.append("<tr>");
                output.append("<td>Fieldname</td>\n");
                output.append("<td>Type</td>\n");
                output.append("<td>Validation</td>\n");
                output.append("</tr>");
                output.append("</thead>\n");



                output.append("<tbody>\n");

                for (String aField : aThing.definition().getFieldNames()) {

                    output.append("<tr>");
                    if(!apiConfig.willResponsesShowGuids() && aField.contentEquals("guid")){
                        continue;
                    }

                    output.append(String.format("<td>%s</td>", aField));

                    Field theField = aThing.definition().getField(aField);
                    output.append(String.format("<td>%s</td>", theField.getType()));

                    output.append("<td>");
                    output.append("<ul>");
                    for (ValidationRule validation : theField.validationRules()) {
                        //use the validation error message in the documentation
                        output.append("<li>" + validation.getErrorMessage(FieldValue.is("","")) + "</li>\n");
                    }

                    output.append(String.format("<li>Mandatory?: %b</li>", theField.isMandatory()));

                    if(theField.getType()== FieldType.INTEGER){
                        output.append(String.format("<li>Values Between: \"%d\" to \"%d\" </li>",
                                theField.getMinimumIntegerValue(), theField.getMaximumIntegerValue()));
                    }

                    if(theField.getType()== FieldType.FLOAT){
                        output.append(String.format("<li>Values Between: \"%f\" to \"%f\" </li>",
                                theField.getMinimumFloatValue(), theField.getMaximumFloatValue()));
                    }

                    output.append("</ul>\n");
                    output.append("</td>\n");

                    output.append("</tr>");

                    String exampleValue = theField.getRandomExampleValue();
                    exampleThing.overrideValue(theField.getName(), exampleValue);

                    output.append(String.format("<tr><td colspan='3' class='examplevalue'>Example: \"%s\"</td></tr>", exampleValue));


                }


                output.append("</tbody>\n");
                output.append("</table>\n");



                // show an example
                if(thingifier.apiConfig().willApiAllowJsonForResponses()) {
                    output.append("<p>Example JSON Output from API calls</p>\n");
                    output.append("<pre class='json'>\n");
                    output.append("<code class='json'>\n");
                    output.append(new GsonBuilder().setPrettyPrinting()
                            .create().toJson(jsonThing.asJsonObject(exampleThing.getInstance())));
                    output.append("</code>\n");
                    output.append("</pre>\n");
                }

                if(thingifier.apiConfig().willApiAllowXmlForResponses()) {
                    output.append("<p>Example XML Output from API calls</p>\n");
                    output.append("<pre class='xml'>\n");
                    output.append("<code class='xml'>\n");
                    output.append(xmlThing.prettyPrintHtml(xmlThing.getSingleObjectXml(exampleThing.getInstance())));
                    output.append("</code>\n");
                    output.append("</pre>\n");
                }

                output.append("<p>Example JSON Input to API calls</p>\n");
                ThingInstance createableExampleThing = exampleThing.withoutIDsOrGUIDs();
                output.append("<pre class='json'>\n");
                output.append("<code class='json'>\n");
                output.append(new GsonBuilder().setPrettyPrinting()
                        .create().toJson(jsonThing.asJsonObject(createableExampleThing)));
                output.append("</code>\n");
                output.append("</pre>\n");

                // todo: conditional output on if API supports XML responses
                output.append("<p>Example XML Input to API calls</p>\n");
                output.append("<pre class='xml'>\n");
                output.append("<code class='xml'>\n");
                output.append(xmlThing.prettyPrintHtml(xmlThing.getSingleObjectXml(createableExampleThing)));
                output.append("</code>\n");
                output.append("</pre>\n");

            }
        }

        if (relationships != null) {
            output.append(heading(3, "Relationships"));
            output.append("<ul>\n");

            for (RelationshipDefinition relationship : relationships) {

                RelationshipVector fromToRelationship = relationship.getFromRelationship();

                //task-of : task => project
                String reportLine = String.format("<li>%s : %s => %s%n",
                        fromToRelationship.getName(),
                        fromToRelationship.getFrom().definition().getName(),
                        fromToRelationship.getTo().definition().getName());


                // for a two way relationship can it be combined on to one line e.g.
                // tasks/task-of : project =(tasks)=> task  / task=(task-of)=> project

                if (relationship.isTwoWay()) {
                    reportLine = String.format("<li>%1$s/%2$s : %3$s =(%1$s)=> %4$s / %4$s =(%2$s)=> %3$s %n",
                            relationship.getFromRelationship().getName(),
                            relationship.getReversedRelationship().getName(),
                            relationship.getFromRelationship().getFrom().definition().getName(),
                            relationship.getFromRelationship().getTo().definition().getName());
                }

                output.append(reportLine);
            }
            output.append("</ul>\n");

        }

        // output the API documentation
        output.append(heading(2, "API"));

        output.append(paragraph("The API takes body with objects using the field definitions and examples shown in the model."));

        output.append(heading(3, "End Points"));

        String currentEndPoint = "";

        for (RoutingDefinition routingDefn : routingDefinitions.definitions()) {
            // only show if not a method not allowed method
            if (!currentEndPoint.equalsIgnoreCase(routingDefn.url())) {
                // new endpoint
                output.append(heading(4, "endpoint", "/" + routingDefn.url()));
                output.append(paragraph("e.g. <span class='endpoint'>" + url(routingDefn.url()) + "</span"));

                if(routingDefn.isFilterable() &&
                        thingifier.apiConfig().forParams().willAllowFilteringThroughUrlParams()){

                    // we are allowed to filter url
                    output.append(paragraph("This endpoint can be filtered with fields as URL Query Parameters."));
                    String exampleFilter = getExampleFilter(routingDefn.getFilterableEntity());
                    if(exampleFilter!=null && exampleFilter.length()>0){
                        output.append(paragraph("e.g. <span class='endpoint'>" + url(routingDefn.url()) + exampleFilter + "</span"));
                    }
                }

                currentEndPoint = routingDefn.url();
            }
            if (routingDefn.status().isReturnedFromCall() || routingDefn.status().value() != 405) {
                // ignore options
                if (routingDefn.verb() != RoutingVerb.OPTIONS) {
                    output.append(String.format("<ul>%n<li class='endpoint'>%n<strong>%s /%s</strong><ul><li class='normal'>%s</li></ul></li>%n</ul>",
                            routingDefn.verb(), routingDefn.url(), routingDefn.getDocumentation()));

                }
            }
        }

        output.append(heading(4, "/docs"));
        output.append(paragraph("e.g. <span class='endpoint'>" + url("/docs") + "</span"));
        output.append(String.format("<ul>%n<li class='endpoint'>%n<strong>%s /%s</strong><ul><li class='normal'>%s</li></ul></li>%n</ul>",
                "GET", url("/docs"), "Show this documentation as HTML."));

        List<String> processedAdditionalRoutes = new ArrayList<>();
        if(additionalRoutes!=null) {
            for (RoutingDefinition route : additionalRoutes) {
                if (!processedAdditionalRoutes.contains(route.url())){
                    output.append(heading(4, "/" + route.url()));
                    processedAdditionalRoutes.add(route.url());
                    output.append(paragraph("e.g. <span class='endpoint'>" + url(route.url()) + "</span"));

                    // handle all verbs for this route
                    for (RoutingDefinition subroute : additionalRoutes) {
                        if (subroute.url().contentEquals(route.url())) {
                            output.append(String.format("<ul>%n<li class='endpoint'>%n<strong>%s /%s</strong><ul><li class='normal'>%s</li></ul></li>%n</ul>",
                                    subroute.verb(), subroute.url(), subroute.getDocumentation()));
                        }
                    }

                }
            }
        }

        output.append(paragraph(href("[download swagger file]","/docs/swagger")));

        output.append(defaultGui.getPageFooter());
        output.append(defaultGui.getPageEnd());
        return output.toString();
    }

    private String getExampleFilter(final ThingDefinition filterableEntity) {
        String exampleFilters = "";
        List<Field> exampleFields = new ArrayList<>();
        List<Integer> indexes = new ArrayList<>();
        Random random = new Random();

        // todo: ignore strings unless none added, in which case add the strings
        for(String fieldName : filterableEntity.getFieldNames()){
            Field field = filterableEntity.getField(fieldName);
            if(field.getType() != FieldType.ID && field.getType()!= FieldType.GUID){
                // we can filter on guid and id, but don't use those as examples
                if(exampleFields.size()==0 || random.nextBoolean()){
                    // make sure at least one
                    indexes.add(exampleFields.size());
                    exampleFields.add(field);
                }
            }
        }

        String delimiter = "?";

        int fieldsToUse=random.nextInt(exampleFields.size())+1;
        for(int x=0; x<fieldsToUse; x++){
            int fieldToUse=random.nextInt(indexes.size());
            Field field = exampleFields.get(indexes.get(fieldToUse));
            indexes.remove(fieldToUse);
            exampleFilters = exampleFilters + delimiter + field.getName() + "=" + field.getRandomExampleValue();
            delimiter = "&";
        }

//        try {
//            exampleFilters =  URLEncoder.encode(exampleFilters, StandardCharsets.UTF_8.toString());
//        } catch (UnsupportedEncodingException ex) {
            exampleFilters = exampleFilters.replace(" ", "%20");
//        }

        return exampleFilters;
    }

    private String url(final String postUrl) {

        String midPath = "";
        if(!postUrl.startsWith("/")){
            midPath = "/";
        }
        // todo: option to make clickable?

        return prependPath + midPath + postUrl;
    }

    private String heading(final int level, final String theclass, final String text) {
        return String.format("<h%1$d class='%2$s'>%3$s</h%1$d>%n", level, theclass, text);
    }


    private String href(final String text, final String url) {
        return String.format("<a href='%s'>%s</a>", url, text);
    }

    private String paragraph(final String initialParagraph) {
        return String.format("<p>%s</p>%n", initialParagraph);
    }


    // Template functions
    private String heading(final int level, final String text) {
        return String.format("<h%1$d>%2$s</h%1$d>%n", level, text);
    }
}
