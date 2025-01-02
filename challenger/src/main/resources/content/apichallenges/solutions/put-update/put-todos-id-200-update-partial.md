---
date:  2025-01-01T12:53:00Z
title: API Challenges Solution For - PUT todos/id 200 full update
description: How to solve API challenge PUT todos/id 200 to update a todo in the application with a full payload.
showads: true
---

# How to complete the challenge `PUT /todos/id (200) partial update`

How to use a PUT request to successfully update a todo item in the application using a partial payload.

PUT request updates are idempotent so should generate the same response each time. A partial PUT update is different from a partial POST update. With a POST update any missing fields in the response will not be amended. With a PUT update the missing fields will be set to their default or empty values.

This behaviour varies for different APIs. Some APIs may not allow partial PUT updates.

## PUT /todos/id (200) partial update

> Issue a PUT request to successfully update a todo using a partial payload

- `PUT` request will update a todo if the provided `id` exists `/todos/id` end point
    - e.g. `PUT /todos/3` for a todo with `id==3`
- `200` is an success code, in this case it means the todo was updated
- The body of the message should be a `json` or `xml` partial set of `todo` details,
-  and the `json` or `xml` should be defined in the `content-type` header
- the id does not need to be included in the payload, but if it is then it should match the id in the url
- the fields included in the message should be the mandatory fields, otherwise the payload will not validate


## Basic Instructions

- Issue a `PUT` request to end point "/todos/id"
    - where `id` is replaced with the id of an existing todo
        - if you don't know any then a `GET /todos` would show a list of todos
    - `{{<ORIGIN_URL>}}/todos/id`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The `content-type` in the message should be `application/json` because we are sending a JSON payload
- The Payload should have a partial set of todo details. All mandatory fields should be included e.g.

```json
{
  "title": "partial update for title"
}
```
- Title is a mandatory field, without a default value
- `doneStatus` is boolean and defaults to `false`
- `description` has a default value of `""`
- The id included in the payload should be the same as the id of the url because we cannot update the id, the id is auto generated.
- The response status code should be `200` when all the details are valid.
- The body of the response will a JSON showing the full todo details, and your updated values should be present.

```json
{
  "id": 3,
  "title": "partial update for title",
  "doneStatus": false,
  "description": ""
}
```

NOTE: if you haven't read the documentation and don't know what format to use then issue a GET request for a single entity and the payload format for the `POST` is likely to be pretty close.

NOTE: because you add an id to the payload you risk triggering an error validation if the id in the payload is different from the id in the URL.

NOTE: PUT is idempotent so the result will always be the same, regardless of the initial values of the todo prior to the update request. As a follow on exercise check that this statement is true.

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
|     "title": "partial update for title"
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
  "id": 3,
  "title": "partial update for title",
  "doneStatus": false,
  "description": ""
}
```





