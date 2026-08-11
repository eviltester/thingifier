package uk.co.compendiumdev.thingifier.swaggerizer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

class SwaggerizerSchemaExampleTest {

    @Test
    void collectionResponseSchemasUseJsonWrapperAndXmlWrappedArray() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition item = thingifier.defineThing("item", "items");
        item.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT).withExample("21"));
        item.addField(Field.is("type", FieldType.STRING).withExample("cd"));
        item.addField(Field.is("isbn13", FieldType.STRING).withExample("123-4-56-789012-3"));
        item.addField(Field.is("price", FieldType.FLOAT).withExample("97.99"));
        item.addField(Field.is("numberinstock", FieldType.INTEGER).withExample("0"));

        final ThingifierApiDocumentationDefn apiDefn =
                new ThingifierApiDocumentationDefn().setThingifier(thingifier);

        final JsonObject document =
                JsonParser.parseString(new Swaggerizer(apiDefn).asJson()).getAsJsonObject();
        final JsonObject schemas =
                document.getAsJsonObject("components").getAsJsonObject("schemas");
        final JsonObject itemsSchema = schemas.getAsJsonObject("items");
        final JsonObject itemsProperty =
                itemsSchema.getAsJsonObject("properties").getAsJsonObject("items");
        final JsonObject itemSchema = itemsProperty.getAsJsonObject("items");
        final JsonObject xmlItemsSchema = schemas.getAsJsonObject("items_xml_collection");
        final JsonObject xmlItemSchema = xmlItemsSchema.getAsJsonObject("items");

        Assertions.assertEquals("object", itemsSchema.get("type").getAsString());
        Assertions.assertEquals("array", itemsProperty.get("type").getAsString());
        Assertions.assertEquals("object", itemSchema.get("type").getAsString());
        Assertions.assertEquals(
                "items", itemsSchema.getAsJsonArray("required").get(0).getAsString());
        Assertions.assertEquals(
                Set.of("id", "type", "isbn13", "price", "numberinstock"),
                stringsIn(itemSchema.getAsJsonArray("required")));
        Assertions.assertEquals(
                "#/components/schemas/items",
                document.getAsJsonObject("paths")
                        .getAsJsonObject("/items")
                        .getAsJsonObject("get")
                        .getAsJsonObject("responses")
                        .getAsJsonObject("200")
                        .getAsJsonObject("content")
                        .getAsJsonObject("application/json")
                        .getAsJsonObject("schema")
                        .get("$ref")
                        .getAsString());
        Assertions.assertEquals(
                "#/components/schemas/items_xml_collection",
                document.getAsJsonObject("paths")
                        .getAsJsonObject("/items")
                        .getAsJsonObject("get")
                        .getAsJsonObject("responses")
                        .getAsJsonObject("200")
                        .getAsJsonObject("content")
                        .getAsJsonObject("application/xml")
                        .getAsJsonObject("schema")
                        .get("$ref")
                        .getAsString());

        Assertions.assertEquals("array", xmlItemsSchema.get("type").getAsString());
        Assertions.assertEquals(
                "items", xmlItemsSchema.getAsJsonObject("xml").get("name").getAsString());
        Assertions.assertTrue(xmlItemsSchema.getAsJsonObject("xml").get("wrapped").getAsBoolean());
        Assertions.assertEquals("object", xmlItemSchema.get("type").getAsString());
        Assertions.assertEquals(
                "item", xmlItemSchema.getAsJsonObject("xml").get("name").getAsString());
        Assertions.assertEquals(
                Set.of("id", "type", "isbn13", "price", "numberinstock"),
                stringsIn(xmlItemSchema.getAsJsonArray("required")));
    }

    private Set<String> stringsIn(final JsonArray values) {
        Set<String> strings = new HashSet<>();
        for (JsonElement value : values) {
            strings.add(value.getAsString());
        }
        return strings;
    }

    @Test
    void openApiSchemaExamplesUseJsonValuesMatchingTheFieldType() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition item = thingifier.defineThing("item", "items");
        item.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT).withExample("21"));
        item.addField(Field.is("type", FieldType.STRING).withExample("cd"));
        item.addField(Field.is("isbn13", FieldType.STRING).withExample("123-4-56-789012-3"));
        item.addField(Field.is("price", FieldType.FLOAT).withExample("97.99"));
        item.addField(Field.is("numberinstock", FieldType.INTEGER).withExample("0"));
        item.addField(Field.is("active", FieldType.BOOLEAN).withExample("false"));

        final ThingifierApiDocumentationDefn apiDefn =
                new ThingifierApiDocumentationDefn().setThingifier(thingifier);

        final JsonObject document =
                JsonParser.parseString(new Swaggerizer(apiDefn).asJson()).getAsJsonObject();
        final JsonObject properties =
                document.getAsJsonObject("components")
                        .getAsJsonObject("schemas")
                        .getAsJsonObject("create_item")
                        .getAsJsonObject("properties");

        Assertions.assertTrue(
                properties.getAsJsonObject("price").get("example").getAsJsonPrimitive().isNumber());
        Assertions.assertTrue(
                properties
                        .getAsJsonObject("numberinstock")
                        .get("example")
                        .getAsJsonPrimitive()
                        .isNumber());
        Assertions.assertTrue(
                properties
                        .getAsJsonObject("active")
                        .get("example")
                        .getAsJsonPrimitive()
                        .isBoolean());
        Assertions.assertTrue(
                properties
                        .getAsJsonObject("isbn13")
                        .get("example")
                        .getAsJsonPrimitive()
                        .isString());

        final JsonObject idParameter =
                document.getAsJsonObject("paths")
                        .getAsJsonObject("/items/{id}")
                        .getAsJsonArray("parameters")
                        .get(0)
                        .getAsJsonObject();
        Assertions.assertTrue(idParameter.get("example").getAsJsonPrimitive().isNumber());
    }
}
