package uk.co.compendiumdev.thingifier.adapter.internalhttp.conversion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpMethod;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;

class InternalHttpRequestToHttpApiRequestTest {

    @Test
    void mapsInternalHttpRequestToLegacyApiRequestShape() {
        Map<String, List<String>> queryParams = new LinkedHashMap<>();
        queryParams.put("p", List.of("1", "2"));
        queryParams.put("empty", List.of());

        InternalHttpRequest internalRequest =
                new InternalHttpRequest("/todos")
                        .setVerb(InternalHttpMethod.POST)
                        .setBody("{\"title\":\"x\"}")
                        .setUrl("http://localhost/todos")
                        .setIP("127.0.0.1")
                        .setRawQueryString("id>=1&id<=4&p=1&p=2")
                        .setQueryParams(queryParams)
                        .setUrlParams(Map.of(":id", "123"))
                        .addHeader("Accept", "APPLICATION/JSON")
                        .addHeader("X-Multi", "one")
                        .addHeader("X-Multi", "two");

        HttpApiRequest apiRequest = InternalHttpRequestToHttpApiRequest.convert(internalRequest);

        Assertions.assertEquals("todos", apiRequest.getPath());
        Assertions.assertEquals(HttpApiRequest.VERB.POST, apiRequest.getVerb());
        Assertions.assertEquals("{\"title\":\"x\"}", apiRequest.getBody());
        Assertions.assertEquals("http://localhost/todos", apiRequest.getUrl());
        Assertions.assertEquals("127.0.0.1", apiRequest.getIP());
        Assertions.assertEquals("123", apiRequest.getUrlParam(":id"));
        Assertions.assertEquals("application/json", apiRequest.getHeader("Accept"));
        Assertions.assertEquals(3, apiRequest.getHeadersList().size());
        Assertions.assertEquals("1", apiRequest.getQueryParams().get("p"));
        Assertions.assertEquals("", apiRequest.getQueryParams().get("empty"));
        Assertions.assertEquals("1", apiRequest.rawQueryParamsValue("p"));
        Assertions.assertEquals(4, apiRequest.getFilterableQueryParams().size());
        Assertions.assertEquals("id", apiRequest.getFilterableQueryParams().get(0).fieldName);
        Assertions.assertEquals(">=", apiRequest.getFilterableQueryParams().get(0).filterOperation);
        Assertions.assertEquals("1", apiRequest.getFilterableQueryParams().get(0).fieldValue);
    }
}
