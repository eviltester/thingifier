package uk.co.compendiumdev.thingifier.crudui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SchemaPreviewServiceTest {

    private final SchemaPreviewService service = new SchemaPreviewService();

    @Test
    public void validYamlResourcesPreviewAndRoundTripThroughDraftJson() {
        String[] resources = {
            "/models/minimal-todo.yaml",
            "/models/validations.yaml",
            "/models/object-fields.yaml",
            "/models/relationships-one-way.yaml",
            "/models/relationships-two-way.yaml"
        };

        for (String resource : resources) {
            UiHttpResponse response = service.fromYaml(TestResources.text(resource));
            JsonObject body = json(response);

            Assertions.assertEquals(200, response.statusCode(), resource);
            Assertions.assertTrue(body.get("valid").getAsBoolean(), resource);
            Assertions.assertTrue(
                    body.get("yaml").getAsString().contains("formatVersion: 1"), resource);
            Assertions.assertTrue(
                    body.get("mermaid").getAsString().contains("erDiagram"), resource);
            Assertions.assertTrue(
                    body.get("graphviz").getAsString().contains("digraph schema"), resource);

            UiHttpResponse preview = service.previewDraft(body.getAsJsonObject("draft").toString());
            Assertions.assertTrue(json(preview).get("valid").getAsBoolean(), resource);
        }
    }

    @Test
    public void invalidPrimaryKeyReturnsSemanticValidationErrors() {
        UiHttpResponse response =
                service.fromYaml(TestResources.text("/models/invalid-primary-key.yaml"));
        JsonObject body = json(response);

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertFalse(body.get("valid").getAsBoolean());
        Assertions.assertTrue(body.get("errors").toString().contains("primaryKey"));
        Assertions.assertEquals("", body.get("yaml").getAsString());
    }

    @Test
    public void invalidRelationshipTargetReturnsSemanticValidationErrors() {
        UiHttpResponse response =
                service.fromYaml(TestResources.text("/models/invalid-relationship-target.yaml"));
        JsonObject body = json(response);

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertFalse(body.get("valid").getAsBoolean());
        Assertions.assertTrue(
                body.get("errors").toString().contains("Unknown relationship target"));
    }

    private JsonObject json(final UiHttpResponse response) {
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }
}
