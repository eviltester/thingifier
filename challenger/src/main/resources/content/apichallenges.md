---
title: API Challenges Solutions Documentation
description: A brief overview of the API challenges and how to use the solutions
---

# API Challenges

You can find solutions to all of the challenges [here](apichallenges/solutions):

- [Solutions for the API Challenges](apichallenges/solutions)

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
