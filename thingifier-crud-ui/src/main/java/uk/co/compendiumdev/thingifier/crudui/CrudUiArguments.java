package uk.co.compendiumdev.thingifier.crudui;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class CrudUiArguments {

    private final int port;
    private final Path modelYamlPath;

    private CrudUiArguments(final int port, final Path modelYamlPath) {
        this.port = port;
        this.modelYamlPath = modelYamlPath;
    }

    public static CrudUiArguments parse(final String[] args) {
        int configuredPort = 4567;
        Path configuredModelYamlPath = null;

        if (args != null) {
            for (String arg : args) {
                if (arg.startsWith("-port=")) {
                    configuredPort = Integer.parseInt(arg.substring("-port=".length()).trim());
                }
                if (arg.startsWith("-modelYaml=")) {
                    configuredModelYamlPath = Paths.get(arg.substring("-modelYaml=".length()));
                }
            }
        }

        return new CrudUiArguments(configuredPort, configuredModelYamlPath);
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
}
