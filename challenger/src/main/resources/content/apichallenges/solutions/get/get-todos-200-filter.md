---
date:  2021-01-30T09:00:00Z
title: API Challenges Solution For - GET todos 200 filter
description: How to solve API challenge GET todos 200 filter to use URL parameters to filter the results.
---

# How to complete the challenge `GET /todos (200) ? filter`

How to issue a GET request on a top level entity endpoint and use a query filter to receive a subset of data.

## GET /todos (200) ? filter

> 	Issue a GET request on the `/todos` end point with a query filter to get only todos which are 'done'. There must exist both 'done' and 'not done' todos, to pass this challenge.

- `GET` request will return all items from the `/todos` end point
- `200` is the success code meaning the request was accepted
- `?` means we need to add a URL Parameter
- `filter` means it will filter based on an attribute. In this case we are asked to filter on those which are 'done'. And this is represented by the `doneStatus` i.e. `doneStatus=true`
- We are using this request to `GET` a filter list of todo items
- Perform a `GET` first to see what the format of the message is
- Add a URL parameter to the request to repeat the `GET` and filter the list of todos e.g. `/todos?doneStatus=true`

## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- Look at the returned format for todos

```js
    {
      "id": 41,
      "title": "create todo process payroll",
      "doneStatus": true,
      "description": ""
    },
```
- we want to use the `doneStatus` attribute as a URL parameter
- if you don't see any todos in the list with a `"doneStatus": true` then you will need to issue a `POST` request to create or amend a todo item. e.g. [challenge post secret 201](/apichallenges/solutions/authentication/post-secret-201)
- Issue a `GET` request with a URL parameter `/todos?doneStatus=true`
- The response status code should be `200` because the request is accepted
- If you get a different response code, check the URL or headers of the message because you made have made a typo.
- If you don't see any todos returned then you may need to create one e.g. [challenge post secret 201](/apichallenges/solutions/authentication/post-secret-201)

## Example Request

~~~~~~~~
> GET /todos?doneStatus=true HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: application/json
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Tue, 15 Dec 2020 17:32:12 GMT
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Returned body:

```json
{
  "todos": [
    {
      "id": 41,
      "title": "create todo process payroll",
      "doneStatus": true,
      "description": ""
    }
  ]
}
```

## Overview Video

{{<youtube-embed key="G-sLuhyPMuw" title="Solution to Get todos with query filter challenge">}}

[Patreon ad free version](https://www.patreon.com/posts/46603286)

