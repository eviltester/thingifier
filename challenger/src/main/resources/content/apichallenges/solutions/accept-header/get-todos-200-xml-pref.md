---
date:  2021-05-29T10:30:00Z
title: API Challenges Solution For - GET todos XML as Preference 200
description: How to solve API challenge GET todos XML 200 to accept the todos in XML format as preferred format.
---

# Use Accept Headers to GET XML Content as a preference

How to complete the challenge `GET /todos XML (200)` to successfully GET all the todos in XML format as first preference.

## GET /todos XML Preference (200)

It is possible to ask for multiple types in the Accept header, expressing a preference for the returned format. If none of the types are available then expect a 406 response.

> Issue a GET request on the `/todos` end point with an `Accept` header of `application/xml` followed by `application/json` to receive results in XML format if supported, or JSON if not.

- `GET` request will receive a response with all the todo items
    - e.g. `GET /todos` to get all the todo items
- `200` is a success code, in this case it means the end point exists and the `todo items were returned
- `Accept` means that an `Accept` header was added to specify that the todos should be returned in **XML** format as first preference, followed by **JSON** as second preference
- add the `X-CHALLENGER` header to track progress


## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/todos`
- The request should have an `Accept` header specifying XML format by using a value of `application/xml,application/json`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `200` when all the details are valid.
- Check the body of the message has JSON format data, which is the default from the server
- Check the `content-type` header in the response has `application/json`

The chained Accept header `application/xml,application/json` asks for XML as first preference, but if not supported then supply JSON.


## Example Request

~~~~~~~~
> GET /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: application/xml,application/json
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Sat, 29 May 2021 10:05:24 GMT
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
        <id>267</id>
        <title>train staff</title>
    </todo>
    <todo>
        <doneStatus>false</doneStatus>
        <description/>
        <id>268</id>
        <title>schedule meeting</title>
    </todo>
</todos>
```


## Overview Video

{{<youtube-embed key="sLChuy9pc9U" title="Solution to Get all Todos in XML as preferred format">}}

[Patreon ad free version](https://www.patreon.com/posts/51831256)




