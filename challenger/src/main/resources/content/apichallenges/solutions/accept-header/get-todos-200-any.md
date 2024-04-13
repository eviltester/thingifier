---
date:  2021-05-29T09:30:00Z
title: API Challenges Solution For - GET todos any 200
description: How to solve API challenge GET todos any 200 to accept the todos in default format.
---

# GET all the todos in default format

How to complete the challenge `GET /todos ANY (200)` to successfully GET all the todos in default format.

## GET /todos ANY (200)

> Issue a GET request on the `/todos` end point with an `Accept` header of `*/*` to receive results in Default format

- `GET` request will receive a response with all the todo items
    - e.g. `GET /todos` to get all the todo items
- `200` is a success code, in this case it means the end point exists and the `todo items were returned
- `Accept` means that an `Accept` header was added to specify that the todos should be returned in **ANY** format i.e. the default from the server
- add the `X-CHALLENGER` header to track progress


## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/todos`
- The request should have an `Accept` header specifying ANY format by using a value of `*/*`, our application defaults to JSON
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `200` when all the details are valid.
- Check the body of the message has JSON format data, which is the default from the server
- Check the `content-type` header in the response has `application/json`


## Example Request

~~~~~~~~
> GET /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: insomnia/2021.2.2
> X-CHALLENGER: x-challenger-guid
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Sat, 29 May 2021 09:06:15 GMT
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Example Response body:

```json
{
  "todos": [
    {
      "id": 235,
      "title": "pay invoices",
      "doneStatus": false,
      "description": ""
    },
    {
      "id": 239,
      "title": "tidy meeting room",
      "doneStatus": false,
      "description": ""
    }
  ]
}
```


## Overview Video

{{<youtube-embed key="O4DhJ8Ohkk8" title="Solution to Get all Todos in default format">}}

[Patreon ad free version](https://www.patreon.com/posts/51830126)




