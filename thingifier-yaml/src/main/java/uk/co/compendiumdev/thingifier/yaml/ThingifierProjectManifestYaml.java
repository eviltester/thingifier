package uk.co.compendiumdev.thingifier.yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

public final class ThingifierProjectManifestYaml {

    public ThingifierProjectManifest load(final Path path) throws IOException {
        try (InputStream input = Files.newInputStream(path)) {
            return load(input);
        }
    }

    public ThingifierProjectManifest load(final InputStream input) {
        try {
            return fromObject(loader().load(input));
        } catch (YAMLException | ClassCastException | IllegalArgumentException e) {
            throw new ThingifierYamlException("Could not parse project manifest", e);
        }
    }

    public ThingifierProjectManifest load(final String yamlText) {
        try {
            return fromObject(loader().load(yamlText));
        } catch (YAMLException | ClassCastException | IllegalArgumentException e) {
            throw new ThingifierYamlException("Could not parse project manifest", e);
        }
    }

    public String export(final ThingifierProjectManifest manifest) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("formatVersion", manifest.formatVersion());
        Map<String, Object> project = new LinkedHashMap<>();
        project.put("title", manifest.title());
        project.put("description", manifest.description());
        root.put("project", project);
        root.put("schemaFile", manifest.schemaFile());
        root.put("dataFile", manifest.dataFile());
        return dumper().dump(root);
    }

    private ThingifierProjectManifest fromObject(final Object parsed) {
        if (!(parsed instanceof Map)) {
            throw new ThingifierYamlException("Project manifest must contain a YAML object");
        }
        Map<?, ?> root = (Map<?, ?>) parsed;
        Map<?, ?> project = mapValue(root.get("project"));
        return new ThingifierProjectManifest(
                intValue(root.get("formatVersion")),
                stringValue(project.get("title")),
                stringValue(project.get("description")),
                stringValue(root.get("schemaFile")),
                stringValue(root.get("dataFile")));
    }

    private int intValue(final Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            return Integer.parseInt(((String) value).trim());
        }
        throw new ThingifierYamlException("Project manifest must contain formatVersion");
    }

    private Map<?, ?> mapValue(final Object value) {
        if (value instanceof Map) {
            return (Map<?, ?>) value;
        }
        return Map.of();
    }

    private String stringValue(final Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Yaml loader() {
        final LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(20);
        return new Yaml(new SafeConstructor(options));
    }

    private Yaml dumper() {
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setSplitLines(false);
        return new Yaml(options);
    }
}
