---
title: Swapi - Star Wars API - Example Site
description: An overview of the Star Wars API application, explaining how to use it to learn about APIs and practice with API tooling.
showads: true
---

In addition to our [API Challenges](/gui/challenges) you should practice on as many sites as possible. Try [Swapi](https://swapi.dev).

# Swapi.dev - A Web Application which uses API

Swapi is an API that returns data about Star Wars characters, planets and starships.

## About Swapi

Swapi is a Star Wars API that is suitable for beginners to API testing. It can be explored without installing any tools and is well documented.

The [main page](https://swapi.dev) describing the API has an input form that you can use to work with the API without having to install any tools. If you do use the front end system rather than a REST API Client, you will have to use the Browser Network tab to see the headers of the response.

The main site suggests that there are only 3 endpoints:

- `/people`
- `/planets`
- `/starships`

Each of the endpoint urls accept the ID of the entity to access the information e.g.

- `/people/1`

Each of the endpoints also support a `/schema` path which the documentation says returns a definition of the response.

Looking through the [Documentation](https://swapi.dev/documentation) we can see that there are actually more endpoints e.g. films, vehicles, species, planets.

There are also URL parameters that can be explored:

- `?format=wookiee`
- `?search=text`

For extra fun, the documentation page lists different implementations of the API in other languages. One exercise, could be to compare the different implementations. You might even automate the main implementation and then try an repeat the automated execution against a different implementation to see how they compare.

The application also has a 'secret' UI when you make API calls from the Browser URL where you can issue OPTIONS, GET and can see the response headers e.g. use https://swapi.dev/api/people/1 in the browser URL

## Links

- The main site [swapi.dev](https://swapi.dev)
- The documentation [swapi.dev/documentation](https://swapi.dev/documentation)
- The source code [github.com/Juriy/swapi](https://github.com/Juriy/swapi)

## Summary

- A Star Wars API App
- A GET based API (no updates allowed) with a variety of data end points
- All endpoints return star wars information
- The UI can help you get started making requests without any tools, but make sure to use a REST Client on the API to explore fully.
- HEAD and OPTIONS are also supported
- Only supports the GET verb... or does it?
- Make sure you read the documentation to find all the features
- There is no OpenAPI specification file

## Exercises

I've created a set of suggested exercises in case you need some prompting for practice.

### Exercise - Explore the API Via the APP

- [swapi.dev](https://swapi.dev)
    - make a request from the GUI
    - Use network tab to view the requests and responses

### Exercise - Use the Built-in REST Interface in the Browser

- visit https://swapi.dev/api/people/1
- explore the functionality of the different UI

### Exercise - Use the API

- Download a REST Client
- Make some API calls that copy what the UI Did

### Exercise - Explore the Docs

- [Swapi documentation](https://swapi.dev/documentation)
- Read the docs
- Use the docs as a basis for exploration
- use an HTTP client to issue requests e.g. Postman, cURL, Insomnia.rest
- try to generate different HTTP Status codes: 200, 400, 404, 405
- experiment with different HTTP Verbs: GET, POST, OPTIONS, HEAD

### Exercise - Try all the endpoints and params listed in the docs

- try all the end points listed
- try all the query parameters listed

### Exercise – Try all the endpoint variants

e.g.

- /people
- /people/1
- /people/1/
- /people/schema

### Exercise - Compare the search results to the entity list results

- issue `/people` then identify a search term to try like `?search=sky `
- How do you know the results are correct?

### Exercise – Create an OpenAPI spec file for the API and try running the spec through the various tools

- Load it into Swagger UI and try to use it to make API calls
- Use Dredd to automatically cover API endpoints

## Some Observations

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

### Non-standard URL parsing

The API is non-standard in the sense that it accepts trailing “/”.

Normally `/people/1` and `/people/1/` would be treated as different endpoints and `/people/1/` would return a 404 and not map on to the person with ID 1.

Seeing this as a tester made me think of using URL formats that I would not normally use e.g.

- How many “/” can I add?
- Can I add leading and trailing “/” ?
- Could the ID be anywhere in the list of leading and trailing slashes? I.e. “////3////”
- What other characters might be valid as part of the URL path? E.g. could I add “\”, what else could I add?

The interesting part of exploring the API is that I don’t know if these non-standard features are by design or if they are bugs.

One thing I might want to do is look through the source code and try to decide if any of the observations are bugs or if they are there intentionally. 

 