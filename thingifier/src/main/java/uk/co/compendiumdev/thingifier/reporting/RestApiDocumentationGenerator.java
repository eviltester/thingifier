package uk.co.compendiumdev.thingifier.reporting;

import com.google.gson.GsonBuilder;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.routings.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;
import uk.co.compendiumdev.thingifier.application.ThingifierVersionDetails;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.generic.definitions.validation.ValidationRule;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.Collection;
import java.util.List;

public class RestApiDocumentationGenerator {
    private final Thingifier thingifier;
    private final List<Thing> things;
    private final Collection<RelationshipDefinition> relationships;
    private final JsonThing jsonThing;

    public RestApiDocumentationGenerator(final Thingifier aThingifier) {
        this.thingifier = aThingifier;
        this.things = thingifier.getThings();
        this.relationships = thingifier.getRelationshipDefinitions();
        jsonThing = new JsonThing(thingifier.apiConfig().jsonOutput());
    }

    public String getApiDocumentation(final ApiRoutingDefinition routingDefinitions) {

        StringBuilder output = new StringBuilder();

        output.append(paragraph("Menu: " + href("/gui", "gui")));

        if (thingifier != null) {
            // create generic API documentation
            output.append(heading(1, thingifier.getTitle()));
            output.append(String.format("%n"));
            output.append(paragraph(thingifier.getInitialParagraph()));
            output.append(String.format("%n"));
        }

        output.append(heading(2, "Model"));

        if (things != null) {
            output.append(heading(3, "Things"));
            for (Thing aThing : things) {
                output.append(heading(4, aThing.definition().getName()));

                output.append("Fields:\n");
                output.append("<ul>\n");

                // todo: generate an example Thing
                final ThingInstance exampleThing = new ThingInstance(aThing.definition());

                for (String aField : aThing.definition().getFieldNames()) {

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


                // show an example
                output.append("<p>Example Output</p>\n");
                output.append("<pre>\n");
                output.append(new GsonBuilder().setPrettyPrinting()
                        .create().toJson(jsonThing.asJsonObject(exampleThing)));
                output.append("</pre>\n");

                output.append("<p>Example Input</p>\n");
                exampleThing.clearNonProtectedFields();
                output.append("<pre>\n");
                output.append(new GsonBuilder().setPrettyPrinting()
                        .create().toJson(jsonThing.asJsonObject(exampleThing)));
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

        output.append(paragraph("The API takes JSON body with objects using the field definitions shown in the model.\n"));
        output.append(paragraph("e.g. <code>{\"guid\": \"1234-1234-1234-1234\"}</code>\n"));
        output.append(paragraph("Or, for XML\n"));
        String codeSnippet = "<%1$s><guid><1234-1234-1234-1234></guid></%1$s>".replace("<", "&lt;");

        output.append(paragraph(String.format("e.g. <code>" + codeSnippet + "</code>\n", thingifier.getThings().get(0).definition().getName())));

        output.append(heading(3, "End Points"));

        String currentEndPoint = "";

        for (RoutingDefinition routingDefn : routingDefinitions.definitions()) {
            // only show if not a method not allowed method
            if (!currentEndPoint.equalsIgnoreCase(routingDefn.url())) {
                // new endpoint
                output.append(heading(4, String.format("/%s%n", routingDefn.url())));
                currentEndPoint = routingDefn.url();
            }
            if (routingDefn.status().isReturnedFromCall() || routingDefn.status().value() != 405) {
                // ignore options
                if (routingDefn.verb() != RoutingVerb.OPTIONS) {
                    output.append(String.format("<ul>%n<li>%n<strong>%s %s</strong><ul><li>%s</li></ul></li>%n</ul>",
                            routingDefn.verb(), routingDefn.url(), routingDefn.getDocumentation()));

                    //output.append(heading(5, String.format("%s %s%n", routingDefn.verb(), routingDefn.url())));
                    //output.append(paragraph(routingDefn.getDocumentation()));
                }
            }
        }

        output.append(heading(4, "/"));
        output.append(paragraph("Show this documentation as HTML."));

        output.append(heading(4, "/shutdown"));
        output.append(paragraph("Shutdown the server."));

        output.append("<br/><hr/>");
        output.append(paragraph(
                String.format(
                        "Thingifier version %s, Copyright Alan Richardson, Compendium Developments Ltd %s ",
                        ThingifierVersionDetails.VERSION_NUMBER,
                        ThingifierVersionDetails.COPYRIGHT_YEAR)));
        output.append(paragraph(href("Thingifier", "https://github.com/eviltester/thingifier")));
        output.append(paragraph(href("EvilTester.com", "http://eviltester.com")));
        output.append(paragraph(href("Compendium Developments", "https://compendiumdev.co.uk")));

        return output.toString();
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
