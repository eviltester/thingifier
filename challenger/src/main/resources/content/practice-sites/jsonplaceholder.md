---
title: JSON Placeholder - Simulator API - Example API
description: An overview of the JSON Placeholder simulator API for practicing tooling requests.
showads: true
---

In addition to our [API Challenges](/gui/challenges) you should practice on as many sites as possible. Try [JSON Placeholder](https://jsonplaceholder.typicode.com/).

# JSON Placeholder - An API which returns JSON data

[JSON Placeholder](https://jsonplaceholder.typicode.com/) is an API that returns a set of canned data. It also simulates the effects of update methods like POST, PUT, DELETE, PATCH.

## About JSON Placeholder

[JSON Placeholder](https://jsonplaceholder.typicode.com/) is an API that returns canned data. It also simulates the effects of update methods like POST, PUT, DELETE, PATCH by returning what would have happened if the request you made had taken effect.

The Data represents 6 types of entities:

- posts
- comments
- albums
- photos
- todos
- users

Each of these has its own endpoint e.g.

- `/posts`
- `/comments`
- etc.

The API is simple enough for beginners to explore.

Initial exploration can be done using the browser itself with GET requests by amending the URL.

e.g. 

- https://jsonplaceholder.typicode.com/posts

This will allow you to explore the data structures, but you will want to use a REST Client to explore the other methods. 

Remember it is a simulator, rather than an API, so your updates do not make back end changes, i.e. issuing a GET after a PUT will not show you the changes you submitted.

It is still fun to explore and it does have what I would consider to be bugs so hunt around and see if you can find some.

## Links

- The main site and documentation [jsonplaceholder.typicode.com/](https://jsonplaceholder.typicode.com/)
- The API usage guide [jsonplaceholder.typicode.com/guide/](https://jsonplaceholder.typicode.com/guide/)
- The source code [github.com/typicode/jsonplaceholder](https://github.com/typicode/jsonplaceholder)

If you want to experiment with creating your own simple mock API you can do so with the sister project from typicode [json-server](https://github.com/typicode/json-server)

## Summary

- GET API
- Canned Data
- Simulates Update requests
- Multiple Entity types and response formats
- Useful for experimenting with tools in a safe environment
- Remember it is a simulator, rather than an API, so your updates do not make back end changes

## Exercises

I've created a set of suggested exercises in case you need some prompting for practice.

### Exercise - Explore the API Via the Browser URL

- Make a GET request from the Browser URL e.g.
  - https://jsonplaceholder.typicode.com/posts
  - https://jsonplaceholder.typicode.com/posts/1
- Explore the Routes and Resources listed in the main documentation using GET requests
  - https://jsonplaceholder.typicode.com/

### Exercise - Use the API with a REST Client

- Download a REST Client
- Make some GET API calls using the REST Client

### Exercise - Explore the Docs

- [Documentation](https://jsonplaceholder.typicode.com/) and [Guide](https://jsonplaceholder.typicode.com/guide/)
- Read the docs
- Use the examples in the docs as a basis for exploration
- experiment with different HTTP Verbs: GET, POST, OPTIONS, HEAD
- The easiest way to issue a POST, is to issue a GET and then use the response as the POST request

### Exercise - Explore the Verbs and Payloads

- experiment with different HTTP Verbs: GET, POST, OPTIONS, HEAD
- Do you get the responses and status codes you expect?

### Exercise - Automate from the Browser JavaScript Console

- Did you try and run all the examples in the [guide](https://jsonplaceholder.typicode.com/guide/) from the developer JavaScript console?
- Open the developer console (Right Click, Choose Inspect), then choose the Console
- Paste each of the examples into the console

### Exercise - Try backend validation

- The API is JSON based so try an invalid JSON payload
- Only JSON is supported, what happens if you pass in XML? (with `content-type: application/xml`)


## Some Observations

- Validation
   - I found the back end validation interesting.
   - The content type was very important for triggering the validation.
   - Malformed JSON (if sent through with content type `application/json`) resulted in 500 errors rather than 400 errors, which suggests the server hasn't been coded to validate responses properly. If this was a real API rather than a simulator then I would want to test this further as there might be vulnerabilities.
   - Any content, sent through with content type `application/xml` did not result in a JSON error, the fact that it was XML was ignored.
- URL Handling
   - URLs were slightly non standard in the sense that both `/posts` and `/posts/` were treated as the same URL, but normally `/posts/` would result in a 404 because an ID would be expected e.g. `/posts/1`
   - POST to `https://jsonplaceholder.typicode.com/posts/3` resulted in a 404, but I would expect it to have been a `405` METHOD NOT ALLOWED because this API simulator uses `PUT` to amend entities

 