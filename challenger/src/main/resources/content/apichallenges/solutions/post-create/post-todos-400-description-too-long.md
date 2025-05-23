---
date:  2024-12-31T14:30:00Z
title: API Challenges Solution For - POST todos 400 - description too long
description: How to solve API challenge POST todos 400 description too long by sending request details such that they fail validation.
showads: true
---

# How to complete the challenge `POST /todos (400) description too long`

How to complete the challenge `POST /todos (400) description too long` to fail to create a todo item in the application due to not passing validation for the description.

## POST /todos (400) description too long

> Issue a POST request to create a todo but fail validation on the `description` field

- `POST` request will create a todo if the details are valid when using the `/todos` end point
- `400` is an error code meaning that we supplied invalid details
- In this case we are asked to make a mistake with the `description` field so that it fails validation on the server side
- The API Documentation for the todos endpoint says that a title `Maximum length allowed is 200`

## Basic Instructions

- Issue a `POST` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The `content-type` in the message should be `application/json` because we are sending a JSON payload
- The Payload should have an error in the `description`. A valid `description` is Maximum of 200 characters so if we create a title with 201 characters it should fail validation and pass the challenge.

```json
{
    "title": "this title is fine",
    "doneStatus": true,
    "description": "Should trigger a 400 error because this description is too long and exceeds the maximum length of 200 characters. We should do additional testing to make sure that 200 is valid. And check large strings"
}
```
- The response status code should be `400` because the request is invalid
- The body of the response will be an error message array with a single message

```json
{
  "errorMessages": [
    "Failed Validation: Maximum allowable length exceeded for description - maximum allowed is 200"
  ]
}
```

Hints:

- when testing for field lengths CounterString tools can be useful to generate strings of the exact length required

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
|       "description": "Should trigger a 400 error because this description is too long and exceeds the maximum length of 200 characters. We should do additional testing to make sure that 200 is valid. And check large strings",
|       "doneStatus": true,
|       "description": "should trigger a 400 error"
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
    "Failed Validation: Maximum allowable length exceeded for description - maximum allowed is 200"
  ]
}
```






