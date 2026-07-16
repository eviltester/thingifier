package uk.co.compendiumdev.thingifier.crudui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CrudUiArgumentsTest {

    @Test
    public void parsesProjectPathAndPort() {
        CrudUiArguments arguments =
                CrudUiArguments.parse(new String[] {"-project=project-folder", "-port=4568"});

        Assertions.assertTrue(arguments.hasProjectPath());
        Assertions.assertEquals("project-folder", arguments.projectPath().toString());
        Assertions.assertEquals(4568, arguments.port());
    }

    @Test
    public void rejectsProjectAndModelYamlTogether() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                        CrudUiArguments.parse(
                                new String[] {"-project=project-folder", "-modelYaml=model.yaml"}));
    }

    @Test
    public void parsesStorageModeAndSqliteFilePath() {
        CrudUiArguments arguments =
                CrudUiArguments.parse(
                        new String[] {
                            "-modelYaml=model.yaml",
                            "-thingifier-repository=sqlite-file",
                            "-thingifier-sqlite-file=data.sqlite"
                        });

        Assertions.assertEquals("sqlite-file", arguments.storage().mode());
        Assertions.assertTrue(arguments.storage().sqliteFilePath().endsWith("data.sqlite"));
    }
}
