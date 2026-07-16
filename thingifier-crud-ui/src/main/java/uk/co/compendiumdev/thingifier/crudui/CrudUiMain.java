package uk.co.compendiumdev.thingifier.crudui;

import java.io.IOException;
import java.util.Map;
import uk.co.compendiumdev.thingifier.crudui.adapter.spark.CrudUiApplication;

public final class CrudUiMain {

    private CrudUiMain() {}

    public static void main(final String[] args) throws IOException {
        CrudUiArguments arguments = CrudUiArguments.parse(args);
        ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace();
        if (arguments.hasProjectPath()) {
            new WorkspaceProjectService(workspace, new WorkspaceMetadataJson())
                    .load(JsonSupport.toJson(Map.of("path", arguments.projectPath().toString())));
        } else if (arguments.hasModelYamlPath()) {
            workspace.replaceWithYaml(arguments.modelYamlPath());
        }

        CrudUiApplication application = new CrudUiApplication(workspace, arguments.port());
        application.start();
        System.out.println("Thingifier CRUD UI running on http://localhost:" + arguments.port());
    }
}
