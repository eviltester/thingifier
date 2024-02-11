package uk.co.compendiumdev.thingifier.api.http.headers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HeadersTest {

    // move from a HashMap to an actual class for headers
    @Test
    public void canManageHeadersWithAClassRatherThanAHashMap(){
        HttpHeadersBlock headers = new HttpHeadersBlock();

        // put for easier backwards compatibility with hashmap
        headers.put("headername", "value");

        Assertions.assertEquals("value", headers.get("headername"));
    }
}
