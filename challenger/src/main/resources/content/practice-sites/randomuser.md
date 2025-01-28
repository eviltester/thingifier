---
title: RandomUser - Random User Generator - Example API
description: An overview of the Random User Generator API, explaining how to use it to learn about APIs and practice with API tooling.
showads: true
---

In addition to our [API Challenges](/gui/challenges) you should practice on as many sites as possible. Try [RandomUser.me](https://randomuser.me/documentation).

# RandomUser.me - An API which returns random data

RandomUser.me is an API that returns a randomly generated user.

## About RandomUser.me

RandomUser.me is a Random Data Generation API. It supports multiple versions, and can be used to generate multiple users at the same time. Because it is GET based you can use the Browser to explore the API without any additional tooling.

The Random Data represents a 'user' or 'person'.

The API is simple enough for beginners to explore.

Because the API is only documented as supporting GET, it is possible to explore the API using a browser by amending the URL.

e.g. 

- https://randomuser.me/api/
- https://randomuser.me/api/?format=xml

The application supports multiple versions, so if you want to explore earlier versions and test the evolution of the API you can add the version number in the endpoint URL.

e.g.

- https://randomuser.me/api/0.1/

The version numbers are listed in the [Changelog](https://randomuser.me/changelog).

Random APIs can be difficult to test, but RandomUser.me has a `seed` parameter which can be used to control the random generation.

i.e. the same user will be generated for each call to https://randomuser.me/api/?seed=123

There are a lot of parameters in the URL to explore, all of which are listed in the documentation.

e.g.

- Include and Exclude fields from the response
- Controlling output format
- Creating users of specific nationalities


## Links

- The main site [randomuser.me](https://randomuser.me)
- The documentation [randomuser.me/documentation](https://randomuser.me/documentation)
- The source code [github.com/RandomAPI/Randomuser.me-Node](https://github.com/RandomAPI/Randomuser.me-Node)

## Summary

- A Random User Generator
- Designed for GET requests only
- Supports multiple versions by embedding the version number in the API URL
- Can return different formats e.g. JSON, CSV, XML
- Can generate multiple users by using the results parameter `?results=5000`
- Seed can be used to generate specific users for repeated calls

## Exercises

I've created a set of suggested exercises in case you need some prompting for practice.

### Exercise - Explore the API Via the Browser URL

- make a request from the Browser URL e.g. https://randomuser.me/api/?seed=123&results=2
- Use network tab to view the requests and responses

### Exercise - Use the API with a REST Client

- Download a REST Client
- Make some API calls using the REST Client

### Exercise - Explore the Docs

- [Documentation](https://randomuser.me/documentation)
- Read the docs
- Use the docs as a basis for exploration
- experiment with different HTTP Verbs: GET, POST, OPTIONS, HEAD

### Exercise - Try all the endpoints and params listed in the docs

- try all the end points listed
- try all the query parameters listed




 