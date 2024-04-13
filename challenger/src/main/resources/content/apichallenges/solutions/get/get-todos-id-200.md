---
title: API Challenges Solution For - GET todos id 200
description: How to solve challenge GET todos id 200.
---

# How to complete the challenge `GET /todos/id 200`.

How to issue a GET request for an existing todo item using the id of the item and receive a 200 status code and see the todo item as JSON in the response body.

## GET /todos/id (200)

> 	Issue a GET request on the `/todos/{id}` end point to return a specific todo

- This will show you a todo in the API response
- 200 status code means OK
- The response is the basic JSON format you use in a POST request to create a todo
- The `{id}` means, replace this with the id of an existing todo item

## Basic Instructions

- Issue a GET request to end point "/todos/{id}"
    - `{{<ORIGIN_URL>}}/todos/{id}`
- The request should have an `X-CHALLENGER` header
- The response status code should be `200`

## Example Request

~~~~~~~~
> GET /todos/79 HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Tue, 01 Sep 2020 13:35:41 GMT
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

## Example Response Body

~~~~~~~~
{
  "todos": [
    {
      "id": 79,
      "title": "tidy meeting room",
      "doneStatus": false,
      "description": ""
    }
  ]
}
~~~~~~~~


## Overview Video

{{<youtube-embed key="JDbbSY3U_rY" title="Solution to Get Specific Todo by ID">}}

[Patreon ad free version](https://www.patreon.com/posts/41108384)

