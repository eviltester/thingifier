package uk.co.compendiumdev.thingifier.htmlgui.htmlgen;

import com.google.gson.GsonBuilder;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.xml.GenericXMLPrettyPrinter;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.ValidationRule;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.XmlThing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RestApiDocumentationGenerator {
    private final Thingifier thingifier;
    private final Collection<RelationshipDefinition> relationships;
    private final JsonThing jsonThing;
    private final XmlThing xmlThing;
    private final DefaultGUIHTML defaultGui;
    private final ThingifierApiConfig apiConfig;
    private final GenericXMLPrettyPrinter XMLPrettyPrinter;
    private String prependPath;

    // TODO: DefaultGUIHTML is hard coded in here, need more flexibility
    // around GUIs to allow hooks and other main classes to expand it
    // possibly a GuiHtml with ability to set meta tags, title, register menu items, change footers etc.
    // start with a menu and register menu items and return menu html

    public RestApiDocumentationGenerator(final Thingifier aThingifier, DefaultGUIHTML defaultGui) {
        this.thingifier = aThingifier;
        this.relationships = thingifier.getRelationshipDefinitions();
        apiConfig = thingifier.apiConfig();
        jsonThing = new JsonThing(apiConfig.jsonOutput());
        xmlThing = new XmlThing(jsonThing);
        this.defaultGui = defaultGui;
        prependPath = "";
        this.XMLPrettyPrinter = new GenericXMLPrettyPrinter();
    }

    public String getApiDocumentation(final ApiRoutingDefinition routingDefinitions,
                                      final List<RoutingDefinition> additionalRoutes,
                                      final String urlPath, String canonicalUrl) {

        StringBuilder output = new StringBuilder();

        output.append(defaultGui.getPageStart("API Documentation", "", canonicalUrl));
        output.append(defaultGui.getMenuAsHTML());
        output.append(defaultGui.getStartOfMainContentMarker());


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

            // the following is fully configurable by api docs config
            if(!thingifier.apidocsconfig().headerSectionOverride().isEmpty()){

                output.append(thingifier.apidocsconfig().headerSectionOverride());

            }else{
                // e.g. if XML is not supported then do not show info about XML
                if (thingifier.apiConfig().willAllowJsonAsDefaultContentType()) {
                    output.append(paragraph("Will accept json by default."));
                } else {
                    output.append(paragraph("Use the <i>Content-Type</i> header to define the payload content e.g."));
                }
                output.append(paragraph("<i>Content-Type: application/json</i>"));
                output.append(paragraph("Set Content-Type header to application/xml if you want to send in XML."));
                output.append(paragraph("<i>Content-Type: application/xml</i>"));

                if (thingifier.apiConfig().willApiAllowXmlForResponses() &&
                        thingifier.apiConfig().willApiAllowJsonForResponses()) {
                    output.append(paragraph("You can control the returned data format by setting the Accept header"));
                }

                if (thingifier.apiConfig().willApiAllowXmlForResponses() &&
                        !thingifier.apiConfig().willApiAllowJsonForResponses()) {
                    output.append(paragraph("Returned data format will be XML by default."));
                }

                if (thingifier.apiConfig().willApiAllowXmlForResponses()) {
                    output.append(paragraph("You can request XML response by setting the Accept header."));
                    output.append(paragraph("i.e. for XML use"));
                    output.append(paragraph("<i>Accept: application/xml</i><br/><br/>\n"));
                }

                if (!thingifier.apiConfig().willApiAllowXmlForResponses() &&
                        thingifier.apiConfig().willApiAllowJsonForResponses()) {
                    output.append(paragraph("You receive JSON by default as the response"));
                }

                if (thingifier.apiConfig().willApiAllowJsonForResponses()) {
                    output.append(paragraph("You can request JSON response by setting the Accept header."));
                    output.append(paragraph("i.e. for JSON use"));
                    output.append(paragraph("<i>Accept: application/json</i><br/><br/>\n"));
                }

                if (thingifier.apiConfig().forParams().willAllowFilteringThroughUrlParams()) {

                    Collection<EntityDefinition> defns = thingifier.getERmodel().getEntityDefinitions();
                    if(!defns.isEmpty()) {
                        output.append(paragraph("Some requests can be filtered by adding query params of fieldname=value. Where only matching items will be returned."));

                        // TODO: generate the filter example string from the entity definitions
                        //defns.toArray()
                        output.append(paragraph("e.g. <i>/thing?size=2&status=true</i><br/><br/>\n"));
                    }
                }

                if (!thingifier.apidocsconfig().headerSectionAppend().isEmpty()) {
                    output.append(paragraph("All data lives in memory and is not persisted so the application is cleared everytime you start it. It does have some test data in here when you start"));
                } else {
                    output.append(thingifier.apidocsconfig().headerSectionAppend());
                }
            }

            output.append("</div>");
        }



        Collection<EntityDefinition> definitions = thingifier.getERmodel().getEntityDefinitions();

        if (definitions != null && !definitions.isEmpty()) {
            output.append(heading(2, "Model"));
            output.append(heading(3, "Things"));
            for (EntityDefinition aThingDefinition : definitions) {

                output.append(heading(4, aThingDefinition.getName()));

                output.append("Fields:\n");

                final DocumentationThingInstance exampleThing = new DocumentationThingInstance(aThingDefinition);

                output.append("<table>\n");
                output.append("<thead>\n");
                output.append("<tr>");
                output.append("<td>Fieldname</td>\n");
                output.append("<td>Type</td>\n");
                output.append("<td>Validation</td>\n");
                output.append("</tr>");
                output.append("</thead>\n");



                output.append("<tbody>\n");

                for (String aField : aThingDefinition.getFieldNames()) {

                    output.append("<tr>");
                    // todo: add list of hidden fields in the api and avoid showing them here
//                    if(apiConfig.hasHiddenFieldsForEntity(aThingDefinition.getName()) && apiConfig.isApiFieldHidden(aThingDefinition.getName(), aField)){
//                        continue;
//                    }

                    output.append(String.format("<td>%s</td>", aField));

                    Field theField = aThingDefinition.getField(aField);
                    output.append(String.format("<td>%s</td>", theField.getType()));

                    output.append("<td>");

                    output.append("<ul>");
                    for (ValidationRule validation : theField.getAllValidationRules()) {
                        //use the validation error message in the documentation
                        output.append("<li>" + validation.getExplanation() + "</li>\n");
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
                    if(thingifier.apiConfig().willReturnSingleGetItemsAsCollection()){
                        output.append(new GsonBuilder().setPrettyPrinting()
                                .create().toJson(jsonThing.asJsonObjectTypedArrayWithContentsUntyped(
                                        List.of(exampleThing.getInstance()),aThingDefinition.getPlural())));
                    }else {
                        output.append(new GsonBuilder().setPrettyPrinting()
                                .create().toJson(jsonThing.asJsonObject(exampleThing.getInstance())));
                    }
                    output.append("</code>\n");
                    output.append("</pre>\n");
                }

                if(thingifier.apiConfig().willApiAllowXmlForResponses()) {
                    output.append("<p>Example XML Output from API calls</p>\n");
                    output.append("<pre class='xml'>\n");
                    output.append("<code class='xml'>\n");
                    if(thingifier.apiConfig().willReturnSingleGetItemsAsCollection()) {
                        output.append(this.XMLPrettyPrinter.prettyPrintHtml(
                                xmlThing.getCollectionOfThings(
                                        List.of(exampleThing.getInstance()),
                                        aThingDefinition)));
                    }else{
                        output.append(this.XMLPrettyPrinter.prettyPrintHtml(
                                xmlThing.getSingleObjectXml(exampleThing.getInstance())));
                    }
                    output.append("</code>\n");
                    output.append("</pre>\n");
                }

                output.append("<p>Example JSON Input to API calls</p>\n");
                EntityInstance createableExampleThing = exampleThing.withoutIDsOrGUIDs();
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
                output.append(this.XMLPrettyPrinter.prettyPrintHtml(xmlThing.getSingleObjectXml(createableExampleThing)));
                output.append("</code>\n");
                output.append("</pre>\n");

            }
        }

        if (relationships != null && !relationships.isEmpty()) {
            output.append(heading(3, "Relationships"));
            output.append("<ul>\n");

            for (RelationshipDefinition relationship : relationships) {

                RelationshipVectorDefinition fromToRelationship = relationship.getFromRelationship();

                //task-of : task => project
                String reportLine = String.format("<li>%s : %s => %s%n",
                        fromToRelationship.getName(),
                        fromToRelationship.getFrom().getName(),
                        fromToRelationship.getTo().getName());


                // for a two way relationship can it be combined on to one line e.g.
                // tasks/task-of : project =(tasks)=> task  / task=(task-of)=> project

                if (relationship.isTwoWay()) {
                    reportLine = String.format("<li>%1$s/%2$s : %3$s =(%1$s)=> %4$s / %4$s =(%2$s)=> %3$s %n",
                            relationship.getFromRelationship().getName(),
                            relationship.getReversedRelationship().getName(),
                            relationship.getFromRelationship().getFrom().getName(),
                            relationship.getFromRelationship().getTo().getName());
                }

                output.append(reportLine);
            }
            output.append("</ul>\n");

        }

        // output the API documentation
        output.append(heading(2, "API"));

        if(thingifier.apidocsconfig().apiIntroductionParaOverride().isEmpty()) {
            output.append(paragraph("The API takes body with objects using the field definitions and examples shown in the model."));
        }else{
            output.append(thingifier.apidocsconfig().apiIntroductionParaOverride());
        }

        output.append(heading(3, "End Points"));

        String currentEndPoint = "";

        for (RoutingDefinition routingDefn : routingDefinitions.definitions()) {
            // only show if not a method not allowed method
            if (!currentEndPoint.equalsIgnoreCase(routingDefn.url())) {
                // new endpoint
                output.append(heading(4, "endpoint", "/" + routingDefn.url()));
                output.append(paragraph("e.g. <span class='endpoint'>" + url(routingDefn.url()) + "</span>"));

                if(routingDefn.isFilterable() &&
                        thingifier.apiConfig().forParams().willAllowFilteringThroughUrlParams()){

                    // we are allowed to filter url
                    output.append(paragraph("This endpoint can be filtered with fields as URL Query Parameters."));
                    String exampleFilter = getExampleFilter(routingDefn.getFilterableEntity());
                    if(exampleFilter!=null && !exampleFilter.isEmpty()){
                        output.append(paragraph("e.g. <span class='endpoint'>" + url(routingDefn.url()) + exampleFilter + "</span>"));
                    }
                }

                currentEndPoint = routingDefn.url();
            }
            if (routingDefn.status().isReturnedFromCall() || routingDefn.status().value() != 405) {
                // ignore options
                boolean show = true;
                if(thingifier.apidocsconfig().ignoreOptionsVerb() && routingDefn.verb() == RoutingVerb.OPTIONS){
                    show = false;
                }
                if (show) {
                    output.append(String.format("<ul>%n<li class='endpoint'>%n<strong>%s /%s</strong><ul><li class='normal'>%s</li></ul></li>%n</ul>",
                            routingDefn.verb(), routingDefn.url(), routingDefn.getDocumentation()));

                }
            }
        }

        // TODO: consider if we want to add an 'optional' HTML injest as a param into this method
        // Not sure why we hard-coded docs (which is html) into the API documentation
//        output.append(heading(4, "/docs"));
//        output.append(paragraph("e.g. <span class='endpoint'>" + url("/docs") + "</span>"));
//        output.append(String.format("<ul>%n<li class='endpoint'>%n<strong>%s /%s</strong><ul><li class='normal'>%s</li></ul></li>%n</ul>",
//                "GET", url("/docs"), "Show this documentation as HTML."));

        List<String> processedAdditionalRoutes = new ArrayList<>();
        if(additionalRoutes!=null) {
            for (RoutingDefinition route : additionalRoutes) {
                if (!processedAdditionalRoutes.contains(route.url())){
                    output.append(heading(4, "/" + route.url()));
                    processedAdditionalRoutes.add(route.url());
                    output.append(paragraph("e.g. <span class='endpoint'>" + url(route.url()) + "</span>"));

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

        output.append(paragraph(href("[download normal swagger file]",prependPath + "/docs/swagger")));
        output.append(paragraph(href("[download swagger file with less validation]",
                prependPath + "/docs/swagger?permissive"))
        );

        output.append(defaultGui.getEndOfMainContentMarker());
        output.append(defaultGui.getPageFooter());
        output.append(defaultGui.getPageEnd());
        return output.toString();
    }

    private String getExampleFilter(final EntityDefinition filterableEntity) {
        String exampleFilters = "";
        List<Field> exampleFields = new ArrayList<>();
        List<Integer> indexes = new ArrayList<>();
        Random random = new Random();

        // todo: ignore strings unless none added, in which case add the strings
        for(String fieldName : filterableEntity.getFieldNames()){
            Field field = filterableEntity.getField(fieldName);
            if(field.getType() != FieldType.AUTO_INCREMENT && field.getType()!= FieldType.AUTO_GUID){
                // we can filter on guid and id, but don't use those as examples
                if(exampleFields.isEmpty() || random.nextBoolean()){
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

        return  midPath + postUrl;
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
