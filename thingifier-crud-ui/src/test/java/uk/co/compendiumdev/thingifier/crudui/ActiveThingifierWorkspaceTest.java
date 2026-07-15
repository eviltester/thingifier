package uk.co.compendiumdev.thingifier.crudui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ActiveThingifierWorkspaceTest {

    @Test
    public void defaultWorkspaceStartsWithTodoManager() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            WorkspaceSnapshot snapshot = workspace.snapshot();

            Assertions.assertEquals(1L, snapshot.version());
            Assertions.assertEquals("Todo Manager", snapshot.definition().title());
            Assertions.assertNotNull(snapshot.definition().entityNamed("todo"));
            Assertions.assertTrue(snapshot.schemaYaml().contains("title: Todo Manager"));
        }
    }

    @Test
    public void yamlLoadReplacesSchemaAndIncrementsVersion() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/minimal-todo.yaml"));

            WorkspaceSnapshot snapshot = workspace.snapshot();
            Assertions.assertEquals(2L, snapshot.version());
            Assertions.assertEquals("Minimal Todo", snapshot.definition().title());
            Assertions.assertNotNull(snapshot.definition().entityNamed("todo"));
            Assertions.assertNull(snapshot.definition().entityNamed("project"));
        }
    }
}
