package uk.co.compendiumdev.thingifier;

import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;
import org.junit.Test;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class ThingDefinitionTest {

    @Test
    public void entityDefinitionCreation(){
        ThingDefinition eDefn;
        eDefn = ThingDefinition.create("Requirement", "Requirements");

        assertThat(eDefn.getName(), is("Requirement"));
        assertThat(eDefn.getPlural(), is("Requirements"));


        assertThat(eDefn.getFieldNames().size(), is(1)); // guid exists
        assertThat(eDefn.getFieldNames(), contains("guid"));

        eDefn.setName("aRequirement");
        eDefn.setPlural("theRequirements");
        eDefn.defineField("Title");

        assertThat(eDefn.getName(), is("aRequirement"));
        assertThat(eDefn.getPlural(), is("theRequirements"));
        assertThat(eDefn.getFieldNames().size(), is(2));

        assertThat(eDefn.getFieldNames(), hasItem("Title"));

        assertThat(eDefn.hasFieldNameDefined("Title"), is(true));
        assertThat(eDefn.hasFieldNameDefined("Description"), is(false));
    }



}
