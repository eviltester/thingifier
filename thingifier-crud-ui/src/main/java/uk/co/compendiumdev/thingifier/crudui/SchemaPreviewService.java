package uk.co.compendiumdev.thingifier.crudui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.application.schema.definition.SchemaDefinitionValidationReport;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelAssembler;
import uk.co.compendiumdev.thingifier.application.schema.definition.ThingifierModelDefinition;
import uk.co.compendiumdev.thingifier.yaml.ThingifierYamlExporter;
import uk.co.compendiumdev.thingifier.yaml.ThingifierYamlLoader;

public final class SchemaPreviewService {

    private final ThingifierYamlLoader yamlLoader;
    private final ThingifierYamlExporter yamlExporter;
    private final ThingifierModelAssembler assembler;
    private final SchemaDefinitionDraftJson draftJson;
    private final SchemaDiagramExporter diagramExporter;

    public SchemaPreviewService() {
        yamlLoader = new ThingifierYamlLoader();
        yamlExporter = new ThingifierYamlExporter();
        assembler = new ThingifierModelAssembler();
        draftJson = new SchemaDefinitionDraftJson();
        diagramExporter = new SchemaDiagramExporter();
    }

    public UiHttpResponse fromYaml(final String yamlText) {
        return responseFor(yamlLoader.loadDefinition(yamlText));
    }

    public UiHttpResponse previewDraft(final String jsonText) {
        return responseFor(draftJson.fromJson(jsonText));
    }

    private UiHttpResponse responseFor(final ThingifierModelDefinition definition) {
        SchemaDefinitionValidationReport report = assembler.validate(definition);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("valid", report.isValid());
        body.put("errors", errorMaps(report));
        body.put("draft", draftJson.toMap(definition));
        body.put("yaml", report.isValid() ? yamlExporter.export(definition) : "");
        body.put("mermaid", diagramExporter.mermaid(definition));
        body.put("graphviz", diagramExporter.graphviz(definition));
        return UiHttpResponse.json(200, JsonSupport.toJson(body));
    }

    private List<Map<String, Object>> errorMaps(final SchemaDefinitionValidationReport report) {
        List<Map<String, Object>> errors = new ArrayList<>();
        for (SchemaDefinitionValidationReport.SchemaDefinitionValidationError error :
                report.errors()) {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("path", error.path());
            value.put("message", error.message());
            errors.add(value);
        }
        return errors;
    }
}
