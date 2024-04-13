---
date:  2021-04-21T09:30:00Z
title: API Challenges Solution For - OPTIONS todos 200
description: How to solve API challenge 13 OPTIONS todos 200 to identify the allowed verbs for an API End Point.
---

# How to complete the challenge `OPTIONS /todos (200)`

We can use `OPTIONS` request to identify the allowed verbs for an API End Point. This is useful to compare these with the Swagger/Open API documentation and also to check if the unlisted verbs are actually disallowed by the API.

## OPTIONS /todos (200)

> Issue an OPTIONS request on the `/todos` end point. You might want to manually check the 'Allow' header in the response is as expected.

- `OPTIONS` request will receive a response with no body, just headers if the provided end point exists i.e the `/todos` end point
    - e.g. `OPTIONS /todos` to show the allowed verbs for the `todos` endpoint
- `200` is a success code, in this case it means the end point exists and the `OPTIONS` verb is allowed
- The body of the message is empty
- add the `X-CHALLENGER` header to track progress
- the important header in the response is the `allow` header as this lists all the allowed verbs to use on the end point.


## Basic Instructions

- Issue an `OPTIONS` request to end point "/todos"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `200` when all the details are valid.
- Check the `allow` header in the response has valid values

As a set of follow on exercises:

- try `OPTIONS` on a few other endpoints in the API and see if the `allow` values are different.
- Try to issue requests for each of the allowed verbs.


## Example Request

~~~~~~~~
> OPTIONS /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: insomnia/2020.3.3
> X-CHALLENGER: x-challenger-guid
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Mon, 12 Apr 2021 09:41:34 GMT
< Allow: OPTIONS, GET, HEAD, POST
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Content-Type: text/html;charset=utf-8
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

## Overview Video

{{<youtube-embed key="Ld5h1TSnXWA" title="Solution to see Supported HTTP Verbs using OPTIONS for an endpoint">}}

[Patreon ad free version](https://www.patreon.com/posts/50387322)



