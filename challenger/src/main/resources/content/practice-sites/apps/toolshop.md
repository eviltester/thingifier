---
title: Toolshop API - A Web Application and API for Practicing Testing and Automating
description: Toolshop API is an online public application and API supporting logins and has multiple versions with bugs and without bugs.
showads: true
---

In addition to our [API Challenges](/gui/challenges) you should practice on as many sites as possible. Try [Toolshop API](https://api.practicesoftwaretesting.com/api/documentation).

# Toolshop API - A Public API and Web Application for practicing Testing and Automating

[Toolshop API](https://api.practicesoftwaretesting.com/api/documentation) by [Roy de Kleijn](https://www.linkedin.com/in/roydekleijn/)

## About Toolshop API

Toolshop API has a [UI](https://practicesoftwaretesting.com/) and an API backend. This allows you to experiment with the UI, open the network tab and see the relevant API requests, or use the API and double-check the results on screen in the UI.

Multiple versions of the Toolshop API are available.

- A [deliberately buggy version of the API](https://api-with-bugs.practicesoftwaretesting.com/api/documentation) with [buggy UI](https://with-bugs.practicesoftwaretesting.com/) is available if you want to explore for issues.
- 
- The [main version](https://api.practicesoftwaretesting.com/api/documentation) with [UI](https://practicesoftwaretesting.com/) where bugs are not intended, or perhaps not perceived as bugs, remember every API is different.

In actual fact, there are (at the time of writing), 5 non-buggy versions, each corresponding to a 'sprint' of development so you can test the application in the different stages of its development. The links can be found in the [source documentation](https://github.com/testsmith-io/practice-software-testing?tab=readme-ov-file#urls-hosted-versions):

- Sprint 1 [UI](https://v1.practicesoftwaretesting.com/) and [API Swagger UI](https://api-v1.practicesoftwaretesting.com/api/documentation)
- Sprint 2 [UI](https://v2.practicesoftwaretesting.com/) and [API Swagger UI](https://api-v2.practicesoftwaretesting.com/api/documentation)
- Sprint 3 [UI](https://v3.practicesoftwaretesting.com/) and [API Swagger UI](https://api-v3.practicesoftwaretesting.com/api/documentation)
- Sprint 4 [UI](https://v4.practicesoftwaretesting.com/) and [API Swagger UI](https://api-v4.practicesoftwaretesting.com/api/documentation)
- Sprint 5 (the default public version) [UI](https://practicesoftwaretesting.com/) and [API Swagger UI](https://api.practicesoftwaretesting.com/api/documentation)
- Buggy Version of Sprint 5 [UI](https://with-bugs.practicesoftwaretesting.com/) and [API Swagger UI](https://api-with-bugs.practicesoftwaretesting.com/api/documentation)

To explore the application, you really need to know the default users (these are also listed in the source documentation):

- admin - email `admin@practicesoftwaretesting.com` and password `welcome01`
- user 1 - email `customer@practicesoftwaretesting.com` and password `welcome01`
- user 2 - email `customer2@practicesoftwaretesting.com` and password `welcome01`

To practise API testing, I recommend:

- using Sprint 5
- starting with the non-buggy version for the majority of sessions
- use the buggy version after working with the 'non-buggy' version

Summary:

- The Swagger file is one of the most complete that I've seen, and is a great example of the capabilities of the format.
- The Toolshop API allows GET,POST, PUT methods without authentication.
- Some endpoints require that you master the authentication process which requires logging in as a user, then using the returned token as an HTTP Bearer Token.
- The DELETE endpoints seemed to require logging in as an admin user.
- The admin user also receives 'more' data in the responses, so its worth experimenting with the different users.
- The Database is shared, so don't add any private information (e.g. your name and email) if you create a new user.
- The error messages returned from the server in the message are useful aids to helping you craft a valid payload.

Links:

- Sprint 5 Online Public Hosted [Web App UI](https://practicesoftwaretesting.com/) and [API](https://api.practicesoftwaretesting.com/api/documentation)
- Sprint 5 deliberately buggy version of the [Web App UI](https://with-bugs.practicesoftwaretesting.com/) and [API](https://api-with-bugs.practicesoftwaretesting.com/api/documentation)
- [Testsmith-io on Github](https://github.com/testsmith-io) where you can find the code for the application and many examples of automating systems.
    - [Code for the PracticeSoftwareTesting application](https://github.com/testsmith-io/practice-software-testing) 

  
## API Endpoints

The public application is bundled with sample data so you will be able to use the data on the back end immediately.

The main endpoints are:

- `/brands`
- `/carts`
- `/categories`
- `/messages`
- `/images`
- `/invoices`
- `/payment`
- `/products`
- `/reports` - various end points for specific reports
- `/users`


## Exercises

### Exercise - Explore the Application and API Documentation

Choose one of the hosted versions (sprint 1 -5, buggy) and explore the UI and Swagger API documentation.

e.g.

- Sprint 5 UI [practicesoftwaretesting.com](https://practicesoftwaretesting.com/)
- Sprint 5 API Swagger [api.practicesoftwaretesting.com/api/documentation](https://api.practicesoftwaretesting.com/api/documentation)

As you use the UI, open the network tab and you can see how the API calls are used in the context of the UI functionality. You should also see some endpoints which require authorisation and authentication.

### Exercise - Explore the API without Authentication

You can use the Swagger UI to make initial API calls against the backend without any authentication.

### Exercise - Explore the API with User Authentication

You can use the Swagger UI to make API calls authenticated as one of the default users.

- make a `POST` call to `/users/login` with a customer email and password

e.g.

```json
{
  "email": "customer@practicesoftwaretesting.com",
  "password": "welcome01"
}
```

- take the returned `access_token` and include it in all future requests
- if you are using the Swagger UI then use the `[Authorize]` button in the UI and paste in the bearer token

### Exercise - Explore the API with Admin Authentication

- During your earlier exploration with User authentication you will have found methods and endpoints which you could not access. Check those methods now using an admin authenticated login session.

### Exercise - Explore the API using a REST Client

- Download and import the Open API file into your REST Client and make authenticated calls from the REST API.
- To make authenticated calls through the REST Client, include the `access_token` as a bearer token in the request:
    - `'Authorization: Bearer eyJ0eXAiOi...`

### Exercise - Explore the API and data validation rules

- The Schema in the Swagger UI does not describe max and min fields or validation so we have to explore the data and rely on error messages to describe the data validation.
- Try and trigger data validation errors so that you understand what field validation rules are being used.

### Exercise - Explore the API with Trickster Intent

- The API allows amendment of data without authentication
- Explore the endpoints with the view of amending data such that it impacts the UI

### Exercise - Explore the API with Security Intent

- we don't necessarily need to know much about security to look at an API from a security perspective
- e.g. can you change data that you should not have access to?

## Some Observations

### Explore the Application and API Documentation

- Using the UI I wasn't sure what API calls require what level of authentication but I could see authentication failures in the network tab. The Swagger UI does show a padlock against calls, but I wasn't sure what level of authentication was required.
- I found it interesting when I looked at the UI that some `POST` requests did not require authentication e.g. uploading files to messages, amending brands etc.
  - This makes 'trickster intent' a viable mind set

### Explore the API without Authentication

- I like to use default data when I'm exploring using Swagger and I noticed that the `/related` call on a `/product` does not return a 404 instead it returns a 500 i.e. `/product/1/related`
- I was interested to see that the search endpoints support database wildcard characters i.e. `%`, which makes me think that I should explore other database specific values.

### Explore the API with Trickster Intent

- Since I can amend Brand data with a put, without authentication I was thinking what can I enter that might trigger issues on the front end.
- This is beyond data validation because I'm now thinking about data with an impact in the UI rendering.
- I found that the system accepts uni-code characters which can impact the rendering of the site ways that normal text does not.

### Explore the API with Security Intent

- The user management is vulnerable, possibly by design because it is a testing app, to insecure access e.g. I should not be able to change the password but that is what the forgotten password request does
- Endpoints also reveal if emails are already in use