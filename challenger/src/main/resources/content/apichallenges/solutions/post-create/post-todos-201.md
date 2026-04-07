---
date:  2021-01-24T10:00:00Z
lastmod: 2026-02-18
title: API Challenges Solution For - POST todos 201
seo_title: Solution: POST todos 201 Guide | API Challenges
description: How to solve API challenge POST todos 201 to create a todo item in the application.
seo_description: Use this walkthrough to solve POST todos 201 with request setup, key headers, and expected status codes so you can complete the challenge confidently.
next_challenge: /apichallenges/solutions/post-create/post-todos-400
schema_howto_steps: Create a POST request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Send a valid JSON payload that matches the field and content constraints||Send the request and verify the response status is 201
showads: true
---

# How to complete the challenge `POST /todos (201)`

How to use a POST request to create a todo item in the application and receive a 201 status response code.

## POST /todos (201)

> 	Issue a `POST` request to successfully create a todo

- `POST` request will make changes to the system
- `201` means an item created
- We are using this request to create a todo item
- Perform a `GET` first to see what the format of the message is
- Add a `Content-Type` of `application/json` to tell the server what format the message body is

## Basic Instructions

- Issue a POST request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- Use the format for the todo which you see in the `GET` response
- Do not include an 'id' in the request because that is added automatically by the system
- The response status code should be `201` because the todo is created
- IF you get a different response code, check the body of the message because you made have made a typo. Read the error message in the response to guide you.

## Example Request

~~~~~~~~
> POST /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/json
> Accept: */*
> Content-Length: 108

|    {
|       "title": "create todo process payroll",
|       "doneStatus": true,
|       "description": ""
|     }
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 201 Created
< Connection: close
< Date: Tue, 31 Aug 2021 16:20:40 GMT
< Content-Type: application/json
< Location: todos/453
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

## Overview Video

{{<youtube-embed key="T0LFHwavsNA" title="Solution to create a Todo item using POST">}}

[Patreon ad free version](https://www.patreon.com/posts/41230767)


