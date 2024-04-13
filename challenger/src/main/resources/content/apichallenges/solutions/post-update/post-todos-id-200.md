---
date:  2021-03-07T09:30:00Z
title: API Challenges Solution For - POST todos/id 200
description: How to solve API challenge POST todos/id 200 to update a todo in the application.
---

# How to complete the challenge `POST /todos/id (200)`

How to use a POST request to successfully update a todo item in the application.

## POST /todos/id (200)

> Issue a POST request to successfully update a todo

- `POST` request will update a todo if the provided `id` exists `/todos/id` end point
    - e.g. `POST /todos/3` for a todo with `id==3`
- `200` is an success code, in this case it means the todo was updated
- The body of the message should be a `json` or `xml` partial set of `todo` details,
-  and the `json` or `xml` should be defined in the `content-type` header


## Basic Instructions

- Issue a `POST` request to end point "/todos/id"
    - where `id` is replaced with the id of an existing todo
        - if you don't know any then a `GET /todos` would show a list of todos, or you could `POST /todos` to create one.
    - `{{<ORIGIN_URL>}}/todos/id`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The `content-type` in the message should be `application/json` because we are sending a JSON payload
- The Payload should have a partial set of todo details. e.g.

```json
{
  "title": "updated title"
}
```
- The response status code should be `200` when all the details are valid.
- The body of the response will a JSON showing the full todo details, and your updated values should be present.

```json
{
  "id": 49,
  "title": "updated title",
  "doneStatus": false,
  "description": ""
}
```

NOTE: if you haven't read the documentation and don't know what format to use then issue a GET request for a single entity and the payload format for the `POST` is likely to be pretty close. You may not be allowed to use all the fields in an update, e.g. the `id` might throw an error becuase you should not be able to update the `id`.


## Example Request

~~~~~~~~
> POST /todos/49 HTTP/1.1
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
< HTTP/1.1 200 OK
< Connection: close
< Date: Sat, 06 Feb 2021 12:08:58 GMT
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Returned body:

```json
{
  "id": 49,
  "title": "updated title",
  "doneStatus": false,
  "description": ""
}
```

## Overview Video

{{<youtube-embed key="feXdRpZ_tgs" title="Solution to amend a todo item using POST">}}

[Patreon ad free version with transcript](https://www.patreon.com/posts/48448220)




