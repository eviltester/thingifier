---
date:  2025-01-01T14:24:00Z
title: API Challenges Solution For - PUT todos/id 400 no amend id
description: How to solve API challenge PUT todos/id 400 to fail updating a todo due to mismatched ids.
showads: true
---

# How to complete the challenge `PUT /todos/id (400) no amend id`

How to use a PUT request to trigger validation errors when attempting to update a todo id.

PUT request updates are idempotent so should generate the same response each time. But the id is auto generated and cannot be amended.

## PUT /todos/id (400) no amend id

> Issue a PUT request to fail updating a todo id

- `PUT` request can update a todo if the provided `id` exists `/todos/id` end point
    - e.g. `PUT /todos/3` for a todo with `id==3`
- `400` is an failure code, in this case it means the request was invalid
- The body of the message should be a `json` or `xml` partial set of `todo` details,
-  and the `json` or `xml` should be defined in the `content-type` header
- the id must be included in the payload and changed to try and amend the saved todo id


## Basic Instructions

- Issue a `PUT` request to end point "/todos/id"
    - where `id` is replaced with the id of an existing todo
        - if you don't know any then a `GET /todos` would show a list of todos
    - `{{<ORIGIN_URL>}}/todos/id`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The `content-type` in the message should be `application/json` because we are sending a JSON payload
- The Payload should have enough data to trigger an amend and try to change the id e.g. `title` and `id`
- id in payload should differ from the URL e.g. if "/todos/3" then the payload below would trigger the issue

```json
{
  "id": 4,
  "title": "updated title"
}
```
- The id included in the payload should not be the same as the id of the url, we are trying to amend the id, but the id is auto generated.
- The response status code should be `400` when all the request is invalid.
- The body of the response will be a JSON showing the error message.

```json
{
  "errorMessages": [
    "Can not amend id from 3 to 4"
  ]
}
```

NOTE: if you haven't read the documentation and don't know what format to use then issue a GET request for a single entity and the payload format for the `POST` is likely to be pretty close.

NOTE: because you add an id to the payload you risk triggering an error validation if the id in the payload is different from the id in the URL.

## Example Request

~~~~~~~~
> PUT /todos/3 HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/json
> Accept: */*
> Content-Length: 32

| 	{
|     "id": 4,
| 	  "title": "updated title"
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
  "errorMessages": [
    "Can not amend id from 3 to 4"
  ]
}
```





