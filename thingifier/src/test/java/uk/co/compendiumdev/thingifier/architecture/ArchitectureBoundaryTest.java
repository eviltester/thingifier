package uk.co.compendiumdev.thingifier.architecture;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.RelationshipRepository;

public class ArchitectureBoundaryTest {

    @Test
    public void applicationPackageDoesNotImportTransportOrApiResponseTypes() throws IOException {
        String applicationSources =
                allJavaSources(
                        moduleRoot()
                                .resolve(
                                        "src/main/java/uk/co/compendiumdev/thingifier/application"));

        Assertions.assertFalse(applicationSources.contains("import spark."));
        Assertions.assertFalse(
                applicationSources.contains("uk.co.compendiumdev.thingifier.adapter."));
        Assertions.assertFalse(
                applicationSources.contains(
                        "uk.co.compendiumdev.thingifier.api.response.ApiResponse"));
    }

    @Test
    public void commandAndQueryPackagesDoNotImportApiResponse() throws IOException {
        String commandSources =
                allJavaSources(
                        moduleRoot()
                                .resolve(
                                        "src/main/java/uk/co/compendiumdev/thingifier/application/command"));
        String querySources =
                allJavaSources(
                        moduleRoot()
                                .resolve(
                                        "src/main/java/uk/co/compendiumdev/thingifier/application/query"));

        Assertions.assertFalse(commandSources.contains("ApiResponse"));
        Assertions.assertFalse(querySources.contains("ApiResponse"));
    }

    @Test
    public void relationshipBodyParserDoesNotResolveRepositoryInstances() throws IOException {
        String parserSource =
                Files.readString(
                        moduleRoot()
                                .resolve(
                                        "src/main/java/uk/co/compendiumdev/thingifier/api/restapihandlers/RelationshipBodyCommandParser.java"));

        Assertions.assertFalse(parserSource.contains("entityQueries()"));
        Assertions.assertFalse(parserSource.contains("findByField("));
        Assertions.assertFalse(parserSource.contains("findByQueryIdentifier("));
    }

    @Test
    public void coreDoesNotContainUrlRouteQueryParser() {
        Assertions.assertFalse(
                Files.exists(
                        coreMainRoot()
                                .resolve(
                                        "uk/co/compendiumdev/thingifier/core/query/RepositoryUrlQuery.java")));
    }

    @Test
    public void relationshipRepositoryRemovalApiDoesNotExposeCascadeDecisions()
            throws NoSuchMethodException {
        Method removeBetween =
                RelationshipRepository.class.getMethod(
                        "removeBetween", EntityInstance.class, EntityInstance.class, String.class);
        Method removeAll =
                RelationshipRepository.class.getMethod("removeAll", EntityInstance.class);

        Assertions.assertEquals(Void.TYPE, removeBetween.getReturnType());
        Assertions.assertEquals(Void.TYPE, removeAll.getReturnType());
    }

    private String allJavaSources(final Path root) throws IOException {
        StringBuilder combined = new StringBuilder();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> combined.append(readUnchecked(path)).append('\n'));
        }
        return combined.toString();
    }

    private String readUnchecked(final Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path moduleRoot() {
        Path cwd = Paths.get("").toAbsolutePath();
        if (Files.exists(cwd.resolve("src/main/java"))) {
            return cwd;
        }
        return cwd.resolve("thingifier");
    }

    private Path coreMainRoot() {
        Path cwd = Paths.get("").toAbsolutePath();
        Path sibling = cwd.resolve("../ercoremodel/src/main/java").normalize();
        if (Files.exists(sibling)) {
            return sibling;
        }
        return cwd.resolve("ercoremodel/src/main/java");
    }
}
