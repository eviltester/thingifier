package uk.co.compendiumdev.uirouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        final Set<String> allowedMetadataKeys = new HashSet<>(Arrays.asList(
                "seo_title",
                "seo_description",
                "meta_robots",
                "og_image",
                "og_image_alt",
                "og_type",
                "twitter_card",
                "twitter_site",
                "schema_type",
                "schema_author",
                "schema_publisher",
                "schema_image",
                "schema_breadcrumb_enabled",
                "schema_howto_enabled",
                "schema_howto_steps",
                "schema_video_enabled",
                "schema_video_id"
        ));
        final List<String> missingSeoTitle = new ArrayList<>();
        final List<String> emptySeoTitle = new ArrayList<>();
        final List<String> outOfRangeSeoTitle = new ArrayList<>();
        final List<String> missingSeoDescription = new ArrayList<>();
        final List<String> emptySeoDescription = new ArrayList<>();
        final List<String> outOfRangeSeoDescription = new ArrayList<>();
        final List<String> missingDescriptionForIndexablePage = new ArrayList<>();
        final List<String> malformedMetadataKeys = new ArrayList<>();
        final List<String> invalidOgImageOverrides = new ArrayList<>();

        final Path publicRoot = resolvePublicRoot();

        for (Path markdownFile : markdownFiles) {
            final String relativePath = contentRoot.relativize(markdownFile).toString().replace("\\", "/");
            final List<String> lines = Files.readAllLines(markdownFile, StandardCharsets.UTF_8);
            final String seoTitle = extractHeaderValue(lines, "seo_title");
            final String description = extractHeaderValue(lines, "description");
            final String seoDescription = extractHeaderValue(lines, "seo_description");
            final String metaRobots = extractHeaderValue(lines, "meta_robots");
            final String ogImage = extractHeaderValue(lines, "og_image");
            final List<String> headerKeys = extractHeaderKeys(lines);

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

            if (seoDescription == null) {
                missingSeoDescription.add(relativePath);
            } else if (seoDescription.trim().isEmpty()) {
                emptySeoDescription.add(relativePath);
            } else {
                // keep explicit fixture override free for escaping/special-char assertions
                if (!relativePath.equals("seo-metadata-test-page.md") &&
                        (seoDescription.length() < 110 || seoDescription.length() > 170)) {
                    outOfRangeSeoDescription.add(relativePath + " (" + seoDescription.length() + "): " + seoDescription);
                }
            }

            final boolean indexable = metaRobots == null || !metaRobots.toLowerCase().contains("noindex");
            if (indexable && (description == null || description.trim().isEmpty())) {
                missingDescriptionForIndexablePage.add(relativePath);
            }

            for (String key : headerKeys) {
                final boolean isMetadataKey = key.startsWith("seo_") ||
                        key.startsWith("og_") ||
                        key.startsWith("twitter_") ||
                        key.startsWith("schema_") ||
                        key.equals("meta_robots");
                if (isMetadataKey && !allowedMetadataKeys.contains(key)) {
                    malformedMetadataKeys.add(relativePath + " -> " + key);
                }
            }

            if (ogImage != null && !ogImage.trim().isEmpty() && ogImage.startsWith("/")) {
                final Path ogImagePath = publicRoot.resolve(ogImage.substring(1).replace("/", java.io.File.separator));
                if (!Files.exists(ogImagePath)) {
                    invalidOgImageOverrides.add(relativePath + " -> " + ogImage);
                }
            }
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
        Assertions.assertTrue(missingSeoDescription.isEmpty(),
                "Missing seo_description in: " + String.join("; ", missingSeoDescription));
        Assertions.assertTrue(emptySeoDescription.isEmpty(),
                "Empty seo_description in: " + String.join("; ", emptySeoDescription));
        Assertions.assertTrue(outOfRangeSeoDescription.isEmpty(),
                "seo_description out of range (110-170 chars): " + String.join("; ", outOfRangeSeoDescription));
        Assertions.assertTrue(duplicateSeoTitles.isEmpty(),
                "Duplicate seo_title values found: " + String.join("; ", duplicateSeoTitles));
        Assertions.assertTrue(missingDescriptionForIndexablePage.isEmpty(),
                "Indexable pages missing description: " + String.join("; ", missingDescriptionForIndexablePage));
        Assertions.assertTrue(malformedMetadataKeys.isEmpty(),
                "Malformed SEO/OG/Twitter metadata keys: " + String.join("; ", malformedMetadataKeys));
        Assertions.assertTrue(invalidOgImageOverrides.isEmpty(),
                "og_image override paths not found in public assets: " + String.join("; ", invalidOgImageOverrides));
    }

    private Path resolveContentRoot() {
        final Path moduleRelative = Paths.get("src", "main", "resources", "content");
        if (Files.exists(moduleRelative)) {
            return moduleRelative;
        }
        return Paths.get("challenger", "src", "main", "resources", "content");
    }

    private Path resolvePublicRoot() {
        final Path moduleRelative = Paths.get("src", "main", "resources", "public");
        if (Files.exists(moduleRelative)) {
            return moduleRelative;
        }
        return Paths.get("challenger", "src", "main", "resources", "public");
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

    private List<String> extractHeaderKeys(final List<String> lines) {
        final List<String> keys = new ArrayList<>();
        if (lines.size() < 3 || !lines.get(0).trim().equals("---")) {
            return keys;
        }

        for (int i = 1; i < lines.size(); i++) {
            final String line = lines.get(i);

            if (line.trim().equals("---")) {
                break;
            }

            final int keySeparator = line.indexOf(": ");
            if (keySeparator > 0) {
                keys.add(line.substring(0, keySeparator).trim());
            }
        }
        return keys;
    }
}
