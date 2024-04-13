---
date:  2021-01-24T09:00:00Z
title: API Challenges Solution For - HEAD todos 200
description: How to solve API challenge HEAD todos 200
---

# How to complete the challenge `HEAD /todos (200)`

How to issue a HEAD request and see the results of a GET request without the body of the response, this can be useful for checking the existence of an item when automating.

## HEAD /todos (200)

> 	Issue a HEAD request on the `/todos` end point

- `HEAD` request is basically a `GET` but doesn't return the body
- Use it to 'ping' an end point and see if it exists, or to check the Headers are working correctly
- Usually returns a 200 (if it exists) or a 404 (if it doesn't exist)

## Basic Instructions

- Issue a HEAD request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `200` because the end point exists
- Compare the response with the response from `GET /todos`

## Example Request

~~~~~~~~
> HEAD /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: keep-alive
< Date: Thu, 27 Aug 2020 14:09:19 GMT
< Content-Type: application/json
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

## Overview Video

{{<youtube-embed key="zKbytTelP84" title="Solution to HEAD specific Todo endpoint">}}

[Patreon ad free version](https://www.patreon.com/posts/41230531)

