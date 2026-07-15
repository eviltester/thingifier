package uk.co.compendiumdev.thingifier.crudui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WorkspaceDataImportExportTest {

    @Test
    public void exportIncludesSchemaEntitiesAndRelationships() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            DynamicThingifierApiProxy proxy = new DynamicThingifierApiProxy(workspace);
            createRelatedProjectAndTodo(proxy);
            CrudUiController controller = new CrudUiController(workspace);

            UiHttpResponse export = controller.exportData();

            Assertions.assertEquals(200, export.statusCode());
            Assertions.assertTrue(export.body().contains("\"schemaYaml\""));
            Assertions.assertTrue(export.body().contains("\"projects\""));
            Assertions.assertTrue(export.body().contains("\"relationships\""));
            Assertions.assertEquals(1, root(export.body()).getAsJsonArray("relationships").size());
        }
    }

    @Test
    public void importRecreatesEntitiesAndRelationshipEdges() {
        String exported;
        try (ActiveThingifierWorkspace source =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            source.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            DynamicThingifierApiProxy sourceProxy = new DynamicThingifierApiProxy(source);
            createRelatedProjectAndTodo(sourceProxy);
            exported = new CrudUiController(source).exportData().body();
        }

        try (ActiveThingifierWorkspace target =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            CrudUiController controller = new CrudUiController(target);
            UiHttpResponse imported = controller.importData(exported);
            DynamicThingifierApiProxy targetProxy = new DynamicThingifierApiProxy(target);

            Assertions.assertEquals(200, imported.statusCode());
            UiHttpResponse projects = targetProxy.getJson("projects");
            UiHttpResponse todos = targetProxy.getJson("todos");
            UiHttpResponse tasks = targetProxy.getJson("projects/1/tasks");

            Assertions.assertTrue(projects.body().contains("Project A"));
            Assertions.assertTrue(todos.body().contains("Task A"));
            Assertions.assertEquals(1, root(tasks.body()).getAsJsonArray("todos").size());
        }
    }

    private void createRelatedProjectAndTodo(final DynamicThingifierApiProxy proxy) {
        String projectId =
                field(proxy.postJson("projects", "{\"title\":\"Project A\"}").body(), "id");
        String todoId = field(proxy.postJson("todos", "{\"title\":\"Task A\"}").body(), "id");
        UiHttpResponse relationship =
                proxy.postJson("projects/" + projectId + "/tasks", "{\"id\":\"" + todoId + "\"}");
        Assertions.assertEquals(201, relationship.statusCode());
    }

    private String field(final String json, final String fieldName) {
        JsonObject object = root(json);
        return object.get(fieldName).getAsString();
    }

    private JsonObject root(final String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }
}
