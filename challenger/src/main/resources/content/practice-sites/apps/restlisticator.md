---
title: RestListicator - A Simple REST API for practicing testing - Practice API
description: A very simple REST API with user management and full HTTP Verb support, runnable via Docker.
showads: true
---

In addition to our [API Challenges](/gui/challenges) you should practice on as many sites as possible. Try RestListicator part of the [TestingApp](https://github.com/eviltester/TestingApp).

# RestListicator - A Software Testing Practice REST API


## About RestListicator

The RestListicator is the first Software Testing API Practice application created by [Alan Richardson](https://eviltester.com). It was written to support [API Testing Training](https://github.com/eviltester/automating-rest-api/tree/master/text/basic-tutorial).

The application is written in Java and full instructions are in the GitHub repository for compiling and running locally. It can also be run using Docker with full instructions for doing so contained below.

QuickStart Guide to run the application on `localhost:4567`:

```
docker run -it -p 4567:4567 eviltester/restlisticator:latest
```

The deliberately buggy version can be downloaded and run from Docker hub with:

```
docker run -it -p 4567:4567 eviltester/restlisticator:latest-buggy
```

The application has text documentation when visiting the url in a browser:

- `http://localhost:4567/listicator`

Additionally a hand created OpenAPI spec file can be downloaded from the Github repository:

- [github.com/eviltester/TestingApp/tree/master/java/testingapps/restlisticator/docs/swagger](https://github.com/eviltester/TestingApp/tree/master/java/testingapps/restlisticator/docs/swagger)

Summary:

- RestListicator is a simple API that can be run locally using Java or Docker
- Can be run in two modes, one with known bugs, one without deliberately added bugs
- Full range of CRUD operations is supported using `GET`, `POST`, `PATCH`, `DELETE`
- Application accepts and returns `XML` and `JSON`
- Open API Definition is downloadable from the GitHub Repo

## Running The Application Using Docker Hub Image

The easiest way to run RestListicator on Docker is to use the image posted to Docker Hub.

The image has been pushed to the `restlisticator` repository `eviltester/restlisticator`

- https://hub.docker.com/r/eviltester/restlisticator

The images have been tagged with the RestListicator version, or `latest` and both the buggy and non-buggy version have been tagged e.g.

Non Buggy version:

- `eviltester/restlisticator:latest`
- `eviltester/restlisticator:1.2.2`

Buggy version:

- `eviltester/restlisticator:latest-buggy`
- `eviltester/restlisticator:1.2.2-buggy`

The image can be downloaded and run as follows:

```
docker run -it -p 4567:4567 eviltester/restlisticator:latest-buggy
```

This will run the buggy version on `localhost:4567`.


## API Endpoints

The application has one main functional Entity `list`, but it also has user access and multi-session access.

Three users are created when the application starts, each with different permissions.

Use Basic authentication to switch between users.

- username: `superadmin`, password: `password`
- username: `admin`, password: `password`
- username: `user`, password: `password`

The API root is `http://localhost:4567/listicator`, with all the endpoints existing below this e.g. `http://localhost:4567/listicator/heartbeat`

The main endpoints are:

- `/heartbeat` - is the server alive?
- `/lists` - manage the List entities - create, amend lists
- `/lists/{guid}` - create, amend, delete a List
- `/users` - user management - create, delete
- `/users/{username}/password` - amend a User's password
- `/users/{username}/apikey` - amend a User's api key
- `/feature-toggles` - superadmin can toggle app features on and off
- `/sessionid` - get a unique sessionid to isolate your testing from other users, use the session id in a X-SESSIONID header

`GET` endpoints are mostly not authenticated. The endpoints beneath `/users` are authenticated i.e. only authenticated user can `GET` their details. Authentication can also be controlled using `X-API-AUTH` header. The api key for each user is visible when the application starts, and in the response headers made by the user.

The application can accept and return `JSON` or `XML`. The response format is controlled by the `accept` header and the request format by the `content-type` header.


## Exercises

### Exercise - Run the application and read the documentation

- Run the application, either using the Java instructions on Github or with Docker
- Visit `http://localhost:4567/listicator` and read the documentation

### Exercise - `GET` all the main endpoints

- Issue a GET request on each of the top level endpoints
- Experiment with the `accept` header to return `XML` or `JSON`
- Check that you receive authentication errors for the appropriate user endpoints.

### Exercise - Basic Auth as normal user

- Explore the API as 'user' using Basic Auth
- You should now be able to experiment with creating, amending
    - minimum information for a list is title e.g. `{title:"this is a title"}` 
- Try changing the password of the user
- Try Deleting a list as 'user'

### Exercise - Use parameters to filter User list

- Documenation for `GET` '/users' lists some filters that can be applied to username
    - can be filtered with `?username=exactmatch` e.g. `?username=admin`
    - can be filtered with `?username="partialmatch"` e.g. `?username="adm"`
- Search for partial match on "r"
- Search for users with "admin" in their username
- Search for users with an absolute name e.g. user

### Exercise: Use DELETE to Delete a list

- DELETE a 'list' that you created using the `guid`

_hint: `user` cannot delete_

### Exercise: Use POST to Amend a List

- default `user` password is `password`
- minimum information for a list is `title` e.g. `{title:"this is a title"}`
- Basic Auth required for a `POST` request to `/lists/{guid}`

How do you know it amended the list?

Can you amend all the fields? `title`, `description`, `guid`, `createdDate`, `amendedDate`

### Exercise: Use OPTIONS to explore capabilities of `/lists`

- [OPTIONS](https://tools.ietf.org/html/rfc7231#section-4.3.7)

_hint: check the headers_

### Exercise - Explore PUT

- Use PUT to create a new 'list'
- Use PUT to amend a list
- Does PUT work with multiple lists?
- Does PUT work with users?
- Test the PUT end points with different payloads


### Exercise - Explore Status Codes

Send requests to deliberately trigger the following status codes:

- `200 OK`
- `201 Created`
- `204 No Content`
- `400 Bad Request`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`
- `405 Method Not Allowed`
- `409 Conflict`

Bonus points for a `5xx` error e.g. `500`

### Exercises: Explore HEAD

- [HEAD](https://tools.ietf.org/html/rfc7231#section-4.3.2)
    - does `HEAD` implementation match the standard?
    - compare `HEAD` to `GET` is it the same information?
    - which parts of the API respond to `HEAD`?
    - does the documentation match the implementation?

### Exercises: Explore PATCH

- [PATCH](https://tools.ietf.org/html/rfc5789) is implemented just like `post`
    - does it comply with the [JSON Merge Patch](https://tools.ietf.org/html/rfc7396) standard?
- The implementation does not comply with the [JavaScript Object Notation (JSON) Patch](https://tools.ietf.org/html/rfc6902) standard
    - read the JSON Patch standard
- The implementation does not comply wiht the [XML Patch Operations standard](https://tools.ietf.org/html/rfc5261)
    - read the XML Patch Standard

## Some Observations

These observations were made using the buggy version of the application.

### Explore `GET` on the main endpoints

- `GET` a list that does not exist generated a `400` without an error message not a `404` e.g. `lists/bob`
- `GET` on a user that does not exist does not show different error to one which does, if I'm not authenticated then I just see `401`, that's good
- `GET` on a listicator endpoint that does not exist returns a 404 in HTML mode rather than an API - content-type text/html - suggests it is more of a 'web app' and only some endpoints are API endpoints


