package uk.co.compendiumdev.thingifier.application.internalhttp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InternalHttpRequestTest {

    @Test
    void headersAreCaseInsensitiveAndRawHeadersAreRetained() {
        InternalHttpRequest request =
                new InternalHttpRequest("/todos")
                        .addHeader("Accept", "application/json")
                        .addHeader("ACCEPT", "application/xml");

        Assertions.assertTrue(request.hasHeader("accept"));
        Assertions.assertEquals("application/xml", request.getHeader("Accept"));
        Assertions.assertEquals(2, request.getRawHeaders().size());
        Assertions.assertEquals("Accept", request.getRawHeaders().get(0).key());
        Assertions.assertEquals("application/json", request.getRawHeaders().get(0).value());
    }

    @Test
    void queryParamsAreCopiedInAndOut() {
        List<String> values = new ArrayList<>(List.of("1", "2"));
        Map<String, List<String>> params = new LinkedHashMap<>();
        params.put("p", values);

        InternalHttpRequest request = new InternalHttpRequest("/todos").setQueryParams(params);
        values.add("3");

        Assertions.assertEquals(2, request.getQueryParams().get("p").size());

        Map<String, List<String>> returnedParams = request.getQueryParams();
        returnedParams.get("p").add("4");

        Assertions.assertEquals(2, request.getQueryParams().get("p").size());
        Assertions.assertEquals("1", request.firstQueryParamValuesAsMap().get("p"));
    }
}
