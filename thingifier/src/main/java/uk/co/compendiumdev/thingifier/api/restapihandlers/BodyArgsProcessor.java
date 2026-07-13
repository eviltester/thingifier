package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

// todo: potentially move this into the BodyParser
public class BodyArgsProcessor {

    private final Thingifier thingifier;
    private final BodyParser bodyargs;

    public BodyArgsProcessor(Thingifier thingifier, BodyParser bodyargs) {
        this.thingifier = thingifier;
        this.bodyargs = bodyargs;
    }

    public List<Map.Entry<String, String>> removeRelationshipsFrom(
            final EntityInstance instance, final String database) {
        return removeRelationshipsFrom(instance.getEntity(), database);
    }

    public List<Map.Entry<String, String>> removeRelationshipsFrom(
            final EntityDefinition entity, final String database) {

        List<Map.Entry<String, String>> fullargs = bodyargs.getFlattenedStringMap();

        for (Map.Entry<String, String> removeMe :
                new RelationshipBodyCommandParser(thingifier)
                        .parse(fullargs, entity, database)
                        .relationshipEntries()) {
            fullargs.remove(removeMe);
        }

        return fullargs;
    }
}
