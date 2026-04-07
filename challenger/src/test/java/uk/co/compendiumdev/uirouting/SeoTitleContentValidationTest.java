package uk.co.compendiumdev.uirouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SeoTitleContentValidationTest {

    @Test
    void allContentMarkdownFilesContainSeoTitleAndMeetQualityChecks() throws IOException {

        final Path contentRoot = resolveContentRoot();

        final List<Path> markdownFiles = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(contentRoot)) {
            walk.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".md"))
                    .forEach(markdownFiles::add);
        }

        Assertions.assertFalse(markdownFiles.isEmpty(), "No markdown files found in content directory");

        final Map<String, List<String>> seoTitlesToPaths = new HashMap<>();
        final List<String> missingSeoTitle = new ArrayList<>();
        final List<String> emptySeoTitle = new ArrayList<>();
        final List<String> outOfRangeSeoTitle = new ArrayList<>();

        for (Path markdownFile : markdownFiles) {
            final String relativePath = contentRoot.relativize(markdownFile).toString().replace("\\", "/");
            final List<String> lines = Files.readAllLines(markdownFile, StandardCharsets.UTF_8);
            final String seoTitle = extractHeaderValue(lines, "seo_title");

            if (seoTitle == null) {
                missingSeoTitle.add(relativePath);
                continue;
            }

            if (seoTitle.trim().isEmpty()) {
                emptySeoTitle.add(relativePath);
                continue;
            }

            if (seoTitle.length() < 45 || seoTitle.length() > 70) {
                outOfRangeSeoTitle.add(relativePath + " (" + seoTitle.length() + "): " + seoTitle);
            }

            seoTitlesToPaths.computeIfAbsent(seoTitle, key -> new ArrayList<>()).add(relativePath);
        }

        final List<String> duplicateSeoTitles = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : seoTitlesToPaths.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicateSeoTitles.add(entry.getKey() + " -> " + String.join(", ", entry.getValue()));
            }
        }

        Assertions.assertTrue(missingSeoTitle.isEmpty(),
                "Missing seo_title in: " + String.join("; ", missingSeoTitle));
        Assertions.assertTrue(emptySeoTitle.isEmpty(),
                "Empty seo_title in: " + String.join("; ", emptySeoTitle));
        Assertions.assertTrue(outOfRangeSeoTitle.isEmpty(),
                "seo_title out of range (45-70 chars): " + String.join("; ", outOfRangeSeoTitle));
        Assertions.assertTrue(duplicateSeoTitles.isEmpty(),
                "Duplicate seo_title values found: " + String.join("; ", duplicateSeoTitles));
    }

    private Path resolveContentRoot() {
        final Path moduleRelative = Paths.get("src", "main", "resources", "content");
        if (Files.exists(moduleRelative)) {
            return moduleRelative;
        }
        return Paths.get("challenger", "src", "main", "resources", "content");
    }

    private String extractHeaderValue(final List<String> lines, final String key) {

        if (lines.size() < 3 || !lines.get(0).trim().equals("---")) {
            return null;
        }

        for (int i = 1; i < lines.size(); i++) {
            final String line = lines.get(i);

            if (line.trim().equals("---")) {
                break;
            }

            if (line.startsWith(key + ": ")) {
                return line.replaceFirst("^" + key + ":\\s*", "").trim();
            }
        }

        return null;
    }
}
