---
date:  2021-05-09T09:30:00Z
title: API Challenges Solution For - GET todos json 200
description: How to solve API challenge GET todos json 200 to accept the todos in json format.
---

# GET all the todos in JSON format

How to complete the challenge `GET /todos JSON (200)` to successfully GET all the todos in JSON format.

## GET /todos JSON (200)

> Issue a GET request on the `/todos` end point with an `Accept` header of `application/json` to receive results in JSON format

- `GET` request will receive a response with all the todo items
    - e.g. `GET /todos` to get all the todo items
- `200` is a success code, in this case it means the end point exists and the `todo items were returned
- `Accept` means that an `Accept` header was added to specify that the todos should be returned in **JSON** format
- add the `X-CHALLENGER` header to track progress


## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/todos`
- The request should have an `Accept` header specifying JSON format by using a value of `application/json`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `200` when all the details are valid.
- Check the body of the message has JSON format data
- Check the `content-type` header in the response has `application/json`


## Example Request

~~~~~~~~
> GET /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: application/json
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Sun, 09 May 2021 11:07:48 GMT
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
            "id": 16,
            "title": "process payroll",
            "doneStatus": false,
            "description": ""
        },
        {
            "id": 15,
            "title": "pay invoices",
            "doneStatus": false,
            "description": ""
        }
    ]
}
```

## Overview Video

{{<youtube-embed key="79JTHiby2Qw" title="Solution to GET todos in JSON format">}}

[Patreon ad free version](https://www.patreon.com/posts/51045284)



