---
date:  2021-04-23T09:30:00Z
title: API Challenges Solution For - GET todos xml 200
description: How to solve API challenge GET todos xml 200 to accept the todos in xml format.
---

# How to complete the challenge `GET /todos XML (200)`

When we issue a GET request we can use the `Accept` header to request a specific format of result from the API. In this case we will ask for XML to successfully GET all the todos in XML format.

## GET /todos XML (200)

> Issue a GET request on the `/todos` end point with an `Accept` header of `application/xml` to receive results in XML format

- `GET` request will receive a response with all the todo items
    - e.g. `GET /todos` to get all the todo items
- `200` is a success code, in this case it means the end point exists and the `todo items were returned
- `Accept` means that an `Accept` header was added to specify that the todos should be returned in XML format
- add the `X-CHALLENGER` header to track progress


## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/todos`
- The request should have an `Accept` header specifying XML format by using a value of `application/xml`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `200` when all the details are valid.
- Check the body of the message has XML format data
- Check the `content-type` header in the response has `application/xml`


## Example Request

~~~~~~~~
> GET /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: application/xml
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Thu, 22 Apr 2021 16:49:31 GMT
< Content-Type: application/xml
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Example Response body:

```xml
<todos>
  <todo>
    <doneStatus>false</doneStatus>
    <description/>
    <id>273</id>
    <title>scan paperwork</title>
  </todo>
  <todo>
    <doneStatus>false</doneStatus>
    <description/>
    <id>277</id>
    <title>pay invoices</title>
  </todo>
</todos>
```


## Overview Video

{{<youtube-embed key="cLeEuZm2VG8" title="Solution to Get all Todos in XML format">}}

[Patreon ad free version](https://www.patreon.com/posts/50348257)




