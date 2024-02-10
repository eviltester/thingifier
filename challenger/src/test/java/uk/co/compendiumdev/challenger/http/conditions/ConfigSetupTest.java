package uk.co.compendiumdev.challenger.http.conditions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.sparkstart.Environment;

import java.util.HashMap;
import java.util.Map;

public class ConfigSetupTest {

    @Test
    public void checkMaxMessageSizeEnforced(){

        // create a challenger
        final HttpMessageSender http = new HttpMessageSender(Environment.getBaseUri());

        final HttpResponseDetails response = http.send("/challenger", "POST");
        Assertions.assertEquals(201, response.statusCode);

        String challenger = response.getHeader("X-CHALLENGER");

        Map<String, String> headers = new HashMap<>();
        headers.put("X-CHALLENGER", challenger);

        final HttpResponseDetails response413 = http.send("/todos/1", "POST", headers, stringOfLength(5001));

        Assertions.assertEquals(413, response413.statusCode);
    }

    private String stringOfLength(int length) {
        StringBuilder str = new StringBuilder();
        for (int currLen = 0; currLen < length; currLen++) {
            str.append('a');
        }
        return str.toString();
    }
}
