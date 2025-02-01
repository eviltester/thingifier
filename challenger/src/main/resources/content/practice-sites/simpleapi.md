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

I've created a set of suggested exercises in case you need some prompting for practice.

### Exercise - Explore the Data in the Backend

The SimpleAPI has a Data Explorer front end so that you can view the data in the backend.

- view the data to get a feel for the Entity that is used
- [apichallenges.eviltester.com/simpleapi/gui/instances](https://apichallenges.eviltester.com/simpleapi/gui/instances?entity=item&database=__default)

### Exercise - Read the Documentation

The Simple API has:

- An [About Page](https://apichallenges.eviltester.com/practice-modes/simpleapi)
   - There is an ISBN generator here which might help when creating requests in the API to create and amend items
- [Documentation](https://apichallenges.eviltester.com/simpleapi/docs)

### Exercise - try the Open API files in a Swagger UI

- Any Swagger UI can be used to load in the Open API files e.g.
- Open the [PetStore Swagger UI](https://petstore.swagger.io/)
- Paste into the url `[Explore]` field either of the [Simple API OpenAPI](/practice-modes/simpleapi-openapi) Urls
   - Normal `https://apichallenges.eviltester.com/simpleapi/docs/swagger`
   - Permissive `https://apichallenges.eviltester.com/simpleapi/docs/swagger?permissive`
- Try each of the API files and see what difference they make to your testing e.g. the Permissive files does not have validation on the input parameters so you can explore more conditions.
   - OpenAPI files are designed for usage, not for testing, you should not rely on a Swagger UI for testing purposes.

### Exercise - Explore the `/items` endpoint

- The main endpoints `/items` will list all of the items in the system and allow creation of a new item
- Explore these with `GET`, `OPTIONS`, `HEAD`
- Try to create an item using `POST` on `/items`
   - if you don't know the format of the payload then copy one of hte item objects listed in the response of `GET /items`
   - you should not include an `id` in the `POST` request
   - the `isbn` field will need to be unique, you can generate a random `isbn` on this page or the [About](/practice-modes/simpleapi) page


### Exercise - Explore the `/items/{id}` endpoint

- The individual endpoint `/item/{id}` will allow you to work with a single item using its id
- Try to amend an item with `POST` on `/items/{id}` for an item that exists
- Try to amend an item with `PUT` on `/items/{id}` for an item that exists
- Try to delete an item with `DELETE` on `/items/{id}` for an item that exists
- Check that the API responds with `404` when the item id does not exist

### Exercise - Explore the endpoints with all verbs

- CRUD operations are based around the `GET`, `PUT`, `POST` and `DELETE` verbs
- Make sure you call the endpoints with other HTTP verbs e.g. `PATCH` and `TRACE`

### Exercise - Explore the `/items/{id}` field validations

- When you create or amend an item, check that the system enforces the validations for the fields listed in the documentation and the Open API spec
   - The ISBN must be unique in the system
   - The ISBN must match a specific format, try the different variations of the format
     - e.g. `384252925993-2`, `618-3051270614`, `9114-91-557340-1`, `2799488037490`
     - find more matching variations than the examples above
     - an easy only tool for generating values from Regex can be found [here](https://onlinetools.com/random/generate-random-data-from-regexp)
   - Check the field validations of the other fields as well

### Exercise - Explore the system data population validations

- The documentation says that there can be only 100 items, is that true?
- What is the minimum number of items that can be in the system? Can it be brought down to 0, 1, 2, 3, ...?

### Exercise - Explore the random ISBN endpoint

- There is an endpoint for generating random ISBNs  `GET /simpleapi/randomisbn`
- Does it work they way you would expect?

### Exercise - Explore the `accept` header

- The Simple API supports both `JSON` and `XML`
- You should be able to request `XML` as the response payload by using an `accept` header with the value `application/xml`
- Do all endpoints and calls support this?

### Exercise - Explore the `content-type` header

- The Simple API supports both `JSON` and `XML`
- You should be able to send request payloads as `XML` by using a `content-type` header with the value `application/xml`
- Do all endpoints and calls support this?
- Do you think you could mix and match `content-type` and `accept` so that you can send `JSON` and receive `XML`?
- Could you send `XML` and receive `JSON`?