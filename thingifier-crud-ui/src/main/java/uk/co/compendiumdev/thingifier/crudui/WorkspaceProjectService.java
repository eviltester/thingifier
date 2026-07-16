package uk.co.compendiumdev.thingifier.crudui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelAssembler;
import uk.co.compendiumdev.thingifier.yaml.ThingifierProjectManifest;
import uk.co.compendiumdev.thingifier.yaml.ThingifierProjectManifestYaml;
import uk.co.compendiumdev.thingifier.yaml.ThingifierYamlException;
import uk.co.compendiumdev.thingifier.yaml.ThingifierYamlLoader;

public final class WorkspaceProjectService {

    private static final String FILE_TYPE_BASE64 = "base64";
    private static final String FILE_TYPE_TEXT = "text";
    private static final String PROJECT_STORAGE_JSON = "json";
    private static final String PROJECT_STORAGE_SQLITE = "sqlite";

    private final ActiveThingifierWorkspace workspace;
    private final WorkspaceMetadataJson metadataJson;
    private final ThingifierProjectManifestYaml manifestYaml;
    private final ThingifierYamlLoader yamlLoader;

    public WorkspaceProjectService(
            final ActiveThingifierWorkspace workspace, final WorkspaceMetadataJson metadataJson) {
        this.workspace = workspace;
        this.metadataJson = metadataJson;
        this.manifestYaml = new ThingifierProjectManifestYaml();
        this.yamlLoader = new ThingifierYamlLoader();
    }

    public UiHttpResponse save(final String requestJson) {
        Path projectFolder = projectFolderFromRequest(requestJson);
        WorkspaceSnapshot snapshot = workspace.snapshot();
        String projectStorageMode = projectStorageModeFromRequest(requestJson, snapshot);

        try {
            if (Files.exists(projectFolder) && !Files.isDirectory(projectFolder)) {
                throw new CrudUiException(400, "Project path must be a folder");
            }
            Files.createDirectories(projectFolder);
            WorkspaceSnapshot saved =
                    PROJECT_STORAGE_SQLITE.equals(projectStorageMode)
                            ? saveSqliteBackedProject(projectFolder, snapshot)
                            : saveJsonBackedProject(projectFolder, snapshot);
            return UiHttpResponse.json(200, metadataJson.toJson(saved, "saved"));
        } catch (IOException e) {
            throw new CrudUiException(400, "Could not save project: " + e.getMessage());
        }
    }

    public UiHttpResponse load(final String requestJson) {
        Path requestedPath = projectPathFromRequest(requestJson);
        ProjectBundle bundle = ProjectBundle.resolve(requestedPath);
        return loadProjectBundle(bundle, bundle.folder().toString());
    }

    public UiHttpResponse check(final String requestJson) {
        ProjectActionRequest request = ProjectActionRequest.fromJson(requestJson, true);
        if (request.isSave()) {
            return UiHttpResponse.json(200, JsonSupport.toJson(checkSavePath(request.path())));
        }
        return UiHttpResponse.json(200, JsonSupport.toJson(checkLoadPath(request.path())));
    }

    public UiHttpResponse exportFiles() {
        return exportFiles("{}");
    }

    public UiHttpResponse exportFiles(final String requestJson) {
        try {
            WorkspaceSnapshot snapshot = workspace.snapshot();
            String projectStorageMode = projectStorageModeFromRequest(requestJson, snapshot);
            List<Map<String, Object>> files =
                    PROJECT_STORAGE_SQLITE.equals(projectStorageMode)
                            ? sqliteBackedProjectFiles(snapshot)
                            : jsonBackedProjectFiles(snapshot);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("formatVersion", 1);
            body.put("projectStatus", "exported");
            body.put("files", files);
            body.put("projectStorageMode", projectStorageMode);
            body.put(
                    "storageMode",
                    PROJECT_STORAGE_SQLITE.equals(projectStorageMode)
                            ? WorkspaceStorage.MODE_SQLITE_FILE
                            : WorkspaceStorage.MODE_MEMORY);
            return UiHttpResponse.json(200, JsonSupport.toJson(body));
        } catch (IOException e) {
            throw new CrudUiException(400, "Could not export project files: " + e.getMessage());
        }
    }

    public UiHttpResponse loadFiles(final String requestJson) {
        Map<?, ?> request =
                JsonSupport.fromJsonMap(
                        requestJson,
                        "Project file load request must contain a JSON object",
                        "Could not parse project file load JSON");
        String folderName = stringValue(request.get("folderName")).trim();
        Object filesValue = request.get("files");
        if (!(filesValue instanceof List)) {
            throw new CrudUiException(400, "Project file load request must contain files");
        }

        try {
            Path tempFolder = Files.createTempDirectory("thingifier-crud-ui-browser-project-");
            writePayloadFiles(tempFolder, (List<?>) filesValue);
            ProjectBundle bundle = ProjectBundle.resolve(tempFolder);
            return loadProjectBundle(bundle, browserProjectDisplay(folderName));
        } catch (IOException e) {
            throw new CrudUiException(400, "Could not load project files: " + e.getMessage());
        }
    }

    private UiHttpResponse loadProjectBundle(
            final ProjectBundle bundle, final String projectPathDisplay) {
        ThingifierProjectManifest manifest;
        String schemaYaml;
        try {
            manifest = manifestYaml.load(bundle.manifestPath());
            schemaYaml = Files.readString(bundle.resolve(manifest.schemaFile()));
        } catch (IOException e) {
            throw new CrudUiException(400, "Could not load project: " + e.getMessage());
        } catch (ThingifierYamlException e) {
            throw new CrudUiException(400, e.getMessage());
        }

        if (isSqliteBackedProject(bundle, manifest)) {
            return loadSqliteBackedProject(bundle, manifest, schemaYaml, projectPathDisplay);
        }

        String dataJson;
        try {
            dataJson = Files.readString(bundle.resolve(manifest.dataFile()));
        } catch (IOException e) {
            throw new CrudUiException(400, "Could not load project: " + e.getMessage());
        }

        ActiveThingifierWorkspace staging = null;
        try {
            Thingifier stagedThingifier = yamlLoader.loadThingifier(schemaYaml);
            staging = ActiveThingifierWorkspace.forThingifier(stagedThingifier);
            WorkspaceDataImporter importer =
                    new WorkspaceDataImporter(
                            staging,
                            new DynamicThingifierApiProxy(staging),
                            new WorkspaceMetadataJson());
            importer.importDataIntoCurrentWorkspace(dataJson);
            Thingifier released = staging.releaseThingifier();
            WorkspaceSnapshot loaded =
                    workspace.replaceWithProjectThingifier(
                            released,
                            WorkspaceStorage.memory(),
                            projectPathDisplay,
                            manifest.title(),
                            manifest.description());
            return UiHttpResponse.json(200, metadataJson.toJson(loaded, "loaded"));
        } finally {
            if (staging != null) {
                staging.close();
            }
        }
    }

    private WorkspaceSnapshot saveJsonBackedProject(
            final Path projectFolder, final WorkspaceSnapshot snapshot) throws IOException {
        ThingifierProjectManifest manifest =
                ThingifierProjectManifest.defaultFor(
                        snapshot.definition().title(), snapshot.definition().description());
        WorkspaceDataExporter exporter =
                new WorkspaceDataExporter(workspace, new DynamicThingifierApiProxy(workspace));

        writeString(
                projectFolder.resolve(ThingifierProjectManifest.DEFAULT_SCHEMA_FILE),
                snapshot.schemaYaml());
        writeString(
                projectFolder.resolve(ThingifierProjectManifest.DEFAULT_DATA_FILE),
                exporter.exportProjectDataJson());
        writeString(
                projectFolder.resolve(ThingifierProjectManifest.DEFAULT_MANIFEST_FILE),
                manifestYaml.export(manifest));
        if (!WorkspaceStorage.MODE_MEMORY.equals(workspace.snapshot().storage().mode())) {
            workspace.switchStorage(WorkspaceStorage.memory());
        }
        return workspace.markProjectSaved(projectFolder, manifest.title(), manifest.description());
    }

    private WorkspaceSnapshot saveSqliteBackedProject(
            final Path projectFolder, final WorkspaceSnapshot snapshot) throws IOException {
        ThingifierProjectManifest manifest =
                ThingifierProjectManifest.sqliteFileBackedFor(
                        snapshot.definition().title(), snapshot.definition().description());
        Path targetDatabase = projectFolder.resolve(manifest.sqliteFile()).normalize();
        if (!targetDatabase.startsWith(projectFolder)) {
            throw new CrudUiException(400, "Project SQLite file path escapes the project folder");
        }

        writeString(
                projectFolder.resolve(ThingifierProjectManifest.DEFAULT_SCHEMA_FILE),
                snapshot.schemaYaml());
        if (!samePath(targetDatabase, snapshot.storage().sqliteFile())) {
            migrateWorkspaceToSqliteFile(targetDatabase);
        }
        writeString(
                projectFolder.resolve(ThingifierProjectManifest.DEFAULT_MANIFEST_FILE),
                manifestYaml.export(manifest));
        return workspace.markProjectSaved(projectFolder, manifest.title(), manifest.description());
    }

    private void migrateWorkspaceToSqliteFile(final Path targetDatabase) throws IOException {
        String dataJson =
                new WorkspaceDataExporter(workspace, new DynamicThingifierApiProxy(workspace))
                        .exportProjectDataJson();
        deleteDatabaseFiles(targetDatabase);

        ActiveThingifierWorkspace staging = null;
        try {
            WorkspaceSnapshot snapshot = workspace.snapshot();
            WorkspaceStorage targetStorage = WorkspaceStorage.sqliteFile(targetDatabase);
            Thingifier stagedThingifier =
                    new ThingifierModelAssembler()
                            .assemble(snapshot.definition(), targetStorage.provider());
            stagedThingifier.clearAllData();
            staging = ActiveThingifierWorkspace.forThingifier(stagedThingifier, targetStorage);
            new WorkspaceDataImporter(
                            staging,
                            new DynamicThingifierApiProxy(staging),
                            new WorkspaceMetadataJson())
                    .importDataIntoCurrentWorkspace(dataJson);
            Thingifier released = staging.releaseThingifier();
            workspace.replaceWithProjectThingifier(
                    released,
                    targetStorage,
                    targetDatabase.getParent(),
                    snapshot.definition().title(),
                    snapshot.definition().description());
        } finally {
            if (staging != null) {
                staging.close();
            }
        }
    }

    private UiHttpResponse loadSqliteBackedProject(
            final ProjectBundle bundle,
            final ThingifierProjectManifest manifest,
            final String schemaYaml,
            final String projectPathDisplay) {
        WorkspaceStorage storage = WorkspaceStorage.sqliteFile(sqliteDataPath(bundle, manifest));
        Thingifier stagedThingifier = yamlLoader.loadThingifier(schemaYaml, storage.provider());
        WorkspaceSnapshot loaded =
                workspace.replaceWithProjectThingifier(
                        stagedThingifier,
                        storage,
                        projectPathDisplay,
                        manifest.title(),
                        manifest.description());
        return UiHttpResponse.json(200, metadataJson.toJson(loaded, "loaded"));
    }

    private boolean isSqliteBackedProject(
            final ProjectBundle bundle, final ThingifierProjectManifest manifest) {
        if (manifest.isSqliteFileStorage()) {
            return true;
        }
        if (manifest.dataFile().isEmpty()) {
            return false;
        }
        return hasSqliteHeader(bundle.resolve(manifest.dataFile()));
    }

    private Path sqliteDataPath(
            final ProjectBundle bundle, final ThingifierProjectManifest manifest) {
        String sqliteFile =
                manifest.sqliteFile().isEmpty() ? manifest.dataFile() : manifest.sqliteFile();
        return bundle.resolve(sqliteFile);
    }

    private boolean hasSqliteHeader(final Path path) {
        if (!Files.isRegularFile(path)) {
            return false;
        }
        byte[] expected = "SQLite format 3\u0000".getBytes(StandardCharsets.US_ASCII);
        byte[] actual = new byte[expected.length];
        try (java.io.InputStream input = Files.newInputStream(path)) {
            int read = input.read(actual);
            return read == expected.length && Arrays.equals(expected, actual);
        } catch (IOException e) {
            return false;
        }
    }

    private Map<String, Object> checkSavePath(final String pathText) {
        Path path = Paths.get(pathText).toAbsolutePath().normalize();
        Map<String, Object> body = baseCheckBody(ProjectActionRequest.ACTION_SAVE, path);
        if (path.getFileName() != null
                && ThingifierProjectManifest.DEFAULT_MANIFEST_FILE.equals(
                        path.getFileName().toString())) {
            body.put("message", "Project save path must be a folder");
            return body;
        }
        if (Files.exists(path) && !Files.isDirectory(path)) {
            body.put("message", "Project path must be a folder");
            return body;
        }
        List<String> managedFiles = managedFilesIn(path);
        body.put("valid", true);
        body.put("canProceed", true);
        body.put("kind", Files.exists(path) ? "folder" : "creatable-folder");
        body.put("exists", Files.exists(path));
        body.put("managedFiles", managedFiles);
        body.put(
                "message",
                Files.exists(path)
                        ? "Project folder is available."
                        : "Project folder can be created.");
        if (!managedFiles.isEmpty()) {
            body.put(
                    "warning",
                    "Saving will overwrite managed project files: "
                            + String.join(", ", managedFiles));
        }
        return body;
    }

    private Map<String, Object> checkLoadPath(final String pathText) {
        Path path = Paths.get(pathText).toAbsolutePath().normalize();
        Map<String, Object> body = baseCheckBody(ProjectActionRequest.ACTION_LOAD, path);
        ProjectBundle bundle = ProjectBundle.resolve(path);
        Path manifestPath = bundle.manifestPath();
        body.put("kind", Files.isDirectory(path) ? "folder" : "project-file");
        if (!Files.exists(manifestPath)) {
            body.put("message", "Project file does not exist.");
            return body;
        }
        if (!Files.isRegularFile(manifestPath)) {
            body.put("message", "Project file path must be a file.");
            return body;
        }
        try {
            ThingifierProjectManifest manifest = manifestYaml.load(manifestPath);
            Path schemaFile = bundle.resolve(manifest.schemaFile());
            if (!Files.exists(schemaFile)) {
                body.put("message", "Project schema file does not exist.");
                return body;
            }
            if (isSqliteBackedProject(bundle, manifest)) {
                Path sqliteFile = sqliteDataPath(bundle, manifest);
                if (!Files.exists(sqliteFile)) {
                    body.put("message", "Project SQLite file does not exist.");
                    return body;
                }
            } else {
                Path dataFile = bundle.resolve(manifest.dataFile());
                if (!Files.exists(dataFile)) {
                    body.put("message", "Project data file does not exist.");
                    return body;
                }
            }
            body.put("valid", true);
            body.put("canProceed", true);
            body.put("message", "Project can be loaded.");
        } catch (ThingifierYamlException e) {
            body.put("message", e.getMessage());
        } catch (IOException e) {
            body.put("message", "Could not check project: " + e.getMessage());
        }
        return body;
    }

    private Map<String, Object> baseCheckBody(final String action, final Path path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("action", action);
        body.put("path", path.toString());
        body.put("valid", false);
        body.put("canProceed", false);
        body.put("managedFiles", List.of());
        body.put("warning", "");
        return body;
    }

    private List<String> managedFilesIn(final Path folder) {
        List<String> managedFiles = new ArrayList<>();
        if (!Files.isDirectory(folder)) {
            return managedFiles;
        }
        addIfExists(managedFiles, folder, ThingifierProjectManifest.DEFAULT_MANIFEST_FILE);
        addIfExists(managedFiles, folder, ThingifierProjectManifest.DEFAULT_SCHEMA_FILE);
        addIfExists(managedFiles, folder, ThingifierProjectManifest.DEFAULT_DATA_FILE);
        addIfExists(managedFiles, folder, ThingifierProjectManifest.DEFAULT_SQLITE_FILE);
        return managedFiles;
    }

    private void addIfExists(final List<String> files, final Path folder, final String name) {
        if (Files.exists(folder.resolve(name))) {
            files.add(name);
        }
    }

    private List<Map<String, Object>> jsonBackedProjectFiles(final WorkspaceSnapshot snapshot) {
        ThingifierProjectManifest manifest =
                ThingifierProjectManifest.defaultFor(
                        snapshot.definition().title(), snapshot.definition().description());
        WorkspaceDataExporter exporter =
                new WorkspaceDataExporter(workspace, new DynamicThingifierApiProxy(workspace));
        List<Map<String, Object>> files = new ArrayList<>();
        files.add(
                textFile(
                        ThingifierProjectManifest.DEFAULT_MANIFEST_FILE,
                        manifestYaml.export(manifest)));
        files.add(textFile(ThingifierProjectManifest.DEFAULT_SCHEMA_FILE, snapshot.schemaYaml()));
        files.add(
                textFile(
                        ThingifierProjectManifest.DEFAULT_DATA_FILE,
                        exporter.exportProjectDataJson()));
        return files;
    }

    private List<Map<String, Object>> sqliteBackedProjectFiles(final WorkspaceSnapshot snapshot)
            throws IOException {
        ThingifierProjectManifest manifest =
                ThingifierProjectManifest.sqliteFileBackedFor(
                        snapshot.definition().title(), snapshot.definition().description());
        List<Map<String, Object>> files = new ArrayList<>();
        files.add(
                textFile(
                        ThingifierProjectManifest.DEFAULT_MANIFEST_FILE,
                        manifestYaml.export(manifest)));
        files.add(textFile(ThingifierProjectManifest.DEFAULT_SCHEMA_FILE, snapshot.schemaYaml()));
        files.add(
                binaryFile(
                        ThingifierProjectManifest.DEFAULT_SQLITE_FILE,
                        sqliteDatabaseBytesForSnapshot(snapshot)));
        return files;
    }

    private byte[] sqliteDatabaseBytesForSnapshot(final WorkspaceSnapshot snapshot)
            throws IOException {
        String dataJson =
                new WorkspaceDataExporter(workspace, new DynamicThingifierApiProxy(workspace))
                        .exportProjectDataJson();
        Path tempFolder = Files.createTempDirectory("thingifier-crud-ui-project-export-");
        Path tempDatabase = tempFolder.resolve(ThingifierProjectManifest.DEFAULT_SQLITE_FILE);
        ActiveThingifierWorkspace staging = null;
        try {
            WorkspaceStorage targetStorage = WorkspaceStorage.sqliteFile(tempDatabase);
            Thingifier stagedThingifier =
                    new ThingifierModelAssembler()
                            .assemble(snapshot.definition(), targetStorage.provider());
            stagedThingifier.clearAllData();
            staging = ActiveThingifierWorkspace.forThingifier(stagedThingifier, targetStorage);
            new WorkspaceDataImporter(
                            staging,
                            new DynamicThingifierApiProxy(staging),
                            new WorkspaceMetadataJson())
                    .importDataIntoCurrentWorkspace(dataJson);
        } finally {
            if (staging != null) {
                staging.close();
            }
        }
        byte[] bytes = Files.readAllBytes(tempDatabase);
        deleteDatabaseFiles(tempDatabase);
        Files.deleteIfExists(tempFolder);
        return bytes;
    }

    private Map<String, Object> textFile(final String name, final String contents) {
        Map<String, Object> file = new LinkedHashMap<>();
        file.put("name", name);
        file.put("type", FILE_TYPE_TEXT);
        file.put("content", contents);
        return file;
    }

    private Map<String, Object> binaryFile(final String name, final byte[] contents) {
        Map<String, Object> file = new LinkedHashMap<>();
        file.put("name", name);
        file.put("type", FILE_TYPE_BASE64);
        file.put("content", Base64.getEncoder().encodeToString(contents));
        return file;
    }

    private void writePayloadFiles(final Path folder, final List<?> files) throws IOException {
        for (Object value : files) {
            if (!(value instanceof Map)) {
                throw new CrudUiException(400, "Project files must be objects");
            }
            Map<?, ?> file = (Map<?, ?>) value;
            String name = stringValue(file.get("name")).trim();
            String type = stringValue(file.get("type")).trim();
            String content = stringValue(file.get("content"));
            Path target = payloadPath(folder, name);
            if (FILE_TYPE_BASE64.equals(type)) {
                Files.write(target, Base64.getDecoder().decode(content));
            } else if (FILE_TYPE_TEXT.equals(type) || type.isEmpty()) {
                writeString(target, content);
            } else {
                throw new CrudUiException(400, "Unsupported project file type: " + type);
            }
        }
    }

    private Path payloadPath(final Path folder, final String fileName) {
        if (fileName.isEmpty()
                || fileName.contains("/")
                || fileName.contains("\\")
                || Paths.get(fileName).isAbsolute()) {
            throw new CrudUiException(400, "Project file names must be project-root files");
        }
        Path resolved = folder.resolve(fileName).normalize();
        if (!resolved.startsWith(folder)) {
            throw new CrudUiException(400, "Project file path escapes the project folder");
        }
        return resolved;
    }

    private String browserProjectDisplay(final String folderName) {
        String safeName = folderName.isEmpty() ? "selected folder" : folderName;
        return "Browser folder: " + safeName;
    }

    private Path projectFolderFromRequest(final String requestJson) {
        Path path = projectPathFromRequest(requestJson).toAbsolutePath().normalize();
        if (path.getFileName() != null
                && ThingifierProjectManifest.DEFAULT_MANIFEST_FILE.equals(
                        path.getFileName().toString())) {
            throw new CrudUiException(400, "Project save path must be a folder");
        }
        return path;
    }

    private Path projectPathFromRequest(final String requestJson) {
        Map<?, ?> request = projectRequestMap(requestJson);
        String path = stringValue(request.get("path")).trim();
        if (path.isEmpty()) {
            throw new CrudUiException(400, "Project request must contain path");
        }
        return Paths.get(path);
    }

    private String projectStorageModeFromRequest(
            final String requestJson, final WorkspaceSnapshot snapshot) {
        Map<?, ?> request = optionalProjectRequestMap(requestJson);
        String requested = stringValue(request.get("projectStorageMode")).trim();
        if (requested.isEmpty()) {
            requested = stringValue(request.get("storageMode")).trim();
        }
        if (requested.isEmpty()) {
            return snapshot.storage().isSqliteFile()
                    ? PROJECT_STORAGE_SQLITE
                    : PROJECT_STORAGE_JSON;
        }
        String normalized = requested.toLowerCase().replace("_", "-");
        if (PROJECT_STORAGE_JSON.equals(normalized)
                || "data-json".equals(normalized)
                || WorkspaceStorage.MODE_MEMORY.equals(normalized)) {
            return PROJECT_STORAGE_JSON;
        }
        if (PROJECT_STORAGE_SQLITE.equals(normalized)
                || WorkspaceStorage.MODE_SQLITE_FILE.equals(normalized)
                || "data-sqlite".equals(normalized)) {
            return PROJECT_STORAGE_SQLITE;
        }
        throw new CrudUiException(400, "Project storage mode must be json or sqlite");
    }

    private Map<?, ?> optionalProjectRequestMap(final String requestJson) {
        if (requestJson == null || requestJson.trim().isEmpty()) {
            return Map.of();
        }
        return projectRequestMap(requestJson);
    }

    private Map<?, ?> projectRequestMap(final String requestJson) {
        return JsonSupport.fromJsonMap(
                requestJson,
                "Project request must contain a JSON object",
                "Could not parse project request JSON");
    }

    private void writeString(final Path path, final String contents) throws IOException {
        Files.writeString(path, contents, StandardCharsets.UTF_8);
    }

    private void deleteDatabaseFiles(final Path databasePath) throws IOException {
        Files.deleteIfExists(databasePath);
        Files.deleteIfExists(Path.of(databasePath.toString() + "-wal"));
        Files.deleteIfExists(Path.of(databasePath.toString() + "-shm"));
    }

    private boolean samePath(final Path first, final Path second) {
        if (first == null || second == null) {
            return false;
        }
        return first.toAbsolutePath().normalize().equals(second.toAbsolutePath().normalize());
    }

    private String stringValue(final Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static final class ProjectBundle {

        private final Path folder;
        private final Path manifestPath;

        private ProjectBundle(final Path folder, final Path manifestPath) {
            this.folder = folder;
            this.manifestPath = manifestPath;
        }

        static ProjectBundle resolve(final Path requestedPath) {
            Path normalized = requestedPath.toAbsolutePath().normalize();
            Path manifest;
            Path folder;
            if (Files.isDirectory(normalized)) {
                folder = normalized;
                manifest = folder.resolve(ThingifierProjectManifest.DEFAULT_MANIFEST_FILE);
            } else {
                manifest = normalized;
                folder = manifest.getParent();
            }
            if (folder == null) {
                folder = Paths.get(".").toAbsolutePath().normalize();
            }
            return new ProjectBundle(folder, manifest);
        }

        Path folder() {
            return folder;
        }

        Path manifestPath() {
            return manifestPath;
        }

        Path resolve(final String relativePath) {
            Path resolved = folder.resolve(relativePath).normalize();
            if (!resolved.startsWith(folder)) {
                throw new CrudUiException(400, "Project file path escapes the project folder");
            }
            return resolved;
        }
    }
}
