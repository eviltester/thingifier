---
date:  2025-01-02T12:14:00Z
title: API Challenges Solution For - Save and Restore Sessions
description: How to solve API challenges for Saving and Restoring Challenge and Data.
showads: true
---

# How to complete the Take a Break Challenges

The API Challenges application has the ability to save and restore the challenge progress and the todo data. These challenges encourage you to track your session and data over time.

The application used to save this automatically to AWS but to keep costs low we encourage doing this locally.

These challenges use the endpoints which allow you to GET and POST the challenge progress data which allows you to save your progress. Additionally save and restore the Todo instance data in case the data is important to your progress.

The UI offers the opportunity to save and restore the data in the browser storage to make the application easier to use, but if you are working with a REST Client or automating then these endpoints can help you save and restore your progress and data.


## Challenger Management

After creating a Challenger using a POST request on the `/challenger` end point, with no body. This creates a new challenger session. The `X-CHALLENGER` header is then used in future requests to track challenge completion. The value of header is the GUID that you can use with the challenger end point.

- GET `/challenger/{guid}` will return the Challenger Progress data in JSON that you can store and use as a payload to restore your progress.
- PUT `/challenger/{guid}` with the JSON progress data as the payload will restore the challenger session and progress in the system. If the Challenger is not already in the system then this will create the challenger and progress. If Challenger is in the system then this will reset their progress to the values in the JSON payload.

To track progress when using these endpoints add the `{guid}` in the `X-CHALLENGER` when you use these endpoints.

## TODO Data Management

After creating a Challenger or restoring a session the TODO database for the user will be reset to the default values. These can be seen using a GET on the `/todos` endpoint.

The `/challenger/database/{guid}` endpoint can be used to save or restore the TODO data. The `guid` is the Challenger GUID used in the `X-CHALLENGER` header.

- GET `/challenger/database/{guid}` will return the todos in a format that can be restored
- PUT `/challenger/database/{guid}` with the payload returned from the `GET` method will restore the todos into memory for future API requests.


To track progress when using these endpoints add the `{guid}` in the `X-CHALLENGER` when you use these endpoints.




