# POST /challenger (201)

> Issue a POST request on the `/challenger` end point, with no body, to create a new challenger session. Use the generated X-CHALLENGER header in future requests to track challenge completion.

- This challenge is essential if you want to persist your sessions in multi-user mode
- This challenge is optional if you want to work in single-user mode

## CURL

~~~~~~~~
curl --request POST \
  --url http://localhost:4567/challenger \
  -v
~~~~~~~~

~~~~~~~~
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 4567 (#0)
> POST /challenger HTTP/1.1
> Host: localhost:4567
> User-Agent: curl/7.64.1
> Accept: */*
> 
< HTTP/1.1 201 Created
< Date: Tue, 28 Jul 2020 15:11:56 GMT
< X-CHALLENGER: rest-api-challenges-single-player
< Location: /gui/challenges
< Content-Type: text/html;charset=utf-8
< Transfer-Encoding: chunked
< Server: Jetty(9.4.z-SNAPSHOT)
< 
* Connection #0 to host localhost left intact
* Closing connection 0
~~~~~~~~

## Insomnia

~~~~~~~~
POST /challenger HTTP/1.1
Host: localhost:4567
User-Agent: insomnia/6.5.4
Accept: */*
Content-Length: 0
~~~~~~~~


~~~~~~~~
HTTP/1.1 201 Created
Date: Tue, 28 Jul 2020 14:26:48 GMT
X-CHALLENGER: rest-api-challenges-single-player
Location: /gui/challenges
Content-Type: text/html;charset=utf-8
Transfer-Encoding: chunked
Server: Jetty(9.4.z-SNAPSHOT)
~~~~~~~~