---
title: Swapi - Star Wars API - Example Site
description: An overview of the Star Wars API application, explaining how to use it to learn about APIs and practice with API tooling.
---

In addition to our [API Challenges](/gui/challenges) you should practice on as many sites as possible. Try [Swapi](https://swapi.dev).

# Swapi.dev - A Web Application which uses API

- A Star Wars API App
- Only supports the GET verb... or does it?

## Exercise - Explore the API Via the APP

- [swapi.dev](https://swapi.dev)
    - make a request from the GUI
    - Use network tab to view the requests

## Exercise - Use the API

- Download a REST Client
- Make some API calls that copy what the UI Did

## Exercise - Explore the Docs

- [Swapi documentation](https://swapi.dev/documentation)
- Read the docs
- Use the docs as a basis for exploration
- use an HTTP client to issue requests e.g. Postman, cURL, Insomnia.rest
- try to generate different HTTP Status codes: 200, 400, 404, 405
- experiment with different HTTP Verbs: GET, POST, OPTIONS, HEAD

### HTTP OPTIONS Verb - Example swapi.dev

Swapi has a very unusual OPTIONS output

e.g. swapi.dev

OPTIONS - https://swapi.dev/api/people/1/

~~~~~~~~
{
    "name": "People Instance",
    "description": "",
    "renders": [
        "application/json",
        "text/html",
        "application/json"
    ],
    "parses": [
        "application/json",
        "application/x-www-form-urlencoded",
        "multipart/form-data"
    ]
}
~~~~~~~~

Hint: Swapi supports GET, OPTIONS and HEAD - try them all, then double check this statement. (Trust no-one).