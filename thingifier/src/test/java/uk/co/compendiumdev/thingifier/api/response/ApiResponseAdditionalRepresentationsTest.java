package uk.co.compendiumdev.thingifier.api.response;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.AUTO_INCREMENT;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.OBJECT;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

class ApiResponseAdditionalRepresentationsTest {

    private final JsonThing jsonThing = new JsonThing(new ThingifierApiConfig("").jsonOutput());

    @Test
    void csvUsesVisibleScalarFieldsAndQuotesValues() {
        Thingifier thingifier = taskThingifier();
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityInstance instance = createTask(thingifier, "Hello, \"Codex\"", "Simple", "secret");

        ApiResponse response =
                ApiResponse.success()
                        .returnSingleInstance(instance)
                        .usingEntityView(task.getViewNamed("public"));

        Assertions.assertEquals(
                "id,title,notes\n1,\"Hello, \"\"Codex\"\"\",Simple",
                new ApiResponseAsDelimitedText(response, ',').getText());
    }

    @Test
    void tsvEscapesTabsAndLineBreaks() {
        Thingifier thingifier = taskThingifier();
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityInstance instance = createTask(thingifier, "Hello", "Tabbed\tLine\nNext", "secret");

        ApiResponse response =
                ApiResponse.success()
                        .returnSingleInstance(instance)
                        .usingEntityView(task.getViewNamed("public"));

        Assertions.assertEquals(
                "id\ttitle\tnotes\n1\tHello\tTabbed\\tLine\\nNext",
                new ApiResponseAsDelimitedText(response, '\t').getText());
    }

    @Test
    void plainTextUsesOneEntityPerLine() {
        Thingifier thingifier = taskThingifier();
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityInstance instance = createTask(thingifier, "Hello", "Simple", "secret");

        ApiResponse response =
                ApiResponse.success()
                        .returnSingleInstance(instance)
                        .usingEntityView(task.getViewNamed("public"));

        Assertions.assertEquals(
                "id=1, title=Hello, notes=Simple", new ApiResponseAsPlainText(response).getText());
    }

    @Test
    void htmlEscapesFieldValues() {
        Thingifier thingifier = taskThingifier();
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityInstance instance = createTask(thingifier, "<Task & \"One\">", "'note'", "secret");

        ApiResponse response =
                ApiResponse.success()
                        .returnSingleInstance(instance)
                        .usingEntityView(task.getViewNamed("public"));

        Assertions.assertEquals(
                "<table><thead><tr><th>id</th><th>title</th><th>notes</th></tr></thead>"
                        + "<tbody><tr><td>1</td><td>&lt;Task &amp; &quot;One&quot;&gt;</td>"
                        + "<td>&#39;note&#39;</td></tr></tbody></table>",
                new ApiResponseAsHtml(response).getHtml());
    }

    @Test
    void jsonLinesUsesOneUnwrappedJsonObjectPerEntity() {
        Thingifier thingifier = taskThingifier();
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityInstance first = createTask(thingifier, "One", "Simple", "secret");
        EntityInstance second = createTask(thingifier, "Two", "More", "secret");
        ArrayList<EntityInstance> instances = new ArrayList<>();
        instances.add(first);
        instances.add(second);

        ApiResponse response =
                ApiResponse.success()
                        .returnInstanceCollection(instances)
                        .resultContainsType(task)
                        .usingEntityView(task.getViewNamed("public"));

        Assertions.assertEquals(
                "{\"id\":1,\"title\":\"One\",\"notes\":\"Simple\","
                        + "\"metadata\":{\"tag\":\"ignored\"}}\n"
                        + "{\"id\":2,\"title\":\"Two\",\"notes\":\"More\","
                        + "\"metadata\":{\"tag\":\"ignored\"}}",
                new ApiResponseAsJsonLines(response, jsonThing).getJsonLines());
    }

    @Test
    void jsonSequenceFramesEveryJsonObject() {
        Thingifier thingifier = taskThingifier();
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityInstance instance = createTask(thingifier, "One", "Simple", "secret");

        ApiResponse response =
                ApiResponse.success()
                        .returnSingleInstance(instance)
                        .usingEntityView(task.getViewNamed("public"));

        Assertions.assertEquals(
                "\u001E{\"id\":1,\"title\":\"One\",\"notes\":\"Simple\","
                        + "\"metadata\":{\"tag\":\"ignored\"}}\n",
                new ApiResponseAsJsonLines(response, jsonThing).getJsonSequence());
    }

    @Test
    void emptyDelimitedCollectionStillIncludesHeaderWhenTypeIsKnown() {
        Thingifier thingifier = taskThingifier();
        EntityDefinition task = thingifier.getDefinitionNamed("task");

        ApiResponse response =
                ApiResponse.success()
                        .returnInstanceCollection(new ArrayList<>())
                        .resultContainsType(task)
                        .usingEntityView(task.getViewNamed("public"));

        Assertions.assertEquals(
                "id,title,notes", new ApiResponseAsDelimitedText(response, ',').getText());
    }

    @Test
    void errorResponsesRenderInAdditionalFormats() {
        ApiResponse response = ApiResponse.error(400, "<bad>");

        Assertions.assertEquals("<bad>", new ApiResponseAsPlainText(response).getText());
        Assertions.assertEquals(
                "<ul><li>&lt;bad&gt;</li></ul>", new ApiResponseAsHtml(response).getHtml());
        Assertions.assertEquals(
                "{\"errorMessage\":\"<bad>\"}",
                new ApiResponseAsJsonLines(response, jsonThing).getJsonLines());
        Assertions.assertEquals(
                "errorMessage\n<bad>", new ApiResponseAsDelimitedText(response, ',').getText());
    }

    private Thingifier taskThingifier() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("id", AUTO_INCREMENT));
        task.addField(Field.is("title", STRING));
        task.addField(Field.is("notes", STRING));
        task.addField(Field.is("hidden", STRING));
        task.addField(Field.is("metadata", OBJECT).withField(Field.is("tag", STRING)));
        task.defineView("public").hideResponseFields("hidden");
        return thingifier;
    }

    private EntityInstance createTask(
            final Thingifier thingifier,
            final String title,
            final String notes,
            final String hidden) {
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityInstanceDraft draft =
                EntityInstanceDraft.forEntity(task)
                        .withField("title", title)
                        .withField("notes", notes)
                        .withField("hidden", hidden)
                        .withField("metadata.tag", "ignored");
        return thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME).entities().create(draft);
    }
}
