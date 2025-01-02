---
date:  2024-01-01T11:26:00Z
title: API Challenges Solution For - POST todos/id 404
description: How to solve API challenge POST todos/id 404 to try to update a todo which does not exist.
showads: true
---

# How to complete the challenge `POST /todos/id (404)`

How to use a POST request to try to update a todo item in the application, but the todo item id should not exist.

## POST /todos/id (404)

> Issue a POST request to try and update a todo, but no todo with this id should exist

- `POST` request will update a todo if the provided `id` exists `/todos/id` end point
    - e.g. `POST /todos/3` for a todo with `id==3`
- `404` is a failure code, in this case it means no todo with this id exists
- The body of the message should be a `json` or `xml` partial set of `todo` details,
-  and the `json` or `xml` should be defined in the `content-type` header
- The 404 response should have an error message explaining the problem


## Basic Instructions

- Issue a `POST` request to end point "/todos/id"
    - where `id` is replaced with the id of a todo that does not exist
        - if you don't know any then a `GET /todos` would show a list of todos.
    - `{{<ORIGIN_URL>}}/todos/id`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The `content-type` in the message should be `application/json` because we are sending a JSON payload
- The Payload should have a partial set of todo details. e.g.

```json
{
  "title": "updated title"
}
```
- The response status code should be `404` when the details are valid and the id does not exist.
- The body of the response will be a JSON showing the error.

```json
{
  "errorMessages": [
    "No such todo entity instance with id == 200 found"
  ]
}
```

## Example Request

~~~~~~~~
> POST /todos/200 HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/json
> Accept: */*
> Content-Length: 32

| 	{
| 		"title": "updated title"
| 	}
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 404 Not Found
< Connection: close
< Date: Sat, 06 Feb 2021 12:08:58 GMT
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur

| 	{
| 	  "errorMessages": [
| 	    "No such todo entity instance with id == 200 found"
| 	  ]
| 	}
~~~~~~~~





