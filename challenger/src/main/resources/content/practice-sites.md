---
title: API Testing Practice Sites
description: A list of the best API Practice sites and applications to improve your Testing and Automating.
showads: true
---

# API Practice Sites

There are many lists of 'free to use' APIs online. Most of these are not designed for practicing testing or automating. I have listed a few collation sites at the bottom of this page.

All of the sites and applications listed here are designed for practice. This carefully curated list only presents the best applications to use to improve your testing and automating.

## Online and Free to Use

### API Simulators

API Simulators provide end points, and simulate a functional API in that the information is hard coded or randomly generated and no create, update or delete action will persist to the server (`POST, PUT, DELETE, PATCH`).

Simulators are a good place to start, and practice with tooling. But do not stay with them too long. Try to move on to Real APIs and applications quickly so you get the experience of controlling the data in the system through API Interaction.

There are many API simulators so we have only listed a few of the best here.

---

#### API Challenges Simulator

[API Challenges Simulator](https://apichallenges.eviltester.com/practice-modes/simulation)

- [follow the instructions and work through the API Challenges Simulator](/practice-modes/simulation)
- An API Simulator (i.e. it always response the same way and you can't update data on the back-end, but the system will respond as though you did.
- Follow the instructions on the linked page and issue `GET`, `POST`, `PUT` and `DELETE` requests.

---

#### HTTPBin

[httpbin.org](https://httpbin.org)
 
- [read our longer write up of HttpBin with exercises to try here](/practice-sites/httpbin) 
- This API can help you get used to your tools.
- It has a set of endpoints that you can call for documented outcomes.
- e.g. a `DELETE` call to `https://httpbin.org/delete/404` will return payload, using any other verb will return a `405 Method Not Allowed` status code.
- I think it is a useful exercise to work through all the API endpoints through the Swagger interface, make sure you know what they do, and then replicate the request through an API client. This should be a useful way of getting used to the functionality in your API client.

---

#### JSON Placeholder

[JSON Placeholder](https://jsonplaceholder.typicode.com/)

- [read our longer write up of JSON Placeholder with exercises to try here](/practice-sites/jsonplaceholder)
- An API For `GET` requests.
- Acts as a simulator for Update and Delete requests
- Useful for getting used to your REST Client Tooling.
- notable for the [usage guide](https://jsonplaceholder.typicode.com/guide/) showing example usages as JavaScript that you can run from the Browser Developer Tools console. This can be a useful approach to experiment when exploring and testing a system.

---

#### Fake REST API

[Fake REST API](https://fakerestapi.azurewebsites.net/index.html)

- [read our longer write up of Fake REST API with exercises to try here](/practice-sites/fakerestapi)
- Another simulator with a Swagger front end to experiment easily, but do make sure to use a REST Client as well.
- The `POST` and `PUT` requests don't update the server but they are validated so you can see an additional approach to JSON validation error reporting.
- A fun API to spend some time exploring.

---

### GET Only APIs

These APIs are full APIs, but they will only respond to `GET` requests. This usually also means they support `OPTIONS` and `HEAD` requests. They are typically not designed for practicing testing but they can be an easy option for getting used to your REST API Client tooling.

Additionally they often have a lot of data so can support Query parameters for filtering, pagination and limiting data. Read the documentation for each API and experiment in your chosen REST Client.

---

#### Swapi Star Wars API

[swapi the Star Wars API](https://swapi.dev) 

- [read our longer write up of SWAPI with exercises to try here](/practice-sites/swapi)   
- A `GET` based API (no updates allowed) with a variety of data end points
- All endpoints return star wars information
- The UI can help you get started but make sure to use a REST Client on the API to get used to your tooling.
- `HEAD` and `OPTIONS` are also supported

---

#### Random User Me

[Random User Me](https://randomuser.me)

- [read our longer write up of Random User Me with exercises to try here](/practice-sites/randomuser)
- A `GET` based API for returning random user details.
- Useful for experimenting with Query parameters to filter, paginate and download
- returns a random User, supports different formats but this is controlled by a `format` URL parameter rather than an `Accept` header, which is a pity, but having different formats can help you practice with additional data formatting and parsing tooling.
- There is no Swagger spec so you can use this as an exercise in modelling the API by hand.

---

#### Dummy JSON

[dummyJSON](https://dummyjson.com/)

- [read our longer write up of Dummy JSON with exercises to try here](/practice-sites/dummyjson)
- An API for JSON requests and responses.
- A lot of endpoints for different types of data.
- Supports filters and limits as URL Query parameters.
- Simulate Update requests (`POST, PUT, DELETE`)
- I originally classified this exclusively as a simulator but because I could craft endpoints that triggered 500 server responses I added it as a GET Only API (Update requests like PUT, POST, PATCH, DELETE are simulated)

---

#### Others

- [pokeapi.co](https://pokeapi.co) - like swapi.dev but for Pokemon data

---

### Full APIs

These are full APIs online, which means you don't have to install anything and can persist data on the server so `PUT, POST, PATCH, DELETE` are usually supported. These APIs usually have the additional step of creating an authentication key or registering a session but the instructions will usually walk you through this.


---

#### API Challenges

[API Challenges](/gui/challenges)

- Our API Challenges is a fully functional TODOs management application.
- Create a session by issuing a `POST` request to `https://apichallenges.eviltester.com/challenger`, you will see an `X-CHALLENGER` header in the response and if you add that header into all your requests you will be able to update data and view your solved challengs progress in the UI.
- It is not deliberately buggy so if you experience problems, you've either found a bug or still have to learn to use the HTTP Client tooling.
- There are 50+ challenges to complete which will guide you through an API testing process.
- Since it is a working API you can make up your own challenges and try whatever tests you want.

---

#### GO Rest

[Go REST](https://gorest.co.in)

- A functional API that persists data to the server in a shared session so **do not use any private data or personal information**.
- To use the ` PUT, POST, PATCH, DELETE` request you need to request an access token by logging in using GitHub, Google or Microsoft. Then add this as a Bearer token in your request.
- It performs data validation, reporting all errors in the response making it easy to work with.
- The documentation is not automatically generated which is good. You have to read the docs, and model the API then experiment with it.
- No swagger file is provided so you create the request collection as you test.
- After reading the documentation and performing some tests I did find a difference between the docs and the implementation so I don't think the system is bug free, which makes it a test application as well as a practice application.
- Also provides a GraphQL endpoint, which I haven't experimented with.

---

#### Simple API

[Simple API](/practice-modes/simpleapi)

- [read our longer write up of Simple API with exercises here](/practice-sites/simpleapi)
- An API for JSON and XML requests and responses.
- A simple data structure to avoid personal data.
- No Authentication required for updates.
- Update requests are supported (`POST, PUT, DELETE`).
- Two OpenAPI specification files: one for using the API, one to support testing

---

#### Toolshop API

[Toolshop API](https://api.practicesoftwaretesting.com/api/documentation)

- [read our longer write up of Simple API with exercises here](/practice-sites/toolshop)
- Toolshop API has a UI and an API backend. This allows you to experiment with the UI, open the network tab and see the relevant API requests, or use the API and double-check the results on screen in the UI.
- Multiple versions of the Toolshop are available - with bugs and without
- The Open API Swagger file is one of the most complete that I've seen, and is a great example of the capabilities of the format.
- Some endpoints require that you master the authentication process which requires logging in as a user, then using the returned token as an HTTP Bearer Token.
- The DELETE endpoints seemed to require logging in as an admin user.
- The Database is shared, so don't add any private information (e.g. your name and email) if you create a new user.
- The error messages returned from the server in the message are useful aids to helping you craft a valid payload.

---

#### Expand Testing Notes API
  
[Notes API from Expand Testing](https://practice.expandtesting.com/notes/api/api-docs/)

- I'm not sure if the API is supposed to be deliberately buggy or not since it comes from a Testing company it might be. But keep your eyes open for inconsistencies when testing.
- It provides a swagger UI interface, but remember to go beyond that and use a REST Client.
- It is interesting to see alternative approaches in an API e.g. seeing an Options response that is human-readable (this approach is also used in Restful Booker).
- You need to 'register', then 'login', find the token and then pass this in as a `X-AUTH-TOKEN` to use the API properly. By doing this your data is saved separate from other users, which means you will find testing easier as no-one else is interfering with your test data.
- There is a [UI that uses the API](https://practice.expandtesting.com/notes/app/) so experiment with this and vew the API calls in the network tab. 

---

### APIs Not for First Time Use

I found the following APIs to be buggy, either deliberately or not. So I wouldn't recommend them as your first API to practice with. Once you are confident with how APIs work, and understand the tooling, then I'd recommend testing with them.

---

#### Restful Booker

[Restful Booker](https://restful-booker.herokuapp.com/apidoc/index.html)

- A deliberately buggy REST API.
- The bugs can make it hard to identify if you are doing something wrong, so I wouldn't start with this one, use this only after you've practiced with some more functional APIs. Otherwise you risk confusing yourself or thinking that your tools are not working correctly.
- Once you've explored a few APIs then this is worth working through. It is designed to be used in an API Training course so adding 'bugs' in the app works well in that scenario because an instructor can guide you through the issues and answer questions.

---

#### Swagger Petstore

[Swagger petstore](https://petstore.swagger.io)

- Nothing in the documentation led me to believe that this is designed to be deliberately buggy.
- But it did seem to have many functional issues when I tried it so in that sense it is worth exploring, but if you are not used to testing APIs it might frustrate you.
- Because it is swagger based it has a GUI, but you can download the Swagger file and load it into your API Client of choice.
- Or use the Network tab to view the requests and then replicate them in a REST Client.
- It is a CRUD interface, and you can seem to make changes on the server.

---

#### Automation Exercise Endpoints

This was interesting and a bit weird so I wouldn't recommend it as a First Time API. It felt more like a 'puzzle API' since it wasn't well documented and I had to explore request formats to get it working.

[Automation Exercise.com](https://automationexercise.com/api_list)

- This site has a list of API End Points that you can use to practice testing.
- An interesting example of non-standard payloads and requests which made interacting with it a bit of a puzzle.
- Hard to know if it is deliberately buggy or not. But I made notes of lots of inconsistencies. It kept me entertained for an hour or so.
- Data is saved to the server so you can practice CRUD operations.

---

#### Reqres

I found Reqres a little hard to understand from the documentation. It seems as though it has a hard coded set of users `/api/users` and then a hard coded set of data that it returns from `/api/<insert entity name here>` e.g. `/api/things`.

- [reqres](https://reqres.in)
    - Supports `JSON` only
  - Has hard coded data.
  - Provides a Swagger [API interface and documentation](https://reqres.in/api-docs/) this can be useful to get started but doesn't cover the generic endpoints so you'll need to use a REST Client for that.
  - There is also a hard coded set of 12 'things' which will respond to any endpoint e.g. `GET https://reqres/in/api/books/1` will return the hardcoded 'thing' with ID 1 (it won't be a 'book' but the API responds as though the `/api/books/` end point exists)
  - Simulates `DELETE, PUT, POST, PATCH` operations.


## Compile and run yourself apps

To have the most control over your practice testing session you can download an application and run it locally. All API calls would then be made to `http://localhost`. You won't experience any issues with SSL, HTTPs, Certificates. There will be no issues with using private data because it will be stored locally.

Getting started with these applications can be a little harder than using the online versions and may require installing Java, Node or Docker.

### Best Buy API Playground

- Best Buy API Playground is a full API that can be run locally using Node or Docker
- Full range of CRUD operations is supported using `GET`, `POST`, `PATCH`, `DELETE`
- Large set of data is provided
- Swagger UI is available
- Open API Definition is downloadable

[Read a full overview of Best BUY API Playground with sample exercises](/practice-sites/apps/bestbuy)

### Device Registry

- Device Registry is a test API with multiple users for managing 'devices'
- Can be run locally if you have Python installed.
- Can be run from Docker using the instructions in our guid
- Has a Swagger UI to allow easy exploration of the API
- Has a Redoc UI for an additional UI based exploration of the API
- API calls are authenticated using two hard coded user/password combinations

[Read a full overview of Device Registry API with sample exercises](/practice-sites/apps/deviceregistry)

### RestListicator

- RestListicator is a simple API that can be run locally using Java or Docker
- Can be run in two modes, one with known bugs, one without deliberately added bugs
- Full range of CRUD operations is supported using `GET`, `POST`, `PATCH`, `DELETE`
- Application accepts and returns `XML` and `JSON`
- Open API Definition is downloadable from the GitHub Repo

[Read a full overview of RestListicator with sample exercises](/practice-sites/apps/restlisticator)

### Tracks

- Tracks is a full todo app that can be run locally if you have Ruby on Rails installed, or use a [virtual machine from Turnkey](https://www.turnkeylinux.org/tracks) or [run from docker](https://github.com/eviltester/tracksdocker).
- I recommend running it from Docker
- Tracks is the case study application used in the book [Automating and Testing a REST API](https://www.eviltester.com/page/books/automating-testing-api-casestudy/)

[Read a full overview of Tracks with sample exercises](/practice-sites/apps/tracks)

### FX Trade Hub

- FX-TradeHub is a simple API simulating a trading application.
- Can be run locally if you have Node installed or from a Docker file.
- Has a Swagger UI for easy exploration

[Read a full overview of Fx Trade Hub with sample exercises](/practice-sites/apps/fxtradehub)

### More Applications

I've listed a few applications here initially and will expand the instructions to include walkthroughs and setup guides in future updates.

- [Api Challenges](https://github.com/eviltester/thingifier/tree/master/challenger)
  - API challenges can be run locally if you install a Java SDK and maven as pre-requisites.
- [JSON Server](https://github.com/typicode/json-server)
   - Can be run locally if you have Node and npm installed.


<!--

TODO: evaluate

https://api.practicesoftwaretesting.com/api/documentation#/User/get-current-customer-info
part of https://practicesoftwaretesting.com/#/

## Play an API Based Game

- spacetraders API
    - https://spacetraders.io/

-->


## Free to Use API lists

The important thing to remember about list of free to use APIs is that the APIs are not designed for testing. They will either be `GET` interfaces to retrieve data or you will need to register and create an authentication key.

I've included a few collated lists for completeness if you want to go beyond the practice APIs listed above.

- [Free APIs](https://free-apis.github.io)
- [Ultimate API Challenge](https://theultimateapichallenge.com/challenges)
- [Rapid API - Free Public APIs](https://rapidapi.com/collection/list-of-free-apis)
- [Postman Collections for Public APIs](https://www.postman.com/explore/collections)
- [API List](https://apilist.fun)