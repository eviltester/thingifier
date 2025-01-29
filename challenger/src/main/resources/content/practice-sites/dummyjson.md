---
title: Dummy JSON - A Mix of API Simulator and GET API - Practice API
description: An overview of the Dummy JSON practice API site, explaining how to use it to learn about APIs and practice with API tooling.
showads: true
---

In addition to our [API Challenges](/gui/challenges) you should practice on as many sites as possible. Try [DummyJSON](https://dummyjson.com/).

# Dummy JSON - An API Simulator and functional API

Dummy JSON is a mix of an API Simulator with canned responses to requests, and also functional API endpoints that you can actually test.

## About Dummy JSON

When I first found Dummy JSON I had categorised it as an API Simulator. An API that responds with the same data, no matter what values are in the request. But there are so many endpoints in this API that after hunting around while practicing I found a few endpoints that I could explore for testing.

The [Docs](https://dummyjson.com/docs) often have a `[Show Output]` button under the examples so you can explore the basic functionality of the endpoint without a REST client.

Additionally the examples in the Docs can be pasted into the Browser Dev Tools JavaScript console and you can experiment with JavaScript and Dev Tools.

Don't let the examples on the Intro page of the docs confuse you, there is no endpoint called `RESOURCE`. The examples here are to show general parameters you can use with any of the endpoints e.g. `recipes`, `carts`, etc.

Many of the endpoints are simple GET endpoints for canned data:

- `products`
- `carts`
- `recipes`
- `users`
- `posts`
- `comments`
- `todos`
- `quotes`

These all return canned data on `GET` and some are documented as providing simulated responses for `DELETE`, `POST`, `PUT`, `PATCH`.

The `image` endpoint, because it generates custom responses based on the input is backed by a real server function, not just canned data. This makes it a viable target for exploratory testing.

## Links

- The main site [dummyjson.com](https://dummyjson.com/)
- The documentation [dummyjson.com/docs](https://dummyjson.com/docs)
- The source code [github.com/Ovi/DummyJSON](https://github.com/Ovi/DummyJSON)

## Summary

- An API for JSON requests and responses.
- A lot of endpoints for different types of data.
- Supports filters and limits as URL Query parameters.
- Simulate Update requests (`POST, PUT, DELETE`)
- More than an API simulator some endpoints can be pushed to trigger 500 server responses and validation messages


## Exercises

I've created a set of suggested exercises in case you need some prompting for practice.

### Exercise - Explore the API Via the Docs

- [dummyjson.com/docs](https://dummyjson.com/docs)
    - read through the documentation and find some requests which have `[Show Output]` buttons
    - click the buttons to see the response

### Exercise - Explore the API Via the Docs with the Browser Dev Tool Network tools Open

- [dummyjson.com/docs](https://dummyjson.com/docs)
  - open the browser dev tools and have the network tab open so you can see the  request and response
  - read through the documentation and find some requests which have `[Show Output]` buttons
  - click the buttons to see the response
  - check in the Network tab to see the actual request
     - Spoiler - that's right, it didn't issue any requests, but now you know not to trust a UI exclusively and to make sure you have the ability to observe network traffic when working with web applications and APIs 

### Exercise - Use the Examples in the Docs with the Browser Dev Tool Network tools Open

- [dummyjson.com/docs](https://dummyjson.com/docs)
  - open the browser dev tools and have the network tab open so you can see the  request and response
  - read through the documentation and find some requests which have `[Show Output]` buttons
  - All of the JavaScript shown on the page can be pasted into the JavaScript console so you can learn how to issue HTTP requests from the dev tools, and you can see the request and response in the Network tabs.

### Exercise - Use the API

- Download a REST Client
- Make some API calls that copy what the Doc examples do
- Make sure to try requests for all verbs listed i.e. `GET`, `POST`, `PUT`, `PATCH`, `DELETE`

### Exercise - Explore the API with functions the Docs do not mention

- [dummyjson.com/docs](https://dummyjson.com/docs)
- Read the docs and you'll see that some endpoints are only documented as having `GET` responses
   - e.g.  `https://dummyjson.com/recipes/1`
- What happens if you issue a `DELETE` request? or a `POST`, `PUT` etc.
- Do you get the responses you expected?

### Exercise - Explore the `image` endpoint as a testing target

- [dummyjson.com/docs/image](https://dummyjson.com/docs/image)
- The image generated is based on the parameters passed in via the request
- Does the end point validate all inputs?
- This might be an interesting test of your REST Client - does it show the image or do you have to test the endpoint in a browser?
- If your REST Client doesn't render the image in the client you might want to explore some of the other REST Clients and see if they can.


## Some Observations

### Unsupported Verbs

- When exploring the unsupported or unlisted verbs for some of the endpoints e.g. `recipes` is only documented as supporting `GET`
- I found that `DELETE`, `POST` etc. returned a 404 instead of, what I expected, [405](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/405)
- This made me then explore the path structure of the URL a little longer

### Image testing

- There are obviously some parameters that are not well validated and can cause issues.
   - Did you find any which trigger server errors?
   - Did you find any parameters which behaved differently than you expected with invalid data?


 