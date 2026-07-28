package uk.co.compendiumdev.thingifier.htmlgui;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityViewDefinition;

public final class ThingifierGuiConfig {

    private final DataExplorerConfig dataExplorer;

    public ThingifierGuiConfig() {
        dataExplorer = new DataExplorerConfig();
    }

    public DataExplorerConfig dataExplorer() {
        return dataExplorer;
    }

    public static final class DataExplorerConfig {
        private final Map<String, String> responseViewNamesByEntity;

        private DataExplorerConfig() {
            responseViewNamesByEntity = new HashMap<>();
        }

        public DataExplorerConfig responseView(final String entityName, final String viewName) {
            final String normalizedEntityName = normalize(entityName);
            final String normalizedViewName = viewName == null ? "" : viewName.trim();
            if (normalizedEntityName.isEmpty()) {
                throw new IllegalArgumentException("Entity name is required");
            }
            if (normalizedViewName.isEmpty()) {
                throw new IllegalArgumentException("View name is required");
            }
            responseViewNamesByEntity.put(normalizedEntityName, normalizedViewName);
            return this;
        }

        public Optional<String> responseViewNameFor(final EntityDefinition definition) {
            if (definition == null) {
                return Optional.empty();
            }
            String viewName = responseViewNamesByEntity.get(normalize(definition.getName()));
            if (viewName == null) {
                viewName = responseViewNamesByEntity.get(normalize(definition.getPlural()));
            }
            return Optional.ofNullable(viewName);
        }

        public EntityViewDefinition responseViewFor(final EntityDefinition definition) {
            if (definition == null) {
                return null;
            }
            return responseViewNameFor(definition)
                    .filter(definition::hasViewNamed)
                    .map(definition::getViewNamed)
                    .orElse(null);
        }

        private static String normalize(final String value) {
            return value == null ? "" : value.trim().toLowerCase();
        }
    }
}
