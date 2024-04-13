---
date:  2021-04-12T09:30:00Z
title: API Challenges Solution For - DELETE todos/id 200
description: How to solve API challenge DELETE todos/id 200 to delete a todo in the application.
---

# Delete a todo item in the application

How to complete the challenge `DELETE /todos/id (200)` to successfully delete a todo item in the application.

## DELETE /todos/id (200)

> 	Issue a DELETE request to successfully delete a todo

- `DELETE` request will delete a todo if the provided `id` exists `/todos/id` end point
    - e.g. `DELETE /todos/3` to delete the todo with `id==3`
- `200` is an success code, in this case it means the todo was deleted
- The body of the message is empty
- add the `X-CHALLENGER` header


## Basic Instructions

- Issue a `DELETE` request to end point "/todos/id"
    - where `id` is replaced with the id of an existing todo
        - if you don't know any then a `GET /todos` would show a list of todos, or you could `POST /todos` to create one.
    - e.g using endpoint
        - `{{<ORIGIN_URL>}}/todos/id`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `200` when all the details are valid and the todo exists.
- To double check that the todo item was deleted, then you could issue a `GET` request on the todo directly and receive a `404` or issue a `GET` request on `/todos` and check it is not in the list of todos.

## Example Request

~~~~~~~~
> DELETE /todos/62 HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Thu, 27 Aug 2020 14:25:53 GMT
< Content-Type: application/json
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~


## Overview Video

{{<youtube-embed key="6MXTkaXn9qU" title="Solution to DELETE todo challenge">}}

[Patreon ad free version](https://www.patreon.com/posts/49931699)




