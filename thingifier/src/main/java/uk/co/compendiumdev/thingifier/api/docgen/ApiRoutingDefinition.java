package uk.co.compendiumdev.thingifier.api.docgen;

import uk.co.compendiumdev.thingifier.api.response.ResponseHeader;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

final public class ApiRoutingDefinition {

    private List<RoutingDefinition> routings;
    private HashMap<String, EntityDefinition> objectSchemas;

    public ApiRoutingDefinition() {
        routings = new ArrayList<>();
        objectSchemas = new HashMap<String,EntityDefinition>();
    }

    public Collection<RoutingDefinition> definitions() {
        return routings;
    }

    public RoutingDefinition addRouting(final String documentation, final RoutingVerb verb, final String url, final RoutingStatus routingStatus) {
        RoutingDefinition defn = new RoutingDefinition(verb, url, routingStatus, null).addDocumentation(documentation);
        routings.add(defn);
        return defn;
    }

    public RoutingDefinition addRouting(final String documentation, final RoutingVerb verb, final String url, final RoutingStatus routingStatus, final ResponseHeader header) {
        RoutingDefinition defn = new RoutingDefinition(verb, url, routingStatus, header).addDocumentation(documentation);
        routings.add(defn);
        return defn;
    }

    public void addObjectSchema(EntityDefinition entityDefn) {
        // TODO this should be an object schema rather than entityDefinition
        // because we don't want it to be editable
        // as single entity
        objectSchemas.put(entityDefn.getName(),entityDefn);

        // used for top level POST requests so there are no auto ids in the payload
        objectSchemas.put("create_" + entityDefn.getName(),entityDefn);

        // and as plural for array responses
        objectSchemas.put(entityDefn.getPlural(),entityDefn);
    }

    public boolean hasObjectSchemaNamed(String aName){
        return objectSchemas.containsKey(aName);
    }

    public Collection<EntityDefinition> getObjectSchemas() {
        return objectSchemas.values();
    }
}
