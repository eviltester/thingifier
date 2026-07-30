package uk.co.compendiumdev.thingifier.apiconfig;

import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.PARTIAL_JSON_UPDATE;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation.CREATE;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation.UPDATE;
import static uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation.CONNECT_EXISTING;
import static uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation.CREATE_AND_CONNECT;
import static uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation.DISCONNECT;

import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;

public class WriteMethodsConfigTest {

    @Test
    public void defaultsPreserveExistingWriteMethodBehavior() {
        WriteMethodsConfig config = new WriteMethodsConfig();

        Assertions.assertEquals(Set.of(CREATE, UPDATE), config.entities().postOperations());
        Assertions.assertEquals(Set.of(CREATE, UPDATE), config.entities().putOperations());
        Assertions.assertEquals(Set.of(), config.entities().patchUpdateStyles());
        Assertions.assertEquals(
                Set.of(CREATE_AND_CONNECT, CONNECT_EXISTING),
                config.relationships().postOperations());
        Assertions.assertEquals(Set.of(DISCONNECT), config.relationships().deleteOperations());
    }

    @Test
    public void emptyOperationSetMeansMethodIsNotSupported() {
        WriteMethodsConfig config = new WriteMethodsConfig();

        config.entities().postCan();
        config.relationships().deleteCan();

        Assertions.assertEquals(Set.of(), config.entities().postOperations());
        Assertions.assertEquals(Set.of(), config.relationships().deleteOperations());
    }

    @Test
    public void configCanBeCopiedBetweenProfiles() {
        ThingifierApiConfig source = new ThingifierApiConfig("");
        source.writeMethods().entities().postCan(CREATE);
        source.writeMethods().entities().patchCan(PARTIAL_JSON_UPDATE);
        source.writeMethods().relationships().postCan(CONNECT_EXISTING);

        ThingifierApiConfig target = new ThingifierApiConfig("");
        target.setFrom(source);

        Assertions.assertEquals(Set.of(CREATE), target.writeMethods().entities().postOperations());
        Assertions.assertEquals(
                Set.of(PARTIAL_JSON_UPDATE), target.writeMethods().entities().patchUpdateStyles());
        Assertions.assertEquals(
                Set.of(CONNECT_EXISTING), target.writeMethods().relationships().postOperations());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void clearerThingifierAliasesExposeSameConfigurationObjects() {
        Thingifier thingifier = new Thingifier();

        Assertions.assertSame(thingifier.apiDefaults(), thingifier.apiConfig());
        Assertions.assertSame(thingifier.apiContract(), thingifier.apiSpec());
    }
}
