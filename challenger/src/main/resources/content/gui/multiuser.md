---
title: API Challenges Multi-User Instructions
description: How to use the API Challenges in multi-user mode to track your progress through the gamified API Testing learning exercises.
---

# Multi-User Help

Getting started with the REST API Challenges can be slightly harder in multi-user mode because you need to tell the API, who you are, so that it can track your individual progress against the challenges.

You don't need to do any of this if you download the application and run it in single user mode.

## Video Explanation

{{<youtube-embed key="XBsM9f9xrhI" title="how to use multi-user mode">}}


## Create a Challenger Session

The first thing we have to do is Create a Challenger Session.

And we do that by issuing a `POST` request against the `/challenger` end point.

If it worked then you should receive a `201` `Created` response. With no body. But with a bunch of headers.

e.g.

~~~~~~~~
Date: Tue, 21 Jul 2020 11:05:21 GMT
X-CHALLENGER: 9a32ef9f-0ebd-4b30-b975-314460bfd1d1
Location: /gui/challenges/9a32ef9f-0ebd-4b30-b975-314460bfd1d1
Content-Type: text/html;charset=utf-8
Transfer-Encoding: chunked
Server: Jetty(9.4.12.v20180830)
~~~~~~~~

When you look at the response to this message you should see an `X-CHALLENGER` header with a GUID.

To track your progress against challenges.

Every request you send to the API should have an `X-CHALLENGER` header with the guid you received.

e.g.

~~~~~~~~
X-CHALLENGER: 9a32ef9f-0ebd-4b30-b975-314460bfd1d1
~~~~~~~~

Also, note in the original response there was a `Location` header.

If you visit that url for this application then you should see the challenges for your `Challenger` session.

e.g.

~~~~~~~~
{{<ORIGIN_URL>}}/gui/challenges/9a32ef9f-0ebd-4b30-b975-314460bfd1d1
~~~~~~~~

Then you should see the status of your challenges.

---

### How to Play Summary

In multi user mode i.e. when using the `apichallenges.eviltester.com` installation, you will need to create an `X-CHALLENGER` session.

Sessions can be stored in localstorage in the browser, or on your local machine (using the API) so you can revisit it and continue your challenges later.

To start a session:

- issue a `POST` request to `{{<ORIGIN_URL>}}/challenges`
- the response status code will be `201` if successful.
- The response will contain an `X-CHALLENGER` header with a unique session value
- Add this header to every request you make on the API, and challenge completion status will be stored against this session.

To view the status of your session:

- issue a `GET` request to `{{<ORIGIN_URL>}}/challenges`
- the response will contain all the challenges and the status of the challenges for your session

To view the status of your session in the GUI:

- visit `{{<ORIGIN_URL>}}/gui/challenges/[unique-session-value]`
- where `[unique-session-value]` is the guid that you were issued as the `X-CHALLENGER` header


## 10 Minutes of data

In multi-user mode the data for the application is reset every 10 minutes, this means that we delete your session data from memory if your session has not been used. Issuing an API request with an X-CHALLENGER header, will keep your data alive in memory.

## Session Persistence

Each challenger session progress is stored on the cloud. The sessions are deleted periodically if they are not used.

You can restore a session's progress, by using the same `X-CHALLENGER` header and if available it will be loaded back into the system and you can continue your challenges.

If you view the challenges page:

- [{{<ORIGIN_URL>}}/gui/challenges](/gui/challenges)

Then your challenger guid should have been stored in the browser local storage, to make it easier for you to find and restart your session.

Even though sessions are stored when the application is not running, so it is still worth making notes about the challenges you have completed so you can track your progress and complete them in various ways, with different tools and by automating.

