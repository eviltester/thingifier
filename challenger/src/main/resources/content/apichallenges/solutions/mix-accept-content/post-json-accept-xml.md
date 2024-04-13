---
date:  2021-07-18T08:00:00Z
title: API Testing Challenge 24 - How To - POST JSON accept XML
description: How to solve API challenge POST /todos JSON to XML. Creating a todo with JSON and receiving response in XML.
---

# How to complete the challenge `POST /todos JSON to XML`

The `content-type` and `accept` headers do not need to be the same.
API Challenges allows us to send a POST request to create a todo item using JSON but receive an XML response.

## 	POST /todos XML to JSON

> Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/json` but Accept `application/xml`

- `POST` request means we will send information in the body of the message
    - e.g. `POST /todos` sends to the todos endpoint
- `create a todo` means that the payload will be valid data to create a todo item
- `using Content-Type` `application/json` means that we will set `content-type` header to `application/json` and the payload will be in JSON format
- `Accept` `application/xml` means add an `accept` header of `application/xml` to receive the response in JSON format
- add the `X-CHALLENGER` header to track progress


## Basic Instructions

We can mix different accept and content-types so we can send payloads in one format, and receive responses in another format. This challenge is about sending payload in JSON but having the response in XML.

- Issue a `POST` request to end point "/todos"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/todos`
- The request should have an `Content-Type` header of `application/json`
- add a valid payload in JSON format to create a todo item e.g.

```json
   {
        "title": "create todo process payroll",
        "doneStatus": true,
        "description": ""
   }
```


- add an `accept` header of `application/xml` to receive the response in XML format
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `201` when all the details are valid.
- Check the body of the response has XML formatted todo item with full details of the created item
- Check the location header for the REST API call to retrieve details of the created item

Extras:

- try GET the location header URL to return the created todo item

## Example Request

~~~~~~~~
> POST /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/json
> accept: application/xml
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
< Date: Sun, 18 Jul 2021 09:03:58 GMT
< Content-Type: application/xml
< Location: todos/320
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Example Response body:

```xml
<todo>
   <doneStatus>true</doneStatus>
   <description/>
   <id>320</id>
   <title>create todo process payroll</title>
</todo>
```


## Overview Video

{{<youtube-embed key="uw1Jq8t1em4" title="Solution to POST using JSON with resopnse in XML">}}

[Patreon ad free version](https://www.patreon.com/posts/53821574)




