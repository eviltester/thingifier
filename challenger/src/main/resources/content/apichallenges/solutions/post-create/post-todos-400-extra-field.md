---
date:  2021-01-30T15:55:00Z
title: API Challenges Solution For - POST todos 400 extra
description: How to solve API challenge POST todos 400 extra to trigger validation errors due to an extra field in the payload.
showads: true
---

# How to complete the challenge `POST /todos (400) extra`

How to complete the challenge `POST /todos (400) extra` to fail to create a todo item in the application due to not passing validation when the payload contains an extra field.

## POST /todos (400) extra

> Issue a POST request to create a todo but fail validation due to an unrecognised field

- `POST` request will create a todo if the details are valid when using the `/todos` end point
- `400` is an error code meaning that we supplied invalid details
- In this case we are asked to make a mistake by adding an extra field not defined in the request schema e.g. `priority="extra"`

## Basic Instructions

- Issue a `POST` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The `content-type` in the message should be `application/json` because we are sending a JSON payload
- The Payload should have an error due to an unexpected field.

```json
{
  "title": "a title",
  "priority": "extra"
}
```
- The response status code should be `400` because the request is invalid
- The body of the response will be an error message array with a single message

```json
{
  "errorMessages": [
    "Could not find field: priority"
  ]
}
```

Hints:

- We don't just want to check for mandatory and missing content we need to make sure that the server does not try and create entities and inject new fields into the database
- For follow on exercises you might want to see what happens:
   - if we duplicate fields e.g. have two `title` fields
   - if we duplicate headers
- When testing APIS we need to go beyond field contents and look at the message format itself

## Example Request

~~~~~~~~
> POST /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/json
> Accept: */*
> Content-Length: 116

|     {
|       "title": "a title",
|       "priority": "extra"
|     }
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 400 Bad Request
< Connection: close
< Date: Thu, 27 Aug 2020 14:23:12 GMT
< Content-Type: application/json
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Returned body:

```json
{
  "errorMessages": [
    "Could not find field: priority"
  ]
}
```





