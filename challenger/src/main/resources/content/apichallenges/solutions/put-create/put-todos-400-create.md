---
date:  2025-01-01T11:05:00Z
title: API Challenges Solution For - PUT todos/id 400
description: How to solve API challenge PUT todos/id 400 invalid to create with PUT.
showads: true
---

# How to complete the challenge `PUT /todos/{id} (400)`

How to use a PUT request and fail to create a todo item in the application using PUT, receiving a 400 status response code. The request is invalid because IDs are auto generated and cannot be specified during creation.

## POST /todos/{id} (400)

> 	Issue a `PUT` request with an ID and fail to create a todo

- `PUT` request will make changes to the system
- `400` means an issue with the request
- We are trying to use this request to create a todo item so all mandatory details are necessary
- We are including an id, that does not exist, which we want to assign to the todo item
- Add a `Content-Type` of `application/json` to tell the server what format the message body is

## Basic Instructions

- Issue a GET request on "/todos" to identify an ID that does not exist
- Issue a PUT request to end point "/todos/{id}" where `{id}` is a specific id that we want to create
    - `{{<ORIGIN_URL>}}/todos/{id}`
- The request should have an `X-CHALLENGER` header to track challenge completion
- Use the format for the todo which you see in a `GET` response, without the ID field
- Do not include an 'id' in the request body because that is added automatically by the system
- Do include an `id` in the URL to try and specify the id we want to create 
- The response status code should be `400` because the request is invalid for this API
- Check the error message to make sure it is for the expected reason `"Cannot create todo with PUT due to Auto fields id"`
- If you get a different response code, check the body of the message because you made have made a typo. Read the error message in the response to guide you.

NOTE: For some APIs this would be an acceptable thing to do. This particular API has logic to prevent creating items with specific IDs and this challenge helps test for it.

## Example Request

~~~~~~~~
> POST /todos/300 HTTP/1.1
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
< HTTP/1.1 400 Bad Request
< Connection: close
< Date: Tue, 31 Aug 2021 16:20:40 GMT
< Content-Type: application/json
< Location: todos/453
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur

|    {
|     "errorMessages": [
|          "Cannot create todo with PUT due to Auto fields id"
|     ]
|    }
~~~~~~~~




