---
title: Device Registry - A simple API with authentication - Practice Test API
description: Device Registry is a simple API with endpoints protected by basic authentication.
showads: true
---

In addition to our [API Challenges](/gui/challenges) you should practice on as many sites as possible. Try [Device Registry](https://github.com/AutomationPanda/device-registry-fastapi).

# Device Registry - Simple Authenticated API

## About Device Registry

[Device Registry](https://github.com/AutomationPanda/device-registry-fastapi) is an API to simulate a a management system for devices, it is written in Python and can be run locally if you have Python installed. To make it easy to run, we have also created a Docker file that makes it easy to get started.

The API has a Swagger UI and a Redoc UI.

Summary:

- [Device Registry](https://github.com/AutomationPanda/device-registry-fastapi)
- Can be run locally if you have Python installed.
- I created a Docker file which can run the application if you use Docker [here](https://github.com/eviltester/thingifier/tree/master/docker)
- Has a Swagger UI to allow easy exploration of the API
- Has a Redoc UI for an additional UI based exploration of the API
- API calls are authenticated using two hard coded user/password combinations: `engineer` : `Muh5devices`, and `pythonista` : `I<3testing`,


## Running The Application

The repo provides instructions for running locally with Python, additionally we created a Docker file to make running the app easier.

### Using Python

Instructions for running the application are available in the project readme.

- [github.com/AutomationPanda/device-registry-fastapi](https://github.com/AutomationPanda/device-registry-fastapi)

An earlier version of the application using flask is also available written using flask, but this guide covers the fastapi version:

- [Device Registry Service](https://github.com/AutomationPanda/device-registry-flask)

### Using Docker && cURL

Create the Docker image locally by downloading our Dockerfile for the project or cloning the apichallenges github repo.

#### Download just the Docker file

Create the Docker image locally by downloading our Dockerfile for the project:

```
mkdir fxtradehub
cd fxtradehub
curl https://raw.githubusercontent.com/eviltester/thingifier/refs/heads/master/docker/device-registry/Dockerfile -o Dockerfile
docker build --tag devregfastapi .
```

Then you can run the image:

```
docker run -it --name=devregfastapi -p 8000:8000 devregfastapi
```

And visit:

- `http://localhost:8000/docs` to see the Swagger Docs UI.
- `http://localhost:8000/redoc` to see the Redoc Documentation UI.

#### Cloning the API Challenges repo

Create the Docker image locally by downloading our Dockerfile for the project:

```
git clone https://github.com/eviltester/thingifier
cd thingifier
docker build -t devregfastapi -f ./docker/device-registry/Dockerfile .
```

Then you can run the image:

```
docker run -it --name=devregfastapi -p 8000:8000 devregfastapi
```

- `http://localhost:8000/docs` to see the Swagger Docs UI.
- `http://localhost:8000/redoc` to see the Redoc Documentation UI.


## API Endpoints

The API is a CRUD interface using verbs: `GET`, `POST`, `PUT`, `DELETE`, `PATCH`.

The main endpoints:

- `/authenticate` - used to get a bearer token
- `/devices` - main endpoint for CRUD requests for a `device` entity
- `/devices/{id}/report` - return a text report of the given device
- `/status` - return the status of the service

Most of the endpoints are authenticated.

The API supports Create, and Update using `POST`, `PUT`, and `PATCH`:

- `POST` is used to create items
- `PUT` will update an item using a full payload
- `PATCH` will update an item using a partial payload

You can either use Basic Authentication using either of the two configured users:

- `engineer` : `Muh5devices`
- `pythonista` : `I<3testing`

Or make a basic authenticated `GET` request on the `/authenticate` api to receive a bearer token which can be used in requests.

## Exercises

A set of suggested exercises to explore the API.

### Exercise - Use the Redoc UI to Explore the Documentation

- The Redoc UI `http://localhost:8000/redoc` will allow you to see the documentation and examples for the endpoints
- You can not use the UI to issue requests, but it can be helpful when exploring the API in another REST Client to copy and paste endpoint URLs or payload data


### Exercise - Use the Swagger UI to Explore the Functionality

- The Swagger UI `http://localhost:8000/docs` will allow you to issue the accepted requests for the API
- You can use the `/status` endpoint without any authentication
- Check that the other endpoints require authentication
- Add a username and password as the HttpBasic authentication details in the `[Authorize]` dialog and then issue requests 
- Use the Swagger UI to issue `GET` requests and explore the data
- Use the Swagger UI to amend the data in the system.


### Exercise - Generate a Bearer token and use this as the authentication mechanism

- Issue either a `GET` or `HEAD` request on the `/authenticate` endpoint
- Copy the bearer token into the `[Authorize]` dialog, remember to remove the username and password
- Check that you are able to make the same authenticated requests that you could using the HttpBasic authentication approach.
- Did you see what happens if you use both username/password and bearer token?

### Exercise - Use a REST Client to Explore the API

- Having gained familiarity with the normal usage of the API, use a REST client to explore the API in more depth.
- There are links to an OpenAPI spec from either of the documentation interfaces


### Exercise - Explore the Validation

- The API documentation does not describe the validation rules, try and trigger validation errors.

### Exercise - Explore Amending Values

- Use the `POST` verb to amend existing entities, does it work, or is it only for creating entity instances?
- Use the `PUT` and `PATCH` verbs to explore the different amendment approaches (full, partial)

### Exercise - Explore the Security

- There are two users in the test database. Each user is only allowed to access their content.
- Check if all the verbs and endpoints are limited to each user's data.

## Some Observations

Some observations made when I followed the sample exercises. It is generally recommended that you perform the exercises first, prior to reading this information.


### Exploring the Redoc UI

- There is no information on lengths or content, perhaps there is no validation?
- The report endpoint says it returns text, but the endpoint spec says it returns json - test this early.

### Explore API using Swagger

- Validation is performed by the swagger UI, really need to use REST API to test the validation
- Swagger does not allow using both HttpBasic and Bearer token - need to try that in a REST Client.
- Type returned by `/report` is `text/plain` and not `application/json` as suggested by the docs

### Exploring Validation

- Was able to trigger validation on 'extra' fields e.g. trying to create device with id and owner, adding an unsupported field e.g. "bob"
- All fields except owner and id seem to be mandatory on creation
- There are errors reported for JSON validation
- Types are not enforced, data is auto converted to String e.g. "name": 2
- Duplicate fields - last one is chosen
- No duplicate value checks e.g. serial number can be duplicated
- Seems to be no validation other than - is it convertable into a String - so even `""` is allowed
- Since the `/device/{id}/report` uses the `name` as the 'filename' it is possible to create items with names which are not allowed as filenames and Swagger fails to return them e.g.  `"name": "\\, /, :, *, ?, \", <, >, I"` - the response has invalid filename format `filename="\, /, :, *, ?, ", <, >, I.txt"` - I had to put this through a proxy to be sure - this will break some clients that try to parse the headers e.g. the browser for swagger
- There is method validation e.g. I can't `POST` to `/devices/1`

### Exploring Security

- When not authenticated the system responds with `unauthenticated`
- When authenticated the system leaks information about the other users' content e.g. `GET /devices/3` results in `"Forbidden"` so I know that some user has a device with `id = 3`, but I don't know who.

