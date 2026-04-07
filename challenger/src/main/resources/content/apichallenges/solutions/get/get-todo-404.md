---
title: API Challenges Solution For -  GET todo 404
seo_title: Solution: GET todo 404 Guide | API Challenges
description: How to solve challenge GET todo 404.
lastmod: 2026-02-18
seo_description: Use this walkthrough to solve GET todo 404 with request setup, key headers, and expected status codes so you can complete the challenge confidently.
next_challenge: /apichallenges/solutions/get/get-todos-id-200
schema_howto_steps: Create a GET request to /todo||Include X-CHALLENGER so the challenge is tracked in your current session||Send the request and verify the response status is 404
showads: true
---

# How to complete the challenge `GET /todo 404`.

How to solve challenge GET todo 404 by issuing a GET request on a non-existent endpoint and receive a 404 status code response.

## GET /todo (404)

> Issue a `GET` request on the `/todo` end point should 404 because nouns should be plural

- This will show you an error status code 404
- 404 means 'not found' i.e. we tried to access something that does not exist
- REST API Endpoints are usually plural e.g. `/todos` we would not normally expect an API to respond to both `/todos` and `/todo`

## Basic Instructions

- Issue a GET request to end point "/todo"
    - `{{<ORIGIN_URL>}}/todo`
- The request should have an `X-CHALLENGER` header
- The response status code should be `404`

## Example Request

~~~~~~~~
> GET /todo HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 404 Not Found
< Connection: close
< Date: Thu, 27 Aug 2020 13:46:19 GMT
< X-Challenger: x-challenger-guid
< Content-Type: text/html;charset=utf-8
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~



## Overview Video

{{<youtube-embed key="gAJzqgcN9dc" title="Solution to GET non-existent todo challenge">}}

[Patreon ad free version](https://www.patreon.com/posts/41107933)


