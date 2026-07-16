package uk.co.compendiumdev.thingifier.crudui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.yaml.ThingifierProjectManifest;
import uk.co.compendiumdev.thingifier.yaml.ThingifierProjectManifestYaml;
import uk.co.compendiumdev.thingifier.yaml.ThingifierYamlException;
import uk.co.compendiumdev.thingifier.yaml.ThingifierYamlLoader;

public final class WorkspaceProjectService {

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
        ThingifierProjectManifest manifest =
                ThingifierProjectManifest.defaultFor(
                        snapshot.definition().title(), snapshot.definition().description());
        WorkspaceDataExporter exporter =
                new WorkspaceDataExporter(workspace, new DynamicThingifierApiProxy(workspace));

        try {
            if (Files.exists(projectFolder) && !Files.isDirectory(projectFolder)) {
                throw new CrudUiException(400, "Project path must be a folder");
            }
            Files.createDirectories(projectFolder);
            writeString(
                    projectFolder.resolve(ThingifierProjectManifest.DEFAULT_SCHEMA_FILE),
                    snapshot.schemaYaml());
            writeString(
                    projectFolder.resolve(ThingifierProjectManifest.DEFAULT_DATA_FILE),
                    exporter.exportProjectDataJson());
            writeString(
                    projectFolder.resolve(ThingifierProjectManifest.DEFAULT_MANIFEST_FILE),
                    manifestYaml.export(manifest));
            WorkspaceSnapshot saved =
                    workspace.markProjectSaved(
                            projectFolder, manifest.title(), manifest.description());
            return UiHttpResponse.json(200, metadataJson.toJson(saved, "saved"));
        } catch (IOException e) {
            throw new CrudUiException(400, "Could not save project: " + e.getMessage());
        }
    }

    public UiHttpResponse load(final String requestJson) {
        Path requestedPath = projectPathFromRequest(requestJson);
        ProjectBundle bundle = ProjectBundle.resolve(requestedPath);
        ThingifierProjectManifest manifest;
        String schemaYaml;
        String dataJson;
        try {
            manifest = manifestYaml.load(bundle.manifestPath());
            schemaYaml = Files.readString(bundle.resolve(manifest.schemaFile()));
            dataJson = Files.readString(bundle.resolve(manifest.dataFile()));
        } catch (IOException e) {
            throw new CrudUiException(400, "Could not load project: " + e.getMessage());
        } catch (ThingifierYamlException e) {
            throw new CrudUiException(400, e.getMessage());
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
                            released, bundle.folder(), manifest.title(), manifest.description());
            return UiHttpResponse.json(200, metadataJson.toJson(loaded, "loaded"));
        } finally {
            if (staging != null) {
                staging.close();
            }
        }
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
        Map<?, ?> request =
                JsonSupport.fromJsonMap(
                        requestJson,
                        "Project request must contain a JSON object",
                        "Could not parse project request JSON");
        String path = stringValue(request.get("path")).trim();
        if (path.isEmpty()) {
            throw new CrudUiException(400, "Project request must contain path");
        }
        return Paths.get(path);
    }

    private void writeString(final Path path, final String contents) throws IOException {
        Files.writeString(path, contents, StandardCharsets.UTF_8);
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
