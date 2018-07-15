package uk.co.compendiumdev.thingifier.reporting;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.RoutingVerb;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.generic.definitions.validation.ValidationRule;
import uk.co.compendiumdev.thingifier.generic.instances.RelationshipInstance;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ThingReporter {

    private Thingifier thingifier;
    private Collection<Thing> things;
    private Collection<RelationshipDefinition> relationships;

    public ThingReporter(Map<String, Thing> things,
                         Map<String, RelationshipDefinition>  relationships) {

        this.things = things.values();
        this.relationships = relationships.values();
    }

    public ThingReporter(Thingifier thingifier) {
        this.things = thingifier.getThings();
        this.relationships = thingifier.getRelationshipDefinitions();
        this.thingifier = thingifier;
    }

    public String basicReport() {
        StringBuilder output = new StringBuilder();

        output.append("\nThings:\n");
        output.append("=======\n");

        for(Thing aThing : things){
            output.append(aThing.definition());
        }


        output.append("\nRelationships\n");
        output.append("=============\n");

        for(RelationshipDefinition aRelationship : relationships){
            output.append(aRelationship);
        }

        output.append("\nInstances\n");
        output.append("=========\n");

        for(Thing aThing : things){

            output.append("## Of " + aThing.definition().getName() + "\n");

            for(ThingInstance anInstance : aThing.getInstances()) {
                output.append(anInstance);
            }
        }


        return output.toString();
    }

    public String getApiDocumentation(ApiRoutingDefinition routingDefinitions) {

        StringBuilder output = new StringBuilder();

        if(thingifier!=null) {
            // create generic API documentation
            output.append(heading(1, thingifier.getTitle()));
            output.append(String.format("%n"));
            output.append(paragraph(thingifier.getInitialParagraph()));
            output.append(String.format("%n"));
        }

        output.append(heading(2, "Model"));

        if(things!=null){
            output.append(heading(3, "Things"));
            for(Thing aThing : things){
                output.append(heading(4,aThing.definition().getName()));

                output.append("Fields:\n");
                output.append("<ul>\n");

                for(String aField : aThing.definition().getFieldNames()){

                    output.append("<li>" + aField + "\n");

                    output.append("<ul>\n");
                    for(ValidationRule validation : aThing.definition().getField(aField).validationRules()){
                        output.append("<li>" + validation.getErrorMessage("") + "</li>\n");

                    }
                    output.append("</ul>\n");
                    output.append("</li>\n");
                }
                output.append("</ul>\n");
            }
        }

        if(relationships!=null){
            output.append(heading(3, "Relationships"));
            output.append("<ul>\n");

            for(RelationshipDefinition relationship : relationships){

                output.append(String.format("<li>%s : %s => %s%n",
                                relationship.getName(),
                                relationship.from().definition().getName(),
                                relationship.to().getName()));
            }
            output.append("</ul>\n");

        }

        // output the API documentation
        output.append(heading(2, "API"));

        output.append(paragraph("The API takes JSON body with objects using the field definitions shown in the model.\n"));
        output.append(paragraph("e.g. {\"guid\", \"1234-1234-1234-1234\"}.\n"));

        output.append(heading(3, "End Points"));

        String currentEndPoint = "";

        for(RoutingDefinition routingDefn : routingDefinitions.definitions()){
            // only show if not a method not allowed method
            if(!currentEndPoint.equalsIgnoreCase(routingDefn.url())){
                // new endpoint
                output.append(heading(4, String.format("/%s%n", routingDefn.url())));
                currentEndPoint = routingDefn.url();
            }
            if(routingDefn.status().isReturnedFromCall() || routingDefn.status().value()!=405) {
                // ignore options
                if(routingDefn.verb()!=RoutingVerb.OPTIONS) {
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



        return output.toString();
    }

    private String paragraph(String initialParagraph) {
        return String.format("<p>%s</p>%n", initialParagraph);
    }


    // Template functions
    private String heading(int level, String text){
            return String.format("<h%1$d>%2$s</h%1$d>%n",level, text);
    }
}
