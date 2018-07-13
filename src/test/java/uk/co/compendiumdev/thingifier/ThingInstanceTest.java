package uk.co.compendiumdev.thingifier;

import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ThingInstanceTest {

    ThingDefinition entityTestSession;

    @Before
    public void createEntity(){

        entityTestSession = ThingDefinition.create("Test Session", "Test Sessions");
        entityTestSession.defineField("Title");
        entityTestSession.addFields(Field.is("CompletedStatus").withDefaultValue("Not Completed"));
        entityTestSession.addFields(Field.is("review", FieldType.BOOLEAN).withDefaultValue("TRUE"));
        entityTestSession.addFields(Field.is("falsey", FieldType.BOOLEAN));
    }

    @Test
    public void canCreateNewEntityInstance() {



        ThingInstance session;
        session = new ThingInstance(entityTestSession);

        assertThat(session.getFieldNames().size(),is(4+1)); // +1 for guid

        session.setValue("Title", "My Test Session");
        assertThat(session.getValue("Title"), is("My Test Session"));

        assertThat(session.getEntity().getName(), is("Test Session"));
    }


    @Test
    public void defaultValuesAreReturnedByGetValue(){

        ThingInstance session = new ThingInstance(entityTestSession);
        Assert.assertEquals("Not Completed", session.getValue("CompletedStatus"));
    }


    @Test
    public void booleanFieldsCanOnlyBeSetAsTrueOrFalse(){

        ThingInstance session = new ThingInstance(entityTestSession);

        // false by default
        Assert.assertEquals("TRUE", session.getValue("review"));

        session.setValue("review", "FALSE");
        Assert.assertEquals("FALSE", session.getValue("review"));

        session.setValue("review", "TRUE");
        Assert.assertEquals("TRUE", session.getValue("review"));


        try {
            session.setValue("review", "BOB");

            Assert.fail("An Exception should have been thrown");
        }catch(Exception e){
            // unchanged from default
            Assert.assertEquals("TRUE", session.getValue("review"));
        }

    }


    @Test
    public void booleanFieldsByDefaultAreFalse(){

        ThingInstance session = new ThingInstance(entityTestSession);

        // false by default
        Assert.assertEquals("FALSE", session.getValue("falsey"));
    }

    @Test
    public void canGetCreatedUniqueGUIDs(){
        ThingInstance session = new ThingInstance(entityTestSession);

        Assert.assertNotNull(session.getGUID());


        ThingInstance session2 = new ThingInstance(entityTestSession);
        Assert.assertNotNull(session2.getGUID());
        Assert.assertTrue(session2.getGUID().length()>10);

        Assert.assertNotEquals(session.getGUID(), session2.getGUID());
    }



    @Test
    public void fieldNameAccessIsCaseInsensitive(){
        ThingInstance session = new ThingInstance(entityTestSession);

        Assert.assertEquals("Not Completed", session.getValue("CompletedStatus"));
        Assert.assertEquals("Not Completed", session.getValue("CoMpletedStatus"));
        Assert.assertEquals("Not Completed", session.getValue("CompletedSTATUS"));
        Assert.assertEquals("Not Completed", session.getValue("completedstatus"));

    }

    @Test
    public void canCreateAThingWithAGUID(){

        // note potential bug this is risky if the GUID is later created
        ThingInstance session = new ThingInstance(entityTestSession, "1234-1234-1324-1234");
        Assert.assertEquals("1234-1234-1324-1234", session.getGUID());



    }
}
