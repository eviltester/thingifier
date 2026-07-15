package uk.co.compendiumdev.thingifier.yaml;

import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelDefinition;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelExporter;
import uk.co.compendiumdev.thingifier.yaml.internal.YamlThingifierDocumentMapper;

public final class ThingifierYamlExporter {

    private final YamlThingifierDocumentMapper mapper;

    public ThingifierYamlExporter() {
        mapper = new YamlThingifierDocumentMapper();
    }

    public String export(final Thingifier thingifier) {
        return export(new ThingifierModelExporter().export(thingifier));
    }

    public String export(final ThingifierModelDefinition definition) {
        final Map<String, Object> document = mapper.toYamlMap(definition);
        return yaml().dump(document);
    }

    private Yaml yaml() {
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setSplitLines(false);
        return new Yaml(options);
    }
}
