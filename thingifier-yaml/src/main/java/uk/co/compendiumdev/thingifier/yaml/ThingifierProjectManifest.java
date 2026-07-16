package uk.co.compendiumdev.thingifier.yaml;

import java.nio.file.Path;

public final class ThingifierProjectManifest {

    public static final int SUPPORTED_FORMAT_VERSION = 1;
    public static final String DEFAULT_MANIFEST_FILE = "projectfile.erproj";
    public static final String DEFAULT_SCHEMA_FILE = "schema.yaml";
    public static final String DEFAULT_DATA_FILE = "data.json";
    public static final String DEFAULT_SQLITE_FILE = "data.sqlite";
    public static final String STORAGE_MEMORY = "memory";
    public static final String STORAGE_SQLITE_MEMORY = "sqlite-memory";
    public static final String STORAGE_SQLITE_FILE = "sqlite-file";

    private final int formatVersion;
    private final String title;
    private final String description;
    private final String schemaFile;
    private final String dataFile;
    private final String storageMode;
    private final String sqliteFile;

    public ThingifierProjectManifest(
            final int formatVersion,
            final String title,
            final String description,
            final String schemaFile,
            final String dataFile) {
        this(formatVersion, title, description, schemaFile, dataFile, STORAGE_MEMORY, "");
    }

    public ThingifierProjectManifest(
            final int formatVersion,
            final String title,
            final String description,
            final String schemaFile,
            final String dataFile,
            final String storageMode,
            final String sqliteFile) {
        this.formatVersion = formatVersion;
        this.title = nullToEmpty(title);
        this.description = nullToEmpty(description);
        this.schemaFile = validatedRelativeFile(schemaFile, "schemaFile");
        this.storageMode = resolvedStorageMode(storageMode, dataFile);
        if (isSqliteFileStorage()) {
            String sqliteDataFile = firstPresent(sqliteFile, dataFile);
            this.sqliteFile = validatedRelativeFile(sqliteDataFile, "storage.sqliteFile");
            this.dataFile = optionalRelativeFile(dataFile, "dataFile");
        } else {
            this.sqliteFile = "";
            this.dataFile = validatedRelativeFile(dataFile, "dataFile");
        }
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

    public static ThingifierProjectManifest sqliteFileBackedFor(
            final String title, final String description) {
        return sqliteFileBackedFor(title, description, DEFAULT_SQLITE_FILE);
    }

    public static ThingifierProjectManifest sqliteFileBackedFor(
            final String title, final String description, final String sqliteFile) {
        return new ThingifierProjectManifest(
                SUPPORTED_FORMAT_VERSION,
                title,
                description,
                DEFAULT_SCHEMA_FILE,
                sqliteFile,
                STORAGE_SQLITE_FILE,
                sqliteFile);
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

    public String storageMode() {
        return storageMode;
    }

    public String sqliteFile() {
        return sqliteFile;
    }

    public boolean isSqliteFileStorage() {
        return STORAGE_SQLITE_FILE.equals(storageMode);
    }

    public boolean hasStorageBlockEquivalent() {
        return !STORAGE_MEMORY.equals(storageMode)
                && !(isSqliteFileStorage() && sqliteFile.equals(dataFile));
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

    private String optionalRelativeFile(final String value, final String fieldName) {
        String text = nullToEmpty(value).trim();
        if (text.isEmpty()) {
            return "";
        }
        return validatedRelativeFile(text, fieldName);
    }

    private String validatedStorageMode(final String value) {
        String text = nullToEmpty(value).trim();
        if (text.isEmpty()) {
            return STORAGE_MEMORY;
        }
        String normalized = text.toLowerCase().replace("_", "-");
        if (STORAGE_MEMORY.equals(normalized)
                || STORAGE_SQLITE_MEMORY.equals(normalized)
                || STORAGE_SQLITE_FILE.equals(normalized)) {
            return normalized;
        }
        throw new ThingifierYamlException("Unsupported project storage mode " + value);
    }

    private String resolvedStorageMode(final String storageMode, final String dataFile) {
        String validated = validatedStorageMode(storageMode);
        if (STORAGE_MEMORY.equals(validated) && looksLikeSqliteFile(dataFile)) {
            return STORAGE_SQLITE_FILE;
        }
        return validated;
    }

    private boolean looksLikeSqliteFile(final String fileName) {
        String normalized = nullToEmpty(fileName).trim().toLowerCase();
        return normalized.endsWith(".sqlite")
                || normalized.endsWith(".sqlite3")
                || normalized.endsWith(".db");
    }

    private String firstPresent(final String first, final String second) {
        String firstText = nullToEmpty(first).trim();
        if (!firstText.isEmpty()) {
            return firstText;
        }
        return nullToEmpty(second).trim();
    }

    private String nullToEmpty(final String value) {
        return value == null ? "" : value;
    }
}
