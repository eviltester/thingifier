package uk.co.compendiumdev.thingifier.yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.schema.definition.SchemaDefinitionValidationReport;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelAssembler;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelDefinition;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreProvider;
import uk.co.compendiumdev.thingifier.yaml.internal.YamlThingifierDocument;
import uk.co.compendiumdev.thingifier.yaml.internal.YamlThingifierDocumentMapper;

public final class ThingifierYamlLoader {

    private final ThingifierModelAssembler assembler;
    private final YamlThingifierDocumentMapper mapper;

    public ThingifierYamlLoader() {
        assembler = new ThingifierModelAssembler();
        mapper = new YamlThingifierDocumentMapper();
    }

    public ThingifierModelDefinition loadDefinition(final Path path) throws IOException {
        try (InputStream input = Files.newInputStream(path)) {
            return loadDefinition(input);
        }
    }

    public ThingifierModelDefinition loadDefinition(final InputStream input) {
        return definitionFrom(loadDocument(input));
    }

    public ThingifierModelDefinition loadDefinition(final String yamlText) {
        return definitionFrom(loadDocument(yamlText));
    }

    public Thingifier loadThingifier(final Path path) throws IOException {
        return assemble(loadDefinition(path));
    }

    public Thingifier loadThingifier(final Path path, final ThingStoreProvider storeProvider)
            throws IOException {
        return assemble(loadDefinition(path), storeProvider);
    }

    public Thingifier loadThingifier(final InputStream input) {
        return assemble(loadDefinition(input));
    }

    public Thingifier loadThingifier(
            final InputStream input, final ThingStoreProvider storeProvider) {
        return assemble(loadDefinition(input), storeProvider);
    }

    public Thingifier loadThingifier(final String yamlText) {
        return assemble(loadDefinition(yamlText));
    }

    public Thingifier loadThingifier(
            final String yamlText, final ThingStoreProvider storeProvider) {
        return assemble(loadDefinition(yamlText), storeProvider);
    }

    private Thingifier assemble(final ThingifierModelDefinition definition) {
        return assemble(definition, null);
    }

    private Thingifier assemble(
            final ThingifierModelDefinition definition, final ThingStoreProvider storeProvider) {
        final SchemaDefinitionValidationReport report = assembler.validate(definition);
        if (!report.isValid()) {
            throw new ThingifierYamlException(report.combinedMessages());
        }
        return assembler.assemble(definition, storeProvider);
    }

    private YamlThingifierDocument loadDocument(final InputStream input) {
        try {
            return YamlThingifierDocument.fromObject(yaml().load(input));
        } catch (YAMLException | ClassCastException | IllegalArgumentException e) {
            throw new ThingifierYamlException("Could not parse YAML schema", e);
        }
    }

    private YamlThingifierDocument loadDocument(final String yamlText) {
        try {
            return YamlThingifierDocument.fromObject(yaml().load(yamlText));
        } catch (YAMLException | ClassCastException | IllegalArgumentException e) {
            throw new ThingifierYamlException("Could not parse YAML schema", e);
        }
    }

    private ThingifierModelDefinition definitionFrom(final YamlThingifierDocument document) {
        try {
            return mapper.toDefinition(document);
        } catch (ClassCastException | IllegalArgumentException e) {
            throw new ThingifierYamlException("Could not map YAML schema", e);
        }
    }

    private Yaml yaml() {
        final LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(20);
        return new Yaml(new SafeConstructor(options));
    }
}
