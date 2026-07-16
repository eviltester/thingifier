package uk.co.compendiumdev.thingifier.yaml;

import java.nio.file.Path;

public final class ThingifierProjectManifest {

    public static final int SUPPORTED_FORMAT_VERSION = 1;
    public static final String DEFAULT_MANIFEST_FILE = "projectfile.erproj";
    public static final String DEFAULT_SCHEMA_FILE = "schema.yaml";
    public static final String DEFAULT_DATA_FILE = "data.json";

    private final int formatVersion;
    private final String title;
    private final String description;
    private final String schemaFile;
    private final String dataFile;

    public ThingifierProjectManifest(
            final int formatVersion,
            final String title,
            final String description,
            final String schemaFile,
            final String dataFile) {
        this.formatVersion = formatVersion;
        this.title = nullToEmpty(title);
        this.description = nullToEmpty(description);
        this.schemaFile = validatedRelativeFile(schemaFile, "schemaFile");
        this.dataFile = validatedRelativeFile(dataFile, "dataFile");
        if (formatVersion != SUPPORTED_FORMAT_VERSION) {
            throw new ThingifierYamlException("Unsupported project formatVersion " + formatVersion);
        }
    }

    public static ThingifierProjectManifest defaultFor(
            final String title, final String description) {
        return new ThingifierProjectManifest(
                SUPPORTED_FORMAT_VERSION,
                title,
                description,
                DEFAULT_SCHEMA_FILE,
                DEFAULT_DATA_FILE);
    }

    public int formatVersion() {
        return formatVersion;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public String schemaFile() {
        return schemaFile;
    }

    public String dataFile() {
        return dataFile;
    }

    private String validatedRelativeFile(final String value, final String fieldName) {
        String text = nullToEmpty(value).trim();
        if (text.isEmpty()) {
            throw new ThingifierYamlException("Project manifest must contain " + fieldName);
        }
        if (text.startsWith("/") || text.startsWith("\\") || text.matches("^[A-Za-z]:.*")) {
            throw new ThingifierYamlException(fieldName + " must be a relative path");
        }

        Path path = Path.of(text);
        if (path.isAbsolute()) {
            throw new ThingifierYamlException(fieldName + " must be a relative path");
        }

        String normalized = path.normalize().toString().replace('\\', '/');
        if (".".equals(normalized)
                || "..".equals(normalized)
                || normalized.startsWith("../")
                || normalized.contains("/../")) {
            throw new ThingifierYamlException(fieldName + " must stay inside the project folder");
        }
        return normalized;
    }

    private String nullToEmpty(final String value) {
        return value == null ? "" : value;
    }
}
