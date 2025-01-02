---
date:  2024-12-31T15:35:00Z
title: API Challenges Solution For - POST todos 413 - content too long
description: How to solve API challenge POST todos 413 content too long by sending a payload that is too large.
showads: true
---

# How to complete the challenge `POST /todos (413) content too long`

How to complete the challenge `POST /todos (413) content too long` to fail to create a todo item in the application because the request payload sent to the API is too long.

## POST /todos (413) content too long

> Issue a POST request to create a todo with total payload content greater than 5000 characters

- `POST` request will create a todo if the details are valid when using the `/todos` end point
- `413` is a status code meaning "Request Entity Too Large" meaning that we supplied a request that is too long for the server to handle
- The server is coded to only accept a maximum of 5000 characters
- To pass this challenge write 5000 characters or more in the descripton field

## Basic Instructions

- Issue a `POST` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The `content-type` in the message should be `application/json` because we are sending a JSON payload
- The Payload should be more than 5000 characters (the example below needs to be amended)
- You can easily generate a 5000 characters String [online using a CounterString Generator](https://eviltester.github.io/TestingApp/apps/counterstrings/counterstrings.html) and replace `<insert 5000 characters here>` with the longer string

```json
    {
        "title": "this title is valid.",
        "doneStatus": true,
        "description": "<insert 5000 characters here>"
    }
```
- The response status code should be `413` because the request is too long, the error message describes the valid length


```json
{
  "errorMessages": [
    "Error: Request body too large, max allowed is 5000 bytes"
  ]
}
```

Hints:

- when testing for field or message lengths CounterString tools can be useful to generate strings of the exact length required
- when testing APIs we need to do more than just test field lengths, we need to make sure the server is not vulnerable to Denial of Service attacks from the payload sizes
- As a follow on exercise: try to send in a payload of exactly 5000 bytes

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
|       "title": "this title is valid.",
|       "doneStatus": true,
|       "description": "<insert 5000 characters here>"
|     }
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 413 Payload Too Large
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
    "Error: Request body too large, max allowed is 5000 bytes"
  ]
}
```






