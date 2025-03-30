---
title: Tracks - A Productivity GTD App - Practice Web App and API
description: Tracks is a mature application implementing the Getting Things Done productivity method with a Web GUI and API.
showads: true
---

In addition to our [API Challenges](/gui/challenges) you should practice on as many sites as possible. Try [Tracks](https://www.getontracks.org/).

# Tracks - A Web Application with API

## About Tracks

[Tracks](https://www.getontracks.org/) is a Ruby on Rails application which implements the Getting Things Done productivity methodology.

## Running Tracks

The application page has '[how to run instructions](https://github.com/TracksApp/tracks/wiki/Installation)'.

It can be run from:

- a [TurnKey Virtual Machine](https://www.turnkeylinux.org/tracks)
- an [Official Docker Image](https://hub.docker.com/r/tracksapp/tracks)

I found the official Docker image hard to get started, and also for my book [Automating and Testing a REST API](https://www.eviltester.com/page/books/automating-testing-api-casestudy/) I used versions 2.2.3 and 2.3.0.

I created Docker images that make it easy to get started with versions 2.2.3 and 2.3.0

- [github.com/eviltester/tracksdocker](https://github.com/eviltester/tracksdocker)

As a summary of the content from the above `eviltester/tracksdocker` repo, you can use Docker to start either version as follows:

- v2.2.3
   - `docker run -p 80:80 eviltester/tracks:2.2.3`
- v.2.3.0
   - `docker run -p 80:80 eviltester/tracks:2.3.0`

After running through docker you can access the application by visiting `http://localhost:80`.

If you find that you can't login or can't create an admin session then try opening the link in incognito mode... if that works then delete the "_tracksapp_session" cookie in your normal browser window.

## Tracks API

The documentation for the Tracks API is available in the application itself from the help menu of the UI:

- REST API documentation `http://localhost:80/integrations/rest_api`

The API uses Basic Authentication where you supply the username and password of the admin user you created when you started the image.

The API payloads are `XML` rather than `JSON`.

The available endpoints are:

- `/todos.xml`
- `/todos/{ID}.xml`
- `/tickler.xml`
- `/done.xml`
- `/hidden.xml`
- `/calendar.xml`
- `/contexts.xml`
- `/contexts/{ID}.xml`
- `/contexts/{ID}/todos.xml`
- `/projects.xml`
- `/projects/{ID}.xml`
- `/projects/{ID}/todos.xml`

You may find it easier to create data in the back end using the UI before issuing API requests.

## Automating Tracks

Some examples for automating the Tracks API can be found in the source code for my book [Automating and Testing a REST API](https://www.eviltester.com/page/books/automating-testing-api-casestudy/)

- [github.com/eviltester/tracksrestcasestudy](https://github.com/eviltester/tracksrestcasestudy)

## Exercises

### Exercise - Create Data using the UI and issue `GET` requests

- Create a Project
- Create a Todo in the project
- Issue `GET` requests to return information on the end points

### Exercise - Create `todo` Data Using the API and `POST`

- each of the GET endpoints returns an XML response e.g. `/todos.xml` returns an array of todos
- use the `todo` element as a request payload with `POST` to create a todo
- experiment with the payload to find out what fields are optional and which are mandatory
- remember to `GET` the data to check the `todo` was created as expected

### Exercise - Amend `todo` Data Using the API and `PUT`

- to amend a todo you need to use the `/todos/{id}.xml` endpoint
- `{id}` would be replaced with the id of the todo you wish to amend
- experiment with the various fields and see what it is possible to amend and what it is not possible to amend

### Exercise - Explore the Documentation

- The API Documentation illustrates the use of the API using `cURL`
- Try the `cURL` commands listed so you learn to use `cURL` on your machine
- Some of the payloads you will send through to the API are quite large, so experiment with the `-d` [data](https://curl.se/docs/manpage.html#-d) paramenter to read information from a file
- The default response output from cURL may not include all the header information you need to check the responses. Try using `cURL` in `-verbose` mode [verbose mode](https://curl.se/docs/manpage.html#-d)

## Some Observations

### Response Content Type

- The response content type of `text/html` may interfere with your REST Client's ability to show the response
- By returning `text.html` the REST Client may attempt to render the response as HTML rather than showing the raw XML
- In Bruno you can view the `raw` response and see the XML

### Request Payload

- I found it interesting that I could create a todo with a duplicate id, and that it would be created with a new id, rather than reporting the attempt as a duplicate