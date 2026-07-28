package uk.co.compendiumdev.thingifier.htmlgui.htmlgen;

import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.XmlThing;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.xml.GenericXMLPrettyPrinter;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityViewDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public class DefaultGuiHtmlPages {

    private final DefaultGUIHTML templates;
    private final Thingifier thingifier;
    private final String tryDefault;
    private final ThingifierApiConfig apiConfig;
    private final GenericXMLPrettyPrinter XMLPrettyPrinter;
    private final XmlThing xmlThing;
    private final JsonThing jsonThing;
    private final String urlPathPrefix;

    // todo: avoid all the hardcoded /gui in here and instantiate with a url prefixpath
    public DefaultGuiHtmlPages(
            DefaultGUIHTML templates, Thingifier thingifier, String urlPathPrefix) {
        this.templates = templates;
        this.thingifier = thingifier;
        this.apiConfig = thingifier.apiConfig();
        this.XMLPrettyPrinter = new GenericXMLPrettyPrinter();
        this.jsonThing = new JsonThing(apiConfig.jsonOutput());
        this.xmlThing = new XmlThing(jsonThing);
        this.urlPathPrefix = urlPathPrefix;
        String firstEntity = firstVisibleThingName();
        String database = EntityRelModel.DEFAULT_DATABASE_NAME;
        this.tryDefault =
                " [<a href='%s/instances?entity=%s&database=%s'>explore default data</a>]"
                        .formatted(urlPathPrefix, firstEntity, database);
    }

    public String getHomePageHtml(
            final String title, final String headInject, final String canonical) {
        StringBuilder html = new StringBuilder();
        html.append(templates.getPageStart(title, headInject, canonical));
        html.append(templates.getMenuAsHTML());

        html.append(templates.getStartOfMainContentMarker());
        // allow an app level configuration html here
        html.append(templates.getHomePageContent());

        html.append(templates.getEndOfMainContentMarker());
        html.append(templates.getPageFooter());
        html.append(templates.getPageEnd());
        return html.toString();
    }

    public String getEntitiesListPage(String database) {

        StringBuilder html = new StringBuilder();
        html.append(
                templates.getPageStart(
                        "Entities Menu",
                        "<meta name='robots' content='noindex'>",
                        "%s/entities".formatted(urlPathPrefix)));

        html.append(templates.getMenuAsHTML());
        html.append("<h1>%s Entities Explorer</h1>".formatted(thingifier.getTitle()));
        html.append(templates.getStartOfMainContentMarker());
        html.append(getInstancesRootMenuHtml(database));
        // html.append(heading(2, "Choose from the Entities Menu Above"));
        html.append(templates.getEndOfMainContentMarker());
        html.append(templates.getPageFooter());
        html.append(templates.getPageEnd());
        return html.toString();
    }

    private String getInstancesRootMenuHtml(String database) {
        StringBuilder html = new StringBuilder();
        html.append("<div class='entity-instances-menu menu'>");
        html.append("<ul>");
        for (String thing : visibleThingNames()) {
            html.append(
                    String.format(
                            "<li><a href='%3$s/instances?entity=%1$s%2$s'>%1$s</a></li>",
                            thing, databaseParam(database), urlPathPrefix));
        }
        html.append("</ul>");
        html.append("</div>");
        return html.toString();
    }

    private String databaseParam(final String database) {
        return "&database=" + database;
    }

    public String getInstancesListPage(String database, String entityName) {
        StringBuilder html = new StringBuilder();

        html.append(
                templates.getPageStart(
                        entityName + " Instances",
                        "<meta name='robots' content='noindex'>",
                        "%s/instances".formatted(urlPathPrefix)));

        html.append(templates.getMenuAsHTML());
        html.append(templates.getStartOfMainContentMarker());
        html.append(getInstancesRootMenuHtml(database));

        String htmlErrorMessage = "";

        if (entityName != null) {

            if (!thingifier.getERmodel().hasEntityNamed(entityName)) {
                html.append("<h1>Entity Instances Explorer</h1>");
                htmlErrorMessage =
                        htmlErrorMessage
                                + "<p>Entity Named "
                                + HtmlUtils.sanitise(entityName)
                                + " not found.</p>";
            }

            if (!thingifier.getERmodel().getDatabaseNames().contains(database)) {
                html.append("<h1>Entity Instances Explorer</h1>");
                htmlErrorMessage =
                        htmlErrorMessage
                                + "<p>Database Named "
                                + HtmlUtils.sanitise(database)
                                + " not found. Have you made any API Calls?"
                                + tryDefault
                                + "</p>";
            }

            EntityDefinition definition =
                    thingifier
                            .getERmodel()
                            .getSchema()
                            .getDefinitionWithSingularOrPluralNamed(entityName);
            List<EntityInstance> instances = new ArrayList<>();

            if (htmlErrorMessage.isEmpty()) {
                try {
                    ThingStore store = thingifier.getStore(database);
                    instances = new ArrayList<>(store.entityQueries().list(definition));
                } catch (Exception e) {
                    // htmlErrorMessage = htmlErrorMessage + "<p>Database Access Error: " +
                    // e.getMessage() + ".</p>";
                }

                if (definition == null) {
                    htmlErrorMessage =
                            htmlErrorMessage
                                    + "<p>Entity instances not found in database, have you made any API calls?"
                                    + tryDefault
                                    + "</p>";
                }
            }

            if (htmlErrorMessage.isEmpty()) {

                html.append(getExploringDatabaseHtml(database));

                html.append(heading(1, entityName + " Instances"));

                try {

                    html.append("<h2>" + definition.getPlural() + "</h2>");

                    html.append(startHtmlTableFor(definition));

                    for (EntityInstance instance : instances) {
                        html.append(htmlTableRowFor(instance, database));
                    }

                    html.append("</tbody>");
                    html.append("</table>");
                } catch (Exception e) {
                    htmlErrorMessage =
                            htmlErrorMessage
                                    + "<p>Rendering Error: "
                                    + HtmlUtils.sanitise(e.getMessage())
                                    + "</p>";
                }
            }
        }

        html.append(htmlErrorMessage);

        html.append(templates.getEndOfMainContentMarker());
        html.append(templates.getPageFooter());
        html.append(templates.getPageEnd());
        return html.toString();
    }

    private String startHtmlTableFor(final EntityDefinition definition) {
        StringBuilder html = new StringBuilder();

        html.append(
                "<p id='%1$sentitytabledescription'>All instances for the %1$s entity are shown in the table below.<p>"
                        .formatted(definition.getPlural()));
        html.append(
                "<table  aria-label='%1$s Instance Details' aria-describedby='%1$sentitytabledescription'>"
                        .formatted(definition.getPlural()));
        html.append("<thead>");
        html.append("<tr>");
        for (String field : responseFieldNamesForTable(definition)) {
            html.append(String.format("<th>%s</th>", HtmlUtils.sanitise(field)));
        }
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");

        return html.toString();
    }

    private String htmlTableRowFor(final EntityInstance instance, String database) {
        StringBuilder html = new StringBuilder();
        final EntityDefinition definition = instance.getEntity();

        html.append("<tr>");
        for (String field : responseFieldNamesForTable(definition)) {
            Field theField = definition.getField(field);
            if (theField == definition.getPrimaryKeyField()
                    || theField.getType() == FieldType.AUTO_INCREMENT
                    || theField.getType() == FieldType.AUTO_GUID) {
                String value = instance.getFieldValue(field).asString();
                String renderAs =
                        String.format(
                                "<a href='%5$s/instance?entity=%1$s&%2$s=%3$s%4$s'>%3$s</a>",
                                definition.getName(),
                                theField.getName(),
                                HtmlUtils.sanitise(value),
                                databaseParam(database),
                                urlPathPrefix);
                html.append(String.format("<td>%s</td>", renderAs));
            } else {
                html.append(
                        String.format(
                                "<td>%s</td>",
                                HtmlUtils.sanitise(instance.getFieldValue(field).asString())));
            }
        }
        html.append("</tr>");

        return html.toString();
    }

    private List<String> visibleThingNames() {
        final List<String> visible = new ArrayList<>();
        for (String thingName : thingifier.getThingNames()) {
            final EntityDefinition definition = thingifier.getDefinitionNamed(thingName);
            if (definition != null && isEntityCollectionVisible(definition)) {
                visible.add(thingName);
            }
        }
        return visible;
    }

    private String firstVisibleThingName() {
        final List<String> visibleNames = visibleThingNames();
        if (!visibleNames.isEmpty()) {
            return visibleNames.get(0);
        }
        if (!thingifier.getThingNames().isEmpty()) {
            return thingifier.getThingNames().get(0);
        }
        return "";
    }

    private boolean isEntityCollectionVisible(final EntityDefinition definition) {
        return isRouteVisible(RoutingVerb.GET, apiPathForCollection(definition));
    }

    private boolean isRelationshipCollectionVisible(
            final RelationshipVectorDefinition relationship) {
        return isRouteVisible(RoutingVerb.GET, apiPathForRelationship(relationship));
    }

    private boolean isRouteVisible(final RoutingVerb verb, final String path) {
        return thingifier
                .apiSpec()
                .ruleFor(verb, path, apiConfig.getApiEndPointPrefix())
                .map(rule -> !rule.isHidden() && !rule.isDisabled())
                .orElse(true);
    }

    private String apiPathForCollection(final EntityDefinition definition) {
        final String entityRouteName =
                apiConfig.willUrlShowInstancesAsPlural()
                        ? definition.getPlural()
                        : definition.getName();
        return "/" + entityRouteName.toLowerCase();
    }

    private String apiPathForRelationship(final RelationshipVectorDefinition relationship) {
        return apiPathForCollection(relationship.getFrom()) + "/{id}/" + relationship.getName();
    }

    private EntityViewDefinition responseViewFor(final EntityDefinition definition) {
        return thingifier.guiConfig().dataExplorer().responseViewFor(definition);
    }

    private boolean isResponseFieldVisible(
            final EntityDefinition definition, final String fieldName) {
        final EntityViewDefinition view = responseViewFor(definition);
        return view == null || view.isResponseVisible(fieldName);
    }

    private List<String> responseFieldNames(final EntityDefinition definition) {
        final List<String> fields = new ArrayList<>();
        for (String fieldName : definition.getFieldNames()) {
            if (isResponseFieldVisible(definition, fieldName)) {
                fields.add(fieldName);
            }
        }
        return fields;
    }

    private List<String> responseFieldNamesForTable(final EntityDefinition definition) {
        final List<String> fields = new ArrayList<>();
        if (definition.hasPrimaryKeyField()
                && isResponseFieldVisible(definition, definition.getPrimaryKeyField().getName())) {
            fields.add(definition.getPrimaryKeyField().getName());
        }
        for (String fieldName : definition.getFieldNames()) {
            Field field = definition.getField(fieldName);
            if (field != definition.getPrimaryKeyField()
                    && (field.getType() == FieldType.AUTO_INCREMENT
                            || field.getType() == FieldType.AUTO_GUID)
                    && isResponseFieldVisible(definition, fieldName)) {
                fields.add(fieldName);
            }
        }
        for (String fieldName : definition.getFieldNames()) {
            Field field = definition.getField(fieldName);
            if (field != definition.getPrimaryKeyField()
                    && field.getType() != FieldType.AUTO_INCREMENT
                    && field.getType() != FieldType.AUTO_GUID
                    && isResponseFieldVisible(definition, fieldName)) {
                fields.add(fieldName);
            }
        }
        return fields;
    }

    private String heading(final int level, final String text) {
        return String.format("<h%1$d>%2$s</h%1$d>%n", level, HtmlUtils.sanitise(text));
    }

    private String getExploringDatabaseHtml(String database) {
        return String.format(
                "<p>Exploring Entities in %s session database.</p>", HtmlUtils.sanitise(database));
    }

    public String getInstanceDetailsPage(
            String database, String entityName, Map<String, String> instanceQueryParams) {

        StringBuilder html = new StringBuilder();

        String htmlErrorMessage = "";

        if (!thingifier.getERmodel().hasEntityNamed(entityName)) {
            htmlErrorMessage =
                    htmlErrorMessage
                            + "<p>Entity Named "
                            + HtmlUtils.sanitise((entityName))
                            + " not found.</p>";
            entityName = "Unknown";
        }

        if (!thingifier.getERmodel().getDatabaseNames().contains(database)) {
            htmlErrorMessage =
                    htmlErrorMessage
                            + "<p>Database Named "
                            + HtmlUtils.sanitise(database)
                            + " not found. Have you made any API Calls?"
                            + tryDefault
                            + "</p>";
        }

        html.append(
                templates.getPageStart(
                        entityName + " Instance",
                        "<meta name='robots' content='noindex'>",
                        "%s/instances".formatted(urlPathPrefix)));

        html.append(templates.getMenuAsHTML());
        html.append(templates.getStartOfMainContentMarker());

        EntityDefinition definition =
                thingifier
                        .getERmodel()
                        .getSchema()
                        .getDefinitionWithSingularOrPluralNamed(entityName);
        ThingStore store = null;

        if (htmlErrorMessage.isEmpty()) {
            try {
                store = thingifier.getStore(database);
            } catch (Exception e) {
                // htmlErrorMessage = htmlErrorMessage + "<p>Database Access Error: " +
                // e.getMessage() + ".</p>";
            }

            if (definition == null || store == null) {
                htmlErrorMessage =
                        htmlErrorMessage
                                + "<p>Entity instances not found in database, have you made any API calls?"
                                + tryDefault
                                + "</p>";
            }
        }

        EntityInstance instance = null;

        if (htmlErrorMessage.isEmpty()) {

            String keyName = "";
            String keyValue = "";
            for (String queryParam : instanceQueryParams.keySet()) {
                Field field = definition.getField(queryParam);
                if (field != null) {
                    if (field == definition.getPrimaryKeyField()
                            || field.getType() == FieldType.AUTO_GUID
                            || field.getType() == FieldType.AUTO_INCREMENT) {
                        keyName = field.getName();
                        keyValue = instanceQueryParams.get(queryParam);
                        break;
                    }
                }
            }

            try {
                instance = store.entityQueries().findByField(definition, keyName, keyValue);
            } catch (Exception e) {
                htmlErrorMessage =
                        htmlErrorMessage
                                + "<p>Instances not found in database, have you made any API calls?"
                                + tryDefault
                                + "</p>";
            }

            if (instance == null) {
                htmlErrorMessage =
                        htmlErrorMessage
                                + String.format(
                                        "<p>Could not find instance with %s, %s",
                                        HtmlUtils.sanitise(keyName), HtmlUtils.sanitise(keyValue));
            }
        }

        if (htmlErrorMessage.isEmpty()) {

            html.append(getExploringDatabaseHtml(database));

            try {

                html.append(getInstancesRootMenuHtml(database));

                html.append(heading(1, entityName + " Instance"));

                html.append("<h2>" + definition.getName() + "</h2>");

                html.append(getInstanceAsTable(instance));

                html.append("<details style='padding:1em'><summary>As List</summary>");
                html.append(getInstanceAsUl(instance));
                html.append("</details>");

                if (!definition.related().getRelationships().isEmpty()) {

                    html.append("<h2>Relationships</h2>");

                    for (RelationshipVectorDefinition relationship :
                            definition.related().getRelationships()) {
                        if (!isRelationshipCollectionVisible(relationship)) {
                            continue;
                        }
                        final List<EntityInstance> relatedItems =
                                store.relationships().listRelated(instance, relationship.getName());
                        html.append("<h3>" + relationship.getName() + "</h3>");
                        if (!relatedItems.isEmpty()) {
                            boolean header = true;

                            for (EntityInstance relatedInstance : relatedItems) {
                                if (header) {
                                    html.append(startHtmlTableFor(relatedInstance.getEntity()));
                                    header = false;
                                }
                                html.append(htmlTableRowFor(relatedInstance, database));
                            }
                            html.append("</tbody>");
                            html.append("</table>");

                        } else {
                            html.append("<ul><li>none</li></ul>");
                        }
                    }
                }

                if (thingifier.apiConfig().willApiAllowJsonForResponses()) {
                    html.append("<h2>JSON Example</h2>");
                    html.append("<pre class='json'>");
                    html.append("<code class='json'>");
                    // pretty print the json
                    html.append(
                            new GsonBuilder()
                                    .setPrettyPrinting()
                                    .create()
                                    .toJson(
                                            jsonThing.asJsonObject(
                                                    instance, null, responseViewFor(definition))));
                    html.append("</code>");
                    html.append("</pre>");
                }

                if (thingifier.apiConfig().willApiAllowXmlForResponses()) {
                    html.append("<h2>XML Example</h2>");
                    html.append("<pre class='xml'>");
                    html.append("<code class='xml'>");
                    // pretty print the json
                    html.append(
                            this.XMLPrettyPrinter.prettyPrintHtml(
                                    xmlThing.getSingleObjectXml(
                                            instance, null, responseViewFor(definition))));
                    html.append("</code>");
                    html.append("</pre>");
                }
            } catch (Exception e) {
                final String value = e.getMessage();
                htmlErrorMessage =
                        htmlErrorMessage
                                + "<p>Error rendering instance details: "
                                + HtmlUtils.sanitise(value)
                                + "</p>";
            }
        }

        html.append(htmlErrorMessage);

        html.append(templates.getEndOfMainContentMarker());
        html.append(templates.getPageFooter());
        html.append(templates.getPageEnd());
        return html.toString();
    }

    private String getInstanceAsUl(final EntityInstance instance) {
        final EntityDefinition definition = instance.getEntity();
        StringBuilder html = new StringBuilder();
        html.append("<ul>");
        for (String field : responseFieldNames(definition)) {
            html.append(
                    String.format(
                            "<li>%s<ul><li>%s</li></ul></li>",
                            field, HtmlUtils.sanitise(instance.getFieldValue(field).asString())));
        }
        html.append("</ul>");
        return html.toString();
    }

    private String getInstanceAsTable(EntityInstance instance) {

        final EntityDefinition definition = instance.getEntity();

        StringBuilder html = new StringBuilder();
        html.append(
                "<p id='instancetabledescription'>All the fields and values for the %s instance are shown in the table below.<p>"
                        .formatted(instance.getEntity().getName()));
        html.append(
                "<table  aria-label='%s Instance Details' aria-describedby='instancetabledescription'>"
                        .formatted(instance.getEntity().getName().toUpperCase()));
        html.append("<thead><tr>");
        for (String fieldName : responseFieldNames(definition)) {
            Field field = definition.getField(fieldName);
            if (field.getType() == FieldType.AUTO_GUID) {
                if (apiConfig.willResponsesShowPrimaryKeyHeader()) {
                    html.append(String.format("<th>%s</th>", field.getName()));
                }
            } else {
                html.append(String.format("<th>%s</th>", field.getName()));
            }
        }
        html.append("</tr></thead>");
        html.append("<tbody><tr>");
        for (String fieldName : responseFieldNames(definition)) {
            Field field = definition.getField(fieldName);
            if (field.getType() == FieldType.AUTO_GUID) {
                if (apiConfig.willResponsesShowPrimaryKeyHeader()) {
                    html.append(
                            String.format(
                                    "<td>%s</td>", instance.getFieldValue(fieldName).asString()));
                }
            } else {
                html.append(
                        String.format(
                                "<td>%s</td>",
                                HtmlUtils.sanitise(instance.getFieldValue(fieldName).asString())));
            }
        }

        html.append("</tr></tbody>");
        html.append("</table>");
        return html.toString();
    }
}
