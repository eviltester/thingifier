---
title: The API Challenges - API Tutorials and API Testing Practice Exercises
description: A practice API application with tutorials for HTTP and REST APIs. Guided exercises and gamification hands on learning path.
template: index
canonical: https://apichallenges.eviltester.com
---

# The API Challenges

## A learning environment to experiment, explore, and play with API Testing and Tooling

Welcome to The API Challenges.

We provide:

- A functioning API for you to practice API Testing
- A set of challenges to guide you in learning API Testing
- A multi-user playground to experiment within
- API documentation to read and learn from
- A gamified learning experience, with challenge status persisted between sessions

## Video Welcome

{{<youtube-embed key="7HN9f5JLt0g" title="API Challenges Overview Video">}}

[Learn how to Test APIs with our online tutorials and guides](/learning)

## About the Application

This is a functional API for managing A public To-Do list.

The API has [documentation available](/docs).

A set of [Challenges](/gui/challenges) are available to guide you through the exploration and learning of the API. To complete each challenge you will have to explore a different aspect of API testing.

You can also view the data in the application without using the API. Using the [Entities Explorer](/gui/entities) view.

## Tooling

We recommend that you use an [API GUI](/tools/clients) to interact with the API e.g.

- [Bruno](https://www.usebruno.com/)
- [Hoppscotch](https://hoppscotch.io/)
- [Thunderclient](https://www.thunderclient.com/)
- [Paw (Mac only)](https://paw.cloud/)
- [Insomnia Core](https://insomnia.rest/)
- [Postman](https://www.postman.com/)

Learn more about [HTTP/REST Clients](/tools/clients) and [HTTP Proxies](/tools/proxies)

## Application

This application has been deployed to a cloud instance. It is also available to download and run locally.

When run in a cloud environment:

- the data clears itself every 10 minutes
- other people might be using it so you can't rely on data existing between requests
- you need to create an `X-CHALLENGER` session to track your progress on the challenges ([more info](/gui/multiuser))

In single user mode, you can use the API without needing any extra headers or configuration.

Full download details are available from [eviltester.com/apichallenges](https://eviltester.com/apichallenges)

## How to Play in Multi-user Mode

More information on how to play the challenges in multi-user mode i.e. `apichallenges.eviltester.com` can be found [on the multi-user instructions page](/gui/multiuser):

- [multi-user instructions page](/gui/multiuser)

## Challenges

The [Challenges](/gui/challenges) can be completed by issuing HTTP API requests.

e.g. `GET {{<ORIGIN_URL>}}/todos` would complete the challenge to "GET the list of todos"

You can also `GET {{<ORIGIN_URL>}}/challenges` to get the list of challenges and their status as an API call.

## Books

Recommended books for learning about API Testing are:

- [Automating and Testing a REST API](https://compendiumdev.co.uk/page.php?title=tracksrestapibook)

## More Information

[eviltester.com/apichallenges](https://eviltester.com/apichallenges)

- created by [Alan Richardson](https://www.linkedin.com/in/eviltester/)
- [@EvilTester](https://twitter.com/eviltester)
- [EvilTester.com](https://eviltester.com)
- [github.com/eviltester](https://github.com/eviltester)

## Sponsors

This application is sponsored by [EvilTester.com](https://eviltester.com) - a blog and free online training and books for Software Testing and Development.

For a full list of corporate sponsors and how you can sponsor and support this application check out our [sponsorship page](/sponsors)

Individuals can support this project through [Patreon](https://patreon.com/eviltester), or Github Sponsors