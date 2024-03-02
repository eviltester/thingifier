package uk.co.compendiumdev.thingifier.api.ermodelconversion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.DataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class JsonPopulator implements DataPopulator {

    private final String jsonData;
    private ERSchema schema;
    private ERInstanceData database;

    public JsonPopulator(String jsonDatabaseContents) {
        this.jsonData = jsonDatabaseContents;
    }

    @Override
    public void populate(ERSchema schema, ERInstanceData database) {

        this.schema = schema;
        this.database = database;

        JsonElement data = JsonParser.parseString(jsonData);

        // expecting an object of entities with instances where each object property is an entity mapped on to an array
        if(!data.isJsonObject()){
            throw new RuntimeException("ERROR: Data is not an object of instantiated entities");
        }

        JsonObject entities = data.getAsJsonObject();
        entities.entrySet().forEach(property -> {
            populateEntityInstances(property);
        });

    }

    private void populateEntityInstances(Map.Entry<String, JsonElement> entity) {
        // entity is an object with a name and an array
        if(!schema.hasEntityWithPluralNamed(entity.getKey())){
            throw new RuntimeException(String.format("ERROR: Unknown entity found: %s", entity.getKey()));
        }

        if(!entity.getValue().isJsonArray()){
            throw new RuntimeException(String.format("ERROR: Expected array of instances as %s value", entity.getKey()));
        }

        database.clearAllData();

        JsonArray instances = entity.getValue().getAsJsonArray();
        instances.forEach(instance -> {populateAsInstanceOf(entity.getKey(), instance);});

    }

    private void populateAsInstanceOf(String entityPluralName, JsonElement instance) {

        if(!instance.isJsonObject()){
            throw new RuntimeException(String.format("ERROR: Expected array of %s to contain objects", entityPluralName));
        }

        EntityDefinition entityDefn = schema.getEntityDefinitionWithPluralNamed(entityPluralName);

        JsonObject obj = instance.getAsJsonObject();
        obj.keySet().forEach(propertyName -> {
            if(!entityDefn.hasFieldNameDefined(propertyName)){
                throw new RuntimeException(String.format("ERROR: Unknown property %s for %s", propertyName, entityDefn.getName()));
            }
        });

        EntityInstance entityInstance = new EntityInstance(entityDefn);
        obj.entrySet().forEach(value -> {
            Field fieldDefn = entityDefn.getField(value.getKey());
            switch (fieldDefn.getType()){
                case AUTO_GUID:
                    entityInstance.overrideValue(fieldDefn.getName(), UUID.fromString(value.getValue().getAsString()).toString());
                    break;
                case AUTO_INCREMENT:
                    entityInstance.overrideValue(fieldDefn.getName(), String.valueOf(value.getValue().getAsInt()));
                    break;
                default:
                    entityInstance.setValue(value.getKey(), value.getValue().getAsString());
            }
        });

        ValidationReport validation = entityInstance.validateFieldValues(new ArrayList<>(), true);
        if(!validation.isValid()){
            throw new RuntimeException(String.format("ERROR: Invalid %s entity instance because %s", entityDefn.getName(), validation.getCombinedErrorMessages()));
        }

        // instance is valid, so add it
        EntityInstanceCollection instances = database.getInstanceCollectionForEntityNamed(entityDefn.getName());
        instances.addInstance(entityInstance);
    }
}
