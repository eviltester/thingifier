---
title: Fake REST API - A Simulator API 
description: An overview of the Fake REST API for practicing tooling requests.
showads: true
---

In addition to our [API Challenges](/gui/challenges) you should practice on as many sites as possible. Try [Fake Rest API](https://fakerestapi.azurewebsites.net/index.html).

# Fake REST API - An API which returns JSON data

[Fake Rest API](https://fakerestapi.azurewebsites.net/index.html) is an API that returns a set of canned data. It also simulates the effects of update methods like POST, PUT, DELETE, PATCH.

## About JSON Placeholder

[Fake Rest API](https://fakerestapi.azurewebsites.net/index.html) is an API that returns canned data. It also claims to simulate the effects of update methods like POST, PUT, DELETE, PATCH by returning what would have happened if the request you made had taken effect.

The Data represents 5 types of entities:

- activities
- authors
- books
- coverphotos
- users

Each of these has its own endpoint e.g.

- `/api/v1/activities`
- `/api/v1/authors`
- etc.

The API is simple enough for beginners to explore but the simulated responses from update requests are different from many APIs.

Initial exploration can be done using the swagger interface itself. This allows you to make `GET`, `PUT`, `POST`, `DELETE` requests.

Remember it is a simulator, rather than an API, so your updates do not make back end changes, i.e. issuing a GET after a PUT will not show you the changes you submitted.

The Swagger interface validates the inputs so you'll need to use a REST Client if you want to test it with invalid values to check for parameter validation.

This API Simulator offers the benefit of an [Open API Specification File](https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json). You can download this and use it to seed the requests in a REST Client, or study the file to guide your exploration.

## Links

- The main site and documentation [fakerestapi.azurewebsites.net/index.html](https://fakerestapi.azurewebsites.net/index.html)
- The API Open API file [fakerestapi.azurewebsites.net/swagger/v1/swagger.json](https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json)


## Summary

- A simulator with a Swagger front end to experiment easily
- Do make sure to use a REST Client as well to explore parameter validation.
- The `POST` and `PUT` requests don't update the server but they are validated so you can see an additional approach to JSON validation and error reporting.
- Even as a simulator it has bugs so you can explore the functionality and find issues.
- A fun API to spend some time exploring.

## Exercises

I've created a set of suggested exercises in case you need some prompting for practice.

### Exercise - Explore the API Via the Swagger Interface

- Use the Swagger front end to make various calls on the endpoints
- You can only input valid values for parameters
- Hint: You may be able to find some inconsistencies between the input data and simulated responses

### Exercise - Use the API with a REST Client using the Open API file

- Download a REST Client
- Download the `swagger.json` file
- Import the `swagger.json` into your REST Client and explore conditions you could not using the swagger interface
    - e.g. `OPTIONS`, `HEAD`, non-integer parameters, invalid endpoint urls, etc.

### Exercise - Use the POST verb to create instances

- The `POST` simulator is quite interesting
- Create instances with minimal fields and all fields
- Try adding duplicate fields in the payload
- What happens if you add extra fields?
- Which fields will auto convert between types e.g. String to Integer, Integer to String, etc.

### Exercise - Explore the DELETE verb on endpoints

- The `DELETE` simulator is also quite interesting
- Delete something that is not supposed to exist, do you get the response you expect?


## Some Observations

- Swagger File
   - The swagger file does not have any `host` or base path entries so I had to amend all the request URLs in the REST client.
   - The swagger file itself is a good example of the type of files you are likely to see in the real world.
- Request Content
   - I found it interesting that the request content is not consistent between calls. e.g. the same number of activities is returned, with the same IDs, but the due dates would change. I didn't spend enough time exploring this but I did wonder what varied between requests and I'm tempted to return and explore this in a future session.
   - This regeneration of due dates also makes it more difficult to automate because you can't directly compare the instances returned from `/activities` and individual call e.g. `/activities/30`. So as a simulator it is not quite consistent
- 40xs
   - I thought the link to the HTTP standard with `400`, `404`, `405`, `415` responses was a nice touch. It is non-standard and you'd never see this in the real world, but if you are just learning APIs then this might be useful to help understand the response you are seeing.
   - The 404s are different between simulated end points and general URL calls to the application
- `POST` simulator
   - The `POST` simulator was quite accurate and encouraged me to explore the field values and types
- `OPTIONS`
  - The `OPTIONS` verb did not seem to be supported, but I thought it was a nice touch that `405` responses contained the `allow` header
- `coverphotos`
  - I wasn't expecting the cover photos url to actually lead to something, but the urls themselves don't seem to be url encoded so might not work if you fed them into a follow on HTTP GET request 

 