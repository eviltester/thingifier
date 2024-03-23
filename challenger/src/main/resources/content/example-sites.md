---
title: API Testing Practise Sites
description: A list of the best API Practice sites and applications to improve your Testing and Automating.
---

# API Practice Sites

There are many list of 'free to use' APIs online. Most of these are not designed for practicing testing or automating. All of the sites and applications listed here are designed for practice. This carefully curated list only presents the best applications to use to improve your testing and automating.

## Online and Free to Use

### Suitable for Beginners

- [swapi the Star Wars API](https://swapi.dev) 
  - [read our longer write up with exercises to try here](/example-sites/swapi)   

- [API Challenges](/challenges)
  - Our API Challenges is a fully functional TODOs management application.
  - It is not deliberately buggy so if you experience problems, you've either found a bug or still have to learn to use the HTTP Client tooling.
  - There are 50+ challenges to complete which will guide you through an API testing process.
  - Since it is a working API you can make up your own challenges and try whatever tests you want.
  
- [httpbin.org](https://httpbin.org)
  - This API can help you get used to your tools.
  - It has a set of endpoints that you can call for documented outcomes.
  - e.g. a `DELETE` call to `https://httpbin.org/delete/404` will return payload, using any other verb will return a `405 Method Not Allowed` status code.
  - I think it is a useful exercise to work through all the API endpoints through the Swagger interface, make sure you know what they do, and then replicate the request through an API client. This should be a useful way of getting used to the functionality in your API client.

- https://jsonplaceholder.typicode.com/
    - practise  requests - see guide https://jsonplaceholder.typicode.com/guide/

- https://gorest.co.in/
- https://reqres.in/
- https://dummyjson.com/
- https://fakerestapi.azurewebsites.net/index.html
    - mainly a simulator since you can POST but there is no persisent side effect
    - but can be used for testing JSON validation and error responses
- https://randomuser.me/
- https://pokeapi.co/
    - like swapi.co but for pokemon

APIs from expandtesting.com
- https://practice.expandtesting.com/notes/api/api-docs/
- https://practice.expandtesting.com/api/api-docs/

## Buggy so not for beginners

I found the following APIs to be buggy, either deliberately or not. So I wouldn't recommend them as your first API to practice with. Once you are confident with how APIs work, and understand the tooling, then I'd recommend testing with them.

- [Restful Booker](https://restful-booker.herokuapp.com/apidoc/index.html)
  - A deliberately buggy REST API.
  - The bugs can make it hard to identify if you are doing something wrong, so I wouldn't start with this one, use this only after you've practiced with some more functional APIs. Otherwise you risk confusing yourself or thinking that your tools are not working correctly.
- [Swagger petstore](https://petstore.swagger.io)
  - Nothing in the documentation led me to believe that this is designed to be deliberately buggy.
  - But it did seem to have many function issues when I tried it so in that sense it worth exploring, but if you are not used to testing APIs it might frustrate you.
  - Because it is swagger based it has a GUI, but you can download the Swagger file and load it into your API Client of choice.
  - Or use the Network tab to view the requests and then replicate them in a REST Client.
  - It is a CRUD interface so you can seem to make changes on the server.

I saved this for last because it is interesting and a bit weird:

- [Automation Exercise.com](https://automationexercise.com/api_list)
  - This site has a list of API End Points that you can use to practice testing.
  - An interesting example of non-standard payloads and requests which made interacting with it a bit of a puzzle.
  - Hard to know if it is deliberately buggy or not. But I made notes of lots of inconsistencies. It kept me entertained for an hour or so.
  - Data is saved to the server so you can practice CRUD operations.

## Compile and run yourself apps

- https://github.com/BestBuy/api-playground
- https://github.com/AutomationPanda/device-registry-flask
    - device registry flask from AutomationPanda
- Tracks
    - https://www.getontracks.org/
    - run it from docker
- TODO API Sample
    - https://github.com/g33klady/TodoApiSample
    - has instructions to use from docker
- JSON Server
    - https://github.com/typicode/json-server


## Play an API Based Game

- spacetraders API
    - https://spacetraders.io/
