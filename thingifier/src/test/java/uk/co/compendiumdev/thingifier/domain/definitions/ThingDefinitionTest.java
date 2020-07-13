package uk.co.compendiumdev.thingifier.domain.definitions;

import junit.framework.Assert;
import org.junit.jupiter.api.Test;

import java.io.File;

public class ThingDefinitionTest {

    @Test
    public void fieldsOrderingIsTheOrderWhenDefined(){

        ThingDefinition defn = ThingDefinition.create("thing", "things");

        // add 19 fields
        for(int x=1; x<20; x++) {
            defn.addField(Field.is("field"+x));
        }

        // guid added automatically - making 20 fields
        Assert.assertEquals(20, defn.getFieldNames().size());

        // check order
        int expectedFieldPostfix = 0;
        for(String fieldName: defn.getFieldNames()){
            if(expectedFieldPostfix==0){
                // first field is GUID
                Assert.assertEquals("guid", fieldName);
            }else{
                Assert.assertEquals("field" + expectedFieldPostfix, fieldName);
            }
            expectedFieldPostfix++;
        }
    }
}
