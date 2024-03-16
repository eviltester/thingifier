---
title: API Challenges Solution For -  GET todo 404
description: How to solve challenge GET todo 404.
---

How to complete the challenge `GET /todo 404`.

## GET /todo (404)

> Issue a `GET` request on the `/todo` end point should 404 because nouns should be plural

- This will show you an error status code 404
- 404 means 'not found' i.e. we tried to access something that does not exist
- REST API Endpoints are usually plural e.g. `/todos` we would not normally expect an API to respond to both `/todos` and `/todo`

## Basic Instructions

- Issue a GET request to end point "/todo"
    - if running locally that would be
        - `http://localhost:4567/todo`
    - if running in the cloud that would be
        - `https://apichallenges.herokuapp.com/todo`
- The request should have an `X-CHALLENGER` header
- The response status code should be `404`

## Example Request

~~~~~~~~
> GET /todo HTTP/1.1
> Host: apichallenges.herokuapp.com
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

{{<youtube-embed key="gAJzqgcN9dc">}}

[Patreon ad free version](https://www.patreon.com/posts/41107933)


