---
date:  2021-07-25T08:30:00Z
title: API Challenges Solution For - unauthorized secret note 401
description: How to solve API challenge 32 - unauthorized to access secret note 403
---


# Hot to solve the Unauthorized challenge

This post and video shows how to complete the unauthorized secret note challenge, which returns a status code of 401 Unauthorized.

## 	Authorization Challenge

Most of the challenges simply require the correct payload, and an X-Challenger header to track the session. The authentication challenges require an extra header, the value for which can only be obtained with a username and password. This value is obtained when completing [challenge post secret 201](/apichallenges/solutions/authentication/post-secret-201).

The `X-CHALLENGER` header authenticates you to access a specific set of secret notes, and the `X-AUTH-TOKEN` authorizes you to gain access.

- Authentication is "are you who you say you are" (`X-CHALLENGER`)
- Authorization is "do you have the right permissions" (`X-AUTH-TOKEN`)


## Challenge 32 Unauthorized

> Issue a GET request on the `/secret/note` end point and receive 401 when no X-AUTH-TOKEN header present

- `GET` request means use the HTTP Verb GET
    - e.g. `GET /secret/note` sends to the secret note endpoint
- `no X-AUTH-TOKEN header present` means no custom header named `X-AUTH-TOKEN` should be added to the message
- add the `X-CHALLENGER` header to track progress
- Receive a 401 UNAUTHORIZED response because no authorization token is present

## Basic Instructions

- Create a new request for the `/secret/note` end point
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/secret/note`
- The verb should be a `GET`
- Ensure there is no custom header with the name `X-AUTH-TOKEN`
- The request should have an `X-CHALLENGER` header to track challenge completion
- You should receive a 401 response - meaning you are not authorized

## Example Request

~~~~~~~~
> GET /secret/note HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: insomnia/2021.2.2
> X-CHALLENGER: x-challenger-guid
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 401 Unauthorized
< Connection: close
< Date: Sun, 25 Jul 2021 10:42:36 GMT
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: 1x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~


## Overview Video

{{<youtube-embed key="__uZlQZ48io" title="Solution to Unauthorized Get challenge">}}

[Patreon ad free version](https://www.patreon.com/posts/54089275)




