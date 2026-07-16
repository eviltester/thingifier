package uk.co.compendiumdev.thingifier.crudui;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class CrudUiArguments {

    private final int port;
    private final Path modelYamlPath;
    private final Path projectPath;

    private CrudUiArguments(final int port, final Path modelYamlPath, final Path projectPath) {
        this.port = port;
        this.modelYamlPath = modelYamlPath;
        this.projectPath = projectPath;
    }

    public static CrudUiArguments parse(final String[] args) {
        int configuredPort = 4567;
        Path configuredModelYamlPath = null;
        Path configuredProjectPath = null;

        if (args != null) {
            for (String arg : args) {
                if (arg.startsWith("-port=")) {
                    configuredPort = Integer.parseInt(arg.substring("-port=".length()).trim());
                }
                if (arg.startsWith("-modelYaml=")) {
                    configuredModelYamlPath = Paths.get(arg.substring("-modelYaml=".length()));
                }
                if (arg.startsWith("-project=")) {
                    configuredProjectPath = Paths.get(arg.substring("-project=".length()));
                }
            }
        }

        if (configuredProjectPath != null && configuredModelYamlPath != null) {
            throw new IllegalArgumentException("Use either -project or -modelYaml, not both");
        }

        return new CrudUiArguments(configuredPort, configuredModelYamlPath, configuredProjectPath);
    }

    public int port() {
        return port;
    }

    public boolean hasModelYamlPath() {
        return modelYamlPath != null;
    }

    public Path modelYamlPath() {
        return modelYamlPath;
    }

    public boolean hasProjectPath() {
        return projectPath != null;
    }

    public Path projectPath() {
        return projectPath;
    }
}
