package uk.co.compendiumdev.challenger.restassured.http;

import java.net.URL;
import java.util.Map;

public interface CanSendHttpRequests {
    HttpResponseDetails send(URL url, String verb, Map<String, String> headers, String body);
    HttpRequestDetails getLastRequest();
    HttpResponseDetails getLastResponse();
}
