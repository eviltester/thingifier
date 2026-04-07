---
date:  2021-07-17T10:32:00Z
lastmod: 2026-02-18
title: API Challenges Solution For - GET todos Invalid Accept Header 406
seo_title: Solution: GET todos Invalid Accept Head | API Challenges
description: How to solve API challenge GET todos Invalid Accept 406 to GET the todos with an unsupported accept header present which generates a 406 error response.
seo_description: Use this walkthrough to solve GET todos Invalid Accept with request setup, key headers, and expected status codes so you can complete the challenge.
schema_howto_steps: Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Set the Accept header to the required media type and verify response format||Send the request and verify the response status is 406
showads: true
---

# How to complete the challenge `GET /todos (406)`

Some APIs will report an error when asked for a return format that they do not support. Other APIs will respond with a default. The API Challenges will respond with a `406` status code. We can test for this by sending a GET request for all todos but pass in an 'accept' format that the system does not support.

## 	GET /todos (406)

When we issue a request with an accept header, we are asking for a specific content format in the response. But... if we ask for a format that is not supported then the system may reespond with a 406 'NOT ACCEPTABLE' status code.

> Issue a GET request on the `/todos` end point with an `Accept` header of `application/gzip` to receive a response with a `406` 'NOT ACCEPTABLE' status code.

- `GET` request asks for a response with all the todo items
    - e.g. `GET /todos` to get all the todo items
- `406` is a success code, in this case it means the accept header is not supported by the system
- add the `X-CHALLENGER` header to track progress


## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/todos`
- The request should have an `Accept` header with the value `application/gzip`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `406` when all the details are valid.
- Check the body of the message has JSON formatted error message response
- Check the `content-type` header in the response has `application/json` matching the response body

Some systems may simply ignore the 'accept' header and return the response in the default format.


## Example Request

~~~~~~~~
> GET /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: insomnia/2021.2.2
> Accept: application/gzip
> X-CHALLENGER: x-challenger-guid
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 406 Not Acceptable
< Connection: close
< Date: Sat, 17 Jul 2021 12:21:37 GMT
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
  "errorMessages": [
    "Unrecognised Accept Type"
  ]
}
```

## Overview Video

{{<youtube-embed key="QzfbegkY1ok" title="Solution to Get all Todos in usupported format">}}

[Patreon ad free version](https://www.patreon.com/posts/53793842)




