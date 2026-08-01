package uk.co.compendiumdev.thingifier.apiconfig;

import static uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle.PARTIAL_JSON_UPDATE;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation.CREATE;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation.UPDATE;
import static uk.co.compendiumdev.thingifier.apiconfig.PutIdentifierPolicy.DISALLOWED;
import static uk.co.compendiumdev.thingifier.apiconfig.PutIdentifierPolicy.MANDATORY;
import static uk.co.compendiumdev.thingifier.apiconfig.PutIdentifierPolicy.OPTIONAL;
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
        Assertions.assertEquals(MANDATORY, config.entities().putIdentifierInUri());
        Assertions.assertEquals(OPTIONAL, config.entities().putIdentifierInPayload());
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
        source.writeMethods().entities().putIdentifierInUri(OPTIONAL);
        source.writeMethods().entities().putIdentifierInPayload(MANDATORY);
        source.writeMethods().relationships().postCan(CONNECT_EXISTING);

        ThingifierApiConfig target = new ThingifierApiConfig("");
        target.setFrom(source);

        Assertions.assertEquals(Set.of(CREATE), target.writeMethods().entities().postOperations());
        Assertions.assertEquals(
                Set.of(PARTIAL_JSON_UPDATE), target.writeMethods().entities().patchUpdateStyles());
        Assertions.assertEquals(OPTIONAL, target.writeMethods().entities().putIdentifierInUri());
        Assertions.assertEquals(
                MANDATORY, target.writeMethods().entities().putIdentifierInPayload());
        Assertions.assertEquals(
                Set.of(CONNECT_EXISTING), target.writeMethods().relationships().postOperations());
    }

    @Test
    public void validationWarnsWhenPutHasNoAllowedIdentifierLocation() {
        ThingifierApiConfig config = new ThingifierApiConfig("");
        config.writeMethods().entities().putIdentifierInUri(DISALLOWED);
        config.writeMethods().entities().putIdentifierInPayload(DISALLOWED);

        ApiConfigValidationReport report = config.validate();

        Assertions.assertTrue(report.isValid());
        Assertions.assertTrue(report.hasWarnings());
        Assertions.assertEquals(
                "writeMethods.entities.put: PUT is enabled but identifiers are disallowed "
                        + "in both URI and payload",
                report.warningMessages().get(0));
    }

    @Test
    public void validationDoesNotWarnWhenPutIsNotSupported() {
        WriteMethodsConfig config = new WriteMethodsConfig();
        config.entities().putCan();
        config.entities().putIdentifierInUri(DISALLOWED);
        config.entities().putIdentifierInPayload(DISALLOWED);

        ApiConfigValidationReport report = config.validate();

        Assertions.assertTrue(report.isValid());
        Assertions.assertFalse(report.hasWarnings());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void clearerThingifierAliasesExposeSameConfigurationObjects() {
        Thingifier thingifier = new Thingifier();

        Assertions.assertSame(thingifier.apiDefaults(), thingifier.apiConfig());
        Assertions.assertSame(thingifier.apiContract(), thingifier.apiSpec());
    }
}
