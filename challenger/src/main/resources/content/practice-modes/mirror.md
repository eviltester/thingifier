---
title: API Challenges Mirror Mode
description: See the raw HTTP request sent to the API server and check if your REST API tool sends what you expect.
---

The API Challenges Mirror Mode shows you the HTTP request that you sent to the API server, an easy way to see if your REST API tool is performing as instructed or did it amend your request in ways you didn't expect? Did it add extra headers, or worse, remove headers that you wanted to include?

# Mirror Mode

{{<youtube-embed key="Q3qbyUNwYbM" title="how to use mirror mode to see request details">}}

[Patreon ad free video](https://www.patreon.com/posts/54382928)

The API has a mirror mode, this allows you to experiment with different verbs and configurations.

You will see, in your API tool, a response showing you the details of the request that you sent.

There are two mirror end points:

- `mirror/request`
- `mirror/raw`

The `mirror/request` end point will try to honour the `accept:` header in the response, so if you ask for `application/json` then the response will be json format.

The `mirror/raw` end point will always send the request back as raw text format.

This endpoint can be very useful for seeing what your HTTP Rest Client is sending to the server. You can spot any additional http headers that the client has added and see if the HTTP Client has combined any headers, or dropped any headers.

## Accessing The Mirror Mode

To access the mirror mode on the public cloud make requests to:

- {{<ORIGIN_URL>}}/mirror/request
- {{<ORIGIN_URL>}}/mirror/raw



## Request EndPoint

e.g. to use the request endpoint:

```
GET {{<ORIGIN_URL>}}/mirror/request
```

Will return 200... everything (almost) returns a 200.

The `mirror/request` endpoint will use the `Accept` header to format the response.

If you want the response in XML or JSON then add the relevant `Accept` header.

e.g.

```
GET /mirror/request HTTP/1.1
Accept: application/json
Content-Length: 0
Host: localhost:4567
```

Would return the response as json

```
HTTP/1.1 200 OK
Date: Sat, 17 Feb 2024 13:09:34 GMT
Content-Type: application/json
access-control-allow-origin: *
x-challenger: x-challenger-guid
access-control-allow-headers: *
Server: Jetty(9.4.12.v20180830)
Content-Length: 320

{
"details":"GET {{<ORIGIN_URL>}}/mirror/request
\n\nQuery Params\n==..."
}
```


## Raw EndPoint

e.g. to use on the public cloud version of apichallenges:

```
GET {{<ORIGIN_URL>}}/mirror/raw
```

```
GET /mirror/raw HTTP/1.1
Accept: application/json
Content-Length: 0
Host: localhost:4567
```

Will return 200.

The `mirror/raw` endpoint will not use the `Accept` header to format the response and will always return a `text` representation.

```
HTTP/1.1 200 OK
Date: Sat, 17 Feb 2024 13:13:58 GMT
Content-Type: text/plain
access-control-allow-origin: *
x-challenger: x-challenger-guid
access-control-allow-headers: *
Server: Jetty(9.4.12.v20180830)
Content-Length: 276

GET {{<ORIGIN_URL>}}/mirror/raw

Query Params
============

IP
=======
127.0.0.1

Raw Headers
=======
Accept: application/json
Content-Length: 2
Host: {{<HOST_URL>}}

Processed Headers
=======
host: localhost:4567
content-length: 2
accept: application/json

Body
====


```

## OPTIONS, HEAD

Only `options` and `head` respond differently... because `options` and `head` should respond differently.

Useful for getting started and getting used to your tooling.


## Why is this Mirror Mode Useful?

The mirror mode is another way of seeing the 'true' request received.

You can configure most API tools to use a Proxy like [BurpSuite](https://portswigger.net/burp) or [OwaspZAP](https://www.zaproxy.org/) and you will see the actual request that the tool sends.

You can also use the Insomnia Timeline to see the request.

In Postman you can use the Postman Console to see the requests.

The Mirror Mode shows you the request received by the server. When run on Localhost there are no intermediate systems so you can see what the tooling sends in the logs.

When run on [apichallenges.eviltester.com](https://apichallenges.eviltester.com/practice-modes/mirror) you see that the Cloud environment adds additional headers in to the request.

Additionally the REST Client we use may add or amend headers.

Very often we are not aware of this level of amendment when testing and may not test for this.

The Mirror mode makes it clear that there are multiple systems involved in issuing a request and they can all pose a risk to the system or our testing. e.g. some REST Clients will not send duplicate headers: some will combine headers, some will pick the first (or last) header.

## Swagger OpenAPI File

You can download a simple Swagger [OpenAPI File for mirror mode](/mirror/docs/swagger).
