---
date:  2021-01-30T09:30:00Z
lastmod: 2026-02-18
title: API Challenges Solution For - POST todos 400
seo_title: Solution: POST todos 400 Guide | API Challenges
description: How to solve API challenge POST todos 400 to amend the request details such that they fail validation.
seo_description: Use this walkthrough to solve POST todos 400 with request setup, key headers, and expected status codes so you can complete the challenge confidently.
next_challenge: /apichallenges/solutions/post-create/post-todos-400-title-too-long
schema_howto_steps: Create a POST request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Send a valid JSON payload that matches the field and content constraints||Send the request and verify the response status is 400
showads: true
---

# How to complete the challenge `POST /todos (400)`

How to complete the challenge `POST /todos (400)` to fail to create a todo item in the application due to not passing validation.

## POST /todos (400)

> Issue a POST request to create a todo but fail validation on the `doneStatus` field

- `POST` request will create a todo if the details are valid when using the `/todos` end point
- `400` is an error code meaning that we supplied invalid details
- In this case we are asked to make a mistake with the `doneStatus` field so that it fails validation server side i.e. `doneStatus="bob"`

## Basic Instructions

- Issue a `POST` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The `content-type` in the message should be `application/json` because we are sending a JSON payload
- The Payload should have an error in the `doneStatus`. A valid `doneStatus` is `true` or `false` so if we send in a String like `"bob"` it should fail validation.

```json
    {
        "title": "create new todo",
        "doneStatus": "bob",
        "description": "created via insomnia"
    }
```
- The response status code should be `400` because the request is invalid
- The body of the response will be an error message array with a single message

```json
{
  "errorMessages": [
    "Failed Validation: doneStatus should be BOOLEAN"
  ]
}
```


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
|       "title": "create new todo",
|       "doneStatus": "bob",
|       "description": "created via insomnia"
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
    "Failed Validation: doneStatus should be BOOLEAN"
  ]
}
```

## Overview Video

{{<youtube-embed key="tlye5bQ72g0" title="Solution to failing to create a Todo item using POST">}}

[Patreon ad free version](https://www.patreon.com/posts/46603655)




