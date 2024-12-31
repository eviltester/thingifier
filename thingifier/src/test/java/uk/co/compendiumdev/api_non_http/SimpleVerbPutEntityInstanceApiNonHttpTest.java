package uk.co.compendiumdev.api_non_http;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SimpleVerbPutEntityInstanceApiNonHttpTest {



    private Thingifier thingifierWithAutoFields() {

        Thingifier thingifier = new Thingifier();
        thingifier.defineThing("entity", "entities");

        EntityDefinition entityDefn = thingifier.getERmodel().getEntityDefinitionNamed("entity");
        entityDefn.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        entityDefn.addField(Field.is("title", FieldType.STRING).makeMandatory());
        entityDefn.addField(Field.is("description", FieldType.STRING));

        return thingifier;


    }
    
       /*


    Non HTTP API Based Tests


    */

    private BodyParser getSimpleParser(final Map requestBody, Thingifier thingifier) {

        final HttpApiRequest arequest = new HttpApiRequest("/path").setBody(new Gson().toJson(requestBody));
        return new BodyParser(arequest, thingifier.getThingNames());
    }


    @Test
    public void putCanAmendExistingInstance() {

        Map requestBody;
        ApiResponse apiresponse;

        Thingifier thingifier = thingifierWithAutoFields();
        EntityInstanceCollection instances = thingifier.getThingInstancesNamed("entity", EntityRelModel.DEFAULT_DATABASE_NAME);

        // PUT

        requestBody = new HashMap<String, String>();
        requestBody.put("title", "My Office Work");

        EntityInstance officeWork = instances.addInstance(new EntityInstance(instances.definition())).
                setValue("title", "An Existing instances");

        String officeWorkGuid = officeWork.getPrimaryKeyValue();
        Assertions.assertNotNull(officeWorkGuid);

        // amend existing instances with PUT - this should validate that all required fields are present
        apiresponse = thingifier.api().put(String.format("entities/%s", officeWork.getPrimaryKeyValue()),  getSimpleParser(requestBody, thingifier), new HttpHeadersBlock());
        Assertions.assertEquals(200, apiresponse.getStatusCode());
        Assertions.assertEquals("My Office Work", officeWork.getFieldValue("title").asString());

        officeWork.setValue("title", "office");
        Assertions.assertEquals("office", officeWork.getFieldValue("title").asString());
        Assertions.assertNotNull(officeWorkGuid);

        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertFalse(apiresponse.isCollection());
        Assertions.assertEquals(officeWorkGuid, apiresponse.getReturnedInstance().getPrimaryKeyValue());

    }

    @Test
    public void putCanAmendExistingInstancesWhenOptionalFieldsAreMissing() {

        Map requestBody;
        ApiResponse apiresponse;

        Thingifier thingifier = thingifierWithAutoFields();
        EntityInstanceCollection instances = thingifier.getThingInstancesNamed("entity", EntityRelModel.DEFAULT_DATABASE_NAME);

        // create something to amend with PUT
        EntityInstance officeWork = instances.addInstance(new EntityInstance(instances.definition())).
                setValue("title", "An Existing instances").
                setValue("description", "Existing Description");

        String officeWorkGuid = officeWork.getPrimaryKeyValue();
        Assertions.assertNotNull(officeWorkGuid);

        // amend existing instances with PUT - this should validate that all required fields are present

        requestBody = new HashMap<String, String>();
        requestBody.put("title", "My Office Work");
        // note, I haven't added a description - description is optional

        apiresponse = thingifier.api().put(String.format("entities/%s", officeWork.getPrimaryKeyValue()),  getSimpleParser(requestBody, thingifier), new HttpHeadersBlock());
        Assertions.assertEquals(200, apiresponse.getStatusCode());
        Assertions.assertEquals("My Office Work",officeWork.getFieldValue("title").asString());
        Assertions.assertEquals("",officeWork.getFieldValue("description").asString());
    }

    @Test
    public void putFailCanNotAmendExistingInstancesWhenMandatoryFieldsAreMissing() {

        Map requestBody;
        ApiResponse apiresponse;

        Thingifier thingifier = thingifierWithAutoFields();
        EntityInstanceCollection instances = thingifier.getThingInstancesNamed("entity", EntityRelModel.DEFAULT_DATABASE_NAME);

        // create something to amend with PUT
        EntityInstance officeWork = instances.addInstance(new EntityInstance(instances.definition())).
                setValue("description", "An Existing instance title");

        String officeWorkGuid = officeWork.getPrimaryKeyValue();
        Assertions.assertNotNull(officeWorkGuid);

        // amend existing instances with PUT - this should validate that all required fields are present

        requestBody = new HashMap<String, String>();
        requestBody.put("description", "My Office Work");
        // note, I haven't added a title - title is mandatory

        apiresponse = thingifier.api().put(String.format("entities/%s", officeWork.getPrimaryKeyValue()),  getSimpleParser(requestBody, thingifier), new HttpHeadersBlock());
        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().contains("title : field is mandatory"));
        Assertions.assertEquals("An Existing instance title",officeWork.getFieldValue("description").asString());
    }


    @Test
    public void putFailCanNotAmendPrimaryKey() {

        Map requestBody;
        ApiResponse apiresponse;

        Thingifier thingifier = thingifierWithAutoFields();
        EntityInstanceCollection instances = thingifier.getThingInstancesNamed("entity", EntityRelModel.DEFAULT_DATABASE_NAME);

        // PUT


        EntityInstance officeWork = instances.addInstance(new EntityInstance(instances.definition())).
                setValue("title", "An Existing instances").
                setValue("description", "my original description");

        String originalID = officeWork.getPrimaryKeyValue();
        Assertions.assertNotNull(originalID);

        // amend existing instances with PUT - this should validate that all required fields are present

        requestBody = new HashMap<String, String>();
        requestBody.put("title", "My Office Work");
        String newId = "22";
        requestBody.put("id", newId);



        apiresponse = thingifier.api().put(String.format("entities/%s", originalID),  getSimpleParser(requestBody, thingifier), new HttpHeadersBlock());

        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().contains("Can not amend id from 1 to 22"));
        Assertions.assertEquals("An Existing instances", officeWork.getFieldValue("title").asString());
        Assertions.assertEquals("my original description", officeWork.getFieldValue("description").asString());
        Assertions.assertEquals(originalID, officeWork.getFieldValue("id").asString());

        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertTrue(apiresponse.isErrorResponse());
    }


    @Test
    public void putFailCanNotCreateAnEntityInstanceWithAutoFields() {

        Map requestBody;
        ApiResponse apiresponse;

        Thingifier thingifier = thingifierWithAutoFields();
        EntityInstanceCollection instances = thingifier.getThingInstancesNamed("entity", EntityRelModel.DEFAULT_DATABASE_NAME);

        // PUT

        requestBody = new HashMap<String, String>();
        String title = "My Office Work " + System.currentTimeMillis();
        requestBody.put("title", title);


        int currentinstances = instances.countInstances();
        Assertions.assertEquals(0, currentinstances);

        // create with a PUT and a given ID
        String id = "200";

        apiresponse = thingifier.api().put(String.format("entities/%s", id),  getSimpleParser(requestBody, thingifier), new HttpHeadersBlock());
        Assertions.assertEquals(400, apiresponse.getStatusCode());


        Assertions.assertEquals(currentinstances, instances.countInstances());


        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertFalse(apiresponse.isCollection());
        Assertions.assertTrue(apiresponse.getErrorMessages().size() > 0);

        Assertions.assertTrue(apiresponse.getErrorMessages().contains("Cannot create entity with PUT due to Auto fields id"));
    }


    @Test
    public void putFailCanNotAmendAutoGuid() {

        Map requestBody;
        ApiResponse apiresponse;

        Thingifier myThingifier = new Thingifier();
        myThingifier.defineThing("entity", "entities");

        EntityDefinition entityDefn = myThingifier.getERmodel().getEntityDefinitionNamed("entity");
        entityDefn.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        entityDefn.addField(Field.is("title", FieldType.STRING).makeMandatory());
        entityDefn.addField(Field.is("guid", FieldType.AUTO_GUID));

        EntityInstanceCollection myInstances = myThingifier.getThingInstancesNamed("entity", EntityRelModel.DEFAULT_DATABASE_NAME);

        // PUT


        EntityInstance officeWork = myInstances.addInstance(new EntityInstance(myInstances.definition())).
                setValue("title", "An Existing instance");

        String originalID = officeWork.getPrimaryKeyValue();
        String originalGuid = officeWork.getFieldValue("guid").asString();
        Assertions.assertNotNull(originalID);

        // amend existing instances with PUT - this should validate that all required fields are present

        requestBody = new HashMap<String, String>();
        requestBody.put("title", "My Office Work");
        String newGuid = UUID.randomUUID().toString();
        requestBody.put("guid", newGuid);



        apiresponse = myThingifier.api().put(String.format("entities/%s", originalID),  getSimpleParser(requestBody, myThingifier), new HttpHeadersBlock());

        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertTrue(apiresponse.getErrorMessages().contains(String.format("Can not amend guid from %s to %s", originalGuid, newGuid)));

        // no change to instance
        Assertions.assertEquals("An Existing instance", officeWork.getFieldValue("title").asString());
        Assertions.assertEquals(originalID, officeWork.getFieldValue("id").asString());
        Assertions.assertEquals(originalGuid, officeWork.getFieldValue("guid").asString());

        Assertions.assertTrue(apiresponse.hasABody());
        Assertions.assertTrue(apiresponse.isErrorResponse());
    }

    @Test
    public void putCanCreateAnInstanceWhenNoAutoFields() {

        Map requestBody;
        ApiResponse apiresponse;

        Thingifier myThingifier = new Thingifier();
        myThingifier.defineThing("entity", "entities");

        EntityDefinition entityDefn = myThingifier.getERmodel().getEntityDefinitionNamed("entity");
        entityDefn.addAsPrimaryKeyField(Field.is("id", FieldType.STRING));
        entityDefn.addField(Field.is("title", FieldType.STRING).makeMandatory());

        EntityInstanceCollection myInstances = myThingifier.getThingInstancesNamed("entity", EntityRelModel.DEFAULT_DATABASE_NAME);

        // PUT

        EntityInstance officeWork = new EntityInstance(entityDefn).setValue("id", "one").
                setValue("title", "An Existing instance");

        myInstances.addInstance(officeWork);

        String originalID = officeWork.getPrimaryKeyValue();
        String originalGuid = officeWork.getFieldValue("id").asString();
        Assertions.assertNotNull(originalID);

        // amend existing instances with PUT - this should validate that all required fields are present

        requestBody = new HashMap<String, String>();
        requestBody.put("title", "My Office Work");
        String newGuid = UUID.randomUUID().toString();
        requestBody.put("id", newGuid);



        apiresponse = myThingifier.api().put(String.format("entities/%s", newGuid),  getSimpleParser(requestBody, myThingifier), new HttpHeadersBlock());

        Assertions.assertEquals(201, apiresponse.getStatusCode());

        // no change to original instance
        Assertions.assertEquals("An Existing instance", officeWork.getFieldValue("title").asString());
        Assertions.assertEquals(originalID, officeWork.getFieldValue("id").asString());

        EntityInstance newInstance = myInstances.findInstanceByPrimaryKey(newGuid);
        Assertions.assertEquals("My Office Work", newInstance.getFieldValue("title").asString());
        Assertions.assertEquals(newGuid, newInstance.getFieldValue("id").asString());
    }

    @Test
    public void putCanCreateAnInstanceWhenNoAutoFieldsAndMissingPrimaryKeyFromBody() {

        Map requestBody;
        ApiResponse apiresponse;

        Thingifier myThingifier = new Thingifier();
        myThingifier.defineThing("entity", "entities");

        EntityDefinition entityDefn = myThingifier.getERmodel().getEntityDefinitionNamed("entity");
        entityDefn.addAsPrimaryKeyField(Field.is("id", FieldType.STRING));
        entityDefn.addField(Field.is("title", FieldType.STRING).makeMandatory());

        EntityInstanceCollection myInstances = myThingifier.getThingInstancesNamed("entity", EntityRelModel.DEFAULT_DATABASE_NAME);

        // PUT

        EntityInstance officeWork = new EntityInstance(entityDefn).setValue("id", "one").
                setValue("title", "An Existing instance");

        myInstances.addInstance(officeWork);

        String originalID = officeWork.getPrimaryKeyValue();
        String originalGuid = officeWork.getFieldValue("id").asString();
        Assertions.assertNotNull(originalID);

        // amend existing instances with PUT - this should validate that all required fields are present

        requestBody = new HashMap<String, String>();
        requestBody.put("title", "My Office Work");
        String newGuid = UUID.randomUUID().toString();
        // body does not contain id, only the url does
        //requestBody.put("id", newGuid);



        apiresponse = myThingifier.api().put(String.format("entities/%s", newGuid),  getSimpleParser(requestBody, myThingifier), new HttpHeadersBlock());

        Assertions.assertEquals(201, apiresponse.getStatusCode());

        // no change to original instance
        Assertions.assertEquals("An Existing instance", officeWork.getFieldValue("title").asString());
        Assertions.assertEquals(originalID, officeWork.getFieldValue("id").asString());

        EntityInstance newInstance = myInstances.findInstanceByPrimaryKey(newGuid);
        Assertions.assertEquals("My Office Work", newInstance.getFieldValue("title").asString());
        Assertions.assertEquals(newGuid, newInstance.getFieldValue("id").asString());
    }

    @Test
    public void putFailCanNotCreateAnInstanceWhenKeyDoesNotMatchBody() {

        Map requestBody;
        ApiResponse apiresponse;

        Thingifier myThingifier = new Thingifier();
        myThingifier.defineThing("entity", "entities");

        EntityDefinition entityDefn = myThingifier.getERmodel().getEntityDefinitionNamed("entity");
        entityDefn.addAsPrimaryKeyField(Field.is("id", FieldType.STRING));
        entityDefn.addField(Field.is("title", FieldType.STRING).makeMandatory());

        EntityInstanceCollection myInstances = myThingifier.getThingInstancesNamed("entity", EntityRelModel.DEFAULT_DATABASE_NAME);

        // PUT

        EntityInstance officeWork = new EntityInstance(entityDefn).setValue("id", "one").
                setValue("title", "An Existing instance");

        myInstances.addInstance(officeWork);

        String originalID = officeWork.getPrimaryKeyValue();
        String originalGuid = officeWork.getFieldValue("id").asString();
        Assertions.assertNotNull(originalID);

        // amend existing instances with PUT - this should validate that all required fields are present

        requestBody = new HashMap<String, String>();
        requestBody.put("title", "My Office Work");
        String newGuid = "newkey";
        requestBody.put("id", "innerkey");



        apiresponse = myThingifier.api().put(String.format("entities/%s", newGuid),  getSimpleParser(requestBody, myThingifier), new HttpHeadersBlock());

        Assertions.assertEquals(400, apiresponse.getStatusCode());
        Assertions.assertEquals("Cannot create entity with PUT as key does not match body value newkey != innerkey", apiresponse.getErrorMessages().toArray()[0]);

        // no change to original instance
        Assertions.assertEquals("An Existing instance", officeWork.getFieldValue("title").asString());
        Assertions.assertEquals(originalID, officeWork.getFieldValue("id").asString());

    }
}
