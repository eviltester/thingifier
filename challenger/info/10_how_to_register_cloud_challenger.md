# How to Register as a Cloud Challenger

If you are using the API Challenges locally you don't have to register as a challenger, that is done for you.

But if you want to use the cloud hosted multi-user version then you need to register as a challenger.

And to do that...

## POST /challenger

We need to make an API call to the `/challenger` uri.

In my API testing tool. I will issue a POST request to `https://apichallenges.herokuapp.com/challenger`

- _Issue POST Request_

~~~~~~~~
POST https://apichallenges.herokuapp.com/challenger
~~~~~~~~

And if I receive a `201` response then that means that I have created a challenger session and my data will be stored on the cloud.

- _View response_

~~~~~~~~
Date: Tue, 21 Jul 2020 11:05:21 GMT
X-CHALLENGER: 9a32ef9f-0ebd-4b30-b975-314460bfd1d1
Location: /gui/challenges/9a32ef9f-0ebd-4b30-b975-314460bfd1d1
Content-Type: text/html;charset=utf-8
Transfer-Encoding: chunked
Server: Jetty(9.4.12.v20180830)
~~~~~~~~

The data will be deleted periodically to keep the storage manageable. Probably every few weeks or so.

## Use the `X-CHALLENGER` header

In the response there should be an `X-CHALLENGER` header.

This is my unique session id, and if I use this as a header in all my requests then every challenge I complete will be tracked.

I will make a request to the `/challenges` URI which lets me see the status of the challenges.

And I will add the `X-CHALLENGER` header with the session I was just allocated.

- _Issue GET /challenges with `X-CHALLENGER` header_

~~~~~~~~~
GET https://apichallenges.herokuapp.com/challenges
~~~~~~~~~

## See Status in the GUI

I can see all the challenges in the GUI.

- _visit https://apichallenges.herokuapp.com/gui/challenges_

And if don't have a session then the GUI will tell me.

I can visit the challenges status for my session.

- _visit https://apichallenges.herokuapp.com/gui/challenges/{guid}_

When I do this, my GUID will also be stored in local storage.

This makes it easier for me to remember and restore my session.

## Summary

Basically to track my challenges in multi-user mode.

- `POST /challenger`
- use the `X-CHALLENGER` header in all requests
- _visit https://apichallenges.herokuapp.com/gui/challenges/{guid}_

You don't have to do any of this if you run the application locally in single user mode. But we want to try and make it easy for as many people as possible to experiment without installing anything locally.

The sessions on the cloud will be deleted periodically, so if you session can't be found, then just start as a new challenger.

---

## Video

> How to track your API Challenge status completion in multi-user mode

[youtu.be/XBsM9f9xrhI](https://youtu.be/XBsM9f9xrhI)

The API Challenges application for helping you train and practice in Testing REST APIs requires creating a session for tracking challenges in multi user mode.

- `POST /challenger`
- use the `X-CHALLENGER` header in all requests
- visit https://apichallenges.herokuapp.com/gui/challenges/{guid}

More information on the API Challenges can be found at:

https://www.eviltester.com/apichallenges

---

Ad free version of video released to Patreon supporters: https://www.patreon.com/posts/39770116