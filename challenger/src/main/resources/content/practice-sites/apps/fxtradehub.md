---
title: FX Trade Hub- A Productivity GTD App - Practice Web App and API
description: Tracks is a mature application implementing the Getting Things Done productivity method with a Web GUI and API.
showads: true
---

In addition to our [API Challenges](/gui/challenges) you should practice on as many sites as possible. Try [FX-TradeHub](https://github.com/sd576/FX-TradeHub-API).

# FX Trade Hub - A Web Application with API


## About FX Trade Hub

[FX-TradeHub](https://github.com/sd576/FX-TradeHub-API) is an API to simulate a trading system. It is written for Node and can be run locally if you have node installed. To make it easy to run, we have also created a Docker file that makes it easy to get started.

The API has a Swagger UI.

Summary:

- [FX-TradeHub](https://github.com/sd576/FX-TradeHub-API)
- Can be run locally if you have Node installed.
- I created a Docker file which can run the application if you use Docker [here](https://github.com/eviltester/thingifier/tree/master/docker)
- Has a Swagger UI to allow easy exploration of the API


## Running The Application

### Using Node

Instructions for running the application are available in the project readme.

- [github.com/sd576/FX-TradeHub-API](https://github.com/sd576/FX-TradeHub-API)


### Using Docker && cURL

Create the Docker image locally by downloading our Dockerfile for the project or cloning the apichallenges github repo.

#### Download just the Docker file

Create the Docker image locally by downloading our Dockerfile for the project:

```
mkdir fxtradehub
cd fxtradehub
curl https://raw.githubusercontent.com/eviltester/thingifier/refs/heads/master/docker/fxtradehub/Dockerfile -o Dockerfile
docker build --tag fxtradehub .
```

Then you can run the image:

```
docker run -it --name=fxtradehub -p 3000:3000 fxtradehub
```

And visit `http://localhost:3000/api-docs` to see the Swagger Docs UI.

#### Cloning the API Challenges repo

Create the Docker image locally by downloading our Dockerfile for the project:

```
git clone https://github.com/eviltester/thingifier
cd thingifier
docker build -t fxtradehub -f ./docker/fxtradehub/Dockerfile .
```

Then you can run the image:

```
docker run -it --name=fxtradehub -p 3000:3000 fxtradehub
```

And visit `http://localhost:3000/api-docs` to see the Swagger Docs UI.


## API Endpoints

The API is a CRUD interface using verbs: `GET`, `POST`, `PUT`, `DELETE`.

The top level url path for the api is `/api`.

Then sub paths for the endpoints:

- `/counterparties`
- `/counterparties/{id}`
- `/settlements`
- `/settlements/{counterpartyId}`
- `/settlements/{counterpartyId}/{currency}`
- `/trades`
- `/trades/{id}`


eg. `GET http://localhost:3000/api/counterparties`

It is a fun API to test. It is still in its early stages, and as it is a hobby project test API... expect to be able to find bugs.

## Exercises

A set of suggested exercises to explore the API.

### Exercise - Use the Swagger UI to Explore the Data

- The Swagger UI `http://localhost:3000/api-docs` will allow you to issue the accepted requests for the API
- Use the Swagger UI to issue `GET` requests and explore the data
- Use the Swagger UI to amend the data in the system.
- Read the Swagger UI documentation carefully and see if you spot any issues

### Exercise - Use a REST Client to Explore the API

- Having gained familiarity with the normal usage of the API, use a REST client to explore the API in more depth.
- When calling `OPTIONS` does the endpoint honour all the verbs listed?


### Exercise - Explore the Validation

- The API documentation does not describe all the validation rules, try and trigger validation errors.
- Vary the input data to see which fields have domain validation rules and which do not e.g. does an email have to be an email?
- Identify which fields need to be present and which are optional.
- Are you happy with all the field validations?

### Exercise - Explore Amending Values

- Use the `PUT` verb to amend existing entities
- When an amend payload using `PUT` is accepted, does it always amend the entity correctly?
- Any fields that you identified as optional during creation. Can you amend an existing entity with those values to make the field value optional?


## Some Observations

These endpoints were made at a specific point in time. The system may have changed since, and you might not be able to repeat the observations below. They are provided here to give you some insight into the type of testing I performed.

### Swagger UI

- When reviewing the Swagger documentation I spotted some inconsistencies in the status code descriptions with the endpoint e.g. `/counterparties` would never return a `404` for a missing counterparty even though this is suggested by the documentation.

### Exploring the API

- I found it interesting that the ID 'looks like' it should be an integer `1` but is actually a string `001` and I had to use the full `001` to access specific items. 
- Creating data was interesting. When experimenting with APIs I tend to `GET` and then use the payload in a `POST` to try and create data. But in this instance when creating a counterparty the phone number would not validate as the returned number `+44 20 7116 1000` exceeded the maximum length of 15.
- I like to call endpoints with verbs which are not listed as valid to see the response. Ideally I'll see `405 Method not allowed` rather than a `404`

### Exploring the Validation

- I tend to check for handling of invalid request formats, not just field formats. Ideally I expect to see a message about malformed JSON with a `400` response as `500` errors can be harder to diagnose.
- Some fields validate as requiring a value and it is not possible to amend them to be `null`, although it is possible to create them as `null`.
- The validation on the primary key allowed me to create an empty or null id, and then subsequently would not be able to amend or delete this entity.
- I like to create entities with minimum payloads to see if the system tells me what should exist, the error report for invalid counterparty items with not enough fields seems to be "Counterparty with this ID already exists"

### Exploring the Amendment

- When amending I like to check if I can amend fields to be `null`, particularly with a `PUT` as I view a `PUT` as overwriting and not a partial update. For this API implementation `PUT` is treated as a partial update and if a field is missing it is not updated on the backend.