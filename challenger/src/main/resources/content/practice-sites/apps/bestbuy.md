---
title: Best Buy API Playground - An old version of the Best Buy API - Practice API
description: Best Buy API Playground is an old version of the Best Buy API. Notable for comprehensive query filter operations.
showads: true
---

In addition to our [API Challenges](/gui/challenges) you should practice on as many sites as possible. Try [BestBuy API Playground](https://github.com/BestBuy/api-playground).

# Best Buy API Playground - A Practice GET API

## About Best Buy

The Best Buy API Playground is a node application which implements an older version of the Best Buy API. It comes pre-populated with production-like data.

Since this is a practice version of the API, update and delete operations are enabled in addition to the expected `GET` verbs. These would not be available in the production version of the API.

Summary:

- Best Buy API Playground is a full API that can be run locally using Node or Docker
- Full range of CRUD operations is supported using `GET`, `POST`, `PATCH`, `DELETE`
- Large set of data is provided
- Swagger UI is available
- Open API Definition is downloadable

## Running The Application Using Node

If you have node installed then the API can best started by cloning the repository and using `npm`.

Full instructions are on the [GitHub page](https://github.com/BestBuy/api-playground), copied here for ease of reference:

```
git clone https://github.com/bestbuy/api-playground/
cd api-playground
npm install
npm start
```

The Best Buy API Playground will now be accessible on `http://localhost:3030`

I personally prefer to run the application using Docker as described below.

## Running The Application Using Docker

Create the Docker image locally:

```
git clone https://github.com/BestBuy/api-playground
cd api-playground
docker build --tag bestbuy .
```

Then run the Docker image, making sure you map the exposed port `3030`:

```
docker run -d --name=bestbuy-api-playground -p 3030:3030 "bestbuy"
```

I map the port to `3030` on localhost to make the Swagger documentation URL work with the default values.

## Documentation End Points

You can then access:

- the basic api documentation at
   - `http://localhost:3030`
- A Swagger UI Front End at
   - `http://localhost:3030/docs/`
- A Swagger API Definition file at
   - `http://localhost:3030/swagger.json`
- A PostMan collection has been bundled with the app at
   - `http://localhost:3030/postman/API.postman_collection.json`
- The Queries documentation shows some examples of queries to use on the API. This is useful because the GET API supports a complex set of query filters using SQL-like operators.
   - `http://localhost:3030/queries` 

## API Endpoints

The application is bundled with sample data so you will be able to query the data on the back end immediately.

The main endpoints are:

- `/products` - products available in the database.
- `/categories` - product categories and their subcategories/path.
- `/stores` - returns a list of Best Buy store locations.
- `/services` - returns a list of services available at Best Buy stores.
- `/version` - returns the current version of the application.
- `/healthcheck` - returns information about the application's health.


## Exercises

### Exercise - `GET` all the main endpoints

- Issue a GET request on each of the top level endpoints listed above
- Initially do this using the Swagger UI `http://localhost:3030/docs/`
- Repeat the exercise using a REST API Client to gain more control. You can download the Swagger file to pre-populate the requests in your REST Client.

### Exercise - Explore the Example Query Formats

- Use the list of Query formats on the Query Example page
    - `http://localhost:3030/queries` 
- Initially do this by clicking on the links
- Copy the URLs into your REST client and repeat the exercise
- Amend Query parameters to explore the results

### Exercise - Create and Update the Data

- Explore the endpoints to create and amend the data in the backend
- Use `OPTIONS` do check if the endpoints honour the verbs listed

### Exercise - Does the API only support JSON?

- The response format can sometimes be influenced using the `accept` header to tell the server that the client will accept a different format for the response.
- By default the server responds with `JSON`.
- What happens if you ask the server for `application/xml`?
- What other formats could you ask for?

## Some Observations

### Create and Updating Data

- When creating data I noticed that the system was fussy around headers, but in a misleading way. Sending through a valid payload in 'text/plain' format I received the information that it `should have required property 'name'` rather than content-type not accepted.
- The server checks payloads for extra and unexpected fields.
- Server responds with all validations that the request fails, not just the first one, which is useful for debugging requests.
- Duplicate fields are allowed with the last one being accepted
- JSON validation fails with a `500` error rather than a `400` error suggesting that a general exception is thrown but not trapped to return a `400` (client) error. NOTE: the swagger documentation suggests that a `400` will be returned
- The swagger documentation did not show the field validation rules so I experimented to find validation lengths. And then used an online counterstring tool to check the length calculations [counterstring generator](https://eviltester.github.io/TestingApp/apps/counterstrings/counterstrings.html)
- Service names do not need to be unique
- Trying to amend an existing item using `POST` returned a `404` rather than a `405 Method Not Allowed`, which also contradicts the methods listed when I asked for `OPTIONS`

### `accept` Header

- The API examples are all `JSON` so I tried asking the API for `application/xml` I received a `500` error. I rather expected a `400` error because the request was asking for something the API did not support, rather than the server throwing a `500` General Error.
- Since the `xml` request failed I wondered what would happen with a `text/html` response and I found it interesting that the server did actually respond with an error in HTML format.