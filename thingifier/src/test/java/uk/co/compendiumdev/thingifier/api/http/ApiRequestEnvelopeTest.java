package uk.co.compendiumdev.thingifier.api.http;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ApiRequestEnvelopeTest {

    @Test
    public void getEnvelopeCarriesRouteHeadersAndQueryWithoutBodyParsing() {
        HttpApiRequest request =
                new HttpApiRequest("/tasks")
                        .addHeader("Accept", "application/json")
                        .setFilterableQueryParams("title=Task");

        ApiRequestEnvelope envelope =
                ApiRequestEnvelope.from(
                        request, ThingifierHttpApi.HttpVerb.GET, List.of("task", "tasks"));

        Assertions.assertEquals(ThingifierHttpApi.HttpVerb.GET, envelope.verb());
        Assertions.assertEquals("tasks", envelope.path());
        Assertions.assertEquals("application/json", envelope.headers().get("Accept"));
        Assertions.assertEquals(1, envelope.queryParams().size());
        Assertions.assertTrue(envelope.bodyFields().asMap().isEmpty());
    }

    @Test
    public void postEnvelopeParsesBodyIntoTypedBodyFieldsOnceAtTheEdge() {
        HttpApiRequest request =
                new HttpApiRequest("/tasks")
                        .setBody(
                                "{\"title\":\"Task\",\"done\":false,\"relationships\":{\"project\":{\"guid\":\"p1\"}}}");

        ApiRequestEnvelope envelope =
                ApiRequestEnvelope.from(
                        request, ThingifierHttpApi.HttpVerb.POST, List.of("task", "tasks"));

        Assertions.assertEquals("Task", envelope.bodyFields().asMap().get("title"));
        Assertions.assertEquals("false", envelope.bodyFields().asStringMap().get("done"));
        Assertions.assertTrue(
                envelope.bodyFields().asFlattenedStringMap().stream()
                        .anyMatch(
                                entry ->
                                        "relationships.project.guid".equals(entry.getKey())
                                                && "p1".equals(entry.getValue())));
    }
}
