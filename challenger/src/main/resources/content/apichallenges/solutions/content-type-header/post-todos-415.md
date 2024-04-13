---
date:  2021-07-17T11:15:00Z
title: API Challenges Solution For - POST todos (415)
description: How to solve API challenge POST todos 415. Failing to creating a todo due to unsupported content format.
---

# How to complete the challenge `POST /todos (415)`

Most APIs will report an error when the `content-type` is specified as a format that they do not support.
Some APIs will parse the input using their default processing which can often result in a 500 error.
The API Challenges checks the `content-type` and if it is unsupported returns a 415 status code,
to see this we send a POST request to try and create a todo item but with unsupported content type.

## 	POST /todos (415)

> Issue a POST request on the `/todos` end point with an unsupported content type to generate a 415 status code

For this challenge we issue a request with a `content-type` which is unsupported.

- `POST` request means we will send information in the body of the message
    - e.g. `POST /todos` sends to the todos endpoint
- `unsupported content type` means that we will set `content-type` to something unknown eg. `bob`
- add the `X-CHALLENGER` header to track progress
- we know it has been rejected when we receive a `415` response
- the body of the response should contain the full details of the error message


## Basic Instructions

- Issue a `POST` request to end point "/todos"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/todos`
- The request should have an `Content-Type` header with an unsupported value e.g `bob`
- add a valid payload, but it won't match the content type so doesn't matter, but adding a valid payload makes sure that the system is not ignoring the content type specified.
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `415` when all the details are valid.
- Check the body of the response has JSON formatted error response
    - the system defaults to json for errors

Extras:

- try setting the 'accept' header to specify the format of the error message in the response

## Example Request

~~~~~~~~
> POST /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Content-Type: bob
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
< HTTP/1.1 415 Unsupported Media Type
< Connection: close
< Date: Sat, 17 Jul 2021 13:57:54 GMT
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: 19b81e3e-841f-41a9-91c1-373f69091a56
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Example Response body:

```json
{
  "errorMessages": [
    "Unsupported Content Type - bob"
  ]
}
```

## Overview Video

{{<youtube-embed key="L8H-vkbXyr0" title="Solution to unsupported content type challenge">}}

[Patreon ad free version](https://www.patreon.com/posts/53795763)




