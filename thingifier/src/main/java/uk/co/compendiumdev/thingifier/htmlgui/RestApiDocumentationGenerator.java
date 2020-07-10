package uk.co.compendiumdev.thingifier.htmlgui;

import com.google.gson.GsonBuilder;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.api.routings.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;
import uk.co.compendiumdev.thingifier.application.ThingifierVersionDetails;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.generic.definitions.validation.ValidationRule;
import uk.co.compendiumdev.thingifier.generic.instances.DocumentationThingInstance;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;
import uk.co.compendiumdev.thingifier.reporting.XmlThing;

import java.util.Collection;
import java.util.List;

public class RestApiDocumentationGenerator {
    private final Thingifier thingifier;
    private final List<Thing> things;
    private final Collection<RelationshipDefinition> relationships;
    private final JsonThing jsonThing;
    private final XmlThing xmlThing;
    private final DefaultGUIHTML mainMenu;
    private final ThingifierApiConfig apiConfig;
    private String prependPath;

    public RestApiDocumentationGenerator(final Thingifier aThingifier) {
        this.thingifier = aThingifier;
        this.things = thingifier.getThings();
        this.relationships = thingifier.getRelationshipDefinitions();
        apiConfig = thingifier.apiConfig();
        jsonThing = new JsonThing(apiConfig.jsonOutput());
        xmlThing = new XmlThing(jsonThing);
        mainMenu = new DefaultGUIHTML();
        prependPath = "";
    }

    public String getApiDocumentation(final ApiRoutingDefinition routingDefinitions,
                                      final List<RoutingDefinition> additionalRoutes,
                                      final String urlPath) {

        StringBuilder output = new StringBuilder();

        output.append(mainMenu.getPageStart("API Documentation"));
        output.append(mainMenu.getMenuAsHTML());


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
            output.append(paragraph("You can control the returned data format by setting the Accept header"));
            output.append(paragraph("i.e. for XML use"));
            output.append(paragraph("<i>Accept: application/xml</i><br/><br/>\n"));
            output.append(paragraph("You get JSON by default but can also request this i.e."));
            output.append(paragraph("<i>Accept: application/json</i><br/><br/>\n"));

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
                    if(!apiConfig.showGuidsInResponses() && aField.contentEquals("guid")){
                        continue;
                    }

                    output.append(String.format("<td>%s</td>", aField));

                    Field theField = aThing.definition().getField(aField);
                    output.append(String.format("<td>%s</td>", theField.getType()));

                    output.append("<td>");
                    output.append("<ul>");
                    for (ValidationRule validation : theField.validationRules()) {
                        output.append("<li>" + validation.getErrorMessage("") + "</li>\n");
                    }

                    output.append(String.format("<li>Mandatory?: %b</li>", theField.isMandatory()));
                    output.append(String.format("<li>Validates?: %b</li>", theField.willValidate()));

                    if(theField.shouldTruncate()){
                        output.append(String.format("<li>Truncate to: %d characters</li>", theField.truncateLength()));
                    }

                    if(theField.getType()== FieldType.STRING) {
                        if (theField.willEnforceLength()) {
                            output.append(String.format("<li>Max Length: %d characters</li>", theField.truncateLength()));
                        }
                    }

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

/*
                output.append("<ul>\n");
                for (String aField : aThing.definition().getFieldNames()) {

                    if(!apiConfig.showGuidsInResponses() && aField.contentEquals("guid")){
                        continue;
                    }

                    output.append(String.format("<li> %s %n", aField));

                    output.append("<ul>\n");

                    Field theField = aThing.definition().getField(aField);
                    output.append(String.format("<li> (%s)</li>", theField.getType()));

                    for (ValidationRule validation : theField.validationRules()) {
                        output.append("<li>" + validation.getErrorMessage("") + "</li>\n");
                    }

                    String exampleValue = theField.getRandomExampleValue();
                    exampleThing.overrideValue(theField.getName(), exampleValue);

                    output.append(String.format("<li>Example: \"%s\"</li>", exampleValue));
                    output.append(String.format("<li>Mandatory?: %b</li>", theField.isMandatory()));
                    output.append(String.format("<li>Validates?: %b</li>", theField.willValidate()));

                    if(theField.shouldTruncate()){
                        output.append(String.format("<li>Truncate to: %d characters</li>", theField.truncateLength()));
                    }

                    if(theField.getType()== FieldType.STRING) {
                        if (theField.willEnforceLength()) {
                            output.append(String.format("<li>Max Length: %d characters</li>", theField.truncateLength()));
                        }
                    }

                    if(theField.getType()== FieldType.INTEGER){
                        output.append(String.format("<li>Values Between: \"%d\" to \"%d\" </li>",
                                            theField.getMinimumIntegerValue(), theField.getMaximumIntegerValue()));
                    }

                    if(theField.getType()== FieldType.FLOAT){
                        output.append(String.format("<li>Values Between: \"%f\" to \"%f\" </li>",
                                theField.getMinimumFloatValue(), theField.getMaximumFloatValue()));
                    }

                    output.append("</ul>\n");
                    output.append("</li>\n");


                }
                output.append("</ul>\n");
*/

                // show an example
                output.append("<p>Example JSON Output from API calls</p>\n");
                output.append("<pre class='json'>\n");
                output.append("<code class='json'>\n");
                output.append(new GsonBuilder().setPrettyPrinting()
                        .create().toJson(jsonThing.asJsonObject(exampleThing.getInstance())));
                output.append("</code>\n");
                output.append("</pre>\n");

                // todo: conditional output on if API supports XML responses
                output.append("<p>Example XML Output from API calls</p>\n");
                output.append("<pre class='xml'>\n");
                output.append(xmlThing.prettyPrintHtml(xmlThing.getSingleObjectXml(exampleThing.getInstance())));
                output.append("</pre>\n");

                output.append("<p>Example JSON Input to API calls</p>\n");
                ThingInstance createableExampleThing = exampleThing.getInstanceWithoutProtectedFields();
                output.append("<pre class='json'>\n");
                output.append(new GsonBuilder().setPrettyPrinting()
                        .create().toJson(jsonThing.asJsonObject(createableExampleThing)));
                output.append("</pre>\n");

                // todo: conditional output on if API supports XML responses
                output.append("<p>Example XML Input to API calls</p>\n");
                output.append("<pre class='xml'>\n");
                output.append(xmlThing.prettyPrintHtml(xmlThing.getSingleObjectXml(createableExampleThing)));
                output.append("</pre>\n");

            }
        }

        if (relationships != null) {
            output.append(heading(3, "Relationships"));
            output.append("<ul>\n");

            for (RelationshipDefinition relationship : relationships) {


                //task-of : task => project
                String reportLine = String.format("<li>%s : %s => %s%n",
                        relationship.getName(),
                        relationship.from().definition().getName(),
                        relationship.to().getName());


                // for a two way relationship can it be combined on to one line e.g.
                // tasks/task-of : project =(tasks)=> task  / task=(task-of)=> project

                if (relationship.isTwoWay()) {
                    reportLine = String.format("<li>%1$s/%2$s : %3$s =(%1$s)=> %4$s / %4$s =(%2$s)=> %3$s %n",
                            relationship.getName(),
                            relationship.getReversedRelationship().getName(),
                            relationship.from().definition().getName(),
                            relationship.to().getName());
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
        output.append(paragraph("Show this documentation as HTML."));

        if(additionalRoutes!=null) {
            for (RoutingDefinition route : additionalRoutes) {
                output.append(heading(4, route.url()));
                output.append(paragraph("e.g. <span class='endpoint'>" + url(route.url()) + "</span"));
                output.append(paragraph(route.getDocumentation()));
            }
        }

        output.append(mainMenu.getPageFooter());
        output.append(mainMenu.getPageEnd());
        return output.toString();
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
