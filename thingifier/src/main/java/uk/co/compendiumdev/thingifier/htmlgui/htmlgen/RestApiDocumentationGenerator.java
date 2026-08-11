package uk.co.compendiumdev.thingifier.htmlgui.htmlgen;

import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.XmlThing;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.xml.GenericXMLPrettyPrinter;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityViewDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.ValidationRule;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.query.PaginationParams;
import uk.co.compendiumdev.thingifier.core.query.SortByFieldName;

public class RestApiDocumentationGenerator {
    private static final String DEFAULT_CANONICAL_HOST = "https://apichallenges.eviltester.com";
    private static final String DEFAULT_SITE_NAME = "API Challenges";
    private static final String DEFAULT_OG_IMAGE_PATH =
            "/images/social/apichallenges-og-1200x630.png";
    private static final String DEFAULT_META_ROBOTS = "index,follow";
    private static final String DEFAULT_OG_TYPE = "website";
    private static final String DEFAULT_TWITTER_CARD = "summary_large_image";
    private static final String MERMAID_ESM_CDN =
            "https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.esm.min.mjs";
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
    // possibly a GuiHtml with ability to set meta tags, title, register menu items, change footers
    // etc.
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

    public String getApiDocumentation(
            final ApiRoutingDefinition routingDefinitions,
            final List<RoutingDefinition> additionalRoutes,
            final ThingifierApiDocumentationDefn apiDocDefn,
            final String urlPath,
            String canonicalUrl) {

        StringBuilder output = new StringBuilder();

        final String htmlTitle = resolveDocsTitle(apiDocDefn);
        final String htmlDescription = resolveDocsDescription(apiDocDefn);
        final String headInject =
                buildDocsHeadInject(apiDocDefn, htmlTitle, htmlDescription, canonicalUrl);

        output.append(defaultGui.getPageStart(htmlTitle, headInject, canonicalUrl));
        output.append(defaultGui.getMenuAsHTML());
        output.append(defaultGui.getStartOfMainContentMarker());

        if (urlPath != null) {
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
            if (!thingifier.apidocsconfig().headerSectionOverride().isEmpty()) {

                output.append(thingifier.apidocsconfig().headerSectionOverride());

            } else {
                // e.g. if XML is not supported then do not show info about XML
                if (thingifier.apiConfig().willAllowJsonAsDefaultContentType()) {
                    output.append(paragraph("Will accept json by default."));
                } else {
                    output.append(
                            paragraph(
                                    "Use the <i>Content-Type</i> header to define the payload content e.g."));
                }
                output.append(paragraph("<i>Content-Type: application/json</i>"));
                output.append(
                        paragraph(
                                "Set Content-Type header to application/xml if you want to send in XML."));
                output.append(paragraph("<i>Content-Type: application/xml</i>"));

                if (thingifier.apiConfig().willApiAllowXmlForResponses()
                        && thingifier.apiConfig().willApiAllowJsonForResponses()) {
                    output.append(
                            paragraph(
                                    "You can control the returned data format by setting the Accept header"));
                }

                if (thingifier.apiConfig().willApiAllowXmlForResponses()
                        && !thingifier.apiConfig().willApiAllowJsonForResponses()) {
                    output.append(paragraph("Returned data format will be XML by default."));
                }

                if (thingifier.apiConfig().willApiAllowXmlForResponses()) {
                    output.append(
                            paragraph(
                                    "You can request XML response by setting the Accept header."));
                    output.append(paragraph("i.e. for XML use"));
                    output.append(
                            paragraph(
                                    acceptHeaderExample(AcceptHeaderParser.ACCEPT_TYPE.XML)
                                            + "<br/><br/>\n"));
                }

                if (!thingifier.apiConfig().willApiAllowXmlForResponses()
                        && thingifier.apiConfig().willApiAllowJsonForResponses()) {
                    output.append(paragraph("You receive JSON by default as the response"));
                }

                if (thingifier.apiConfig().willApiAllowJsonForResponses()) {
                    output.append(
                            paragraph(
                                    "You can request JSON response by setting the Accept header."));
                    output.append(paragraph("i.e. for JSON use"));
                    output.append(
                            paragraph(
                                    acceptHeaderExample(AcceptHeaderParser.ACCEPT_TYPE.JSON)
                                            + "<br/><br/>\n"));
                }

                output.append(paragraph("Additional response Accept headers are supported."));
                output.append(paragraph(additionalResponseAcceptHeaders()));

                if (thingifier.apiConfig().forParams().willAllowFilteringThroughUrlParams()) {

                    Collection<EntityDefinition> defns =
                            thingifier.getERmodel().getEntityDefinitions();
                    if (!defns.isEmpty()) {
                        output.append(
                                paragraph(
                                        "Some requests can be filtered by adding query params of fieldname=value. Where only matching items will be returned."));
                        output.append(
                                paragraph(
                                        "Filter conditions can use <i>field=value</i> for equals, <i>field!=value</i> or <i>field!value</i> for not equals, <i>field&lt;value</i>, <i>field&gt;value</i>, <i>field&lt;=value</i>, and <i>field&gt;=value</i> for comparisons, <i>field~=regex</i> for regular expression matches, and <i>field*=wildcard</i> for wildcard matches where <i>*</i> matches many characters and <i>?</i> matches one character. Multiple query params are combined as AND conditions."));

                        // TODO: generate the filter example string from the entity definitions
                        // defns.toArray()
                        output.append(
                                paragraph("e.g. <i>/thing?size=2&status=true</i><br/><br/>\n"));
                        output.append(
                                paragraph(
                                        "Some requests can be sorted by adding the <i>"
                                                + SortByFieldName.PARAMETER_NAME
                                                + "</i> query param with a field name. Use <i>"
                                                + SortByFieldName.PARAMETER_NAME
                                                + "=+field</i> or <i>"
                                                + SortByFieldName.PARAMETER_NAME
                                                + "=field</i> for ascending order, and <i>"
                                                + SortByFieldName.PARAMETER_NAME
                                                + "=-field</i> for descending order. Multiple"
                                                + " fields can be combined with commas, e.g. <i>"
                                                + SortByFieldName.PARAMETER_NAME
                                                + "=+field,-other</i>."));
                    }
                }

                if (thingifier.apiConfig().forParams().willAllowPagingThroughUrlParams()) {
                    output.append(
                            paragraph(
                                    "Collection requests can be paged with <i>"
                                            + PaginationParams.LIMIT_PARAMETER_NAME
                                            + "=limit</i> and <i>"
                                            + PaginationParams.OFFSET_PARAMETER_NAME
                                            + "=offset</i>. Offset is zero-based, the default"
                                            + " limit is "
                                            + thingifier
                                                    .apiConfig()
                                                    .forParams()
                                                    .defaultPagingLimit()
                                            + ", and the maximum limit is "
                                            + thingifier.apiConfig().forParams().maxPagingLimit()
                                            + "."));
                }

                if (!thingifier.apidocsconfig().headerSectionAppend().isEmpty()) {
                    output.append(
                            paragraph(
                                    "All data lives in memory and is not persisted so the application is cleared everytime you start it. It does have some test data in here when you start"));
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
                if (aThingDefinition.hasDescription()) {
                    output.append(paragraph(escapeHtmlText(aThingDefinition.getDescription())));
                }

                output.append("Fields:\n");

                final DocumentationThingInstance exampleThing =
                        new DocumentationThingInstance(aThingDefinition);

                output.append("<table>\n");
                output.append("<thead>\n");
                output.append("<tr>");
                output.append("<td>Fieldname</td>\n");
                output.append("<td>Type</td>\n");
                output.append("<td>Description</td>\n");
                output.append("</tr>");
                output.append("</thead>\n");

                output.append("<tbody>\n");

                for (String aField : aThingDefinition.getFieldNames()) {

                    output.append("<tr>");
                    // todo: add list of hidden fields in the api and avoid showing them here
                    //
                    // if(apiConfig.hasHiddenFieldsForEntity(aThingDefinition.getName()) &&
                    // apiConfig.isApiFieldHidden(aThingDefinition.getName(), aField)){
                    //                        continue;
                    //                    }

                    output.append(String.format("<td>%s</td>", aField));

                    Field theField = aThingDefinition.getField(aField);
                    output.append(String.format("<td>%s</td>", theField.getType()));

                    output.append("<td>");

                    output.append("<ul>");
                    if (theField.hasDescription()) {
                        output.append(
                                "<li>" + escapeHtmlText(theField.getDescription()) + "</li>\n");
                    }
                    for (ValidationRule validation : theField.getAllValidationRules()) {
                        // use the validation error message in the documentation
                        output.append(
                                "<li>" + escapeHtmlText(validation.getExplanation()) + "</li>\n");
                    }

                    output.append("</ul>\n");
                    output.append("</td>\n");

                    output.append("</tr>");

                    String exampleValue = theField.getRandomExampleValue();
                    exampleThing.overrideValue(theField.getName(), exampleValue);

                    output.append(
                            String.format(
                                    "<tr><td colspan='3' class='examplevalue'>Example: \"%s\"</td></tr>",
                                    exampleValue));
                }

                output.append("</tbody>\n");
                output.append("</table>\n");

                if (!aThingDefinition.getViews().isEmpty()) {
                    output.append("Views:\n");
                    output.append("<table>\n");
                    output.append("<thead>\n");
                    output.append("<tr>");
                    output.append("<td>View</td>\n");
                    output.append("<td>Request Fields</td>\n");
                    output.append("<td>Response Fields</td>\n");
                    output.append("<td>Input Allowed Fields</td>\n");
                    output.append("</tr>");
                    output.append("</thead>\n");
                    output.append("<tbody>\n");
                    for (EntityViewDefinition view : aThingDefinition.getViews()) {
                        output.append("<tr>");
                        output.append(String.format("<td>%s</td>", escapeHtmlText(view.getName())));
                        output.append(
                                String.format(
                                        "<td>%s</td>",
                                        escapeHtmlText(
                                                fieldsInView(
                                                        aThingDefinition,
                                                        view::isRequestVisible))));
                        output.append(
                                String.format(
                                        "<td>%s</td>",
                                        escapeHtmlText(
                                                fieldsInView(
                                                        aThingDefinition,
                                                        view::isResponseVisible))));
                        output.append(
                                String.format(
                                        "<td>%s</td>",
                                        escapeHtmlText(
                                                fieldsInView(
                                                        aThingDefinition, view::isInputAllowed))));
                        output.append("</tr>");
                    }
                    output.append("</tbody>\n");
                    output.append("</table>\n");
                }

                // show an example
                if (thingifier.apiConfig().willApiAllowJsonForResponses()) {
                    output.append("<p>Example JSON Output from API calls</p>\n");
                    output.append("<pre class='json'>\n");
                    output.append("<code class='json'>\n");
                    if (thingifier.apiConfig().willReturnSingleGetItemsAsCollection()) {
                        output.append(
                                new GsonBuilder()
                                        .setPrettyPrinting()
                                        .create()
                                        .toJson(
                                                jsonThing
                                                        .asJsonObjectTypedDraftArrayWithContentsUntyped(
                                                                List.of(exampleThing.getDraft()),
                                                                aThingDefinition.getPlural())));
                    } else {
                        output.append(
                                new GsonBuilder()
                                        .setPrettyPrinting()
                                        .create()
                                        .toJson(jsonThing.asJsonObject(exampleThing.getDraft())));
                    }
                    output.append("</code>\n");
                    output.append("</pre>\n");
                }

                if (thingifier.apiConfig().willApiAllowXmlForResponses()) {
                    output.append("<p>Example XML Output from API calls</p>\n");
                    output.append("<pre class='xml'>\n");
                    output.append("<code class='xml'>\n");
                    if (thingifier.apiConfig().willReturnSingleGetItemsAsCollection()) {
                        output.append(
                                this.XMLPrettyPrinter.prettyPrintHtml(
                                        xmlThing.getCollectionOfDrafts(
                                                List.of(exampleThing.getDraft()),
                                                aThingDefinition)));
                    } else {
                        output.append(
                                this.XMLPrettyPrinter.prettyPrintHtml(
                                        xmlThing.getSingleObjectXml(exampleThing.getDraft())));
                    }
                    output.append("</code>\n");
                    output.append("</pre>\n");
                }

                output.append("<p>Example JSON Input to API calls</p>\n");
                EntityInstanceDraft createableExampleThing = exampleThing.withoutIDsOrGUIDs();
                output.append("<pre class='json'>\n");
                output.append("<code class='json'>\n");
                output.append(
                        new GsonBuilder()
                                .setPrettyPrinting()
                                .create()
                                .toJson(jsonThing.asJsonObject(createableExampleThing)));
                output.append("</code>\n");
                output.append("</pre>\n");

                // todo: conditional output on if API supports XML responses
                output.append("<p>Example XML Input to API calls</p>\n");
                output.append("<pre class='xml'>\n");
                output.append("<code class='xml'>\n");
                output.append(
                        this.XMLPrettyPrinter.prettyPrintHtml(
                                xmlThing.getSingleObjectXml(createableExampleThing)));
                output.append("</code>\n");
                output.append("</pre>\n");
            }
        }

        if (relationships != null && !relationships.isEmpty()) {
            output.append(heading(3, "Relationships"));
            output.append(mermaidErDiagram());
            output.append("<ul>\n");

            for (RelationshipDefinition relationship : relationships) {

                output.append(relationshipLine(relationship.getFromRelationship()));
                if (relationship.isTwoWay()) {
                    output.append(relationshipLine(relationship.getReversedRelationship()));
                }
            }
            output.append("</ul>\n");
            output.append(mermaidEsmScript());
        }

        // output the API documentation
        output.append(heading(2, "API"));

        if (thingifier.apidocsconfig().apiIntroductionParaOverride().isEmpty()) {
            output.append(
                    paragraph(
                            "The API takes body with objects using the field definitions and examples shown in the model."));
        } else {
            output.append(thingifier.apidocsconfig().apiIntroductionParaOverride());
        }

        output.append(heading(3, "End Points"));

        String currentEndPoint = "";

        for (RoutingDefinition routingDefn : routingDefinitions.definitions()) {
            if (routingDefn.isHiddenFromDocumentation() || routingDefn.isDisabled()) {
                continue;
            }
            // only show if not a method not allowed method
            if (!currentEndPoint.equalsIgnoreCase(routingDefn.url())) {
                // new endpoint
                output.append(heading(4, "endpoint", "/" + routingDefn.url()));
                output.append(
                        paragraph(
                                "e.g. <span class='endpoint'>"
                                        + url(routingDefn.url())
                                        + "</span>"));

                if (routingDefn.isFilterable()
                        && thingifier
                                .apiConfig()
                                .forParams()
                                .willAllowFilteringThroughUrlParams()) {

                    // we are allowed to filter url
                    output.append(
                            paragraph(
                                    "This endpoint can be filtered with fields as URL Query Parameters."));
                    String exampleFilter = getExampleFilter(routingDefn.getFilterableEntity());
                    if (exampleFilter != null && !exampleFilter.isEmpty()) {
                        output.append(
                                paragraph(
                                        "e.g. <span class='endpoint'>"
                                                + url(routingDefn.url())
                                                + exampleFilter
                                                + "</span>"));
                    }

                    output.append(
                            paragraph(
                                    "This endpoint can be sorted with the <i>"
                                            + SortByFieldName.PARAMETER_NAME
                                            + "</i> URL Query Parameter. Use <i>"
                                            + SortByFieldName.PARAMETER_NAME
                                            + "=+field</i> or <i>"
                                            + SortByFieldName.PARAMETER_NAME
                                            + "=field</i> for ascending order, and <i>"
                                            + SortByFieldName.PARAMETER_NAME
                                            + "=-field</i> for descending order. Multiple fields"
                                            + " can be combined with commas, e.g. <i>"
                                            + SortByFieldName.PARAMETER_NAME
                                            + "=+field,-other</i>."));
                    String exampleSort = getExampleSort(routingDefn.getFilterableEntity());
                    if (exampleSort != null && !exampleSort.isEmpty()) {
                        output.append(
                                paragraph(
                                        "e.g. <span class='endpoint'>"
                                                + url(routingDefn.url())
                                                + exampleSort
                                                + "</span>"));
                    }
                }

                if (routingDefn.isFilterable()
                        && thingifier.apiConfig().forParams().willAllowPagingThroughUrlParams()) {
                    output.append(
                            paragraph(
                                    "This endpoint can be paged with the <i>"
                                            + PaginationParams.LIMIT_PARAMETER_NAME
                                            + "</i> and <i>"
                                            + PaginationParams.OFFSET_PARAMETER_NAME
                                            + "</i> URL Query Parameters."));
                    output.append(
                            paragraph(
                                    "e.g. <span class='endpoint'>"
                                            + url(routingDefn.url())
                                            + getExamplePage()
                                            + "</span>"));
                }

                currentEndPoint = routingDefn.url();
            }
            if (routingDefn.status().isReturnedFromCall() || routingDefn.status().value() != 405) {
                // ignore options
                boolean show = true;
                if (thingifier.apidocsconfig().ignoreOptionsVerb()
                        && routingDefn.verb() == RoutingVerb.OPTIONS) {
                    show = false;
                }
                if (show) {
                    output.append(
                            String.format(
                                    "<ul>%n<li class='endpoint'>%n<strong>%s /%s</strong><ul><li class='normal'>%s</li></ul></li>%n</ul>",
                                    routingDefn.verb(),
                                    routingDefn.url(),
                                    routingDefn.getDocumentation()));
                    if (routingDefn.verb() == RoutingVerb.QUERY) {
                        output.append(
                                paragraph(
                                        "QUERY form content uses <i>Content-Type: "
                                                + ThingifierHttpApi.QUERY_CONTENT_TYPE
                                                + "</i> with fields such as <i>title=Task&amp;"
                                                + SortByFieldName.PARAMETER_NAME
                                                + "=-id</i>."));
                        output.append(
                                paragraph(
                                        "QUERY JSONPath content uses <i>Content-Type: "
                                                + ThingifierHttpApi.JSONPATH_QUERY_CONTENT_TYPE
                                                + "</i> with an expression such as <i>"
                                                + jsonPathQueryExampleFor(routingDefn)
                                                + "</i>."));
                    }
                }
            }
        }

        // TODO: consider if we want to add an 'optional' HTML injest as a param into this method
        // Not sure why we hard-coded docs (which is html) into the API documentation
        //        output.append(heading(4, "/docs"));
        //        output.append(paragraph("e.g. <span class='endpoint'>" + url("/docs") +
        // "</span>"));
        //        output.append(String.format("<ul>%n<li class='endpoint'>%n<strong>%s
        // /%s</strong><ul><li class='normal'>%s</li></ul></li>%n</ul>",
        //                "GET", url("/docs"), "Show this documentation as HTML."));

        List<String> processedAdditionalRoutes = new ArrayList<>();
        if (additionalRoutes != null) {
            for (RoutingDefinition route : additionalRoutes) {
                if (!processedAdditionalRoutes.contains(route.url())) {
                    output.append(heading(4, "/" + route.url()));
                    processedAdditionalRoutes.add(route.url());
                    output.append(
                            paragraph(
                                    "e.g. <span class='endpoint'>" + url(route.url()) + "</span>"));

                    // handle all verbs for this route
                    for (RoutingDefinition subroute : additionalRoutes) {
                        if (subroute.url().contentEquals(route.url())) {
                            output.append(
                                    String.format(
                                            "<ul>%n<li class='endpoint'>%n<strong>%s /%s</strong><ul><li class='normal'>%s</li></ul></li>%n</ul>",
                                            subroute.verb(),
                                            subroute.url(),
                                            subroute.getDocumentation()));
                        }
                    }
                }
            }
        }

        if (apiDocDefn.willShowSwaggerUiLink()) {
            output.append(paragraph(href("Open Swagger UI", prependPath + "/docs/swagger-ui")));
        }
        if (apiDocDefn.willShowScalarUiLink()) {
            output.append(paragraph(href("Open Scalar UI", prependPath + "/docs/scalar-ui")));
        }
        output.append(openApiVersionLinks());

        output.append(defaultGui.getEndOfMainContentMarker());
        output.append(defaultGui.getPageFooter());
        output.append(defaultGui.getPageEnd());
        return output.toString();
    }

    private String relationshipLine(final RelationshipVectorDefinition relationship) {
        return String.format(
                "<li>%s : %s =(%s, max %s)=> %s</li>%n",
                escapeHtmlText(relationship.getName()),
                escapeHtmlText(relationship.getFrom().getName()),
                escapeHtmlText(relationship.getName()),
                escapeHtmlText(relationship.getCardinality().right()),
                escapeHtmlText(relationship.getTo().getName()));
    }

    private String fieldsInView(
            final EntityDefinition entity, final java.util.function.Predicate<String> included) {
        final List<String> fieldNames = new ArrayList<>();
        for (String fieldName : entity.getFieldNames()) {
            if (included.test(fieldName)) {
                fieldNames.add(fieldName);
            }
        }
        return String.join(", ", fieldNames);
    }

    private String mermaidErDiagram() {
        StringBuilder diagram = new StringBuilder();
        diagram.append("<pre class='mermaid'>\n");
        diagram.append("erDiagram\n");
        for (RelationshipDefinition relationship : relationships) {
            RelationshipVectorDefinition fromRelationship = relationship.getFromRelationship();
            diagram.append("    ")
                    .append(mermaidEntityId(fromRelationship.getFrom()))
                    .append(" ")
                    .append(mermaidLeftCardinalityMarker(fromRelationship.getCardinality().left()))
                    .append("--")
                    .append(
                            mermaidRightCardinalityMarker(
                                    fromRelationship.getCardinality().right()))
                    .append(" ")
                    .append(mermaidEntityId(fromRelationship.getTo()))
                    .append(" : ")
                    .append(mermaidRelationshipLabel(fromRelationship.getName()))
                    .append("\n");
        }
        diagram.append("</pre>\n");
        return diagram.toString();
    }

    private String mermaidEsmScript() {
        return "<script type='module'>\n"
                + "  import mermaid from '"
                + MERMAID_ESM_CDN
                + "';\n"
                + "  mermaid.initialize({ startOnLoad: true });\n"
                + "</script>\n";
    }

    private String mermaidEntityId(final EntityDefinition entity) {
        String sanitized = entity.getName().replaceAll("[^A-Za-z0-9_]", "_").toUpperCase();
        sanitized = sanitized.replaceAll("_+", "_");
        sanitized = sanitized.replaceAll("^_+|_+$", "");
        if (sanitized.isEmpty()) {
            sanitized = "ENTITY";
        }
        if (Character.isDigit(sanitized.charAt(0))) {
            sanitized = "ENTITY_" + sanitized;
        }
        return sanitized;
    }

    private String mermaidRelationshipLabel(final String relationshipName) {
        String sanitized = relationshipName.replaceAll("[^A-Za-z0-9 _-]", " ").trim();
        sanitized = sanitized.replaceAll("\\s+", " ");
        if (sanitized.isEmpty()) {
            return "relates to";
        }
        return sanitized;
    }

    private String mermaidLeftCardinalityMarker(final String cardinality) {
        if ("1".equals(cardinality)) {
            return "||";
        }
        if ("0".equals(cardinality)) {
            return "|o";
        }
        return "}o";
    }

    private String mermaidRightCardinalityMarker(final String cardinality) {
        if ("1".equals(cardinality)) {
            return "||";
        }
        if ("0".equals(cardinality)) {
            return "o|";
        }
        return "o{";
    }

    private String openApiVersionLinks() {
        StringBuilder links = new StringBuilder();
        links.append("<ul>%n".formatted());
        links.append(openApiVersionLink("3.0", prependPath + "/docs/openapi-3.0.json"));
        links.append(openApiVersionLink("3.1", prependPath + "/docs/openapi-3.1.json"));
        links.append(openApiVersionLink("3.2", prependPath + "/docs/openapi-3.2.json"));
        links.append("</ul>%n".formatted());
        return links.toString();
    }

    private String openApiVersionLink(final String version, final String specUrl) {
        return "<li>OpenAPI v %s JSON %s %s - %s %s</li>%n"
                .formatted(
                        version,
                        href("[standard validation]", specUrl),
                        href("[download]", specUrl + "?download"),
                        href("[less validation]", specUrl + "?permissive"),
                        href("[download]", specUrl + "?permissive&amp;download"));
    }

    private String resolveDocsTitle(final ThingifierApiDocumentationDefn apiDocDefn) {
        if (apiDocDefn != null
                && apiDocDefn.getSeoTitle() != null
                && !apiDocDefn.getSeoTitle().trim().isEmpty()) {
            return apiDocDefn.getSeoTitle().trim();
        }
        if (apiDocDefn != null
                && apiDocDefn.getTitle() != null
                && !apiDocDefn.getTitle().trim().isEmpty()) {
            return apiDocDefn.getTitle().trim() + " API Documentation";
        }
        if (thingifier != null
                && thingifier.getTitle() != null
                && !thingifier.getTitle().trim().isEmpty()) {
            return thingifier.getTitle().trim() + " API Documentation";
        }
        return "API Documentation";
    }

    private String resolveDocsDescription(final ThingifierApiDocumentationDefn apiDocDefn) {
        if (apiDocDefn != null
                && apiDocDefn.getSeoDescription() != null
                && !apiDocDefn.getSeoDescription().trim().isEmpty()) {
            return apiDocDefn.getSeoDescription().trim();
        }
        if (apiDocDefn != null
                && apiDocDefn.getDescription() != null
                && !apiDocDefn.getDescription().trim().isEmpty()) {
            return apiDocDefn.getDescription().trim();
        }
        if (thingifier != null
                && thingifier.getInitialParagraph() != null
                && !thingifier.getInitialParagraph().trim().isEmpty()) {
            return thingifier.getInitialParagraph().trim();
        }
        return "Browse API endpoints, payload formats, examples, and route behaviors.";
    }

    private String buildDocsHeadInject(
            final ThingifierApiDocumentationDefn apiDocDefn,
            final String htmlTitle,
            final String htmlDescription,
            final String canonicalUrl) {
        final String canonicalHost =
                getEnvironmentOrDefault("SEO_CANONICAL_HOST", DEFAULT_CANONICAL_HOST);
        final String canonicalAbsoluteUrl = absolutizeUrl(canonicalUrl, canonicalHost);
        final String metaRobots =
                firstNonBlank(
                        apiDocDefn == null ? "" : apiDocDefn.getMetaRobots(), DEFAULT_META_ROBOTS);
        final String ogImagePath =
                firstNonBlank(
                        apiDocDefn == null ? "" : apiDocDefn.getOgImage(),
                        getEnvironmentOrDefault("SEO_DEFAULT_OG_IMAGE", DEFAULT_OG_IMAGE_PATH));
        final String ogImageAbsoluteUrl = absolutizeUrl(ogImagePath, canonicalHost);
        final String ogType =
                firstNonBlank(apiDocDefn == null ? "" : apiDocDefn.getOgType(), DEFAULT_OG_TYPE);
        final String twitterCard =
                firstNonBlank(
                        apiDocDefn == null ? "" : apiDocDefn.getTwitterCard(),
                        DEFAULT_TWITTER_CARD);
        final String twitterSite =
                firstNonBlank(
                        apiDocDefn == null ? "" : apiDocDefn.getTwitterSite(),
                        getEnvironmentOrDefault("SEO_TWITTER_SITE", ""));

        StringBuilder head = new StringBuilder();
        if (!htmlDescription.isEmpty()) {
            head.append("<meta name='description' content='")
                    .append(escapeHtmlAttribute(htmlDescription))
                    .append("'>");
        }
        head.append("<meta name='robots' content='")
                .append(escapeHtmlAttribute(metaRobots))
                .append("'>");
        head.append("<meta property='og:title' content='")
                .append(escapeHtmlAttribute(htmlTitle))
                .append("'>");
        head.append("<meta property='og:description' content='")
                .append(escapeHtmlAttribute(htmlDescription))
                .append("'>");
        head.append("<meta property='og:type' content='")
                .append(escapeHtmlAttribute(ogType))
                .append("'>");
        head.append("<meta property='og:url' content='")
                .append(escapeHtmlAttribute(canonicalAbsoluteUrl))
                .append("'>");
        head.append("<meta property='og:site_name' content='")
                .append(escapeHtmlAttribute(DEFAULT_SITE_NAME))
                .append("'>");
        head.append("<meta property='og:image' content='")
                .append(escapeHtmlAttribute(ogImageAbsoluteUrl))
                .append("'>");
        head.append("<meta name='twitter:card' content='")
                .append(escapeHtmlAttribute(twitterCard))
                .append("'>");
        head.append("<meta name='twitter:title' content='")
                .append(escapeHtmlAttribute(htmlTitle))
                .append("'>");
        head.append("<meta name='twitter:description' content='")
                .append(escapeHtmlAttribute(htmlDescription))
                .append("'>");
        head.append("<meta name='twitter:image' content='")
                .append(escapeHtmlAttribute(ogImageAbsoluteUrl))
                .append("'>");
        if (!twitterSite.isEmpty()) {
            head.append("<meta name='twitter:site' content='")
                    .append(escapeHtmlAttribute(twitterSite))
                    .append("'>");
        }
        return head.toString();
    }

    private String getEnvironmentOrDefault(final String envName, final String defaultValue) {
        final String envValue = System.getenv(envName);
        if (envValue == null || envValue.trim().isEmpty()) {
            return defaultValue;
        }
        return envValue.trim();
    }

    private String firstNonBlank(final String preferred, final String fallback) {
        if (preferred != null && !preferred.trim().isEmpty()) {
            return preferred.trim();
        }
        return fallback == null ? "" : fallback.trim();
    }

    private String absolutizeUrl(final String url, final String host) {
        if (url == null || url.trim().isEmpty()) {
            return host;
        }
        final String trimmed = url.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.startsWith("/")) {
            return host + trimmed;
        }
        return host + "/" + trimmed;
    }

    private String escapeHtmlAttribute(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String escapeHtmlText(final String value) {
        return escapeHtmlAttribute(value);
    }

    private String getExampleFilter(final EntityDefinition filterableEntity) {
        String exampleFilters = "";
        List<Field> exampleFields = new ArrayList<>();
        List<Integer> indexes = new ArrayList<>();
        Random random = new Random();

        // todo: ignore strings unless none added, in which case add the strings
        for (String fieldName : filterableEntity.getFieldNames()) {
            Field field = filterableEntity.getField(fieldName);
            if (field.getType() != FieldType.AUTO_INCREMENT
                    && field.getType() != FieldType.AUTO_GUID) {
                // we can filter on guid and id, but don't use those as examples
                if (exampleFields.isEmpty() || random.nextBoolean()) {
                    // make sure at least one
                    indexes.add(exampleFields.size());
                    exampleFields.add(field);
                }
            }
        }

        String delimiter = "?";

        int fieldsToUse = random.nextInt(exampleFields.size()) + 1;
        for (int x = 0; x < fieldsToUse; x++) {
            int fieldToUse = random.nextInt(indexes.size());
            Field field = exampleFields.get(indexes.get(fieldToUse));
            indexes.remove(fieldToUse);
            exampleFilters =
                    exampleFilters
                            + delimiter
                            + field.getName()
                            + "="
                            + field.getRandomExampleValue();
            delimiter = "&";
        }

        //        try {
        //            exampleFilters =  URLEncoder.encode(exampleFilters,
        // StandardCharsets.UTF_8.toString());
        //        } catch (UnsupportedEncodingException ex) {
        exampleFilters = exampleFilters.replace(" ", "%20");
        //        }

        return exampleFilters;
    }

    private String getExampleSort(final EntityDefinition filterableEntity) {
        String fieldName = "field";
        if (filterableEntity != null) {
            Field primaryKeyField = filterableEntity.getPrimaryKeyField();
            if (primaryKeyField != null) {
                fieldName = primaryKeyField.getName();
            } else {
                for (String name : filterableEntity.getFieldNames()) {
                    fieldName = name;
                    break;
                }
            }
        }
        return "?" + SortByFieldName.PARAMETER_NAME + "=+" + fieldName;
    }

    private String getExamplePage() {
        return "?"
                + PaginationParams.LIMIT_PARAMETER_NAME
                + "="
                + thingifier.apiConfig().forParams().defaultPagingLimit()
                + "&"
                + PaginationParams.OFFSET_PARAMETER_NAME
                + "=0";
    }

    private String url(final String postUrl) {

        String midPath = "";
        if (!postUrl.startsWith("/")) {
            midPath = "/";
        }
        // todo: option to make clickable?

        return midPath + postUrl;
    }

    private String heading(final int level, final String theclass, final String text) {
        return String.format("<h%1$d class='%2$s'>%3$s</h%1$d>%n", level, theclass, text);
    }

    private String href(final String text, final String url) {
        return String.format("<a href='%s'>%s</a>", url, text);
    }

    private String additionalResponseAcceptHeaders() {
        StringBuilder headers = new StringBuilder();
        for (AcceptHeaderParser.ACCEPT_TYPE responseType :
                AcceptHeaderParser.ACCEPT_TYPE.responseMediaTypes()) {
            if (responseType == AcceptHeaderParser.ACCEPT_TYPE.JSON
                    || responseType == AcceptHeaderParser.ACCEPT_TYPE.XML) {
                continue;
            }
            headers.append(acceptHeaderExample(responseType)).append("<br/>\n");
        }
        headers.append("<br/>\n");
        return headers.toString();
    }

    private String acceptHeaderExample(final AcceptHeaderParser.ACCEPT_TYPE responseType) {
        return String.format("<i>Accept: %s</i>", responseType.mediaType());
    }

    private String jsonPathQueryExampleFor(final RoutingDefinition routingDefn) {
        final EntityDefinition entity = routingDefn.getFilterableEntity();
        if (entity == null) {
            return "$[*]";
        }

        final String fieldName =
                entity.getField("title") == null ? exampleFieldNameFor(entity) : "title";
        return "$"
                + jsonPathPropertySelector(entity.getPlural())
                + "[?@"
                + jsonPathPropertySelector(fieldName)
                + " == '"
                + escapeHtmlText(jsonPathSingleQuotedContent(exampleValueFor(fieldName)))
                + "']";
    }

    private String jsonPathPropertySelector(final String propertyName) {
        return "['" + escapeHtmlText(jsonPathSingleQuotedContent(propertyName)) + "']";
    }

    private String jsonPathSingleQuotedContent(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    private String exampleFieldNameFor(final EntityDefinition entity) {
        final Field primaryKeyField = entity.getPrimaryKeyField();
        if (primaryKeyField != null) {
            return primaryKeyField.getName();
        }

        for (String fieldName : entity.getFieldNames()) {
            return fieldName;
        }

        return "field";
    }

    private String exampleValueFor(final String fieldName) {
        if ("title".equals(fieldName)) {
            return "Task";
        }
        return "value";
    }

    private String paragraph(final String initialParagraph) {
        return String.format("<p>%s</p>%n", initialParagraph);
    }

    // Template functions
    private String heading(final int level, final String text) {
        return String.format("<h%1$d>%2$s</h%1$d>%n", level, text);
    }
}
