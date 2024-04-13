---
date:  2021-07-17T11:00:00Z
title: API Challenges Solution For - POST todos create todo with JSON
description: How to solve API challenge POST todos JSON. Creating a todo with JSON format.
---

# How to complete the challenge `POST /todos JSON`

To fully specify a request and response format we should control the `content-type` and `accept` headers. In this challenge we do this to send a POST request to create a todo item with body and response in JSON format.

## 	POST /todos JSON

To create and amend items in a REST API we usually use a POST request. POST requests are defined as 'partial' requests in that they don't need to have all the fields, but they need to have enough fields to be valid for the request.

> Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/json`, and Accepting only JSON ie. Accept header of `application/json`

For this challenge we issue a request with an accept header specifying JSON and we will send the message as JSON so we set the `content-type` header as well.

- `POST` request means we will send information in the body of the message
    - e.g. `POST /todos` sends to the todos endpoint
- `create a todo` means that we will not include a todo `id`, so a new todo is created
- `Content-Type` `application/json` means set the `content-type` header to `application/json` because we are sending a JSON formatted message
- `Accept header` of `application/json` means set the `accept` header to  `application/json` so the response is formatted as JSON
- the body of the message will have to be a valid todo item, and we can see the format in the documentation, or by issuing a `GET` request on the `/todos` endpoint
- add the `X-CHALLENGER` header to track progress
- we know it has been created when we receive a `201` response
- the body of the response should contain the full details of the `todo` created in JSON format


## Basic Instructions

- First issue a `GET` request on "/todos" with an `accept` header of `application/json` to see the format of a todo in JSON format
    - or read the documentation at [/docs](/docs)
    - copy a todo from the response to edit as payload for the `POST` message
- Issue a `POST` request to end point "/todos"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/todos`
- The request should have an `Accept` header with the value `application/json` because we want the response to be in JSON
- The request should have an `Content-Type` header with the value `application/json` because our payload in the message is in JSON format
- Use the `todo` that you copied from the `GET` request, remembering to remove the `id` because when we create a todo, it will be issued with an `id` automatically

We only need to use the minimum details, but could add a description if we wanted.

```json
   {
        "title": "create todo process payroll",
        "doneStatus": true,
        "description": ""
   }
```

- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `201` when all the details are valid.
- Check the body of the response has JSON formatted 'todo' response
    - this will contain the full details of the todo we created
- Check the `content-type` header in the response has `application/json` matching the requested accept format


## Additional Exercises

- you might want to experiment with removing fields like 'description', what happens if you miss out fields?
- check the `Location` header has the endpoint we can use to retrieve the todo, issue a `GET` on that endpoint to retrieve the details of the todo


## Example Request

~~~~~~~~
> POST /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/json
> Accept: */*
> Content-Length: 106

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
< Date: Sat, 17 Jul 2021 13:34:23 GMT
< Content-Type: application/json
< Location: todos/237
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Example Response body:

```json
{
    "id": 237,
    "title": "create todo process payroll",
    "doneStatus": true,
    "description": ""
}
```


## Overview Video

{{<youtube-embed key="VS9qIhgp51Q" title="Solution to JSON content-type challenge">}}

[Patreon ad free version](https://www.patreon.com/posts/53795265)




