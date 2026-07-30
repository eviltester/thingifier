package uk.co.compendiumdev.thingifier.application.examples;

import static uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation.CREATE;
import static uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation.UPDATE;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.VRule;

public class ConfiguredWriteMethodsThingifier {

    public Thingifier get() {
        Thingifier notes = new Thingifier();
        notes.setDocumentation(
                "Configured Write Methods",
                "A sample API configured so POST creates, PATCH updates, and PUT updates.");

        EntityDefinition note = notes.defineThing("note", "notes");
        note.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        note.addFields(
                Field.is("title", STRING).makeMandatory().withValidation(VRule.notEmpty()),
                Field.is("description", STRING));

        notes.apiDefaults().writeMethods().entities().postCan(CREATE);
        notes.apiDefaults().writeMethods().entities().patchCan(UPDATE);
        notes.apiDefaults().writeMethods().entities().putCan(UPDATE);

        return notes;
    }
}
