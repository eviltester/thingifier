package uk.co.compendiumdev.challenger.http.http;

//import sun.net.www.MessageHeader;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class HttpClientRequestSender implements CanSendHttpRequests {

    private final HttpClient client;
    // https://stackoverflow.com/questions/1432961/how-do-i-make-httpurlconnection-use-a-proxy
    // Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.1", 8080));
    // conn = new URL(urlString).openConnection(proxy);
    Proxy proxy;
    private HttpRequestDetails lastRequest;
    private HttpResponseDetails lastResponse;

    public HttpClientRequestSender(String proxyHost, int proxyPort) {
        if(proxyHost!=null){
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            client = HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort))).build();
        }else{
            client = HttpClient.newBuilder().proxy(ProxySelector.getDefault()).build();
        }
    }

//    public static void setProxy(String ip, int port){
//        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
//    }

    public HttpRequestDetails getLastRequest(){
        return this.lastRequest;
    }

    public HttpResponseDetails getLastResponse(){
        return this.lastResponse;
    }

    public HttpResponseDetails send(URL url, String verb, Map<String, String> headers, String body) {

        HttpResponseDetails response = new HttpResponseDetails();

        try {


            lastRequest = new HttpRequestDetails();

            final HttpRequest.Builder request = HttpRequest.newBuilder().
                    uri(url.toURI()).
                    method(verb, HttpRequest.BodyPublishers.ofString(body));

            // SET HEADERS
            for(Map.Entry<String, String> header : headers.entrySet()){
                request.header(header.getKey(), header.getValue());
                System.out.println("Header - " + header.getKey() + " : " +  headers.get(header.getValue()) );
            }

            System.out.println("\nSending '" + verb +"' request to URL : " + url);

            final HttpRequest actualRequest = request.build();

            lastRequest.body = body;
            for(Map.Entry<String, List<String>> actualHeader :
                    actualRequest.headers().map().entrySet()){
                lastRequest.addHeader(actualHeader.getKey(), actualHeader.getValue().get(0));
                System.out.println(String.format("Request Header - %s:%s", actualHeader.getKey(), actualHeader.getValue().get(0)));
            }

            final HttpResponse<String> actualResponse = client.send(actualRequest, HttpResponse.BodyHandlers.ofString());

            response.statusCode = actualResponse.statusCode();

            System.out.println("Response Code : " + response.statusCode);

            response.body = actualResponse.body();

            //print result
            System.out.println("Response Body: " + response.body);


            // add the headers
            Map<String, String> responseHeaders = new HashMap<>();
            for(Map.Entry<String, List<String>> header : actualResponse.headers().map().entrySet()){
                String headerValue =  header.getValue().get(0);
                responseHeaders.put(header.getKey(),headerValue);
                System.out.println("Header: " + header.getKey() + " - " + headerValue);
            }
            response.setHeaders(responseHeaders);



            lastResponse = response;




        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return response;
    }
}
