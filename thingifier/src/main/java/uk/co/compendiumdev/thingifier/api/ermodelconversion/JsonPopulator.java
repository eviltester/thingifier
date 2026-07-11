package uk.co.compendiumdev.thingifier.api.ermodelconversion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Map;
import java.util.UUID;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public class JsonPopulator implements RepositoryDataPopulator {

    private final String jsonData;
    private ERSchema schema;
    private ThingStore store;

    public JsonPopulator(String jsonDatabaseContents) {
        this.jsonData = jsonDatabaseContents;
    }

    @Override
    public void populate(ERSchema schema, ThingStore store) {

        this.schema = schema;
        this.store = store;
        this.store.administration().refreshSchema(schema);

        JsonElement data = JsonParser.parseString(jsonData);

        // expecting an object of entities with instances where each object property is an entity
        // mapped on to an array
        if (!data.isJsonObject()) {
            throw new RuntimeException("ERROR: Data is not an object of instantiated entities");
        }

        JsonObject entities = data.getAsJsonObject();
        store.administration().clearAllData();
        entities.entrySet()
                .forEach(
                        property -> {
                            populateEntityInstances(property);
                        });
    }

    private void populateEntityInstances(Map.Entry<String, JsonElement> entity) {
        // entity is an object with a name and an array
        if (!schema.hasEntityWithPluralNamed(entity.getKey())) {
            throw new RuntimeException(
                    String.format("ERROR: Unknown entity found: %s", entity.getKey()));
        }

        if (!entity.getValue().isJsonArray()) {
            throw new RuntimeException(
                    String.format(
                            "ERROR: Expected array of instances as %s value", entity.getKey()));
        }

        JsonArray instances = entity.getValue().getAsJsonArray();
        instances.forEach(
                instance -> {
                    populateAsInstanceOf(entity.getKey(), instance);
                });
    }

    private void populateAsInstanceOf(String entityPluralName, JsonElement instance) {

        if (!instance.isJsonObject()) {
            throw new RuntimeException(
                    String.format(
                            "ERROR: Expected array of %s to contain objects", entityPluralName));
        }

        EntityDefinition entityDefn = schema.getEntityDefinitionWithPluralNamed(entityPluralName);

        JsonObject obj = instance.getAsJsonObject();
        obj.keySet()
                .forEach(
                        propertyName -> {
                            if (!entityDefn.hasFieldNameDefined(propertyName)) {
                                throw new RuntimeException(
                                        String.format(
                                                "ERROR: Unknown property %s for %s",
                                                propertyName, entityDefn.getName()));
                            }
                        });

        EntityInstanceDraft draft = EntityInstanceDraft.forEntity(entityDefn);
        obj.entrySet()
                .forEach(
                        value -> {
                            Field fieldDefn = entityDefn.getField(value.getKey());
                            switch (fieldDefn.getType()) {
                                case AUTO_GUID:
                                    draft.withProtectedField(
                                            fieldDefn.getName(),
                                            UUID.fromString(value.getValue().getAsString())
                                                    .toString());
                                    break;
                                case AUTO_INCREMENT:
                                    draft.withProtectedField(
                                            fieldDefn.getName(),
                                            String.valueOf(value.getValue().getAsInt()));
                                    break;
                                default:
                                    draft.withField(value.getKey(), value.getValue().getAsString());
                            }
                        });

        // instance is valid, so add it
        store.entities().create(draft);
    }
}
