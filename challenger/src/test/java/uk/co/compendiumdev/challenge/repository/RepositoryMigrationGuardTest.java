package uk.co.compendiumdev.challenge.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RepositoryMigrationGuardTest {

    @Test
    public void productionCodeDoesNotCallLegacySnapshotEscapesOutsideCompatibilityAdapters() throws IOException {
        List<String> violations = productionJavaLinesContaining(
                List.of(
                        "getInstanceData(",
                        "getThingInstancesNamed(",
                        "getInstancesForSingularOrPluralNamedEntity(",
                        "getThings(",
                        "cloneWithDifferentData(",
                        "createInstanceCollectionFor(",
                        "getAllInstanceCollections(",
                        "getInstanceCollectionForEntityNamed("),
                List.of(
                        "ercoremodel/src/main/java/uk/co/compendiumdev/thingifier/core/EntityRelModel.java",
                        "ercoremodel/src/main/java/uk/co/compendiumdev/thingifier/core/domain/instances/ERInstanceData.java",
                        "ercoremodel/src/main/java/uk/co/compendiumdev/thingifier/core/domain/datapopulator/LegacyDataPopulatorAdapter.java",
                        "ercoremodel/src/main/java/uk/co/compendiumdev/thingifier/core/query/SimpleQuery.java",
                        "ercoremodel/src/main/java/uk/co/compendiumdev/thingifier/core/repository/ThingRepository.java",
                        "ercoremodel/src/main/java/uk/co/compendiumdev/thingifier/core/repository/InMemoryThingRepository.java",
                        "ercoremodel/src/main/java/uk/co/compendiumdev/thingifier/core/repository/SqliteThingRepository.java",
                        "thingifier/src/main/java/uk/co/compendiumdev/thingifier/Thingifier.java"));

        Assertions.assertTrue(
                violations.isEmpty(),
                "Production code should use repository-native APIs instead of legacy snapshot escapes:\n" +
                        String.join("\n", violations));
    }

    @Test
    public void thingifierAndChallengerProductionCodeDoesNotDependOnCompatibilityCollections() throws IOException {
        List<String> violations = productionJavaLinesContaining(
                List.of("EntityInstanceCollection"),
                List.of("thingifier/src/main/java/uk/co/compendiumdev/thingifier/Thingifier.java"),
                List.of("thingifier", "challenger"));

        Assertions.assertTrue(
                violations.isEmpty(),
                "Thingifier and Challenger runtime code should not use compatibility collections outside the deprecated public Thingifier API:\n" +
                        String.join("\n", violations));
    }

    @Test
    public void productionCodeDoesNotUseDeprecatedSimpleQueryOutsideItsOwnClass() throws IOException {
        List<String> violations = productionJavaLinesContaining(
                List.of("SimpleQuery"),
                List.of("ercoremodel/src/main/java/uk/co/compendiumdev/thingifier/core/query/SimpleQuery.java"));

        Assertions.assertTrue(
                violations.isEmpty(),
                "Production code should use RepositoryUrlQuery or repository APIs instead of SimpleQuery:\n" +
                        String.join("\n", violations));
    }

    private List<String> productionJavaLinesContaining(
            final List<String> terms,
            final List<String> allowedFileSuffixes) throws IOException {
        return productionJavaLinesContaining(
                terms, allowedFileSuffixes, List.of("ercoremodel", "thingifier", "challenger"));
    }

    private List<String> productionJavaLinesContaining(
            final List<String> terms,
            final List<String> allowedFileSuffixes,
            final List<String> modules) throws IOException {

        Path root = repoRoot();
        List<String> violations = new ArrayList<>();

        for (String module : modules) {
            Path sourceRoot = root.resolve(module).resolve("src/main/java");
            if (!Files.exists(sourceRoot)) {
                continue;
            }

            try (Stream<Path> paths = Files.walk(sourceRoot)) {
                for (Path path : paths.filter(path -> path.toString().endsWith(".java")).toList()) {
                    String relativePath = root.relativize(path).toString().replace('\\', '/');
                    if (isAllowed(relativePath, allowedFileSuffixes)) {
                        continue;
                    }

                    List<String> lines = Files.readAllLines(path);
                    for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                        String line = lines.get(lineIndex);
                        if (containsAny(line, terms)) {
                            violations.add(relativePath + ":" + (lineIndex + 1) + ": " + line.trim());
                        }
                    }
                }
            }
        }

        return violations;
    }

    private Path repoRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("ercoremodel")) &&
                    Files.exists(current.resolve("thingifier")) &&
                    Files.exists(current.resolve("challenger"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate thingifier repository root");
    }

    private boolean isAllowed(final String relativePath, final List<String> allowedFileSuffixes) {
        for (String allowed : allowedFileSuffixes) {
            if (relativePath.endsWith(allowed)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAny(final String line, final List<String> terms) {
        for (String term : terms) {
            if (line.contains(term)) {
                return true;
            }
        }
        return false;
    }
}
