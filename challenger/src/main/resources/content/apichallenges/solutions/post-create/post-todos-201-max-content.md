---
date:  2024-12-31T14:04:00Z
title: API Challenges Solution For - POST todos 201 - max out content
description: How to solve API challenge POST todos 201 max out content by sending request details such that all fields are maximum length.
showads: true
---

# How to complete the challenge `POST /todos (201) max out content`

How to complete the challenge `POST /todos (201) max out content` to successfully create a todo item in the application where the title and the description are the maximum length.

## POST /todos (201) max out content

> Issue a POST request to create a todo with maximum length allowed `title` and `description` fields

- `POST` request will create a todo if the details are valid when using the `/todos` end point
- `201` is a status code meaning that we supplied valid details and a new item was created
- In this case we are asked to make `title` field and `description` field contents the maximum length
- The API Documentation for the todos endpoint says that a title `Maximum length allowed is 50`
- The API Documentation for the todos endpoint says that a description `Maximum length allowed is 200`

## Basic Instructions

- Issue a `POST` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The `content-type` in the message should be `application/json` because we are sending a JSON payload
- The Payload should have a `title` containing 50 characters and a `description` of 200 characters length.

```json
    {
        "title": "this title has just enough characters to validate.",
        "doneStatus": true,
        "description": "This description has just enough characters to validate because it is exactly 200 characters in length. I had to use a tool to check this - so I should have used a CounterString to be absolutely sure."
    }
```
- The response status code should be `201` because the request is valid and the todo item created
- The body of the response will be the todo details

```json
{
  "errorMessages": [
    "Failed Validation: Maximum allowable length exceeded for title - maximum allowed is 50"
  ]
}
```

Hints:

- when testing for field lengths CounterString tools can be useful to generate strings of the exact length required
- a 201 response usually has a `Location` header with a URL which we could `GET` to see the created item

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
|       "title": "this title has just enough characters to validate.",
|       "doneStatus": true,
|       "description": "This description has just enough characters to validate because it is exactly 200 characters in length. I had to use a tool to check this - so I should have used a CounterString to be absolutely sure."
|     }
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 201 Created
< Connection: close
< Date: Thu, 27 Aug 2020 14:23:12 GMT
< Content-Type: application/json
< Location:	/todos/11
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Returned body:

```json
{
  "id": 11,
  "title": "this title has just enough characters to validate.",
  "doneStatus": true,
  "description": "This description has just enough characters to validate because it is exactly 200 characters in length. I had to use a tool to check this - so I should have used a CounterString to be absolutely sure."
}
```






