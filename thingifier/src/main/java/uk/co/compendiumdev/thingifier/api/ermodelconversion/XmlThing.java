package uk.co.compendiumdev.thingifier.api.ermodelconversion;

import org.json.JSONObject;
import org.json.XML;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.*;


public class XmlThing {

    private final JsonThing jsonConvertor;

    public XmlThing(final JsonThing jsonThing) {
        this.jsonConvertor = jsonThing;
    }

    public String getSingleObjectXml(final EntityInstance instance) {
        String parseForXMLOutput = jsonConvertor.asNamedJsonObject(instance).toString();
        //System.out.println(parseForXMLOutput);
        return XML.toString(new JSONObject(parseForXMLOutput));
    }

    public String getCollectionOfThings(final List<EntityInstance> thingsToReturn, final EntityDefinition typeOfThingReturned) {
        String parseForXMLOutput = jsonConvertor.asJsonTypedArrayWithContentsTyped(
                                                thingsToReturn, typeOfThingReturned);

        String output = XML.toString(new JSONObject(parseForXMLOutput));

        // TODO: workaround for this seems like a bug in XML.toString, but work around it at the moment
        // i.e. it outputs <todos><todo>...</todo></todos><todos><todo>...</todo></todos>
        output = output.replace(String.format("</%1$s><%1$s>", thingsToReturn.get(0).getEntity().getPlural()), "");
        return output;
    }
}
