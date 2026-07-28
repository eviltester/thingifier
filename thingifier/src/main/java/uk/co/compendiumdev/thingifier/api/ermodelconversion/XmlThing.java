package uk.co.compendiumdev.thingifier.api.ermodelconversion;

import java.util.*;
import org.json.JSONObject;
import org.json.XML;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityViewDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.RelationshipRepository;

public class XmlThing {

    private final JsonThing jsonConvertor;

    public XmlThing(final JsonThing jsonThing) {
        this.jsonConvertor = jsonThing;
    }

    public String getSingleObjectXml(final EntityInstance instance) {
        return getSingleObjectXml(instance, null);
    }

    public String getSingleObjectXml(
            final EntityInstance instance, final RelationshipRepository relationships) {
        return getSingleObjectXml(instance, relationships, null);
    }

    public String getSingleObjectXml(
            final EntityInstance instance,
            final RelationshipRepository relationships,
            final EntityViewDefinition view) {
        String parseForXMLOutput =
                jsonConvertor.asNamedJsonObject(instance, relationships, view).toString();
        // System.out.println(parseForXMLOutput);
        return XML.toString(new JSONObject(parseForXMLOutput));
    }

    public String getSingleObjectXml(final EntityInstanceDraft draft) {
        String parseForXMLOutput = jsonConvertor.asNamedJsonObject(draft).toString();
        return XML.toString(new JSONObject(parseForXMLOutput));
    }

    public String getCollectionOfThings(
            final List<EntityInstance> thingsToReturn, final EntityDefinition typeOfThingReturned) {
        return getCollectionOfThings(thingsToReturn, typeOfThingReturned, null);
    }

    public String getCollectionOfThings(
            final List<EntityInstance> thingsToReturn,
            final EntityDefinition typeOfThingReturned,
            final RelationshipRepository relationships) {
        return getCollectionOfThings(thingsToReturn, typeOfThingReturned, relationships, null);
    }

    public String getCollectionOfThings(
            final List<EntityInstance> thingsToReturn,
            final EntityDefinition typeOfThingReturned,
            final RelationshipRepository relationships,
            final EntityViewDefinition view) {
        String parseForXMLOutput =
                jsonConvertor.asJsonTypedArrayWithContentsTyped(
                        thingsToReturn, typeOfThingReturned, relationships, view);

        String output = XML.toString(new JSONObject(parseForXMLOutput));

        // TODO: workaround for this seems like a bug in XML.toString, but work around it at the
        // moment
        // i.e. it outputs <todos><todo>...</todo></todos><todos><todo>...</todo></todos>
        output =
                output.replace(
                        String.format(
                                "</%1$s><%1$s>", thingsToReturn.get(0).getEntity().getPlural()),
                        "");
        return output;
    }

    public String getCollectionOfDrafts(
            final List<EntityInstanceDraft> thingsToReturn,
            final EntityDefinition typeOfThingReturned) {
        String parseForXMLOutput =
                jsonConvertor
                        .asJsonObjectTypedDraftArrayWithContentsUntyped(
                                thingsToReturn, typeOfThingReturned.getPlural())
                        .toString();

        String output = XML.toString(new JSONObject(parseForXMLOutput));
        output =
                output.replace(String.format("</%1$s><%1$s>", typeOfThingReturned.getPlural()), "");
        return output;
    }
}
