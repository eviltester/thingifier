---
title: About The API Challenges
description: A brief overview of the API challenges and how to use them as a self-guided learning path for API Testing
---

# API Challenges

The API Challenges API is used for managing a To-Do list.

You need to authenticate to create a session in the system. This will create a unique API repository with basic test data.
As you interact with the API there are a variety of challenges to complete e.g. Get all TODOs, Delete a Todo, etc.

Each challenge is designed to teach you some aspect of how APIs work, and how to test them.

The API has [documentation available](/docs).

A set of [Challenges](/gui/challenges) are available to guide you through the exploration and learning of the API. To complete each challenge you will have to explore a different aspect of API testing.

You can also view the data in the application without using the API. Using the [Entities Explorer](/gui/entities) view.

## Application

This application has been deployed to a cloud instance. It is also available to download and run locally.

When run in a cloud environment:

- the data clears itself every 10 minutes
- each user has a unique set of data to work with so no interference from other users
- you need to create an `X-CHALLENGER` session to track your progress on the challenges ([more info](/gui/multiuser))

In single user mode, you can use the API without needing any extra headers or configuration.


## How to Play in Multi-user Mode

More information on how to play the challenges in multi-user mode i.e. `apichallenges.eviltester.com` can be found [on the multi-user instructions page](/gui/multiuser):

- [multi-user instructions page](/gui/multiuser)

## Challenges

The [Challenges](/gui/challenges) can be completed by issuing HTTP API requests.

e.g. `GET {{<ORIGIN_URL>}}/todos` would complete the challenge to "GET the list of todos"

You can also `GET {{<ORIGIN_URL>}}/challenges` to get the list of challenges and their status as an API call.

## Challenge Solutions

You can find solutions to all of the challenges [here](/apichallenges/solutions):

- [Solutions for the API Challenges](/apichallenges/solutions)

## How to Track Your Progress

The actual challenges page is available from the site menu:

- [challenges list](/gui/challenges)

To track your challenges and use the gamification tracking to view your progress you'll need to:

- create a challenger by issuing a `POST` request on the `/challenger` endpoint
- the response will include an `X-CHALLENGER` header with a unique GUID
- this GUID is the reference for an in-memory database in the API Challenges
- make requests to the API with the `X-CHALLENGER` header in the request headers
- you can then create and amend data in the database using the API
- and your progress will be tracked in the system memory

Full details are included in the [multi-user](/gui/multiuser) instructions. And in the information on the [challenges list page](/gui/challenges).
