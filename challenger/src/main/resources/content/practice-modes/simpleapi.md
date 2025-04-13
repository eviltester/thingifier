---
title: API Challenges Simple API
description: The Simple API is a multi-user REST API that you can use to practice testing without any authentication.
---



# Simple API

The API Challenges Simple API is an easy-to-use API where you can GET, DELETE, PUT and POST without any authentication.

## Overview of Simple API

{{<youtube-embed key="EBXSJ0C2j5I" title="Simple API Overview">}}


[Patreon ad free video](https://www.patreon.com/posts/126496992)

## About Simple API

To help you get started with API testing and practice using your tools, we have created the Simple API.

The Simple API has a single end point `/simpleapi/items` and you can `GET`, `DELETE`, `PUT` and `POST` without
requiring any authentication or authorization, making Simple API a recommended first step in your API Testing learning journey.

Data will refresh automatically when low, and there is a limit to the number of items that can be added.

Because the API has no unstructured text fields e.g. `description`, there is no way to add any potentially offensive
or personal information.

To create a new item you need to add a unique `ISBN`. We have added an endpoint to generate a random ISBN `/simpleapi/randomisbn`.

Or you can click the button below and copy and paste the value into your APi call.

{{<PARTIAL_SNIPPET filename="partials/generate-random-isbn.html">}}

## Why did we create this?

We noticed that most APIs, including our API Challenges, require some sort of authentication to use the full capabilities
of the API, and we wanted to make learning easier.

We wanted to put no barriers between yourself and your learning how to use APIs.

The [documentation](/simpleapi/docs) explains the data formats and the validations. Additionally, you can download an
Open API Swagger File to load into your API tool of choice and start testing straight away.

## Swagger OpenAPI File

You can download a Simple API Swagger [OpenAPI File for Simple Api](/practice-modes/simpleapi-openapi).