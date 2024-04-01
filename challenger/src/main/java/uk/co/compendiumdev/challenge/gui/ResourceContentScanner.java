package uk.co.compendiumdev.challenge.gui;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;

import java.util.ArrayList;
import java.util.List;

public class ResourceContentScanner {

    List<String> availableContent = new ArrayList<>();

    public List<String> scanForFullPathsOfExtensionsIn(String folder, String extension) {
        List<String> pathsToFileContent = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph().acceptPaths("content/").scan()) {
            scanResult.getResourcesWithExtension("md").forEach( (Resource res) -> {
                pathsToFileContent.add(res.getPath());
            });
        }
        return pathsToFileContent;
    }

    public void addPathsToAvailableContent(List<String> pathsToFileContent) {
        availableContent.addAll(pathsToFileContent);
    }
}
