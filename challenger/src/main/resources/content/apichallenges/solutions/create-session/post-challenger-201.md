---
title: API Challenges Solution For - POST Challenger (201)
description: How to use a POST request to create a Challenger session and start using the API challenges
---

# POST Challenger 201 Solution

How to complete the challenge `POST /challenger 201`.

## POST /challenger (201)

> Issue a POST request on the `/challenger` end point, with no body, to create a new challenger session. Use the generated X-CHALLENGER header in future requests to track challenge completion.

- This challenge is essential if you want to persist your sessions in multi-user mode
- This challenge is optional if you want to work in single-user mode

## Basic Instructions

- Issue a POST request to end point "/challenger"
   - `{{<ORIGIN_URL>}}/challenger`
- The response will have an `X-CHALLENGER` header
- Use this in any future requests to track your progress
- The `LOCATION` header has a url to access your challenge status through the GUI

## Example Request

~~~~~~~~
POST /challenger HTTP/1.1
Host: localhost:4567
User-Agent: rest-client
Accept: */*
Content-Length: 0
~~~~~~~~

## Example Response

~~~~~~~~
HTTP/1.1 201 Created
Date: Tue, 28 Jul 2020 14:26:48 GMT
X-CHALLENGER: rest-api-challenges-single-player
Location: /gui/challenges
Content-Type: text/html;charset=utf-8
Transfer-Encoding: chunked
Server: Jetty(9.4.z-SNAPSHOT)
~~~~~~~~

## Overview Video

{{<youtube-embed key="tNGuZMQgHxw" title="Solution to create challenge session">}}

[Patreon ad free version](https://www.patreon.com/posts/39882254)

