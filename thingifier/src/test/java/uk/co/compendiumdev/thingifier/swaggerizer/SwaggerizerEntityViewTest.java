package uk.co.compendiumdev.thingifier.swaggerizer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

class SwaggerizerEntityViewTest {

    @Test
    void entityViewsGenerateSeparateRequestAndResponseSchemas() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition item = thingifier.defineThing("item", "items", 5);
        item.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        item.addField(Field.is("name", FieldType.STRING));
        item.addField(Field.is("secret", FieldType.STRING));
        item.defineView("PublicItem").hideRequestFields("secret").hideResponseFields("secret");
        thingifier.apiSpec().route(RoutingVerb.POST, "/api/items").entityView("PublicItem");

        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.setThingifier(thingifier);
        apiDefn.setPathPrefix("/api");

        final OpenAPI openApi = new Swaggerizer(apiDefn).swagger();

        final Schema<?> requestSchema =
                openApi.getComponents().getSchemas().get("create_PublicItem");
        Assertions.assertNotNull(requestSchema);
        Assertions.assertTrue(requestSchema.getProperties().containsKey("name"));
        Assertions.assertFalse(requestSchema.getProperties().containsKey("id"));
        Assertions.assertFalse(requestSchema.getProperties().containsKey("secret"));

        final Schema<?> responseSchema = openApi.getComponents().getSchemas().get("PublicItem");
        Assertions.assertNotNull(responseSchema);
        Assertions.assertTrue(responseSchema.getProperties().containsKey("id"));
        Assertions.assertTrue(responseSchema.getProperties().containsKey("name"));
        Assertions.assertFalse(responseSchema.getProperties().containsKey("secret"));

        final String requestRef =
                openApi.getPaths()
                        .get("/api/items")
                        .getPost()
                        .getRequestBody()
                        .getContent()
                        .get("application/json")
                        .getSchema()
                        .get$ref();
        Assertions.assertEquals("#/components/schemas/create_PublicItem", requestRef);
    }
}
