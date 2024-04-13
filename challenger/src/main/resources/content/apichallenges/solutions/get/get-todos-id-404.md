---
date:  2020-12-15T09:00:00Z
title: API Challenges Solution For - GET todos id 404
description: How to solve challenge GET todos id 404.
---

# How to complete the challenge `GET /todos/id 404`.

How to receive a 404 status code response by trying to GET a todo item by id for a non-existent todo item.

## GET /todos/id (404)

> 	Issue a GET request on the `/todos/{id}` end point for a todo that does not exist

- This will show you a 404 status code in the API response
- 404 status code means Not Found
- The `{id}` means, replace this with the id of a non-existant todo item

## Basic Instructions

- Issue a GET request to end point "/todos/{id}"
    - `{{<ORIGIN_URL>}}/todos/{id}`
- The request should have an `X-CHALLENGER` header
- The response status code should be `404` because `{id}` does not exist
- an error message should be shown in the response body

## Example Request

~~~~~~~~
> GET /todos/20 HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 404 Not Found
< Connection: close
< Date: Thu, 27 Aug 2020 13:53:54 GMT
< Content-Type: application/json
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

## Example Response Body

~~~~~~~~
{
  "errorMessages": [
    "Could not find an instance with todos/20"
  ]
}
~~~~~~~~


## Overview Video

{{<youtube-embed key="1S5kpd8-xfM" title="Solution to Get Missing Todo 404">}}

[Patreon ad free version](https://www.patreon.com/posts/41109076)

