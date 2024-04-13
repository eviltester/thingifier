---
date:  2021-07-17T10:45:00Z
title: API Challenges Solution For - POST todos create todo with XML
description: How to solve API challenge POST todos XML. Creating a todo with XML format.
---

# How to complete the challenge `POST /todos XML`

The `content-type` should match the data in the request. In this case we will send a POST request to create a todo item with body and response in XML format.

## 	POST /todos XML


To create and amend items in a REST API we usually use a POST request. POST requests are defined as 'partial' requests in that they don't need to have all the fields, but they need to have enough fields to be valid for the request.

> Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/xml`, and Accepting only XML ie. Accept header of `application/xml`

For this challenge we issue a request with an accept header specifying XML and we will send the message as XML so we set the `content-type` header as well.

- `POST` request means we will send information in the body of the message
    - e.g. `POST /todos` sends to the todos endpoint
- `create a todo` means that we will not include a todo id, so a new todo is created
- `Content-Type` `application/xml` means set the `content-type` header to `application/xml`
- `Accept header` of `application/xml` means set the `accept` header to  `application/xml`
- the body of the message will have to be a valid todo item, and we can see the format in the documentation, or by issuing a `GET` request on the `/todos` endpoint
- add the `X-CHALLENGER` header to track progress
- we know it has been created when we receive a `201` response
- the body of the response should contain the full details of the `todo` created


## Basic Instructions

- First issue a `GET` request on "/todos" with an `accept` header of `application/xml` to see the format of a todo in XML format
    - or read the documentation at [/docs](/docs)
    - copy a todo from the response to edit as payload for the `POST` message
- Issue a `POST` request to end point "/todos"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/todos`
- The request should have an `Accept` header with the value `application/xml`
- The request should have an `Content-Type` header with the value `application/xml`
- Use the `todo` that you copied from the `GET` request, remembering to remove the `id` because when we create a todo, it will be issued with an `id` automatically

We only need to use the minimum details, but could add a description if we wanted.

```xml
  <todo>
    <doneStatus>true</doneStatus>
    <title>file paperwork today</title>
  </todo>
```

- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `201` when all the details are valid.
- Check the body of the response has XML formatted 'todo' response
    - this will contain the full details of the todo we created
- Check the `content-type` header in the response has `application/xml` matching the requested accept format


## Additional Exercises

- you might want to experiment with adding additional fields like 'description', what happens if you miss out fields?
- check the `Location` header has the endpoint we can use to retrieve the todo, issue a `GET` on that endpoint to retrieve the details of the todo



## Example Request

~~~~~~~~
> POST /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/xml
> Accept: application/xml
> Content-Length: 93

|   <todo>
|     <doneStatus>true</doneStatus>
|     <title>file paperwork today</title>
|   </todo>
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 201 Created
< Connection: close
< Date: Sat, 17 Jul 2021 13:14:28 GMT
< Content-Type: application/xml
< Location: todos/226
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
  <id>226</id>
  <title>file paperwork today</title>
</todo>
```


## Overview Video

{{<youtube-embed key="2-KBYHwb7MM" title="Solution to XML content-type challenge">}}

[Patreon ad free version](https://www.patreon.com/posts/53794821)




