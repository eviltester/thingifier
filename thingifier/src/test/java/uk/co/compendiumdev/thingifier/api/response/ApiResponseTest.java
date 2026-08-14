package uk.co.compendiumdev.thingifier.api.response;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public class ApiResponseTest {

    @Test
    public void response404HasSingleErrorMessage() {

        ApiResponse response = ApiResponse.error404("oops");

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(true, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());

        Assertions.assertEquals(1, response.getErrorMessages().size());
        Assertions.assertTrue(response.getErrorMessages().contains("oops"));
    }

    @Test
    public void responseError() {

        ApiResponse response = ApiResponse.error(500, "oopsy");

        Assertions.assertEquals(500, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(true, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());

        Assertions.assertEquals(1, response.getErrorMessages().size());
        Assertions.assertTrue(response.getErrorMessages().contains("oopsy"));
    }

    @ParameterizedTest
    @MethodSource("acceptQualityValueErrorFormatExamples")
    public void errorFormatterUsesAcceptQualityValuesForJsonAndXml(
            final String acceptHeader, final String expectedPrefix) {
        Assertions.assertTrue(
                ApiResponseError.asAppropriate(acceptHeader, "oopsy").startsWith(expectedPrefix));
    }

    @Test
    public void errorFormatterUsesTextXmlAcceptType() {
        Assertions.assertTrue(ApiResponseError.asAppropriate("text/xml", "oopsy").startsWith("<"));
    }

    @ParameterizedTest
    @MethodSource("genericErrorAcceptHeadersThatFallBackToJson")
    public void genericErrorFormatterUsesJsonWhenAcceptHeaderIsUnsupportedOrNeedsEntityContext(
            final String acceptHeader) {
        Assertions.assertTrue(
                ApiResponseError.asAppropriate(acceptHeader, "oopsy").startsWith("{"));
    }

    @Test
    public void responseErrors() {

        List<String> errors = new ArrayList();
        errors.add("oopsy");
        errors.add("doopsy");
        errors.add("do");

        ApiResponse response = ApiResponse.error(501, errors);

        Assertions.assertEquals(501, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(true, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());

        Assertions.assertEquals(3, response.getErrorMessages().size());
        Assertions.assertTrue(response.getErrorMessages().contains("oopsy"));
        Assertions.assertTrue(response.getErrorMessages().contains("doopsy"));
        Assertions.assertTrue(response.getErrorMessages().contains("do"));
    }

    @Test
    public void response200() {

        ApiResponse response = ApiResponse.success();

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(false, response.hasABody());
        Assertions.assertEquals(false, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());
    }

    @Test
    public void response200WithInstance() {

        Thingifier thingifier = new Thingifier();
        EntityDefinition todo = thingifier.defineThing("todo", "todos");
        todo.addFields(Field.is("title", STRING));
        EntityDefinition todos =
                thingifier.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");

        EntityInstance aTodo =
                thingifier
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(EntityInstanceDraft.forEntity(todos).withField("title", "a todo"));

        ApiResponse response = ApiResponse.success().returnSingleInstance(aTodo);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(false, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());

        Assertions.assertEquals(aTodo, response.getReturnedInstance());
    }

    @Test
    public void response200WithInstances() {

        Thingifier thingifier = new Thingifier();
        EntityDefinition todo = thingifier.defineThing("todo", "todos");
        todo.addFields(Field.is("title", STRING));
        EntityDefinition todos =
                thingifier.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");

        EntityInstance aTodo =
                thingifier
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(EntityInstanceDraft.forEntity(todos).withField("title", "a todo"));
        EntityInstance anotherTodo =
                thingifier
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(todos)
                                        .withField("title", "another todo"));

        ApiResponse response =
                ApiResponse.success()
                        .returnInstanceCollection(
                                new ArrayList(
                                        thingifier
                                                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                                                .entityQueries()
                                                .list(todos)));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(false, response.isErrorResponse());
        Assertions.assertEquals(true, response.isCollection());

        Assertions.assertEquals(2, response.getReturnedInstanceCollection().size());
        Assertions.assertTrue(response.getReturnedInstanceCollection().contains(aTodo));
        Assertions.assertTrue(response.getReturnedInstanceCollection().contains(anotherTodo));
    }

    @Test
    public void response200WithEmptyInstances() {

        ApiResponse response = ApiResponse.success().returnInstanceCollection(new ArrayList());

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(false, response.isErrorResponse());
        Assertions.assertEquals(true, response.isCollection());

        Assertions.assertEquals(0, response.getReturnedInstanceCollection().size());
    }

    private static Stream<Arguments> acceptQualityValueErrorFormatExamples() {
        return Stream.of(
                Arguments.of("application/json;q=0, application/xml", "<"),
                Arguments.of("application/xml;q=0.1, application/json;q=0.9", "{"));
    }

    private static Stream<String> genericErrorAcceptHeadersThatFallBackToJson() {
        return Stream.of(
                "application/problem+json",
                "application/vnd.example.todo+xml",
                "application/*+xml",
                "application/problem+xml");
    }
}
