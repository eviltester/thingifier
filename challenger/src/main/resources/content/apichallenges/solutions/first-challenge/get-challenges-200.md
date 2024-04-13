---
title: API Challenges Solution For - GET Challenges (200)
description: How to use a GET request with an x-challenger header to get the progress status of all the API Challenges
---

# How to complete the challenge `GET /challenges 200`.

How to use a GET request with an x-challenger header to get the progress status of all the API Challenges.

## GET /challenges (200)

> Issue a `GET` request on the `/challenges` end point

- This will show you the status of all the challenges in your REST Client, if you include an `X-CHALLENGER` guid header in your request.

## Basic Instructions

- Issue a GET request to end point "/challenges"
    - `{{<ORIGIN_URL>}}/challenges`
- The request should have an `X-CHALLENGER` header
- The response body shows the status of all the challenges.

## Example Request

~~~~~~~~
> GET /challenges HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Thu, 27 Aug 2020 13:38:45 GMT
< Content-Type: application/json
< Location: /gui/challenges/x-challenger-guid
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

## Example Response Payload

~~~~~~~~
{
  "challenges": [
    {
      "name": "POST /challenger (201)",
      "description": "Issue a POST request on the `/challenger` end point, 
            with no body, to create a new challenger session. 
            Use the generated X-CHALLENGER header in 
            future requests to track challenge completion.",
      "status": true
    },
    {
      "name": "GET /challenges (200)",
      "description": "Issue a GET request on the `/challenges` end point",
      "status": true
    }
  ]
}
~~~~~~~~

## Overview Video

{{<youtube-embed key="DrAjk2NaPRo" title="Solution to Get Challenges progress">}}

[Patreon ad free version](https://www.patreon.com/posts/41106708)

