package uk.co.compendiumdev.challenge.gui;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;

import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDate;
import java.util.*;

public class ResourceContentScanner {

    List<String> availableContent = new ArrayList<>();

    public List<String> scanForFullPathsOfExtensionsIn(String folder, String extension) {
        List<String> pathsToFileContent = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph().acceptPaths(folder).scan()) {
            scanResult.getResourcesWithExtension(extension).forEach( (Resource res) -> {
                pathsToFileContent.add(res.getPath());
            });
        }
        return pathsToFileContent;
    }

    public Map<String, LocalDate> scanForUrlsWithDates(String folder, String extension) {
        Map<String, LocalDate> urlsWithLastModDates = new HashMap<>();
        try (ScanResult scanResult = new ClassGraph().acceptPaths(folder).scan()) {
            scanResult.getResourcesWithExtension(extension).forEach( (Resource res) -> {
                urlsWithLastModDates.put(
                        res.getPath().replaceFirst(folder,"").replace("." + extension,""),
                        Instant.ofEpochMilli(res.getLastModified()).atZone(ZoneId.systemDefault()).toLocalDate()
                );
            });
        }
        return urlsWithLastModDates;
    }
    public void addPathsToAvailableContent(List<String> pathsToFileContent) {
        availableContent.addAll(pathsToFileContent);
    }

}
