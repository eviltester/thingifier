---
date:  2021-07-24T08:30:00Z
title: API Challenges Solution For - forbidden secret note 403
description: How to solve API challenge 31 - forbidden to access secret note 403
---

# How to complete the unauthorized secret note challenge

When we are not authorized to access information we should receive a status code of 403 Forbidden.

## 	Authorization Challenge

Most of the challenges simply require the correct payload, and an X-Challenger header to track the session. The authentication challenges require an extra header, the value for which can only be obtained with a username and password. This value is obtained when completing [challenge post secret 201](/apichallenges/solutions/authentication/post-secret-201).

The `X-CHALLENGER` header authenticates you to access a specific set of secret notes, and the `X-AUTH-TOKEN` authorizes you to gain access.

- Authentication is "are you who you say you are" (`X-CHALLENGER`)
- Authorization is "do you have the right permissions" (`X-AUTH-TOKEN`)


## Challenge 31 Forbidden

> Issue a GET request on the `/secret/note` end point and receive 403 when `X-AUTH-TOKEN` does not match a valid token

- `GET` request means use the HTTP Verb GET
    - e.g. `GET /secret/note` sends to the secret note endpoint
- `X-AUTH-TOKEN` means include a header named `X-AUTH-TOKEN` in the message. The `X-` implies it is a non-standard custom header
- `does not match a valid token` means that the value in the header should be different from the value returned from the `secret/token` endpoint
- add the `X-CHALLENGER` header to track progress and because the authentication code we need is asociated with the `X-challenger` session
- Receive a 403 FORBIDDEN response because the authorization token does not match the token required to access the data

## Basic Instructions

- Create a new request for the `/secret/note` end point
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/secret/note`
- The verb should be a `GET`
- Add a custom header with the name `X-AUTH-TOKEN` the value should be different from the value returned in Challenge 30
- The request should have an `X-CHALLENGER` header to track challenge completion
- You should receive a 403 response - meaning you are not authorized


## Example Request

~~~~~~~~
> GET /secret/note HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> X-AUTH-TOKEN: bob
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 403 Forbidden
< Connection: close
< Date: Sat, 24 Jul 2021 16:18:40 GMT
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~


## Overview Video

{{<youtube-embed key="77mnUQezdas" title="Solution to Get Forbidden challenge">}}

[Patreon ad free version](https://www.patreon.com/posts/54065276)




