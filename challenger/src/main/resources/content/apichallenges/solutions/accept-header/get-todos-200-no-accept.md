---
date:  2021-05-29T10:32:00Z
title: API Challenges Solution For - GET todos No Accept Header 200
description: How to solve API challenge GET todos No Accept 200 to GET the todos with no accept header present.
---

# How to complete the challenge `GET /todos No Accept (200)`

Accept headers are optional. Most API clients will add one by default. But we do not need to pass in an `Acdept` header to successfully GET all the todos in JSON format. This challenge allows us to test this, to complete it we must ensure that we do not pass in an accept header.

## GET /todos No Accept (200)

When we issue a request with no accept header, we should receive the default from the server. But... sending a request without an Accept header might be harder depending on the tool we use.

> Issue a GET request on the `/todos` end point with no `Accept` header to receive results in JSON format.

- `GET` request will receive a response with all the todo items
    - e.g. `GET /todos` to get all the todo items
- `200` is a success code, in this case it means the end point exists and the `todo items were returned
- `No Accept` means that the request should not include an `Accept` header
- add the `X-CHALLENGER` header to track progress


## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/todos`
- The request should not have an `Accept` header at all
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `200` when all the details are valid.
- Check the body of the message has JSON format data, which is the default from the server
- Check the `content-type` header in the response has `application/json`

Sometimes it is useful to send our requests through a proxy or look at the debug output from our tool to make sure that we are sending the requests we expect to send.

A Proxy can also be used to amend the request and remove the headers.

cURL may provide more flexibility for doing this than other tools.

In Insomnia, right click on the [Send] button and Generate Client Code for cURL. Then amend the code to remove the header by adding `--header 'Accept:'`


## cURL Details

~~~~~~~~
curl --request GET \
  --url {{<ORIGIN_URL>}}/todos \
  --header 'X-CHALLENGER: x-challenger-guid'
  --header 'Accept:'
  -v
~~~~~~~~

Hints:

- add `--header 'Accept:'` to the generated code to remove Accept header from the request.
- add `-v` to the generated code if you want to see the full response output.


## Example Request

~~~~~~~~
> GET /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: curl/7.64.1
> X-CHALLENGER: x-challenger-guid
~~~~~~~~

## Example Response

~~~~~~~~ 
< HTTP/1.1 200 OK
< Connection: close
< Date: Sat, 29 May 2021 10:35:04 GMT
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Example Response body:

```json
{"todos":[{"id":280,"title":"install webcam","doneStatus":false,"description":""}]}
```


## Overview Video

{{<youtube-embed key="CSVP2PcvOdg" title="Solution to Get all Todos in defaulted format">}}

[Patreon ad free version](https://www.patreon.com/posts/51831718)




