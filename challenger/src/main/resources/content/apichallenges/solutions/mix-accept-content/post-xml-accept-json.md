---
date:  2021-07-17T11:30:00Z
title: API Challenges Solution For - POST XML accept JSON
description: How to solve API challenge POST /todos XML to JSON. Creating a todo with XML and receiving response in JSON.
---

# How to complete the challenge `POST /todos XML to JSON`

When an API supports multiple formats in request and response we might be able to mix and match.
In this challenge we send a POST request to create a todo item using XML but receive JSON response.

## 	POST /todos XML to JSON

> Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/xml` but Accept `application/json`

- `POST` request means we will send information in the body of the message
    - e.g. `POST /todos` sends to the todos endpoint
- `create a todo` means that the payload will be valid data to create a todo item
- `using Content-Type` `application/xml` means that we will set `content-type` header to `application/xml` and the payload will be in XML format
- `Accept` `application/json` means add an `accept` header of `application/json` to receive the response in JSON format
- add the `X-CHALLENGER` header to track progress


## Basic Instructions

We can mix different accept and content-types so we can send payloads in one format, and receive responses in another format. This challenge is about sending payload in XML but having the response in JSON.

- Issue a `POST` request to end point "/todos"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/todos`
- The request should have an `Content-Type` header of `application/xml`
- add a valid payload in XML format to create a todo item e.g.

```xml
  <todo>
    <doneStatus>true</doneStatus>
    <title>file paperwork today</title>
  </todo>
```


- add an `accept` header of `application/json` to receive the response in JSON format
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `201` when all the details are valid.
- Check the body of the response has JSON formatted todo item with full details of the created item
- Check the location header for the REST API call to retrieve details of the created item

Extras:

- try GET the location header URL to return the created todo item

## Example Request

~~~~~~~~
> POST /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/xml
> Accept: application/json
> Content-Length: 92

|   <todo>
|     <doneStatus>true</doneStatus>
|     <title>file paperwork today</title>
|   </todo>
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 201 Created
< Connection: close
< Date: Sat, 17 Jul 2021 14:37:49 GMT
< Content-Type: application/json
< Location: todos/278
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Example Response body:

```json
{
  "id": 278,
  "title": "file paperwork today",
  "doneStatus": true,
  "description": ""
}
```

## Overview Video

{{<youtube-embed key="kfe7VtaV7u0" title="Solution to POST Todo in XML with response in JSON">}}

[Patreon ad free version](https://www.patreon.com/posts/53796838)




