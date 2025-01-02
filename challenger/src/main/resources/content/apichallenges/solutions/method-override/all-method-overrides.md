---
date:  2025-01-01T14:54:00Z
title: API Challenges Solution For - Method Override Challenges
description: How to solve API challenges for Method Override DELETE, PATCH, TRACE.
showads: true
---

# How to complete the HTTP Method Override Challenges

All of the method override challenges use the same mechanism so we can cover them all in this solution.

Sometimes tools and libraries will not issue TRACE or PATCH requests. There is a specific HTTP header we can use to try and have POST requests treated as other verbs.

The header "X-HTTP-Method-Override" is not guaranteed to work on every server, but some HTTP servers will take this header and treat the request using the value in the header:

`X-HTTP-Method-Override: DELETE`

This is worth understanding because it might also be used to bypass validation, or trigger functionality that the user is not authorized to trigger.


## POST /heartbeat

> 	Issue a `POST` request to `/heartbeat` with an `X-HTTP-Method-Override` header specifying the verb you actually want

- `POST` request can be sent by all tools
- We need to add the header `X-HTTP-Method-Override` to the request and the value should be the verb we want to send e.g. `TRACE`


## Basic Instructions

Each challenge requires a different verb, but the process is the same for each, the only difference is the value of the `X-HTTP-Method-Override` header

- Issue a POST request to end point "/heartbeat"
- The request should have an `X-HTTP-Method-Override` with the value associated with the challenge i.e. `DELETE`, `PATCH`, `TRACE`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should match the value for teh challenge overridden verb
   - for `DELETE` be `405`
   - for `TRACE` be `501`
   - for `PATCH` be `500` as the API is simulating a server error

NOTE: This header feature is normally implemented by the HTTP server so often development teams are not even aware that this is possible. Depending on how requests are validated in code it might be possible for someone, who has amend access using `POST` but who does not have `DELETE` access, to be able to use this header approach to delete something.

NOTE: As an additional exercise, you might want to see if you can DELETE todos using a POST and the `X-HTTP-Method-Override` header. Experiment and see what you can achieve using this approach.

## Example Request

~~~~~~~~
> POST /todos/3 HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-HTTP-Method-Override: DELETE
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/json
> Accept: */*
> Content-Length: 108
~~~~~~~~





