---
title: Simple API - A Multi-verb API with no security requirements - Practice API
description: An overview of the Simple API practice API site, explaining how to use it to learn about APIs and practice with API tooling.
showads: true
---

In addition to our [API Challenges](/gui/challenges) you should practice on as many sites as possible. Try [SimpleAPI](/practice-modes/simpleapi).

# Simple API - A functional API that supports GET and Update

Simple API is a fully functional API allowing you to retrieve and update from the server.

## About Simple API

Simple API is a fully functional API allowing you to retrieve and update from the server. It is suitable as an entry level practice API because it requires no authentication and there is no risk of exposing private data.

The API allows access to a store inventory `item` entity which has:

- `id` - an auto generated number id
- `isbn` - a 13 digit numeric value that must be unique
- `type` e.g. `cd`, `dvd`, `book`, `blu-ray`
- `price`
- `numberInStock`

The API refreshes when there are a small number of records so that there is always content in the API to work with.

Only 100 items can be stored in the API at any point in time.

All of the fields of the 'item' are numeric or enum, so there is no chance of encountering any risky user generated data or to enter any personal data.

The only complexity involved is generating an ISBN that matches the format required.

There is an endpoint in the API to return a random ISBN `/simpleapi/randomisbn` or you can use the Button below to generate a random data value suitable for using in the API.

{{<PARTIAL_SNIPPET filename="partials/generate-random-isbn.html">}}

Two OpenAPI files are available for the API, one with validation, one without validation. These can be imported to a REST client or a Swagger UI to help you get started with the API.

The API Supports two main endpoints:

- `items`
- `item`

Verbs supported are:

- `GET` at an `/items` level to see all items
- `POST` at an `/items` level to create an item
- `GET` a specific item with id e.g. `/items/1`
- `PUT` and `POST` to update a specific item
- `DELETE` to delete a specific item e.g. `/items/1`

The API is useful for getting used to creating, updating, reading and deleting entities from an API.

The API also supports both XML and JSON payloads by amending the `content-type` and `accept` headers in the request.

## Links

- The main site [SimpleAPI](/practice-modes/simpleapi)
- The documentation [SimpleAPI/docs](/simpleapi/docs)
- Data viewer for current items [SimpleAPI/gui/entities](/simpleapi/gui/entities)
- OpenAPI Files [simpleapi-openapi](/practice-modes/simpleapi-openapi)

## Summary

- An API for JSON and XML requests and responses.
- A simple data structure to avoid personal data
- No Authentication required for updates
- Update requests are supported (`POST, PUT, DELETE`)


## Exercises

Coming Soon.


 