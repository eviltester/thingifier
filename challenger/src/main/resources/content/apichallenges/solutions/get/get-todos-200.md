---
title: API Challenges Solution For - GET Todos (200)
description: How to solve the API challenge and GET all the Todos
---

# How to complete the challenge `GET /todos 200`.

How to solve the API challenge and issue a GET request to return all the Todos in default JSON format.

## GET /todos (200)

> Issue a `GET` request on the `/todos` end point

- This will show you all the todos in the system
- The return format is a useful guide for the syntax of the request you will send in POST messages
- Perform a `GET` prior to any amendment or deletion, to make sure that the data in the system is what you expect it to be.
- remember not to add a trailing `/` on the request e.g. `/todos/` - that is a different end point

## Basic Instructions

- Issue a GET request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header
- The response body shows all the todos.

## Example Request

~~~~~~~~
> GET /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Fri, 28 Aug 2020 13:15:04 GMT
< Content-Type: application/json
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

## Example Response Payload

~~~~~~~~
{
  "todos": [
    {
      "id": 6,
      "title": "process payroll",
      "doneStatus": false,
      "description": ""
    },
    {
      "id": 9,
      "title": "tidy meeting room",
      "doneStatus": false,
      "description": ""
    }
   ]
}
~~~~~~~~

## Overview Video

{{<youtube-embed key="OpisB0UZq0c" title="Solution video for GET all TODOs">}}

[Patreon ad free version](https://www.patreon.com/posts/41107610)
